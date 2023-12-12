package com.example.androidauth.utils;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.service.autofill.UserData;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;

import com.example.androidauth.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GoogleSignInHandler implements SignInHandler {
    private OAuthVerificationHandler verificationHandler;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private Activity mActivity;

    public GoogleSignInHandler(Activity activity ,ActivityResultLauncher<Intent> launcher,OAuthVerificationHandler verificationHandler) {
        this.mActivity = activity;
        this.verificationHandler = verificationHandler;
        this.googleSignInLauncher = launcher;
        this.performSignIn();

    }

    public void performSignIn(){

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.OPEN_ID), new Scope(Scopes.PLUS_ME))
                .requestServerAuthCode(mActivity.getString(R.string.your_web_client_id))
                .requestIdToken(mActivity.getString(R.string.your_web_client_id))
                .requestProfile()
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(mActivity, gso);

        SignInButton signInBtn = mActivity.findViewById(R.id.googleSignInButton);
        signInBtn.setOnClickListener(view -> googleSignInLauncher.launch(mGoogleSignInClient.getSignInIntent()));

    }

    public void handleSignInResult(Task<GoogleSignInAccount> task){

        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            Log.d("GoogleLoginlog", "DisplayName : " + account.getDisplayName());

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://www.googleapis.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            GoogleOAuthApiService service = retrofit.create(GoogleOAuthApiService.class);
            service.getToken(
                    "authorization_code",
                    mActivity.getString(R.string.your_web_client_id),
                    mActivity.getString(R.string.your_web_client_secret),
                    mActivity.getString(R.string.redirect_uri),
                    account.getServerAuthCode()
            ).enqueue(new Callback<GoogleResponse>() {
                @Override
                public void onResponse(Call<GoogleResponse> call, Response<GoogleResponse> response) {
                    if (response.isSuccessful()) {
                        GoogleResponse googleResponse = response.body();
                        String responseAccessToken = googleResponse.getAccessToken();
                        Log.d(TAG, responseAccessToken);

                        AuthProvider provider = AuthProvider.GOOGLE;
                        verificationHandler.retrofitRequest( responseAccessToken ,provider.getProviderName());
                        // Call retrofitRequest or other methods to handle the response...
                    } else {
                        Log.e(TAG, "Response not successful");
                    }
                }

                @Override
                public void onFailure(Call<GoogleResponse> call, Throwable t) {
                    Log.e(TAG, "Failed to get token", t);
                }
            });

        } catch (ApiException e) {
            Log.w("TAG", "signInResult:failed code=" + e.getStatusCode());
        }
    }

    public  void  onSignInSuccess(UserData data){

    }

    public void onSignInFailure(String errorMessage){

    }

}
