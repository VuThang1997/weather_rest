package com.example.weather_rest_clone.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@ToString
public class DeleteWeatherCityRequest {

    @Setter
    private String cityName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate retrieveDate;

    public DeleteWeatherCityRequest(String cityName, LocalDate retrieveDate) {
        this.cityName = cityName;
        this.retrieveDate = retrieveDate;
    }
}
