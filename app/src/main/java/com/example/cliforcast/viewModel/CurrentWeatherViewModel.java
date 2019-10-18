package com.example.cliforcast.viewModel;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cliforcast.network.RetrofitClientInstance;
import com.example.cliforcast.network.Weather;
import com.example.cliforcast.network.WeatherList;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CurrentWeatherViewModel extends ViewModel {
    private int cityId;
    private MutableLiveData<Weather> weatherObservable = new MutableLiveData<>();
    private MutableLiveData<WeatherList> weatherListObservable = new MutableLiveData<>();

    public CurrentWeatherViewModel(int cityId) {
        this.cityId = cityId;
    }

    public MutableLiveData<Weather> getWeather() {
        if (weatherObservable != null)
            return weatherObservable;
        return null;
    }

    public MutableLiveData<WeatherList> getWeatherList() {
        if (weatherListObservable != null)
            return weatherListObservable;
        return null;
    }

    public void requestWeather() {
        RetrofitClientInstance.getINSTANCE().getWeather(cityId).enqueue(new Callback<Weather>() {
            @Override
            public void onResponse(Call<Weather> call, Response<Weather> response) {
                if (response.body() != null)
                    weatherObservable.postValue(response.body());
            }

            @Override
            public void onFailure(Call<Weather> call, Throwable t) {

            }
        });
        RetrofitClientInstance.getINSTANCE().getFiveDayWeather(cityId).enqueue(new Callback<WeatherList>() {
            @Override
            public void onResponse(Call<WeatherList> call, Response<WeatherList> response) {
                if (response.body() != null)
                    weatherListObservable.postValue(response.body());
            }

            @Override
            public void onFailure(Call<WeatherList> call, Throwable t) {

            }
        });
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}
