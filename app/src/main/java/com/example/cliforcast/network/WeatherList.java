package com.example.cliforcast.network;

import com.google.gson.annotations.SerializedName;

public class WeatherList{
    @SerializedName("list")
    private Weather[] weather;
    public Weather[] getWeather() {
        return weather;
    }
}
