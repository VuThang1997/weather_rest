package com.example.weather_rest_clone.model.enumeration;

import lombok.Getter;

@Getter
public enum Authority {

    ADMIN("ADMIN"), STAFF("STAFF");

    private final String authority;

    Authority(String authority) {
        this.authority = authority;
    }

    public static Authority getInstance(String roleName) {
        if (roleName == null) {
            return null;
        }

        for (Authority role: Authority.values()) {
            if (role.getAuthority().equalsIgnoreCase(roleName)) {
                return role;
            }
        }

        return null;
    }
}
