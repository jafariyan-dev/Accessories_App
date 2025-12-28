package com.example.accessories_app.RequestAndResponse

class EditProductRequest(val Id:Int,
                         val Title: String ,
                         val Description: String ,
                         val PhotoUrl: String ,
                         val VideoUrl : String,
                         val Price: Int ,
                         val CategoryId : Int)