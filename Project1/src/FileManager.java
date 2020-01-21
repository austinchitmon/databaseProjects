import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class FileManager {
    DatabaseCreator dbc = new DatabaseCreator();
    Scanner input = new Scanner(System.in);
    String fileName = null;
    RandomAccessFile currentData = null;
    RandomAccessFile currentConfig = null;
    RandomAccessFile currentOverflow = null;
    int currentNumRecords = 0;
    int currentRecordSize = 0;
    List<String> currentFields = new ArrayList<>();
    List<String> currentFieldsNoSpace = new ArrayList<>();
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

                    //get number of records, field names, and record size of the currently opened database file.
                    String line;
                    while((line = this.currentConfig.readLine()) != null) {
                        String[] values = line.split(",");
                        switch(values[0]) {
                            case "NUMRECORDS":
                                this.currentNumRecords = Integer.parseInt(values[1]);
                                break;
                            case "RECORDSIZE":
                                this.currentRecordSize = Integer.parseInt(values[1]);
                                break;
                            case "ID":
                            case "RANK":
                            case "NAME":
                            case "CITY":
                            case "ZIP":
                            case "EMPLOYEES":
                            case "STATE":
                                // in the currentFields array, add each field AND the remaining empty spaces for the field
                                // this is so when we create the report, everything lines up nicely
                                this.currentFields.add(values[0] + new String(new char[(Integer.parseInt(values[1]) - values[0].length())]).replace('\0', ' '));
                            default:
                        }
                    }

                    System.out.printf("Database '%s' successfully opened \n \n", dbName);
                } else {
                    System.out.print("Please close the already opened database to open another database. \n \n");
                }

            } catch(FileNotFoundException e) {
                System.out.printf("Error, file not found: %s\nCannot open database. \n \n", e.toString());
            } catch (IOException e) {
                System.out.printf("Error occurred %s \n", e.toString());
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
            currentRecordSize = 0;
            currentNumRecords = 0;
            currentFields = new ArrayList<>();
            currentFieldsNoSpace = new ArrayList<>();
            isDatabaseOpen = false;
            System.out.print("Closing opened database...\n\n");
        }

    }

    public void displayUpdateOrDeleteRecord(int operation) throws IOException {
        String operationType = "";
        String currentRecord = null;

        switch(operation) {
            case 4:
                operationType = "display";
                break;
            case 5:
                operationType = "update";
                break;
            case 8:
                operationType = "delete";
                break;
        }

        try {
            if(noOpenedDatabase()) {
                throw new Exception("No open Database to operate on");
            }
            System.out.print("Enter the ID (rank if data unmodified) of the record you would like to find: ");
            int numRecord = Integer.parseInt(input.nextLine());
            currentRecord = this.findRecord(numRecord);
            if(currentRecord.contains("MISSING")) {
                throw new Exception("The record you searched for has been previously deleted.");
            }
            this.formatFoundRecord(currentRecord);
            if(operationType.equals("update")) {
                this.updateRecord(currentRecord, numRecord - 1);
            }
            else if(operationType.equals("delete")) {
                this.deleteRecord(currentRecord, numRecord - 1);
            }

            //reset pointer to current data to the 0th pos.
            this.currentData.seek(0);
        }
        catch (Exception e) {
            System.out.printf("Error occurred: %s \n", e.toString());
        }

        // reset necessary trackers specific to displaying record
        this.currentFieldsNoSpace = new ArrayList<>();
    }

    public String findRecord(int numRecord) throws Exception {
        if ((numRecord >=1) && (numRecord <= this.currentNumRecords)) {
            currentData.seek(0); // return to the top fo the file
            currentData.skipBytes((numRecord - 1) * this.currentRecordSize);
            return currentData.readLine();
        }
        else {
            throw new Exception("Out of bounds");
        }
    }

    public void updateRecord(String currentRecord, int numRecord) throws Exception {
        System.out.println("Which field would you like to update?");
        for (String word : this.currentFieldsNoSpace) System.out.printf("'%s' ", word);
        System.out.print("\nChoice: ");
        String fieldToUpdate = input.nextLine();
        if(this.currentFieldsNoSpace.contains(fieldToUpdate)) {
            if(fieldToUpdate.equals("ID")) {
                throw new Exception("Cannot update ID, as it is the primary key.");
            }
            // TODO: Have field they want to update, now update the field
        }
        else {
            throw new Exception("Field not present.");
        }
    }

    public void formatFoundRecord(String record) {
        List<String> parsedRecord = new ArrayList<>();
        for (String word : this.currentFields) this.currentFieldsNoSpace.add(word.trim());
        record = record.replaceAll("-+",",");
        parsedRecord = new ArrayList<String>(Arrays.asList(record.split(",")));

        for(int i = 0; i < currentFieldsNoSpace.size(); i++) {
            System.out.printf("%s: %s \t", this.currentFieldsNoSpace.get(i), parsedRecord.get(i));
        }
        System.out.print("\n");
    }

    public void createReport() {
        if(noOpenedDatabase()) {
            System.out.print("Error! No database open to create report from. \n\n");
        }
        else {
            try {
                this.currentData.seek(0);
                FileWriter writer = new FileWriter("report.txt");
                String currentFieldsString = "";
                for(String word: currentFields) currentFieldsString = currentFieldsString.concat(word);
                writer.write(currentFieldsString.toCharArray());
                writer.write("\r\n");
                String line;
                for(int i = 0; i<10; i++){
                    if((line = currentData.readLine()) != null) {
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

    public void deleteRecord(String currentRecord, int numRecord) throws Exception {
        String replacementString = "MISSING";
        replacementString = replacementString.concat(new String(new char[this.currentRecordSize - replacementString.length() -2]).replace('\0', '-'));
        System.out.println(replacementString);
        this.currentData.seek(0);
        currentData.skipBytes((numRecord) * this.currentRecordSize);
        currentData.writeBytes(replacementString + "\r\n");
        System.out.print("Deleting the above record... \n\n");
    }

    public boolean noOpenedDatabase() {
        return currentOverflow == null || currentConfig == null || currentData == null && isDatabaseOpen;
    }
}
