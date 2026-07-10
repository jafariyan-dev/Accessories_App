package com.example.accessories_app

import com.example.accessories_app.requestAndResponse.CategoryResponse
import com.example.accessories_app.requestAndResponse.DBResponse
import com.example.accessories_app.requestAndResponse.EditProductRequest
import com.example.accessories_app.requestAndResponse.LoginRequest
import com.example.accessories_app.requestAndResponse.LoginResponse
import com.example.accessories_app.requestAndResponse.PhotoUploadResponse
import com.example.accessories_app.requestAndResponse.ProductRequest
import com.example.accessories_app.requestAndResponse.ProductResponse
import com.example.accessories_app.requestAndResponse.ProductWithCategoryRequest
import com.example.accessories_app.requestAndResponse.UserRequest
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
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