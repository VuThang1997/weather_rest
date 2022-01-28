package com.example.weather_rest_clone.model.pojo.info;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoordInfo {
    private double lon;
    private double lat;

    public CoordInfo(double lon, double lat) {
        this.lon = lon;
        this.lat = lat;
    }
}
