package com.example.cliforcast.network;

public class WeatherWrapper<E> {
    private E weather;
    private Error error;

    public WeatherWrapper(E weather, Error error) {
        this.weather = weather;
        this.error = error;
    }

    public E getWeather() {
        return weather;
    }

    public void setWeather(E weather) {
        this.weather = weather;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }
}
