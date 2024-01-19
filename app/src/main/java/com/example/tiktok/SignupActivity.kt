package com.example.tiktok

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.example.tiktok.databinding.ActivitySignupBinding
import com.example.tiktok.model.UserModel
import com.example.tiktok.util.Uiutil
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignin.setOnClickListener {
            signUp()
        }
    }

    fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnSignin.visibility = View.GONE
            binding.goToLogin.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.btnSignin.visibility = View.VISIBLE
            binding.goToLogin.visibility = View.VISIBLE
        }
    }

    private fun signUp() {
        val email = binding.edtEmail.text.toString()
        val password = binding.password.text.toString()
        val confirmPassword = binding.confirmPassword.text.toString()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edtEmail.error = "Email not valid"
            return
        }
        if (password.length < 7) {
            binding.password.error = "Minimum 7 Characters"
            return
        }
        if (password != confirmPassword) {

            binding.confirmPassword.error = "Password doesn't matches"
            return
        }
        signupWithFirebase(email, password)
    }

    private fun signupWithFirebase(email: String, password: String) {
        setInProgress(true)
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                it.user?.let { user ->
                    val userModel = UserModel(user.uid, email, email.substringBefore("@"))
                    Firebase.firestore.collection("users")
                        .document(user.uid)
                        .set(userModel).addOnSuccessListener {
                            Uiutil.showToast(this, "Account Created Successfully")
                            setInProgress(false)
                            startActivity(Intent(this, MainActivity::class.java))
                        }
                }
            }
            .addOnFailureListener {
                Uiutil.showToast(this, it.localizedMessage ?: "Something went wrong")
                setInProgress(false)
            }
    }
}