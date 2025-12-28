package com.example.accessories_app.Activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.size
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.accessories_app.Adaptors.ProductAdapter
import com.example.accessories_app.Api
import com.example.accessories_app.Domain.CategoryModel
import com.example.accessories_app.Domain.Product
import com.example.accessories_app.RequestAndResponse.CategoryResponse
import com.example.accessories_app.RequestAndResponse.ProductResponse
import com.example.accessories_app.databinding.ActivityAdminPanelBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.addAll

class AdminPanel : AppCompatActivity() {
    private lateinit var binding: ActivityAdminPanelBinding
    private lateinit var adapter: ProductAdapter
    private var productList = ArrayList<Product>()
    private lateinit var imagePicker: ActivityResultLauncher<String>
    private var imageCallback: ((Uri) -> Unit)? = null
    private var categoryList = ArrayList<CategoryModel>()

    // لانچر برای افزودن محصول جدید

    private val addProductLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "محصول جدید با موفقیت اضافه شد. در حال بارگذاری مجدد لیست...", Toast.LENGTH_SHORT).show()
            fetchProductsFromServer()
        }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initImagePicker()
        setupRecyclerView()
        fetchProductsFromServer()
        fetchCategoriesFromServer()

        binding.recyclerProducts.layoutManager = LinearLayoutManager(this)
        binding.recyclerProducts.adapter = adapter
        binding.showAllBtn.setOnClickListener {
            startActivity(Intent(this@AdminPanel, MainActivity::class.java))
        }
        // دکمه افزودن محصول جدید
        binding.btnAddProduct.setOnClickListener {
            val intent = Intent(this, AddProduct::class.java)
            addProductLauncher.launch(intent)
        }
    }
    private fun fetchProductsFromServer() {
        Api().instance().getItems().enqueue(object : Callback<ProductResponse> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ProductResponse>, response: Response<ProductResponse>) {
                if (response.isSuccessful) {
                    val productResponse = response.body()
                    if (productResponse != null && productResponse.IsSuccess) {
                        productList.clear()
                        productList.addAll(productResponse.Products)
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@AdminPanel, "خطا در سرور: ${productResponse?.Message}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@AdminPanel, "خطای شبکه: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                Log.e("AdminPanel", "Failed to fetch products", t)
                Toast.makeText(this@AdminPanel, "شکست در ارتباط با سرور: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setupRecyclerView() {
        // آداپتور را با یک لیست خالی اولیه می‌سازیم
        adapter = ProductAdapter(
            productList,
            categoryList
        ) { callback -> // categoryList را به constructor اضافه کنید
            imageCallback = callback
            imagePicker.launch("image/*")
            binding.recyclerProducts.layoutManager = LinearLayoutManager(this)
            binding.recyclerProducts.adapter = adapter
        }
    }

    private fun initImagePicker() {
        imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { selectedUri ->
                imageCallback?.invoke(selectedUri)
            }
        }
    }

    private fun fetchCategoriesFromServer() {
        Api().instance().getCategory().enqueue(object : Callback<CategoryResponse> {
            override fun onResponse(call: Call<CategoryResponse>, response: Response<CategoryResponse>) {
                if (response.isSuccessful && response.body()?.IsSuccess == true) {
                    categoryList.clear()
                    categoryList.addAll(response.body()!!.Categories)
                    Log.d("AdminPanel", "Successfully loaded ${categoryList.size} categories.")
                } else {
                    Toast.makeText(this@AdminPanel, "Failed to load categories", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<CategoryResponse>, t: Throwable) {
                Log.e("AdminPanel", "Error fetching categories", t)
            }
        })
    }
}