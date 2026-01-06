package com.example.accessories_app

import com.example.accessories_app.Domain.Product
import com.example.accessories_app.Domain.User
import com.example.accessories_app.RequestAndResponse.CategoryResponse
import com.example.accessories_app.RequestAndResponse.DBResponse
import com.example.accessories_app.RequestAndResponse.EditProductRequest
import com.example.accessories_app.RequestAndResponse.LoginRequest
import com.example.accessories_app.RequestAndResponse.LoginResponse
import com.example.accessories_app.RequestAndResponse.PhotoUploadResponse
import com.example.accessories_app.RequestAndResponse.ProductRequest
import com.example.accessories_app.RequestAndResponse.ProductResponse
import com.example.accessories_app.RequestAndResponse.ProductWithCategoryRequest
import com.example.accessories_app.RequestAndResponse.UserRequest
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

interface ApiInterface {
    @Headers("Content-Type: application/json")
        @POST("login.php")
        fun loginCall(@Body request: LoginRequest)
        : Call<LoginResponse>

    @Headers("Content-Type: application/json")
        @POST("register.php")
        fun regCall(@Body user:UserRequest)
        : Call<DBResponse>

    @Headers("Content-Type: application/json")
        @GET("getItems.php")
        fun getItems()
        : Call<ProductResponse>

    @Headers("Content-Type: application/json")
    @GET("getCategory.php")
    fun getCategory()
            : Call<CategoryResponse>

    @Headers("Content-Type: application/json")
    @POST("getItemCategory.php")
    fun getItemCategory(@Body request : ProductWithCategoryRequest)
            : Call<ProductResponse>

    @Multipart
    @POST("uploadPhoto.php")
    fun uploadPhoto(@Part image: MultipartBody.Part
    ): Call<PhotoUploadResponse>


    @Headers("Content-Type: application/json")
        @POST("postItems.php")
        fun addProduct(@Body request : ProductRequest)
        : Call<DBResponse>

    @Headers("Content-Type: application/json")
        @POST("editProduct.php")
        fun editProduct(@Body request: EditProductRequest)
        : Call<DBResponse>

    @FormUrlEncoded
    @POST("deleteProduct.php")
    fun deleteProduct(@Field("Id") productId: Int
    ): Call<DBResponse>
}