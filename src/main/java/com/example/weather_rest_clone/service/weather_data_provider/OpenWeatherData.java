package com.example.weather_rest_clone.service.weather_data_provider;

import com.example.weather_rest_clone.model.pojo.info.CoordInfo;
import com.example.weather_rest_clone.model.pojo.info.WeatherDescInfo;
import com.example.weather_rest_clone.model.pojo.info.WeatherMainInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Setter
@Getter
@ToString
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenWeatherData {

    public static final long NOT_FOUND_DATA_ID = -1L;

    // all field's names = name in response from provider; must not change

    private long id;

    private String name;

    private int timezone;

    private int cod;

    private CoordInfo coord;

    private Set<WeatherDescInfo> weather;

    private WeatherMainInfo main;

}
