package com.example.accessories_app

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Api {
        var BASE_URL: String = "http://10.178.79.48/php/"

        fun instance():ApiInterface{
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(ApiInterface::class.java)
        }
}