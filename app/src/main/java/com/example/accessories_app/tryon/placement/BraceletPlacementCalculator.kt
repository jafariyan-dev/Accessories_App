package com.example.accessories_app.tryon.placement

import android.graphics.PointF
import com.example.accessories_app.tryon.model.OverlayTransform
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.atan2
import kotlin.math.hypot

class BraceletPlacementCalculator {

    fun calculate(
        landmarks: List<NormalizedLandmark>,
        mapLandmark: (NormalizedLandmark) -> PointF
    ): OverlayTransform? {
        if (landmarks.size <= PINKY_MCP) {
            return null
        }

        val wrist =
            mapLandmark(landmarks[WRIST])

        val indexMcp =
            mapLandmark(landmarks[INDEX_MCP])

        val middleMcp =
            mapLandmark(landmarks[MIDDLE_MCP])

        val pinkyMcp =
            mapLandmark(landmarks[PINKY_MCP])

        val palmWidth = hypot(
            indexMcp.x - pinkyMcp.x,
            indexMcp.y - pinkyMcp.y
        )

        if (palmWidth < MIN_PALM_WIDTH_PX) {
            return null
        }

        val directionX =
            wrist.x - middleMcp.x

        val directionY =
            wrist.y - middleMcp.y

        val directionLength = hypot(
            directionX,
            directionY
        ).coerceAtLeast(1f)

        val centerX =
            wrist.x +
                directionX / directionLength *
                palmWidth *
                WRIST_OFFSET_RATIO

        val centerY =
            wrist.y +
                directionY / directionLength *
                palmWidth *
                WRIST_OFFSET_RATIO

        val screenLeftPoint: PointF
        val screenRightPoint: PointF

        if (indexMcp.x <= pinkyMcp.x) {
            screenLeftPoint = indexMcp
            screenRightPoint = pinkyMcp
        } else {
            screenLeftPoint = pinkyMcp
            screenRightPoint = indexMcp
        }

        val rawAngle = Math.toDegrees(
            atan2(
                screenRightPoint.y - screenLeftPoint.y,
                screenRightPoint.x - screenLeftPoint.x
            ).toDouble()
        ).toFloat()

        return OverlayTransform(
            anchorX = centerX,
            anchorY = centerY,
            desiredWidth =
                palmWidth * BRACELET_WIDTH_RATIO,
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
        const val WRIST = 0
        const val INDEX_MCP = 5
        const val MIDDLE_MCP = 9
        const val PINKY_MCP = 17

        const val WRIST_OFFSET_RATIO = 0.15f
        const val BRACELET_WIDTH_RATIO = 1.35f
        const val MIN_PALM_WIDTH_PX = 10f
    }
}
