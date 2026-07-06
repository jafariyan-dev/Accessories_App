package com.example.accessories_app

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Api {
        var BASE_URL: String = "http://192.168.1.104/php/"

        fun instance():ApiInterface{
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(ApiInterface::class.java)
        }
}