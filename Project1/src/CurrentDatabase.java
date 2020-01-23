import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class CurrentDatabase {

    public RandomAccessFile currentData = null;
    public RandomAccessFile currentConfig = null;
    public RandomAccessFile currentOverflow = null;
    public int currentNumRecords = 0;
    public int currentRecordSize = 0;
    public String currentDBName;
    public List<Integer> currentFieldSizes = new ArrayList<>();
    public List<String> currentFields = new ArrayList<>();
    public List<String> currentFieldsNoSpace = new ArrayList<>();


    public CurrentDatabase(String databaseName) throws IOException {
        this.currentData = new RandomAccessFile(databaseName + ".data", "rw");
        this.currentConfig = new RandomAccessFile(databaseName + ".config", "rw");
        this.currentOverflow = new RandomAccessFile(databaseName + ".overflow", "rw");
        this.currentDBName = databaseName;
        this.setNumRecordsFieldNamesRecordSize();
    }

    public CurrentDatabase() {
        this.currentConfig = null;
        this.currentData = null;
        this.currentOverflow = null;
        this.currentRecordSize = 0;
        this.currentNumRecords = 0;
        this.currentFields = new ArrayList<>();
        this.currentFieldsNoSpace = new ArrayList<>();
        this.currentFieldSizes = new ArrayList<>();
    }

    public void setNumRecordsFieldNamesRecordSize() throws IOException {
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
                    this.currentFieldSizes.add(Integer.parseInt(values[1]));
                    this.currentFields.add(values[0] + new String(new char[(Integer.parseInt(values[1]) - values[0].length())]).replace('\0', ' '));
                default:
            }
        }
    }
    public void close() throws IOException {
        this.currentData.close();
        this.currentConfig.close();
        this.currentOverflow.close();
        this.currentConfig = null;
        this.currentData = null;
        this.currentOverflow = null;
        this.currentRecordSize = 0;
        this.currentNumRecords = 0;
        this.currentFields = new ArrayList<>();
        this.currentFieldsNoSpace = new ArrayList<>();
        this.currentFieldSizes = new ArrayList<>();
    }

    public void updateConfigWithDeletedID(String record) throws IOException {
        String line = "";
        String newLine = "";
        String id = (record.substring(0, 4).replaceAll("-+", ""));
        currentConfig.seek(0);
        while ((line = this.currentConfig.readLine()) != null) {
            if (line.contains("DELETEDIDS,")) {
                newLine = line.concat("," + id);
            }
        }
        currentConfig.seek(0);
        while ((line = this.currentConfig.readLine()) != null) {
            if (line.contains("NEXTID")) {
                this.currentConfig.writeBytes(newLine);
            }
        }
        this.currentConfig.seek(0);
    }
}
