package com.example.accessories_app.RequestAndResponse

import com.example.accessories_app.Domain.CategoryModel
import com.google.gson.annotations.Expose

class CategoryResponse (var IsSuccess:Boolean, var Message : String, var Categories : ArrayList<CategoryModel>
)