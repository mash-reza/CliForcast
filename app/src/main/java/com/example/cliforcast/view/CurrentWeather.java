package com.example.cliforcast.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cliforcast.R;
import com.example.cliforcast.Util.Constants;
import com.example.cliforcast.Util.Utility;
import com.example.cliforcast.database.RoomHelper;
import com.example.cliforcast.network.Weather;
import com.example.cliforcast.network.RetrofitClientInstance;
import com.example.cliforcast.network.WeatherList;
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

    private boolean getLocation = false;
    private Boolean isCityResponseSuccessful = false;
    private Boolean isFiveDayResponseSuccessful = false;
    private Weather currentWeather;
    private WeatherList fiveDayWeather;

    String place = "Sari";

    //GoogleApiClient client;
    FusedLocationProviderClient fusedLocationProviderClient;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_weather);

        preferences = getSharedPreferences(Constants.LOCATION_PREFERENCES, MODE_PRIVATE);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        checkPermission();

        initUi();
        getCityForcast();
    }

    private void initUi() {
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

    private void getCityForcast() {
        RetrofitClientInstance.getINSTANCE().getWeather(place).enqueue(new Callback<Weather>() {
            @Override
            public void onResponse(Call<Weather> call, Response<Weather> response) {
                currentWeather = response.body();
                AsyncTask.execute(() -> {
                    com.example.cliforcast.database.Weather city = null;
//                            RoomHelper.getInstance(getApplicationContext()).getDao().getCity(
//                                    response.body().getCoord().getLat(),
//                                    response.body().getCoord().getLon());
                    if (city != null) {
                        if (new Date().getTime() - (city.getTime()) < 1000) {
                            runOnUiThread(() -> {
                                currentWeatherTemperatureTextView.setText(Utility.kelvinToCelsius(city.getTemp()) + "°");
                                currentWeatherCityNameTextView.setText(city.getName());
                                currentWeatherDescriptionTextView.setText(city.getMain());
                            });
                        } else {
                            runOnUiThread(() -> {
                                currentWeatherTemperatureTextView.setText(Utility.kelvinToCelsius(response.body().getMain().getTemp()) + "°");
                                currentWeatherCityNameTextView.setText(response.body().getName());
                                currentWeatherDescriptionTextView.setText(response.body().getWeather()[0].getMain());
                                currentWeatherStatusImageView.setImageResource(Utility.idToConditionMapper(
                                        response.body().getWeather()[0].getId()
                                ));
                                currentWeatherDateTextView.setText(Utility.epochToDate(response.body().getDate()));
                                currentWeatherDescriptionTextView.setText(Utility.idToStringMapper(
                                        getApplicationContext(), response.body().getWeather()[0].getId()
                                ));
                                currentWeatherMaxTempTextView.setText(Utility.kelvinToCelsius(response.body().getMain().getTemp_max()) + "°");
                                currentWeatherMinTempTextView.setText(Utility.kelvinToCelsius(response.body().getMain().getTemp_min()) + "°");
                                currentWeatherWindTextView.setText(Utility.mphToKmh(response.body().getWind().getSpeed()) + " Kmh");
                                currentWeatherCloudsTextView.setText(response.body().getClouds().getClouds() + "%");
                                currentWeatherHumidityTextView.setText(response.body().getMain().getHumidity() + "%");
                                currentWeatherNowIconImageView.setImageResource(
                                        Utility.idToConditionMapper(
                                                response.body().getWeather()[0].getId()
                                        )
                                );
                                currentWeatherNowTempTextView.setText(Utility.kelvinToCelsius(response.body().getMain().getTemp()) + "°");
                            });
                            {
//                                RoomHelper.getInstance(getApplicationContext()).getDao().insertCity(new com.example.cliforcast.database.Weather(
//                                        response.body().getCoord().getLon(),
//                                        response.body().getCoord().getLat(),
//                                        response.body().getWeather()[0].getMain(),
//                                        response.body().getWeather()[0].getDescription(),
//                                        response.body().getMain().getTemp(),
//                                        response.body().getMain().getPressure(),
//                                        response.body().getMain().getHumidity(),
//                                        response.body().getMain().getTemp_min(),
//                                        response.body().getMain().getTemp_max(),
//                                        response.body().getWind().getSpeed(),
//                                        response.body().getClouds().getClouds(),
//                                        new Date().getTime(),
//                                        response.body().getId(),
//                                        response.body().getName()
//                                ));
                            }
                        }
                    } else {
                        runOnUiThread(() -> {
                            currentWeatherTemperatureTextView.setText(Utility.kelvinToCelsius(response.body().getMain().getTemp()) + "°");
                            currentWeatherCityNameTextView.setText(response.body().getName());
                            currentWeatherDescriptionTextView.setText(response.body().getWeather()[0].getMain());
                            currentWeatherStatusImageView.setImageResource(Utility.idToConditionMapper(
                                    response.body().getWeather()[0].getId()
                            ));
                            currentWeatherDateTextView.setText(Utility.epochToDate(response.body().getDate()));
                            currentWeatherDescriptionTextView.setText(Utility.idToStringMapper(
                                    getApplicationContext(), response.body().getWeather()[0].getId()
                            ));
                            currentWeatherMaxTempTextView.setText(Utility.kelvinToCelsius(response.body().getMain().getTemp_max()) + "°");
                            currentWeatherMinTempTextView.setText(Utility.kelvinToCelsius(response.body().getMain().getTemp_min()) + "°");
                            currentWeatherWindTextView.setText(Utility.mphToKmh(response.body().getWind().getSpeed()) + " Kmh");
                            currentWeatherCloudsTextView.setText(response.body().getClouds().getClouds() + "%");
                            currentWeatherHumidityTextView.setText(response.body().getMain().getHumidity() + "%");
                            currentWeatherNowIconImageView.setImageResource(
                                    Utility.idToConditionMapper(
                                            response.body().getWeather()[0].getId()
                                    )
                            );
                            currentWeatherNowTempTextView.setText(Utility.kelvinToCelsius(response.body().getMain().getTemp()) + "°");
                        });
                        {
//                            RoomHelper.getInstance(getApplicationContext()).getDao().insertCity(new com.example.cliforcast.database.Weather(
//                                    response.body().getCoord().getLon(),
//                                    response.body().getCoord().getLat(),
//                                    response.body().getWeather()[0].getMain(),
//                                    response.body().getWeather()[0].getDescription(),
//                                    response.body().getMain().getTemp(),
//                                    response.body().getMain().getPressure(),
//                                    response.body().getMain().getHumidity(),
//                                    response.body().getMain().getTemp_min(),
//                                    response.body().getMain().getTemp_max(),
//                                    response.body().getWind().getSpeed(),
//                                    response.body().getClouds().getClouds(),
//                                    new Date().getTime(),
//                                    response.body().getId(),
//                                    response.body().getName()
//                            ));
                        }
                    }
                });
                isCityResponseSuccessful = true;
            }

            @Override
            public void onFailure(Call<Weather> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
                //System.err.println(t);
            }
        });
        RetrofitClientInstance.getINSTANCE().getFiveDayWeather(place).enqueue(new Callback<WeatherList>() {
            @Override
            public void onResponse(Call<WeatherList> call, Response<WeatherList> response) {
                fiveDayWeather = response.body();
                isFiveDayResponseSuccessful = true;
                currentWeatherFirstDayMaxTempTextView.setText(Utility.kelvinToCelsius(response.body().getWeather()[8].getMain().getTemp_max()) + "°");
                currentWeatherFirstDayMinTempTextView.setText(Utility.kelvinToCelsius(response.body().getWeather()[8].getMain().getTemp_min()) + "°");
                currentWeatherFirstDayIconImageView.setImageResource(Utility.idToConditionMapper(response.body().getWeather()[8].getWeather()[0].getId()));
                currentWeatherSecondDayMaxTempTextView.setText(Utility.kelvinToCelsius(response.body().getWeather()[16].getMain().getTemp_max()) + "°");
                currentWeatherSecondDayMinTempTextView.setText(Utility.kelvinToCelsius(response.body().getWeather()[16].getMain().getTemp_min()) + "°");
                currentWeatherSecondDayIconImageView.setImageResource(Utility.idToConditionMapper(response.body().getWeather()[16].getWeather()[0].getId()));
                currentWeatherThirdDayMaxTempTextView.setText(Utility.kelvinToCelsius(response.body().getWeather()[24].getMain().getTemp_max()) + "°");
                currentWeatherThirdDayMinTempTextView.setText(Utility.kelvinToCelsius(response.body().getWeather()[24].getMain().getTemp_min()) + "°");
                currentWeatherThirdDayIconImageView.setImageResource(Utility.idToConditionMapper(response.body().getWeather()[24].getWeather()[0].getId()));
                currentWeatherForthDayMaxTempTextView.setText(Utility.kelvinToCelsius(response.body().getWeather()[32].getMain().getTemp_max()) + "°");
                currentWeatherForthDayMinTempTextView.setText(Utility.kelvinToCelsius(response.body().getWeather()[32].getMain().getTemp_min()) + "°");
                currentWeatherForthDayIconImageView.setImageResource(Utility.idToConditionMapper(response.body().getWeather()[32].getWeather()[0].getId()));
            }

            @Override
            public void onFailure(Call<WeatherList> call, Throwable t) {
                Log.i(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
    }

    private void getCityListForecast() {
        RetrofitClientInstance.getINSTANCE().getWeatherList(112931, 125188, 128747).enqueue(new Callback<WeatherList>() {
            @Override
            public void onResponse(Call<WeatherList> call, Response<WeatherList> response) {

            }

            @Override
            public void onFailure(Call<WeatherList> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
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
        if (getLocation) {
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
        fiveDayEventControl();
    }

    private void fiveDayEventControl() {
        currentWeatherNowIconImageView.setOnClickListener(v -> {
            if (isFiveDayResponseSuccessful && isCityResponseSuccessful) {
                currentWeatherTemperatureTextView.setText(Utility.kelvinToCelsius(currentWeather.getMain().getTemp()) + "°");
                currentWeatherCityNameTextView.setText(currentWeather.getName());
                currentWeatherDescriptionTextView.setText(currentWeather.getWeather()[0].getMain());
                currentWeatherStatusImageView.setImageResource(Utility.idToConditionMapper(
                        currentWeather.getWeather()[0].getId()
                ));
                currentWeatherDateTextView.setText(Utility.epochToDate(currentWeather.getDate()));
                currentWeatherDescriptionTextView.setText(Utility.idToStringMapper(
                        getApplicationContext(), currentWeather.getWeather()[0].getId()
                ));
                currentWeatherMaxTempTextView.setText(Utility.kelvinToCelsius(currentWeather.getMain().getTemp_max()) + "°");
                currentWeatherMinTempTextView.setText(Utility.kelvinToCelsius(currentWeather.getMain().getTemp_min()) + "°");
                currentWeatherWindTextView.setText(Utility.mphToKmh(currentWeather.getWind().getSpeed()) + " Kmh");
                currentWeatherCloudsTextView.setText(currentWeather.getClouds().getClouds() + "%");
                currentWeatherHumidityTextView.setText(currentWeather.getMain().getHumidity() + "%");
                currentWeatherNowIconImageView.setImageResource(
                        Utility.idToConditionMapper(
                                currentWeather.getWeather()[0].getId()
                        )
                );
                currentWeatherNowTempTextView.setText(Utility.kelvinToCelsius(currentWeather.getMain().getTemp()) + "°");
            }
        });
        currentWeatherFirstDayIconImageView.setOnClickListener(v -> {
            if (isFiveDayResponseSuccessful && isCityResponseSuccessful) {
                Toast.makeText(this, R.string.day1, Toast.LENGTH_SHORT).show();
                currentWeatherTemperatureTextView.setText(Utility.kelvinToCelsius(fiveDayWeather.getWeather()[8].getMain().getTemp()) + "°");
                currentWeatherCityNameTextView.setText(currentWeather.getName());
                currentWeatherDescriptionTextView.setText(fiveDayWeather.getWeather()[8].getWeather()[0].getMain());
                currentWeatherStatusImageView.setImageResource(Utility.idToConditionMapper(
                        fiveDayWeather.getWeather()[8].getWeather()[0].getId()
                ));
                currentWeatherDateTextView.setText(Utility.epochToDate(fiveDayWeather.getWeather()[8].getDate()));
                currentWeatherDescriptionTextView.setText(Utility.idToStringMapper(
                        getApplicationContext(), fiveDayWeather.getWeather()[8].getWeather()[0].getId()
                ));
                currentWeatherMaxTempTextView.setText(Utility.kelvinToCelsius(fiveDayWeather.getWeather()[8].getMain().getTemp_max()) + "°");
                currentWeatherMinTempTextView.setText(Utility.kelvinToCelsius(fiveDayWeather.getWeather()[8].getMain().getTemp_min()) + "°");
                currentWeatherWindTextView.setText(Utility.mphToKmh(fiveDayWeather.getWeather()[8].getWind().getSpeed()) + " Kmh");
                currentWeatherCloudsTextView.setText(fiveDayWeather.getWeather()[8].getClouds().getClouds() + "%");
                currentWeatherHumidityTextView.setText(fiveDayWeather.getWeather()[8].getMain().getHumidity() + "%");
            }
        });
        currentWeatherSecondDayIconImageView.setOnClickListener(v -> {
            if (isFiveDayResponseSuccessful && isCityResponseSuccessful) {
                Toast.makeText(this, R.string.day2, Toast.LENGTH_SHORT).show();
                currentWeatherTemperatureTextView.setText(Utility.kelvinToCelsius(fiveDayWeather.getWeather()[16].getMain().getTemp()) + "°");
                currentWeatherCityNameTextView.setText(currentWeather.getName());
                currentWeatherDescriptionTextView.setText(fiveDayWeather.getWeather()[16].getWeather()[0].getMain());
                currentWeatherStatusImageView.setImageResource(Utility.idToConditionMapper(
                        fiveDayWeather.getWeather()[16].getWeather()[0].getId()
                ));
                currentWeatherDateTextView.setText(Utility.epochToDate(fiveDayWeather.getWeather()[16].getDate()));
                currentWeatherDescriptionTextView.setText(Utility.idToStringMapper(
                        getApplicationContext(), fiveDayWeather.getWeather()[16].getWeather()[0].getId()
                ));
                currentWeatherMaxTempTextView.setText(Utility.kelvinToCelsius(fiveDayWeather.getWeather()[16].getMain().getTemp_max()) + "°");
                currentWeatherMinTempTextView.setText(Utility.kelvinToCelsius(fiveDayWeather.getWeather()[16].getMain().getTemp_min()) + "°");
                currentWeatherWindTextView.setText(Utility.mphToKmh(fiveDayWeather.getWeather()[16].getWind().getSpeed()) + " Kmh");
                currentWeatherCloudsTextView.setText(fiveDayWeather.getWeather()[16].getClouds().getClouds() + "%");
                currentWeatherHumidityTextView.setText(fiveDayWeather.getWeather()[16].getMain().getHumidity() + "%");
            }
        });
        currentWeatherThirdDayIconImageView.setOnClickListener(v -> {
            if (isFiveDayResponseSuccessful && isCityResponseSuccessful) {
                Toast.makeText(this, R.string.day3, Toast.LENGTH_SHORT).show();
                currentWeatherTemperatureTextView.setText(Utility.kelvinToCelsius(fiveDayWeather.getWeather()[24].getMain().getTemp()) + "°");
                currentWeatherCityNameTextView.setText(currentWeather.getName());
                currentWeatherDescriptionTextView.setText(fiveDayWeather.getWeather()[24].getWeather()[0].getMain());
                currentWeatherStatusImageView.setImageResource(Utility.idToConditionMapper(
                        fiveDayWeather.getWeather()[24].getWeather()[0].getId()
                ));
                currentWeatherDateTextView.setText(Utility.epochToDate(fiveDayWeather.getWeather()[24].getDate()));
                currentWeatherDescriptionTextView.setText(Utility.idToStringMapper(
                        getApplicationContext(), fiveDayWeather.getWeather()[24].getWeather()[0].getId()
                ));
                currentWeatherMaxTempTextView.setText(Utility.kelvinToCelsius(fiveDayWeather.getWeather()[24].getMain().getTemp_max()) + "°");
                currentWeatherMinTempTextView.setText(Utility.kelvinToCelsius(fiveDayWeather.getWeather()[24].getMain().getTemp_min()) + "°");
                currentWeatherWindTextView.setText(Utility.mphToKmh(fiveDayWeather.getWeather()[24].getWind().getSpeed()) + " Kmh");
                currentWeatherCloudsTextView.setText(fiveDayWeather.getWeather()[24].getClouds().getClouds() + "%");
                currentWeatherHumidityTextView.setText(fiveDayWeather.getWeather()[24].getMain().getHumidity() + "%");
            }
        });
        currentWeatherForthDayIconImageView.setOnClickListener(v -> {
            if (isFiveDayResponseSuccessful && isCityResponseSuccessful) {
                Toast.makeText(this, R.string.day4, Toast.LENGTH_SHORT).show();
                currentWeatherTemperatureTextView.setText(Utility.kelvinToCelsius(fiveDayWeather.getWeather()[32].getMain().getTemp()) + "°");
                currentWeatherCityNameTextView.setText(currentWeather.getName());
                currentWeatherDescriptionTextView.setText(fiveDayWeather.getWeather()[32].getWeather()[0].getMain());
                currentWeatherStatusImageView.setImageResource(Utility.idToConditionMapper(
                        fiveDayWeather.getWeather()[32].getWeather()[0].getId()
                ));
                currentWeatherDateTextView.setText(Utility.epochToDate(fiveDayWeather.getWeather()[32].getDate()));
                currentWeatherDescriptionTextView.setText(Utility.idToStringMapper(
                        getApplicationContext(), fiveDayWeather.getWeather()[32].getWeather()[0].getId()
                ));
                currentWeatherMaxTempTextView.setText(Utility.kelvinToCelsius(fiveDayWeather.getWeather()[32].getMain().getTemp_max()) + "°");
                currentWeatherMinTempTextView.setText(Utility.kelvinToCelsius(fiveDayWeather.getWeather()[32].getMain().getTemp_min()) + "°");
                currentWeatherWindTextView.setText(Utility.mphToKmh(fiveDayWeather.getWeather()[32].getWind().getSpeed()) + " Kmh");
                currentWeatherCloudsTextView.setText(fiveDayWeather.getWeather()[32].getClouds().getClouds() + "%");
                currentWeatherHumidityTextView.setText(fiveDayWeather.getWeather()[32].getMain().getHumidity() + "%");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.currentWeatherSearchMenuItem:
                onSearchRequested();
                return true;
            case R.id.currentWeatherLocationMenuItem:
                return true;
            default:
                return true;
        }
    }
}
