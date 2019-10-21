package com.example.cliforcast.viewModel;

import android.icu.text.IDNA;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cliforcast.network.RetrofitClientInstance;
import com.example.cliforcast.network.Weather;
import com.example.cliforcast.network.WeatherList;
import com.example.cliforcast.network.Error;
import com.example.cliforcast.network.WeatherWrapper;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CurrentWeatherViewModel extends ViewModel {
    private int cityId;
    private double lat;
    private double lon;
    private MutableLiveData<WeatherWrapper<Weather>> weatherObservable = new MutableLiveData<>();
    private MutableLiveData<WeatherWrapper<WeatherList>> weatherListObservable = new MutableLiveData<>();
    private boolean requestedByLocation = false;

    public CurrentWeatherViewModel(int cityId) {
        this.cityId = cityId;
    }

    public MutableLiveData<WeatherWrapper<Weather>> getWeather() {
        if (weatherObservable != null)
            return weatherObservable;
        return null;
    }

    public MutableLiveData<WeatherWrapper<WeatherList>> getWeatherList() {
        if (weatherListObservable != null)
            return weatherListObservable;
        return null;
    }

    public void requestWeatherByCityID() {
        weatherObservable.setValue(new WeatherWrapper<>(null, Error.REQUEST_NOT_COMPELLED));
        weatherListObservable.setValue(new WeatherWrapper<>(null, Error.REQUEST_NOT_COMPELLED));
        RetrofitClientInstance.getINSTANCE().getWeather(cityId).enqueue(new Callback<Weather>() {
            @Override
            public void onResponse(Call<Weather> call, Response<Weather> response) {
                if (response.body() != null) {
                    requestedByLocation = false;
                    weatherObservable.postValue(new WeatherWrapper<>(response.body(),Error.NO_ERROR));
                }
            }

            @Override
            public void onFailure(Call<Weather> call, Throwable t) {
                if (t instanceof IOException)
                    weatherObservable.postValue(new WeatherWrapper<>(null,Error.NO_INTERNET));
            }
        });
        RetrofitClientInstance.getINSTANCE().getFiveDayWeather(cityId).enqueue(new Callback<WeatherList>() {
            @Override
            public void onResponse(Call<WeatherList> call, Response<WeatherList> response) {
                if (response.body() != null)
                    weatherListObservable.postValue(new WeatherWrapper<>(response.body(),Error.NO_ERROR));
            }

            @Override
            public void onFailure(Call<WeatherList> call, Throwable t) {
                if (t instanceof IOException)
                    weatherListObservable.postValue(new WeatherWrapper<>(null,Error.NO_INTERNET));
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
                    weatherObservable.postValue(new WeatherWrapper<>(response.body(),Error.NO_ERROR));

                }
            }

            @Override
            public void onFailure(Call<Weather> call, Throwable t) {
                if(t instanceof IOException)
                    weatherObservable.postValue(new WeatherWrapper<>(null,Error.NO_INTERNET));
            }
        });
        RetrofitClientInstance.getINSTANCE().getFiveDayWeather(lat, lon).enqueue(new Callback<WeatherList>() {
            @Override
            public void onResponse(Call<WeatherList> call, Response<WeatherList> response) {
                if (response.body() != null)
                    weatherListObservable.postValue(new WeatherWrapper<>(response.body(),Error.NO_ERROR));
            }

            @Override
            public void onFailure(Call<WeatherList> call, Throwable t) {
                if(t instanceof IOException)
                    weatherObservable.postValue(new WeatherWrapper<>(null,Error.NO_INTERNET));
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
