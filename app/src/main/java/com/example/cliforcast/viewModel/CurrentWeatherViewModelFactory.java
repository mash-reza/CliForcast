package com.example.cliforcast.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class CurrentWeatherViewModelFactory implements ViewModelProvider.Factory {
    private Application application;
    private int cityId;

    public CurrentWeatherViewModelFactory(Application application, int cityId) {
        this.application = application;
        this.cityId = cityId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return modelClass.cast(new CurrentWeatherViewModel(cityId, application));
    }
}
