import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class OverFlowOperations {
    List<String> fields;
    List<Integer> currentFieldSizes;
    String fieldsAsString;
    final int NUM_FILES_FOR_MERGE = 5;
    Scanner input = new Scanner(System.in);

    OverFlowOperations(List<String> currentFields, List<Integer> currentFieldSizes) {
        this.fields = new ArrayList<>();
        this.currentFieldSizes = currentFieldSizes;
        this.fieldsAsString = "";
        for (String word : currentFields) this.fields.add(word.trim());
        this.fields.remove(0);
        for (String word : this.fields) fieldsAsString = fieldsAsString + word + " ";

    }

    OverFlowOperations() {
        this.fields = new ArrayList<>();
        this.currentFieldSizes = new ArrayList<>();
        this.fieldsAsString = "";
    }


    public List<String> createNewRecord(RandomAccessFile currentConfig, RandomAccessFile currentData) throws Exception {
        System.out.printf("Please enter fields for %s\n", this.fieldsAsString);
        System.out.print("Separate each field with commas.\nThe record will be given the lowest unused ID number. \n");
        String inputVal = input.nextLine();
        if(!inputVal.contains(",")) {
            throw new Exception("Error: Please seperate fields using commas ',' ");
        }
        String[] inputVals = inputVal.split(",");
        if(!(inputVals.length == this.fields.size())) {
            throw new Exception("Error: Entered values do not match number of fields");
        }
        List<String> myStringList = new ArrayList<String>(inputVals.length);

        int id = getNextID(currentConfig);
        if(id != 0) {
            this.incrementNextID(id, currentConfig);
        }
        myStringList.add(Integer.toString(id));
        Collections.addAll( myStringList, inputVals );
        for(int i = 1; i < myStringList.size(); i++) {
            if(myStringList.get(i).length() >= this.currentFieldSizes.get(i)) {
                System.out.printf("Field value: %s\nLength: %d\nMax Length: %d\n", myStringList.get(i), myStringList.get(i).length(), this.currentFieldSizes.get(i + 1));
                throw new Exception("Error: Field value cannot exceed max field length.");
            }
        }
        return myStringList;
    }

    public String addToOverFlowFile(String record, CurrentDatabase currentDB) throws IOException {
        int numFiles = 0;
        String line = "";
        currentDB.currentOverflow.seek(0);
        while ((line = currentDB.currentOverflow.readLine()) != null) {
            numFiles = numFiles + 1;
        }
        currentDB.currentOverflow.writeBytes(record + "\r\n");
        numFiles = numFiles + 1;
        if(numFiles >= NUM_FILES_FOR_MERGE) {
            this.mergeFiles(currentDB);
            return "merged";
        }
        return "";
    }
    public void mergeFiles(CurrentDatabase currentDB) throws IOException {
        System.out.println("More than 4 records in overflow detected! Merging records...");
       List<Record> overflowRecords = createSortedListForOverflow(currentDB.currentOverflow);
       String line = "";
        currentDB.currentData.seek(0);
        BufferedWriter writer = new BufferedWriter(new FileWriter("temp.data", true));

        while((line = currentDB.currentData.readLine()) != null) {
            if(!line.contains("MISSING")) {
                if (overflowRecords.size() != 0) {
                    int choice = determineLowestID(line, overflowRecords.get(0).getId());

                    if(choice == 0) {
                        writer.append(line).append("\r\n");
                    }
                    else {
                        writer.append(overflowRecords.get(0).getRecord()).append("\r\n");
                        overflowRecords.remove(0);
                        currentDB.currentData.seek(currentDB.currentData.getFilePointer() - currentDB.currentRecordSize);
                    }
                }
            }
        }
        if(overflowRecords.size() > 0) {
            for(Record record : overflowRecords) writer.append(record.getRecord()).append("\r\n");
        }
        writer.flush();
        writer.close();
        currentDB.currentData.close();

    }

    public int determineLowestID(String dataValue, int overflowId) {
        int dataID = Integer.parseInt(dataValue.substring(0,4).replaceAll("-+", ""));

        if(dataID < overflowId)
            return 0;
        else
            return 1;
    }

    public List<Record> createSortedListForOverflow(RandomAccessFile currentOverflow) throws IOException {
        String line = "";
        List<Record> overflowRecords = new ArrayList<>();
        currentOverflow.seek(0);
        while((line = currentOverflow.readLine()) != null) {
            overflowRecords.add(new Record(line));
        }

        Collections.sort(overflowRecords);
        return overflowRecords;
    }

    public int getNextID(RandomAccessFile currentConfig) throws IOException {
        String line = "";
        currentConfig.seek(0);
        while((line = currentConfig.readLine()) != null) {
            if (line.contains("NEXTID")) {
                String[] lineVals = line.split(",");
                return Integer.parseInt(lineVals[1]);
            }
        }
        return 0;
    }

    public void incrementNextID(int lastID, RandomAccessFile currentConfig) throws IOException {
        String line;
        currentConfig.seek(0);
        while((line = currentConfig.readLine()) != null) {
            if (line.contains("RECORDSIZE")) {
                currentConfig.writeBytes("NEXTID," + (lastID + 1));
            }
        }
    }
}
