package com.chiknas.swancloud.api.services.authentication;

import java.util.List;

public class JwtToken {

    private String token;
    private String type;
    private String refreshToken;
    private Long id;
    private String username;
    private List<String> roles;

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public List<String> getRoles() {
        return roles;
    }
}
