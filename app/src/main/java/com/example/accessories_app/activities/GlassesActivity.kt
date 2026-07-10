package com.example.accessories_app.activities

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.accessories_app.databinding.ActivityGlassesBinding
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import kotlin.math.atan2
import android.graphics.BitmapFactory
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import java.io.File
import java.io.FileOutputStream
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.accessories_app.domain.Product
import kotlin.math.hypot

class GlassesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGlassesBinding

    private var faceLandMarker: FaceLandmarker? = null
    private var handLandMarker: HandLandmarker? = null
    private var selectedProduct: Product? = null
    private var selectedAccessory: String = ""
    private var currentUserImageFile: File? = null
    private var currentBitmap: Bitmap? = null
    private var autoScale = 1f
    private var userScale = 1f
    private var isAutoDetectionAvailable = false
    private var dX = 0f
    private var dY = 0f

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->

        if (uri == null) return@registerForActivityResult

        val savedFile = copyUserImageToInternalStorage(uri)

        currentUserImageFile = savedFile

        val bitmap = decodeBitmapFromFile(savedFile)

        currentBitmap = bitmap

        binding.mainImage.setImageBitmap(bitmap)

        binding.mainImage.post {
            processImage(bitmap)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGlassesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedProduct = intent.getSerializableExtra("product") as? Product

        if (selectedProduct == null) {
            Toast.makeText(this, "محصولی برای تست انتخاب نشده", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        selectedProduct?.let { product ->
            selectedAccessory = getTryOnType(product)
            loadProductImageOnAccessoryView(product)
        }

        setupLandmarkersSafely()
        setupSeekbar()
        enableDrag(binding.accessoryView)

        binding.pickButton.setOnClickListener {
            pickImageLauncher.launch(
                PickVisualMediaRequest(PickVisualMedia.ImageOnly)
            )
        }

        binding.root.post {
            pickImageLauncher.launch(
                PickVisualMediaRequest(PickVisualMedia.ImageOnly)
            )
        }
    }

    private fun setupLandmarkersSafely() {
        try {
            setupFaceLandmarker()
            setupHandLandMarker()
            isAutoDetectionAvailable = true
        } catch (e: UnsatisfiedLinkError) {
            isAutoDetectionAvailable = false
            e.printStackTrace()

            Toast.makeText(
                this,
                "MediaPipe روی این دستگاه/شبیه‌ساز پشتیبانی نمی‌شود. حالت دستی فعال شد.",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            isAutoDetectionAvailable = false
            e.printStackTrace()

            Toast.makeText(
                this,
                "خطا در راه‌اندازی تشخیص خودکار. حالت دستی فعال شد.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    private fun loadProductImageOnAccessoryView(product: Product) {
        binding.accessoryView.visibility = View.VISIBLE

        Glide.with(this)
            .load(product.PhotoUrl)
            .into(binding.accessoryView)
    }

    private fun getTryOnType(product: Product): String {
        return when (product.CategoryId) {
            1 -> "glasses"
            4 -> "bracelet"
            6 -> "ring"
            5 -> "earring"
            else -> "manual"
        }
    }

    private fun landmarkToParentPoint(
        landmark: NormalizedLandmark
    ): PointF {
        val imageView = binding.mainImage
        val drawable = imageView.drawable ?: return PointF(0f, 0f)

        val values = FloatArray(9)
        imageView.imageMatrix.getValues(values)

        val scaleX = values[Matrix.MSCALE_X]
        val scaleY = values[Matrix.MSCALE_Y]
        val transX = values[Matrix.MTRANS_X]
        val transY = values[Matrix.MTRANS_Y]

        val displayedImageRect = RectF(
            imageView.x + transX,
            imageView.y + transY,
            imageView.x + transX + drawable.intrinsicWidth * scaleX,
            imageView.y + transY + drawable.intrinsicHeight * scaleY
        )

        val x = displayedImageRect.left + landmark.x() * displayedImageRect.width()
        val y = displayedImageRect.top + landmark.y() * displayedImageRect.height()

        return PointF(x, y)
    }

    private fun copyUserImageToInternalStorage(uri: Uri): File {
        val directory = File(filesDir, "try_on_uploads")

        if (!directory.exists()) {
            directory.mkdirs()
        }

        val mimeType = contentResolver.getType(uri)
        val extension = when (mimeType) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }

        val file = File(
            directory,
            "user_image_${System.currentTimeMillis()}.$extension"
        )

        contentResolver.openInputStream(uri).use { input ->
            FileOutputStream(file).use { output ->
                input?.copyTo(output)
            }
        }

        return file
    }

    private fun decodeBitmapFromFile(file: File): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(file)

            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = true
            }
        } else {
            BitmapFactory.decodeFile(file.absolutePath)
        }
    }

    private fun setupSeekbar() {
        binding.sizeSeekBar.progress = 100

        binding.sizeSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    userScale = progress.coerceAtLeast(30) / 100f
                    applyAccessoryScale()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )
    }

    private fun setupFaceLandmarker() {

        val options =
            FaceLandmarker.FaceLandmarkerOptions.builder()
                .setBaseOptions(
                    BaseOptions.builder()
                        .setModelAssetPath("face_landmarker.task")
                        .build()
                )
                .setRunningMode(RunningMode.IMAGE)
                .setNumFaces(1)
                .build()

        faceLandMarker =
            FaceLandmarker.createFromOptions(this, options)
    }

    private fun setupHandLandMarker() {

        val options =
            HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(
                    BaseOptions.builder()
                        .setModelAssetPath("hand_landmarker.task")
                        .build()
                )
                .setRunningMode(RunningMode.IMAGE)
                .setNumHands(1)
                .build()

        handLandMarker =
            HandLandmarker.createFromOptions(this, options)
    }

    private fun processImage(bitmap: Bitmap) {
        if (!isAutoDetectionAvailable) {
            placeAccessoryManuallyAtCenter()
            return
        }

        when (selectedAccessory) {
            "glasses" -> detectFaceAndPlaceGlasses(bitmap)
            "ring" -> detectHandAndPlaceRing(bitmap)
            "bracelet" -> detectHandAndPlaceBracelet(bitmap)
            "watch" -> detectHandAndPlaceBracelet(bitmap)
            "earring" -> placeAccessoryManuallyAtCenter()
            else -> placeAccessoryManuallyAtCenter()
        }
    }
    private fun detectFaceAndPlaceGlasses(bitmap: Bitmap) {
        try {
            val mpImage = BitmapImageBuilder(bitmap).build()

            val result = faceLandMarker?.detect(mpImage)

            if (result == null || result.faceLandmarks().isEmpty()) {
                Toast.makeText(
                    this,
                    "صورت در تصویر تشخیص داده نشد",
                    Toast.LENGTH_SHORT
                ).show()

                placeAccessoryManuallyAtCenter()
                return
            }

            val landmarks = result.faceLandmarks()[0]

            placeGlassesOnFace(landmarks)

        } catch (e: Exception) {
            e.printStackTrace()

            Toast.makeText(
                this,
                "خطا در تشخیص صورت",
                Toast.LENGTH_SHORT
            ).show()

            placeAccessoryManuallyAtCenter()
        }
    }

    private fun placeGlassesOnFace(
        landmarks: List<NormalizedLandmark>
    ) {
        if (landmarks.size <= 263) {
            placeAccessoryManuallyAtCenter()
            return
        }

        val leftEye = landmarkToParentPoint(landmarks[33])
        val rightEye = landmarkToParentPoint(landmarks[263])

        val centerX = (leftEye.x + rightEye.x) / 2f
        val centerY = (leftEye.y + rightEye.y) / 2f

        val eyeDistance = hypot(
            rightEye.x - leftEye.x,
            rightEye.y - leftEye.y
        )

        val angle = Math.toDegrees(
            atan2(
                rightEye.y - leftEye.y,
                rightEye.x - leftEye.x
            ).toDouble()
        ).toFloat()

        binding.accessoryView.visibility = View.VISIBLE

        binding.accessoryView.post {
            val accessoryWidth = binding.accessoryView.width.takeIf { it > 0 } ?: 1

            autoScale = (eyeDistance * 2.1f) / accessoryWidth

            applyAccessoryScale()

            binding.accessoryView.pivotX = binding.accessoryView.width / 2f
            binding.accessoryView.pivotY = binding.accessoryView.height / 2f

            binding.accessoryView.x =
                centerX - binding.accessoryView.width / 2f

            binding.accessoryView.y =
                centerY - binding.accessoryView.height / 2f

            binding.accessoryView.rotation = angle
        }
    }

    private fun detectHandAndPlaceBracelet(bitmap: Bitmap) {
        try {
            val mpImage = BitmapImageBuilder(bitmap).build()

            val result = handLandMarker?.detect(mpImage)

            if (result == null || result.landmarks().isEmpty()) {
                Toast.makeText(
                    this,
                    "دست در تصویر تشخیص داده نشد",
                    Toast.LENGTH_SHORT
                ).show()

                placeAccessoryManuallyAtCenter()
                return
            }

            val landmarks = result.landmarks()[0]

            placeBraceletOnWrist(landmarks)

        } catch (e: Exception) {
            e.printStackTrace()

            Toast.makeText(
                this,
                "خطا در تشخیص دست",
                Toast.LENGTH_SHORT
            ).show()

            placeAccessoryManuallyAtCenter()
        }
    }

    private fun placeAccessoryManuallyAtCenter() {
        binding.accessoryView.visibility = View.VISIBLE

        binding.accessoryView.post {
            val parent = binding.frameContainer

            binding.accessoryView.x =
                (parent.width - binding.accessoryView.width) / 2f

            binding.accessoryView.y =
                (parent.height - binding.accessoryView.height) / 2f

            binding.accessoryView.rotation = 0f

            autoScale = 1f
            applyAccessoryScale()
        }
    }

    private fun applyAccessoryScale() {
        val finalScale = autoScale * userScale

        binding.accessoryView.scaleX = finalScale
        binding.accessoryView.scaleY = finalScale
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun enableDrag(view: View) {
        view.setOnTouchListener { v, event ->

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                }

                MotionEvent.ACTION_MOVE -> {
                    v.x = event.rawX + dX
                    v.y = event.rawY + dY
                }
            }

            true
        }
    }

    private fun placeBraceletOnWrist(
        landmarks: List<NormalizedLandmark>
    ) {
        if (landmarks.size <= 17) {
            placeAccessoryManuallyAtCenter()
            return
        }

        val wrist = landmarkToParentPoint(landmarks[0])
        val indexMcp = landmarkToParentPoint(landmarks[5])
        val middleMcp = landmarkToParentPoint(landmarks[9])
        val pinkyMcp = landmarkToParentPoint(landmarks[17])

        val palmWidth = hypot(
            indexMcp.x - pinkyMcp.x,
            indexMcp.y - pinkyMcp.y
        )

        val handAngle = Math.toDegrees(
            atan2(
                indexMcp.y - pinkyMcp.y,
                indexMcp.x - pinkyMcp.x
            ).toDouble()
        ).toFloat()

        val directionX = wrist.x - middleMcp.x
        val directionY = wrist.y - middleMcp.y

        val directionLength = hypot(directionX, directionY).coerceAtLeast(1f)

        val normalizedX = directionX / directionLength
        val normalizedY = directionY / directionLength

        val braceletCenterX = wrist.x + normalizedX * palmWidth * 0.15f
        val braceletCenterY = wrist.y + normalizedY * palmWidth * 0.15f

        binding.accessoryView.visibility = View.VISIBLE

        binding.accessoryView.post {
            val accessoryWidth = binding.accessoryView.width.takeIf { it > 0 } ?: 1

            autoScale = (palmWidth * 1.35f) / accessoryWidth

            applyAccessoryScale()

            binding.accessoryView.pivotX = binding.accessoryView.width / 2f
            binding.accessoryView.pivotY = binding.accessoryView.height / 2f

            binding.accessoryView.x =
                braceletCenterX - binding.accessoryView.width / 2f

            binding.accessoryView.y =
                braceletCenterY - binding.accessoryView.height / 2f

            binding.accessoryView.rotation = handAngle
        }
    }
    private fun detectHandAndPlaceRing(bitmap: Bitmap) {
        try {
            val mpImage = BitmapImageBuilder(bitmap).build()

            val result = handLandMarker?.detect(mpImage)

            if (result == null || result.landmarks().isEmpty()) {
                Toast.makeText(
                    this,
                    "دست در تصویر تشخیص داده نشد",
                    Toast.LENGTH_SHORT
                ).show()

                placeAccessoryManuallyAtCenter()
                return
            }

            val landmarks = result.landmarks()[0]

            placeRingOnRingFinger(landmarks)

        } catch (e: Exception) {
            e.printStackTrace()

            Toast.makeText(
                this,
                "خطا در تشخیص دست برای انگشتر",
                Toast.LENGTH_SHORT
            ).show()

            placeAccessoryManuallyAtCenter()
        }
    }

    private fun placeRingOnRingFinger(
        landmarks: List<NormalizedLandmark>
    ) {
        if (landmarks.size <= 16) {
            placeAccessoryManuallyAtCenter()
            return
        }

        val ringMcp = landmarkToParentPoint(landmarks[13])
        val ringPip = landmarkToParentPoint(landmarks[14])

        val ringPositionRatio = 0.55f

        val centerX = ringMcp.x + (ringPip.x - ringMcp.x) * ringPositionRatio
        val centerY = ringMcp.y + (ringPip.y - ringMcp.y) * ringPositionRatio

        val fingerAngle = Math.toDegrees(
            atan2(
                ringPip.y - ringMcp.y,
                ringPip.x - ringMcp.x
            ).toDouble()
        ).toFloat()

        val fingerSegmentLength = hypot(
            ringPip.x - ringMcp.x,
            ringPip.y - ringMcp.y
        )

        val estimatedFingerWidth = if (landmarks.size > 18) {
            val middlePip = landmarkToParentPoint(landmarks[10])
            val pinkyPip = landmarkToParentPoint(landmarks[18])

            hypot(
                pinkyPip.x - middlePip.x,
                pinkyPip.y - middlePip.y
            ) / 2.3f
        } else {
            fingerSegmentLength * 0.45f
        }

        binding.accessoryView.visibility = View.VISIBLE

        binding.accessoryView.post {
            val accessoryWidth =
                binding.accessoryView.width.takeIf { it > 0 } ?: 1

            autoScale = (estimatedFingerWidth * 1.4f) / accessoryWidth

            applyAccessoryScale()

            binding.accessoryView.pivotX = binding.accessoryView.width / 2f
            binding.accessoryView.pivotY = binding.accessoryView.height / 2f

            binding.accessoryView.x =
                centerX - binding.accessoryView.width / 2f

            binding.accessoryView.y =
                centerY - binding.accessoryView.height / 2f

            binding.accessoryView.rotation = fingerAngle + 90f
        }
    }
    override fun onDestroy() {
        super.onDestroy()

        faceLandMarker?.close()
        handLandMarker?.close()
    }
}