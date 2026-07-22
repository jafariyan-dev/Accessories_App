package com.example.accessories_app.tryon.detector

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker

class HandLandmarkDetector(
    context: Context
) : AutoCloseable {

    private val detector: HandLandmarker

    init {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath(MODEL_NAME)
            .build()

        val options = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.IMAGE)
            .setNumHands(1)
            .build()

        detector = HandLandmarker.createFromOptions(
            context.applicationContext,
            options
        )
    }

    fun detect(
        bitmap: Bitmap
    ): Result<List<NormalizedLandmark>> {
        return runCatching {
            val preparedBitmap =
                if (bitmap.config == Bitmap.Config.ARGB_8888) {
                    bitmap
                } else {
                    bitmap.copy(
                        Bitmap.Config.ARGB_8888,
                        false
                    )
                }

            val mpImage = BitmapImageBuilder(
                preparedBitmap
            ).build()

            val result = detector.detect(mpImage)

            result.landmarks().firstOrNull()
                ?: throw HandNotDetectedException()
        }
    }

    override fun close() {
        detector.close()
    }

    private companion object {
        const val MODEL_NAME = "hand_landmarker.task"
    }
}

class HandNotDetectedException :
    IllegalStateException("No hand was detected")
