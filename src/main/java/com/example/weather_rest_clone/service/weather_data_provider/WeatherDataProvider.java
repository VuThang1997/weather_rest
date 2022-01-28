package com.example.weather_rest_clone.service.weather_data_provider;


import com.example.weather_rest_clone.model.pojo.WeatherData;

public interface WeatherDataProvider {

    WeatherData getTodayWeatherData(String standardizedCityName);
}
