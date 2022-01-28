package com.example.weather_rest_clone.service.weather_data_provider;

import com.example.weather_rest_clone.exception.CustomInternalServerException;
import com.example.weather_rest_clone.model.pojo.WeatherData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;

@Service
public class OpenWeatherProvider implements WeatherDataProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenWeatherProvider.class);

    @Value("${url.get-current-weather}")
    private String getCurrentWeatherUrl;

    @Value("${url.get-current-weather.query-city-name}")
    private String cityNameParamForGetCurrentWeatherUrl;

    @Value("${url.get-current-weather.api-key}")
    private String apiKeyParamForGetCurrentWeatherUrl;

    @Value("${openweather.api-key}")
    private String apiKey;


    private final ObjectMapper objectMapper;

    @Autowired
    public OpenWeatherProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public WeatherData getTodayWeatherData(String standardizedCityName) {
        HttpGet request = buildGetCurrentWeatherRequest(standardizedCityName);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {

            int statusCode = response.getStatusLine().getStatusCode();
            switch (statusCode) {
                case HttpStatus.SC_OK:
                    var jsonContent = EntityUtils.toString(response.getEntity());
                    var openWeatherData = objectMapper.readValue(jsonContent, OpenWeatherData.class);
                    return buildNewWeatherData(openWeatherData);

                case HttpStatus.SC_NOT_FOUND:
                    return WeatherData.buildNotFoundRecord();

                default:
                    LOGGER.error("Error happened while getting weather data from OpenWeather: cityName = {}, response = {}", standardizedCityName, response);
                    throw new CustomInternalServerException();
            }

        } catch (IOException e) {
            LOGGER.error("Error happened while getting weather data from OpenWeather: cityName = {}", standardizedCityName, e);
            throw new CustomInternalServerException();
        }
    }

    private HttpGet buildGetCurrentWeatherRequest(String standardizedCityName) {
        try {
            URI uri = new URIBuilder(getCurrentWeatherUrl)
                    .setParameter(apiKeyParamForGetCurrentWeatherUrl, apiKey)
                    .setParameter(cityNameParamForGetCurrentWeatherUrl, standardizedCityName)
                    .build();

            return new HttpGet(uri);
        } catch (Exception e) {
            LOGGER.error("Error happened while building URI GetCurrentWeather for OpenWeather: ", e);
            throw new CustomInternalServerException();
        }
    }

    private WeatherData buildNewWeatherData(OpenWeatherData openWeatherData) {
        var weatherData = new WeatherData();

        weatherData.setCityId(openWeatherData.getId());
        weatherData.setCityName(openWeatherData.getName());
        weatherData.setTimezone(openWeatherData.getTimezone());
        weatherData.setCoordinate(openWeatherData.getCoord());
        weatherData.setWeatherMainInfo(openWeatherData.getMain());
        weatherData.setWeatherDescInfos(openWeatherData.getWeather());

        return weatherData;
    }
}
