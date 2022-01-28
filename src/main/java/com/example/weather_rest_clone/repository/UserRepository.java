package com.example.weather_rest_clone.repository;

import com.example.weather_rest_clone.model.entity.User;
import org.springframework.lang.NonNull;

public interface UserRepository {

    User findByUsernameWithRoles(String username);

    void updateJwtTokenForUser(@NonNull User user, @NonNull String jwtToken);
}
