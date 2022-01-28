package com.example.weather_rest_clone.security;


import com.example.weather_rest_clone.model.enumeration.Authority;
import com.example.weather_rest_clone.security.jwt_filter.JwtAuthenticationFilter;
import com.example.weather_rest_clone.security.jwt_filter.JwtAuthenticationProvider;
import com.example.weather_rest_clone.security.rate_limit_filter.ApiRateLimitFilter;
import com.example.weather_rest_clone.security.rate_limit_filter.ApiRateLimitHandler;
import com.example.weather_rest_clone.service.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationProvider jwtAuthenticationProvider;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ApiRateLimitHandler apiRateLimitHandler;

    @Override
    @Bean
    protected AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(jwtAuthenticationProvider);
    }

    private JwtAuthenticationFilter jwtAuthenticationFilter() throws Exception {
        return new JwtAuthenticationFilter(authenticationManager(), jwtUtil);
    }

    private ApiRateLimitFilter apiRateLimitFilter() {
        return new ApiRateLimitFilter(apiRateLimitHandler);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .cors().disable();

        http.addFilterBefore(apiRateLimitFilter(), BasicAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter(), BasicAuthenticationFilter.class);

        http.authorizeRequests()
                .mvcMatchers("/weather/today/**", "/weather/period").hasAnyAuthority(Authority.ADMIN.getAuthority(), Authority.STAFF.getAuthority())
                .mvcMatchers(HttpMethod.POST,"/weather").hasAuthority(Authority.ADMIN.getAuthority())
                .mvcMatchers(HttpMethod.PUT,"/weather").hasAuthority(Authority.ADMIN.getAuthority())
                .mvcMatchers(HttpMethod.DELETE,"/weather").hasAuthority(Authority.ADMIN.getAuthority())
                .anyRequest().permitAll();
    }
}
