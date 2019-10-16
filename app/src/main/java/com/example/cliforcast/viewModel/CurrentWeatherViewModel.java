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
    private Application application;
    private int cityId;
    private MutableLiveData<Weather> weatherObservable = new MutableLiveData<>();
    private MutableLiveData<WeatherList> weatherListObservable= new MutableLiveData<>();

    public CurrentWeatherViewModel(int cityId, Application application) {
        this.cityId = cityId;
        this.application = application;
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
    }

    public MutableLiveData<Weather> getWeather() {
        return weatherObservable;
    }

    public MutableLiveData<WeatherList> getWeatherList() {
        return weatherListObservable;
    }
}
