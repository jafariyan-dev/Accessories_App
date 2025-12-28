package com.example.accessories_app.RequestAndResponse

import com.example.accessories_app.Domain.Product
import com.google.gson.annotations.Expose

class ProductRequest(val Title: String ,
                     val Description: String ,
                     val PhotoUrl: String ,
                     val VideoUrl : String,
                     val Price: Int ,
                     val CategoryId : Int)