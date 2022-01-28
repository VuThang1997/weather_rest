package com.example.weather_rest_clone.service.util;

import com.example.weather_rest_clone.exception.CustomInternalServerException;
import com.example.weather_rest_clone.model.entity.WeatherCity;
import com.example.weather_rest_clone.model.pojo.WeatherData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class WeatherDataConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherDataConverter.class);

    private final ObjectMapper objectMapper;

    @Autowired
    public WeatherDataConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    public WeatherData parseWeatherDataJsonToObject(String weatherDataJson) {
        try {
            return objectMapper.readValue(weatherDataJson, WeatherData.class);
        } catch (IOException e) {
            LOGGER.error("Error happened while parsing WeatherDataJson to object: ", e);
            throw new CustomInternalServerException();
        }
    }

    public String writeWeatherDataToJson(WeatherData weatherData) {
        try {
            return objectMapper.writeValueAsString(weatherData);
        } catch (IOException e) {
            LOGGER.error("Error happened while write WeatherData as json string: ", e);
            throw new CustomInternalServerException();
        }
    }

    public List<WeatherData> getWeatherDataListFromWeatherCity(Collection<WeatherCity> weatherCities) {
        return weatherCities.stream()
                .map(weatherCity -> this.parseWeatherDataJsonToObject(weatherCity.getWeatherDataJson()))
                .collect(Collectors.toList());
    }
}