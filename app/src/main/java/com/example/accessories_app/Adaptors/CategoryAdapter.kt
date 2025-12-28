package com.example.accessories_app.Adaptors
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import androidx.compose.animation.core.Transition
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.accessories_app.Activities.ItemsListActivity
import com.example.accessories_app.Domain.CategoryModel
import com.example.accessories_app.R
import com.example.accessories_app.databinding.ViewholderCategoryBinding
import com.google.firebase.database.Transaction

class CategoryAdapter(
    val items: MutableList<CategoryModel>
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private lateinit var context: Context
    private var selectedPosition = -1
    private var lastSelectedPosition = -1

    inner class ViewHolder(val binding: ViewholderCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding = ViewholderCategoryBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    // درست نوشتن نوع holder
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = items[position]
        holder.binding.titleCat.text = item.Title

        holder.binding.root.setOnClickListener {
            lastSelectedPosition = selectedPosition
            selectedPosition = position

            notifyItemChanged(lastSelectedPosition)
            notifyItemChanged(selectedPosition)

            Handler(Looper.getMainLooper()).postDelayed({
                val intent=Intent(context,ItemsListActivity::class.java).apply {
                   putExtra("Title",item.Title)
                   putExtra("Id",item.Id.toString())
                }
                ContextCompat.startActivity(context,intent,null)
            }, 500)
        }

        if (selectedPosition == position) {
            holder.binding.titleCat.setBackgroundResource(R.drawable.brown_full_corner_bg)
        } else {
            holder.binding.titleCat.setBackgroundResource(R.drawable.brown_2_full_corner)
             }
    }

    override fun getItemCount(): Int = items.size
}

