package com.example.weather_rest_clone.config;

import com.example.weather_rest_clone.model.entity.WeatherCity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Configuration
public class BeanConfig {


    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean(value = "updateWeatherCityQueue")
    public BlockingQueue<WeatherCity> updateWeatherCityQueue() {
        return new ArrayBlockingQueue<>(2000);
    }
}
