package com.example.accessories_app.requestAndResponse

import com.example.accessories_app.domain.Product

class ProductResponse( var Products: ArrayList<Product>,
                       var IsSuccess:Boolean,
                       var Message : String,)