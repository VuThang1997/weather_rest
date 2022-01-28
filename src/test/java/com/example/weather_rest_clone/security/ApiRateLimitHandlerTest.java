package com.example.weather_rest_clone.security;

import com.example.weather_rest_clone.security.rate_limit_filter.ApiRateLimitHandler;
import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ApiRateLimitHandlerTest {

    private static final String IP_ADDR = "127.0.0.1";
    private static final int MAX_REQUEST_PER_IP_CONFIG = 4;
    private static final int WINDOW_DURATION_IN_MINUTE_CONFIG = 3;

    private final ApiRateLimitHandler apiRateLimitHandler = new ApiRateLimitHandler();


    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(apiRateLimitHandler, "maxRequestPerIp", MAX_REQUEST_PER_IP_CONFIG);
        ReflectionTestUtils.setField(apiRateLimitHandler, "windowDurationInMinute", WINDOW_DURATION_IN_MINUTE_CONFIG);
    }

    @AfterEach
    void tearDown() {
        apiRateLimitHandler.clearRequestCounterStore();
    }

    @Test
    void checkIpAddrExceedRateLimit_whenIpNotReachLimit_thenReturnFalse() {
        //nigh-exhaust limit
        for (int i = 1; i < MAX_REQUEST_PER_IP_CONFIG; i++) {
            assertFalse(apiRateLimitHandler.checkIpAddrExceedRateLimit(IP_ADDR));
        }

        assertFalse(apiRateLimitHandler.checkIpAddrExceedRateLimit(IP_ADDR));
    }

    @Test
    void checkIpAddrExceedRateLimit_whenIpReachLimit_thenReturnTrue() {
        exhaustIpLimit();
        assertTrue(apiRateLimitHandler.checkIpAddrExceedRateLimit(IP_ADDR));
    }

    @Test
    void checkIpAddrExceedRateLimit_whenIpReachLimit_andWindowSlides_thenReturnFalse() {
        exhaustIpLimit();
        assertTrue(apiRateLimitHandler.checkIpAddrExceedRateLimit(IP_ADDR));

        final long windowDurationInMilli = TimeUnit.MINUTES.toMillis(WINDOW_DURATION_IN_MINUTE_CONFIG);
        sleep(windowDurationInMilli);

        assertFalse(apiRateLimitHandler.checkIpAddrExceedRateLimit(IP_ADDR));
    }

    @Test
    void testExpireTimeAspect_whenIpCounterCacheNotUpdateInALongTime_thenItGetDeleted() {
        exhaustIpLimit();

        final long ipExpireTimeFromLassAccessInMinute = (long) ReflectionTestUtils.getField(apiRateLimitHandler, "IP_EXPIRE_TIME_FROM_LAST_ACCESS_IN_MINUTE");
        sleep(TimeUnit.MINUTES.toMillis(ipExpireTimeFromLassAccessInMinute));

        final Cache<String, Cache<Long, Integer>> counterStore = (Cache<String, Cache<Long, Integer>>) ReflectionTestUtils.getField(apiRateLimitHandler, "REQUEST_COUNTER_STORE");
        assertNotNull(counterStore);
        assertNull(counterStore.getIfPresent(IP_ADDR));
    }

    private void exhaustIpLimit() {
        for (int i = 1; i <= MAX_REQUEST_PER_IP_CONFIG; i++) {
            assertFalse(apiRateLimitHandler.checkIpAddrExceedRateLimit(IP_ADDR));
        }
    }

    private void sleep(long windowDurationInMilli) {
        try {
            Thread.sleep(windowDurationInMilli);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
