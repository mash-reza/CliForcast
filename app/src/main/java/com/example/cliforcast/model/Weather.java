package com.example.cliforcast.model;

import com.google.gson.annotations.SerializedName;

public class Weather {
    private Coord coord;
    private Weather1[] weather;
    private Main main;
    private Wind wind;
    private Clouds clouds;

    public Coord getCoord() {
        return coord;
    }

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
    }

    public class Main {
        @SerializedName("temp")
        private double temp;
        @SerializedName("pressure")
        private int pressure;
        @SerializedName("humidity")
        private int humidity;
        @SerializedName("temp_min")
        private double temp_min;
        @SerializedName("temp_max")
        private double temp_max;

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
