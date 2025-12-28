package com.example.accessories_app.RequestAndResponse

import com.example.accessories_app.Domain.Product
import com.google.gson.annotations.Expose

class ProductResponse( var Products: ArrayList<Product>,
                       var IsSuccess:Boolean,
                       var Message : String,)