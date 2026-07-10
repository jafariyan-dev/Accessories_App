package com.example.accessories_app.domain
import java.io.Serializable


data class Product(
     var Id : Int = 0,
     var Title: String ,
     var Description: String ,
     var PhotoUrl: String ,
     var VideoUrl : String,
     var Price: Int ,
     var numberInCart: Int = 0,
     var CategoryId : Int
) : Serializable