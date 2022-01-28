package com.example.weather_rest_clone.config;

import com.example.weather_rest_clone.model.pojo.UserLoginInfo;
import com.example.weather_rest_clone.model.pojo.WeatherData;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean(name = "todayWeatherDataCache")
    public Cache<String, WeatherData> todayWeatherDataCache() {
        return Caffeine.newBuilder()
                .maximumSize(200)
                .expireAfter(new Expiry<String, WeatherData>() {

                    @Override
                    public long expireAfterCreate(@NonNull String key, @NonNull WeatherData value, long currentTime) {
                        return timeLeftBeforeTomorrow();
                    }

                    @Override
                    public long expireAfterUpdate(@NonNull String key, @NonNull WeatherData value, long currentTime, @NonNegative long currentDuration) {
                        return timeLeftBeforeTomorrow();
                    }

                    @Override
                    public long expireAfterRead(@NonNull String key, @NonNull WeatherData value, long currentTime, @NonNegative long currentDuration) {
                        return timeLeftBeforeTomorrow();
                    }
                })
                .build();
    }

    private long timeLeftBeforeTomorrow() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        var destroyTime = tomorrow.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant();

        return Duration.between(Instant.now(), destroyTime).toNanos();
    }

    @Value("${jwt-token.live-time-in-second}")
    private Long jwtLiveTimeInSecond;

    @Bean(name = "userLoginCache")
    public Cache<String, UserLoginInfo> userLoginCache() {
        return Caffeine.newBuilder()
                .maximumSize(200)
                .expireAfter(new Expiry<String, UserLoginInfo>() {

                    @Override
                    public long expireAfterCreate(@NonNull String key, @NonNull UserLoginInfo value, long currentTime) {
                        return TimeUnit.SECONDS.toNanos(jwtLiveTimeInSecond);
                    }

                    @Override
                    public long expireAfterUpdate(@NonNull String key, @NonNull UserLoginInfo value, long currentTime, @NonNegative long currentDuration) {
                        return currentDuration;
                    }

                    @Override
                    public long expireAfterRead(@NonNull String key, @NonNull UserLoginInfo value, long currentTime, @NonNegative long currentDuration) {
                        return currentDuration;
                    }
                })
                .build();
    }
}
