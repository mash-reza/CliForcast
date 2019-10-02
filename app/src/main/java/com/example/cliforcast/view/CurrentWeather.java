package com.example.cliforcast.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.cliforcast.R;
import com.example.cliforcast.Util.Constants;
import com.example.cliforcast.database.RoomHelper;
import com.example.cliforcast.network.Weather;
import com.example.cliforcast.network.RetrofitClientInstance;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CurrentWeather extends AppCompatActivity {
    private static final String TAG = "CurrentWeather";
    private SharedPreferences preferences;

    //GoogleApiClient client;
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_weather);

        preferences = getSharedPreferences(Constants.LOCATION_PREFERENCES, MODE_PRIVATE);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        checkPermission();

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
                Log.e(TAG, "onFailure: " + t.getMessage());
                //System.err.println(t);
            }
        });

    }

    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION}, Constants.LOCATION_REQUSET_CODE);
        }
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_WIFI_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_WIFI_STATE}, Constants.WIFI_STATE_REQUSET_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.LOCATION_REQUSET_CODE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    preferences.edit().putBoolean(Constants.LOCATION_PREFERENCES, true).apply();
                } else {
                    new AlertDialog.Builder(this)
                            .setMessage(R.string.location_permission_rejected_messege_alert_dialog)
                            .setPositiveButton(R.string.i_agree, (dialog, which) -> {
                                checkPermission();
                            })
                            .setNegativeButton(R.string.i_disagree, (dialog, which) -> {
                            })
                            .create()
                            .show();
                }
                break;
            case Constants.WIFI_STATE_REQUSET_CODE:
                if ((grantResults.length > 0) && (grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    preferences.edit().putBoolean(Constants.WIFI_PREFERENCES, true).apply();
                } else {
                    new AlertDialog.Builder(this)
                            .setMessage(R.string.wifit_permission_rejected_messege_alert_dialog)
                            .setPositiveButton(R.string.i_agree, (dialog, which) -> {
                                checkPermission();
                            })
                            .setNegativeButton(R.string.i_disagree, (dialog, which) -> {
                            })
                            .create()
                            .show();
                }

        }
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();
        LocationRequest request = new LocationRequest();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(5000);
        request.setFastestInterval(1000);
        fusedLocationProviderClient.requestLocationUpdates(request, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null) {
                    Toast.makeText(CurrentWeather.this, "null location result", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(CurrentWeather.this, locationResult.getLastLocation().getLatitude() + " - " +
                        locationResult.getLastLocation().getLongitude(), Toast.LENGTH_SHORT).show();
            }
        }, Looper.getMainLooper());
    }
}
