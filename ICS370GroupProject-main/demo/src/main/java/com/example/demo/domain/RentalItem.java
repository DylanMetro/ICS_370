package com.example.demo.domain;

public class RentalItem {
    private int itemID;
    private int venueID;
    private boolean isReserved;
    private String rentedTime;
    private String returnedTime;
    private String renterName;
    private String itemType;

    public RentalItem(int itemID, String rentedTime, String returnedTime, int venueID, boolean isReserved, String renterName, String itemType) {
        this.itemID = itemID;
        this.rentedTime = rentedTime;
        this.returnedTime = returnedTime;
        this.venueID = venueID;
        this.isReserved = isReserved;
        this.renterName = renterName;
        this.itemType = itemType;
    }

    public int getItemID() {
        return itemID;
    }

    public int getVenueID() {
        return venueID;
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

    public String getItemType() {
        return itemType;
    }
}
