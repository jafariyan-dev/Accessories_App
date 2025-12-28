package com.example.accessories_app.Activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.accessories_app.Domain.CategoryModel
import com.example.accessories_app.Domain.Product
import com.example.accessories_app.R
import com.example.accessories_app.RequestAndResponse.CategoryResponse
import com.example.accessories_app.RequestAndResponse.DBResponse
import com.example.accessories_app.RequestAndResponse.PhotoUploadResponse
import com.example.accessories_app.RequestAndResponse.ProductRequest
import com.example.accessories_app.databinding.ActivityAddProductBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.text.isBlank

class AddProduct : AppCompatActivity() {
    private lateinit var binding: ActivityAddProductBinding
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                uploadPhotoToServer(it)
            }
        }
    private lateinit var categorySpinner: Spinner
    private var categoryList = mutableListOf<CategoryModel>()
    private var selectedCategoryId: Int = -1
    private var uploadedPhotoUrl: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
        categorySpinner = findViewById(R.id.spinnerCategory)
        loadCategory()
        binding.btnSelectImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnAddProduct.setOnClickListener {
            val name = binding.edtProductName.text.toString()
            val priceStr = binding.edtProductPrice.text.toString()
            val price:Int
            val description = binding.edtProductDescription.text.toString()

            if (selectedCategoryId == -1) {
                Toast.makeText(this, "Please select a category.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (name.isBlank() || priceStr.isBlank()) {
                Toast.makeText(this, "همه فیلدهای ضروری را پر کنید", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (priceStr.isBlank()) {
                Toast.makeText(this, "Price cannot be empty.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
               price = priceStr.toInt()
            }

            val product = Product(0,name,description,uploadedPhotoUrl.toString(),"", price,0 , selectedCategoryId)
            val intent = Intent()
            intent.putExtra("new_product", product)
            setResult(Activity.RESULT_OK)
            saveProductToDatabase(product)
            finish()
        }
    }
    private fun uploadPhotoToServer(imageUri: Uri) {
        Toast.makeText(this, "در حال آپلود تصویر...", Toast.LENGTH_SHORT).show()
        val inputStream = contentResolver.openInputStream(imageUri)
        val fileBytes = inputStream?.readBytes()
        inputStream?.close()

        if (fileBytes == null) {
            Toast.makeText(this, "نمی‌توان تصویر را خواند", Toast.LENGTH_SHORT).show()
            return
        }
        val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), fileBytes)
        val body = MultipartBody.Part.createFormData("image", "photo.jpg", requestFile)
        com.example.accessories_app.Api().instance().uploadPhoto(body).enqueue(object : Callback<PhotoUploadResponse> {
            override fun onResponse(call: Call<PhotoUploadResponse>, response: Response<PhotoUploadResponse>) {
                if (response.isSuccessful) {
                    val uploadResponse = response.body()
                    if (uploadResponse != null && uploadResponse.IsSuccess) {
                        uploadedPhotoUrl = uploadResponse.Url
                        Toast.makeText(this@AddProduct, "تصویر با موفقیت آپلود شد", Toast.LENGTH_SHORT).show()
                        Glide.with(this@AddProduct)
                            .load(uploadedPhotoUrl)
                            .into(binding.imgProduct)
                    } else {
                        Toast.makeText(this@AddProduct, "خطا در آپلود: ${uploadResponse?.Message}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@AddProduct, "خطای سرور: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<PhotoUploadResponse>, t: Throwable) {
                Toast.makeText(this@AddProduct, "شکست در ارتباط: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
    private fun saveProductToDatabase(newProduct: Product) {
        var request = ProductRequest(newProduct.Title,
            newProduct.Description,
            newProduct.PhotoUrl,
            newProduct.VideoUrl,
            newProduct.Price,
            newProduct.CategoryId)
        com.example.accessories_app.Api().instance()
            .addProduct(request)
            .enqueue(object : Callback<DBResponse> {
                override fun onResponse(call: Call<DBResponse>, response: Response<DBResponse>) {
                    if (response.isSuccessful) {
                        val simpleResponse = response.body()
                        if (simpleResponse != null) {
                            if (simpleResponse.IsSuccess) {
                                Toast.makeText(
                                    applicationContext,
                                    "Product saved to database!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Log.d("ADD_PRODUCT", "Data is valid. Creating request object.")
                                Toast.makeText(
                                    applicationContext, "API Error: ${simpleResponse?.Message}", Toast.LENGTH_LONG).show()
                            }
                        }
                        else {
                            Log.d("ADD_PRODUCT", "Data is valid. Creating request object.")
                            Toast.makeText(
                                applicationContext, "Empty Response: ${simpleResponse?.Message}", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Log.d("ADD_PRODUCT", "Data is valid. Creating request object.")
                        Toast.makeText(applicationContext, "Server Error: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<DBResponse>, t: Throwable) {
                    Log.d("ADD_PRODUCT", "Data is valid. Creating request object.")
                    Toast.makeText(applicationContext, "Network Failure: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }
    private fun loadCategory(){
        com.example.accessories_app.Api()
            .instance().getCategory().enqueue(object : Callback<CategoryResponse> {
            override fun onResponse(call: Call<CategoryResponse>, response: Response<CategoryResponse>) {
                if (response.isSuccessful) {
                    val categoryResponse = response.body()
                    if (categoryResponse != null && categoryResponse.IsSuccess) {
                        categoryList = categoryResponse.Categories
                        setupSpinner()
                    } else {
                        Log.d("FETCH_CATEGORY", "Data is valid. Creating request object.")
                        Toast.makeText(this@AddProduct, "Could not load categories: ${categoryResponse?.Message}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Log.d("FETCH_CATEGORY", "Data is valid. Creating request object.")
                    Toast.makeText(this@AddProduct, "Server Error: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<CategoryResponse>, t: Throwable) {
                Log.d("FETCH_CATEGORY", "Data is valid. Creating request object.")
                Toast.makeText(this@AddProduct, "Network Failure: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
    private fun setupSpinner() {
        val categoryTitles = categoryList.map { it.Title }
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categoryTitles
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = spinnerAdapter
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = categoryList[position]
                selectedCategoryId = selectedCategory.Id
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCategoryId = -1
            }
        }
    }
}
