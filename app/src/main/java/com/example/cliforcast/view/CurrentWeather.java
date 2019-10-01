package com.example.cliforcast.view;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.example.cliforcast.R;
import com.example.cliforcast.model.Weather;
import com.example.cliforcast.network.RetrofitClientInstance;

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
                Log.i(TAG, "onCreate: \n"+
                        "lat :"+response.body().getCoord().getLat()+"\n" +
                        "lon :"+response.body().getCoord().getLon()+"\n" +
                        "main : "+ response.body().getWeather()[0].getMain()+"\n" +
                        "decription : "+response.body().getWeather()[0].getDescription()+"\n" +
                        "temp : "+response.body().getMain().getTemp()+"\n" +
                        "pressure : " + response.body().getMain().getPressure()+"\n" +
                        "humidity : "+ response.body().getMain().getHumidity()+"\n" +
                        "temp_min : "+response.body().getMain().getTemp_min()+"\n" +
                        "temp_max : "+response.body().getMain().getTemp_max()+"\n" +
                        "wind : "+response.body().getWind().getSpeed()+"\n" +
                        "clouds : "+response.body().getClouds().getClouds());
            }

            @Override
            public void onFailure(Call<Weather> call, Throwable t) {
                Log.e(TAG, "onFailure: ",t );
                //System.err.println(t);
            }
        });
    }
}
