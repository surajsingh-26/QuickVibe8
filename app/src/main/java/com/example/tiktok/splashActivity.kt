package com.example.tiktok

import android.os.Bundle
import android.os.Handler
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class splashActivity : AppCompatActivity() {
    private val splashTimeout = 3000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            if (Firebase.auth.currentUser != null) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, SignupActivity::class.java)
                startActivity(intent)
            }
            finish()
        }, splashTimeout.toLong())

    }
}