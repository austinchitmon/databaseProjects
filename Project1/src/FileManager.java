import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class FileManager {
    DatabaseCreator dbc = new DatabaseCreator();
    Scanner input = new Scanner(System.in);
    String fileName = null;
    RandomAccessFile currentData = null;
    RandomAccessFile currentConfig = null;
    RandomAccessFile currentOverflow = null;
    Record currentRecord = new Record();
    boolean isDatabaseOpen = false;

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

        // Try and create config and data files
        try {
            newConfig = new File(fileName + ".config");
            newData = new File(fileName + ".data");
            if (newConfig.createNewFile() && newData.createNewFile()) {
                System.out.println("File created: " + newConfig.getName());
                System.out.println("File created: " + newData.getName());
                dbc.createNewConfigAndDataFiles(csvFile, fileName);

            } else {
                System.out.println("Data and Config files already exist, stopping database creation...");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        // Try and create overflow file
        try {
            newOverflow = new File(fileName + ".overflow");
            if (newOverflow.createNewFile()) {
                System.out.println("File created: " + newOverflow.getName());
            } else {
                System.out.println("Overflow file already exists, stopping database creation...");
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
      else {
          System.out.println("Error retrieving .csv file");
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
                new RandomAccessFile(dbName + ".overflow", "r");

                // if passed the three statements above, means files already exist, which is good.
                // so now, set 'current' variables to track the opened files.
                //  First, check if another database is already open.
                if(!isDatabaseOpen) {
                    this.currentData = new RandomAccessFile(dbName + ".data", "rw");
                    this.currentConfig = new RandomAccessFile(dbName + ".config", "rw");
                    this.currentOverflow = new RandomAccessFile(dbName + ".overflow", "rw");
                    this.isDatabaseOpen = true;
                    System.out.printf("Database '%s' successfully opened \n \n", dbName);
                } else {
                    System.out.print("Please close the already opened database to open another database. \n \n");
                }

            } catch(FileNotFoundException e) {
                System.out.printf("Error, file not found: %s\nCannot open database. \n \n", e.toString());
            }
    }

    public void closeDatabase() {
        if(noOpenedDatabase()) {
            System.out.print("Error! No database open to close \n\n");
        }
        else {
            currentData = null;
            currentConfig = null;
            currentOverflow = null;
            isDatabaseOpen = false;
            System.out.print("Closing opened database...\n\n");
        }

    }

    public void displayOrUpdateRecord(int operation) throws IOException {
        String operationType = (operation == 4) ? "display" : "update";
        if(noOpenedDatabase()) {
            System.out.printf("Error! No database open to %s record from. \n\n", operationType);
        }
        else {
            System.out.println( this.findRecord(4) );
        }

    }

    public String findRecord(int numRecord) throws IOException {
        String record = "NOT_FOUND";
        if ((numRecord >=1) && (numRecord <= dbc.numRecords))
        {
            currentData.seek(0); // return to the top fo the file
            currentData.skipBytes(numRecord * dbc.recordSize);
            record = currentData.readLine();
        }
        return record;

    }

    public void createReport() {
        if(noOpenedDatabase()) {
            System.out.print("Error! No database open to create report from. \n\n");
        }
        else {
            try {
                FileWriter writer = new FileWriter("report.txt");
                String line;
                for(int i = 0; i<10; i++){
                    if((line = currentData.readLine()) != null) {
                        System.out.println(line);
                        writer.write(line.toCharArray());
                        writer.write("\r\n");
                    }
                    else {
                        System.out.println("End of file reached before 10 records created in report. \n");
                    }
                }
                writer.close();
                currentData.seek(0);
                System.out.print("Creating report...\n\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addRecord() {

    }

    public void deleteRecord() {

    }

    public boolean noOpenedDatabase() {
        return currentOverflow == null || currentConfig == null || currentData == null && isDatabaseOpen;
    }
}
