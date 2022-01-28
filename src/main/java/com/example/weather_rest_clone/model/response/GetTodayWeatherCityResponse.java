package com.example.weather_rest_clone.model.response;

import com.example.weather_rest_clone.model.pojo.WeatherData;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class GetTodayWeatherCityResponse {

    private final WeatherData weatherData;

    public GetTodayWeatherCityResponse(WeatherData weatherData) {
        this.weatherData = weatherData;
    }
}
