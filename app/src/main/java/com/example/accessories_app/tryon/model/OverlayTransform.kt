package com.example.accessories_app.tryon.model

data class OverlayTransform(
    val anchorX: Float,
    val anchorY: Float,
    val desiredWidth: Float,
    val rotation: Float = 0f,
    val mirrorHorizontally: Boolean = false
)
