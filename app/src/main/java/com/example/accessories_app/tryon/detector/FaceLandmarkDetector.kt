package com.example.accessories_app.tryon.detector

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker

class FaceLandmarkDetector(
    context: Context
) : AutoCloseable {

    private val detector: FaceLandmarker

    init {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("face_landmarker.task")
            .build()

        val options = FaceLandmarker.FaceLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.IMAGE)
            .setNumFaces(1)
            .build()

        detector = FaceLandmarker.createFromOptions(
            context.applicationContext,
            options
        )
    }

    fun detect(
        bitmap: Bitmap
    ): Result<List<NormalizedLandmark>> {
        return runCatching {
            val mpImage = BitmapImageBuilder(bitmap).build()
            val result = detector.detect(mpImage)

            result.faceLandmarks().firstOrNull()
                ?: throw FaceNotDetectedException()
        }
    }

    override fun close() {
        detector.close()
    }
}

class FaceNotDetectedException :
    IllegalStateException("No face was detected")