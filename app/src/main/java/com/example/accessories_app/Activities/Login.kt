package com.example.accessories_app.Activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.accessories_app.Api
import com.example.accessories_app.RequestAndResponse.LoginRequest
import com.example.accessories_app.RequestAndResponse.LoginResponse
import com.example.accessories_app.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Login : AppCompatActivity() {
    lateinit var binding : ActivityLoginBinding
    lateinit var shared : SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        shared = getSharedPreferences("userStorage", MODE_PRIVATE)
        val edit=shared.edit()
        setContentView(binding.root)
        binding.apply {
            loginSharedPrefernce()
            btnLogin.setOnClickListener {
                var Username = username.text.toString()
               val loginRequest = LoginRequest(username.text.toString(),password.text.toString())
                Api().instance().loginCall(loginRequest)
                    .enqueue(object : Callback<LoginResponse> {
                        override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                            if (response.isSuccessful) {
                                val loginResponse = response.body()
                                if (loginResponse != null) {
                                    if (loginResponse.IsSuccess) {
                                        edit.putString("username", Username)
                                        edit.putBoolean("isAdmin", loginResponse.IsAdmin)
                                        edit.apply()
                                        if (loginResponse.IsAdmin) {
                                            startActivity(Intent(this@Login, AdminPanel::class.java))
                                        } else
                                            startActivity(Intent(this@Login, MainActivity::class.java))

                                        Toast.makeText(this@Login, "ورود با موفقیت انجام شد", Toast.LENGTH_LONG).show()
                                    } else {
                                        val errorMessage = loginResponse.Message
                                        Toast.makeText(this@Login, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                                        Log.e("LOGIN_FAIL", "API Error: $errorMessage")
                                    }
                                } else {
                                    Toast.makeText(this@Login, "Error: Received an empty response from server.", Toast.LENGTH_LONG).show()
                                    Log.e("LOGIN_FAIL", "Response body is null even with a successful response code.")
                                }
                            }
                            else {
                                val errorBody = response.errorBody()?.string()
                                Toast.makeText(this@Login, "Server Error: ${response.code()}", Toast.LENGTH_LONG).show()
                                Log.e("LOGIN_FAIL", "HTTP Error Code: ${response.code()}. Body: $errorBody")
                            }
                        }
                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                            Toast.makeText(this@Login, t.message, Toast.LENGTH_LONG).show()
                        }
                    })
            }
            SignupBtn.setOnClickListener {
                startActivity(Intent(this@Login, Signup::class.java))
            }
        }
    }
    fun loginSharedPrefernce(){
        val userStr= shared.getString("username","")!!
        val isAdmin=shared.getBoolean("isAdmin",false)

        if(userStr.isNotBlank() && isAdmin) {
            startActivity(Intent(this@Login, AdminPanel::class.java))
        }
        else if(userStr.isNotBlank() && !isAdmin)
        {
            startActivity(Intent(this@Login, MainActivity::class.java))
        }
    }
}