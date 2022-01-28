package com.example.weather_rest_clone.model.response;

import com.example.weather_rest_clone.model.pojo.WeatherData;
import com.example.weather_rest_clone.model.pojo.paging.PaginationInfo;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class GetPeriodWeatherCityResponse {

    private final List<WeatherData> weatherDataList;
    private final PaginationInfo paginationInfo;

    public GetPeriodWeatherCityResponse(List<WeatherData> weatherDataList, PaginationInfo paginationInfo) {
        this.weatherDataList = weatherDataList;
        this.paginationInfo = paginationInfo;
    }
}
