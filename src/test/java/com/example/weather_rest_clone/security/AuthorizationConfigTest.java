package com.example.weather_rest_clone.security;

import com.example.weather_rest_clone.model.enumeration.Authority;
import com.example.weather_rest_clone.model.request.*;
import com.example.weather_rest_clone.model.response.*;
import com.example.weather_rest_clone.security.jwt_filter.JwtAuthentication;
import com.example.weather_rest_clone.security.rate_limit_filter.ApiRateLimitHandler;
import com.example.weather_rest_clone.service.domain_service.AccessService;
import com.example.weather_rest_clone.service.domain_service.WeatherService;
import com.example.weather_rest_clone.service.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.istack.NotNull;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.doReturn;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AuthorizationConfigTest {

    private final String UNKNOWN_AUTHORITY = "UNKNOWN";
    private final String HEADER_FOR_JWT_TOKEN = "Authorization";
    private final String MOCK_JWT_TOKEN = "eyJhbGciOiJIUzM4NCJ9.eyJ1c2VybmFtZSI6InN0YWZmIiwiaWF0IjoxNjIzMjk0ODU1LCJleHAiOjE2MjMzODEyNTV9.V9H5AYQRqsRdXGiNWUY5Z_teet8XW7g2fNZ2Z3EiwhZXH3lZfEGpVaK4oSu2xhZO";


    private final String GET_TODAY_WEATHER_CITY_URL = "/weather/today/hanoi";
    private final GetTodayWeatherCityRequest mockGetTodayWeatherCityRequest = new GetTodayWeatherCityRequest(null);
    private final GetTodayWeatherCityResponse mockGetTodayWeatherCityResponse = new GetTodayWeatherCityResponse(null);

    private final String GET_PERIOD_WEATHER_CITY_URL = "/weather/period";
    private final GetPeriodWeatherCityRequest mockGetPeriodWeatherCityRequest = new GetPeriodWeatherCityRequest(null, null, null);
    private final GetPeriodWeatherCityResponse mockGetPeriodWeatherCityResponse = new GetPeriodWeatherCityResponse(null, null);

    private final String SAVE_NEW_WEATHER_CITY_URL = "/weather";
    private final SaveNewWeatherCityRequest mockSaveNewWeatherCityRequest = new SaveNewWeatherCityRequest(null, null, null);
    private final SaveNewWeatherCityResponse mockSaveNewWeatherCityResponse = new SaveNewWeatherCityResponse(-1);

    private final String UPDATE_EXISTING_WEATHER_CITY_URL = "/weather";
    private final UpdateWeatherCityRequest mockUpdateWeatherCityRequest = new UpdateWeatherCityRequest(null, null, null);
    private final UpdateWeatherCityResponse mockUpdateWeatherCityResponse = new UpdateWeatherCityResponse(null, null, null);

    private final String DELETE_WEATHER_CITY_URL = "/weather";
    private final DeleteWeatherCityRequest mockDeleteWeatherCityRequest = new DeleteWeatherCityRequest(null, null);
    private final DeleteWeatherCityResponse mockDeleteWeatherCityResponse = new DeleteWeatherCityResponse(null, null, null);


    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private ApiRateLimitHandler apiRateLimitHandler;

    @MockBean
    private AccessService accessService;

    @MockBean
    private WeatherService weatherService;


    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        setupToPassApiRateLimitFilter();
    }

    private void setupToPassAuthenticationFlow(String username, GrantedAuthority authority) {
        setupToPassJwtFilter(username, authority);
    }

    private void setupToPassApiRateLimitFilter() {
        final String mockIpAddr = "0:0:0:0:0:0:0:1";
        doReturn(false).when(apiRateLimitHandler).checkIpAddrExceedRateLimit(mockIpAddr);
    }

    private void setupToPassJwtFilter(String username, GrantedAuthority authority) {
        Claims mockJwtClaim = buildMockJwtClaims(username);
        doReturn(mockJwtClaim).when(jwtUtil).parseJwtToken(MOCK_JWT_TOKEN);

        JwtAuthentication incompleteJwtAuth = new JwtAuthentication(username, MOCK_JWT_TOKEN);
        JwtAuthentication completeJwtAuth = new JwtAuthentication(username, MOCK_JWT_TOKEN, List.of(authority));
        doReturn(completeJwtAuth).when(authenticationManager).authenticate(incompleteJwtAuth);
    }



    @Test
    @WithAnonymousUser
    void login_whenAnonymousUserAccess_thenResponseStatusIsOk() throws Exception {
        LoginRequest request = new LoginRequest("anonymous", "123");
        LoginResponse response = new LoginResponse(null);

        doReturn(response).when(accessService).login(request);

        mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isOk());
    }


    @ParameterizedTest
    @MethodSource("buildValidMockUserForGetTodayWeatherCity")
    void getTodayWeatherCity_whenUserHasValidAuthority_thenResponseStatusIsOk(UserAuthority userAuthority) throws Exception {
        setupToPassAuthenticationFlow(userAuthority.username, userAuthority.authority);

        doReturn(mockGetTodayWeatherCityResponse).when(weatherService).getTodayWeatherCity(mockGetTodayWeatherCityRequest);

        mockMvc.perform(
                get(GET_TODAY_WEATHER_CITY_URL).header(HEADER_FOR_JWT_TOKEN, MOCK_JWT_TOKEN)
        ).andExpect(status().isOk());
    }

    private static Stream<UserAuthority> buildValidMockUserForGetTodayWeatherCity() {
        return Stream.of(
                new UserAuthority("admin", Authority.ADMIN.getAuthority()),
                new UserAuthority("staff", Authority.STAFF.getAuthority())
        );
    }

    @Test
    void getTodayWeatherCity_whenUserDoesNotHaveValidAuthority_thenResponseStatusIsForbidden() throws Exception {
        UserAuthority userAuthority = new UserAuthority("stranger", UNKNOWN_AUTHORITY);
        setupToPassAuthenticationFlow(userAuthority.username, userAuthority.authority);

        doReturn(mockGetTodayWeatherCityResponse).when(weatherService).getTodayWeatherCity(mockGetTodayWeatherCityRequest);

        mockMvc.perform(
                get(GET_TODAY_WEATHER_CITY_URL).header(HEADER_FOR_JWT_TOKEN, MOCK_JWT_TOKEN)
        ).andExpect(status().isForbidden());
    }


    @ParameterizedTest
    @MethodSource("buildValidMockUserForGetPeriodWeatherCity")
    void getPeriodWeatherCity_whenUserHasValidAuthority_thenResponseStatusIsOk(UserAuthority userAuthority) throws Exception {
        setupToPassAuthenticationFlow(userAuthority.username, userAuthority.authority);

        doReturn(mockGetPeriodWeatherCityResponse).when(weatherService).getPeriodWeatherCity(mockGetPeriodWeatherCityRequest);

        mockMvc.perform(
                post(GET_PERIOD_WEATHER_CITY_URL)
                        .header(HEADER_FOR_JWT_TOKEN, MOCK_JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockGetPeriodWeatherCityRequest))
        ).andExpect(status().isOk());
    }

    private static Stream<UserAuthority> buildValidMockUserForGetPeriodWeatherCity() {
        return Stream.of(
                new UserAuthority("admin", Authority.ADMIN.getAuthority()),
                new UserAuthority("staff", Authority.STAFF.getAuthority())
        );
    }

    @Test
    void getPeriodWeatherCity_whenUserDoesNotHaveValidAuthority_thenResponseStatusIsForbidden() throws Exception {
        UserAuthority userAuthority = new UserAuthority("stranger", UNKNOWN_AUTHORITY);
        setupToPassAuthenticationFlow(userAuthority.username, userAuthority.authority);

        doReturn(mockGetPeriodWeatherCityResponse).when(weatherService).getPeriodWeatherCity(mockGetPeriodWeatherCityRequest);

        mockMvc.perform(
                post(GET_PERIOD_WEATHER_CITY_URL)
                        .header(HEADER_FOR_JWT_TOKEN, MOCK_JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockGetPeriodWeatherCityRequest))
        ).andExpect(status().isForbidden());
    }



    @ParameterizedTest
    @MethodSource("buildValidMockUserForSaveNewWeatherCity")
    void saveNewWeatherCity_whenUserHasValidAuthority_thenResponseStatusIsOk(UserAuthority userAuthority) throws Exception {
        setupToPassAuthenticationFlow(userAuthority.username, userAuthority.authority);

        doReturn(mockSaveNewWeatherCityResponse).when(weatherService).saveNewWeatherCity(mockSaveNewWeatherCityRequest);

        mockMvc.perform(
                post(SAVE_NEW_WEATHER_CITY_URL)
                        .header(HEADER_FOR_JWT_TOKEN, MOCK_JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockSaveNewWeatherCityRequest))
        ).andExpect(status().isOk());
    }

    private static Stream<UserAuthority> buildValidMockUserForSaveNewWeatherCity() {
        return Stream.of(
                new UserAuthority("admin", Authority.ADMIN.getAuthority())
        );
    }

    @Test
    void saveNewWeatherCity_whenUserDoesNotHaveValidAuthority_thenResponseStatusIsForbidden() throws Exception {
        UserAuthority userAuthority = new UserAuthority("stranger", UNKNOWN_AUTHORITY);
        setupToPassAuthenticationFlow(userAuthority.username, userAuthority.authority);

        doReturn(mockSaveNewWeatherCityResponse).when(weatherService).saveNewWeatherCity(mockSaveNewWeatherCityRequest);

        mockMvc.perform(
                post(SAVE_NEW_WEATHER_CITY_URL)
                        .header(HEADER_FOR_JWT_TOKEN, MOCK_JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockSaveNewWeatherCityRequest))
        ).andExpect(status().isForbidden());
    }



    @ParameterizedTest
    @MethodSource("buildValidMockUserForUpdateExistingWeatherCity")
    void updateExistingWeatherCity_whenUserHasValidAuthority_thenResponseStatusIsOk(UserAuthority userAuthority) throws Exception {
        setupToPassAuthenticationFlow(userAuthority.username, userAuthority.authority);

        doReturn(mockUpdateWeatherCityResponse).when(weatherService).updateExistingWeatherCity(mockUpdateWeatherCityRequest);

        mockMvc.perform(
                put(UPDATE_EXISTING_WEATHER_CITY_URL)
                        .header(HEADER_FOR_JWT_TOKEN, MOCK_JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockUpdateWeatherCityRequest))
        ).andExpect(status().isOk());
    }

    private static Stream<UserAuthority> buildValidMockUserForUpdateExistingWeatherCity() {
        return Stream.of(
                new UserAuthority("admin", Authority.ADMIN.getAuthority())
        );
    }

    @Test
    void updateExistingWeatherCity_whenUserDoesNotHaveValidAuthority_thenResponseStatusIsForbidden() throws Exception {
        UserAuthority userAuthority = new UserAuthority("stranger", UNKNOWN_AUTHORITY);
        setupToPassAuthenticationFlow(userAuthority.username, userAuthority.authority);

        doReturn(mockUpdateWeatherCityResponse).when(weatherService).updateExistingWeatherCity(mockUpdateWeatherCityRequest);

        mockMvc.perform(
                put(UPDATE_EXISTING_WEATHER_CITY_URL)
                        .header(HEADER_FOR_JWT_TOKEN, MOCK_JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockUpdateWeatherCityRequest))
        ).andExpect(status().isForbidden());
    }



    @ParameterizedTest
    @MethodSource("buildValidMockUserForDeleteWeatherCity")
    void deleteWeatherCity_whenUserHasValidAuthority_thenResponseStatusIsOk(UserAuthority userAuthority) throws Exception {
        setupToPassAuthenticationFlow(userAuthority.username, userAuthority.authority);

        doReturn(mockDeleteWeatherCityResponse).when(weatherService).deleteWeatherCity(mockDeleteWeatherCityRequest);

        mockMvc.perform(
                delete(DELETE_WEATHER_CITY_URL)
                        .header(HEADER_FOR_JWT_TOKEN, MOCK_JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockDeleteWeatherCityRequest))
        ).andExpect(status().isOk());
    }

    private static Stream<UserAuthority> buildValidMockUserForDeleteWeatherCity() {
        return Stream.of(
                new UserAuthority("admin", Authority.ADMIN.getAuthority())
        );
    }

    @Test
    void deleteWeatherCity_whenUserDoesNotHaveValidAuthority_thenResponseStatusIsForbidden() throws Exception {
        UserAuthority userAuthority = new UserAuthority("stranger", UNKNOWN_AUTHORITY);
        setupToPassAuthenticationFlow(userAuthority.username, userAuthority.authority);

        doReturn(mockDeleteWeatherCityResponse).when(weatherService).deleteWeatherCity(mockDeleteWeatherCityRequest);

        mockMvc.perform(
                delete(DELETE_WEATHER_CITY_URL)
                        .header(HEADER_FOR_JWT_TOKEN, MOCK_JWT_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockDeleteWeatherCityRequest))
        ).andExpect(status().isForbidden());
    }


    @NotNull
    private Claims buildMockJwtClaims(String username) {
        Claims mockJwtClaim = new DefaultClaims();
        mockJwtClaim.put("username", username);

        return mockJwtClaim;
    }

    private static class UserAuthority {
        final String username;
        final SimpleGrantedAuthority authority;

        public UserAuthority(String username, String authorityName) {
            this.username = username;
            this.authority = new SimpleGrantedAuthority(authorityName.toUpperCase());
        }
    }

}
