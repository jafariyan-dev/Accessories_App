package com.example.accessories_app.Activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.accessories_app.Adaptors.CategoryAdapter
import com.example.accessories_app.databinding.ActivityMainBinding
import com.example.accessories_app.Adaptors.PopularAdaptors
import com.example.accessories_app.Api
import com.example.accessories_app.RequestAndResponse.CategoryResponse
import com.example.accessories_app.RequestAndResponse.ProductResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadCategory()
        loadProduct()
        initBottomMenu()
    }//end
    private fun loadProduct(){

        Api().instance().getItems()
            .enqueue(object : Callback<ProductResponse> {
                override fun onResponse(
                    call: Call<ProductResponse>,
                    response: Response<ProductResponse>
                ) {
                    if (response.isSuccessful) {
                        var productResponse = response.body()
                        if (productResponse != null) {
                            if (productResponse.IsSuccess) {
                                val productList = productResponse.Products
                                if (productList.isNotEmpty()) {
                                    binding.apply {
                                        PopularView.layoutManager = LinearLayoutManager(
                                            this@MainActivity,
                                            LinearLayoutManager.HORIZONTAL,
                                            false
                                        )
                                        PopularView.adapter = PopularAdaptors(productList)
                                        progressBarPopular.visibility = View.GONE
                                    }
                                }
                            }
                            else {
                                val errorMessage = productResponse.Message
                                Toast.makeText(
                                    this@MainActivity,
                                    "Error: $errorMessage",
                                    Toast.LENGTH_LONG
                                ).show()
                                Log.e("GETPRODUCT_FAIL", "API Error: $errorMessage")
                            }
                        }
                        else {
                            Toast.makeText(
                                this@MainActivity,
                                "Error: Received an empty response from server.",
                                Toast.LENGTH_LONG
                            ).show()
                            Log.e(
                                "GETPRODUCT_FAIL",
                                "Response body is null even with a successful response code."
                            )
                        }
                    }
                    else {
                        val errorBody = response.errorBody()?.string()
                        Toast.makeText(this@MainActivity, "Server Error: ${response.code()}", Toast.LENGTH_LONG).show()
                        Log.e("GETPRODUCT_FAIL", "HTTP Error Code: ${response.code()}. Body: $errorBody")
                    }
                }
                override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_LONG).show()
                }
            })
    }
    private fun initBottomMenu() {
        binding.CartBtn.setOnClickListener {
            startActivity(Intent(this,CartActivity::class.java))
        }
        binding.AboutUs.setOnClickListener{
            startActivity(Intent(this@MainActivity, About_Us::class.java))
        }
        binding.ContactUs.setOnClickListener{
            startActivity(Intent(this@MainActivity, Contact_Us::class.java))
        }
    }
    private fun loadCategory(){
        binding.apply {
            Api().instance().getCategory()
                .enqueue(object : Callback<CategoryResponse>{
                    override fun onResponse(
                        call: Call<CategoryResponse>,
                        response: Response<CategoryResponse>
                    ) {
                        if (response.isSuccessful) {
                            var categoryResponse = response.body()
                            if (categoryResponse != null) {
                                if (categoryResponse.IsSuccess) {
                                    val categoryList = categoryResponse.Categories
                                    if (categoryList.isNotEmpty()) {
                                        binding.apply {
                                            categoryView.layoutManager = LinearLayoutManager(
                                                this@MainActivity,
                                                LinearLayoutManager.HORIZONTAL,
                                                false
                                            )
                                            categoryView.adapter =
                                                CategoryAdapter(items = categoryList)
                                        }
                                    }
                                }
                                else {
                                    val errorMessage = categoryResponse.Message
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Error: $errorMessage",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    Log.e("GETCATEGORY_FAIL", "API Error: $errorMessage")
                                }
                            }
                            else {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Error: Received an empty response from server.",
                                    Toast.LENGTH_LONG
                                ).show()
                                Log.e(
                                    "GETCATEGORY_FAIL",
                                    "Response body is null even with a successful response code."
                                )
                            }
                        }
                        else {
                            val errorBody = response.errorBody()?.string()
                            Toast.makeText(this@MainActivity, "Server Error: ${response.code()}", Toast.LENGTH_LONG).show()
                            Log.e("GETCATEGORY_FAIL", "HTTP Error Code: ${response.code()}. Body: $errorBody")
                        }

                    }

                    override fun onFailure(call: Call<CategoryResponse>, t: Throwable) {
                        Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_LONG).show()
                    }
                })
        }
    }
}