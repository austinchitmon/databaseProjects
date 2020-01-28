import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class DatabaseCreator {

    List<Integer> numBytesForEachField;
    int recordSize;
    int numRecords;
    int idIncrementer;

    DatabaseCreator() {
        this.numBytesForEachField = new ArrayList<>();
        this.recordSize = 0;
        this.numRecords = 0;
        this.idIncrementer = 1;
    }

    public void createNewConfigAndDataFiles(RandomAccessFile csvFile, String fileName) throws IOException {
        RandomAccessFile newConfigFile = new RandomAccessFile(fileName + ".config", "rw");
        RandomAccessFile newDatabaseFile = new RandomAccessFile(fileName + ".data", "rw");
        String line = null;
        while((line = csvFile.readLine()) != null) {

            // get rid of BOM
            line = line.replace("\u00EF\u00BB\u00BF", "");

            String[] values = line.split(",");

            //Operations on first line that contains field keys
            if(numRecords == 0) {
                this.createConfigFileFields(values, newConfigFile);
            }
            // when not first line, you are reading a record
            else {
                this.createDataFile(values, newDatabaseFile);
            }
            numRecords = numRecords + 1;
        }
        numRecords = numRecords - 1;
        newConfigFile.writeBytes("NUMRECORDS," + (numRecords) + "\n");

        //add number of bytes (chars) for each field together to find total record size for each record.
        for (int num : numBytesForEachField) recordSize = recordSize + num;
        // +2 to account for the /n/r escape characters
        // +1 if on linux
        newConfigFile.writeBytes("RECORDSIZE," + (recordSize + 1) + "\n");
        newConfigFile.writeBytes("NEXTID," + (numRecords + 1) + "\n");
        newConfigFile.writeBytes("DELETEDIDS");

        newDatabaseFile.close();
        newConfigFile.close();
    }

    public void createConfigFileFields(String[] values, RandomAccessFile newConfigFile) throws IOException {

        // configure ID field
        newConfigFile.writeBytes("ID,"+"5\n");
        this.numBytesForEachField.add(5);

        for(int i = 0; i < values.length; i++) {
            newConfigFile.writeBytes(values[i] + ",");
            switch(values[i]){
                case "NAME":
                    newConfigFile.writeBytes("45\n");
                    this.numBytesForEachField.add(45);
                    break;
                case "CITY":
                    newConfigFile.writeBytes("20\n");
                    this.numBytesForEachField.add(20);
                    break;
                case "RANK":
                case "STATE":
                case "ZIP":
                    newConfigFile.writeBytes("6\n");
                    this.numBytesForEachField.add(6);
                    break;
                case "EMPLOYEES":
                    newConfigFile.writeBytes("10\n");
                    this.numBytesForEachField.add(10);
                    break;
                default:
                    newConfigFile.writeBytes("UNDEFINED\n");
            }
        }
    }

    public void createDataFile(String[] values, RandomAccessFile newDatabaseFile) throws IOException {

        // increment ID and write ID field
        newDatabaseFile.writeBytes(Integer.toString(this.idIncrementer));
        int remainingEmptyBytesLength = numBytesForEachField.get(0) - String.valueOf(this.idIncrementer).length();
        String emptyString = new String(new char[remainingEmptyBytesLength]).replace('\0', '-');
        newDatabaseFile.writeBytes(emptyString);



        // write each other record field
        // use get(i + 1) beacuse first field in numBytesForEachField is size of ID.
        for(int i = 0; i < values.length; i++) {
            newDatabaseFile.writeBytes(values[i]);
            remainingEmptyBytesLength = numBytesForEachField.get(i + 1) - values[i].length();
            if (remainingEmptyBytesLength > 0) {
                emptyString = new String(new char[remainingEmptyBytesLength]).replace('\0', '-');
                newDatabaseFile.writeBytes(emptyString);
            }
        }
        newDatabaseFile.writeBytes( "\n");
        this.idIncrementer = this.idIncrementer + 1;
    }
}
