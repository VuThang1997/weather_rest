package com.example.weather_rest_clone.security.rate_limit_filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class ApiRateLimitHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiRateLimitHandler.class);

    private static final long MAX_NUMBER_OF_IP_CAN_BE_CACHED = 500L;
    private static final long IP_EXPIRE_TIME_FROM_LAST_ACCESS_IN_MINUTE = 5L;

    private static final Cache<String, Cache<Long, Integer>> REQUEST_COUNTER_STORE = buildRequestCounterStore();

    private static Cache<String, Cache<Long, Integer>> buildRequestCounterStore() {
        return Caffeine.newBuilder()
                .maximumSize(MAX_NUMBER_OF_IP_CAN_BE_CACHED)
                .expireAfterAccess(IP_EXPIRE_TIME_FROM_LAST_ACCESS_IN_MINUTE, TimeUnit.MINUTES)
                .build();
    }

    private final Object lock = new Object();

    @Value("${api-rate-limit.max-request-per-ip:5}")
    private int maxRequestPerIp;

    @Value("${api-rate-limit.window-duration-in-minute:5}")
    private long windowDurationInMinute;

    private long getWindowDurationInMilli() {
        return TimeUnit.MINUTES.toMillis(windowDurationInMinute);
    }



    public boolean checkIpAddrExceedRateLimit(String ipAddr) {
        final long currentMillis = System.currentTimeMillis();
        final long nearestMinuteInMilli = getNearestMinuteInMilli(currentMillis);
        final long startWindowTime = currentMillis - getWindowDurationInMilli();

        synchronized (lock) {
            Cache<Long, Integer> requestCounterCache = REQUEST_COUNTER_STORE.get(ipAddr, ip -> buildIpRequestCounterCache());
            if (countPreviousRequestInWindowDuration(requestCounterCache, startWindowTime) >= maxRequestPerIp) {
                return true;
            }

            registerRequestToCounterCache(requestCounterCache, nearestMinuteInMilli);

            LOGGER.info("IpAddr: {} | requestCounter: {} - {}", ipAddr, nearestMinuteInMilli, requestCounterCache.getIfPresent(nearestMinuteInMilli));
        }

        return false;
    }

    public void clearRequestCounterStore() {
        synchronized (lock) {
            for (var ipRequestCounter : REQUEST_COUNTER_STORE.asMap().entrySet()) {
                ipRequestCounter.getValue().invalidateAll();
                REQUEST_COUNTER_STORE.invalidate(ipRequestCounter.getKey());
            }
        }
    }

    private long getNearestMinuteInMilli(long currentMilli) {
        var instant = Instant.ofEpochMilli(currentMilli);
        return instant.truncatedTo(ChronoUnit.MINUTES).toEpochMilli();
    }

    private Cache<Long, Integer> buildIpRequestCounterCache() {
        return Caffeine.newBuilder()
                .maximumSize(maxRequestPerIp)
                .expireAfter(new Expiry<Long, Integer>() {

                    @Override
                    public long expireAfterCreate(@NonNull Long key, @NonNull Integer value, long currentTime) {
                        return TimeUnit.MILLISECONDS.toNanos(getWindowDurationInMilli());
                    }

                    @Override
                    public long expireAfterUpdate(@NonNull Long key, @NonNull Integer value, long currentTime, @NonNegative long currentDuration) {
                        return currentDuration;
                    }

                    @Override
                    public long expireAfterRead(@NonNull Long key, @NonNull Integer value, long currentTime, @NonNegative long currentDuration) {
                        return currentDuration;
                    }
                })
                .build();
    }

    private int countPreviousRequestInWindowDuration(Cache<Long, Integer> requestCounterCache, long startWindowTime) {
        return requestCounterCache
                .asMap()
                .entrySet().stream()
                .filter(entry -> entry.getKey() >= startWindowTime)         // this filter is like a defensive barrier, quite redundant but may be necessary in some cases
                .map(Map.Entry::getValue)
                .reduce(0, Integer::sum);
    }

    private void registerRequestToCounterCache(Cache<Long, Integer> requestCounterCache, long nearestMinuteInMilli) {
        int previousValue = requestCounterCache.get(nearestMinuteInMilli, k -> 0);
        requestCounterCache.put(nearestMinuteInMilli, previousValue + 1);
    }
}
