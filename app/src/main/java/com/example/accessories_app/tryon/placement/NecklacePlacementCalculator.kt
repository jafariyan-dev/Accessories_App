package com.example.accessories_app.tryon.placement

import android.graphics.PointF
import com.example.accessories_app.tryon.model.OverlayTransform
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.atan2
import kotlin.math.hypot

class NecklacePlacementCalculator {

    fun calculate(
        landmarks: List<NormalizedLandmark>,
        mapLandmark: (NormalizedLandmark) -> PointF
    ): OverlayTransform? {

        if (landmarks.size <= RIGHT_SHOULDER) {
            return null
        }

        val leftShoulder =
            mapLandmark(landmarks[LEFT_SHOULDER])

        val rightShoulder =
            mapLandmark(landmarks[RIGHT_SHOULDER])

        val leftEar =
            mapLandmark(landmarks[LEFT_EAR])

        val rightEar =
            mapLandmark(landmarks[RIGHT_EAR])

        val shoulderWidth = hypot(
            rightShoulder.x - leftShoulder.x,
            rightShoulder.y - leftShoulder.y
        )

        if (shoulderWidth < MIN_SHOULDER_WIDTH_PX) {
            return null
        }

        val shoulderCenter = PointF(
            (leftShoulder.x + rightShoulder.x) / 2f,
            (leftShoulder.y + rightShoulder.y) / 2f
        )

        val earCenter = PointF(
            (leftEar.x + rightEar.x) / 2f,
            (leftEar.y + rightEar.y) / 2f
        )

        /*
         * حرکت از مرکز گوش‌ها به سمت مرکز شانه‌ها.
         *
         * مقدار 0.82 یعنی نقطه انتخابی نزدیک شانه‌ها باشد،
         * نه نزدیک صورت و سر.
         */
        val neckBaseX = lerp(
            earCenter.x,
            shoulderCenter.x,
            NECK_BASE_INTERPOLATION
        )

        val neckBaseY = lerp(
            earCenter.y,
            shoulderCenter.y,
            NECK_BASE_INTERPOLATION
        )

        /*
         * کمی انتقال به پایین، روی قسمت بالایی سینه.
         * چون محور Y در Android به سمت پایین افزایش پیدا می‌کند،
         * اینجا از علامت مثبت استفاده می‌کنیم.
         */
        val necklaceCenterX = neckBaseX

        val necklaceCenterY =
            neckBaseY +
                    shoulderWidth * NECKLACE_DOWN_OFFSET_RATIO

        val screenLeftShoulder: PointF
        val screenRightShoulder: PointF

        if (leftShoulder.x < rightShoulder.x) {
            screenLeftShoulder = leftShoulder
            screenRightShoulder = rightShoulder
        } else {
            screenLeftShoulder = rightShoulder
            screenRightShoulder = leftShoulder
        }

        val rawShoulderAngle = Math.toDegrees(
            atan2(
                screenRightShoulder.y - screenLeftShoulder.y,
                screenRightShoulder.x - screenLeftShoulder.x
            ).toDouble()
        ).toFloat()

        val shoulderAngle =
            normalizeOverlayAngle(rawShoulderAngle)

        val necklaceWidth =
            shoulderWidth * NECKLACE_WIDTH_RATIO

        return OverlayTransform(
            anchorX = necklaceCenterX,
            anchorY = necklaceCenterY,
            desiredWidth = necklaceWidth,
            rotation = shoulderAngle
        )
    }

    private fun lerp(
        start: Float,
        end: Float,
        amount: Float
    ): Float {
        return start + (end - start) * amount
    }
    private fun normalizeOverlayAngle(
        angle: Float
    ): Float {
        return when {
            angle > 90f -> angle - 180f
            angle < -90f -> angle + 180f
            else -> angle
        }
    }

    private companion object {

        const val LEFT_EAR = 7
        const val RIGHT_EAR = 8

        const val LEFT_SHOULDER = 11
        const val RIGHT_SHOULDER = 12

        /*
         * نزدیک‌ترشدن به شانه‌ها.
         * مقدار بیشتر یعنی گردنبند پایین‌تر قرار می‌گیرد.
         */
        const val NECK_BASE_INTERPOLATION = 0.82f

        /*
         * انتقال نهایی به پایین روی سینه.
         */
        const val NECKLACE_DOWN_OFFSET_RATIO = 0.08f

        /*
         * عرض گردنبند نسبت به عرض شانه‌ها.
         */
        const val NECKLACE_WIDTH_RATIO = 0.60f

        const val MIN_SHOULDER_WIDTH_PX = 30f
    }
}