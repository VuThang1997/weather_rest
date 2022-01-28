package com.example.weather_rest_clone.service.domain_service;


import com.example.weather_rest_clone.model.request.*;
import com.example.weather_rest_clone.model.response.*;
import org.springframework.lang.NonNull;

public interface WeatherService {

    GetTodayWeatherCityResponse getTodayWeatherCity(@NonNull GetTodayWeatherCityRequest request);

    GetPeriodWeatherCityResponse getPeriodWeatherCity(@NonNull GetPeriodWeatherCityRequest request);

    SaveNewWeatherCityResponse saveNewWeatherCity(@NonNull SaveNewWeatherCityRequest request);

    UpdateWeatherCityResponse updateExistingWeatherCity(@NonNull UpdateWeatherCityRequest request);

    DeleteWeatherCityResponse deleteWeatherCity(@NonNull DeleteWeatherCityRequest request);
}
