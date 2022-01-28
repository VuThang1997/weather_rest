package com.example.weather_rest_clone.security.jwt_filter;


import com.example.weather_rest_clone.exception.CustomUnauthenticatedException;
import com.example.weather_rest_clone.security.HttpResponseWriter;
import com.example.weather_rest_clone.service.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Note: not register Filter as Bean to avoid Filter get provoked twice by Spring Security and Spring Boot
 */
public final class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;

    private final AuthenticationManager authenticationManager;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException {
        String jwtToken = request.getHeader("Authorization");

        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            HttpResponseWriter.updateHttpServletResponseToShowError(response, HttpStatus.FORBIDDEN, "Jwt token not found");
            return;
        }

        try {
            Claims claims = jwtUtil.parseJwtToken(jwtToken);
            String username = String.valueOf(claims.get("username"));

            Authentication jwtAuth = new JwtAuthentication(username, jwtToken);
            Authentication authenticatedJwtAuth = authenticationManager.authenticate(jwtAuth);

            var securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authenticatedJwtAuth);

            filterChain.doFilter(request, response);

        } catch (CustomUnauthenticatedException e) {
            HttpResponseWriter.updateHttpServletResponseToShowError(response, HttpStatus.FORBIDDEN, e.getDescription());

        } catch (JwtException e) {
            LOGGER.error("JwtAuthenticationFilter get jwt exception: ", e);
            HttpResponseWriter.updateHttpServletResponseToShowError(response, HttpStatus.FORBIDDEN, "Invalid jwt token");

        } catch (Exception e) {
            LOGGER.error("JwtAuthenticationFilter get exception: ", e);
            HttpResponseWriter.updateHttpServletResponseToShowError(response, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        List<String> notFilterApis = List.of("/login");
        for (String api : notFilterApis) {
            if (api.equals(request.getRequestURI())) {
                return true;
            }
        }

        return false;
    }

}
