package com.example.cliforcast.viewModel;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cliforcast.Util.Constants;
import com.example.cliforcast.Util.Utility;
import com.example.cliforcast.database.RoomHelper;
import com.example.cliforcast.network.RetrofitClientInstance;
import com.example.cliforcast.network.Weather;
import com.example.cliforcast.network.WeatherList;
import com.example.cliforcast.network.Error;
import com.example.cliforcast.network.WeatherWrapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CurrentWeatherViewModel extends ViewModel {
    private static final String TAG = "CurrentWeatherViewModel";
    private Context context;
    private int cityId;
    private double lat;
    private double lon;
    private MutableLiveData<WeatherWrapper<com.example.cliforcast.database.Weather>> weatherObservable = new MutableLiveData<>();
    private MutableLiveData<WeatherWrapper<List<com.example.cliforcast.database.Weather>>> weatherListObservable = new MutableLiveData<>();
    private boolean requestedByLocation = false;
    private FusedLocationProviderClient locationProviderClient;
    private LocationManager locationManager;
    private SharedPreferences preferences;

    public CurrentWeatherViewModel(Context context, int cityId) {
        this.context = context;
        this.cityId = cityId;
        preferences = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
        locationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        locationManager = (LocationManager) context.getSystemService(Service.LOCATION_SERVICE);
    }

    public MutableLiveData<WeatherWrapper<com.example.cliforcast.database.Weather>> getWeather() {
        if (weatherObservable != null)
            return weatherObservable;
        return null;
    }

    public MutableLiveData<WeatherWrapper<List<com.example.cliforcast.database.Weather>>> getWeatherList() {
        if (weatherListObservable != null)
            return weatherListObservable;
        return null;
    }

    public void requestWeatherByCityID() {
        weatherObservable.setValue(new WeatherWrapper<>(null, Error.REQUEST_NOT_COMPELLED));
        weatherListObservable.setValue(new WeatherWrapper<>(null, Error.REQUEST_NOT_COMPELLED));
        long date = new Date().getTime();
        AsyncTask.execute(() -> {
            com.example.cliforcast.database.Weather databaseWeather =
                    RoomHelper.getInstance(context).getDao().getCityById(cityId, 0);
            if (databaseWeather == null) {
                RetrofitClientInstance.getINSTANCE().getWeather(cityId).enqueue(new Callback<Weather>() {
                    @Override
                    public void onResponse(Call<Weather> call, Response<Weather> response) {
                        if (response.body() != null) {
                            requestedByLocation = false;
                            com.example.cliforcast.database.Weather weather = new com.example.cliforcast.database.Weather.Builder()
                                    .id(response.body().getId())
                                    .name(response.body().getName())
                                    .date(response.body().getDate() * 1000)
                                    .compare_date(date)
                                    .temp(response.body().getMain().getTemp())
                                    .temp_min(response.body().getMain().getTemp_min())
                                    .temp_max(response.body().getMain().getTemp_max())
                                    .pressure(response.body().getMain().getPressure())
                                    .humidity(response.body().getMain().getHumidity())
                                    .clouds(response.body().getClouds().getClouds())
                                    .day(0)
                                    .wind(response.body().getWind().getSpeed())
                                    .condition(response.body().getWeather()[0].getId())
                                    .build();

                            weatherObservable.postValue(new WeatherWrapper<>(weather, Error.NO_ERROR));
                            AsyncTask.execute(() -> {
                                RoomHelper.getInstance(context).getDao().insertCity(weather);
                            });
                            Log.i(TAG, "onResponse: from network city");
                        }
                    }

                    @Override
                    public void onFailure(Call<Weather> call, Throwable t) {
                        if (t instanceof IOException)
                            weatherObservable.postValue(new WeatherWrapper<>(null, Error.NO_INTERNET));
                    }
                });
                RetrofitClientInstance.getINSTANCE().getFiveDayWeather(cityId).enqueue(new Callback<WeatherList>() {
                    @Override
                    public void onResponse(Call<WeatherList> call, Response<WeatherList> response) {
                        List<com.example.cliforcast.database.Weather> weathers = new ArrayList<>(4);
                        if (response.body() != null) {
                            for (int i = 1; i <= 4; i++) {
                                weathers.add(new com.example.cliforcast.database.Weather.Builder()
                                        .id(response.body().getWeather()[i * 8].getId())
                                        .name(response.body().getWeather()[i * 8].getName())
                                        .date(response.body().getWeather()[i * 8].getDate() * 1000)
                                        .compare_date(date)
                                        .temp(response.body().getWeather()[i * 8].getMain().getTemp())
                                        .temp_min(response.body().getWeather()[i * 8].getMain().getTemp_min())
                                        .temp_max(response.body().getWeather()[i * 8].getMain().getTemp_max())
                                        .pressure(response.body().getWeather()[i * 8].getMain().getPressure())
                                        .humidity(response.body().getWeather()[i * 8].getMain().getHumidity())
                                        .clouds(response.body().getWeather()[i * 8].getClouds().getClouds())
                                        .day(i)
                                        .wind(response.body().getWeather()[i * 8].getWind().getSpeed())
                                        .condition(response.body().getWeather()[i * 8].getWeather()[0].getId())
                                        .build());
                            }
                            weatherListObservable.postValue(new WeatherWrapper<>(weathers, Error.NO_ERROR));
                            AsyncTask.execute(() -> {
                                for (int i = 1; i <= 4; i++) {
                                    RoomHelper.getInstance(context).getDao().insertCity(
                                            new com.example.cliforcast.database.Weather.Builder()
                                                    .id(cityId)
                                                    .name(response.body().getWeather()[i * 8].getName())
                                                    .date(response.body().getWeather()[i * 8].getDate() * 1000)
                                                    .compare_date(date)
                                                    .temp(response.body().getWeather()[i * 8].getMain().getTemp())
                                                    .temp_min(response.body().getWeather()[i * 8].getMain().getTemp_min())
                                                    .temp_max(response.body().getWeather()[i * 8].getMain().getTemp_max())
                                                    .pressure(response.body().getWeather()[i * 8].getMain().getPressure())
                                                    .humidity(response.body().getWeather()[i * 8].getMain().getHumidity())
                                                    .clouds(response.body().getWeather()[i * 8].getClouds().getClouds())
                                                    .day(i)
                                                    .wind(response.body().getWeather()[i * 8].getWind().getSpeed())
                                                    .condition(response.body().getWeather()[i * 8].getWeather()[0].getId())
                                                    .build()
                                    );
                                }
                            });
                        }
                        Log.i(TAG, "onResponse: from network city list");
                    }

                    @Override
                    public void onFailure(Call<WeatherList> call, Throwable t) {
                        if (t instanceof IOException)
                            weatherListObservable.postValue(new WeatherWrapper<>(null, Error.NO_INTERNET));
                    }
                });
            } else if (Utility.isWeatherExpired(databaseWeather.getCompare_date())) {
                RetrofitClientInstance.getINSTANCE().getWeather(cityId).enqueue(new Callback<Weather>() {
                    @Override
                    public void onResponse(Call<Weather> call, Response<Weather> response) {
                        if (response.body() != null) {
                            requestedByLocation = false;
                            com.example.cliforcast.database.Weather weather = new com.example.cliforcast.database.Weather.Builder()
                                    .id(response.body().getId())
                                    .name(response.body().getName())
                                    .date(response.body().getDate() * 1000)
                                    .compare_date(date)
                                    .temp(response.body().getMain().getTemp())
                                    .temp_min(response.body().getMain().getTemp_min())
                                    .temp_max(response.body().getMain().getTemp_max())
                                    .pressure(response.body().getMain().getPressure())
                                    .humidity(response.body().getMain().getHumidity())
                                    .clouds(response.body().getClouds().getClouds())
                                    .day(0)
                                    .wind(response.body().getWind().getSpeed())
                                    .condition(response.body().getWeather()[0].getId())
                                    .build();

                            weatherObservable.postValue(new WeatherWrapper<>(weather, Error.NO_ERROR));
                            AsyncTask.execute(() -> {
                                com.example.cliforcast.database.Weather databaseWeather = new com.example.cliforcast.database.Weather.Builder()
                                        .id(response.body().getId())
                                        .name(response.body().getName())
                                        .date(response.body().getDate() * 1000)
                                        .compare_date(date)
                                        .temp(response.body().getMain().getTemp())
                                        .temp_min(response.body().getMain().getTemp_min())
                                        .temp_max(response.body().getMain().getTemp_max())
                                        .pressure(response.body().getMain().getPressure())
                                        .humidity(response.body().getMain().getHumidity())
                                        .clouds(response.body().getClouds().getClouds())
                                        .day(0)
                                        .wind(response.body().getWind().getSpeed())
                                        .condition(response.body().getWeather()[0].getId())
                                        .build();
                                RoomHelper.getInstance(context).getDao().insertCity(databaseWeather);
                            });
                            Log.i(TAG, "onResponse: from network city");
                        }
                    }

                    @Override
                    public void onFailure(Call<Weather> call, Throwable t) {
                        if (t instanceof IOException)
                            weatherObservable.postValue(new WeatherWrapper<>(null, Error.NO_INTERNET));
                    }
                });
                RetrofitClientInstance.getINSTANCE().getFiveDayWeather(cityId).enqueue(new Callback<WeatherList>() {
                    @Override
                    public void onResponse(Call<WeatherList> call, Response<WeatherList> response) {
                        List<com.example.cliforcast.database.Weather> weathers = new ArrayList<>(4);
                        if (response.body() != null) {
                            for (int i = 1; i <= 4; i++) {
                                weathers.add(new com.example.cliforcast.database.Weather.Builder()
                                        .id(response.body().getWeather()[i * 8].getId())
                                        .name(response.body().getWeather()[i * 8].getName())
                                        .date(response.body().getWeather()[i * 8].getDate() * 1000)
                                        .compare_date(date)
                                        .temp(response.body().getWeather()[i * 8].getMain().getTemp())
                                        .temp_min(response.body().getWeather()[i * 8].getMain().getTemp_min())
                                        .temp_max(response.body().getWeather()[i * 8].getMain().getTemp_max())
                                        .pressure(response.body().getWeather()[i * 8].getMain().getPressure())
                                        .humidity(response.body().getWeather()[i * 8].getMain().getHumidity())
                                        .clouds(response.body().getWeather()[i * 8].getClouds().getClouds())
                                        .day(i)
                                        .wind(response.body().getWeather()[i * 8].getWind().getSpeed())
                                        .condition(response.body().getWeather()[i * 8].getWeather()[0].getId())
                                        .build());
                            }
                            weatherListObservable.postValue(new WeatherWrapper<>(weathers, Error.NO_ERROR));
                            AsyncTask.execute(() -> {
                                for (int i = 1; i <= 4; i++) {
                                    RoomHelper.getInstance(context).getDao().insertCity(
                                            new com.example.cliforcast.database.Weather.Builder()
                                                    .id(cityId)
                                                    .name(response.body().getWeather()[i * 8].getName())
                                                    .date(response.body().getWeather()[i * 8].getDate() * 1000)
                                                    .compare_date(date)
                                                    .temp(response.body().getWeather()[i * 8].getMain().getTemp())
                                                    .temp_min(response.body().getWeather()[i * 8].getMain().getTemp_min())
                                                    .temp_max(response.body().getWeather()[i * 8].getMain().getTemp_max())
                                                    .pressure(response.body().getWeather()[i * 8].getMain().getPressure())
                                                    .humidity(response.body().getWeather()[i * 8].getMain().getHumidity())
                                                    .clouds(response.body().getWeather()[i * 8].getClouds().getClouds())
                                                    .day(i)
                                                    .wind(response.body().getWeather()[i * 8].getWind().getSpeed())
                                                    .condition(response.body().getWeather()[i * 8].getWeather()[0].getId())
                                                    .build()
                                    );
                                }
                            });
                        }
                        Log.i(TAG, "onResponse: from network city list");
                    }

                    @Override
                    public void onFailure(Call<WeatherList> call, Throwable t) {
                        if (t instanceof IOException)
                            weatherListObservable.postValue(new WeatherWrapper<>(null, Error.NO_INTERNET));
                    }
                });
            } else {
                weatherObservable.postValue(new WeatherWrapper<>(databaseWeather, Error.NO_ERROR));
                Log.i(TAG, "onResponse: from database city");

                List<com.example.cliforcast.database.Weather> weathers = new ArrayList<>();
                for (int i = 1; i <= 4; i++) {
                    com.example.cliforcast.database.Weather weather = RoomHelper.getInstance(context).getDao().getCityById(cityId, i);
                    weathers.add(weather);
                }
                weatherListObservable.postValue(new WeatherWrapper<>(weathers, Error.NO_ERROR));
                Log.i(TAG, "onResponse: from database city list");
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void requestWeatherByLatLon() {
        weatherObservable.setValue(new WeatherWrapper<>(null, Error.REQUEST_NOT_COMPELLED));
        weatherListObservable.setValue(new WeatherWrapper<>(null, Error.REQUEST_NOT_COMPELLED));
        long date = new Date().getTime();
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            LocationRequest request = new LocationRequest();
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            request.setInterval(1800000);
            locationProviderClient.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    if (locationResult == null) {
                        Log.e(TAG, "onLocationResult: location null");
                        return;
                    }
                    AsyncTask.execute(() -> {
                        com.example.cliforcast.database.Weather databaseWeather =
                                RoomHelper.getInstance(context).getDao().getCityByCoord(
                                        locationResult.getLastLocation().getLatitude(),
                                        locationResult.getLastLocation().getLongitude());
                        DecimalFormat df = new DecimalFormat("#.##");
                        float lat = Float.valueOf(df.format(locationResult.getLastLocation().getLatitude()));
                        float lon = Float.valueOf(df.format(locationResult.getLastLocation().getLongitude()));
                        float prefLat = Float.valueOf(df.format(preferences.getFloat(Constants.LAT_PREFERENCES, -1)));
                        float prefLon = Float.valueOf(df.format(preferences.getFloat(Constants.LON_PREFERENCES, -1)));
                        if (databaseWeather == null) {
                            //request by location and update database
                            if (databaseWeather == null) {
                                RetrofitClientInstance.getINSTANCE().getWeather(
                                        locationResult.getLastLocation().getLatitude(),
                                        locationResult.getLastLocation().getLongitude()).enqueue(new Callback<Weather>() {
                                    @Override
                                    public void onResponse(Call<Weather> call, Response<Weather> response) {
                                        if (response.body() != null) {
                                            requestedByLocation = true;
                                            com.example.cliforcast.database.Weather weather = new com.example.cliforcast.database.Weather.Builder()
                                                    .lat(locationResult.getLastLocation().getLatitude())
                                                    .lon(locationResult.getLastLocation().getLongitude())
                                                    .id(response.body().getId())
                                                    .name(response.body().getName())
                                                    .date(response.body().getDate() * 1000)
                                                    .temp(response.body().getMain().getTemp())
                                                    .temp_min(response.body().getMain().getTemp_min())
                                                    .temp_max(response.body().getMain().getTemp_max())
                                                    .pressure(response.body().getMain().getPressure())
                                                    .humidity(response.body().getMain().getHumidity())
                                                    .clouds(response.body().getClouds().getClouds())
                                                    .day(0)
                                                    .wind(response.body().getWind().getSpeed())
                                                    .condition(response.body().getWeather()[0].getId())
                                                    .build();

                                            weatherObservable.postValue(new WeatherWrapper<>(weather, Error.NO_ERROR));
                                            AsyncTask.execute(() -> {
                                                com.example.cliforcast.database.Weather databaseWeather = new com.example.cliforcast.database.Weather.Builder()
                                                        .lat(locationResult.getLastLocation().getLatitude())
                                                        .lon(locationResult.getLastLocation().getLongitude())
                                                        .id(response.body().getId())
                                                        .name(response.body().getName())
                                                        .date(date)
                                                        .temp(response.body().getMain().getTemp())
                                                        .temp_min(response.body().getMain().getTemp_min())
                                                        .temp_max(response.body().getMain().getTemp_max())
                                                        .pressure(response.body().getMain().getPressure())
                                                        .humidity(response.body().getMain().getHumidity())
                                                        .clouds(response.body().getClouds().getClouds())
                                                        .day(0)
                                                        .wind(response.body().getWind().getSpeed())
                                                        .condition(response.body().getWeather()[0].getId())
                                                        .build();
                                                RoomHelper.getInstance(context).getDao().insertCity(databaseWeather);
                                            });
                                            Log.i(TAG, "onResponse: from network city");
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Weather> call, Throwable t) {
                                        if (t instanceof IOException)
                                            weatherObservable.postValue(new WeatherWrapper<>(null, Error.NO_INTERNET));
                                    }
                                });
                                RetrofitClientInstance.getINSTANCE().getFiveDayWeather(
                                        locationResult.getLastLocation().getLatitude(),
                                        locationResult.getLastLocation().getLongitude()
                                ).enqueue(new Callback<WeatherList>() {
                                    @Override
                                    public void onResponse(Call<WeatherList> call, Response<WeatherList> response) {
                                        List<com.example.cliforcast.database.Weather> weathers = new ArrayList<>(4);
                                        if (response.body() != null) {
                                            for (int i = 1; i <= 4; i++) {
                                                weathers.add(new com.example.cliforcast.database.Weather.Builder()
                                                        .lat(locationResult.getLastLocation().getLatitude())
                                                        .lon(locationResult.getLastLocation().getLongitude())
                                                        .id(response.body().getWeather()[i * 8].getId())
                                                        .name(response.body().getWeather()[i * 8].getName())
                                                        .date(response.body().getWeather()[i * 8].getDate() * 1000)
                                                        .temp(response.body().getWeather()[i * 8].getMain().getTemp())
                                                        .temp_min(response.body().getWeather()[i * 8].getMain().getTemp_min())
                                                        .temp_max(response.body().getWeather()[i * 8].getMain().getTemp_max())
                                                        .pressure(response.body().getWeather()[i * 8].getMain().getPressure())
                                                        .humidity(response.body().getWeather()[i * 8].getMain().getHumidity())
                                                        .clouds(response.body().getWeather()[i * 8].getClouds().getClouds())
                                                        .day(i)
                                                        .wind(response.body().getWeather()[i * 8].getWind().getSpeed())
                                                        .condition(response.body().getWeather()[i * 8].getWeather()[0].getId())
                                                        .build());
                                            }
                                            weatherListObservable.postValue(new WeatherWrapper<>(weathers, Error.NO_ERROR));
                                            AsyncTask.execute(() -> {
                                                for (int i = 1; i <= 4; i++) {
                                                    RoomHelper.getInstance(context).getDao().insertCity(
                                                            new com.example.cliforcast.database.Weather.Builder()
                                                                    .lat(locationResult.getLastLocation().getLatitude())
                                                                    .lon(locationResult.getLastLocation().getLongitude())
                                                                    .id(cityId)
                                                                    .name(response.body().getWeather()[i * 8].getName())
                                                                    .date(date)
                                                                    .temp(response.body().getWeather()[i * 8].getMain().getTemp())
                                                                    .temp_min(response.body().getWeather()[i * 8].getMain().getTemp_min())
                                                                    .temp_max(response.body().getWeather()[i * 8].getMain().getTemp_max())
                                                                    .pressure(response.body().getWeather()[i * 8].getMain().getPressure())
                                                                    .humidity(response.body().getWeather()[i * 8].getMain().getHumidity())
                                                                    .clouds(response.body().getWeather()[i * 8].getClouds().getClouds())
                                                                    .day(i)
                                                                    .wind(response.body().getWeather()[i * 8].getWind().getSpeed())
                                                                    .condition(response.body().getWeather()[i * 8].getWeather()[0].getId())
                                                                    .build()
                                                    );
                                                }
                                            });
                                        }
                                        Log.i(TAG, "onResponse: from network city list");
                                    }

                                    @Override
                                    public void onFailure(Call<WeatherList> call, Throwable t) {
                                        if (t instanceof IOException)
                                            weatherListObservable.postValue(new WeatherWrapper<>(null, Error.NO_INTERNET));
                                    }
                                });
                            } else
                                if (Utility.isWeatherByLocationExpired(databaseWeather.getDate())) {
                                RetrofitClientInstance.getINSTANCE().getWeather(
                                        locationResult.getLastLocation().getLatitude(),
                                        locationResult.getLastLocation().getLongitude()
                                ).enqueue(new Callback<Weather>() {
                                    @Override
                                    public void onResponse(Call<Weather> call, Response<Weather> response) {
                                        if (response.body() != null) {
                                            requestedByLocation = true;
                                            com.example.cliforcast.database.Weather weather = new com.example.cliforcast.database.Weather.Builder()
                                                    .lat(locationResult.getLastLocation().getLatitude())
                                                    .lon(locationResult.getLastLocation().getLongitude())
                                                    .id(response.body().getId())
                                                    .name(response.body().getName())
                                                    .date(response.body().getDate() * 1000)
                                                    .temp(response.body().getMain().getTemp())
                                                    .temp_min(response.body().getMain().getTemp_min())
                                                    .temp_max(response.body().getMain().getTemp_max())
                                                    .pressure(response.body().getMain().getPressure())
                                                    .humidity(response.body().getMain().getHumidity())
                                                    .clouds(response.body().getClouds().getClouds())
                                                    .day(0)
                                                    .wind(response.body().getWind().getSpeed())
                                                    .condition(response.body().getWeather()[0].getId())
                                                    .build();

                                            weatherObservable.postValue(new WeatherWrapper<>(weather, Error.NO_ERROR));
                                            AsyncTask.execute(() -> {
                                                com.example.cliforcast.database.Weather databaseWeather = new com.example.cliforcast.database.Weather.Builder()
                                                        .lat(locationResult.getLastLocation().getLatitude())
                                                        .lon(locationResult.getLastLocation().getLongitude())
                                                        .id(response.body().getId())
                                                        .name(response.body().getName())
                                                        .date(date)
                                                        .temp(response.body().getMain().getTemp())
                                                        .temp_min(response.body().getMain().getTemp_min())
                                                        .temp_max(response.body().getMain().getTemp_max())
                                                        .pressure(response.body().getMain().getPressure())
                                                        .humidity(response.body().getMain().getHumidity())
                                                        .clouds(response.body().getClouds().getClouds())
                                                        .day(0)
                                                        .wind(response.body().getWind().getSpeed())
                                                        .condition(response.body().getWeather()[0].getId())
                                                        .build();
                                                RoomHelper.getInstance(context).getDao().insertCity(databaseWeather);
                                            });
                                            Log.i(TAG, "onResponse: from network city");
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Weather> call, Throwable t) {
                                        if (t instanceof IOException)
                                            weatherObservable.postValue(new WeatherWrapper<>(null, Error.NO_INTERNET));
                                    }
                                });
                                RetrofitClientInstance.getINSTANCE().getFiveDayWeather(cityId).enqueue(new Callback<WeatherList>() {
                                    @Override
                                    public void onResponse(Call<WeatherList> call, Response<WeatherList> response) {
                                        List<com.example.cliforcast.database.Weather> weathers = new ArrayList<>(4);
                                        if (response.body() != null) {
                                            for (int i = 1; i <= 4; i++) {
                                                weathers.add(new com.example.cliforcast.database.Weather.Builder()
                                                        .lat(locationResult.getLastLocation().getLatitude())
                                                        .lon(locationResult.getLastLocation().getLongitude())
                                                        .id(response.body().getWeather()[i * 8].getId())
                                                        .name(response.body().getWeather()[i * 8].getName())
                                                        .date(response.body().getWeather()[i * 8].getDate() * 1000)
                                                        .temp(response.body().getWeather()[i * 8].getMain().getTemp())
                                                        .temp_min(response.body().getWeather()[i * 8].getMain().getTemp_min())
                                                        .temp_max(response.body().getWeather()[i * 8].getMain().getTemp_max())
                                                        .pressure(response.body().getWeather()[i * 8].getMain().getPressure())
                                                        .humidity(response.body().getWeather()[i * 8].getMain().getHumidity())
                                                        .clouds(response.body().getWeather()[i * 8].getClouds().getClouds())
                                                        .day(i)
                                                        .wind(response.body().getWeather()[i * 8].getWind().getSpeed())
                                                        .condition(response.body().getWeather()[i * 8].getWeather()[0].getId())
                                                        .build());
                                            }
                                            weatherListObservable.postValue(new WeatherWrapper<>(weathers, Error.NO_ERROR));
                                            AsyncTask.execute(() -> {
                                                for (int i = 1; i <= 4; i++) {
                                                    RoomHelper.getInstance(context).getDao().insertCity(
                                                            new com.example.cliforcast.database.Weather.Builder()
                                                                    .lat(locationResult.getLastLocation().getLatitude())
                                                                    .lon(locationResult.getLastLocation().getLongitude())
                                                                    .id(cityId)
                                                                    .name(response.body().getWeather()[i * 8].getName())
                                                                    .date(date)
                                                                    .temp(response.body().getWeather()[i * 8].getMain().getTemp())
                                                                    .temp_min(response.body().getWeather()[i * 8].getMain().getTemp_min())
                                                                    .temp_max(response.body().getWeather()[i * 8].getMain().getTemp_max())
                                                                    .pressure(response.body().getWeather()[i * 8].getMain().getPressure())
                                                                    .humidity(response.body().getWeather()[i * 8].getMain().getHumidity())
                                                                    .clouds(response.body().getWeather()[i * 8].getClouds().getClouds())
                                                                    .day(i)
                                                                    .wind(response.body().getWeather()[i * 8].getWind().getSpeed())
                                                                    .condition(response.body().getWeather()[i * 8].getWeather()[0].getId())
                                                                    .build()
                                                    );
                                                }
                                            });
                                        }
                                        Log.i(TAG, "onResponse: from network city list");
                                    }

                                    @Override
                                    public void onFailure(Call<WeatherList> call, Throwable t) {
                                        if (t instanceof IOException)
                                            weatherListObservable.postValue(new WeatherWrapper<>(null, Error.NO_INTERNET));
                                    }
                                });
                            }
                            preferences.edit()
                                    .putFloat(Constants.LAT_PREFERENCES, (float) locationResult.getLastLocation().getLatitude())
                                    .putFloat(Constants.LON_PREFERENCES, (float) locationResult.getLastLocation().getLongitude())
                                    .apply();
                        } else
                            if ((prefLat == -1 || prefLon == -1) || (prefLat != lat) || (prefLon != lon)) {
                            //post database weather to livedata
                            RetrofitClientInstance.getINSTANCE().getWeather(
                                    locationResult.getLastLocation().getLatitude(),
                                    locationResult.getLastLocation().getLongitude()).enqueue(new Callback<Weather>() {
                                @Override
                                public void onResponse(Call<Weather> call, Response<Weather> response) {
                                    if (response.body() != null) {
                                        requestedByLocation = true;
                                        com.example.cliforcast.database.Weather weather = new com.example.cliforcast.database.Weather.Builder()
                                                .lat(locationResult.getLastLocation().getLatitude())
                                                .lon(locationResult.getLastLocation().getLongitude())
                                                .id(response.body().getId())
                                                .name(response.body().getName())
                                                .date(response.body().getDate() * 1000)
                                                .temp(response.body().getMain().getTemp())
                                                .temp_min(response.body().getMain().getTemp_min())
                                                .temp_max(response.body().getMain().getTemp_max())
                                                .pressure(response.body().getMain().getPressure())
                                                .humidity(response.body().getMain().getHumidity())
                                                .clouds(response.body().getClouds().getClouds())
                                                .day(0)
                                                .wind(response.body().getWind().getSpeed())
                                                .condition(response.body().getWeather()[0].getId())
                                                .build();

                                        weatherObservable.postValue(new WeatherWrapper<>(weather, Error.NO_ERROR));
                                        AsyncTask.execute(() -> {
                                            com.example.cliforcast.database.Weather databaseWeather = new com.example.cliforcast.database.Weather.Builder()
                                                    .lat(locationResult.getLastLocation().getLatitude())
                                                    .lon(locationResult.getLastLocation().getLongitude())
                                                    .id(response.body().getId())
                                                    .name(response.body().getName())
                                                    .date(date)
                                                    .temp(response.body().getMain().getTemp())
                                                    .temp_min(response.body().getMain().getTemp_min())
                                                    .temp_max(response.body().getMain().getTemp_max())
                                                    .pressure(response.body().getMain().getPressure())
                                                    .humidity(response.body().getMain().getHumidity())
                                                    .clouds(response.body().getClouds().getClouds())
                                                    .day(0)
                                                    .wind(response.body().getWind().getSpeed())
                                                    .condition(response.body().getWeather()[0].getId())
                                                    .build();
                                            RoomHelper.getInstance(context).getDao().insertCity(databaseWeather);
                                        });
                                        Log.i(TAG, "onResponse: from network city");
                                    }
                                }

                                @Override
                                public void onFailure(Call<Weather> call, Throwable t) {
                                    if (t instanceof IOException)
                                        weatherObservable.postValue(new WeatherWrapper<>(null, Error.NO_INTERNET));
                                }
                            });
                            RetrofitClientInstance.getINSTANCE().getFiveDayWeather(
                                    locationResult.getLastLocation().getLatitude(),
                                    locationResult.getLastLocation().getLongitude()
                            ).enqueue(new Callback<WeatherList>() {
                                @Override
                                public void onResponse(Call<WeatherList> call, Response<WeatherList> response) {
                                    List<com.example.cliforcast.database.Weather> weathers = new ArrayList<>(4);
                                    if (response.body() != null) {
                                        for (int i = 1; i <= 4; i++) {
                                            weathers.add(new com.example.cliforcast.database.Weather.Builder()
                                                    .lat(locationResult.getLastLocation().getLatitude())
                                                    .lon(locationResult.getLastLocation().getLongitude())
                                                    .id(response.body().getWeather()[i * 8].getId())
                                                    .name(response.body().getWeather()[i * 8].getName())
                                                    .date(response.body().getWeather()[i * 8].getDate() * 1000)
                                                    .temp(response.body().getWeather()[i * 8].getMain().getTemp())
                                                    .temp_min(response.body().getWeather()[i * 8].getMain().getTemp_min())
                                                    .temp_max(response.body().getWeather()[i * 8].getMain().getTemp_max())
                                                    .pressure(response.body().getWeather()[i * 8].getMain().getPressure())
                                                    .humidity(response.body().getWeather()[i * 8].getMain().getHumidity())
                                                    .clouds(response.body().getWeather()[i * 8].getClouds().getClouds())
                                                    .day(i)
                                                    .wind(response.body().getWeather()[i * 8].getWind().getSpeed())
                                                    .condition(response.body().getWeather()[i * 8].getWeather()[0].getId())
                                                    .build());
                                        }
                                        weatherListObservable.postValue(new WeatherWrapper<>(weathers, Error.NO_ERROR));
                                        AsyncTask.execute(() -> {
                                            for (int i = 1; i <= 4; i++) {
                                                RoomHelper.getInstance(context).getDao().insertCity(
                                                        new com.example.cliforcast.database.Weather.Builder()
                                                                .lat(locationResult.getLastLocation().getLatitude())
                                                                .lon(locationResult.getLastLocation().getLongitude())
                                                                .id(cityId)
                                                                .name(response.body().getWeather()[i * 8].getName())
                                                                .date(date)
                                                                .temp(response.body().getWeather()[i * 8].getMain().getTemp())
                                                                .temp_min(response.body().getWeather()[i * 8].getMain().getTemp_min())
                                                                .temp_max(response.body().getWeather()[i * 8].getMain().getTemp_max())
                                                                .pressure(response.body().getWeather()[i * 8].getMain().getPressure())
                                                                .humidity(response.body().getWeather()[i * 8].getMain().getHumidity())
                                                                .clouds(response.body().getWeather()[i * 8].getClouds().getClouds())
                                                                .day(i)
                                                                .wind(response.body().getWeather()[i * 8].getWind().getSpeed())
                                                                .condition(response.body().getWeather()[i * 8].getWeather()[0].getId())
                                                                .build()
                                                );
                                            }
                                        });
                                    }
                                    Log.i(TAG, "onResponse: from network city list");
                                }

                                @Override
                                public void onFailure(Call<WeatherList> call, Throwable t) {
                                    if (t instanceof IOException)
                                        weatherListObservable.postValue(new WeatherWrapper<>(null, Error.NO_INTERNET));
                                }
                            });
                        } else
                            if (Utility.isWeatherByLocationExpired(databaseWeather.getDate())) {
                            RetrofitClientInstance.getINSTANCE().getWeather(
                                    locationResult.getLastLocation().getLatitude(),
                                    locationResult.getLastLocation().getLongitude()
                            ).enqueue(new Callback<Weather>() {
                                @Override
                                public void onResponse(Call<Weather> call, Response<Weather> response) {
                                    if (response.body() != null) {
                                        requestedByLocation = true;
                                        com.example.cliforcast.database.Weather weather = new com.example.cliforcast.database.Weather.Builder()
                                                .lat(locationResult.getLastLocation().getLatitude())
                                                .lon(locationResult.getLastLocation().getLongitude())
                                                .id(response.body().getId())
                                                .name(response.body().getName())
                                                .date(response.body().getDate() * 1000)
                                                .temp(response.body().getMain().getTemp())
                                                .temp_min(response.body().getMain().getTemp_min())
                                                .temp_max(response.body().getMain().getTemp_max())
                                                .pressure(response.body().getMain().getPressure())
                                                .humidity(response.body().getMain().getHumidity())
                                                .clouds(response.body().getClouds().getClouds())
                                                .day(0)
                                                .wind(response.body().getWind().getSpeed())
                                                .condition(response.body().getWeather()[0].getId())
                                                .build();

                                        weatherObservable.postValue(new WeatherWrapper<>(weather, Error.NO_ERROR));
                                        AsyncTask.execute(() -> {
                                            com.example.cliforcast.database.Weather databaseWeather = new com.example.cliforcast.database.Weather.Builder()
                                                    .lat(locationResult.getLastLocation().getLatitude())
                                                    .lon(locationResult.getLastLocation().getLongitude())
                                                    .id(response.body().getId())
                                                    .name(response.body().getName())
                                                    .date(date)
                                                    .temp(response.body().getMain().getTemp())
                                                    .temp_min(response.body().getMain().getTemp_min())
                                                    .temp_max(response.body().getMain().getTemp_max())
                                                    .pressure(response.body().getMain().getPressure())
                                                    .humidity(response.body().getMain().getHumidity())
                                                    .clouds(response.body().getClouds().getClouds())
                                                    .day(0)
                                                    .wind(response.body().getWind().getSpeed())
                                                    .condition(response.body().getWeather()[0].getId())
                                                    .build();
                                            RoomHelper.getInstance(context).getDao().insertCity(databaseWeather);
                                        });
                                        Log.i(TAG, "onResponse: from network city");
                                    }
                                }

                                @Override
                                public void onFailure(Call<Weather> call, Throwable t) {
                                    if (t instanceof IOException)
                                        weatherObservable.postValue(new WeatherWrapper<>(null, Error.NO_INTERNET));
                                }
                            });
                            RetrofitClientInstance.getINSTANCE().getFiveDayWeather(cityId).enqueue(new Callback<WeatherList>() {
                                @Override
                                public void onResponse(Call<WeatherList> call, Response<WeatherList> response) {
                                    List<com.example.cliforcast.database.Weather> weathers = new ArrayList<>(4);
                                    if (response.body() != null) {
                                        for (int i = 1; i <= 4; i++) {
                                            weathers.add(new com.example.cliforcast.database.Weather.Builder()
                                                    .lat(locationResult.getLastLocation().getLatitude())
                                                    .lon(locationResult.getLastLocation().getLongitude())
                                                    .id(response.body().getWeather()[i * 8].getId())
                                                    .name(response.body().getWeather()[i * 8].getName())
                                                    .date(response.body().getWeather()[i * 8].getDate() * 1000)
                                                    .temp(response.body().getWeather()[i * 8].getMain().getTemp())
                                                    .temp_min(response.body().getWeather()[i * 8].getMain().getTemp_min())
                                                    .temp_max(response.body().getWeather()[i * 8].getMain().getTemp_max())
                                                    .pressure(response.body().getWeather()[i * 8].getMain().getPressure())
                                                    .humidity(response.body().getWeather()[i * 8].getMain().getHumidity())
                                                    .clouds(response.body().getWeather()[i * 8].getClouds().getClouds())
                                                    .day(i)
                                                    .wind(response.body().getWeather()[i * 8].getWind().getSpeed())
                                                    .condition(response.body().getWeather()[i * 8].getWeather()[0].getId())
                                                    .build());
                                        }
                                        weatherListObservable.postValue(new WeatherWrapper<>(weathers, Error.NO_ERROR));
                                        AsyncTask.execute(() -> {
                                            for (int i = 1; i <= 4; i++) {
                                                RoomHelper.getInstance(context).getDao().insertCity(
                                                        new com.example.cliforcast.database.Weather.Builder()
                                                                .lat(locationResult.getLastLocation().getLatitude())
                                                                .lon(locationResult.getLastLocation().getLongitude())
                                                                .id(cityId)
                                                                .name(response.body().getWeather()[i * 8].getName())
                                                                .date(date)
                                                                .temp(response.body().getWeather()[i * 8].getMain().getTemp())
                                                                .temp_min(response.body().getWeather()[i * 8].getMain().getTemp_min())
                                                                .temp_max(response.body().getWeather()[i * 8].getMain().getTemp_max())
                                                                .pressure(response.body().getWeather()[i * 8].getMain().getPressure())
                                                                .humidity(response.body().getWeather()[i * 8].getMain().getHumidity())
                                                                .clouds(response.body().getWeather()[i * 8].getClouds().getClouds())
                                                                .day(i)
                                                                .wind(response.body().getWeather()[i * 8].getWind().getSpeed())
                                                                .condition(response.body().getWeather()[i * 8].getWeather()[0].getId())
                                                                .build()
                                                );
                                            }
                                        });
                                    }
                                    Log.i(TAG, "onResponse: from network city list");
                                }

                                @Override
                                public void onFailure(Call<WeatherList> call, Throwable t) {
                                    if (t instanceof IOException)
                                        weatherListObservable.postValue(new WeatherWrapper<>(null, Error.NO_INTERNET));
                                }
                            });
                            preferences.edit()
                                    .putFloat(Constants.LAT_PREFERENCES, (float) locationResult.getLastLocation().getLatitude())
                                    .putFloat(Constants.LON_PREFERENCES, (float) locationResult.getLastLocation().getLongitude())
                                    .apply();
                        } else {
                            weatherObservable.postValue(new WeatherWrapper<>(databaseWeather, Error.NO_ERROR));
                            Log.i(TAG, "onResponse: from database city");

                            List<com.example.cliforcast.database.Weather> weathers = new ArrayList<>();
                            for (int i = 1; i <= 4; i++) {
                                com.example.cliforcast.database.Weather weather = RoomHelper.getInstance(context).getDao().getCityByCoord(
                                        locationResult.getLastLocation().getLatitude(),
                                        locationResult.getLastLocation().getLongitude()
                                );
                                weathers.add(weather);
                            }
                            weatherListObservable.postValue(new WeatherWrapper<>(weathers, Error.NO_ERROR));
                            Log.i(TAG, "onResponse: from database city list");
                        }
                    });
                }
            }, Looper.getMainLooper());

        }
//        RetrofitClientInstance.getINSTANCE().getWeather(lat, lon).enqueue(new Callback<Weather>() {
//            @Override
//            public void onResponse(Call<Weather> call, Response<Weather> response) {
//                if (response.body() != null) {
//                    requestedByLocation = true;
//                    com.example.cliforcast.database.Weather weather = new com.example.cliforcast.database.Weather.Builder()
//                            .id(response.body().getId())
//                            .name(response.body().getName())
//                            .date(response.body().getDate() * 1000)
//                            .temp(response.body().getMain().getTemp())
//                            .temp_min(response.body().getMain().getTemp_min())
//                            .temp_max(response.body().getMain().getTemp_max())
//                            .pressure(response.body().getMain().getPressure())
//                            .humidity(response.body().getMain().getHumidity())
//                            .clouds(response.body().getClouds().getClouds())
//                            .day(0)
//                            .wind(response.body().getWind().getSpeed())
//                            .condition(response.body().getWeather()[0].getId())
//                            .build();
//
//                    weatherObservable.postValue(new WeatherWrapper<>(weather, Error.NO_ERROR));
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Weather> call, Throwable t) {
//                if (t instanceof IOException)
//                    weatherObservable.postValue(new WeatherWrapper<>(null, Error.NO_INTERNET));
//            }
//        });
//        RetrofitClientInstance.getINSTANCE().getFiveDayWeather(lat, lon).enqueue(new Callback<WeatherList>() {
//            @Override
//            public void onResponse(Call<WeatherList> call, Response<WeatherList> response) {
//                List<com.example.cliforcast.database.Weather> weathers = new ArrayList<>(4);
//                if (response.body() != null) {
//                    for (int i = 1; i <= 4; i++) {
//                        weathers.add(new com.example.cliforcast.database.Weather.Builder()
//                                .id(response.body().getWeather()[i * 8].getId())
//                                .name(response.body().getWeather()[i * 8].getName())
//                                .date(response.body().getWeather()[i * 8].getDate() * 1000)
//                                .temp(response.body().getWeather()[i * 8].getMain().getTemp())
//                                .temp_min(response.body().getWeather()[i * 8].getMain().getTemp_min())
//                                .temp_max(response.body().getWeather()[i * 8].getMain().getTemp_max())
//                                .pressure(response.body().getWeather()[i * 8].getMain().getPressure())
//                                .humidity(response.body().getWeather()[i * 8].getMain().getHumidity())
//                                .clouds(response.body().getWeather()[i * 8].getClouds().getClouds())
//                                .day(i)
//                                .wind(response.body().getWeather()[i * 8].getWind().getSpeed())
//                                .condition(response.body().getWeather()[i * 8].getWeather()[0].getId())
//                                .build());
//                    }
//                    weatherListObservable.postValue(new WeatherWrapper<>(weathers, Error.NO_ERROR));
//                }
//            }
//
//            @Override
//            public void onFailure(Call<WeatherList> call, Throwable t) {
//                if (t instanceof IOException)
//                    weatherListObservable.postValue(new WeatherWrapper<>(null, Error.NO_INTERNET));
//            }
//        });
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }


    public boolean isRequestedByLocation() {
        return requestedByLocation;
    }
}
