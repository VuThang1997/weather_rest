package com.example.weather_rest_clone.service.domain_service.impl;


import com.example.weather_rest_clone.exception.CustomNotFoundException;
import com.example.weather_rest_clone.exception.CustomUnauthenticatedException;
import com.example.weather_rest_clone.model.entity.User;
import com.example.weather_rest_clone.model.pojo.UserLoginInfo;
import com.example.weather_rest_clone.model.request.LoginRequest;
import com.example.weather_rest_clone.model.response.LoginResponse;
import com.example.weather_rest_clone.repository.UserRepository;
import com.example.weather_rest_clone.service.domain_service.AccessService;
import com.example.weather_rest_clone.service.util.JwtUtil;
import com.example.weather_rest_clone.service.validator.AccessRequestValidator;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccessServiceImpl implements AccessService {

    private final UserRepository userRepository;

    private final AccessRequestValidator accessRequestValidator;

    private final Cache<String, UserLoginInfo> userLoginCache;

    private final JwtUtil jwtUtil;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AccessServiceImpl(UserRepository userRepository, AccessRequestValidator accessRequestValidator,
                             Cache<String, UserLoginInfo> userLoginCache, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.accessRequestValidator = accessRequestValidator;
        this.userRepository = userRepository;
        this.userLoginCache = userLoginCache;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        accessRequestValidator.validateLoginRequest(request);

        String jwtToken = getJwtTokenFromCache(request.getUsername());
        if (jwtToken == null) {
            jwtToken = getJwtTokenFromDb(request.getUsername(), request.getPassword());
        }

        return new LoginResponse(jwtToken);
    }

    private String getJwtTokenFromCache(String username) {
        UserLoginInfo userLoginInfo = userLoginCache.getIfPresent(username);
        return (userLoginInfo == null) ? null : userLoginInfo.getLastJwtToken();
    }

    private String getJwtTokenFromDb(String username, String rawPassword) {
        User user = userRepository.findByUsernameWithRoles(username);
        if (user == null) {
            throw new CustomNotFoundException("User not found");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new CustomUnauthenticatedException("Bad credentials");
        }

        renewJwtTokenIfNecessary(user);

        saveUserLoginInfoToCache(user);

        return user.getJwtToken();
    }

    private void renewJwtTokenIfNecessary(User user) {
        String jwtToken = user.getJwtToken();
        if (jwtToken == null || jwtUtil.doesTokenExpire(jwtToken)) {
            renewJwtToken(user);
        }
    }

    private void saveUserLoginInfoToCache(User user) {
        UserLoginInfo userLoginInfo = new UserLoginInfo(user.getJwtToken(), user.getRoles());
        userLoginCache.put(user.getUsername(), userLoginInfo);
    }

    private String renewJwtToken(User user) {
        String renewToken = jwtUtil.generateNewJwtToken(user);
        userRepository.updateJwtTokenForUser(user, renewToken);
        return renewToken;
    }
}
