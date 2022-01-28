package com.example.weather_rest_clone.model.pojo.info;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherMainInfo {
    private double temp;
    private double feels_like;
    private double temp_min;
    private double temp_max;
    private double pressure;
    private double humidity;
    private double sea_level;
    private double grnd_level;
}
