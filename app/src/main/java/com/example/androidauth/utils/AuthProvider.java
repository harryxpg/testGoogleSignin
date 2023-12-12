package com.example.androidauth.utils;

public enum AuthProvider {
    GOOGLE("Google"),
    LINE("Line"),
    FACEBOOK("Facebook"),
    TWITTER("Twitter"),
    YAHOO("Yahoo");

    private final String providerName;

    AuthProvider(String providerName) {
        this.providerName = providerName;
    }

    public String getProviderName() {
        return providerName;
    }
}
