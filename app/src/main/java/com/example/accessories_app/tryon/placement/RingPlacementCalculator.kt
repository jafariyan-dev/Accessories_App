package com.example.accessories_app.tryon.placement

import android.graphics.PointF
import com.example.accessories_app.tryon.model.OverlayTransform
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.atan2
import kotlin.math.hypot

class RingPlacementCalculator {

    fun calculate(
        landmarks: List<NormalizedLandmark>,
        mapLandmark: (NormalizedLandmark) -> PointF
    ): OverlayTransform? {
        if (landmarks.size <= PINKY_PIP) {
            return null
        }

        val ringMcp =
            mapLandmark(landmarks[RING_MCP])

        val ringPip =
            mapLandmark(landmarks[RING_PIP])

        val middlePip =
            mapLandmark(landmarks[MIDDLE_PIP])

        val pinkyPip =
            mapLandmark(landmarks[PINKY_PIP])

        val centerX =
            ringMcp.x +
                (ringPip.x - ringMcp.x) *
                RING_POSITION_RATIO

        val centerY =
            ringMcp.y +
                (ringPip.y - ringMcp.y) *
                RING_POSITION_RATIO

        val estimatedFingerWidth = hypot(
            pinkyPip.x - middlePip.x,
            pinkyPip.y - middlePip.y
        ) / FINGER_WIDTH_DIVISOR

        if (estimatedFingerWidth < MIN_FINGER_WIDTH_PX) {
            return null
        }

        val fingerAngle = Math.toDegrees(
            atan2(
                ringPip.y - ringMcp.y,
                ringPip.x - ringMcp.x
            ).toDouble()
        ).toFloat()

        return OverlayTransform(
            anchorX = centerX,
            anchorY = centerY,
            desiredWidth =
                estimatedFingerWidth * RING_WIDTH_RATIO,
            rotation =
                normalizeAngle(fingerAngle + 90f)
        )
    }

    private fun normalizeAngle(
        angle: Float
    ): Float {
        var normalized = angle

        while (normalized > 180f) {
            normalized -= 360f
        }

        while (normalized < -180f) {
            normalized += 360f
        }

        return when {
            normalized > 90f -> normalized - 180f
            normalized < -90f -> normalized + 180f
            else -> normalized
        }
    }

    private companion object {
        const val MIDDLE_PIP = 10
        const val RING_MCP = 13
        const val RING_PIP = 14
        const val PINKY_PIP = 18

        const val RING_POSITION_RATIO = 0.55f
        const val FINGER_WIDTH_DIVISOR = 2.3f
        const val RING_WIDTH_RATIO = 1.4f
        const val MIN_FINGER_WIDTH_PX = 3f
    }
}
