package com.example.weather_rest_clone.config;

import com.example.weather_rest_clone.model.entity.WeatherCity;
import com.example.weather_rest_clone.repository.WeatherCityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class HandleUpdateWeatherCityQueue implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(HandleUpdateWeatherCityQueue.class);

    private final WeatherCityRepository weatherCityRepository;

    private final BlockingQueue<WeatherCity> updateWeatherCityQueue;


    @Autowired
    public HandleUpdateWeatherCityQueue(WeatherCityRepository weatherCityRepository, BlockingQueue<WeatherCity> updateWeatherCityQueue) {
        this.weatherCityRepository = weatherCityRepository;
        this.updateWeatherCityQueue = updateWeatherCityQueue;
    }

    @Override
    public void run(String... args) {
        Runnable saveWeatherCityTask = () -> {
          while (true) {
              WeatherCity weatherCity = updateWeatherCityQueue.poll();
              if (weatherCity != null) {
                  String standardizedCityName = weatherCity.getCityName();
                  LocalDate retrieveDate = weatherCity.getRetrieveDate();

                  if (!weatherCityRepository.checkWeatherCityExist(standardizedCityName, retrieveDate)) {
                      int weatherCityId = weatherCityRepository.saveNewWeatherCity(weatherCity);
                      LOGGER.info("Save new WeatherCity: id = {}, cityName = {}, retrieveDate = {}", weatherCityId, standardizedCityName, retrieveDate);
                  }
              }
          }
        };

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(saveWeatherCityTask);
    }
}
