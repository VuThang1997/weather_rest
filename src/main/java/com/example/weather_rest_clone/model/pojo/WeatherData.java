package com.example.weather_rest_clone.model.pojo;

import com.example.weather_rest_clone.model.pojo.info.CoordInfo;
import com.example.weather_rest_clone.model.pojo.info.WeatherDescInfo;
import com.example.weather_rest_clone.model.pojo.info.WeatherMainInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class WeatherData {
    private long cityId;

    private String cityName;

    private int timezone;

    private CoordInfo coordinate;

    private Set<WeatherDescInfo> weatherDescInfos;

    private WeatherMainInfo weatherMainInfo;


    private static final long CITY_ID_FOR_NOT_FOUND_RECORD = -1L;

    public static WeatherData buildNotFoundRecord() {
        var weatherData = new WeatherData();
        weatherData.setCityId(CITY_ID_FOR_NOT_FOUND_RECORD);

        return weatherData;
    }

    @JsonIgnore
    public boolean isNotFoundRecord() {
        return CITY_ID_FOR_NOT_FOUND_RECORD == this.cityId;
    }
}
