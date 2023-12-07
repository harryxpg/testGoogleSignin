package com.example.android_auth

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android_auth.navigation.Navigation
import com.example.android_auth.screens.Home
import com.example.android_auth.screens.Login
import com.example.android_auth.ui.theme.Android_authTheme
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.Identity.getSignInClient
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import java.util.Date
import kotlin.math.log


class MainActivity : ComponentActivity() {

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var signUpRequest: BeginSignInRequest
    private lateinit var getSignInIntentRequest: GetSignInIntentRequest
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
    private var showOneTapUI = true
    private val oneTapContract = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 处理成功的结果
            Log.d(TAG, "successfully. Result data: ${result.data}")
            Log.d(TAG, "One Tap UI launched successfully.")
        } else {
            // 处理失败的结果
            Log.e(TAG, "Failed to launch One Tap UI. Result code: ${result.resultCode}")
            Log.e(TAG, "Failed. Result data: ${result.data}")

        }
    }

    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initGoogleSignIn()

//        setContent {
//            Android_authTheme {
//                // A surface container using the 'background' color from the theme
//                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
//                    Navigation(this)
//                }
//            }
//        }
//        val googleSignInButton: SignInButton = findViewById(R.id.googleSignInButton)

        // 设置点击事件
//        googleSignInButton.setOnClickListener {
//            // 处理 Google 登录按钮点击事件
//            // 启动 Google 登录流程
//            singInGoogle()
//        }

//        val btnGoogleSignOut: Button = findViewById(R.id.btnGoogleSignOut)
//        btnGoogleSignOut.setOnClickListener {
//            // 执行 Google One Tap 注销操作
//            signOutFromOneTap()
//        }



    }

    private fun initGoogleSignIn(){


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(Scopes.OPEN_ID), Scope(Scopes.PLUS_ME))
            .requestIdToken(getString(R.string.your_app_client_id))
            .requestProfile()
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        val signInBtn: SignInButton = findViewById(R.id.login_button)

        signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d(TAG,"signIn ${result.resultCode}")
            if (result.resultCode == RESULT_OK) {
                // 处理登录成功的情况
                // 可以在 result.data 中获取登录结果的数据
                 val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                 handleSignInResult(account)
                Log.d(TAG,"RESULT_OK ${account}")
            } else {
                // 处理登录失败的情况
//                Snackbar.make(signInBtn, "Login failed", Snackbar.LENGTH_SHORT).show()
            }
        }

        signInBtn.setOnClickListener {
            signInLauncher.launch(mGoogleSignInClient.signInIntent)
        }


//        oneTapClient = Identity.getSignInClient(this)
//        signInRequest = BeginSignInRequest.builder()
//            .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
//                .setSupported(true)
//                .build())
//            .setGoogleIdTokenRequestOptions(
//                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
//                    .setSupported(true)
//                    // Your server's client ID, not your Android client ID.
//                    .setServerClientId(getString(R.string.your_web_client_id))
//                    // Only show accounts previously used to sign in.
//                    .setFilterByAuthorizedAccounts(true)
//                    .build())
//            // Automatically sign in when exactly one credential is retrieved.
//            .setAutoSelectEnabled(true)
//            .build()
//        signUpRequest = BeginSignInRequest.builder()
//            .setGoogleIdTokenRequestOptions(
//                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
//                    .setSupported(true)
//                    // Your server's client ID, not your Android client ID.
//                    .setServerClientId(getString(R.string.your_web_client_id))
//                    // Show all accounts on the device.
//                    .setFilterByAuthorizedAccounts(false)
//                    .build())
//            .build()
//        getSignInIntentRequest = GetSignInIntentRequest.builder()
//            .setNonce(Date().time.toString())
//            .setServerClientId(getString(R.string.your_app_client_id))
//            .build()
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            // 代表有成功
            val account = completedTask.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.
            Log.d("GoogleLoginlog", "DisplayName : " + account!!.displayName)
            Log.d("GoogleLoginlog", "GivenName : " + account.givenName)
            Log.d("GoogleLoginlog", "FamilyName : " + account.familyName)
            Log.d("GoogleLoginlog", "Email : " + account.email)
            Log.d("GoogleLoginlog", "Id : " + account.id)
            Log.d("GoogleLoginlog", "idtoken : " + account.idToken)
            Log.d("GoogleLoginlog", "PhotoUrl : " + account.photoUrl)

            val it = Intent(this, ShowInfo::class.java)
            it.putExtra("displayname", account.displayName)
            it.putExtra("givenname", account.givenName)
            it.putExtra("familyname", account.familyName)
            it.putExtra("id", account.id)
            it.putExtra("idtoken", account.idToken)
            it.putExtra("email", account.email)
            it.putExtra("photourl", account.photoUrl?.toString())
            startActivity(it)
        } catch (e: ApiException) {
            // 代表沒成功
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("TAG", "signInResult:failed code=" + e.statusCode)
        }
    }

    private fun singInGoogle(){
        Log.d(TAG, "singInGoogle.")

//        oneTapClient.beginSignIn(signInRequest)
//            .addOnSuccessListener(this) { result ->
//                try {
//                    oneTapContract.launch(IntentSenderRequest.Builder(result.pendingIntent.intentSender).build())
//                } catch (e: IntentSender.SendIntentException) {
//                    Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
//                }
//            }
//            .addOnFailureListener(this) { e ->
//                // No saved credentials found. Launch the One Tap sign-up flow, or
//                // do nothing and continue presenting the signed-out UI.
//                Log.d(TAG, e.localizedMessage)
//            }

//        oneTapClient.beginSignIn(signUpRequest)
//            .addOnSuccessListener(this) { result ->
//                try {
//                    oneTapContract.launch(IntentSenderRequest.Builder(result.pendingIntent.intentSender).build())
//                } catch (e: IntentSender.SendIntentException) {
//                    Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
//                }
//            }
//            .addOnFailureListener(this) { e ->
//                // No Google Accounts found. Just continue presenting the signed-out UI.
//                Log.d(TAG, e.localizedMessage)
//            }


        oneTapClient
            .getSignInIntent(getSignInIntentRequest)
            .addOnSuccessListener { pendingIntent ->
                try {
                    oneTapContract.launch(IntentSenderRequest.Builder(pendingIntent.intentSender).build())
                } catch (e: IntentSender.SendIntentException) {
                    Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener {
                // No saved credentials found. Launch the One Tap sign-up flow, or
                // do nothing and continue presenting the signed-out UI.
                Log.d(TAG, it.localizedMessage)
            }
    }

    private fun signOutFromOneTap() {
        // 获取 Google SignInClient
//        val signInClient = getSignInClient(this)

        // 清除本地缓存的用户会话信息（可根据实际情况添加）
        // ...

        // 发起 Google One Tap 注销
        oneTapClient.signOut()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 注销成功
                    Log.d(TAG, "One Tap sign out successful.")
                } else {
                    // 注销失败
                    Log.e(TAG, "One Tap sign out failed: ${task.exception?.localizedMessage}")
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_ONE_TAP -> {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    val username = credential.id
                    val password = credential.password
                    when {
                        idToken != null -> {
                            // Got an ID token from Google. Use it to authenticate
                            // with your backend.
                            Log.d(TAG, "Got ID token.")
                        }
                        password != null -> {
                            // Got a saved username and password. Use them to authenticate
                            // with your backend.
                            Log.d(TAG, "Got password.")
                        }
                        else -> {
                            // Shouldn't happen.
                            Log.d(TAG, "No ID token or password!")
                        }
                    }
                } catch (e: ApiException) {
                    when (e.statusCode) {
                        CommonStatusCodes.CANCELED -> {
                            Log.d(TAG, "One-tap dialog was closed.")
                            // Don't re-prompt the user.
                            showOneTapUI = false
                        }
                        CommonStatusCodes.NETWORK_ERROR -> {
                            Log.d(TAG, "One-tap encountered a network error.")
                            // Try again or just ignore.
                        }
                        else -> {
                            Log.d(TAG, "Couldn't get credential from result." +
                                    " (${e.localizedMessage})")
                        }
                    }
                }
            }
        }
    }
}
