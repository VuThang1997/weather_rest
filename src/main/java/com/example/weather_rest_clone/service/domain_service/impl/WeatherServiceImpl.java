package com.example.weather_rest_clone.service.domain_service.impl;


import com.example.weather_rest_clone.exception.CustomNotFoundException;
import com.example.weather_rest_clone.model.entity.WeatherCity;
import com.example.weather_rest_clone.model.pojo.WeatherData;
import com.example.weather_rest_clone.model.pojo.paging.PaginationResult;
import com.example.weather_rest_clone.model.request.*;
import com.example.weather_rest_clone.model.response.*;
import com.example.weather_rest_clone.repository.WeatherCityRepository;
import com.example.weather_rest_clone.service.domain_service.WeatherService;
import com.example.weather_rest_clone.service.util.WeatherDataConverter;
import com.example.weather_rest_clone.service.validator.WeatherRequestValidator;
import com.example.weather_rest_clone.service.weather_data_provider.WeatherDataProvider;
import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.BlockingQueue;

@Service
public class WeatherServiceImpl implements WeatherService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherServiceImpl.class);


    private final WeatherRequestValidator weatherRequestValidator;

    private final WeatherCityRepository weatherCityRepository;

    private final WeatherDataConverter weatherDataConverter;

    private final WeatherDataProvider weatherDataProvider;

    private final Cache<String, WeatherData> todayWeatherDataCache;

    private final BlockingQueue<WeatherCity> updateWeatherCityQueue;


    @Autowired
    public WeatherServiceImpl(WeatherRequestValidator weatherRequestValidator, WeatherCityRepository weatherCityRepository,
                              WeatherDataConverter weatherDataConverter, WeatherDataProvider weatherDataProvider,
                              BlockingQueue<WeatherCity> updateWeatherCityQueue,
                              Cache<String, WeatherData> todayWeatherDataCache) {
        this.weatherRequestValidator = weatherRequestValidator;
        this.weatherCityRepository = weatherCityRepository;
        this.weatherDataConverter = weatherDataConverter;
        this.weatherDataProvider = weatherDataProvider;

        this.updateWeatherCityQueue = updateWeatherCityQueue;
        this.todayWeatherDataCache = todayWeatherDataCache;
    }

    @Override
    public GetTodayWeatherCityResponse getTodayWeatherCity(@NonNull GetTodayWeatherCityRequest request) {
        transformWeatherRequestBeforeValidation(request);
        weatherRequestValidator.validateGetTodayWeatherCityRequest(request);

        WeatherData weatherData = getTodayWeatherData(request.getCityName());
        return new GetTodayWeatherCityResponse(weatherData);
    }

    private void transformWeatherRequestBeforeValidation(GetTodayWeatherCityRequest request) {
        String standardizedCityName = standardizeCityName(request.getCityName());
        request.setCityName(standardizedCityName);
    }

    private WeatherData getTodayWeatherData(String standardizedCityName) {
        WeatherData cachedData = getTodayWeatherDataFromCache(standardizedCityName);
        if (cachedData != null) {
            return cachedData;
        }

        WeatherData savedWeatherData = getTodayWeatherDataFromDb(standardizedCityName);
        if (savedWeatherData != null) {
            todayWeatherDataCache.put(standardizedCityName, savedWeatherData);
            return savedWeatherData;
        }

        return getTodayWeatherDataFromProvider(standardizedCityName);
    }

    private WeatherData getTodayWeatherDataFromCache(String standardizedCityName) {
        WeatherData cachedData = todayWeatherDataCache.getIfPresent(standardizedCityName);

        if (cachedData == null) {
            return null;
        }

        if (cachedData.isNotFoundRecord()) {
            throw new CustomNotFoundException("City " + standardizedCityName + " is not found");
        }

        return cachedData;
    }

    private WeatherData getTodayWeatherDataFromDb(String standardizedCityName) {
        WeatherCity weatherCity = weatherCityRepository.findByCityNameAndRetrieveDate(standardizedCityName, LocalDate.now());
        if (weatherCity == null) {
            return null;
        }

        return weatherDataConverter.parseWeatherDataJsonToObject(weatherCity.getWeatherDataJson());
    }

    private WeatherData getTodayWeatherDataFromProvider(String standardizedCityName) {
        WeatherData weatherData = weatherDataProvider.getTodayWeatherData(standardizedCityName);
        if (weatherData.isNotFoundRecord()) {
            LOGGER.error("City {} is not provided", standardizedCityName);
            throw new CustomNotFoundException("City " + standardizedCityName + " is not found");
        }

        todayWeatherDataCache.put(standardizedCityName, weatherData);
        addWeatherDataToQueue(standardizedCityName, weatherDataConverter.writeWeatherDataToJson(weatherData));

        return weatherData;
    }

    private void addWeatherDataToQueue(String standardizedCityName, String weatherDataJson) {
        var weatherCity = new WeatherCity(standardizedCityName, LocalDate.now(), weatherDataJson);
        updateWeatherCityQueue.offer(weatherCity);
    }


    @Override
    public GetPeriodWeatherCityResponse getPeriodWeatherCity(@NonNull GetPeriodWeatherCityRequest request) {
        weatherRequestValidator.validateGetPeriodWeatherCityRequest(request);

        PaginationResult<WeatherCity> paginationResult = weatherCityRepository.findByPeriod(request.getStartDate(), request.getEndDate(), request.getPaginationSetting());
        List<WeatherData> weatherDataList = weatherDataConverter.getWeatherDataListFromWeatherCity(paginationResult.getData());

        return new GetPeriodWeatherCityResponse(weatherDataList, paginationResult.getPaginationInfo());
    }


    @Override
    public SaveNewWeatherCityResponse saveNewWeatherCity(@NonNull SaveNewWeatherCityRequest request) {
        transformWeatherRequestBeforeValidation(request);
        weatherRequestValidator.validateSaveNewWeatherDataRequest(request);

        String weatherDataJson = weatherDataConverter.writeWeatherDataToJson(request.getWeatherData());
        var weatherCity = new WeatherCity(request.getCityName(), request.getRetrieveDate(), weatherDataJson);
        int weatherCityId = weatherCityRepository.saveNewWeatherCity(weatherCity);

        return new SaveNewWeatherCityResponse(weatherCityId);
    }

    private void transformWeatherRequestBeforeValidation(SaveNewWeatherCityRequest request) {
        String standardizedCityName = standardizeCityName(request.getCityName());
        request.setCityName(standardizedCityName);
    }


    @Override
    public UpdateWeatherCityResponse updateExistingWeatherCity(@NonNull UpdateWeatherCityRequest request) {
        transformWeatherRequestBeforeValidation(request);
        WeatherCity weatherCity = weatherRequestValidator.validateUpdateWeatherCityRequest(request);

        String weatherDataJson = weatherDataConverter.writeWeatherDataToJson(request.getWeatherData());
        weatherCityRepository.updateExistingWeatherCity(weatherCity, weatherDataJson);

        return new UpdateWeatherCityResponse(request.getCityName(), request.getRetrieveDate(), "Success");
    }

    private void transformWeatherRequestBeforeValidation(UpdateWeatherCityRequest request) {
        String standardizedCityName = standardizeCityName(request.getCityName());
        request.setCityName(standardizedCityName);
    }


    @Override
    public DeleteWeatherCityResponse deleteWeatherCity(@NonNull DeleteWeatherCityRequest request) {
        transformWeatherRequestBeforeValidation(request);
        weatherRequestValidator.validateDeleteNewWeatherDataRequest(request);

        deleteCachedWeatherData(request.getRetrieveDate(), request.getCityName());
        weatherCityRepository.deleteWeatherCity(request.getCityName(), request.getRetrieveDate());

        return new DeleteWeatherCityResponse(request.getCityName(), request.getRetrieveDate(), "Success");
    }

    private void transformWeatherRequestBeforeValidation(DeleteWeatherCityRequest request) {
        String standardizedCityName = standardizeCityName(request.getCityName());
        request.setCityName(standardizedCityName);
    }

    private void deleteCachedWeatherData(LocalDate retrieveDate, String standardizedCityName) {
        if (LocalDate.now().equals(retrieveDate)) {
            todayWeatherDataCache.invalidate(standardizedCityName);
        }
    }


    // this standard cityName is used to query Db
    private String standardizeCityName(@Nullable String cityName) {
        if (cityName == null || cityName.isBlank()) {
            return null;
        }

        return cityName.replaceAll("\\s", "").toUpperCase();
    }
}