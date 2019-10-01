package com.example.cliforcast.view;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.cliforcast.R;
import com.example.cliforcast.database.RoomHelper;
import com.example.cliforcast.network.Weather;
import com.example.cliforcast.network.RetrofitClientInstance;

import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CurrentWeather extends AppCompatActivity {
    private static final String TAG = "CurrentWeather";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_weather);


        RetrofitClientInstance.getINSTANCE().getWeather("Las vegas,us").enqueue(new Callback<Weather>() {
            @Override
            public void onResponse(Call<Weather> call, Response<Weather> response) {
                AsyncTask.execute(() -> {
                    com.example.cliforcast.database.Weather city =
                            RoomHelper.getInstance(getApplicationContext()).getDao().getCity(
                                    response.body().getCoord().getLat(),
                                    response.body().getCoord().getLon());
                    if (city != null) {
                        if (new Date().getTime() - (city.getTime()) < 20000) {
                            Log.i(TAG, "Loaded from database\n" +
                                    "lat :" + city.getLat() + "\n" +
                                    "lon :" + city.getLon() + "\n" +
                                    "main : " + city.getMain() + "\n" +
                                    "decription : " + city.getDescription() + "\n" +
                                    "temp : " + city.getTemp() + "\n" +
                                    "pressure : " + city.getPressure() + "\n" +
                                    "humidity : " + city.getHumidity() + "\n" +
                                    "temp_min : " + city.getTemp_min() + "\n" +
                                    "temp_max : " + city.getTemp_max() + "\n" +
                                    "wind : " + city.getSpeed() + "\n" +
                                    "clouds : " + city.getClouds() + "\n" +
                                    "date : " + city.getTime());
                        } else {
                            Log.i(TAG, "Loaded from network\n" +
                                    "lat :" + response.body().getCoord().getLat() + "\n" +
                                    "lon :" + response.body().getCoord().getLon() + "\n" +
                                    "main : " + response.body().getWeather()[0].getMain() + "\n" +
                                    "decription : " + response.body().getWeather()[0].getDescription() + "\n" +
                                    "temp : " + response.body().getMain().getTemp() + "\n" +
                                    "pressure : " + response.body().getMain().getPressure() + "\n" +
                                    "humidity : " + response.body().getMain().getHumidity() + "\n" +
                                    "temp_min : " + response.body().getMain().getTemp_min() + "\n" +
                                    "temp_max : " + response.body().getMain().getTemp_max() + "\n" +
                                    "wind : " + response.body().getWind().getSpeed() + "\n" +
                                    "clouds : " + response.body().getClouds().getClouds());
                            RoomHelper.getInstance(getApplicationContext()).getDao().insertCity(new com.example.cliforcast.database.Weather(
                                    response.body().getCoord().getLon(),
                                    response.body().getCoord().getLat(),
                                    response.body().getWeather()[0].getMain(),
                                    response.body().getWeather()[0].getDescription(),
                                    response.body().getMain().getTemp(),
                                    response.body().getMain().getPressure(),
                                    response.body().getMain().getHumidity(),
                                    response.body().getMain().getTemp_min(),
                                    response.body().getMain().getTemp_max(),
                                    response.body().getWind().getSpeed(),
                                    response.body().getClouds().getClouds(),
                                    new Date().getTime()
                            ));
                        }
                    } else {
                        Log.i(TAG, "Loaded from network\n" +
                                "lat :" + response.body().getCoord().getLat() + "\n" +
                                "lon :" + response.body().getCoord().getLon() + "\n" +
                                "main : " + response.body().getWeather()[0].getMain() + "\n" +
                                "decription : " + response.body().getWeather()[0].getDescription() + "\n" +
                                "temp : " + response.body().getMain().getTemp() + "\n" +
                                "pressure : " + response.body().getMain().getPressure() + "\n" +
                                "humidity : " + response.body().getMain().getHumidity() + "\n" +
                                "temp_min : " + response.body().getMain().getTemp_min() + "\n" +
                                "temp_max : " + response.body().getMain().getTemp_max() + "\n" +
                                "wind : " + response.body().getWind().getSpeed() + "\n" +
                                "clouds : " + response.body().getClouds().getClouds());
                        RoomHelper.getInstance(getApplicationContext()).getDao().insertCity(new com.example.cliforcast.database.Weather(
                                response.body().getCoord().getLon(),
                                response.body().getCoord().getLat(),
                                response.body().getWeather()[0].getMain(),
                                response.body().getWeather()[0].getDescription(),
                                response.body().getMain().getTemp(),
                                response.body().getMain().getPressure(),
                                response.body().getMain().getHumidity(),
                                response.body().getMain().getTemp_min(),
                                response.body().getMain().getTemp_max(),
                                response.body().getWind().getSpeed(),
                                response.body().getClouds().getClouds(),
                                new Date().getTime()
                        ));
                    }
                });

            }

            @Override
            public void onFailure(Call<Weather> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
                //System.err.println(t);
            }
        });
    }
}
