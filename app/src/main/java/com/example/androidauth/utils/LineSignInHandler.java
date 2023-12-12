package com.example.androidauth.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;

import com.example.androidauth.R;
import com.linecorp.linesdk.LineApiResponseCode;
import com.linecorp.linesdk.Scope;
import com.linecorp.linesdk.auth.LineAuthenticationParams;
import com.linecorp.linesdk.auth.LineLoginApi;
import com.linecorp.linesdk.auth.LineLoginResult;

import java.util.Arrays;
import java.util.UUID;


public class LineSignInHandler {

    private OAuthVerificationHandler verificationHandler;
    private ActivityResultLauncher<Intent> lineSignInLauncher;
    private Activity mActivity;

    public LineSignInHandler(Activity activity , ActivityResultLauncher<Intent> launcher, OAuthVerificationHandler verificationHandler) {
        this.mActivity = activity;
        this.verificationHandler = verificationHandler;
        this.lineSignInLauncher = launcher;
        this.performSignIn();

    }

    public void performSignIn(){
        Button customLineLoginButton = mActivity.findViewById(R.id.button_custom_line_login);
        customLineLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLineLogin(mActivity);
            }
        });
    }

    private String generateRandomState() {
        return UUID.randomUUID().toString();
    }

    private void startLineLogin(Activity activity) {
        String randomState = generateRandomState();
        Scope lineScope = Scope.PROFILE;
        Scope lineOpenIdScope = Scope.OPENID_CONNECT;

        Intent loginIntent = LineLoginApi.getLoginIntent(
                activity,
                activity.getString(R.string.line_channelId),
                new LineAuthenticationParams.Builder()
                        .scopes(Arrays.asList(lineScope, lineOpenIdScope))
                        .build()
        );
        lineSignInLauncher.launch(loginIntent);
    }

    public void handleLineLoginResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            LineLoginResult loginResult = LineLoginApi.getLoginResultFromIntent(data);
            if (loginResult.getResponseCode() == LineApiResponseCode.SUCCESS) {
                // 處理登入成功
                String idToken = loginResult.getLineIdToken().getRawString();
                // 調用 retrofitRequest 等...
                AuthProvider provider = AuthProvider.LINE;
                verificationHandler.retrofitRequest( idToken ,provider.getProviderName());
            } else {
                // 處理登入失敗或取消
            }
        } else {
            // 處理登入失敗或取消
        }
    }

}
