package com.example.tiktok

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import com.example.tiktok.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            logIn()
        }

        fun setInProgress(inProgress: Boolean) {
            if (inProgress) {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnLogin.visibility = View.GONE
                binding.goToSignup.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.visibility = View.VISIBLE
                binding.goToSignup.visibility = View.VISIBLE
            }
        }
    }

    fun logIn() {
        val email = binding.edtEmail.text.toString()
        val password = binding.password.text.toString()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edtEmail.error = "Email not valid"
            return
        }
        if (password.length < 7) {
            binding.password.error = "Minimum 7 Characters"
            return
        }

        binding.goToSignup.setOnClickListener {
            startActivity(
                Intent(
                    this, SignupActivity::class.java
                )
            )
        }


    }
}
