package com.example.androidauth.utils;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface YahooApiService {

    @GET("openid/v1/userinfo")
    Call<ResponseBody> getUserInfo(
            @Header("Authorization")String authorization
    );
}
