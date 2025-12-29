package com.example.accessories_app.Activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.accessories_app.Api
import com.example.accessories_app.RequestAndResponse.ProductResponse
import com.example.accessories_app.RequestAndResponse.ProductWithCategoryRequest
import com.example.accessories_app.databinding.ActivityItemsListBinding
import com.uilover.project262.adapters.ItemsListCategoryAdapters
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ItemsListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityItemsListBinding
    private var id: String = ""
    private var title: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityItemsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getBundles()
        loadProductCategory()

    }//end
    private fun loadProductCategory(){
        binding.apply {
            progressBar.visibility=View.VISIBLE
            var request = ProductWithCategoryRequest(id.toInt())
            Api().instance().getItemCategory(request)
                .enqueue(object : Callback<ProductResponse> {
                    override fun onResponse(
                        call: Call<ProductResponse>,
                        response: Response<ProductResponse>
                    ) {
                        if (response.isSuccessful) {
                            val productResponse = response.body()
                            if (productResponse != null && productResponse.IsSuccess) {
                                val productList = productResponse.Products
                                binding.listView.layoutManager = GridLayoutManager(this@ItemsListActivity, 2)
                                binding.listView.adapter = ItemsListCategoryAdapters(productList)
                                progressBar.visibility=View.GONE
                            } else {
                                Toast.makeText(
                                    this@ItemsListActivity,
                                    productResponse?.Message ?: "An error occurred",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                        Toast.makeText(this@ItemsListActivity, t.message, Toast.LENGTH_LONG).show()
                    }
                })
            BackBtn.setOnClickListener{finish()}
        }
    }
    private fun getBundles(){
        id = intent.getStringExtra("Id") ?: ""
        title = intent.getStringExtra("Title") ?: ""
        binding.CategroyTxt.text=title
    }
}