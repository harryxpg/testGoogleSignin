package com.example.android_auth
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task

class ShowInfo : AppCompatActivity() {

    private lateinit var logoutBtn: Button
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_showinfo)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        val mButton: Button = findViewById(R.id.button)
        mButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        logoutBtn = findViewById(R.id.logoutBtn)
        logoutBtn.setOnClickListener {
            signOut()
        }

        var result: TextView?

        intent?.let {
            result = findViewById(R.id.displayname)
            result?.text = "使用者displayname : ${it.getStringExtra("displayname")}"

            result = findViewById(R.id.givenname)
            result?.text = "使用者givenname : ${it.getStringExtra("givenname")}"

            result = findViewById(R.id.familyname)
            result?.text = "使用者familyname : ${it.getStringExtra("familyname")}"

            result = findViewById(R.id.id)
            result?.text = "使用者id : ${it.getStringExtra("id")}"

            result = findViewById(R.id.idtoken)
            result?.text = "使用者idtoken : ${it.getStringExtra("idtoken")}"

            result = findViewById(R.id.email)
            result?.text = "使用者email : ${it.getStringExtra("email")}"

            result = findViewById(R.id.photourl)
            result?.text = "圖片連結 : ${it.getStringExtra("photourl")}"
        }
    }

    private fun signOut() {
        mGoogleSignInClient.signOut()
            .addOnCompleteListener(this, OnCompleteListener {
                val intent = Intent(this@ShowInfo, MainActivity::class.java)
                startActivity(intent)
            })
        Toast.makeText(this, "登出成功!!", Toast.LENGTH_SHORT).show()
    }
}