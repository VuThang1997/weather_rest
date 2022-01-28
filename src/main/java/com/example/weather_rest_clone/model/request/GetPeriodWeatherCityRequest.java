package com.example.weather_rest_clone.model.request;

import com.example.weather_rest_clone.model.pojo.paging.PaginationSetting;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class GetPeriodWeatherCityRequest {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private PaginationSetting paginationSetting;


    public GetPeriodWeatherCityRequest(LocalDate startDate, LocalDate endDate, PaginationSetting paginationSetting) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.paginationSetting = paginationSetting;
    }
}
