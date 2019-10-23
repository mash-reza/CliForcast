package com.example.cliforcast.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SearchEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cliforcast.R;
import com.example.cliforcast.Util.Constants;
import com.example.cliforcast.Util.Utility;
import com.example.cliforcast.network.Error;
import com.example.cliforcast.network.Weather;
import com.example.cliforcast.network.WeatherList;
import com.example.cliforcast.viewModel.CurrentWeatherViewModel;
import com.example.cliforcast.viewModel.CurrentWeatherViewModelFactory;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CurrentWeather extends AppCompatActivity {
    private static final String TAG = "CurrentWeather";
    private SharedPreferences preferences;


    private com.example.cliforcast.database.Weather currentWeather;
    private List<com.example.cliforcast.database.Weather> fiveDayWeather;

    private int cityIndexInArray;

    //GoogleApiClient client;
    LocationManager locationManager;

    InputStream in;
    Reader reader;
    private List<City> cities;
    City[] citiesArray;

    ConstraintLayout currentWeatherLayout;
    RecyclerView currentWeatherSearchRecyclerView;
    LinearLayout currentWeatherLoadingLinearLayout;
    ImageView currentWeatherErrorImageView;
    TextView currentWeatherErrorTextView;
    LinearLayout currentWeatherErrorLinearLayout;

    TextView currentWeatherDateTextView;
    TextView currentWeatherTemperatureTextView;
    TextView currentWeatherCityNameTextView;
    ImageView currentWeatherStatusImageView;
    TextView currentWeatherDescriptionTextView;

    TextView currentWeatherMaxTempTextView;
    TextView currentWeatherMinTempTextView;
    TextView currentWeatherWindTextView;
    TextView currentWeatherHumidityTextView;
    TextView currentWeatherCloudsTextView;

    TextView currentWeatherNowTempTextView;
    ImageView currentWeatherNowIconImageView;
    TextView currentWeatherFirstDayMaxTempTextView;
    ImageView currentWeatherFirstDayIconImageView;
    TextView currentWeatherFirstDayMinTempTextView;
    TextView currentWeatherSecondDayMaxTempTextView;
    ImageView currentWeatherSecondDayIconImageView;
    TextView currentWeatherSecondDayMinTempTextView;
    TextView currentWeatherThirdDayMaxTempTextView;
    ImageView currentWeatherThirdDayIconImageView;
    TextView currentWeatherThirdDayMinTempTextView;
    TextView currentWeatherForthDayMaxTempTextView;
    ImageView currentWeatherForthDayIconImageView;
    TextView currentWeatherForthDayMinTempTextView;

    RecyclerView dialogRecyclerView;

    Menu menu;


    CurrentWeatherViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_weather);
        preferences = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);

//        viewModel = new CurrentWeatherViewModelFactory(getApplication(),preferences.getInt(Constants.CITYID,0)).create(new CurrentWeatherViewModel());
        viewModel = ViewModelProviders.of(this, new CurrentWeatherViewModelFactory(
                getApplication(), preferences.getInt(Constants.CITYID, 0)
        )).get(CurrentWeatherViewModel.class);


        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        in = getApplicationContext().getResources().openRawResource(R.raw.iran_cities_fa);
        reader = new BufferedReader(new InputStreamReader(in));
        cities = new ArrayList<>();
        citiesArray = new Gson().fromJson(reader, City[].class);
        setIndexInArray(preferences.getInt(Constants.CITYID, 0));

        checkPermission();

        fillCitiesList();
        initUi();
        if (preferences.getBoolean(Constants.FIRST_LAUNCH_PREFERENCES, true)) {
            currentWeatherLayout.setVisibility(View.GONE);
            showChooseCityDialog();
        } else {
            viewModel.requestWeatherByCityID();
            observeViewModel(viewModel);
        }
        getSearchIntent();
    }

    private void observeViewModel(CurrentWeatherViewModel viewModel) {

        viewModel.getWeather().observe(this, weather -> {
            if (weather != null && weather.getError().equals(Error.NO_ERROR)) {
                currentWeatherSearchRecyclerView.setVisibility(View.GONE);
                currentWeatherErrorLinearLayout.setVisibility(View.GONE);
                currentWeatherLoadingLinearLayout.setVisibility(View.GONE);
                currentWeatherLayout.setVisibility(View.VISIBLE);
                currentWeather = weather.getWeather();
                currentWeatherTemperatureTextView.setText(Utility.kelvinToCelsius(weather.getWeather().getTemp()) + "°");
                if (!viewModel.isRequestedByLocation())
                    currentWeatherCityNameTextView.setText(citiesArray[cityIndexInArray].name);
                else
                    currentWeatherCityNameTextView.setText(weather.getWeather().getName());
                currentWeatherStatusImageView.setImageResource(Utility.idToConditionMapper(
                        weather.getWeather().getCondition()
                ));
                currentWeatherDateTextView.setText(Utility.epochToDate(weather.getWeather().getDate()));
                currentWeatherDescriptionTextView.setText(Utility.idToStringMapper(
                        getApplicationContext(), weather.getWeather().getCondition()
                ));
                currentWeatherMaxTempTextView.setText(Utility.kelvinToCelsius(weather.getWeather().getTemp_max()) + "°");
                currentWeatherMinTempTextView.setText(Utility.kelvinToCelsius(weather.getWeather().getTemp_min()) + "°");
                currentWeatherWindTextView.setText(Utility.mphToKmh(weather.getWeather().getWind()) + " Kmh");
                currentWeatherCloudsTextView.setText(weather.getWeather().getClouds() + "%");
                currentWeatherHumidityTextView.setText(weather.getWeather().getHumidity() + "%");
                currentWeatherNowIconImageView.setImageResource(
                        Utility.idToConditionMapper(
                                weather.getWeather().getCondition()
                        )
                );
                currentWeatherNowTempTextView.setText(Utility.kelvinToCelsius(weather.getWeather().getTemp()) + "°");
            } else {
                switch (weather.getError()) {
                    case REQUEST_NOT_COMPELLED:
                        currentWeatherSearchRecyclerView.setVisibility(View.GONE);
                        currentWeatherLayout.setVisibility(View.GONE);
                        currentWeatherErrorLinearLayout.setVisibility(View.GONE);
                        currentWeatherLoadingLinearLayout.setVisibility(View.VISIBLE);
                        break;
                    case NO_INTERNET:
                        currentWeatherLayout.setVisibility(View.GONE);
                        currentWeatherSearchRecyclerView.setVisibility(View.GONE);
                        currentWeatherLoadingLinearLayout.setVisibility(View.GONE);
                        currentWeatherErrorImageView.setImageResource(R.drawable.no_internet);
                        currentWeatherErrorTextView.setText(R.string.no_internet_problem);
                        currentWeatherErrorLinearLayout.setVisibility(View.VISIBLE);
                        break;
                    case NO_LOCATION:
                        currentWeatherLayout.setVisibility(View.GONE);
                        currentWeatherSearchRecyclerView.setVisibility(View.GONE);
                        currentWeatherLoadingLinearLayout.setVisibility(View.GONE);
                        currentWeatherErrorImageView.setImageResource(R.drawable.no_location);
                        currentWeatherErrorTextView.setText(R.string.no_location_problem);
                        currentWeatherErrorLinearLayout.setVisibility(View.VISIBLE);
                        break;
                    case COMMON:
                        currentWeatherLayout.setVisibility(View.GONE);
                        currentWeatherSearchRecyclerView.setVisibility(View.GONE);
                        currentWeatherLoadingLinearLayout.setVisibility(View.GONE);
                        currentWeatherErrorImageView.setImageResource(R.drawable.error);
                        currentWeatherErrorTextView.setText(R.string.common_problem);
                        currentWeatherErrorLinearLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        viewModel.getWeatherList().observe(this, weatherList -> {
            if (weatherList != null && weatherList.getError().equals(Error.NO_ERROR)) {
                fiveDayWeather = weatherList.getWeather();
                currentWeatherFirstDayMaxTempTextView.setText(Utility.kelvinToCelsius(weatherList.getWeather().get(0).getTemp_max()) + "°");
                currentWeatherFirstDayMinTempTextView.setText(Utility.kelvinToCelsius(weatherList.getWeather().get(0).getTemp_min()) + "°");
                currentWeatherFirstDayIconImageView.setImageResource(Utility.idToConditionMapper(weatherList.getWeather().get(0).getCondition()));
                currentWeatherSecondDayMaxTempTextView.setText(Utility.kelvinToCelsius(weatherList.getWeather().get(1).getTemp_max()) + "°");
                currentWeatherSecondDayMinTempTextView.setText(Utility.kelvinToCelsius(weatherList.getWeather().get(1).getTemp_min()) + "°");
                currentWeatherSecondDayIconImageView.setImageResource(Utility.idToConditionMapper(weatherList.getWeather().get(1).getCondition()));
                currentWeatherThirdDayMaxTempTextView.setText(Utility.kelvinToCelsius(weatherList.getWeather().get(2).getTemp_max()) + "°");
                currentWeatherThirdDayMinTempTextView.setText(Utility.kelvinToCelsius(weatherList.getWeather().get(2).getTemp_min()) + "°");
                currentWeatherThirdDayIconImageView.setImageResource(Utility.idToConditionMapper(weatherList.getWeather().get(2).getCondition()));
                currentWeatherForthDayMaxTempTextView.setText(Utility.kelvinToCelsius(weatherList.getWeather().get(3).getTemp_max()) + "°");
                currentWeatherForthDayMinTempTextView.setText(Utility.kelvinToCelsius(weatherList.getWeather().get(3).getTemp_min()) + "°");
                currentWeatherForthDayIconImageView.setImageResource(Utility.idToConditionMapper(weatherList.getWeather().get(3).getCondition()));
            } else {
                switch (weatherList.getError()) {
                    case REQUEST_NOT_COMPELLED:
                        currentWeatherSearchRecyclerView.setVisibility(View.GONE);
                        currentWeatherLayout.setVisibility(View.GONE);
                        currentWeatherErrorLinearLayout.setVisibility(View.GONE);
                        currentWeatherLoadingLinearLayout.setVisibility(View.VISIBLE);
                        break;
                    case NO_INTERNET:
                        currentWeatherLayout.setVisibility(View.GONE);
                        currentWeatherSearchRecyclerView.setVisibility(View.GONE);
                        currentWeatherLoadingLinearLayout.setVisibility(View.GONE);
                        currentWeatherErrorImageView.setImageResource(R.drawable.no_internet);
                        currentWeatherErrorTextView.setText(R.string.no_internet_problem);
                        currentWeatherErrorLinearLayout.setVisibility(View.VISIBLE);
                        break;
                    case NO_LOCATION:
                        currentWeatherLayout.setVisibility(View.GONE);
                        currentWeatherSearchRecyclerView.setVisibility(View.GONE);
                        currentWeatherLoadingLinearLayout.setVisibility(View.GONE);
                        currentWeatherErrorImageView.setImageResource(R.drawable.no_location);
                        currentWeatherErrorTextView.setText(R.string.no_location_problem);
                        currentWeatherErrorLinearLayout.setVisibility(View.VISIBLE);
                        break;
                    case COMMON:
                        currentWeatherLayout.setVisibility(View.GONE);
                        currentWeatherSearchRecyclerView.setVisibility(View.GONE);
                        currentWeatherLoadingLinearLayout.setVisibility(View.GONE);
                        currentWeatherErrorImageView.setImageResource(R.drawable.error);
                        currentWeatherErrorTextView.setText(R.string.common_problem);
                        currentWeatherErrorLinearLayout.setVisibility(View.VISIBLE);

                }
            }
        });
    }


    private void setIndexInArray(int cityID) {
        if (cityID != 0)
            for (int i = 0; i < citiesArray.length; i++) {
                if (citiesArray[i].id == cityID)
                    cityIndexInArray = i;
            }
    }

    private void fillCitiesList() {
        this.cities.clear();
        Collections.addAll(cities, citiesArray);
    }

    private void showChooseCityDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).setCancelable(false).create();
        View dialog = LayoutInflater.from(getApplicationContext()).inflate(R.layout.select_city_dialog, null);
        EditText dialogEditText = dialog.findViewById(R.id.selectCityDialogEditText);
        dialogRecyclerView = dialog.findViewById(R.id.selectCityDialogRecyclerView);
        dialogRecyclerView.setLayoutManager(
                new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false)
        );
        dialogRecyclerView.setAdapter(new DialogAdapter(getApplicationContext(), cities, id -> {
            preferences.edit().putInt(Constants.CITYID, id).apply();
            setIndexInArray(id);
            viewModel.setCityId(id);
            viewModel.requestWeatherByCityID();
            observeViewModel(viewModel);
            alertDialog.cancel();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(Constants.FIRST_LAUNCH_PREFERENCES, false).apply();
        }));
        dialogEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.equals("")) {
                    dialogRecyclerView.setAdapter(null);
                    dialogRecyclerView.setAdapter(new DialogAdapter(getApplicationContext(), cities, id -> {
                        preferences.edit().putInt(Constants.CITYID, id).apply();
                        setIndexInArray(id);
                        viewModel.setCityId(id);
                        viewModel.requestWeatherByCityID();
                        alertDialog.cancel();
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean(Constants.FIRST_LAUNCH_PREFERENCES, false).apply();
                    }));
                } else {
                    List<City> cities = new ArrayList<>();
                    for (int i = 0; i < CurrentWeather.this.cities.size(); i++) {
                        if (CurrentWeather.this.cities.get(i).name.toLowerCase().matches(s + ".*"))
                            cities.add(CurrentWeather.this.cities.get(i));
                    }
                    dialogRecyclerView.setAdapter(null);
                    dialogRecyclerView.setAdapter(new DialogAdapter(getApplicationContext(), cities, id -> {
                        preferences.edit().putInt(Constants.CITYID, id).apply();
                        setIndexInArray(id);
                        viewModel.setCityId(id);
                        viewModel.requestWeatherByCityID();
                        alertDialog.cancel();
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean(Constants.FIRST_LAUNCH_PREFERENCES, false).apply();
                    }));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        alertDialog.setView(dialog);
        alertDialog.show();
    }

    private void initUi() {
        currentWeatherLayout = findViewById(R.id.currentWeatherLayout);
        currentWeatherSearchRecyclerView = findViewById(R.id.currentWeatherSearchRecyclerView);
        currentWeatherLoadingLinearLayout = findViewById(R.id.currentWeatherLoadingLinearLayout);
        currentWeatherErrorImageView = findViewById(R.id.currentWeatherErrorImageView);
        currentWeatherErrorTextView = findViewById(R.id.currentWeatherErrorTextView);
        currentWeatherErrorLinearLayout = findViewById(R.id.currentWeatherErrorLinearLayout);
        currentWeatherSearchRecyclerView.setLayoutManager(
                new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false)
        );
        SearchAdapter adapter = new SearchAdapter(getApplicationContext(), cities, (id) -> {
            currentWeatherSearchRecyclerView.setVisibility(View.GONE);
            currentWeatherLayout.setVisibility(View.VISIBLE);
            preferences.edit().putInt(Constants.CITYID, id).apply();
            setIndexInArray(id);
            viewModel.setCityId(id);
            viewModel.requestWeatherByCityID();
        });
        currentWeatherSearchRecyclerView.setAdapter(adapter);

        currentWeatherDateTextView = findViewById(R.id.currentWeatherDateTextView);
        currentWeatherTemperatureTextView = findViewById(R.id.currentWeatherTemperatureTextView);
        currentWeatherCityNameTextView = findViewById(R.id.currentWeatherCityNameTextView);
        currentWeatherStatusImageView = findViewById(R.id.currentWeatherStatusImageView);
        currentWeatherDescriptionTextView = findViewById(R.id.currentWeatherDescriptionTextView);
        currentWeatherMaxTempTextView = findViewById(R.id.currentWeatherMaxTempTextView);
        currentWeatherMinTempTextView = findViewById(R.id.currentWeatherMinTempTextView);
        currentWeatherWindTextView = findViewById(R.id.currentWeatherWindTextView);
        currentWeatherHumidityTextView = findViewById(R.id.currentWeatherHumidityTextView);
        currentWeatherCloudsTextView = findViewById(R.id.currentWeatherCloudsTextView);
        currentWeatherNowTempTextView = findViewById(R.id.currentWeatherNowTempTextView);
        currentWeatherNowIconImageView = findViewById(R.id.currentWeatherNowIconImageView);
        currentWeatherFirstDayMaxTempTextView = findViewById(R.id.currentWeatherFirstDayMaxTempTextView);
        currentWeatherFirstDayIconImageView = findViewById(R.id.currentWeatherFirstDayIconImageView);
        currentWeatherFirstDayMinTempTextView = findViewById(R.id.currentWeatherFirstDayMinTempTextView);
        currentWeatherSecondDayMaxTempTextView = findViewById(R.id.currentWeatherSecondDayMaxTempTextView);
        currentWeatherSecondDayIconImageView = findViewById(R.id.currentWeatherSecondDayIconImageView);
        currentWeatherSecondDayMinTempTextView = findViewById(R.id.currentWeatherSecondDayMinTempTextView);
        currentWeatherThirdDayMaxTempTextView = findViewById(R.id.currentWeatherThirdDayMaxTempTextView);
        currentWeatherThirdDayIconImageView = findViewById(R.id.currentWeatherThirdDayIconImageView);
        currentWeatherThirdDayMinTempTextView = findViewById(R.id.currentWeatherThirdDayMinTempTextView);
        currentWeatherForthDayMaxTempTextView = findViewById(R.id.currentWeatherForthDayMaxTempTextView);
        currentWeatherForthDayIconImageView = findViewById(R.id.currentWeatherForthDayIconImageView);
        currentWeatherForthDayMinTempTextView = findViewById(R.id.currentWeatherForthDayMinTempTextView);

    }

    @Override
    protected void onResume() {
        super.onResume();
        fiveDayEventControl();
    }

    private void fiveDayEventControl() {
        currentWeatherNowIconImageView.setOnClickListener(v -> {
            if (currentWeather != null) {
                currentWeatherTemperatureTextView.setText(Utility.kelvinToCelsius(currentWeather.getTemp()) + "°");
                if (!viewModel.isRequestedByLocation())
                    currentWeatherCityNameTextView.setText(citiesArray[cityIndexInArray].name);
                else
                    currentWeatherCityNameTextView.setText(currentWeather.getName());
                currentWeatherStatusImageView.setImageResource(Utility.idToConditionMapper(
                        currentWeather.getCondition()
                ));
                currentWeatherDateTextView.setText(Utility.epochToDate(currentWeather.getDate()));
                currentWeatherDescriptionTextView.setText(Utility.idToStringMapper(
                        getApplicationContext(), currentWeather.getCondition()
                ));
                currentWeatherMaxTempTextView.setText(Utility.kelvinToCelsius(currentWeather.getTemp_max()) + "°");
                currentWeatherMinTempTextView.setText(Utility.kelvinToCelsius(currentWeather.getTemp_min()) + "°");
                currentWeatherWindTextView.setText(Utility.mphToKmh(currentWeather.getWind()) + " Kmh");
                currentWeatherCloudsTextView.setText(currentWeather.getClouds() + "%");
                currentWeatherHumidityTextView.setText(currentWeather.getHumidity() + "%");
                currentWeatherNowIconImageView.setImageResource(
                        Utility.idToConditionMapper(
                                currentWeather.getCondition()
                        )
                );
                currentWeatherNowTempTextView.setText(Utility.kelvinToCelsius(currentWeather.getTemp()) + "°");
            }
        });
        currentWeatherFirstDayIconImageView.setOnClickListener(v -> {
            if (currentWeather != null && fiveDayWeather != null) {
                Toast.makeText(this, R.string.day1, Toast.LENGTH_SHORT).show();
                currentWeatherTemperatureTextView.setText(Utility.kelvinToCelsius(fiveDayWeather.get(0).getTemp()) + "°");
                if (!viewModel.isRequestedByLocation())
                    currentWeatherCityNameTextView.setText(citiesArray[cityIndexInArray].name);
                else
                    currentWeatherCityNameTextView.setText(currentWeather.getName());
                currentWeatherStatusImageView.setImageResource(Utility.idToConditionMapper(
                        fiveDayWeather.get(0).getCondition()
                ));
                currentWeatherDateTextView.setText(Utility.epochToDate(fiveDayWeather.get(0).getDate()));
                currentWeatherDescriptionTextView.setText(Utility.idToStringMapper(
                        getApplicationContext(), fiveDayWeather.get(1).getCondition()
                ));
                currentWeatherMaxTempTextView.setText(Utility.kelvinToCelsius(fiveDayWeather.get(0).getTemp_max()) + "°");
                currentWeatherMinTempTextView.setText(Utility.kelvinToCelsius(fiveDayWeather.get(0).getTemp_min()) + "°");
                currentWeatherWindTextView.setText(Utility.mphToKmh(fiveDayWeather.get(0).getWind()) + " Kmh");
                currentWeatherCloudsTextView.setText(fiveDayWeather.get(0).getClouds() + "%");
                currentWeatherHumidityTextView.setText(fiveDayWeather.get(0).getHumidity() + "%");
            }
        });
        currentWeatherSecondDayIconImageView.setOnClickListener(v -> {
            if (currentWeather != null && fiveDayWeather != null) {
                Toast.makeText(this, R.string.day2, Toast.LENGTH_SHORT).show();
                currentWeatherTemperatureTextView.setText(Utility.kelvinToCelsius(fiveDayWeather.get(1).getTemp()) + "°");
                if (!viewModel.isRequestedByLocation())
                    currentWeatherCityNameTextView.setText(citiesArray[cityIndexInArray].name);
                else
                    currentWeatherCityNameTextView.setText(currentWeather.getName());
                currentWeatherStatusImageView.setImageResource(Utility.idToConditionMapper(
                        fiveDayWeather.get(1).getCondition()
                ));
                currentWeatherDateTextView.setText(Utility.epochToDate(fiveDayWeather.get(1).getDate()));
                currentWeatherDescriptionTextView.setText(Utility.idToStringMapper(
                        getApplicationContext(), fiveDayWeather.get(1).getCondition()
                ));
                currentWeatherMaxTempTextView.setText(Utility.kelvinToCelsius(fiveDayWeather.get(1).getTemp_max()) + "°");
                currentWeatherMinTempTextView.setText(Utility.kelvinToCelsius(fiveDayWeather.get(1).getTemp_min()) + "°");
                currentWeatherWindTextView.setText(Utility.mphToKmh(fiveDayWeather.get(1).getWind()) + " Kmh");
                currentWeatherCloudsTextView.setText(fiveDayWeather.get(1).getClouds() + "%");
                currentWeatherHumidityTextView.setText(fiveDayWeather.get(1).getHumidity() + "%");
            }
        });
        currentWeatherThirdDayIconImageView.setOnClickListener(v -> {
            if (currentWeather != null && fiveDayWeather != null) {
                Toast.makeText(this, R.string.day3, Toast.LENGTH_SHORT).show();
                currentWeatherTemperatureTextView.setText(Utility.kelvinToCelsius(fiveDayWeather.get(2).getTemp()) + "°");
                if (!viewModel.isRequestedByLocation())
                    currentWeatherCityNameTextView.setText(citiesArray[cityIndexInArray].name);
                else
                    currentWeatherCityNameTextView.setText(currentWeather.getName());
                currentWeatherStatusImageView.setImageResource(Utility.idToConditionMapper(
                        fiveDayWeather.get(2).getCondition()
                ));
                currentWeatherDateTextView.setText(Utility.epochToDate(fiveDayWeather.get(2).getDate()));
                currentWeatherDescriptionTextView.setText(Utility.idToStringMapper(
                        getApplicationContext(), fiveDayWeather.get(2).getCondition()
                ));
                currentWeatherMaxTempTextView.setText(Utility.kelvinToCelsius(fiveDayWeather.get(2).getTemp_max()) + "°");
                currentWeatherMinTempTextView.setText(Utility.kelvinToCelsius(fiveDayWeather.get(2).getTemp_min()) + "°");
                currentWeatherWindTextView.setText(Utility.mphToKmh(fiveDayWeather.get(2).getWind()) + " Kmh");
                currentWeatherCloudsTextView.setText(fiveDayWeather.get(2).getClouds() + "%");
                currentWeatherHumidityTextView.setText(fiveDayWeather.get(2).getHumidity() + "%");
            }
        });
        currentWeatherForthDayIconImageView.setOnClickListener(v -> {
            if (currentWeather != null && fiveDayWeather != null) {
                Toast.makeText(this, R.string.day4, Toast.LENGTH_SHORT).show();
                currentWeatherTemperatureTextView.setText(Utility.kelvinToCelsius(fiveDayWeather.get(3).getTemp()) + "°");
                if (!viewModel.isRequestedByLocation())
                    currentWeatherCityNameTextView.setText(citiesArray[cityIndexInArray].name);
                else
                    currentWeatherCityNameTextView.setText(currentWeather.getName());
                currentWeatherStatusImageView.setImageResource(Utility.idToConditionMapper(
                        fiveDayWeather.get(3).getCondition()
                ));
                currentWeatherDateTextView.setText(Utility.epochToDate(fiveDayWeather.get(3).getDate()));
                currentWeatherDescriptionTextView.setText(Utility.idToStringMapper(
                        getApplicationContext(), fiveDayWeather.get(3).getCondition()
                ));
                currentWeatherMaxTempTextView.setText(Utility.kelvinToCelsius(fiveDayWeather.get(3).getTemp_max()) + "°");
                currentWeatherMinTempTextView.setText(Utility.kelvinToCelsius(fiveDayWeather.get(3).getTemp_min()) + "°");
                currentWeatherWindTextView.setText(Utility.mphToKmh(fiveDayWeather.get(3).getWind()) + " Kmh");
                currentWeatherCloudsTextView.setText(fiveDayWeather.get(3).getClouds() + "%");
                currentWeatherHumidityTextView.setText(fiveDayWeather.get(3).getHumidity() + "%");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem locationMenuItem = menu.findItem(R.id.currentWeatherLocationMenuItem);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            locationMenuItem.setIcon(R.drawable.location_on_icon);
        else
            locationMenuItem.setIcon(R.drawable.location_off_icon);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    @SuppressLint("MissingPermission")
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.currentWeatherSearchMenuItem:
                onSearchRequested();
                return true;
            case R.id.currentWeatherLocationMenuItem:
                viewModel.requestWeatherByLatLon();
            case R.id.currentWeatherRefreshMenuItem:
                MenuItem locationMenuItem = this.menu.findItem(R.id.currentWeatherLocationMenuItem);
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    locationMenuItem.setIcon(R.drawable.location_on_icon);
                else
                    locationMenuItem.setIcon(R.drawable.location_off_icon);

                if (!viewModel.isRequestedByLocation()) {
                    viewModel.requestWeatherByCityID();
                } else {
                    viewModel.requestWeatherByLatLon();
                }
                return true;
            default:
                return true;
        }
    }

    @Override
    public boolean onSearchRequested(@Nullable SearchEvent searchEvent) {
        return super.onSearchRequested(searchEvent);

    }

    private void getSearchIntent() {
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            InputStream in = getApplicationContext().getResources().openRawResource(R.raw.iran_cities_fa);
            Reader reader = new BufferedReader(new InputStreamReader(in));
            City[] cities = new Gson().fromJson(reader, City[].class);
            this.cities.clear();
            for (City city : cities) {
                if (city.name.toLowerCase().contains(query)) {
                    this.cities.add(city);
                }
            }
            currentWeatherSearchRecyclerView.setVisibility(View.VISIBLE);
            currentWeatherLayout.setVisibility(View.GONE);
            currentWeatherLoadingLinearLayout.setVisibility(View.GONE);
            if (currentWeatherSearchRecyclerView.getAdapter() != null)
                currentWeatherSearchRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        getSearchIntent();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (preferences.getBoolean(Constants.FIRST_LAUNCH_PREFERENCES, true)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(Constants.FIRST_LAUNCH_PREFERENCES, false).apply();
        }
    }

    @Override
    public void onBackPressed() {
        if (preferences.getBoolean(Constants.FIRST_LAUNCH_PREFERENCES, true)) {
            Toast.makeText(this, "please choose a city before quiting", Toast.LENGTH_SHORT).show();
        } else {
            finish();
        }
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
}
