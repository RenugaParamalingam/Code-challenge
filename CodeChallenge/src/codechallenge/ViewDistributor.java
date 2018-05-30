/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package codechallenge;

import static codechallenge.AddDistributor.con;
import static codechallenge.AddDistributor.ps;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Renuga
 */
class ViewDistributor {

    static Connection con;
    static Statement st;
    static PreparedStatement ps;
    static ResultSet rs;

    /* Displays the distributor name list with parent distributor name*/
    public void viewDistributor() {

        System.out.println("Fetching data...");
        DataBase dataBase_obj = new DataBase();
        con = dataBase_obj.dbConnection();
        String test_query = "Select Name, ParentId from distributor";
        String name;
        int parentId;
        System.out.println("----------------------------------------------\n"
                +" Distributor \t\t Parent Distributor \n----------------------------------------------");
        try {
            ps = con.prepareStatement(test_query);
            rs = ps.executeQuery();
            while (rs.next()) {
                name = rs.getString("Name");
                parentId = rs.getInt("ParentId");
                if (parentId == 0) {
                    System.out.printf("\n %-10s %20s", name, "--");
                } else if (parentId > 0) {
                    String query = "Select Name from distributor where Id = ?";
                    try {
                        PreparedStatement pstmt = con.prepareStatement(query);
                        pstmt.setInt(1, parentId);
                        ResultSet rset = pstmt.executeQuery();
                        while (rset.next()) {
                            String parentName = rset.getString("Name");
                            System.out.printf("\n %-10s %25s", name, parentName);
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(ViewDistributor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ViewDistributor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
