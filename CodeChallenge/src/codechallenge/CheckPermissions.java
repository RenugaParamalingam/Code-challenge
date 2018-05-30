/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package codechallenge;

import static codechallenge.AddDistributor.ps;
import static codechallenge.DataBase.con;
import static codechallenge.ViewDistributor.con;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Renuga
 */
class CheckPermissions {

    Scanner s = new Scanner(System.in);
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    static Connection con;
    static Statement st;
    static PreparedStatement ps;
    static ResultSet rs;
    DataBase dataBase_obj = new DataBase();

    /* Gets the distributor name and location to check valid permission*/
    public void getPermissions() throws IOException {
        ArrayList<String> includeList = new ArrayList<String>();
        ArrayList<String> excludeList = new ArrayList<String>();
        System.out.println("Distributor Name: ");
        String distributorName = br.readLine();
        distributorName = validateInput(distributorName);
        System.out.println(" * Please Enter Location following the order City Name, State Name, Country Name separated by `-` (not case sensitive)\n"
                        + " * Use space for name like Tamil Nadu\n"
                        + " * Use ',' to separate multipleLocation Example: France,Japan \n"
                        + " * Sample 1: CHENNAI-TAMIL nadu-INDIA \n   Sample 2: Tamil Nadu-India \n   Sample 3: India \n");
        System.out.println("Location: ");
        String location = br.readLine();
        location = validateInput(location);
        if (location.contains("-")) {
            location = location.substring(0, location.indexOf("-"));
        }
        con = dataBase_obj.dbConnection();

        String query = "Select Include,Exclude,ParentId from distributor where Name = ?";

        try {
            ps = con.prepareStatement(query);
            ps.setString(1, distributorName);
            rs = ps.executeQuery();
            if (!rs.isBeforeFirst()) {
                System.out.println("InValid Distributor Name ");
            } else {
                while (rs.next()) {

                    String include = rs.getString("Include");
                    if (include.contains(",")) {
                        String[] array = include.split(",");
                        includeList.addAll(Arrays.asList(array));
                    } else {
                        includeList.add(include);
                    }
                    String exclude = rs.getString("Exclude");
                    if (exclude.contains(",")) {
                        String[] array = exclude.split(",");
                        excludeList.addAll(Arrays.asList(array));
                    } else {
                        excludeList.add(exclude);
                    }
                    int parentId = rs.getInt("ParentId");
                    if (parentId == 0) {
                        checkPermission(excludeList, location, "Exclude");
                        checkPermission(includeList, location, "Include");
                    } else {
                        while (parentId != 0) {
                            try {
                                PreparedStatement pstmt = con.prepareStatement("Select Include,Exclude,ParentID from distributor where Id = ?");
                                pstmt.setInt(1, parentId);
                                ResultSet rset = pstmt.executeQuery();
                                while (rset.next()) {
                                    String parentInclude = rset.getString("Include");
                                    parentId = rset.getInt("ParentId");
                                    includeList.add(parentInclude);
                                    String parentExclude = rset.getString("Exclude");
                                    excludeList.add(parentExclude);
                                }
                            } catch (SQLException ex) {
                                Logger.getLogger(CheckPermissions.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        checkPermission(excludeList, location, "Exclude");
                        checkPermission(includeList, location, "Include");
                    }

                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(AddDistributor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /* Splits the location name and displays whether permission is valid or not */
    public void checkPermission(ArrayList<String> rawList, String location, String id) {
        int flag = 0;
        ArrayList<String> locationList = new ArrayList<String>();

        for (String str : rawList) {

            if (str.contains("-")) {
                locationList.add(str.substring(0, str.indexOf("-")));

            } else if (str.contains(",")) {
                String[] array = str.split(",");
                locationList.addAll(Arrays.asList(array));

            } else {
                locationList.add(str);

            }
        }
        for (String llist : locationList) {
            String result = fetchData(location, llist);
            if ((result.equals("Valid Permission")) && (id.equals("Include"))) {
                System.out.println("Valid Permission ");
                flag = 1;
                break;
            }

            if ((location.equalsIgnoreCase(llist)) && (id.equals("Include"))) {
                System.out.println("Valid Permission ");
                flag = 1;
                break;
            }

        }
        if (flag == 0 && (id.equals("Include"))) {
            System.out.println("InValid Permission ");
        }
    }

    /* Fetch data from server and returns result as valid or not*/
    public String fetchData(String input, String location) {
        String valid = "Valid Permission";
        String Invalid = "Invalid Permission";
        con = dataBase_obj.dbConnection();
        // if input is country -> checks cityName and stateName
        String countryQuery = "SELECT CityName, ProvinceName FROM cities WHERE CountryName LIKE  ? ";
        try {
            PreparedStatement pstmt = con.prepareStatement(countryQuery);
            pstmt.setString(1, "%" + location + "%");
            ResultSet rset = pstmt.executeQuery();

            while (rset.next()) {
                String CityName = rset.getString("CityName");
                CityName = CityName.replaceFirst("\\s++$", "");
                String ProvinceName = rset.getString("ProvinceName");
                ProvinceName = ProvinceName.replaceFirst("\\s++$", "");
                if (input.equalsIgnoreCase(CityName) || input.equalsIgnoreCase(ProvinceName)) {
                    return valid;
                }

            }
        } catch (SQLException ex) {
            Logger.getLogger(CheckPermissions.class.getName()).log(Level.SEVERE, null, ex);
        }
        // if input is state -> checks for cityname
        String stateQuery = "SELECT CityName, ProvinceName FROM cities WHERE ProvinceName LIKE  ? ";
        try {
            PreparedStatement pstmt = con.prepareStatement(stateQuery);
            pstmt.setString(1, "%" + location + "%");
            ResultSet rset = pstmt.executeQuery();

            while (rset.next()) {
                String CityName = rset.getString("CityName");
                CityName = CityName.replaceFirst("\\s++$", "");
                String ProvinceName = rset.getString("ProvinceName");
                ProvinceName = ProvinceName.replaceFirst("\\s++$", "");
                if (input.equalsIgnoreCase(CityName) || input.equalsIgnoreCase(ProvinceName)) {
                    return valid;
                }

            }

        } catch (SQLException ex) {
            Logger.getLogger(CheckPermissions.class.getName()).log(Level.SEVERE, null, ex);
        }

        return Invalid;
    }

    /* Validates the user input */
    public String validateInput(String input) throws IOException {
        while (input == null || input.trim().isEmpty()) {
            System.out.println("Input cannot be null or whitespace !  Try Again!");
            input = br.readLine();
        }
        return input;
    }
}
