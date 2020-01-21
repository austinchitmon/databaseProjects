// Created by: Austin Chitmon
// Purpose: Basic file-based database manager

import java.io.IOException;
import java.util.Scanner;

public class Driver {
    public static void main(String[] args) throws IOException {
        Scanner input = new Scanner(System.in);
        FileManager fileManager = new FileManager();
        boolean alive = true;

        while(alive) {
            int operation = 0;
            boolean hasError = false;
            System.out.print(
                    "Select a database operation: \n" +
                            "1) Create new database\n" +
                            "2) Open database\n" +
                            "3) Close database\n" +
                            "4) Display record\n" +
                            "5) Update record\n" +
                            "6) Create report\n" +
                            "7) Add record\n" +
                            "8) Delete record\n" +
                            "9) Quit \n");
            do {
                try {
                    System.out.print("Enter your option: ");
                    operation = Integer.parseInt(input.nextLine());
                    if(operation > 9 || operation < 1) {
                        throw new Exception("Number out of range");
                    }
                    hasError = false;
                } catch (Exception e) {
                    System.out.printf("Error occurred %s \n", e.toString());
                    hasError = true;
                }
            } while(hasError);

            switch(operation){
                case 1:
                    fileManager.createNewDatabase();
                    break;
                case 2:
                    fileManager.openDatabase();
                    break;
                case 3:
                    fileManager.closeDatabase();
                    break;
                case 4:
                case 5:
                case 8:
                    fileManager.displayUpdateOrDeleteRecord(operation);
                    break;
                case 6:
                    fileManager.createReport();
                    break;
                case 7:
                    fileManager.addRecord();
                    break;
                case 9:
                    System.out.print("Closing... \n");
                    alive = false;
                    break;
            }
        }
    }
}
