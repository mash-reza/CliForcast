package com.example.cliforcast.database;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import java.util.Date;

@Entity(tableName = "cities", primaryKeys = {"lat", "lon"})
public class Weather {
    @ColumnInfo(name = "lon")
    private double lon;
    @ColumnInfo(name = "lat")
    private double lat;
    @ColumnInfo(name = "main")
    private String main;
    @ColumnInfo(name = "description")
    private String description;
    @ColumnInfo(name = "temp")
    private double temp;
    @ColumnInfo(name = "pressure")
    private int pressure;
    @ColumnInfo(name = "humidity")
    private int humidity;
    @ColumnInfo(name = "temp_min")
    private double temp_min;
    @ColumnInfo(name = "temp_max")
    private double temp_max;
    @ColumnInfo(name = "speed")
    private double speed;
    @ColumnInfo(name = "clouds")
    private int clouds;
    @ColumnInfo(name = "time")
    private long time;

    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
    }

    public String getMain() {
        return main;
    }

    public String getDescription() {
        return description;
    }

    public double getTemp() {
        return temp;
    }

    public int getPressure() {
        return pressure;
    }

    public int getHumidity() {
        return humidity;
    }

    public double getTemp_min() {
        return temp_min;
    }

    public double getTemp_max() {
        return temp_max;
    }

    public double getSpeed() {
        return speed;
    }

    public int getClouds() {
        return clouds;
    }

    public long getTime() { return time;}


    public Weather(double lon, double lat, String main, String description, double temp, int pressure, int humidity, double temp_min, double temp_max, double speed, int clouds, long time) {
        this.lon = lon;
        this.lat = lat;
        this.main = main;
        this.description = description;
        this.temp = temp;
        this.pressure = pressure;
        this.humidity = humidity;
        this.temp_min = temp_min;
        this.temp_max = temp_max;
        this.speed = speed;
        this.clouds = clouds;
        this.time = time;
    }
}
