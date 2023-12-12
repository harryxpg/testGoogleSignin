package com.example.androidauth.utils;

import com.google.gson.annotations.SerializedName;

public class VerificationResponse {

    @SerializedName("isAuth")
    private Boolean isAuth;

    @SerializedName("token")
    private String token;

    public Boolean getAuth() {
        return isAuth;
    }

    public String getToken() {
        return token;
    }
}
