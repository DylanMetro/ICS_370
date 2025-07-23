package com.example.demo.domain;

import java.util.ArrayList;

public class Venue {

    public String name;
    public int venueID;
    public String paymentInfo;
    public ArrayList <BookingTime> bookingSheet;

    public Venue (String name, int venueID, String paymentInfo, ArrayList <BookingTime> bookingSheet){

        this.name = name;
        this.venueID = venueID;
        this.paymentInfo = paymentInfo;
        this.bookingSheet = bookingSheet;

    }

    public int getVenueID(){
        return this.venueID;
    }
    public String getName(){
        return  this.name;
    }
    public String getPaymentInfo(){
        return this.paymentInfo;
    }
    public ArrayList <BookingTime> getBookingSheet(){
        return this.bookingSheet;
    }


}
