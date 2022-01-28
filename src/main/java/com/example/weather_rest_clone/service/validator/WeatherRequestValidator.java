package com.example.weather_rest_clone.service.validator;

import com.example.weather_rest_clone.exception.CustomBadRequestException;
import com.example.weather_rest_clone.exception.CustomNotFoundException;
import com.example.weather_rest_clone.model.entity.WeatherCity;
import com.example.weather_rest_clone.model.pojo.WeatherData;
import com.example.weather_rest_clone.model.pojo.paging.PaginationSetting;
import com.example.weather_rest_clone.model.request.*;
import com.example.weather_rest_clone.repository.WeatherCityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class WeatherRequestValidator {

    public static final int MAX_DAYS_OF_PERIOD = 90;

    public static final int MAX_PAGE_SIZE = 100;


    private final WeatherCityRepository weatherCityRepository;


    @Autowired
    public WeatherRequestValidator(WeatherCityRepository weatherCityRepository) {
        this.weatherCityRepository = weatherCityRepository;
    }


    public void validateGetTodayWeatherCityRequest(GetTodayWeatherCityRequest request) {
        validateStandardizedCityName(request.getCityName());
    }

    private void validateStandardizedCityName(@Nullable String standardizedCityName) {
        if (standardizedCityName == null) {
            throw new CustomBadRequestException("Invalid city name");
        }
    }


    public void validateGetPeriodWeatherCityRequest(@NonNull GetPeriodWeatherCityRequest request) {
        validatePeriod(request.getStartDate(), request.getEndDate());
        validatePaginationSettingInRequest(request.getPaginationSetting());
    }

    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        validateStartDateOfPeriod(startDate);
        validateEndDateOfPeriod(endDate, startDate);
        validateNumDayOfPeriod(startDate, endDate);
    }

    private void validateStartDateOfPeriod(LocalDate startDate) {
        if (startDate == null) {
            throw new CustomBadRequestException("Missing start date");
        }

        if (startDate.isAfter(LocalDate.now())) {
            throw new CustomBadRequestException("Invalid start date");
        }
    }

    private void validateEndDateOfPeriod(LocalDate endDate, LocalDate startDate) {
        if (endDate == null) {
            throw new CustomBadRequestException("Missing end date");
        }

        if (endDate.isBefore(startDate)) {
            throw new CustomBadRequestException("Invalid end date");
        }
    }

    private void validateNumDayOfPeriod(LocalDate startDate, LocalDate endDate) {
        long numDay = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (numDay > MAX_DAYS_OF_PERIOD) {
            throw new CustomBadRequestException("Period exceeds system's limit");
        }
    }

    private void validatePaginationSettingInRequest(@Nullable PaginationSetting setting) {
        if (setting == null) {
            throw new CustomBadRequestException("Missing pagination setting");
        }

        Integer pageIndex = setting.getPageIndex();
        if (pageIndex == null || pageIndex <= 0) {
            throw new CustomBadRequestException("Invalid page index in pagination setting");
        }

        Integer pageSize = setting.getPageSize();
        if (pageSize == null || pageSize <= 0 || pageSize > MAX_PAGE_SIZE) {
            throw new CustomBadRequestException("Invalid page size in pagination setting");
        }
    }



    public void validateSaveNewWeatherDataRequest(@NonNull SaveNewWeatherCityRequest request) {
        LocalDate retrieveDate = request.getRetrieveDate();
        validateRetrieveDate(retrieveDate);

        String standardizedCityName = request.getCityName();
        validateStandardizedCityName(request.getCityName());

        checkWeatherDataNotNull(request.getWeatherData());

        checkWeatherDataAlreadyExistedInDb(retrieveDate, standardizedCityName);
    }

    private void checkWeatherDataNotNull(WeatherData weatherData) {
        if (weatherData == null) {
            throw new CustomBadRequestException("Missing weather data");
        }
    }

    private void checkWeatherDataAlreadyExistedInDb(LocalDate retrieveDate, String standardizedCityName) {
        if (weatherCityRepository.checkWeatherCityExist(standardizedCityName, retrieveDate)) {
            throw new CustomBadRequestException("City " + standardizedCityName + " already has weather data");
        }
    }



    public WeatherCity validateUpdateWeatherCityRequest(@NonNull UpdateWeatherCityRequest request) {
        validateRetrieveDate(request.getRetrieveDate());
        validateStandardizedCityName(request.getCityName());
        checkWeatherDataNotNull(request.getWeatherData());

        WeatherCity weatherCity = weatherCityRepository.findByCityNameAndRetrieveDate(request.getCityName(), request.getRetrieveDate());
        if (weatherCity == null) {
            throw new CustomNotFoundException("No WeatherData exist to update");
        }

        return weatherCity;
    }

    private void validateRetrieveDate(LocalDate retrieveDate) {
        if (retrieveDate == null) {
            throw new CustomBadRequestException("Missing retrieve date");
        }

        if (retrieveDate.isAfter(LocalDate.now())) {
            throw new CustomBadRequestException("Invalid retrieve date");
        }
    }



    public void validateDeleteNewWeatherDataRequest(@NonNull DeleteWeatherCityRequest request) {
        validateRetrieveDate(request.getRetrieveDate());
        validateStandardizedCityName(request.getCityName());
    }
}
