package com.example.androidauth.utils;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;

import com.example.androidauth.R;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.CodeVerifierUtil;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenRequest;
import net.openid.appauth.TokenResponse;

import org.json.JSONException;
import org.json.JSONObject;

import io.jsonwebtoken.io.IOException;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class YahooSignInHandler {

    private static final int RC_AUTH = 1001;
    private static final String AUTHENTICATION_END_POINT = "https://api.login.yahoo.com/oauth2/request_auth?lang=en-US";
    private static final String TOKEN_END_POINT = "https://api.login.yahoo.com/oauth2/get_token";
    private static final String REDIRECT_URL = "myapp://redirecturipath";
    private static final String CLIENT_SECRET = "e9c19b1b79b987969fa44abc1e437c2c2456b0a1";
    private static final String CLIENT_ID = "dj0yJmk9eEpuM2ZiZjFHaWxxJmQ9WVdrOU5qTmhZVUpKT1VVbWNHbzlNQT09JnM9Y29uc3VtZXJzZWNyZXQmc3Y9MCZ4PTAx";
    private static final String SHARED_PREFERENCES_NAME = "AuthStatePreference";
    private static final String AUTH_STATE = "AUTH_STATE";

    private OAuthVerificationHandler verificationHandler;
    private ActivityResultLauncher<Intent> yahooSignInLauncher;
    private YahooApiService yahooApiService;
    private Activity mActivity;

    public YahooSignInHandler(Activity activity , ActivityResultLauncher<Intent> launcher, OAuthVerificationHandler verificationHandler) {
        this.mActivity = activity;
        this.verificationHandler = verificationHandler;
        this.yahooSignInLauncher = launcher;
        this.performSignIn();

    }

    public void performSignIn(){
// 初始化 Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.login.yahoo.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        yahooApiService = retrofit.create(YahooApiService.class);

        // 初始化登入按鈕和登入流程
        Button customYahooLoginButton = mActivity.findViewById(R.id.button_custom_yahoo_login);
        customYahooLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doAuthorization();
            }
        });
    }

    private void doAuthorization() {
        AuthorizationService authService = new AuthorizationService(mActivity);
        Intent authIntent = authService.getAuthorizationRequestIntent(getAuthRequest());
        yahooSignInLauncher.launch(authIntent);
    }

    private AuthorizationRequest getAuthRequest() {
        Uri tokenEndPointUri = Uri.parse(TOKEN_END_POINT);
        Uri authenticationEndPointUri = Uri.parse(AUTHENTICATION_END_POINT);
        AuthorizationServiceConfiguration configuration = new AuthorizationServiceConfiguration(authenticationEndPointUri, tokenEndPointUri);

        return new AuthorizationRequest.Builder(
                configuration,
                CLIENT_ID,
                ResponseTypeValues.CODE,
                Uri.parse(REDIRECT_URL)
        ).setLoginHint("sheng@xpg.tech")
                .setCodeVerifier(CodeVerifierUtil.generateRandomCodeVerifier()) // 添加用於 PKCE 的隨機代碼驗證器
                .build();
    }

    private void persistAuthState(AuthState authState) {
        SharedPreferences sharedPreferences = mActivity.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit()
                .putString(AUTH_STATE, authState.jsonSerializeString())
                .apply(); // 或者使用 commit() 來立即提交變更
    }

    private AuthState restoreAuthState() {
        SharedPreferences sharedPreferences = mActivity.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String jsonString = sharedPreferences.getString(AUTH_STATE, null);
        if (jsonString != null && !jsonString.isEmpty()) {
            try {
                return AuthState.jsonDeserialize(jsonString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void handleAuthorizationResponse(Intent intent) {
        if (intent != null) {
            AuthorizationResponse response = AuthorizationResponse.fromIntent(intent);
            AuthorizationException error = AuthorizationException.fromIntent(intent);
            AuthState authState = new AuthState(response, error);

            if (response != null) {
                AuthorizationService service = new AuthorizationService(mActivity);
                TokenRequest request = response.createTokenExchangeRequest();

                service.performTokenRequest(request, new AuthorizationService.TokenResponseCallback() {
                    @Override
                    public void onTokenRequestCompleted(@Nullable TokenResponse tokenResponse, @Nullable AuthorizationException exception) {
                        if (tokenResponse != null) {
                            authState.update(tokenResponse, exception);
                            persistAuthState(authState);

                            // 獲取用戶信息
                            getUserInfo();
                        }
                    }
                });
            }
        }
    }

    private void getUserInfo() {
        AuthState authState = restoreAuthState();
        AuthorizationService authorizationService = new AuthorizationService(mActivity);

        if (authState != null) {
            authState.performActionWithFreshTokens(authorizationService, new AuthState.AuthStateAction() {
                @Override
                public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException exception) {
                    if (accessToken != null) {

                        Log.d(TAG, "getUserInfo accessToken ");
                        AuthProvider provider = AuthProvider.YAHOO;
                        verificationHandler.retrofitRequest( accessToken ,provider.getProviderName());
                        yahooApiService.getUserInfo("Bearer " + accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if (response.isSuccessful()) {
                                    try {
                                        String userInfoString = response.body().toString();
                                        JSONObject userJSONObject = new JSONObject(userInfoString);
                                        setUserProfile(userJSONObject);
                                    } catch (IOException | JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Log.e("TAG", "getUserInfo onFailure", t);
                            }
                        });
                    }
                }
            });
        }
    }

    private void setUserProfile(JSONObject userJSONObject) {
        // 在這裡處理用戶個人資料的更新
        // 例如更新 UI 或保存數據
    }

}
