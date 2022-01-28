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
public class WeatherDescInfo {
    private int id;
    private String main;
    private String description;
    private String icon;
}
