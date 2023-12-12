package com.example.androidauth.utils;

import com.google.gson.annotations.SerializedName;

public class GoogleResponse {
    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("expires_in")
    private int expiresIn;

    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("scope")
    private String scope;

    @SerializedName("token_type")
    private String tokenType;

    @SerializedName("id_token")
    private String idToken;

    // Getters and Setters...


    public String getAccessToken() {
        return accessToken;
    }
}
