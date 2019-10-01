package com.example.cliforcast.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClientInstance {
    private static Retrofit INSTANCE;
    private static final String BASE_URL = "https://api.openweathermap.org";

    public static GetWeatherService getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return INSTANCE.create(GetWeatherService.class);
    }
}
