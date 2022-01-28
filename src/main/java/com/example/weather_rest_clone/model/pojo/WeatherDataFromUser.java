package com.example.weather_rest_clone.model.pojo;

import com.example.weather_rest_clone.model.pojo.info.WeatherDescInfo;
import com.example.weather_rest_clone.model.pojo.info.WeatherMainInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Set;

@Setter
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherDataFromUser {
    private String cityName;
    private Integer cityId;
    private Set<WeatherDescInfo> weatherDescInfos;
    private WeatherMainInfo main;
}
