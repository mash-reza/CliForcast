package com.example.cliforcast.viewModel;

import android.content.Context;
import android.content.SharedPreferences;
import android.icu.text.IDNA;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.room.Room;

import com.example.cliforcast.Util.Constants;
import com.example.cliforcast.Util.Utility;
import com.example.cliforcast.database.RoomHelper;
import com.example.cliforcast.network.RetrofitClientInstance;
import com.example.cliforcast.network.Weather;
import com.example.cliforcast.network.WeatherList;
import com.example.cliforcast.network.Error;
import com.example.cliforcast.network.WeatherWrapper;

import java.io.IOException;
import java.lang.invoke.CallSite;
import java.util.ArrayList;
import java.util.Collections;
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

    public CurrentWeatherViewModel(Context context, int cityId) {
        this.context = context;
        this.cityId = cityId;
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
                    RoomHelper.getInstance(context).getDao().getCity(cityId, 0);
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
            } else if (Utility.isWeatherExpired(databaseWeather.getDate())) {
                RetrofitClientInstance.getINSTANCE().getWeather(cityId).enqueue(new Callback<Weather>() {
                    @Override
                    public void onResponse(Call<Weather> call, Response<Weather> response) {
                        if (response.body() != null) {
                            requestedByLocation = false;
                            com.example.cliforcast.database.Weather weather = new com.example.cliforcast.database.Weather.Builder()
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
            } else {
                weatherObservable.postValue(new WeatherWrapper<>(databaseWeather, Error.NO_ERROR));
                Log.i(TAG, "onResponse: from database city");

                List<com.example.cliforcast.database.Weather> weathers = new ArrayList<>();
                for (int i = 1; i <= 4; i++) {
                    com.example.cliforcast.database.Weather weather = RoomHelper.getInstance(context).getDao().getCity(cityId, i);
                    weathers.add(weather);
                }
                weatherListObservable.postValue(new WeatherWrapper<>(weathers, Error.NO_ERROR));
                Log.i(TAG, "onResponse: from database city list");
            }
        });
    }

    public void requestWeatherByLatLon() {
        weatherObservable.setValue(new WeatherWrapper<>(null, Error.REQUEST_NOT_COMPELLED));
        weatherListObservable.setValue(new WeatherWrapper<>(null, Error.REQUEST_NOT_COMPELLED));
        RetrofitClientInstance.getINSTANCE().getWeather(lat, lon).enqueue(new Callback<Weather>() {
            @Override
            public void onResponse(Call<Weather> call, Response<Weather> response) {
                if (response.body() != null) {
                    requestedByLocation = true;
                    com.example.cliforcast.database.Weather weather = new com.example.cliforcast.database.Weather.Builder()
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
                }
            }

            @Override
            public void onFailure(Call<Weather> call, Throwable t) {
                if (t instanceof IOException)
                    weatherObservable.postValue(new WeatherWrapper<>(null, Error.NO_INTERNET));
            }
        });
        RetrofitClientInstance.getINSTANCE().getFiveDayWeather(lat, lon).enqueue(new Callback<WeatherList>() {
            @Override
            public void onResponse(Call<WeatherList> call, Response<WeatherList> response) {
                List<com.example.cliforcast.database.Weather> weathers = new ArrayList<>(4);
                if (response.body() != null) {
                    for (int i = 1; i <= 4; i++) {
                        weathers.add(new com.example.cliforcast.database.Weather.Builder()
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
                }
            }
            @Override
            public void onFailure(Call<WeatherList> call, Throwable t) {
                if (t instanceof IOException)
                    weatherListObservable.postValue(new WeatherWrapper<>(null, Error.NO_INTERNET));
            }
        });
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public void setLatLon(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public boolean isRequestedByLocation() {
        return requestedByLocation;
    }
}
