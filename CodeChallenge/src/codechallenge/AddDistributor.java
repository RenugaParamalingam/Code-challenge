/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package codechallenge;

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
public class AddDistributor {

    Scanner s = new Scanner(System.in);
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    static Connection con;
    static Statement st;
    static PreparedStatement ps;
    static ResultSet rs;
    int parentId;
    DataBase dataBase_obj = new DataBase();

    /* add distributor data to server until valid data is entered */
    public void addDistributor() throws IOException {
        System.out.println(" 1. Direct Distributor \n 2. Sub Distributor");
        int distributorChoice;
        distributorChoice = s.nextInt();
        con = dataBase_obj.dbConnection();
        switch (distributorChoice) {
            case 1:
                parentId = 0;
                System.out.println("Distributor Name: ");
                String name = br.readLine();
                name = validateInput(name);
                name = checkDistributorName(name);
                System.out.println(" * Please Enter Location following the order City Name, State Name, Country Name separated by `-` (not case sensitive)\n"
                        + " * Use space for name like Tamil Nadu\n"
                        + " * Use ',' to separate multipleLocation Example: France,Japan \n"
                        + " * Sample 1: CHENNAI-TAMIL nadu-INDIA \n   Sample 2: Tamil Nadu-India \n   Sample 3: India \n");
                System.out.println("Include: ");
                String include = br.readLine();
                include = validateInput(include);
                include = splitLocaion(include);
                System.out.println("Exclude: ");
                String exclude = br.readLine();
                exclude = validateInput(exclude);
                while (exclude.equalsIgnoreCase(include)) {
                    System.out.println("Include and Exclude must not be same.. Try again");
                    exclude = br.readLine();
                }
                exclude = splitLocaion(exclude);

                try {
                    String query = "insert into distributor(Name, Include, Exclude, ParentID)" + "values(?, ?, ?, ?)";
                    ps = con.prepareStatement(query);
                    ps.setString(1, name);
                    ps.setString(2, include);
                    ps.setString(3, exclude);
                    ps.setInt(4, parentId);
                    ps.execute();
                } catch (Exception ex) {
                    System.out.println("E:" + ex);
                }
                System.out.println("Distributor Added Successfuly");
                break;

            case 2:
                System.out.println("Parent Distributor Name: ");
                String parentName = br.readLine();
                parentName = validateInput(parentName);
                parentId = getParentId(parentName);
                System.out.println("Sub Distributor Name: ");
                String subName = br.readLine();
                subName = validateInput(subName);
                subName = checkDistributorName(subName);
                System.out.println(" * Please Enter Location following the order City Name, State Name, Country Name separated by `-` (not case sensitive)\n"
                        + " * Use space for name like Tamil Nadu\n"
                        + " * Use ',' to separate multipleLocation Example: France,Japan \n"
                        + " * Sample 1: CHENNAI-TAMIL nadu-INDIA \n   Sample 2: Tamil Nadu-India \n   Sample 3: India \n");
                System.out.println("Include: ");
                String sub_include = br.readLine();
                sub_include = validateInput(sub_include);
                sub_include = splitLocaion(sub_include);
                System.out.println("Exclude: ");
                String sub_exclude = br.readLine();
                sub_exclude = validateInput(sub_exclude);
                while (sub_exclude.equalsIgnoreCase(sub_include)) {
                    System.out.println("Include and Exclude must not be same.. Try again");
                    sub_exclude = br.readLine();
                }
                sub_exclude = splitLocaion(sub_exclude);
                try {
                    String insert_sub_query = "insert into distributor(Name, Include, Exclude, ParentID)" + "values(?, ?, ?, ?)";
                    ps = con.prepareStatement(insert_sub_query);
                    ps.setString(1, subName);
                    ps.setString(2, sub_include);
                    ps.setString(3, sub_exclude);
                    ps.setInt(4, parentId);
                    ps.execute();
                } catch (Exception ex) {
                    System.out.println("E:" + ex);
                }
                System.out.println("Distributor Added Successfuly");
                break;

            default:
                System.out.println("Invalid Choice");
        }

    }

    /*checks whether the entered distributor name is already registered*/
    public String checkDistributorName(String testName) throws IOException {
        int flag;
        do {
            flag = 0;
            String test_query = "Select Name from distributor";
            try {
                ps = con.prepareStatement(test_query);
                rs = ps.executeQuery();
                while (rs.next()) {
                    String tempName = rs.getString("Name");
                    if (tempName.equalsIgnoreCase(testName)) {
                        System.out.println("Distributor name already registered.. Try with new!");
                        flag = 1;
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(AddDistributor.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (flag == 0) {
                return testName;
            } else {
                System.out.println("Distributor Name: ");
                testName = br.readLine();
            }
        } while (true);

    }

    /* Splits the location by hyphen and comma */
    public String splitLocaion(String location) throws IOException {

        ArrayList<Integer> flagArray = new ArrayList<Integer>();
        do {
            if (location.contains("-") || location.contains(",")) {
                String[] array = location.split("-|\\,");
                for (String str : array) {
                    int temp = checkLocationName(str);
                    flagArray.add(temp);
                }
            } else {
                int temp = checkLocationName(location);
                flagArray.add(temp);
            }
            if (flagArray.contains(0)) {
                flagArray.clear();
                System.out.println("InValid Location name \n Try again: ");
                location = br.readLine();
            } else {
                return location;
            }
        } while (true);
    }

    /* Validates the input location with location stored in server*/
    public int checkLocationName(String include) throws IOException {

        con = dataBase_obj.dbConnection();
        int flag = 0;
        ResultSet rs;
        PreparedStatement stmt;
        String validate = "SELECT * FROM cities WHERE CONCAT(CityName, '', ProvinceName, '', CountryName) LIKE  ? ";
        try {
            stmt = con.prepareStatement(validate);
            stmt.setString(1, "%" + include + "%");
            rs = stmt.executeQuery();
            while (rs.next()) {
                String CityName = rs.getString("CityName");
                CityName = CityName.replaceFirst("\\s++$", "");
                String ProvinceName = rs.getString("ProvinceName");
                ProvinceName = ProvinceName.replaceFirst("\\s++$", "");
                String CountryName = rs.getString("CountryName");
                CountryName = CountryName.replaceFirst("\\s++$", "");
                if (include.equalsIgnoreCase(CityName) || include.equalsIgnoreCase(ProvinceName) || include.equalsIgnoreCase(CountryName)) {
                    flag = 1;
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(AddDistributor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return flag;

    }

    /* Prevents the user by entering invalid string*/
    public String validateInput(String input) throws IOException {
        while (input == null || input.trim().isEmpty()) {
            System.out.println("Input cannot be null or whitespace !  Try Again!");
            input = br.readLine();
        }
        return input;
    }

    /* Fetches parent id from server*/
    public int getParentId(String parentName) throws IOException {
        int id = 0, flag = 0;
        String query = "Select Id from distributor where Name = ?";
        try {
            ps = con.prepareStatement(query);
            ps.setString(1, parentName);
            rs = ps.executeQuery();
            if (!rs.next()) {

                flag = 1;
            }
            while (rs.next()) {
                id = rs.getInt("Id");
            }

        } catch (SQLException ex) {
            Logger.getLogger(AddDistributor.class.getName()).log(Level.SEVERE, null, ex);
        }
        while (flag == 1) {
            System.out.println("No such Distributor.. Enter valid Distributor name");
            String name = br.readLine();
            name = validateInput(name);
            flag = 0;
            id = getParentId(name);
        }
        return id;
    }
}
