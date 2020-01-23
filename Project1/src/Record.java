public class Record implements Comparable< Record >{
    int id;
    String record;


    Record(String inputRecord) {
        this.record = inputRecord;
        this.id = Integer.parseInt(inputRecord.substring(0,4).replaceAll("-+", ""));
    }


    public Integer getId() {
        return this.id;
    }

    public String getRecord() {
        return this.record;
    }

    @Override
    public int compareTo(Record record) {
        return this.getId().compareTo(record.getId());
    }
}
