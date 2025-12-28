package com.example.accessories_app.Activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.accessories_app.Adaptors.CartAdapter
import com.example.accessories_app.R
import com.example.accessories_app.databinding.ActivityCartBinding

class CartActivity : AppCompatActivity() {
    lateinit var binding: ActivityCartBinding
    lateinit var managementCart: ManagmentCart
    private var tax: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        managementCart = ManagmentCart(context = this)

        calculateCart()
        setVariable()
        initCartList()
    }

    private fun initCartList() {
        binding.apply {
            listView.layoutManager =
                LinearLayoutManager(
                     this@CartActivity, LinearLayoutManager.VERTICAL,false
                )
            listView.adapter = CartAdapter(
                 managementCart.getListCart(),
                this@CartActivity,
                 object : ChangeNumberItemsListener {
                    override fun onChanged() {
                        calculateCart()
                    }
                }
            )
        }
    }

    private fun setVariable() {
        binding.BackBtn.setOnClickListener { finish() }
    }

    private fun calculateCart() {
            val percentTax = 0.02
            val delivery = 10.0
            tax = ((managementCart.getTotalFee() * percentTax) * 100) / 100
            val total = ((managementCart.getTotalFee() + tax + delivery) * 100) / 100
            val itemTotal = (managementCart.getTotalFee() * 100) / 100
            binding.apply {
                TotalFeetTxt.text = "$$itemTotal"
                textTotalTax.text = "$$tax"
                DeliveryTxt.text = "$$delivery"
                FinalTotalTxt.text = "$$total"
            }
        }
    }

