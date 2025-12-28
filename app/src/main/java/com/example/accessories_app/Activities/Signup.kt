package com.example.accessories_app.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.accessories_app.Api
import com.example.accessories_app.RequestAndResponse.DBResponse
import com.example.accessories_app.Domain.User
import com.example.accessories_app.RequestAndResponse.UserRequest
import com.example.accessories_app.databinding.ActivitySignupBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Signup : AppCompatActivity() {
    lateinit var binding: ActivitySignupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySignupBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        var IsAdmin: Boolean
        setContentView(binding.root)
        binding.apply {
            IsAdmin=false
            btnSignup.setOnClickListener {
                var Username = username
                var Password = password
                if(radioAdmin.isChecked) IsAdmin = true

                val request = UserRequest( User(Username.text.toString(),Password.text.toString(), IsAdmin))
                Api().instance().regCall(request)
                    .enqueue(object : Callback<DBResponse> {
                        override fun onResponse(
                            call: Call<DBResponse>,
                            response: Response<DBResponse>
                        ) {
                            if (response.isSuccessful) {
                                val signupResponse = response.body()

                                if (signupResponse != null) {
                                    if (signupResponse.IsSuccess) {
                                        Toast.makeText(this@Signup, "ثبت نام با موفقیت انجام شد", Toast.LENGTH_LONG).show()
                                    } else {
                                        val errorMessage = signupResponse.Message ?: "An unknown error occurred."
                                        Toast.makeText(this@Signup, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                                        Log.e("SIGNUP_FAIL", "API Error: $errorMessage")
                                    }
                                } else {
                                    Toast.makeText(this@Signup, "Error: Received an empty response from server.", Toast.LENGTH_LONG).show()
                                    Log.e("SIGNUP_FAIL", "Response body is null even with a successful response code.")
                                }
                            } else {
                                val errorBody = response.errorBody()?.string()
                                Toast.makeText(this@Signup, "Server Error: ${response.code()}", Toast.LENGTH_LONG).show()
                                Log.e("SIGNUP_FAIL", "HTTP Error Code: ${response.code()}. Body: $errorBody")
                            }
                        }

                        override fun onFailure(call: Call<DBResponse>, t: Throwable) {
                            Toast.makeText(this@Signup, t.message, Toast.LENGTH_LONG).show()
                        }
                    })
            }
            btnLogin.setOnClickListener {
                startActivity(Intent(this@Signup,Login::class.java))
            }
        }
    }
}