package com.example.weather_rest_clone.security.rate_limit_filter;

import com.example.weather_rest_clone.security.HttpResponseWriter;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Note: not register Filter as Bean to avoid Filter get provoked twice by Spring Security and Spring Boot
 */
public final class ApiRateLimitFilter extends OncePerRequestFilter {

    private final ApiRateLimitHandler apiRateLimitHandler;

    public ApiRateLimitFilter(ApiRateLimitHandler apiRateLimitHandler) {
        this.apiRateLimitHandler = apiRateLimitHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String ipAddress = request.getRemoteAddr();

        if (apiRateLimitHandler.checkIpAddrExceedRateLimit(ipAddress)) {
            HttpResponseWriter.updateHttpServletResponseToShowError(response, HttpStatus.TOO_MANY_REQUESTS, "Exceed API's limit");
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        List<String> listApiNeedFilter = List.of("/weather/today/");
        String requestUri = request.getRequestURI();

        return listApiNeedFilter.stream()
                .noneMatch(requestUri::contains);
    }
}
