package com.example.weather_rest_clone.service.util;

import com.example.weather_rest_clone.model.entity.WeatherCity;
import com.example.weather_rest_clone.model.pojo.WeatherData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class WeatherDataConverterTest {

    private static final LocalDate DUMMY_RETRIEVAL_DATE = LocalDate.of(2021, 5, 30);
    private static final String DUMMY_STANDARDIZED_CITY_NAME = "HANOI";
    private static final String DUMMY_WEATHER_DATA_JSON = "{\"dt\": 1621910237, \"id\": 1581130, \"cod\": 200, " +
            "\"sys\": {\"id\": 9308, \"type\": 1, \"sunset\": 1621942284, \"country\": \"VN\", \"sunrise\": 1621894562}, \"base\": \"stations\", " +
            "\"main\": {\"temp\": 296.15, \"humidity\": 76, \"pressure\": 1011, \"temp_max\": 296.15, \"temp_min\": 296.15, \"sea_level\": 1011, \"feels_like\": 296.49," +
            " \"grnd_level\": 1009}, \"name\": \"Hanoi\", \"wind\": {\"deg\": 20, \"gust\": 3.08, \"speed\": 1.88}, " +
            "\"coord\": {\"lat\": 21.0245, \"lon\": 105.8412}, " +
            "\"clouds\": {\"all\": 100}, \"weather\": [{\"id\": 804, \"icon\": \"04d\", \"main\": \"Clouds\", \"description\": \"overcast clouds\"}], \"timezone\": 25200, \"visibility\": 10000}";


    @Autowired
    private WeatherDataConverter weatherDataConverter;


    @Test
    void getWeatherDataListFromWeatherCity_whenReceiveListOfWeatherCity_thenReturnCorrectList() {
        WeatherCity weatherCity = new WeatherCity(DUMMY_STANDARDIZED_CITY_NAME, DUMMY_RETRIEVAL_DATE, DUMMY_WEATHER_DATA_JSON);
        List<WeatherCity> weatherCities = List.of(weatherCity);

        List<WeatherData> weatherDataList = weatherDataConverter.getWeatherDataListFromWeatherCity(weatherCities);

        assertNotNull(weatherDataList);
        assertEquals(weatherCities.size(), weatherDataList.size());
    }
}