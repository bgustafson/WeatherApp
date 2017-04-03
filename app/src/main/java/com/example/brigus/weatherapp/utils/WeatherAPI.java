package com.example.brigus.weatherapp.utils;

import com.example.brigus.weatherapp.models.Location;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

import java.util.Map;


public interface WeatherAPI {


    @GET("data/2.5/weather")
    Call<Location> getWeatherInfo(@QueryMap Map<String, String> inputs);
}
