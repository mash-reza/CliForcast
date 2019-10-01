package com.example.cliforcast.network;

import com.example.cliforcast.model.Weather;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GetWeatherService {
    @GET("/data/2.5/weather?APPID=276e7cbc702e178eb512aec1a2610679")
    Call<Weather> getWeather(@Query("q") String city);
}
