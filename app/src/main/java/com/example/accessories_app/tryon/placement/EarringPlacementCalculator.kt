package com.example.accessories_app.tryon.placement

import android.graphics.PointF
import com.example.accessories_app.tryon.model.EarringPlacement
import com.example.accessories_app.tryon.model.OverlayTransform
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.atan2
import kotlin.math.hypot

class EarringPlacementCalculator {

    companion object {
        private const val LEFT_FACE_SIDE = 234
        private const val LEFT_JAW = 172

        private const val RIGHT_FACE_SIDE = 454
        private const val RIGHT_JAW = 397

        private const val LEFT_EYE = 33
        private const val RIGHT_EYE = 263

        private const val JAW_INTERPOLATION = 0.68f
        private const val OUTWARD_OFFSET_RATIO = 0.035f
        private const val VERTICAL_OFFSET_RATIO = 0.015f
        private const val EARRING_WIDTH_RATIO = 0.18f
        private const val ROTATION_RATIO = 0.15f
    }

    fun calculate(
        landmarks: List<NormalizedLandmark>,
        mapLandmark: (NormalizedLandmark) -> PointF
    ): EarringPlacement? {
        if (landmarks.size <= RIGHT_FACE_SIDE) {
            return null
        }

        val leftFaceSide = mapLandmark(landmarks[LEFT_FACE_SIDE])
        val leftJaw = mapLandmark(landmarks[LEFT_JAW])

        val rightFaceSide = mapLandmark(landmarks[RIGHT_FACE_SIDE])
        val rightJaw = mapLandmark(landmarks[RIGHT_JAW])

        val leftEye = mapLandmark(landmarks[LEFT_EYE])
        val rightEye = mapLandmark(landmarks[RIGHT_EYE])

        val faceWidth = hypot(
            rightFaceSide.x - leftFaceSide.x,
            rightFaceSide.y - leftFaceSide.y
        ).coerceAtLeast(1f)

        var leftEarX = lerp(
            leftFaceSide.x,
            leftJaw.x,
            JAW_INTERPOLATION
        )

        var leftEarY = lerp(
            leftFaceSide.y,
            leftJaw.y,
            JAW_INTERPOLATION
        )

        var rightEarX = lerp(
            rightFaceSide.x,
            rightJaw.x,
            JAW_INTERPOLATION
        )

        var rightEarY = lerp(
            rightFaceSide.y,
            rightJaw.y,
            JAW_INTERPOLATION
        )

        val outwardOffset = faceWidth * OUTWARD_OFFSET_RATIO
        val verticalOffset = faceWidth * VERTICAL_OFFSET_RATIO

        leftEarX -= outwardOffset
        rightEarX += outwardOffset

        leftEarY += verticalOffset
        rightEarY += verticalOffset

        val faceAngle = Math.toDegrees(
            atan2(
                rightEye.y - leftEye.y,
                rightEye.x - leftEye.x
            ).toDouble()
        ).toFloat()

        val desiredWidth = faceWidth * EARRING_WIDTH_RATIO
        val rotation = faceAngle * ROTATION_RATIO

        return EarringPlacement(
            left = OverlayTransform(
                anchorX = leftEarX,
                anchorY = leftEarY,
                desiredWidth = desiredWidth,
                rotation = rotation
            ),
            right = OverlayTransform(
                anchorX = rightEarX,
                anchorY = rightEarY,
                desiredWidth = desiredWidth,
                rotation = rotation,
                mirrorHorizontally = true
            )
        )
    }

    private fun lerp(
        start: Float,
        end: Float,
        amount: Float
    ): Float {
        return start + (end - start) * amount
    }
}