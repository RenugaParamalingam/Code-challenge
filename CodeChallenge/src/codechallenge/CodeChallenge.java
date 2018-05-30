/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package codechallenge;

import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author Renuga
 */
public class CodeChallenge {

    static Scanner s = new Scanner(System.in);

    public static void main(String[] args) throws IOException {

        do {
            System.out.println("\t\t\t <----- Distribution Management System  -----> \n");
            System.out.println(" 1. Add Distributor \n 2. View Distributor \n 3. Check Permissions \n 4. Quit"
                    + "\n Enter your choice: ");
            int choice = s.nextInt();
            switch (choice) {
                case 1:
                    AddDistributor AddDistributor_obj = new AddDistributor();
                    AddDistributor_obj.addDistributor();
                    break;
                case 2:
                    ViewDistributor ViewDistributor_obj = new ViewDistributor();
                    ViewDistributor_obj.viewDistributor();
                    break;

                case 3:
                    CheckPermissions CheckPermissions_obj = new CheckPermissions();
                    CheckPermissions_obj.getPermissions();
                    break;

                case 4:
                    System.out.println("Terminating..");
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid Choice");
            }
            System.out.println("\n Continue : yes/no");
            String yn = s.next();
            if (yn.startsWith("n")) {
                break;
            }
        } while (true);

    }

}
