package com.example.accessories_app.tryon.model

import com.example.accessories_app.domain.Product

object AccessoryTypeMapper {

    fun from(product: Product): AccessoryType {
        return when (product.CategoryId) {
            1 -> AccessoryType.GLASSES
            2 -> AccessoryType.NECKLACE
            4 -> AccessoryType.BRACELET
            6 -> AccessoryType.RING
            5 -> AccessoryType.EARRING
            else -> AccessoryType.UNKNOWN
        }
    }
}
