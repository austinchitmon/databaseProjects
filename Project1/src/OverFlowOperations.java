import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class OverFlowOperations {
    List<String> fields;
    String fieldsAsString;

    OverFlowOperations(List<String> currentFields) {
        this.fields = new ArrayList<>();
        this.fieldsAsString = "";
        for (String word : currentFields) this.fields.add(word.trim());
        this.fields.remove(0);
        for (String word : this.fields) fieldsAsString = fieldsAsString + word + " ";

    }


    public void createNewRecord(RandomAccessFile currentConfig, RandomAccessFile currentData) {
        System.out.printf("Please enter fields for %s\n", this.fieldsAsString);
        System.out.print("Seperate each field with commas.\n");

    }
}
