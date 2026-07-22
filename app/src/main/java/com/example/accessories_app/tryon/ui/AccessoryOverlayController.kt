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

    private enum class OverlayMode {
        SINGLE,
        EARRINGS
    }

    private var overlayMode =
        OverlayMode.SINGLE

    private var singleAutomaticScale = 1f
    private var leftEarringAutomaticScale = 1f
    private var rightEarringAutomaticScale = 1f
    private var userScale = 1f

    fun showSingleAccessory(
        transform: OverlayTransform
    ) {
        showCenteredSingle(
            transform = transform,
            scaleMultiplier = 1f
        )
    }

    fun showNecklace(
        transform: OverlayTransform
    ) {
        showCenteredSingle(
            transform = transform,
            scaleMultiplier =
                NECKLACE_SCALE_MULTIPLIER
        )
    }

    fun showEarrings(
        placement: EarringPlacement
    ) {
        overlayMode = OverlayMode.EARRINGS

        singleAccessoryView.visibility = View.GONE
        leftEarringView.visibility = View.VISIBLE
        rightEarringView.visibility = View.VISIBLE

        container.post {
            applyEarringTransform(
                view = leftEarringView,
                transform = placement.left,
                isLeft = true
            )

            applyEarringTransform(
                view = rightEarringView,
                transform = placement.right,
                isLeft = false
            )

            applyScale()
        }
    }

    fun showSingleManually() {
        overlayMode = OverlayMode.SINGLE

        hideEarrings()
        singleAccessoryView.visibility = View.VISIBLE

        container.post {
            val view = singleAccessoryView

            singleAutomaticScale = 1f

            view.pivotX = view.width / 2f
            view.pivotY = view.height / 2f

            view.x =
                (container.width - view.width) / 2f

            view.y =
                (container.height - view.height) / 2f

            reset3DRotation(view)
            view.rotation = 0f

            applyScale()
        }
    }

    fun showNecklaceManually() {
        overlayMode = OverlayMode.SINGLE

        hideEarrings()
        singleAccessoryView.visibility = View.VISIBLE

        container.post {
            val view = singleAccessoryView

            singleAutomaticScale =
                (
                    container.width *
                        MANUAL_NECKLACE_WIDTH_RATIO /
                        view.width.coerceAtLeast(1).toFloat()
                ) * NECKLACE_SCALE_MULTIPLIER

            view.pivotX = view.width / 2f
            view.pivotY = view.height / 2f

            view.x =
                (container.width - view.width) / 2f

            view.y =
                container.height *
                    MANUAL_NECKLACE_TOP_RATIO -
                    view.height / 2f

            reset3DRotation(view)
            view.rotation = 0f

            applyScale()
        }
    }

    fun showEarringsManually() {
        overlayMode = OverlayMode.EARRINGS

        singleAccessoryView.visibility = View.GONE
        leftEarringView.visibility = View.VISIBLE
        rightEarringView.visibility = View.VISIBLE

        container.post {
            leftEarringAutomaticScale =
                MANUAL_EARRING_SCALE

            rightEarringAutomaticScale =
                MANUAL_EARRING_SCALE

            val centerX =
                container.width / 2f

            val centerY =
                container.height / 2f

            val horizontalDistance =
                container.width *
                    MANUAL_EARRING_DISTANCE_RATIO

            configureTopAnchoredView(
                view = leftEarringView,
                anchorX =
                    centerX - horizontalDistance,
                anchorY =
                    centerY -
                        leftEarringView.height / 2f,
                rotation = 0f,
                mirrorHorizontally = false
            )

            configureTopAnchoredView(
                view = rightEarringView,
                anchorX =
                    centerX + horizontalDistance,
                anchorY =
                    centerY -
                        rightEarringView.height / 2f,
                rotation = 0f,
                mirrorHorizontally = true
            )

            applyScale()
        }
    }

    fun setUserScale(
        scale: Float
    ) {
        userScale = scale
        applyScale()
    }

    private fun showCenteredSingle(
        transform: OverlayTransform,
        scaleMultiplier: Float
    ) {
        overlayMode = OverlayMode.SINGLE

        hideEarrings()
        singleAccessoryView.visibility = View.VISIBLE

        singleAccessoryView.post {
            val view = singleAccessoryView

            singleAutomaticScale =
                (
                    transform.desiredWidth /
                        view.width
                            .coerceAtLeast(1)
                            .toFloat()
                ) * scaleMultiplier

            view.pivotX = view.width / 2f
            view.pivotY = view.height / 2f

            view.x =
                transform.anchorX -
                    view.width / 2f

            view.y =
                transform.anchorY -
                    view.height / 2f

            view.rotationX = 0f

            view.rotationY =
                if (
                    transform.mirrorHorizontally
                ) {
                    180f
                } else {
                    0f
                }

            view.rotation = transform.rotation

            applyScale()
        }
    }

    private fun applyEarringTransform(
        view: ImageView,
        transform: OverlayTransform,
        isLeft: Boolean
    ) {
        val automaticScale =
            transform.desiredWidth /
                view.width
                    .coerceAtLeast(1)
                    .toFloat()

        if (isLeft) {
            leftEarringAutomaticScale =
                automaticScale
        } else {
            rightEarringAutomaticScale =
                automaticScale
        }

        configureTopAnchoredView(
            view = view,
            anchorX = transform.anchorX,
            anchorY = transform.anchorY,
            rotation = transform.rotation,
            mirrorHorizontally =
                transform.mirrorHorizontally
        )
    }

    private fun configureTopAnchoredView(
        view: ImageView,
        anchorX: Float,
        anchorY: Float,
        rotation: Float,
        mirrorHorizontally: Boolean
    ) {
        view.pivotX = view.width / 2f
        view.pivotY = 0f

        view.x =
            anchorX - view.width / 2f

        view.y = anchorY

        view.rotationX = 0f

        view.rotationY =
            if (mirrorHorizontally) 180f else 0f

        view.rotation = rotation
    }

    private fun applyScale() {
        when (overlayMode) {
            OverlayMode.SINGLE -> {
                val scale =
                    singleAutomaticScale *
                        userScale

                singleAccessoryView.scaleX =
                    scale

                singleAccessoryView.scaleY =
                    scale
            }

            OverlayMode.EARRINGS -> {
                val leftScale =
                    leftEarringAutomaticScale *
                        userScale

                val rightScale =
                    rightEarringAutomaticScale *
                        userScale

                leftEarringView.scaleX =
                    leftScale

                leftEarringView.scaleY =
                    leftScale

                rightEarringView.scaleX =
                    rightScale

                rightEarringView.scaleY =
                    rightScale
            }
        }
    }

    private fun hideEarrings() {
        leftEarringView.visibility = View.GONE
        rightEarringView.visibility = View.GONE
    }

    private fun reset3DRotation(
        view: View
    ) {
        view.rotationX = 0f
        view.rotationY = 0f
    }

    private companion object {
        /*
         * اگر قبلاً اندازه گردنبند را تنظیم کرده‌ای،
         * مقدار مناسب پروژه خودت را اینجا قرار بده.
         */
        const val NECKLACE_SCALE_MULTIPLIER = 0.75f

        const val MANUAL_NECKLACE_WIDTH_RATIO = 0.55f
        const val MANUAL_NECKLACE_TOP_RATIO = 0.42f

        const val MANUAL_EARRING_SCALE = 0.7f
        const val MANUAL_EARRING_DISTANCE_RATIO = 0.22f
    }
}
