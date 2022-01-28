package com.example.weather_rest_clone.repository;

import com.example.weather_rest_clone.model.entity.WeatherCity;
import com.example.weather_rest_clone.model.pojo.paging.PaginationResult;
import com.example.weather_rest_clone.model.pojo.paging.PaginationSetting;
import org.springframework.lang.NonNull;

import java.time.LocalDate;


public interface WeatherCityRepository {

    boolean checkWeatherCityExist(@NonNull String standardizedCityName, @NonNull LocalDate retrieveDate);

    WeatherCity findByCityNameAndRetrieveDate(@NonNull String standardizedCityName, @NonNull LocalDate retrieveDate);

    PaginationResult<WeatherCity> findByPeriod(@NonNull LocalDate startDate, @NonNull LocalDate endDate, @NonNull PaginationSetting setting);

    int saveNewWeatherCity(@NonNull WeatherCity weatherCity);

    void deleteWeatherCity(@NonNull String standardizedCityName, @NonNull LocalDate retrieveDate);

    void updateExistingWeatherCity(@NonNull WeatherCity weatherCity, @NonNull String weatherDataJson);

}