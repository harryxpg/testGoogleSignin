package com.example.androidauth.utils;

import android.app.Activity;
import android.content.Intent;
import android.service.autofill.UserData;

public interface SignInHandler {

    // 處理登入邏輯
    void performSignIn();

    // 處理登入結果
//    void  handldSignInResult(Intent data);

    // 處理登入成功返回的資料
    void onSignInSuccess(UserData userData);

    // 處理登入失敗訊息
    void onSignInFailure(String errorMessage);

}
