package com.example.androidauth.utils;

import android.app.Activity;
import android.util.Log;
import com.example.androidauth.R;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

public class FacebookSignInHandler {
    private OAuthVerificationHandler verificationHandler;
    private CallbackManager facebookCallbackManager;
    private Activity mActivity;

    public FacebookSignInHandler(Activity activity , OAuthVerificationHandler verificationHandler) {
        this.mActivity = activity;
        this.verificationHandler = verificationHandler;
        FacebookSdk.fullyInitialize();
        facebookCallbackManager = CallbackManager.Factory.create();
        // 初始化 Facebook 登入邏輯...
        this.performSignIn();
    }

    // Facebook 登入初始化邏輯...
    public void performSignIn(){
        LoginButton loginButton = mActivity.findViewById(R.id.login_button);
        loginButton.setPermissions("email");

        // 註冊回調
        loginButton.registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // 應用程式代碼
                Log.d("TAG", "Facebook login Button Callback successfully.");
                Log.d("TAG", loginResult.getAccessToken().getUserId());
                // 其他代碼...
            }

            @Override
            public void onCancel() {
                Log.d("TAG", "Facebook login Button Callback Cancel.");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("TAG", "Facebook login Button Callback Error.");
            }
        });

        // 註冊 LoginManager 的回調
        LoginManager.getInstance().registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // 應用程式代碼
                Log.d("TAG", "Facebook LoginManager Callback successfully.");
                Log.d("TAG", loginResult.getAccessToken().getUserId());
                // 其他代碼...
                String responseAccessToken = loginResult.getAccessToken().getToken();

                AuthProvider provider = AuthProvider.FACEBOOK;
                verificationHandler.retrofitRequest( responseAccessToken ,provider.getProviderName());
            }

            @Override
            public void onCancel() {
                Log.d("TAG", "Facebook LoginManager Callback Cancel.");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("TAG", "Facebook LoginManager Callback Error.");
            }
        });
    }

}
