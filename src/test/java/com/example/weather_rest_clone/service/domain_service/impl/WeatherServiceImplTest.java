package com.example.weather_rest_clone.service.domain_service.impl;

import com.example.weather_rest_clone.exception.CustomBadRequestException;
import com.example.weather_rest_clone.exception.CustomNotFoundException;
import com.example.weather_rest_clone.model.entity.WeatherCity;
import com.example.weather_rest_clone.model.pojo.WeatherData;
import com.example.weather_rest_clone.model.pojo.paging.PaginationInfo;
import com.example.weather_rest_clone.model.pojo.paging.PaginationResult;
import com.example.weather_rest_clone.model.pojo.paging.PaginationSetting;
import com.example.weather_rest_clone.model.request.*;
import com.example.weather_rest_clone.model.response.*;
import com.example.weather_rest_clone.repository.WeatherCityRepository;
import com.example.weather_rest_clone.service.util.WeatherDataConverter;
import com.example.weather_rest_clone.service.validator.WeatherRequestValidator;
import com.example.weather_rest_clone.service.weather_data_provider.WeatherDataProvider;
import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringBootTest
class WeatherServiceImplTest {

    private static final String DUMMY_CITY_NAME = "haNoi";
    private static final String DUMMY_STANDARDIZED_CITY_NAME = "HANOI";
    private static final LocalDate DUMMY_RETRIEVE_DATE = LocalDate.of(2021, 5, 1);
    private static final int DUMMY_WEATHER_CITY_ID = 1;
    private static final String DUMMY_WEATHER_DATA_JSON = "{\"dt\": 1621910237, \"id\": 1581130, \"cod\": 200, " +
            "\"sys\": {\"id\": 9308, \"type\": 1, \"sunset\": 1621942284, \"country\": \"VN\", \"sunrise\": 1621894562}, \"base\": \"stations\", " +
            "\"main\": {\"temp\": 296.15, \"humidity\": 76, \"pressure\": 1011, \"temp_max\": 296.15, \"temp_min\": 296.15, \"sea_level\": 1011, \"feels_like\": 296.49," +
            " \"grnd_level\": 1009}, \"name\": \"Hanoi\", \"wind\": {\"deg\": 20, \"gust\": 3.08, \"speed\": 1.88}, " +
            "\"coord\": {\"lat\": 21.0245, \"lon\": 105.8412}, " +
            "\"clouds\": {\"all\": 100}, \"weather\": [{\"id\": 804, \"icon\": \"04d\", \"main\": \"Clouds\", \"description\": \"overcast clouds\"}], \"timezone\": 25200, \"visibility\": 10000}";
    private static final WeatherData DUMMY_WEATHER_DATA = new WeatherData();

    private static final LocalDate DUMMY_START_DATE = LocalDate.of(2021, 5, 28);
    private static final LocalDate DUMMY_END_DATE = LocalDate.of(2021, 5, 28);

    private static final int DUMMY_PAGE_SIZE = 1;
    private static final int DUMMY_PAGE_INDEX = 1;

    private static final CustomBadRequestException DUMMY_CUSTOM_BAD_REQUEST_EXCEPTION = new CustomBadRequestException("");


    @MockBean
    private WeatherRequestValidator weatherRequestValidator;

    @MockBean
    private WeatherDataConverter weatherDataConverter;

    @MockBean
    private WeatherCityRepository weatherCityRepository;

    @MockBean
    private WeatherDataProvider weatherDataProvider;

    @MockBean
    private Cache<String, WeatherData> todayWeatherDataCache;

    @MockBean
    private BlockingQueue<WeatherCity> updateWeatherCityQueue;

    @Autowired
    private WeatherServiceImpl weatherServiceImpl;


    @ParameterizedTest
    @MethodSource("buildInvalidGetTodayWeatherCityRequest")
    void getTodayWeatherCity_whenRequestIsInvalid_thenThrowCustomBadRequestException(GetTodayWeatherCityRequest request) {
        doThrow(DUMMY_CUSTOM_BAD_REQUEST_EXCEPTION).when(weatherRequestValidator).validateGetTodayWeatherCityRequest(request);

        assertThrows(CustomBadRequestException.class, () -> weatherServiceImpl.getTodayWeatherCity(request));
    }

    private static Stream<GetTodayWeatherCityRequest> buildInvalidGetTodayWeatherCityRequest() {
        return getInvalidCityNames().map(GetTodayWeatherCityRequest::new);
    }

    @Test
    void getTodayWeatherCity_whenRequestIsValid_andCachedDataExist_andCityIsNotFoundRecord_thenThrowCustomNotFoundException() {
        GetTodayWeatherCityRequest request = buildValidGetTodayWeatherCity();
        WeatherData notFoundRecord = WeatherData.buildNotFoundRecord();

        doNothing().when(weatherRequestValidator).validateGetTodayWeatherCityRequest(request);
        doReturn(notFoundRecord).when(todayWeatherDataCache).getIfPresent(DUMMY_STANDARDIZED_CITY_NAME);

        assertThrows(CustomNotFoundException.class, () -> weatherServiceImpl.getTodayWeatherCity(request));
    }

    @Test
    void getTodayWeatherCity_whenRequestIsValid_andCachedDataExist_andCityIsNormalRecord_thenReturnResponse() {
        GetTodayWeatherCityRequest request = buildValidGetTodayWeatherCity();

        doNothing().when(weatherRequestValidator).validateGetTodayWeatherCityRequest(request);
        doReturn(DUMMY_WEATHER_DATA).when(todayWeatherDataCache).getIfPresent(DUMMY_STANDARDIZED_CITY_NAME);

        GetTodayWeatherCityResponse response = weatherServiceImpl.getTodayWeatherCity(request);

        assertNotNull(response);
        assertNotNull(response.getWeatherData());
    }

    private GetTodayWeatherCityRequest buildValidGetTodayWeatherCity() {
        return new GetTodayWeatherCityRequest(DUMMY_CITY_NAME);
    }

    @Test
    void getTodayWeatherCity_whenRequestIsValid_andCachedDataIsNull_andDbHasTodayRecord_thenReturnResponse() {
        GetTodayWeatherCityRequest request = buildValidGetTodayWeatherCity();
        WeatherCity dummyWeatherCity = buildDummyWeatherCity();

        doNothing().when(weatherRequestValidator).validateGetTodayWeatherCityRequest(request);
        doReturn(null).when(todayWeatherDataCache).getIfPresent(DUMMY_STANDARDIZED_CITY_NAME);
        doReturn(dummyWeatherCity).when(weatherCityRepository).findByCityNameAndRetrieveDate(DUMMY_STANDARDIZED_CITY_NAME, LocalDate.now());
        doReturn(DUMMY_WEATHER_DATA).when(weatherDataConverter).parseWeatherDataJsonToObject(DUMMY_WEATHER_DATA_JSON);
        doNothing().when(todayWeatherDataCache).put(DUMMY_STANDARDIZED_CITY_NAME, DUMMY_WEATHER_DATA);

        GetTodayWeatherCityResponse response = weatherServiceImpl.getTodayWeatherCity(request);

        assertNotNull(response);
        assertNotNull(response.getWeatherData());
    }

    @Test
    void getTodayWeatherCity_whenRequestIsValid_andCachedDataIsNull_andDbNotHaveTodayRecord_andProviderReturnData_thenReturnResponse() {
        GetTodayWeatherCityRequest request = buildValidGetTodayWeatherCity();
        WeatherCity dummyWeatherCityForQueue = new WeatherCity(DUMMY_STANDARDIZED_CITY_NAME, LocalDate.now(), DUMMY_WEATHER_DATA_JSON);

        doNothing().when(weatherRequestValidator).validateGetTodayWeatherCityRequest(request);
        doReturn(null).when(todayWeatherDataCache).getIfPresent(DUMMY_STANDARDIZED_CITY_NAME);
        doReturn(null).when(weatherCityRepository).findByCityNameAndRetrieveDate(DUMMY_STANDARDIZED_CITY_NAME, LocalDate.now());
        doReturn(DUMMY_WEATHER_DATA).when(weatherDataProvider).getTodayWeatherData(DUMMY_STANDARDIZED_CITY_NAME);
        doNothing().when(todayWeatherDataCache).put(DUMMY_STANDARDIZED_CITY_NAME, DUMMY_WEATHER_DATA);
        doReturn(DUMMY_WEATHER_DATA_JSON).when(weatherDataConverter).writeWeatherDataToJson(DUMMY_WEATHER_DATA);
        doReturn(true).when(updateWeatherCityQueue).offer(dummyWeatherCityForQueue);

        GetTodayWeatherCityResponse response = weatherServiceImpl.getTodayWeatherCity(request);

        assertNotNull(response);
        assertNotNull(response.getWeatherData());
    }

    @Test
    void getTodayWeatherCity_whenRequestIsValid_andCachedDataIsNull_andDbNotHaveTodayRecord_andProviderReturnNotFound_thenThrowCustomNotFoundException() {
        GetTodayWeatherCityRequest request = buildValidGetTodayWeatherCity();
        WeatherData notFoundRecord = WeatherData.buildNotFoundRecord();

        doNothing().when(weatherRequestValidator).validateGetTodayWeatherCityRequest(request);
        doReturn(null).when(todayWeatherDataCache).getIfPresent(DUMMY_STANDARDIZED_CITY_NAME);
        doReturn(null).when(weatherCityRepository).findByCityNameAndRetrieveDate(DUMMY_STANDARDIZED_CITY_NAME, LocalDate.now());
        doReturn(notFoundRecord).when(weatherDataProvider).getTodayWeatherData(DUMMY_STANDARDIZED_CITY_NAME);

        assertThrows(CustomNotFoundException.class, () -> weatherServiceImpl.getTodayWeatherCity(request));
    }

    @ParameterizedTest
    @MethodSource("buildInvalidGetPeriodWeatherCityRequest")
    void getPeriodWeatherCity_whenRequestIsInvalid_thenThrowCustomBadRequestException(GetPeriodWeatherCityRequest request) {
        doThrow(DUMMY_CUSTOM_BAD_REQUEST_EXCEPTION).when(weatherRequestValidator).validateGetPeriodWeatherCityRequest(request);

        assertThrows(CustomBadRequestException.class, () -> weatherServiceImpl.getPeriodWeatherCity(request));
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
    void getPeriodWeatherCity_whenRequestIsValid_thenReturnResponse() {
        var dummyPaginationSetting = new PaginationSetting(DUMMY_PAGE_SIZE, DUMMY_PAGE_INDEX);
        var dummyRequest = new GetPeriodWeatherCityRequest(DUMMY_START_DATE, DUMMY_END_DATE, dummyPaginationSetting);

        var dummyWeatherCity = buildDummyWeatherCity();
        List<WeatherCity> dummyListWeatherCity = List.of(dummyWeatherCity);
        var dummyPaginationInfo = new PaginationInfo(dummyPaginationSetting.getPageIndex(), dummyPaginationSetting.getPageSize(), 2, 2);
        PaginationResult<WeatherCity> dummyPaginationResult = new PaginationResult<>(dummyPaginationInfo, dummyListWeatherCity);

        List<WeatherData> dummyListWeatherData = List.of(DUMMY_WEATHER_DATA);


        Mockito.doNothing().when(weatherRequestValidator).validateGetPeriodWeatherCityRequest(dummyRequest);
        doReturn(dummyPaginationResult).when(weatherCityRepository).findByPeriod(DUMMY_START_DATE, DUMMY_END_DATE, dummyPaginationSetting);
        doReturn(dummyListWeatherData).when(weatherDataConverter).getWeatherDataListFromWeatherCity(dummyListWeatherCity);

        GetPeriodWeatherCityResponse response = weatherServiceImpl.getPeriodWeatherCity(dummyRequest);

        assertNotNull(response);
        assertEquals(dummyPaginationInfo, response.getPaginationInfo());
        assertEquals(dummyListWeatherData, response.getWeatherDataList());
    }



    @ParameterizedTest
    @MethodSource("buildInvalidSaveNewWeatherCityRequest")
    void saveNewWeatherCity_whenRequestIsInvalid_thenThrowCustomBadRequestException(SaveNewWeatherCityRequest request) {
        doThrow(DUMMY_CUSTOM_BAD_REQUEST_EXCEPTION).when(weatherRequestValidator).validateSaveNewWeatherDataRequest(request);
        assertThrows(CustomBadRequestException.class, () -> weatherServiceImpl.saveNewWeatherCity(request));
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
    void saveNewWeatherCity_whenRequestIsValid_thenReturnResponse() {
        SaveNewWeatherCityRequest dummyRequest = new SaveNewWeatherCityRequest(DUMMY_CITY_NAME, DUMMY_RETRIEVE_DATE, DUMMY_WEATHER_DATA);
        WeatherCity dummyWeatherCity = buildDummyWeatherCityWithoutId();

        doNothing().when(weatherRequestValidator).validateSaveNewWeatherDataRequest(dummyRequest);
        doReturn(DUMMY_WEATHER_DATA_JSON).when(weatherDataConverter).writeWeatherDataToJson(DUMMY_WEATHER_DATA);
        doReturn(DUMMY_WEATHER_CITY_ID).when(weatherCityRepository).saveNewWeatherCity(dummyWeatherCity);

        SaveNewWeatherCityResponse response = weatherServiceImpl.saveNewWeatherCity(dummyRequest);

        assertNotNull(response);
        assertEquals(DUMMY_WEATHER_CITY_ID, response.getWeatherCityId());
    }


    @ParameterizedTest
    @MethodSource("buildInvalidUpdateWeatherCityRequest")
    void updateExistingWeatherCity_whenRequestIsInvalid_thenThrowCustomBadRequestException(UpdateWeatherCityRequest request) {
        doThrow(DUMMY_CUSTOM_BAD_REQUEST_EXCEPTION).when(weatherRequestValidator).validateUpdateWeatherCityRequest(request);
        assertThrows(CustomBadRequestException.class, () -> weatherServiceImpl.updateExistingWeatherCity(request));
    }

    private static Stream<UpdateWeatherCityRequest> buildInvalidUpdateWeatherCityRequest() {
        var cityNameIsNullRequest = new UpdateWeatherCityRequest(null, DUMMY_RETRIEVE_DATE, DUMMY_WEATHER_DATA);
        var cityNameIsEmptyRequest = new UpdateWeatherCityRequest("", DUMMY_RETRIEVE_DATE, DUMMY_WEATHER_DATA);
        var cityNameIsBlankRequest = new UpdateWeatherCityRequest("   ", DUMMY_RETRIEVE_DATE, DUMMY_WEATHER_DATA);

        var retrieveDateIsNullRequest = new UpdateWeatherCityRequest(DUMMY_CITY_NAME, null, DUMMY_WEATHER_DATA);
        var retrieveDateIsTomorrowRequest = new UpdateWeatherCityRequest(DUMMY_CITY_NAME, getTomorrow(), DUMMY_WEATHER_DATA);

        var missingWeatherDataRequest = new UpdateWeatherCityRequest(DUMMY_CITY_NAME, DUMMY_RETRIEVE_DATE, null);

        return Stream.of(
                cityNameIsNullRequest, cityNameIsEmptyRequest, cityNameIsBlankRequest,
                retrieveDateIsNullRequest, retrieveDateIsTomorrowRequest,
                missingWeatherDataRequest
        );
    }

    @Test
    void updateExistingWeatherCity_whenRequestIsValid_thenReturnResponse() {
        UpdateWeatherCityRequest dummyRequest = new UpdateWeatherCityRequest(DUMMY_CITY_NAME, DUMMY_RETRIEVE_DATE, DUMMY_WEATHER_DATA);
        WeatherCity dummyWeatherCity = buildDummyWeatherCity();

        doReturn(dummyWeatherCity).when(weatherRequestValidator).validateUpdateWeatherCityRequest(dummyRequest);
        doReturn(DUMMY_WEATHER_DATA_JSON).when(weatherDataConverter).writeWeatherDataToJson(DUMMY_WEATHER_DATA);
        doNothing().when(weatherCityRepository).updateExistingWeatherCity(dummyWeatherCity, DUMMY_WEATHER_DATA_JSON);

        UpdateWeatherCityResponse response = weatherServiceImpl.updateExistingWeatherCity(dummyRequest);

        assertNotNull(response);
        assertEquals(DUMMY_STANDARDIZED_CITY_NAME, response.getCityName());
        assertEquals(DUMMY_RETRIEVE_DATE, response.getRetrieveDate());
        assertEquals("Success", response.getMessage());
    }



    @ParameterizedTest
    @MethodSource("buildInvalidDeleteWeatherCityRequest")
    void deleteWeatherCity_whenRequestIsInvalid_thenThrowCustomBadRequestException(DeleteWeatherCityRequest request) {
        doThrow(DUMMY_CUSTOM_BAD_REQUEST_EXCEPTION).when(weatherRequestValidator).validateDeleteNewWeatherDataRequest(request);
        assertThrows(CustomBadRequestException.class, () -> weatherServiceImpl.deleteWeatherCity(request));
    }

    private static Stream<DeleteWeatherCityRequest> buildInvalidDeleteWeatherCityRequest() {
        var cityNameIsNullRequest = new DeleteWeatherCityRequest(null, DUMMY_RETRIEVE_DATE);
        var cityNameIsEmptyRequest = new DeleteWeatherCityRequest("", DUMMY_RETRIEVE_DATE);
        var cityNameIsBlankRequest = new DeleteWeatherCityRequest("   ", DUMMY_RETRIEVE_DATE);

        var retrieveDateIsNullRequest = new DeleteWeatherCityRequest(DUMMY_CITY_NAME, null);
        var retrieveDateIsTomorrowRequest = new DeleteWeatherCityRequest(DUMMY_CITY_NAME, getTomorrow());

        return Stream.of(
                cityNameIsNullRequest, cityNameIsEmptyRequest, cityNameIsBlankRequest,
                retrieveDateIsNullRequest, retrieveDateIsTomorrowRequest
        );
    }

    @Test
    void deleteWeatherCity_whenRequestIsValid_thenReturnResponse() {
        DeleteWeatherCityRequest request = new DeleteWeatherCityRequest(DUMMY_CITY_NAME, DUMMY_RETRIEVE_DATE);
        WeatherCity dummyWeatherCity = buildDummyWeatherCity();

        doNothing().when(weatherRequestValidator).validateDeleteNewWeatherDataRequest(request);
        doNothing().when(todayWeatherDataCache).invalidate(DUMMY_STANDARDIZED_CITY_NAME);
        doNothing().when(weatherCityRepository).deleteWeatherCity(DUMMY_STANDARDIZED_CITY_NAME, DUMMY_RETRIEVE_DATE);

        DeleteWeatherCityResponse response = weatherServiceImpl.deleteWeatherCity(request);

        assertNotNull(response);
        assertEquals(DUMMY_STANDARDIZED_CITY_NAME, response.getCityName());
        assertEquals(DUMMY_RETRIEVE_DATE, response.getRetrieveDate());
        assertEquals("Success", response.getMessage());
    }



    private WeatherCity buildDummyWeatherCity() {
        return new WeatherCity(DUMMY_WEATHER_CITY_ID, DUMMY_STANDARDIZED_CITY_NAME, DUMMY_RETRIEVE_DATE, DUMMY_WEATHER_DATA_JSON);
    }

    private WeatherCity buildDummyWeatherCityWithoutId() {
        return new WeatherCity(DUMMY_STANDARDIZED_CITY_NAME, DUMMY_RETRIEVE_DATE, DUMMY_WEATHER_DATA_JSON);
    }

    private static Stream<String> getInvalidCityNames() {
        return Stream.of(null, "", "   ");
    }

    private static LocalDate getTomorrow() {
        return LocalDate.now().plusDays(1);
    }
}
