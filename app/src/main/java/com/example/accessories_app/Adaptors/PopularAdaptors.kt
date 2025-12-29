package com.example.accessories_app.Adaptors

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.animation.with
import androidx.compose.ui.semantics.text
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.accessories_app.Activities.DetailsActivity
import com.example.accessories_app.Domain.Product
import com.example.accessories_app.databinding.ViewholderPopularBinding
import kotlin.io.path.name

class PopularAdaptors(val items: ArrayList<Product>) :
    RecyclerView.Adapter<PopularAdaptors.ViewHolder>() {
    lateinit var context:Context

    class ViewHolder(val binding:ViewholderPopularBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val binding = ViewholderPopularBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val currentItem = items[position]
        holder.binding.TitelTxt.text = currentItem.Title
        holder.binding.PriceTxt.text = "$" + currentItem.Price
        holder.binding.SubtitelTxt.text = currentItem.Description
        val context = holder.itemView.context
        Glide.with(context)
            .load(currentItem.PhotoUrl)
            .into(holder.binding.Pic)
        val intent = Intent(context, DetailsActivity::class.java)
        intent.putExtra("object", items[position])
        context.startActivity(intent)
    }

    override fun getItemCount(): Int =items.size
    }
