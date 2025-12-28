package com.example.accessories_app.Activities

import android.content.Context
import android.widget.Toast
import com.example.accessories_app.Domain.Product
import com.example.accessories_app.Helper.TinyDB

class ManagmentCart(val context: Context) {

    private val tinyDB = TinyDB(context)

    fun insertItems(item: Product) {
        val listItem = getListCart()
        val index = listItem.indexOfFirst { it.Id == item.Id }

        if (index != -1) {
            listItem[index].numberInCart = item.numberInCart
        } else {
            listItem.add(item)
        }
        tinyDB.putListObject("CartList", listItem)
        Toast.makeText(context, "Added to your Cart", Toast.LENGTH_SHORT).show()
    }

    fun getListCart(): ArrayList<Product> {
        return tinyDB.getListObject("CartList", Product::class.java)
    }

    fun minusItem(listItems: ArrayList<Product>, position: Int, listener: ChangeNumberItemsListener) {
        if (listItems[position].numberInCart == 1) {
            listItems.removeAt(position)
        } else {
            listItems[position].numberInCart--
        }
        tinyDB.putListObject("CartList", listItems)
        listener.onChanged()
    }

    fun removeItem(listItems: ArrayList<Product>, position: Int, listener: ChangeNumberItemsListener) {
        listItems.removeAt(position)
        tinyDB.putListObject("CartList", listItems)
        listener.onChanged()
    }

    fun plusItem(listItems: ArrayList<Product>, position: Int, listener: ChangeNumberItemsListener) {
        listItems[position].numberInCart++
        tinyDB.putListObject("CartList", listItems)
        listener.onChanged()
    }

    fun getTotalFee(): Double {
        val listItem = getListCart()
        return listItem.sumOf { it.Price.toDouble() * it.numberInCart }
    }
}
