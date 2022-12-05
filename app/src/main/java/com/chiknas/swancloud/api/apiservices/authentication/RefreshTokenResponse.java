package com.chiknas.swancloud.api.apiservices.authentication;

public class RefreshTokenResponse {

    private String accessToken;
    private long accessTokenExpiry;
    private String tokenType;

    public String getAccessToken() {
        return accessToken;
    }

    public long getAccessTokenExpiry() {
        return accessTokenExpiry;
    }

    public String getTokenType() {
        return tokenType;
    }
}
