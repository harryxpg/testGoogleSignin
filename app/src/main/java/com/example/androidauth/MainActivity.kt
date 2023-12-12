package com.example.androidauth

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import com.google.gson.annotations.SerializedName
import com.linecorp.linesdk.LineApiResponseCode
import com.linecorp.linesdk.auth.LineAuthenticationParams
import com.linecorp.linesdk.auth.LineLoginApi
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ClientSecretBasic
import net.openid.appauth.CodeVerifierUtil
import net.openid.appauth.ResponseTypeValues
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private lateinit var facebookCallbackManager: CallbackManager  // 在這裡初始化 CallbackManager
    private lateinit var lineLoginLauncher:ActivityResultLauncher<Intent>
    private lateinit var yahooLoginLauncher: ActivityResultLauncher<Intent>

    private val EMAIL = "email"

    data class VerificationResponse(val isAuth: Boolean, val token: String)
//    data class VerificationResponse(val IsAuth: String, val OauthType: String ,val DisplayName: String, val Email: String )
    data class GoogleResponse(
        @SerializedName("access_token") val accessToken: String,
        @SerializedName("expires_in") val expiresIn: Int,
        @SerializedName("refresh_token") val refreshToken: String,
        @SerializedName("scope") val scope: String,
        @SerializedName("token_type") val tokenType: String,
        @SerializedName("id_token") val idToken: String
    )
    interface OAuthApiService {
        @GET("v1/oAuth/app/callback")
        fun authenticate(
            @Query("access_token") accessToken: String,
            @Query("auth_type") authType: String
        ): Call<VerificationResponse>
    }

    interface GoogleOAuthApiService {
        @FormUrlEncoded
        @POST("oauth2/v4/token")
        fun getToken(
            @Field("grant_type") grantType: String,
            @Field("client_id") clientId: String,
            @Field("client_secret") clientSecret: String,
            @Field("redirect_uri") redirectUri: String,
            @Field("code") code: String
        ): Call<GoogleResponse>
    }

    //yahoo定義
    private val RC_AUTH = 1001
    private val authenticationEndPoint = "https://api.login.yahoo.com/oauth2/request_auth?lang=en-US"
    private val tokenEndPoint = "https://api.login.yahoo.com/oauth2/get_token"
//    private val redirectUrl = "https://819d-211-23-35-187.ngrok-free.app/v1/oAuth/yahoo/callback" // 替換為您的 redirectUrl (例如: com.yahoo.ydn://callback)
    private val redirectUrl = "myapp://redirecturipath" // 替換為您的 redirectUrl (例如: com.yahoo.ydn://callback)
    private val clientSecret = "e9c19b1b79b987969fa44abc1e437c2c2456b0a1" // 替換為您從開發者控制台獲得的客戶端密鑰
    private val clientId = "dj0yJmk9eEpuM2ZiZjFHaWxxJmQ9WVdrOU5qTmhZVUpKT1VVbWNHbzlNQT09JnM9Y29uc3VtZXJzZWNyZXQmc3Y9MCZ4PTAx" // 替換為您從開發者控制台獲得的客戶端 ID
    private val SHARED_PREFERENCES_NAME = "AuthStatePreference"
    private val AUTH_STATE = "AUTH_STATE" // 偏好設置名稱鍵值

    interface YahooApiService {
        @GET("openid/v1/userinfo")
        fun getUserInfo(@Header("Authorization") authorization: String): Call<ResponseBody>
    }
    private lateinit var yahooApiService: YahooApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initFacebook()

        initGoogleSignIn()

        initLineSignIn()

        initYahooSignIn()

    }

    private  fun initYahooSignIn() {

        // 初始化 Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.login.yahoo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        yahooApiService = retrofit.create(YahooApiService::class.java)

        // 初始化 ActivityResultLauncher
        yahooLoginLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "yahoo Activity.RESULT_OK")
                handleAuthorizationResponse(result.data)
            } else {
                Log.d(TAG, "yahoo Failed to authenticate ....")
            }
        }

        val customLineLoginButton: Button = findViewById(R.id.button_custom_yahoo_login)
        customLineLoginButton.setOnClickListener {
            // 啟動 yahoo 登入流程
            this.doAuthorization()
        }
    }

    private fun doAuthorization() {
        val authService = AuthorizationService(this) // 活動上下文傳遞至此
        val authIntent = authService.getAuthorizationRequestIntent(getAuthRequest())
        yahooLoginLauncher.launch(authIntent)
    }

    private fun getAuthRequest(): AuthorizationRequest {
        val tokenEndPointUri = Uri.parse(tokenEndPoint)
        val authenticationEndPointUri = Uri.parse(authenticationEndPoint)
        val configuration = AuthorizationServiceConfiguration(authenticationEndPointUri, tokenEndPointUri)

        return AuthorizationRequest.Builder(
            configuration,
            clientId,
            ResponseTypeValues.CODE,
            Uri.parse(redirectUrl)
        ).apply {
            setLoginHint("sheng@xpg.tech")
            setCodeVerifier(CodeVerifierUtil.generateRandomCodeVerifier()) // 添加用於 PKCE 的隨機代碼驗證器
        }.build()
    }

    private fun persistAuthState(authState: AuthState) {
        val sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString(AUTH_STATE, authState.jsonSerializeString())
            apply() // 或者使用 commit() 來立即提交變更
        }
    }

    private fun restoreAuthState(): AuthState? {
        val jsonString = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getString(AUTH_STATE, null)
        return jsonString?.takeIf { it.isNotEmpty() }?.let {
            try {
                AuthState.jsonDeserialize(it)
            } catch (e: JSONException) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun handleAuthorizationResponse(intent: Intent?) {
        intent?.let {
            val response = AuthorizationResponse.fromIntent(it)
            val error = AuthorizationException.fromIntent(it)
            val authState = AuthState(response, error)

            Log.d(TAG, "response ${response}")

            response?.let { res ->
                val service = AuthorizationService(this)
                val request = res.createTokenExchangeRequest()

                service.performTokenRequest(request) { tokenResponse, exception ->
                    tokenResponse?.let {
                        authState.update(tokenResponse, exception)
                        persistAuthState(authState)

                        // 獲取用戶信息
                        getUserInfo()
                    }
                }
            }
        }
    }

    private fun getUserInfo() {
        val authState: AuthState? = restoreAuthState()
        val authorizationService = AuthorizationService(this)

        authState?.performActionWithFreshTokens(authorizationService) { accessToken, _, _ ->
            accessToken?.let {

                Log.d(TAG, "getUserInfo accessToken ${accessToken}")
                retrofitRequest( accessToken.toString() ,"Yahoo" )

                yahooApiService.getUserInfo("Bearer $accessToken").enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        val userInfoString = response.body()?.string()
                        userInfoString?.let {
                            val userJSONObject = JSONObject(it)
                            setUserProfile(userJSONObject)
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        runOnUiThread {
                            Log.d(TAG, "getUserInfo onFailure")
                        }
                    }
                })
            }
        }
    }

    private fun setUserProfile(userJSONObject: JSONObject) {
//        runOnUiThread {
//            fullName.text = userJSONObject.optString("name", null)
//            givenName.text = userJSONObject.optString("given_name", null)
//            familyName.text = userJSONObject.optString("family_name", null)
//            val imageUrl = userJSONObject.optString("picture", null)
//            Glide.with(this@MainActivity).load(imageUrl).into(profileImage)
//        }
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

                    loginResult.lineIdToken?.let {
                        retrofitRequest( it.rawString,"Line" )
                    }

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

                retrofitRequest( result.accessToken.token.toString() ,"Facebook" )

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
//            this.retrofitRequest( account.serverAuthCode.toString() ,"Google" )
//            this.retrofitRequest( account.idToken.toString() ,"Google" )

            val retrofit = Retrofit.Builder()
                .baseUrl("https://www.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(GoogleOAuthApiService::class.java)
            service.getToken(
                "authorization_code",
                getString(R.string.your_web_client_id),
                getString(R.string.your_web_client_secret),
                getString(R.string.redirect_uri),
                account.serverAuthCode.toString()
            ).enqueue(object : Callback<GoogleResponse> {
                override fun onResponse(call: Call<GoogleResponse>, response: Response<GoogleResponse>) {
                    if (response.isSuccessful) {
                        // 處理成功的響應
                        val responseAccessToken = response.body()?.accessToken
                        Log.d(TAG, " ${responseAccessToken}")

                        retrofitRequest( responseAccessToken.toString() ,"Google" )

                    } else {
                        // 處理錯誤響應
                        Log.e(TAG, "Response not successful")
                    }
                }

                override fun onFailure(call: Call<GoogleResponse>, t: Throwable) {
                    // 處理失敗情況
                    Log.e(TAG, "Failed to get token", t)
                }
            })


        } catch (e: ApiException) {
            // 代表沒成功
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("TAG", "signInResult:failed code=" + e.statusCode)
        }
    }

    private fun retrofitRequest (accessToken: String,anthType:String ) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.17.57:8180/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val oAuthApiService = retrofit.create(OAuthApiService::class.java)

        oAuthApiService.authenticate(accessToken, anthType).enqueue(object : Callback<VerificationResponse> {
            override fun onResponse(call: Call<VerificationResponse>, response: Response<VerificationResponse>) {
                if (response.isSuccessful) {
                    // 處理成功響應
                    val oauthResponse = response.body()
                    oauthResponse?.let {
//                        val oauthId = it.OauthId
//                        val oauthType = it.OauthType
//                        val displayName = it.DisplayName
//                        val email =it.Email
                        val isAuth = it.isAuth
                        val responseToken =it.token
                    }

                    Log.d(TAG,"response isSuccessful call ${call}")
                    Log.d(TAG,"response isSuccessful response ${response}")
                    Log.d(TAG,"response isSuccessful oauthResponse ${oauthResponse}")
                } else {
                    // 處理錯誤響應
                    Log.d(TAG,"response isFail call ${call}")
                    Log.d(TAG,"response isFail response ${response}")
                }
            }

            override fun onFailure(call: Call<VerificationResponse>, t: Throwable) {
                // 處理網絡錯誤或請求失敗
                Log.d(TAG,"response onFailure call ${call}")
                Log.d(TAG,"response onFailure t ${t}")
            }
        })
    }


}
