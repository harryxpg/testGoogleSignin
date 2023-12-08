package com.example.androidauth

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.facebook.FacebookSdk
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.linecorp.linesdk.LineApiResponseCode
import com.linecorp.linesdk.auth.LineAuthenticationParams
import com.linecorp.linesdk.auth.LineLoginApi
import org.json.JSONException
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private lateinit var facebookCallbackManager: CallbackManager  // 在這裡初始化 CallbackManager
    private lateinit var lineLoginLauncher:ActivityResultLauncher<Intent>

    private val EMAIL = "email"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initFacebook()

        initGoogleSignIn()

        initLineSignIn()

    }

    private fun initLineSignIn(){

        // 初始化 ActivityResultLauncher
        lineLoginLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // 處理登入成功
                val loginResult = LineLoginApi.getLoginResultFromIntent(result.data)
                //                handleLineLoginSuccess(loginResult)
                Log.d(TAG, "handleLineLoginSuccess.")
                Log.d(TAG, "data : ${loginResult.lineIdToken}" )

                if(loginResult.responseCode == LineApiResponseCode.SUCCESS ) {
                    Log.d(TAG, "Line login response success")
                }else{
                    Log.d(TAG, "LineApiResponse. ${loginResult.errorData.message}")
                }

            } else {
                // 處理登入失敗或取消
//                handleLineLoginFailure()
                Log.e(TAG, "handleLineLoginFailure.")
            }
        }

        val customLineLoginButton: Button = findViewById(R.id.button_custom_line_login)
        customLineLoginButton.setOnClickListener {
            // 啟動 LINE 登入流程
            startLineLogin()
        }

    }

    private fun generateRandomState(): String {
        return UUID.randomUUID().toString()
    }

    private fun startLineLogin() {

        val randomState = generateRandomState()
        val lineScope = com.linecorp.linesdk.Scope.PROFILE
        val lineOpenIdScope = com.linecorp.linesdk.Scope.OPENID_CONNECT

        val loginIntent = LineLoginApi.getLoginIntent(
            this,
            getString(R.string.line_channelId),
            LineAuthenticationParams.Builder()
                .scopes(listOf(lineScope,lineOpenIdScope))
                .build()
        )
        lineLoginLauncher.launch(loginIntent)
    }

    private fun initFacebook(){
        FacebookSdk.fullyInitialize()
//        AppEventsLogger.activateApp(this);


// 初始化 CallbackManager
        facebookCallbackManager = CallbackManager.Factory.create()


// 在你的 onCreate 方法中
        val loginButton: LoginButton = findViewById(R.id.login_button)
        loginButton.setPermissions(EMAIL)
// 如果你在 fragment 中使用，调用 loginButton.setFragment(this)

// 回调注册
        loginButton.registerCallback(facebookCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                // 应用代码
                Log.d(TAG, "Facebook login Button Callback successfully.")
                Log.d(TAG, result.accessToken.userId)
            }

            override fun onCancel() {
                // 应用代码
                Log.d(TAG, "Facebook login Button Callback Cancel.")
            }

            override fun onError(error: FacebookException) {
                // 应用代码
                Log.d(TAG, "Facebook login Button Callback Error.")
            }
        })

        // 註冊 LoginManager 的回調
        LoginManager.getInstance().registerCallback(facebookCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                // 應用程式代碼
                Log.d(TAG, "Facebook LoginManager Callback successfully.")
                Log.d(TAG, result.accessToken.userId)
                // 用戶成功登入後，使用 Graph API 獲取用戶信息
                val request = GraphRequest.newMeRequest(
                    result.accessToken
                ) { jsonObject, _ ->
                    try {
                        val name = jsonObject?.optString("name") ?: "Default Name"
                        val email = jsonObject?.optString("email", "No Email")  ?: "No Email"// 安全地處理沒有電子郵件的情況
                        // 在這裡處理用戶名稱和電子郵件
                        // 例如更新 UI 或保存數據

                        Log.d(TAG, "name : ${name}")
                        Log.d(TAG, "email : ${email}")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }

                val parameters = Bundle()
                parameters.putString("fields", "id,name,email")
                request.parameters = parameters
                request.executeAsync()
            }

            override fun onCancel() {
                // 應用程式代碼
                Log.d(TAG, "Facebook LoginManager Callback Cancel.")

            }

            override fun onError(error: FacebookException) {
                // 應用程式代碼
                Log.d(TAG, "Facebook LoginManager Callback Error.")
            }
        })


    }


    private fun initGoogleSignIn(){

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(Scopes.OPEN_ID), Scope(Scopes.PLUS_ME))
            .requestServerAuthCode(getString(R.string.your_web_client_id))
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestProfile()
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        val signInBtn: SignInButton = findViewById(R.id.googleSignInButton)

        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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
            googleSignInLauncher.launch(mGoogleSignInClient.signInIntent)
        }
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
            Log.d("GoogleLoginlog", "serverAuthCode : " + account.serverAuthCode)

//            val it = Intent(this, ShowInfo::class.java)
//            it.putExtra("displayname", account.displayName)
//            it.putExtra("givenname", account.givenName)
//            it.putExtra("familyname", account.familyName)
//            it.putExtra("id", account.id)
//            it.putExtra("idtoken", account.idToken)
//            it.putExtra("email", account.email)
//            it.putExtra("photourl", account.photoUrl?.toString())
//            it.putExtra("serverAuthCode", account.serverAuthCode)
//            startActivity(it)
        } catch (e: ApiException) {
            // 代表沒成功
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("TAG", "signInResult:failed code=" + e.statusCode)
        }
    }

}
