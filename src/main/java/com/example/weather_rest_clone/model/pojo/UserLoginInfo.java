package com.example.weather_rest_clone.model.pojo;

import com.example.weather_rest_clone.model.entity.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@NoArgsConstructor
public class UserLoginInfo {

    private String lastJwtToken;

    private List<Role> roles;

    public UserLoginInfo(String lastJwtToken, List<Role> roles) {
        this.lastJwtToken = lastJwtToken;
        this.roles = roles;
    }
}
