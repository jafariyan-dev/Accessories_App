package com.uilover.project262.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.accessories_app.Activities.DetailsActivity
import com.example.accessories_app.Domain.Product
import com.example.accessories_app.databinding.ViewholderPopularBinding

class ItemsListCategoryAdapters(val items: ArrayList<Product>) :
    RecyclerView.Adapter<ItemsListCategoryAdapters.ViewHolder>() {
    lateinit var context:Context

    class ViewHolder(val binding:ViewholderPopularBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsListCategoryAdapters.ViewHolder {
        context=parent.context
        val binding=ViewholderPopularBinding.inflate(LayoutInflater.from(context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.TitelTxt.text = items[position].Title
        holder.binding.PriceTxt.text = "$" + items[position].Price.toString()

        Glide.with(context)
            .load(items[position].PhotoUrl)
            .into(holder.binding.Pic)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailsActivity::class.java)
            intent.putExtra("object", items[position])
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int =items.size
    }
