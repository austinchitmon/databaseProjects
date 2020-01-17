import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class FileManager {
    String[] fields = {"RANK", "NAME", "CITY", "STATE", "ZIP", "EMPLOYEES"};
    Scanner input = new Scanner(System.in);
    String fileName = null;
    RandomAccessFile currentData = null;
    RandomAccessFile currentConfig = null;
    RandomAccessFile currentOverflow = null;
    Record currentRecord = new Record();

    public RandomAccessFile getCSVFileToCreateDatabase() {
        RandomAccessFile csvFile = null;
            try {
                System.out.print("Enter name of .csv file to create DB from (omit extension): ");
                fileName = input.nextLine();
                csvFile = new RandomAccessFile(fileName + ".csv", "r");
            } catch(FileNotFoundException e) {
                System.out.printf("Error, file not found: %s \n Aborting creation of database... \n \n", e.toString());
                return null;
            }
        return csvFile;
    }

    public void createNewFiles(RandomAccessFile csvFile) {
        File newConfig;
        File newData;
        File newOverflow;

        // Try and create config file
        try {
            newConfig = new File(fileName + ".config");
            if (newConfig.createNewFile()) {
                System.out.println("File created: " + newConfig.getName());
                // TODO: import data to config file
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        // Try and create data file
        try {
            newData = new File(fileName + ".data");
            if (newData.createNewFile()) {
                System.out.println("File created: " + newData.getName());
                // TODO: import data to data file
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        // Try and create overflo file
        try {
            newOverflow = new File(fileName + ".overflo");
            if (newOverflow.createNewFile()) {
                System.out.println("File created: " + newOverflow.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void createNewDatabase() {
      RandomAccessFile csvFile = getCSVFileToCreateDatabase();

      //previous function returns null if csv file not found.
      if(csvFile != null) {
          createNewFiles(csvFile);
      }
    }

    public void openDatabase() {
        String dbName = null;
            try {
                System.out.print("Enter name of database to open (omit extension): ");
                dbName = input.nextLine();

                // Use read only to see if the file exists. Without these 3 statements,
                // program would just create new files because of the writing options.
                // throws FileNotFoundException if files aren't already created.
                new RandomAccessFile(dbName + ".data", "r");
                new RandomAccessFile(dbName + ".config", "r");
                new RandomAccessFile(dbName + ".overflo", "r");

                // if passed the three statements above, means files already exist, which is good.
                // so now, set 'current' variables to track the opened files.
                currentData = new RandomAccessFile(dbName + ".data", "rw");
                currentConfig = new RandomAccessFile(dbName + ".config", "rw");
                currentOverflow = new RandomAccessFile(dbName + ".overflo", "rw");
            } catch(FileNotFoundException e) {
                System.out.printf("Error, file not found: %s \n Cannot open database. \n \n", e.toString());
                return;
            }
        System.out.printf("Database %s successfully opened \n \n", dbName);
    }

    public void closeDatabase() {
        if(currentOverflow == null || currentConfig == null || currentData == null) {
            System.out.print("Error! No database open to close \n\n");
        }
        else {
            currentData = null;
            currentConfig = null;
            currentOverflow = null;
            System.out.print("Closing opened database...\n\n");
        }

    }

    public void displayRecord() {

    }

    public void updateRecord() {

    }

    public void createReport() {

    }

    public void addRecord() {

    }

    public void deleteRecord() {

    }



}
