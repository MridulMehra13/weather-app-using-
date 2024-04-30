package com.example.weatherforcast;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("weather")
    Call<WeatherData> getWeatherData(
            @Query("q") String cityName,
            @Query("appid") String apiKey,
            @Query("units") String units
    );
}

