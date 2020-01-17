public class Record {
    int rank;
    String name;
    String city;
    String state;
    int zip;
    int employees;
    boolean exists;


    Record(int iRank, String iName, String iCity, String iState, int iZip) {
        exists = true;
        rank = iRank;
        name = iName;
        city = iCity;
        state = iState;
        zip = iZip;
    }
    Record() {
        exists = false;
        rank = 0;
        name = null;
        city = null;
        state = null;
        zip = 0;
    }

    public boolean getExists() { return this.exists;}

    public int getEmployees() {
        return this.employees;
    }

    public int getRank() {
        return this.rank;
    }

    public int getZip() {
        return this.zip;
    }

    public String getCity() {
        return this.city;
    }

    public String getName() {
        return this.name;
    }

    public String getState() {
        return this.state;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setEmployees(int employees) {
        this.employees = employees;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setZip(int zip) {
        this.zip = zip;
    }
}
