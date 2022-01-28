package com.example.weather_rest_clone.model.request;

import com.example.weather_rest_clone.model.pojo.WeatherData;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class UpdateWeatherCityRequest {

    private String cityName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate retrieveDate;

    private WeatherData weatherData;

    public UpdateWeatherCityRequest(String cityName, LocalDate retrieveDate, WeatherData weatherData) {
        this.cityName = cityName;
        this.retrieveDate = retrieveDate;
        this.weatherData = weatherData;
    }
}
