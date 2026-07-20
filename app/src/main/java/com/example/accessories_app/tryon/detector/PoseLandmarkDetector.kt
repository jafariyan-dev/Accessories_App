package com.example.accessories_app.tryon.detector

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker

class PoseLandmarkDetector(
    context: Context
) : AutoCloseable {

    private val poseLandmarker: PoseLandmarker

    init {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath(MODEL_NAME)
            .build()

        val options = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.IMAGE)
            .setNumPoses(1)
            .setMinPoseDetectionConfidence(0.5f)
            .setMinPosePresenceConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .build()

        poseLandmarker = PoseLandmarker.createFromOptions(
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

            val result = poseLandmarker.detect(mpImage)

            result.landmarks().firstOrNull()
                ?: throw PoseNotDetectedException()
        }
    }

    override fun close() {
        poseLandmarker.close()
    }

    private companion object {
        const val MODEL_NAME =
            "pose_landmarker_lite.task"
    }
}

class PoseNotDetectedException :
    IllegalStateException("No body pose was detected")