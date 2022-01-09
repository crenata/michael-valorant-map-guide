package com.scranaver.valorantmapguide.ui.forgot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.scranaver.valorantmapguide.R
import com.scranaver.valorantmapguide.databinding.ActivityForgotBinding

class ForgotActivity : AppCompatActivity() {
    private var auth: FirebaseAuth = Firebase.auth

    private lateinit var binding: ActivityForgotBinding

    private lateinit var loading: ProgressBar
    private lateinit var reset: Button
    private lateinit var emailEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityForgotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Reset Password"
        }

        loading = binding.loading
        reset = binding.reset
        emailEditText = binding.email

        reset.setOnClickListener {
            reset()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun reset() {
        val email: String = emailEditText.text.toString()
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

        loading.visibility = View.VISIBLE
        reset.isEnabled = false

        auth.sendPasswordResetEmail(email).addOnSuccessListener {
            loading.visibility = View.GONE
            reset.isEnabled = true
            Toast.makeText(applicationContext, R.string.check_email_reset, Toast.LENGTH_LONG).show()
            finish()
        }.addOnFailureListener { error ->
            loading.visibility = View.GONE
            reset.isEnabled = true
            Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
        }
    }
}