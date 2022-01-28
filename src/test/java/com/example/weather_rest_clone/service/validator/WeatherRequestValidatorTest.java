package com.example.weather_rest_clone.service.validator;

import com.example.weather_rest_clone.exception.CustomBadRequestException;
import com.example.weather_rest_clone.exception.CustomNotFoundException;
import com.example.weather_rest_clone.model.entity.WeatherCity;
import com.example.weather_rest_clone.model.pojo.WeatherData;
import com.example.weather_rest_clone.model.pojo.paging.PaginationSetting;
import com.example.weather_rest_clone.model.request.*;
import com.example.weather_rest_clone.repository.WeatherCityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;


@SpringBootTest
class WeatherRequestValidatorTest {

    private static final String DUMMY_STANDARDIZED_CITY_NAME = "LONDON";
    private static final LocalDate DUMMY_RETRIEVE_DATE = LocalDate.of(2021, 5, 1);
    private static final String DUMMY_WEATHER_DATA_JSON = "{\"cityId\":2761369,\"cityName\":\"Vienna\",\"timezone\":7200,\"coordinate" +
            "\":{\"lon\":16.3721,\"lat\":48.2085},\"weatherDescInfos\":[{\"id\":801,\"main\":\"Clouds\",\"description\":\"few clouds\"" +
            ",\"icon\":\"02d\"}],\"weatherMainInfo\":{\"temp\":303.31,\"feels_like\":305.5,\"temp_min\":301.1,\"temp_max\":305.7,\"pressure\":1003.0,\"humidity\":56.0,\"sea_level\":0.0,\"grnd_level\":0.0}}";
    private static final WeatherData DUMMY_WEATHER_DATA = new WeatherData();

    private static final LocalDate DUMMY_START_DATE = LocalDate.of(2021, 1, 1);
    private static final LocalDate DUMMY_END_DATE = LocalDate.of(2021, 1, 1);
    private static final int DUMMY_PAGE_SIZE = 1;
    private static final int DUMMY_PAGE_INDEX = 1;


    @MockBean
    private WeatherCityRepository weatherCityRepository;

    @Autowired
    private WeatherRequestValidator weatherRequestValidator;



    @ParameterizedTest
    @MethodSource("buildInvalidGetTodayWeatherCityRequest")
    void validateGetTodayWeatherCityRequest_whenRequestIsInvalid_thenThrowCustomBadRequestException(GetTodayWeatherCityRequest request) {
        assertThrows(CustomBadRequestException.class, () -> weatherRequestValidator.validateGetTodayWeatherCityRequest(request));
    }

    private static Stream<GetTodayWeatherCityRequest> buildInvalidGetTodayWeatherCityRequest() {
        var cityNameIsNullRequest = new GetTodayWeatherCityRequest(null);
        return Stream.of(cityNameIsNullRequest);
    }

    @Test
    void validateGetTodayWeatherCityRequest_whenRequestIsValid_thenPass() {
        var validRequest = new GetTodayWeatherCityRequest(DUMMY_STANDARDIZED_CITY_NAME);
        assertDoesNotThrow(() -> weatherRequestValidator.validateGetTodayWeatherCityRequest(validRequest));
    }




    @ParameterizedTest
    @MethodSource("buildInvalidGetPeriodWeatherCityRequest")
    void validateGetPeriodWeatherCityRequest_whenRequestIsInvalid_thenThrowCustomBadRequestException(GetPeriodWeatherCityRequest request) {
        assertThrows(CustomBadRequestException.class, () -> weatherRequestValidator.validateGetPeriodWeatherCityRequest(request));
    }

    private static Stream<GetPeriodWeatherCityRequest> buildInvalidGetPeriodWeatherCityRequest() {
        var missingStartDateRequest = new GetPeriodWeatherCityRequest(null, DUMMY_END_DATE, null);
        var startDateIsTomorrowRequest = new GetPeriodWeatherCityRequest(getTomorrow(), DUMMY_END_DATE, null);
        var missingEndDateRequest = new GetPeriodWeatherCityRequest(DUMMY_START_DATE, null, null);
        var endDateIsBeforeStartDateRequest = new GetPeriodWeatherCityRequest(DUMMY_START_DATE, DUMMY_START_DATE.minusDays(1), null);

        LocalDate endDateForExceedingLimit = DUMMY_START_DATE.plusDays(WeatherRequestValidator.MAX_DAYS_OF_PERIOD);
        var periodExceedLimitRequest = new GetPeriodWeatherCityRequest(DUMMY_START_DATE, endDateForExceedingLimit, null);

        var missingPaginationSettingRequest = new GetPeriodWeatherCityRequest(DUMMY_START_DATE, DUMMY_END_DATE, null);
        var pageIndexIsNullRequest = new GetPeriodWeatherCityRequest(DUMMY_START_DATE, DUMMY_END_DATE, new PaginationSetting(null, DUMMY_PAGE_SIZE));
        var negativePageIndexRequest = new GetPeriodWeatherCityRequest(DUMMY_START_DATE, DUMMY_END_DATE, new PaginationSetting(-1, DUMMY_PAGE_SIZE));
        var pageSizeIsNullRequest = new GetPeriodWeatherCityRequest(DUMMY_START_DATE, DUMMY_END_DATE, new PaginationSetting(DUMMY_PAGE_INDEX, null));
        var negativePageSizeRequest = new GetPeriodWeatherCityRequest(DUMMY_START_DATE, DUMMY_END_DATE, new PaginationSetting(DUMMY_PAGE_INDEX, -1));
        var pageSizeExceedLimitRequest = new GetPeriodWeatherCityRequest(DUMMY_START_DATE, DUMMY_END_DATE,
                new PaginationSetting(DUMMY_PAGE_INDEX, WeatherRequestValidator.MAX_PAGE_SIZE + 1));

        return Stream.of(
                missingStartDateRequest, startDateIsTomorrowRequest,
                missingEndDateRequest, endDateIsBeforeStartDateRequest,
                periodExceedLimitRequest,
                missingPaginationSettingRequest,
                pageIndexIsNullRequest, negativePageIndexRequest,
                pageSizeIsNullRequest, negativePageSizeRequest, pageSizeExceedLimitRequest
        );
    }


    @Test
    void validateGetPeriodWeatherCityRequest_whenRequestIsValid_thenPass() {
        var validRequest = new GetPeriodWeatherCityRequest(DUMMY_START_DATE, DUMMY_END_DATE, new PaginationSetting(DUMMY_PAGE_INDEX, DUMMY_PAGE_SIZE));
        assertDoesNotThrow(() -> weatherRequestValidator.validateGetPeriodWeatherCityRequest(validRequest));
    }



    @ParameterizedTest
    @MethodSource(value = "buildInvalidSaveNewWeatherCityRequest")
    void validateSaveNewWeatherDataRequest_whenCityNameIsInvalid_thenThrowCustomBadRequestException(SaveNewWeatherCityRequest request) {
        assertThrows(CustomBadRequestException.class, () -> weatherRequestValidator.validateSaveNewWeatherDataRequest(request));
    }

    private static Stream<SaveNewWeatherCityRequest> buildInvalidSaveNewWeatherCityRequest() {
        WeatherData dummyWeatherData = new WeatherData();

        var missingWeatherDataRequest = new SaveNewWeatherCityRequest(DUMMY_STANDARDIZED_CITY_NAME, DUMMY_RETRIEVE_DATE, null);
        var cityNameIsNullRequest = new SaveNewWeatherCityRequest(null, DUMMY_RETRIEVE_DATE, dummyWeatherData);

        var retrieveDateIsNullRequest = new SaveNewWeatherCityRequest(DUMMY_STANDARDIZED_CITY_NAME, null, dummyWeatherData);
        var retrieveDateIsTomorrowRequest = new SaveNewWeatherCityRequest(DUMMY_STANDARDIZED_CITY_NAME, getTomorrow(), dummyWeatherData);

        return Stream.of(
                missingWeatherDataRequest, cityNameIsNullRequest,
                retrieveDateIsNullRequest, retrieveDateIsTomorrowRequest
        );
    }


    @Test
    void validateSaveNewWeatherDataRequest_whenRequestIsValid_andDbHasRecord_thenThrowCustomBadRequestException() {
        doReturn(true).when(weatherCityRepository).checkWeatherCityExist(DUMMY_STANDARDIZED_CITY_NAME, DUMMY_RETRIEVE_DATE);

        SaveNewWeatherCityRequest request = buildValidSaveNewWeatherCityRequest();
        assertThrows(CustomBadRequestException.class, () -> weatherRequestValidator.validateSaveNewWeatherDataRequest(request));
    }


    @Test
    void validateSaveNewWeatherDataRequest_whenRequestValid_andDbDoesNotHaveRecord_thenPass() {
        doReturn(false).when(weatherCityRepository).checkWeatherCityExist(DUMMY_STANDARDIZED_CITY_NAME, DUMMY_RETRIEVE_DATE);

        SaveNewWeatherCityRequest request = buildValidSaveNewWeatherCityRequest();
        assertDoesNotThrow(() ->weatherRequestValidator.validateSaveNewWeatherDataRequest(request));
    }

    private SaveNewWeatherCityRequest buildValidSaveNewWeatherCityRequest() {
        return new SaveNewWeatherCityRequest(DUMMY_STANDARDIZED_CITY_NAME, DUMMY_RETRIEVE_DATE, DUMMY_WEATHER_DATA);
    }


    @ParameterizedTest
    @MethodSource(value = "buildInvalidDeleteWeatherCityRequest")
    void validateDeleteWeatherDataRequest_whenRequestIsInvalid_thenThrowCustomBadRequestException(DeleteWeatherCityRequest request) {
        assertThrows(CustomBadRequestException.class, () -> weatherRequestValidator.validateDeleteNewWeatherDataRequest(request));
    }

    private static Stream<DeleteWeatherCityRequest> buildInvalidDeleteWeatherCityRequest() {
        var citNameIsNullRequest = new DeleteWeatherCityRequest(null, DUMMY_RETRIEVE_DATE);

        var retrieveDataIsNullRequest = new DeleteWeatherCityRequest(DUMMY_STANDARDIZED_CITY_NAME, null);
        var retrieveDataIsTomorrowRequest = new DeleteWeatherCityRequest(DUMMY_STANDARDIZED_CITY_NAME, getTomorrow());

        return Stream.of(
                citNameIsNullRequest,
                retrieveDataIsNullRequest, retrieveDataIsTomorrowRequest
        );
    }



    @ParameterizedTest
    @MethodSource(value = "buildInvalidUpdateWeatherCityRequest")
    void validateUpdateWeatherCityRequest_whenRequestIsInvalid_thenThrowCustomBadRequestException(UpdateWeatherCityRequest request) {
        assertThrows(CustomBadRequestException.class, () -> weatherRequestValidator.validateUpdateWeatherCityRequest(request));
    }

    private static Stream<UpdateWeatherCityRequest> buildInvalidUpdateWeatherCityRequest() {
        var citNameIsNullRequest = new UpdateWeatherCityRequest(null, DUMMY_RETRIEVE_DATE, DUMMY_WEATHER_DATA);

        var retrieveDataIsNullRequest = new UpdateWeatherCityRequest(DUMMY_STANDARDIZED_CITY_NAME, null, DUMMY_WEATHER_DATA);
        var retrieveDataIsTomorrowRequest = new UpdateWeatherCityRequest(DUMMY_STANDARDIZED_CITY_NAME, getTomorrow(), DUMMY_WEATHER_DATA);

        var weatherDataIsNullRequest = new UpdateWeatherCityRequest(DUMMY_STANDARDIZED_CITY_NAME, DUMMY_RETRIEVE_DATE, null);

        return Stream.of(
                citNameIsNullRequest,
                retrieveDataIsNullRequest, retrieveDataIsTomorrowRequest,
                weatherDataIsNullRequest
        );
    }



    @Test
    void validateUpdateWeatherCityRequest_whenRequestIsValid_andDbDoesNotHaveRecord_thenThrowCustomNotFoundException() {
        var request = buildValidUpdateWeatherCityRequest();

        doReturn(null).when(weatherCityRepository).findByCityNameAndRetrieveDate(DUMMY_STANDARDIZED_CITY_NAME, DUMMY_RETRIEVE_DATE);

        assertThrows(CustomNotFoundException.class, () -> weatherRequestValidator.validateUpdateWeatherCityRequest(request));
    }

    @Test
    void validateUpdateWeatherCityRequest_whenRequestIsValid_andDbDoesHasRecord_thenReturnWeatherCity() {
        var request = buildValidUpdateWeatherCityRequest();
        WeatherCity mockWeatherCity = new WeatherCity(1, DUMMY_STANDARDIZED_CITY_NAME, DUMMY_RETRIEVE_DATE, DUMMY_WEATHER_DATA_JSON);

        doReturn(mockWeatherCity).when(weatherCityRepository).findByCityNameAndRetrieveDate(DUMMY_STANDARDIZED_CITY_NAME, DUMMY_RETRIEVE_DATE);

        WeatherCity result = weatherRequestValidator.validateUpdateWeatherCityRequest(request);

        assertNotNull(result);
        assertEquals(mockWeatherCity, result);
    }

    private UpdateWeatherCityRequest buildValidUpdateWeatherCityRequest() {
        return new UpdateWeatherCityRequest(DUMMY_STANDARDIZED_CITY_NAME, DUMMY_RETRIEVE_DATE, DUMMY_WEATHER_DATA);
    }


    private static LocalDate getTomorrow() {
        return LocalDate.now().plusDays(1);
    }

}