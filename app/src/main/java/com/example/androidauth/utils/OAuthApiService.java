package com.example.androidauth.utils;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OAuthApiService {

    @GET("v1/oAuth/app/callback")
    Call<VerificationResponse> authenticate(
            @Query("access_token") String accessToken,
            @Query("auth_type") String authType
    );

}
