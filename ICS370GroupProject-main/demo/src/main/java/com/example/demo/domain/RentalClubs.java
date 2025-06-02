package com.example.demo.domain;

public class RentalClubs {
    private int clubsID;
    private int courseID;
    private boolean isReserved;
    private String rentedTime;
    private String returnedTime;
    private String renterName;

    public RentalClubs(int clubsID, String rentedTime, String returnedTime, int courseID, boolean isReserved, String renterName) {
        this.clubsID = clubsID;
        this.rentedTime = rentedTime;
        this.returnedTime = returnedTime;
        this.courseID = courseID;
        this.isReserved = isReserved;
        this.renterName = renterName;
    }

    public int getClubsID() {
        return clubsID;
    }

    public int getCourseID() {
        return courseID;
    }

    public boolean isIsReserved() {
        return isReserved;
    }

    public void setReserved(boolean isReserved) {
        this.isReserved = isReserved;
    }

    public String getRentedTime() {
        return rentedTime;
    }

    public void setRentedTime(String rentedTime) {
        this.rentedTime = rentedTime;
    }

    public String getReturnedTime() {
        return returnedTime;
    }

    public void setReturnedTime(String returnedTime) {
        this.returnedTime = returnedTime;
    }

    public String getRenterName() {
        return renterName;
    }

    public void setRenterName(String renterName) {
        this.renterName = renterName;
    }
}
