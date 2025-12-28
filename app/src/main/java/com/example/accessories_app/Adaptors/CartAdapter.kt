package com.example.accessories_app.Adaptors

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.example.accessories_app.Activities.ChangeNumberItemsListener
import com.example.accessories_app.Activities.ManagmentCart
import com.example.accessories_app.Domain.Product
import com.example.accessories_app.databinding.ViewholderCartBinding

class CartAdapter (private val listItemSelected: ArrayList<Product>,
                   context:Context,
                   var changeNumber:ChangeNumberItemsListener?= null
    ): RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    class ViewHolder(val binding: ViewholderCartBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val managementCart = ManagmentCart(context)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartAdapter.ViewHolder {
        val binding =
            ViewholderCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartAdapter.ViewHolder, position: Int) {
        val item = listItemSelected[position]
        holder.binding.titelTxt.text = item.Title
        holder.binding.totalTxt.text = "$${item.Price}"
        holder.binding.totalEachPrice.text = "$${item.numberInCart * item.Price.toDouble()}"
        holder.binding.NumberCart.text = item.numberInCart.toString()

        Glide.with(holder.itemView.context)
            .load(item.PhotoUrl[0])
            .apply(RequestOptions().transform(CenterCrop()))
            .into(holder.binding.picCart)

        holder.binding.PlusBtn.setOnClickListener {
            managementCart.plusItem(listItemSelected, position, object : ChangeNumberItemsListener {
                override fun onChanged() {
                    notifyDataSetChanged()
                    changeNumber?.onChanged()
                }
            })
        }
        holder.binding.MinusBtn.setOnClickListener {
            managementCart.minusItem(
                listItems = listItemSelected,
                position,
                listener = object : ChangeNumberItemsListener {
                    override fun onChanged() {
                        notifyDataSetChanged()
                        changeNumber?.onChanged()
                    }
                })
        }

        holder.binding.removeItemBtn.setOnClickListener {
            managementCart.removeItem(
                listItems = listItemSelected,
                position,
                listener = object : ChangeNumberItemsListener {
                    override fun onChanged() {
                        notifyDataSetChanged()
                        changeNumber?.onChanged()
                    }
                })
        }

    }

    override fun getItemCount(): Int = listItemSelected.size
}