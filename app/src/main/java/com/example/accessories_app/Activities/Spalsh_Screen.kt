package com.example.accessories_app.Activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.accessories_app.databinding.ActivitySpalshScreenBinding

class Spalsh_Screen : AppCompatActivity() {
     lateinit var binding:ActivitySpalshScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySpalshScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            Handler(Looper.getMainLooper()).postDelayed({
                var p=Intent(this@Spalsh_Screen,Login::class.java)
                startActivity(p)
            },4000)
        }
    }
}