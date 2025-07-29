package com.example.demo.domain;


import java.util.ArrayList;

public class member {

    public String name;
    private int id;
    //public static userRole;
    protected ArrayList<Integer> memberships;

    //will add list of memberships and userRole to the member object class later.

    public member (String name, int id){
        this.name = name;
        this.id = id;
    }

    public String getName(){
        return this.name;
    }
    public int getId(){
        return this.id;
    }



}
