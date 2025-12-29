package com.example.accessories_app.Activities

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.accessories_app.databinding.ActivityContactUsBinding

class Contact_Us : AppCompatActivity() {
    lateinit var binding: ActivityContactUsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityContactUsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // مدیریت کیبورد و فوکوس
        setupKeyboardNavigation()

        binding.btnSend.setOnClickListener {
            if (validateForm()) {
                Toast.makeText(
                    this,
                    "Your message has been successfully sent. Thank you! ✨",
                    Toast.LENGTH_LONG
                ).show()
                // اگر می‌خواهید بعد از ارسال به صفحه اصلی برگردید:
                startActivity(Intent(this, MainActivity::class.java))
                finish()



            }
        }
    }

    private fun setupKeyboardNavigation() {
        binding.messageEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.btnSend.performClick()
                true
            } else false
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        val name = binding.nameEditText.text.toString().trim()
        val phone = binding.phoneEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val message = binding.messageEditText.text.toString().trim()

        // Name
        if (name.isEmpty()) {
            binding.nameLayout.error = "Please enter your full name"
            isValid = false
        } else {
            binding.nameLayout.error = null
        }

        // Phone
        if (!phone.matches("^09\\d{9}$".toRegex())) {
            binding.phoneLayout.error = "Please enter a valid mobile number (e.g., 09123456789)"
            isValid = false
        } else {
            binding.phoneLayout.error = null
        }

        // Email
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex())) {
            binding.emailLayout.error = "Please enter a valid email address"
            isValid = false
        } else {
            binding.emailLayout.error = null
        }

        // Message
        if (message.isEmpty()) {
            binding.messageLayout.error = "Please write your message"
            isValid = false
        } else {
            binding.messageLayout.error = null
        }

        return isValid
    }
}