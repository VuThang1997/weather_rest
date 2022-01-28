package com.example.weather_rest_clone.security;

import com.example.weather_rest_clone.model.entity.Role;
import com.example.weather_rest_clone.model.entity.User;
import com.example.weather_rest_clone.model.pojo.UserLoginInfo;
import com.example.weather_rest_clone.model.request.GetTodayWeatherCityRequest;
import com.example.weather_rest_clone.model.request.LoginRequest;
import com.example.weather_rest_clone.model.response.GetTodayWeatherCityResponse;
import com.example.weather_rest_clone.model.response.LoginResponse;
import com.example.weather_rest_clone.security.rate_limit_filter.ApiRateLimitHandler;
import com.example.weather_rest_clone.security.user_detail.SecurityUser;
import com.example.weather_rest_clone.security.user_detail.SecurityUserService;
import com.example.weather_rest_clone.service.domain_service.AccessService;
import com.example.weather_rest_clone.service.domain_service.WeatherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JwtAuthenticationFilterTest {

    private final String USERNAME = "staff";
    private final String TEST_URL = "/weather/today/hanoi";
    private static final String EXPIRED_JWT_TOKEN = "eyJhbGciOiJIUzM4NCJ9.eyJ1c2VybmFtZSI6InN0YWZmIiwiaWF0IjoxNjIzMzkyOTgzLCJleHAiOjE2MjMzOTMwNDN9.uQzpytp2pUswtaGyYCmROd_uzX-npbIKIbHuKYiFT0bETWFkP21_TwvwOtPp1_w5";
    private static final String MALFORMED_JWT_TOKEN = "eyJhbGciOiJIUzM4NCJ9.eyJ1c2VybmFtZSI6InN0YWZmIiwiaWF0IjoxNjIzMzkyOTgzLCJleHAiOjE2MjMzOTMwNDN9.uQzpytp2pUswtaGyYCmROd_uzX";
    private static final String VALID_JWT_TOKEN = "eyJhbGciOiJIUzM4NCJ9.eyJ1c2VybmFtZSI6InN0YWZmIiwiaWF0IjoxNjIzMzkzNDcyLCJleHAiOjI1NzIwNjU0NzJ9.-YbFAU3YWH5qUMSGNj4VNFipPsp6qznU268J51GwGYYo-ps9_EIY9KIz8FGs3WL1";

    @MockBean
    private ApiRateLimitHandler apiRateLimitHandler;

    @MockBean
    private Cache<String, UserLoginInfo> userLoginCache;

    @MockBean
    private SecurityUserService securityUserService;

    @MockBean
    private WeatherService weatherService;

    @MockBean
    private AccessService accessService;


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setupToPassApiRateLimitFilter() {
        final String mockIpAddr = "0:0:0:0:0:0:0:1";
        doReturn(false).when(apiRateLimitHandler).checkIpAddrExceedRateLimit(mockIpAddr);
    }


    @Test
    void callLogin_thenSkipJwtAuthentication_andResponseStatusIsOk() throws Exception {
        LoginRequest request = new LoginRequest("anonymous", "123");
        LoginResponse response = new LoginResponse(null);

        doReturn(response).when(accessService).login(request);

        mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isOk());
    }

    @Test
    void callTestUrl_whenMissingJwtTokenInHeader_thenResponseStatusIsForbidden() throws Exception {
        mockMvc.perform(get(TEST_URL)).andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @MethodSource("buildInvalidJwtToken")
    void callTestUrl_whenJwtTokenCannotBeParsedToClaims_thenResponseStatusIsForbidden(String invalidJwtToken) throws Exception {
        mockMvc.perform(
                buildTestHttpRequest(invalidJwtToken)
        ).andExpect(status().isForbidden());
    }

    private static Stream<String> buildInvalidJwtToken() {
        return Stream.of(EXPIRED_JWT_TOKEN, MALFORMED_JWT_TOKEN);
    }


    @Test
    void callTestUrl_whenCachedLoginInfoExist_andCacheTokenMatch_thenResponseStatusIsOk() throws Exception {
        UserLoginInfo mockUserLoginInfo = new UserLoginInfo(VALID_JWT_TOKEN, buildMockRoleList());
        doReturn(mockUserLoginInfo).when(userLoginCache).getIfPresent(USERNAME);

        GetTodayWeatherCityRequest mockRequest = new GetTodayWeatherCityRequest("hanoi");
        GetTodayWeatherCityResponse mockResponse = new GetTodayWeatherCityResponse(null);
        doReturn(mockResponse).when(weatherService).getTodayWeatherCity(mockRequest);

        mockMvc.perform(buildTestHttpRequest(VALID_JWT_TOKEN)).andExpect(status().isOk());
    }

    @Test
    void callTestUrl_whenCachedLoginInfoExist_andCacheTokenNotMatch_thenResponseStatusIsForbidden() throws Exception {
        final String lastJwtToken = "eyJhbGciOiJIUzM4NCJ9.eyJ1c2VybmFtZSI6InN0YWZmIiwiaWF0IjoxNjIzMzk1NTgwLCJleHAiOjE2MjMzOTkxODB9.veII91JCCqQ4HluxaZoyMW_HVReVJojZCl0IsRGfY2u9P48WKWOvGkURKCECJ5vh";
        UserLoginInfo mockUserLoginInfo = new UserLoginInfo(lastJwtToken, buildMockRoleList());
        doReturn(mockUserLoginInfo).when(userLoginCache).getIfPresent(USERNAME);

        GetTodayWeatherCityRequest mockRequest = new GetTodayWeatherCityRequest("hanoi");
        GetTodayWeatherCityResponse mockResponse = new GetTodayWeatherCityResponse(null);
        doReturn(mockResponse).when(weatherService).getTodayWeatherCity(mockRequest);

        mockMvc.perform(buildTestHttpRequest(VALID_JWT_TOKEN)).andExpect(status().isForbidden());
    }

    @Test
    void callTestUrl_whenCachedLoginInfoNotExist_andSecurityUserNotFound_thenResponseStatusIsForbidden() throws Exception {
        doReturn(null).when(userLoginCache).getIfPresent(USERNAME);
        doReturn(null).when(securityUserService).loadUserByUsername(USERNAME);

        mockMvc.perform(buildTestHttpRequest(VALID_JWT_TOKEN)).andExpect(status().isForbidden());
    }

    @Test
    void callTestUrl_whenCachedLoginInfoNotExist_andSecurityUserNotHaveToken_thenResponseStatusIsForbidden() throws Exception {
        User mockUser = new User(USERNAME, null, null, buildMockRoleList());
        SecurityUser mockSecurityUser = new SecurityUser(mockUser);

        doReturn(null).when(userLoginCache).getIfPresent(USERNAME);
        doReturn(mockSecurityUser).when(securityUserService).loadUserByUsername(USERNAME);

        mockMvc.perform(buildTestHttpRequest(VALID_JWT_TOKEN)).andExpect(status().isForbidden());
    }

    @Test
    void callTestUrl_whenCachedLoginInfoNotExist_andSecurityUserHasExpiredToken_thenResponseStatusIsForbidden() throws Exception {
        User mockUser = new User(USERNAME, null, EXPIRED_JWT_TOKEN, buildMockRoleList());
        SecurityUser mockSecurityUser = new SecurityUser(mockUser);

        doReturn(null).when(userLoginCache).getIfPresent(USERNAME);
        doReturn(mockSecurityUser).when(securityUserService).loadUserByUsername(USERNAME);

        mockMvc.perform(buildTestHttpRequest(VALID_JWT_TOKEN)).andExpect(status().isForbidden());
    }

    @Test
    void callTestUrl_whenCachedLoginInfoNotExist_andSecurityUserExist_andTokenNotMatch_thenResponseStatusIsForbidden() throws Exception {
        final String lastJwtToken = "eyJhbGciOiJIUzM4NCJ9.eyJ1c2VybmFtZSI6InN0YWZmIiwiaWF0IjoxNjIzMzk1NTgwLCJleHAiOjE2MjMzOTkxODB9.veII91JCCqQ4HluxaZoyMW_HVReVJojZCl0IsRGfY2u9P48WKWOvGkURKCECJ5vh";
        User mockUser = new User(USERNAME, null, lastJwtToken, buildMockRoleList());
        SecurityUser mockSecurityUser = new SecurityUser(mockUser);

        doReturn(null).when(userLoginCache).getIfPresent(USERNAME);
        doReturn(mockSecurityUser).when(securityUserService).loadUserByUsername(USERNAME);

        mockMvc.perform(buildTestHttpRequest(VALID_JWT_TOKEN)).andExpect(status().isForbidden());
    }

    @Test
    void callTestUrl_whenCachedLoginInfoNotExist_andSecurityUserExist_andTokenMatch_thenResponseStatusIsOk() throws Exception {
        doReturn(null).when(userLoginCache).getIfPresent(USERNAME);

        User mockUser = new User(USERNAME, null, VALID_JWT_TOKEN, buildMockRoleList());
        SecurityUser mockSecurityUser = new SecurityUser(mockUser);
        doReturn(mockSecurityUser).when(securityUserService).loadUserByUsername(USERNAME);

        UserLoginInfo mockLoginInfo = new UserLoginInfo(mockUser.getJwtToken(), mockUser.getRoles());
        doNothing().when(userLoginCache).put(USERNAME, mockLoginInfo);

        GetTodayWeatherCityRequest mockRequest = new GetTodayWeatherCityRequest("hanoi");
        GetTodayWeatherCityResponse mockResponse = new GetTodayWeatherCityResponse(null);
        doReturn(mockResponse).when(weatherService).getTodayWeatherCity(mockRequest);

        mockMvc.perform(buildTestHttpRequest(VALID_JWT_TOKEN)).andExpect(status().isOk());
    }

    private RequestBuilder buildTestHttpRequest(String jwtToken) {
        return get(TEST_URL).header("Authorization", jwtToken);
    }

    private List<Role> buildMockRoleList() {
        return List.of(new Role("STAFF"));
    }

}