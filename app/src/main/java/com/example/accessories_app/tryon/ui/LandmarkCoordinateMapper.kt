package com.example.accessories_app.tryon.ui

import android.graphics.Matrix
import android.graphics.PointF
import android.widget.ImageView
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark

/**
 * مختصات نرمال‌شده MediaPipe را به مختصات والد ImageView تبدیل می‌کند.
 *
 * این کلاس زمانی درست کار می‌کند که ImageView تصویر کاربر و Viewهای
 * اکسسوری داخل یک والد مشترک، مانند FrameLayout، قرار داشته باشند.
 */
class LandmarkCoordinateMapper(
    private val imageView: ImageView
) {

    fun map(
        landmark: NormalizedLandmark
    ): PointF {
        val drawable =
            imageView.drawable ?: return PointF()

        val values = FloatArray(9)

        imageView.imageMatrix.getValues(values)

        val scaleX =
            values[Matrix.MSCALE_X]

        val scaleY =
            values[Matrix.MSCALE_Y]

        val translateX =
            values[Matrix.MTRANS_X]

        val translateY =
            values[Matrix.MTRANS_Y]

        val displayedLeft =
            imageView.left.toFloat() + translateX

        val displayedTop =
            imageView.top.toFloat() + translateY

        val displayedWidth =
            drawable.intrinsicWidth * scaleX

        val displayedHeight =
            drawable.intrinsicHeight * scaleY

        return PointF(
            displayedLeft +
                landmark.x() * displayedWidth,
            displayedTop +
                landmark.y() * displayedHeight
        )
    }
}
