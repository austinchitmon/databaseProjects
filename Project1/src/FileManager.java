import java.io.*;
import java.util.*;

public class FileManager {
    DatabaseCreator dbc = new DatabaseCreator();
    CurrentDatabase currentDB = new CurrentDatabase();

    OverFlowOperations overflowOp = new OverFlowOperations();
    Scanner input = new Scanner(System.in);
    String fileName = null;

    List<String> parsedRecord = new ArrayList<>();
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
                RandomAccessFile data = new RandomAccessFile(dbName + ".data", "r");
                RandomAccessFile config = new RandomAccessFile(dbName + ".config", "r");
                RandomAccessFile overflow = new RandomAccessFile(dbName + ".overflow", "r");
                data.close();
                config.close();
                overflow.close();
                // if passed the three statements above, means files already exist, which is good.
                // so now, set 'current' variables to track the opened files.
                //  First, check if another database is already open.
                if(!isDatabaseOpen) {
                    this.currentDB = new CurrentDatabase(dbName);
                    this.isDatabaseOpen = true;
                    System.out.printf("Database '%s' successfully opened \n \n", dbName);
                } else
                    System.out.print("Please close the already opened database to open another database. \n \n");

            }
            catch(FileNotFoundException e) {
                System.out.printf("Error, file not found: %s\nCannot open database. \n \n", e.toString());
            }
            catch (IOException e) {
                System.out.printf("Error occurred %s \n", e.toString());
            }
    }

    public void closeDatabase() throws IOException {
        if(noOpenedDatabase()) {
            System.out.print("Error! No database open to close \n\n");
        }
        else {
            this.currentDB.close();
            parsedRecord = new ArrayList<>();
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
            // todo: maybe refactor to search by name
            System.out.printf("Enter the ID (rank if data unmodified) of the record you would like to %s: ", operationType);
            String recordID = input.nextLine();
            currentRecord = this.binarySearch(recordID);
            this.formatFoundRecord(currentRecord);
            if(operationType.equals("update")) {
                this.updateRecord(currentRecord, recordID);
            }
            else if(operationType.equals("delete")) {
                this.deleteOrUpdateRecord(currentRecord, "Deleting", "");
            }

            //reset pointer to current data to the 0th pos.
            this.currentDB.currentData.seek(0);
        }
        catch (Exception e) {
            System.out.printf("Error occurred: %s \n", e.toString());
        }

        // reset necessary trackers specific to displaying record
        this.currentDB.currentFieldsNoSpace = new ArrayList<>();
    }



    public void updateRecord(String currentRecord, String recordId) throws Exception {
        String newRecord;
        System.out.println("Which field would you like to update?");
        for (String word : this.currentDB.currentFieldsNoSpace) System.out.printf("'%s' ", word);
        System.out.print("\nChoice: ");
        String fieldToUpdate = input.nextLine();
        if(this.currentDB.currentFieldsNoSpace.contains(fieldToUpdate)) {
            switch(fieldToUpdate){
                case "ID":
                    throw new Exception("Cannot update ID, as it is the primary key.");
                case "RANK":
                    newRecord = inputUpdatedRecordField(1);
                case "NAME":
                    newRecord = inputUpdatedRecordField(2);
                    break;
                case "CITY":
                    newRecord = inputUpdatedRecordField(3);
                    break;
                case "STATE":
                    newRecord = inputUpdatedRecordField(4);
                    break;
                case "ZIP":
                    newRecord = inputUpdatedRecordField(5);
                    break;
                case "EMPLOYEES":
                    newRecord = inputUpdatedRecordField(6);
                    break;
                default:
                    throw new Exception("Field not present.");
            }

            this.deleteOrUpdateRecord(currentRecord, "Updating", newRecord);
        }
        else {
            throw new Exception("Field not present.");
        }
    }
    public String inputUpdatedRecordField(int pos) throws Exception {
        String replacementValue;
        System.out.print("Enter replacement value: ");
        replacementValue = input.nextLine();
        if(this.currentDB.currentFieldSizes.get(pos) <= replacementValue.length()) {
            System.out.printf("Field size: %d \t Replacement length: %d \n", this.currentDB.currentFieldSizes.get(pos), replacementValue.length());
            throw new Exception("Replacement string length cannot be equal to or greater than maximum field size.");
        }
        else {
            this.parsedRecord.set(pos, replacementValue);
            return formatUpdatedRecordString();
        }
    }

    public String formatUpdatedRecordString() {
        String parsedString = "";

        for(int i = 0; i < this.parsedRecord.size(); i++) {
            String stringToAppend = this.parsedRecord.get(i);
            int remainingEmptyBytesLength = this.currentDB.currentFieldSizes.get(i) - this.parsedRecord.get(i).length();
                stringToAppend = stringToAppend.concat(new String(new char[remainingEmptyBytesLength]).replace('\0', '-'));
                parsedString = parsedString.concat(stringToAppend);
            }

        return parsedString;
    }

    public void formatFoundRecord(String record) {
        for (String word : this.currentDB.currentFields) this.currentDB.currentFieldsNoSpace.add(word.trim());
        record = record.replaceAll("-+",",");
        this.parsedRecord = new ArrayList<String>(Arrays.asList(record.split(",")));

        for(int i = 0; i < this.currentDB.currentFieldsNoSpace.size(); i++) {
            System.out.printf("%s: %s \t", this.currentDB.currentFieldsNoSpace.get(i), this.parsedRecord.get(i));
        }
        System.out.print("\n");
    }

    public void createReport() {
        if(noOpenedDatabase()) {
            System.out.print("Error! No database open to create report from. \n\n");
        }
        else {
            try {
                this.currentDB.currentData.seek(0);
                FileWriter writer = new FileWriter("report.txt");
                String currentFieldsString = "";
                for(String word: this.currentDB.currentFields) currentFieldsString = currentFieldsString.concat(word);
                writer.write(currentFieldsString.toCharArray());
                writer.write("\r\n");
                String line;
                for(int i = 0; i<10; i++){
                    if((line = this.currentDB.currentData.readLine()) != null) {
                        if(!line.contains("MISSING")) {
                            writer.write(line.toCharArray());
                            writer.write("\r\n");
                            i = i - 1;
                        }
                    }
                    else {
                        System.out.println("End of file reached before 10 records created in report. \n");
                    }
                }
                writer.close();
                this.currentDB.currentData.seek(0);
                System.out.print("Creating report...\n\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addRecord() {

        if(noOpenedDatabase()) {
            System.out.print("Error! No database open to add record to. \n\n");
        }
        else {
            try {
                overflowOp = new OverFlowOperations(this.currentDB.currentFields, this.currentDB.currentFieldSizes);
               this.parsedRecord = overflowOp.createNewRecord(this.currentDB.currentConfig, this.currentDB.currentData);

                String parsedString = this.formatUpdatedRecordString();

                String result = overflowOp.addToOverFlowFile(parsedString, this.currentDB);
                if(result.equals("merged")) {
                    this.renameRecordFiles();
                }
            }
            catch (Exception e) {
                System.out.printf("Error occurred: %s \n", e.toString());
            }
        }
    }

    public void renameRecordFiles() throws IOException {
        this.currentDB.currentData.close();
        this.currentDB.currentData = null;
        File oldDataFile = new File("Data.data");

        if(oldDataFile.delete()) {
            System.out.println("Old data deleted");
        }

        File mergedDataFile = new File("temp.data");
        if(mergedDataFile.renameTo(new File(currentDB.currentDBName+".data"))) {
            System.out.println("successfully renamed");
        }
        this.currentDB.currentData = new RandomAccessFile(currentDB.currentDBName+".data", "rw");
        //todo update config file with new record num
    }

    public void deleteOrUpdateRecord(String currentRecord, String operation, String newRecord) throws Exception {
        // seek to record
        String line;
        this.currentDB.currentData.seek(0);
        while(( line = this.currentDB.currentData.readLine()) != null && !line.contains(currentRecord)) { }

        //prev while statement ends the file pointer at the NEXT file, so seek to the one before it
        this.currentDB.currentData.seek(this.currentDB.currentData.getFilePointer() - this.currentDB.currentRecordSize);


        if(operation.equals("Deleting")) {
            String replacementString = "MISSING";
            replacementString = replacementString.concat(new String(new char[this.currentDB.currentRecordSize - replacementString.length() -2]).replace('\0', '-'));
            this.currentDB.updateConfigWithDeletedID(currentRecord);

            this.currentDB.currentData.writeBytes(replacementString + "\r\n");
        }
        else if(operation.equals("Updating")) {
            this.currentDB.currentData.writeBytes(newRecord + "\r\n");
        }

        System.out.printf("%s the above record... \n\n", operation);
    }

    public boolean noOpenedDatabase() {
        return this.currentDB.currentOverflow == null || this.currentDB.currentConfig == null || this.currentDB.currentData == null && isDatabaseOpen;
    }

    public  String binarySearch(String id) throws Exception
    {
        int Low = 0;
        int High = this.currentDB.currentNumRecords;
        int Middle;
        String MiddleId;
        String record = "NOT_FOUND";
        boolean Found = false;

        while (!Found && (High >= Low))
        {
            Middle = (Low + High) / 2;
            if(Middle == 0) {
                record = this.findRecord(Middle + 1);
            }
            else {
                record = this.findRecord(Middle);
            }
            while(record.contains("MIS")) {
                record = this.findRecord(Middle+1);
            }
            MiddleId = record.substring(0,5).replaceAll("-+","");;

            int result = Integer.parseInt(MiddleId) - Integer.parseInt(id);
            if (result == 0)   // ids match
                Found = true;
            else if (result > 0)
                High = Middle - 1;
            else
                Low = Middle + 1;
        }
        if (Found)
            return record;
        else
            return "NOT_FOUND";
    }
    public String findRecord(int numRecord) throws Exception {
        if ((numRecord >=1) && (numRecord <= this.currentDB.currentNumRecords)) {
            this.currentDB.currentData.seek(0); // return to the top fo the file
            this.currentDB.currentData.skipBytes((numRecord - 1) * this.currentDB.currentRecordSize);
            return this.currentDB.currentData.readLine();
        }
        else {
            throw new Exception("Out of bounds");
        }
    }
}
