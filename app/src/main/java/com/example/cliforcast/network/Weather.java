package com.example.cliforcast.network;

import com.google.gson.annotations.SerializedName;

public class Weather {
//    private Coord coord;
    private Weather1[] weather;
    private Main main;
    private Wind wind;
    private Clouds clouds;

    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("dt")
    private long date;


    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getDate() {
        return date;
    }

//    public Coord getCoord() {
//        return coord;
//    }

    public Weather1[] getWeather() {
        return weather;
    }

    public Main getMain() {
        return main;
    }

    public Wind getWind() {
        return wind;
    }

    public Clouds getClouds() {
        return clouds;
    }


    public class Coord {
        @SerializedName("lon")
        private double lon;
        @SerializedName("lat")
        private double lat;

        public double getLon() {
            return lon;
        }

        public double getLat() {
            return lat;
        }
    }


    public class Weather1 {
        @SerializedName("id")
        private int id;
        @SerializedName("main")
        private String main;
        @SerializedName("description")
        private String description;

        public String getMain() {
            return main;
        }

        public String getDescription() {
            return description;
        }

        public int getId() {
            return id;
        }
    }

    public class Main {
        @SerializedName("temp")
        private double temp;
        @SerializedName("pressure")
        private float pressure;
        @SerializedName("humidity")
        private int humidity;
        @SerializedName("temp_min")
        private double temp_min;
        @SerializedName("temp_max")
        private double temp_max;

        public double getTemp() {
            return temp;
        }

        public float getPressure() {
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
    }

    public class Wind {
        @SerializedName("speed")
        private double speed;

        public double getSpeed() {
            return speed;
        }
    }

    public class Clouds {
        @SerializedName("clouds")
        private int clouds;

        public int getClouds() {
            return clouds;
        }
    }
}
