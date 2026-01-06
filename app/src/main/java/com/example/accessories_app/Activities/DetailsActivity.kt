package com.example.accessories_app.Activities

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.example.accessories_app.Domain.Product
import com.example.accessories_app.R
import com.example.accessories_app.databinding.ActivityDetailsBinding

class DetailsActivity : AppCompatActivity() {
    lateinit var binding:ActivityDetailsBinding
    private lateinit var item:Product
    private lateinit var managmentCart: ManagmentCart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        managmentCart = ManagmentCart(context = this)
        bundle()
        initSizeList()
    }

    private fun initSizeList() {
        binding.apply {
            SmallBtn.setOnClickListener {
                SmallBtn.setBackgroundResource(R.drawable.brown_full_corner_bg)
                MediumBtn.setBackgroundResource(0)
                LargeBtn.setBackgroundResource(0)
            }

            MediumBtn.setOnClickListener {
                SmallBtn.setBackgroundResource(0)
                MediumBtn.setBackgroundResource(R.drawable.brown_full_corner_bg)
                LargeBtn.setBackgroundResource(0)
            }

            LargeBtn.setOnClickListener {
                SmallBtn.setBackgroundResource(0)
                MediumBtn.setBackgroundResource(0)
                LargeBtn.setBackgroundResource(R.drawable.brown_full_corner_bg)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun bundle() {
        binding.apply {
            item = intent.getSerializableExtra("object") as Product
            Glide.with(this@DetailsActivity)
                .load(item.VideoUrl)
                .into( binding.VideoMain)

            titelTxt.text = item.Title
            DescriptionTxt.text = item.Description
            priceTxt.text = "$" + item.Price

            AddtochartBtn.setOnClickListener {
                item.numberInCart = Integer.valueOf(NumberCart.text.toString())
                managmentCart.insertItems(item)
            }

            BackBtn.setOnClickListener { finish() }

            PlusBtn.setOnClickListener {
                item.numberInCart++
                NumberCart.text = (item.numberInCart).toString()
            }

            MinusBtn.setOnClickListener{
                if (item.numberInCart>0){
                    item.numberInCart--
                    NumberCart.text=(item.numberInCart).toString()
                }
            }
            }
    }
}