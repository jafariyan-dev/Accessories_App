package com.example.accessories_app.tryon.ui

import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.example.accessories_app.tryon.model.EarringPlacement
import com.example.accessories_app.tryon.model.OverlayTransform

class AccessoryOverlayController(
    private val container: FrameLayout,
    private val singleAccessoryView: ImageView,
    private val leftEarringView: ImageView,
    private val rightEarringView: ImageView
) {

    private var automaticScale = 1f
    private var userScale = 1f
    private var overlayMode = OverlayMode.SINGLE

    fun showSingleAccessory() {
        leftEarringView.visibility = View.GONE
        rightEarringView.visibility = View.GONE
        singleAccessoryView.visibility = View.VISIBLE
    }
    private enum class OverlayMode {
        SINGLE,
        EARRINGS
    }

    fun showEarrings(placement: EarringPlacement) {
        overlayMode = OverlayMode.EARRINGS
        singleAccessoryView.visibility = View.GONE

        leftEarringView.visibility = View.VISIBLE
        rightEarringView.visibility = View.VISIBLE

        leftEarringView.post {
            applyTransform(
                view = leftEarringView,
                transform = placement.left
            )

            applyTransform(
                view = rightEarringView,
                transform = placement.right
            )
        }
    }

    fun showEarringsManually() {
        overlayMode = OverlayMode.EARRINGS
        singleAccessoryView.visibility = View.GONE

        leftEarringView.visibility = View.VISIBLE
        rightEarringView.visibility = View.VISIBLE

        container.post {
            automaticScale = 0.7f
            applyScale()

            val centerX = container.width / 2f
            val centerY = container.height / 2f
            val distance = container.width * 0.22f

            leftEarringView.x =
                centerX - distance - leftEarringView.width / 2f

            leftEarringView.y =
                centerY - leftEarringView.height / 2f

            rightEarringView.x =
                centerX + distance - rightEarringView.width / 2f

            rightEarringView.y =
                centerY - rightEarringView.height / 2f

            leftEarringView.rotation = 0f
            rightEarringView.rotation = 0f

            leftEarringView.rotationY = 0f
            rightEarringView.rotationY = 180f
        }
    }

    fun setUserScale(scale: Float) {
        userScale = scale
        applyScale()
    }

    private fun applyTransform(
        view: ImageView,
        transform: OverlayTransform
    ) {
        val baseWidth = view.width.coerceAtLeast(1)

        automaticScale =
            transform.desiredWidth / baseWidth.toFloat()

        view.pivotX = view.width / 2f
        view.pivotY = 0f

        view.x = transform.anchorX - view.width / 2f
        view.y = transform.anchorY

        view.rotation = transform.rotation
        view.rotationY =
            if (transform.mirrorHorizontally) 180f else 0f

        applyScale()
    }

    private fun applyScale() {
        val finalScale = automaticScale * userScale

        leftEarringView.scaleX = finalScale
        leftEarringView.scaleY = finalScale

        rightEarringView.scaleX = finalScale
        rightEarringView.scaleY = finalScale
    }

    fun showNecklace(
        transform: OverlayTransform
    ) {
        overlayMode = OverlayMode.SINGLE

        leftEarringView.visibility = View.GONE
        rightEarringView.visibility = View.GONE
        singleAccessoryView.visibility = View.VISIBLE

        singleAccessoryView.post {
            val view = singleAccessoryView

            val baseWidth =
                view.width.coerceAtLeast(1)

            automaticScale =
                transform.desiredWidth /
                        baseWidth.toFloat()

            /*
             * مرکز View مبنای چرخش و مقیاس است.
             */
            view.pivotX = view.width / 2f
            view.pivotY = view.height / 2f

            /*
             * anchor محل مرکز گردنبند است.
             */
            view.x =
                transform.anchorX -
                        view.width / 2f

            view.y =
                transform.anchorY -
                        view.height / 2f

            view.rotation = transform.rotation
            view.rotationY = 0f

            applyScale()
        }
    }

    fun showNecklaceManually() {
        overlayMode = OverlayMode.SINGLE

        leftEarringView.visibility = View.GONE
        rightEarringView.visibility = View.GONE
        singleAccessoryView.visibility = View.VISIBLE

        container.post {
            val view = singleAccessoryView

            view.pivotX = view.width / 2f
            view.pivotY = 0f

            automaticScale =
                (container.width * 0.55f) /
                        view.width.coerceAtLeast(1).toFloat()

            view.x =
                (container.width - view.width) / 2f

            /*
             * محل تقریبی بالای سینه در حالت دستی.
             */
            view.y = container.height * 0.42f

            view.rotation = 0f
            view.rotationY = 0f

            applyScale()
        }
    }
}