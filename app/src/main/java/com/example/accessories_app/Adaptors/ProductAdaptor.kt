package com.example.accessories_app.Adaptors

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.accessories_app.Domain.CategoryModel
import com.example.accessories_app.Domain.Product
import com.example.accessories_app.R
import com.example.accessories_app.RequestAndResponse.CategoryResponse
import com.example.accessories_app.RequestAndResponse.DBResponse
import com.example.accessories_app.RequestAndResponse.EditProductRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductAdapter(
    private val products: ArrayList<Product>,
    private val categories: ArrayList<CategoryModel>,
    private val onSelectImageClick: (callback: (Uri) -> Unit) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.imgProduct)
        val name: TextView = itemView.findViewById(R.id.txtProductName)
        val price: TextView = itemView.findViewById(R.id.txtProductPrice)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]

        holder.name.text = product.Title
        holder.price.text = product.Price.toString()
        Glide.with(holder.itemView.context)
            .load(product.PhotoUrl)
            .into(holder.img)

        holder.btnEdit.setOnClickListener {
            showEditDialog(holder.itemView.context, position)
        }

        holder.btnDelete.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("حذف محصول")
                .setMessage("آیا مطمئن هستید می‌خواهید ${product.Title} را حذف کنید؟")
                .setPositiveButton("حذف") { d, _ ->
                    com.example.accessories_app.Api().instance().deleteProduct(product.Id)
                        .enqueue(object : Callback<DBResponse> {
                            override fun onResponse(
                                call: Call<DBResponse>,
                                response: Response<DBResponse>
                            ) {
                                if (response.isSuccessful && response.body()?.IsSuccess == true) {
                                    val itemIndex = products.indexOfFirst { it.Id == product.Id }
                                    if (itemIndex != -1) {
                                        products.removeAt(itemIndex)
                                        notifyItemRemoved(itemIndex)
                                        notifyItemRangeChanged(itemIndex, products.size)
                                    }
                                } else {
                                    val errorMessage =
                                        response.body()?.Message ?: "Unknown server error"
                                    Log.e(
                                        "DELETE_PRODUCT",
                                        "Error deleting product:$errorMessage"
                                    )
                                }
                            }

                            override fun onFailure(call: Call<DBResponse>, t: Throwable) {
                                Log.e("DELETE_PRODUCT", "API call failed", t)
                            }
                        })
                    d.dismiss()
                }
                .setNegativeButton("انصراف") { d, _ -> d.dismiss() }
                .show()
        }
    }
    override fun getItemCount(): Int = products.size

    private fun showEditDialog(context: Context, position: Int) {
        val product = products[position]

        val dialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_edit_product, null)

        val edtName = dialogView.findViewById<EditText>(R.id.edtProductName)
        val edtPrice = dialogView.findViewById<EditText>(R.id.edtProductPrice)
        val edtDescription = dialogView.findViewById<EditText>(R.id.edtProductDescription)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val btnSelectImage = dialogView.findViewById<Button>(R.id.btnSelectImage)
        val imgProduct = dialogView.findViewById<ImageView>(R.id.imgProduct)

        // مقداردهی امن فیلدها
        edtName.setText(product.Title)
        edtPrice.setText(product.Price.toString())
        edtDescription.setText(product.Description)

        Glide.with(context).load(product.PhotoUrl).into(imgProduct)
        imgProduct.tag = product.PhotoUrl
        val categoryTitles = categories.map { it.Title }

        val spinnerAdapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            categoryTitles
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = spinnerAdapter
        var currentCategoryIndex = -1
        for (i in categories.indices) {
            if (categories[i].Id == product.CategoryId) {
                currentCategoryIndex = i
                break
            }
        }
        if (currentCategoryIndex != -1) {
            spinnerCategory.setSelection(currentCategoryIndex)
        }

        // انتخاب تصویر
        btnSelectImage.setOnClickListener {
            onSelectImageClick { selectedUri ->
                imgProduct.setImageURI(selectedUri)
                imgProduct.tag = selectedUri
            }
        }

        AlertDialog.Builder(context)
            .setTitle("ویرایش محصول")
            .setView(dialogView)
            .setPositiveButton("ذخیره") { d, _ ->
                product.Title = edtName.text.toString()
                product.Price = edtPrice.text.toString().toInt()
                product.Description = edtDescription.text.toString()
                product.PhotoUrl = (imgProduct.tag as? Uri).toString()
                val selectedPosition = spinnerCategory.selectedItemPosition
                val newCategoryId =
                    if (selectedPosition >= 0 && selectedPosition < categories.size) {
                        categories[selectedPosition].Id
                    } else {
                        product.CategoryId
                    }
                product.CategoryId = newCategoryId
                var request = EditProductRequest(product.Id
                    ,product.Title
                    ,product.Description,
                    product.PhotoUrl,
                    product.VideoUrl,
                    product.Price,
                    product.CategoryId)
                com.example.accessories_app.Api().instance().editProduct(request)
                    .enqueue(object : Callback<DBResponse> {
                        override fun onResponse(
                            call: Call<DBResponse>,
                            response: Response<DBResponse>
                        ) {
                            if (response.isSuccessful && response.body()?.IsSuccess == true) {
                                products[position] = product
                                notifyItemChanged(position)
                                Toast.makeText(
                                    context,
                                    "محصول با موفقیت ویرایش شد",
                                    Toast.LENGTH_SHORT
                                ).show()
                                d.dismiss()
                            } else {
                                Toast.makeText(
                                    context, "خطا در ویرایش: ${response.body()?.Message}", Toast.LENGTH_LONG).show()
                            }
                        }
                        override fun onFailure(call: Call<DBResponse>, t: Throwable) {
                            Toast.makeText(
                                context, "شکست در ارتباط با سرور: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    })
                notifyItemChanged(position)
                Toast.makeText(context, "محصول ویرایش شد", Toast.LENGTH_SHORT).show()
                d.dismiss()
            }
            .setNegativeButton("انصراف") { d, _ -> d.dismiss() }
            .show()
    }
}