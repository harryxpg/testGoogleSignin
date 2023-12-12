package com.example.androidauth.utils;

import static android.content.ContentValues.TAG;

import android.util.Log;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OAuthVerificationHandler {
    private static final String BASE_URL = "http://192.168.17.57:8180/";

    public void retrofitRequest(String accessToken, String authType) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        OAuthApiService oAuthApiService = retrofit.create(OAuthApiService.class);

        oAuthApiService.authenticate(accessToken, authType).enqueue(new Callback<VerificationResponse>() {
            @Override
            public void onResponse(Call<VerificationResponse> call, Response<VerificationResponse> response) {
                if (response.isSuccessful()) {
                    VerificationResponse oauthResponse = response.body();
                    if (oauthResponse != null) {
                        // 使用 oauthResponse 的數據
                        boolean isAuth = oauthResponse.getAuth();
                        String responseToken = oauthResponse.getToken();
                        // ...其他處理
                    }

                    Log.d(TAG, "response isSuccessful call " + call);
                    Log.d(TAG, "response isSuccessful response " + response);
                    Log.d(TAG, "response isSuccessful oauthResponse " + oauthResponse);
                } else {
                    // 處理錯誤響應
                    Log.d(TAG, "response isFail call " + call);
                    Log.d(TAG, "response isFail response " + response);
                }
            }

            @Override
            public void onFailure(Call<VerificationResponse> call, Throwable t) {
                // 處理網絡錯誤或請求失敗
                Log.d(TAG, "response onFailure call " + call);
                Log.d(TAG, "response onFailure t " + t);
            }
        });
    }
}

