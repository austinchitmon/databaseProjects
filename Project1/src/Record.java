public class Record implements Comparable< Record >{
    int id;
    String name;
    String record;


    Record(String inputRecord) {
        this.record = inputRecord;
        this.id = Integer.parseInt(inputRecord.substring(0,4).replaceAll("-+", ""));
        this.name = inputRecord.substring(11,56).replaceAll("-+", "");
    }


    public Integer getId() {
        return this.id;
    }
    public String getName() { return this.name;}

    public String getRecord() {
        return this.record;
    }

    @Override
    public int compareTo(Record record) {
        return this.getName().compareTo(record.getName());
    }
}
