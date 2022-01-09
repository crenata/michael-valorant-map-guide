package com.scranaver.valorantmapguide.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.scranaver.valorantmapguide.MainActivity
import com.scranaver.valorantmapguide.R
import com.scranaver.valorantmapguide.databinding.ActivityLoginBinding
import com.scranaver.valorantmapguide.ui.forgot.ForgotActivity
import com.scranaver.valorantmapguide.ui.register.RegisterActivity

class LoginActivity: AppCompatActivity() {
    private var auth: FirebaseAuth = Firebase.auth

    private lateinit var binding: ActivityLoginBinding

    private lateinit var loading: ProgressBar
    private lateinit var registerButton: TextView
    private lateinit var forgotButton: TextView
    private lateinit var loginButton: Button

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loading = binding.loading
        registerButton = binding.register
        forgotButton = binding.forgot
        loginButton = binding.login
        emailEditText = binding.email
        passwordEditText = binding.password

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
        forgotButton.setOnClickListener {
            startActivity(Intent(this, ForgotActivity::class.java))
        }
        loginButton.setOnClickListener {
            login()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            if (currentUser.isEmailVerified) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun login() {
        val email: String = emailEditText.text.toString()
        val password: String = passwordEditText.text.toString()

        if (email.isBlank()) {
            emailEditText.error = "Email is required!"
            emailEditText.requestFocus()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Email is invalid!"
            emailEditText.requestFocus()
            return
        }
        if (password.isBlank()) {
            passwordEditText.error = "Password is required!"
            passwordEditText.requestFocus()
            return
        }
        if (password.length < 6) {
            passwordEditText.error = "Min 6 characters!"
            passwordEditText.requestFocus()
            return
        }

        loading.visibility = View.VISIBLE
        loginButton.isEnabled = false

        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            val user = auth.currentUser
            if (user!!.isEmailVerified) {
                loading.visibility = View.GONE
                loginButton.isEnabled = true
                Toast.makeText(applicationContext, R.string.welcome, Toast.LENGTH_LONG).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                user.sendEmailVerification().addOnSuccessListener {
                    loading.visibility = View.GONE
                    loginButton.isEnabled = true
                    Toast.makeText(applicationContext, R.string.check_email_verify, Toast.LENGTH_LONG).show()
                }.addOnFailureListener { error ->
                    loading.visibility = View.GONE
                    loginButton.isEnabled = true
                    Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener { error ->
            loading.visibility = View.GONE
            loginButton.isEnabled = true
            Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
        }
    }
}