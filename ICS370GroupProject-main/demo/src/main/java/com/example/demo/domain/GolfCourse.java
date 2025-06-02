package com.example.demo.domain;

import java.util.ArrayList;

public class GolfCourse {

    public String name;
    public int courseID;
    public String paymentInfo;
    public ArrayList <TeeTime> teeSheet;

    public GolfCourse (String name, int courseID, String paymentInfo, ArrayList <TeeTime> teeSheet){

        this.name = name;
        this.courseID = courseID;
        this.paymentInfo = paymentInfo;
        this.teeSheet = teeSheet;

    }

    public int getCourseID(){
        return this.courseID;
    }
    public String getName(){
        return  this.name;
    }
    public String getPaymentInfo(){
        return this.paymentInfo;
    }
    public ArrayList <TeeTime> getTeeSheet(){
        return this.teeSheet;
    }


}
