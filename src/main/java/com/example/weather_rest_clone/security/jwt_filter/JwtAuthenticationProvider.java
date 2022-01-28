package com.example.weather_rest_clone.security.jwt_filter;


import com.example.weather_rest_clone.exception.CustomUnauthenticatedException;
import com.example.weather_rest_clone.model.entity.User;
import com.example.weather_rest_clone.model.pojo.UserLoginInfo;
import com.example.weather_rest_clone.security.user_detail.SecurityUser;
import com.example.weather_rest_clone.security.user_detail.SecurityUserService;
import com.example.weather_rest_clone.service.util.JwtUtil;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final SecurityUserService securityUserService;

    private final Cache<String, UserLoginInfo> userLoginCache;

    private final JwtUtil jwtUtil;

    @Autowired
    public JwtAuthenticationProvider(SecurityUserService securityUserService, Cache<String, UserLoginInfo> userLoginCache, JwtUtil jwtUtil) {
        this.securityUserService = securityUserService;
        this.userLoginCache = userLoginCache;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String jwtToken = String.valueOf(authentication.getCredentials());

        UserLoginInfo userLoginInfo = userLoginCache.getIfPresent(username);
        if (checkAuthenticationWithCache(userLoginInfo, jwtToken)) {
            return new JwtAuthentication(authentication.getPrincipal(), authentication.getCredentials(),
                    SecurityUser.convertRolesToGrantedAuthorities(userLoginInfo.getRoles()));
        }

        var securityUser = (SecurityUser) securityUserService.loadUserByUsername(username);

        authenticateSecurityUser(securityUser, jwtToken);

        saveUserLoginInfoToCache(securityUser);

        return new JwtAuthentication(authentication.getPrincipal(), authentication.getCredentials(), securityUser.getAuthorities());
    }

    private boolean checkAuthenticationWithCache(UserLoginInfo userLoginInfo, String jwtToken) {
        if (userLoginInfo == null) {
            return false;
        }

        if (userLoginInfo.getLastJwtToken().equals(jwtToken)) {
            return true;
        }

        throw new CustomUnauthenticatedException("Bad credentials. Invalid Jwt token");
    }


    private void authenticateSecurityUser(SecurityUser securityUser, String jwtToken) {
        if (securityUser == null) {
            throw new CustomUnauthenticatedException("Bad credentials. Invalid user");
        }

        String userJwtToken = securityUser.getUser().getJwtToken();
        if (userJwtToken == null || jwtUtil.doesTokenExpire(userJwtToken) || !jwtToken.equals(userJwtToken)) {
            throw new CustomUnauthenticatedException("Bad credentials. Invalid Jwt token");
        }
    }

    private void saveUserLoginInfoToCache(SecurityUser securityUser) {
        User user = securityUser.getUser();
        UserLoginInfo userLoginInfo = new UserLoginInfo(user.getJwtToken(), user.getRoles());
        userLoginCache.put(user.getUsername(), userLoginInfo);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthentication.class.isAssignableFrom(authentication);
    }
}
