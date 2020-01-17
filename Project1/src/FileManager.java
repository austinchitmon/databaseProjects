import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class FileManager {
    String[] fields = {"RANK", "NAME", "CITY", "STATE", "ZIP", "EMPLOYEES"};
    Record currentRecord = new Record();
    Scanner input = new Scanner(System.in);

    public void createNewDatabase() {
        boolean hasError = false;
        String fileName = null;


        do {
            try {
                System.out.print("Enter name of .csv file to create DB from (omit extension): ");
                fileName = input.nextLine();
                RandomAccessFile csvFile = new RandomAccessFile(fileName + ".csv", "r");
                hasError = false;
            } catch(FileNotFoundException e) {
                System.out.printf("Error, file not found: %s \n", e.toString());
                hasError = true;
            }

        } while(hasError);
    }

    public void openDatabase() {

    }

    public void closeDatabase() {

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
