package com.scranaver.valorantmapguide.ui.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.scranaver.valorantmapguide.R
import com.scranaver.valorantmapguide.data.Constants
import com.scranaver.valorantmapguide.databinding.ActivityRegisterBinding
import com.scranaver.valorantmapguide.ui.login.LoginActivity

class RegisterActivity: AppCompatActivity() {
    private var userDocRef: CollectionReference = FirebaseFirestore.getInstance().collection(Constants.userCollectionKey())
    private var auth: FirebaseAuth = Firebase.auth

    private lateinit var binding: ActivityRegisterBinding

    private lateinit var loading: ProgressBar
    private lateinit var registerButton: Button
    private lateinit var loginButton: TextView

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = ""
        }

        loading = binding.loading
        registerButton = binding.register
        loginButton = binding.login
        emailEditText = binding.email
        passwordEditText = binding.password
        confirmPasswordEditText = binding.confirmPassword

        registerButton.setOnClickListener {
            register()
        }
        loginButton.setOnClickListener {
            goToLogin()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        goToLogin()
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun register() {
        val email: String = emailEditText.text.toString()
        val password: String = passwordEditText.text.toString()
        val confirmPassword: String = confirmPasswordEditText.text.toString()

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
        if (confirmPassword.isBlank()) {
            confirmPasswordEditText.error = "Confirm Password is required!"
            confirmPasswordEditText.requestFocus()
            return
        }
        if (confirmPassword.length < 6) {
            confirmPasswordEditText.error = "Min 6 characters!"
            confirmPasswordEditText.requestFocus()
            return
        }
        if (password != confirmPassword) {
            confirmPasswordEditText.error = "Password does not match!"
            confirmPasswordEditText.requestFocus()
            return
        }

        loading.visibility = View.VISIBLE
        registerButton.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
            val newUser = hashMapOf(
                "name" to email,
                "email" to email,
                "image" to ""
            )
            val user = auth.currentUser
            if (user != null) {
                userDocRef.document(user.uid).set(newUser).addOnSuccessListener {
                    loading.visibility = View.GONE
                    registerButton.isEnabled = true
                    Toast.makeText(applicationContext, R.string.success, Toast.LENGTH_LONG).show()
                    goToLogin()
                }.addOnFailureListener { error ->
                    loading.visibility = View.GONE
                    registerButton.isEnabled = true
                    Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                }
            } else {
                loading.visibility = View.GONE
                registerButton.isEnabled = true
                Toast.makeText(applicationContext, R.string.error, Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { error ->
            loading.visibility = View.GONE
            registerButton.isEnabled = true
            Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
        }
    }
}