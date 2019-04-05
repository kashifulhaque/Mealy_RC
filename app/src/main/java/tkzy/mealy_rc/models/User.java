package tkzy.mealy_rc.models;

@SuppressWarnings("FieldCanBeLocal")
public class User {

    private String name;
    private String building;
    private String bedNumber;
    private String roomNumber;
    private String collegeName;
    private String phoneNumber;
    private String numberOfGuests;
    private Boolean isDayMealON;
    private Boolean isNightMealON;

    public User(String bedNumber, String building, String collegeName,
                Boolean isDayMealON, Boolean isNightMealON,
                String name, String phoneNumber, String roomNumber,
                String numberOfGuests) {

        this.bedNumber = bedNumber;
        this.building = building;
        this.collegeName = collegeName;
        this.isDayMealON = isDayMealON;
        this.isNightMealON = isNightMealON;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.roomNumber = roomNumber;
        this.numberOfGuests = numberOfGuests;

    }

    public User() {}

    public String getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(String numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    public String getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public Boolean getDayMealON() {
        return isDayMealON;
    }

    public void setDayMealON(Boolean dayMealON) {
        isDayMealON = dayMealON;
    }

    public Boolean getNightMealON() {
        return isNightMealON;
    }

    public void setNightMealON(Boolean nightMealON) {
        isNightMealON = nightMealON;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

}
