package com.example.accessories_app.tryon.placement

import android.graphics.PointF
import com.example.accessories_app.tryon.model.OverlayTransform
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.atan2
import kotlin.math.hypot

class GlassesPlacementCalculator {

    fun calculate(
        landmarks: List<NormalizedLandmark>,
        mapLandmark: (NormalizedLandmark) -> PointF
    ): OverlayTransform? {
        if (landmarks.size <= RIGHT_EYE) {
            return null
        }

        val firstEye = mapLandmark(landmarks[LEFT_EYE])
        val secondEye = mapLandmark(landmarks[RIGHT_EYE])

        val eyeDistance = hypot(
            secondEye.x - firstEye.x,
            secondEye.y - firstEye.y
        )

        if (eyeDistance < MIN_EYE_DISTANCE_PX) {
            return null
        }

        val screenLeftEye: PointF
        val screenRightEye: PointF

        if (firstEye.x <= secondEye.x) {
            screenLeftEye = firstEye
            screenRightEye = secondEye
        } else {
            screenLeftEye = secondEye
            screenRightEye = firstEye
        }

        val centerX =
            (firstEye.x + secondEye.x) / 2f

        val centerY =
            (firstEye.y + secondEye.y) / 2f

        val rawAngle = Math.toDegrees(
            atan2(
                screenRightEye.y - screenLeftEye.y,
                screenRightEye.x - screenLeftEye.x
            ).toDouble()
        ).toFloat()

        return OverlayTransform(
            anchorX = centerX,
            anchorY = centerY,
            desiredWidth =
                eyeDistance * GLASSES_WIDTH_RATIO,
            rotation = normalizeAngle(rawAngle)
        )
    }

    private fun normalizeAngle(
        angle: Float
    ): Float {
        return when {
            angle > 90f -> angle - 180f
            angle < -90f -> angle + 180f
            else -> angle
        }
    }

    private companion object {
        const val LEFT_EYE = 33
        const val RIGHT_EYE = 263

        const val GLASSES_WIDTH_RATIO = 2.1f
        const val MIN_EYE_DISTANCE_PX = 10f
    }
}
