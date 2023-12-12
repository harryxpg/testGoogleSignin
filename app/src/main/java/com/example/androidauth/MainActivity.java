package com.example.androidauth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.androidauth.utils.FacebookSignInHandler;
import com.example.androidauth.utils.GoogleSignInHandler;
import com.example.androidauth.utils.LineSignInHandler;
import com.example.androidauth.utils.OAuthVerificationHandler;
import com.example.androidauth.utils.YahooSignInHandler;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    private OAuthVerificationHandler verificationHandler;
    //Google
    private GoogleSignInHandler googleSignInHandler;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    //Facebook
    private FacebookSignInHandler facebookSignInHandler;
    //Line
    private LineSignInHandler lineSignInHandler;
    private ActivityResultLauncher<Intent> lineSignInLauncher;
    //Yahoo
    private YahooSignInHandler yahooSignInHandler;
    private ActivityResultLauncher<Intent> yahooSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verificationHandler = new OAuthVerificationHandler();
        initGoogle();
        initFacebook();
        initLine();
        initYahoo();
    }

    private void initGoogle(){
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        googleSignInHandler.handleSignInResult(task);
                    }
                }
        );

        googleSignInHandler = new GoogleSignInHandler(this, googleSignInLauncher ,verificationHandler);
    }

    private void initFacebook(){
        facebookSignInHandler = new FacebookSignInHandler(this,verificationHandler);
    }


    private void initLine(){
        lineSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> lineSignInHandler.handleLineLoginResult(result.getResultCode(), result.getData())
        );

        lineSignInHandler = new LineSignInHandler(this,lineSignInLauncher ,verificationHandler);
    }

    private void initYahoo(){
        yahooSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> yahooSignInHandler.handleAuthorizationResponse(result.getData())
        );

        yahooSignInHandler = new YahooSignInHandler(this, yahooSignInLauncher ,verificationHandler);
    }

}
