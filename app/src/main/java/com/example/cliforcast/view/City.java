package com.example.cliforcast.view;

import com.google.gson.annotations.SerializedName;

public class City {
    @SerializedName("id")
    int id;
    @SerializedName("name")
    String name;
    @SerializedName("country")
    String country;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }
}
