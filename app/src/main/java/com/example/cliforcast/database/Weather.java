package com.example.cliforcast.database;

import android.app.AlertDialog;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

@Entity(tableName = "cities", primaryKeys = {"id", "day"})
public class Weather {
    @ColumnInfo(name = "id")
    private int id;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "dt")
    private long date;
    @ColumnInfo(name = "compare_date")
    private long compare_date;
    @ColumnInfo(name = "condition")
    private int condition;
    @ColumnInfo(name = "temp")
    private double temp;
    @ColumnInfo(name = "pressure")
    private float pressure;
    @ColumnInfo(name = "humidity")
    private int humidity;
    @ColumnInfo(name = "temp_min")
    private double temp_min;
    @ColumnInfo(name = "temp_max")
    private double temp_max;
    @ColumnInfo(name = "wind")
    private double wind;
    @ColumnInfo(name = "clouds")
    private int clouds;
    @ColumnInfo(name = "day")
    private int day;
    @ColumnInfo(name = "lat")
    private double lat;
    @ColumnInfo(name = "lon")
    private double lon;

    public Weather() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getCondition() {
        return condition;
    }

    public void setCondition(int condition) {
        this.condition = condition;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public float getPressure() {
        return pressure;
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public double getTemp_min() {
        return temp_min;
    }

    public void setTemp_min(double temp_min) {
        this.temp_min = temp_min;
    }

    public double getTemp_max() {
        return temp_max;
    }

    public void setTemp_max(double temp_max) {
        this.temp_max = temp_max;
    }

    public double getWind() {
        return wind;
    }

    public void setWind(double wind) {
        this.wind = wind;
    }

    public int getClouds() {
        return clouds;
    }

    public void setClouds(int clouds) {
        this.clouds = clouds;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public long getCompare_date() {
        return compare_date;
    }

    public void setCompare_date(long compare_date) {
        this.compare_date = compare_date;
    }

    public Weather(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.date = builder.date;
        this.compare_date = builder.compare_date;
        this.condition = builder.condition;
        this.temp = builder.temp;
        this.pressure = builder.pressure;
        this.humidity = builder.humidity;
        this.temp_min = builder.temp_min;
        this.temp_max = builder.temp_max;
        this.wind = builder.wind;
        this.clouds = builder.clouds;
        this.day = builder.day;
        this.lat = builder.lat;
        this.lon = builder.lon;
    }

    public static class Builder {
        private int id;
        private String name;
        private long date;
        private long compare_date;
        private int condition;
        private double temp;
        private float pressure;
        private int humidity;
        private double temp_min;
        private double temp_max;
        private double wind;
        private int clouds;
        private int day;
        private double lat;
        private double lon;

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder date(long date) {
            this.date = date;
            return this;
        }

        public Builder compare_date(long compare_date) {
            this.compare_date = compare_date;
            return this;
        }

        public Builder condition(int condition) {
            this.condition = condition;
            return this;
        }

        public Builder temp(double temp) {
            this.temp = temp;
            return this;
        }

        public Builder pressure(float pressure) {
            this.pressure = pressure;
            return this;
        }

        public Builder humidity(int humidity) {
            this.humidity = humidity;
            return this;
        }

        public Builder temp_min(double temp_min) {
            this.temp_min = temp_min;
            return this;
        }

        public Builder temp_max(double temp_max) {
            this.temp_max = temp_max;
            return this;
        }

        public Builder wind(double wind) {
            this.wind = wind;
            return this;
        }

        public Builder clouds(int clouds) {
            this.clouds = clouds;
            return this;
        }

        public Builder day(int day) {
            this.day = day;
            return this;
        }

        public Builder lat(double lat) {
            this.lat = lat;
            return this;
        }

        public Builder lon(double lon) {
            this.lon = lon;
            return this;
        }

        public Weather build() {
            return new Weather(this);
        }
    }

}
