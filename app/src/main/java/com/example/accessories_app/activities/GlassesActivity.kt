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
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.accessories_app.R
import com.example.accessories_app.domain.Product
import com.example.accessories_app.tryon.detector.FaceLandmarkDetector
import com.example.accessories_app.tryon.detector.PoseLandmarkDetector
import com.example.accessories_app.tryon.model.AccessoryType
import com.example.accessories_app.tryon.placement.EarringPlacementCalculator
import com.example.accessories_app.tryon.placement.NecklacePlacementCalculator
import com.example.accessories_app.tryon.ui.AccessoryOverlayController
import com.example.accessories_app.tryon.ui.DragTouchListener
import kotlin.math.hypot

class GlassesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGlassesBinding

    private lateinit var faceDetector: FaceLandmarkDetector
    private lateinit var overlayController: AccessoryOverlayController

    private val earringPlacementCalculator =
        EarringPlacementCalculator()

    private var selectedAccessory =
        AccessoryType.UNKNOWN

    private var faceLandMarker: FaceLandmarker? = null
    private var handLandMarker: HandLandmarker? = null
    private var selectedProduct: Product? = null
    private var currentUserImageFile: File? = null
    private var currentBitmap: Bitmap? = null
    private var autoScale = 1f
    private var userScale = 1f
    private var isAutoDetectionAvailable = false
    private var dX = 0f
    private var dY = 0f

    private var poseDetector: PoseLandmarkDetector? = null

    private val necklacePlacementCalculator =
        NecklacePlacementCalculator()

    private val detectionExecutor =
        java.util.concurrent.Executors
            .newSingleThreadExecutor()

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

        setupScaleSeekBar()

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
        setupTryOnComponents()
        setupLandmarkersSafely()
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

    @SuppressLint("ClickableViewAccessibility")
    private fun setupDraggableViews() {
        binding.accessoryView.setOnTouchListener(DragTouchListener())
        binding.leftEarringView.setOnTouchListener(DragTouchListener())
        binding.rightEarringView.setOnTouchListener(DragTouchListener())
    }

    private fun setupTryOnComponents() {
        overlayController = AccessoryOverlayController(
            container = binding.frameContainer,
            singleAccessoryView = binding.accessoryView,
            leftEarringView = binding.leftEarringView,
            rightEarringView = binding.rightEarringView
        )

        /*
         * فقط برای محصول گردنبند مدل Pose ساخته می‌شود.
         */
        if (selectedAccessory == AccessoryType.NECKLACE) {
            poseDetector = PoseLandmarkDetector(this)
        }

        /*
         * مدل Face را برای عینک و گوشواره بساز.
         */
        if (
            selectedAccessory == AccessoryType.GLASSES ||
            selectedAccessory == AccessoryType.EARRING
        ) {
            faceDetector = FaceLandmarkDetector(this)
        }

        setupDraggableViews()
    }


    private fun processEarring(bitmap: Bitmap) {
        faceDetector.detect(bitmap)
            .onSuccess { landmarks ->
                val placement =
                    earringPlacementCalculator.calculate(
                        landmarks = landmarks,
                        mapLandmark = ::landmarkToParentPoint
                    )

                if (placement == null) {
                    showFaceNotDetected()
                    overlayController.showEarringsManually()
                    return@onSuccess
                }

                overlayController.showEarrings(placement)
            }
            .onFailure {
                showFaceNotDetected()
                overlayController.showEarringsManually()
            }
    }

    private fun showFaceNotDetected() {
        Toast.makeText(
            this,
            R.string.face_not_detected,
            Toast.LENGTH_SHORT
        ).show()
    }
    private fun setupScaleSeekBar() {
        binding.sizeSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val scale = 0.5f + progress / 100f
                    overlayController.setUserScale(scale)
                }

                override fun onStartTrackingTouch(
                    seekBar: SeekBar?
                ) = Unit

                override fun onStopTrackingTouch(
                    seekBar: SeekBar?
                ) = Unit
            }
        )
    }
    override fun onDestroy() {
        poseDetector?.close()
        poseDetector = null

        if (::faceDetector.isInitialized) {
            faceDetector.close()
        }

        detectionExecutor.shutdownNow()

        super.onDestroy()
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
        if (selectedAccessory == AccessoryType.EARRING) {
            binding.accessoryView.visibility = View.GONE

            binding.leftEarringView.visibility = View.INVISIBLE
            binding.rightEarringView.visibility = View.INVISIBLE

            Glide.with(this)
                .load(product.PhotoUrl)
                .into(binding.leftEarringView)

            Glide.with(this)
                .load(product.PhotoUrl)
                .into(binding.rightEarringView)
        } else {
            binding.leftEarringView.visibility = View.GONE
            binding.rightEarringView.visibility = View.GONE

            binding.accessoryView.visibility = View.VISIBLE

            Glide.with(this)
                .load(product.PhotoUrl)
                .into(binding.accessoryView)
        }
    }

    private fun getTryOnType(product: Product): AccessoryType {
        return when (product.CategoryId) {
            1 -> AccessoryType.GLASSES
            2 -> AccessoryType.NECKLACE
            4 -> AccessoryType.BRACELET
            6 -> AccessoryType.RING
            5 -> AccessoryType.EARRING
            else -> AccessoryType.UNKNOWN
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
            if (selectedAccessory == AccessoryType.EARRING) {
                placeEarringsManually()
            } else {
                placeAccessoryManuallyAtCenter()
            }
            return
        }

        when (selectedAccessory) {
            AccessoryType.GLASSES -> detectFaceAndPlaceGlasses(bitmap)
            AccessoryType.RING -> detectHandAndPlaceRing(bitmap)
            AccessoryType.BRACELET -> processEarring(bitmap)
            AccessoryType.WATCH -> detectHandAndPlaceBracelet(bitmap)
            AccessoryType.EARRING -> detectFaceAndPlaceEarrings(bitmap)
            AccessoryType.NECKLACE -> {
                processNecklace(bitmap)
            }
            else -> placeAccessoryManuallyAtCenter()
        }
    }
    private fun processNecklace(
        bitmap: Bitmap
    ) {
        val detector = poseDetector

        if (detector == null) {
            showPoseDetectionError()
            overlayController.showNecklaceManually()
            return
        }

        /*
         * detect در حالت IMAGE مسدودکننده است؛
         * بنابراین روی Thread پس‌زمینه اجرا می‌شود.
         */
        detectionExecutor.execute {
            val detectionResult = detector.detect(bitmap)

            runOnUiThread {
                if (isFinishing || isDestroyed) {
                    return@runOnUiThread
                }

                detectionResult
                    .onSuccess { landmarks ->
                        val placement =
                            necklacePlacementCalculator.calculate(
                                landmarks = landmarks,
                                mapLandmark =
                                ::landmarkToParentPoint
                            )

                        if (placement == null) {
                            showPoseNotDetected()
                            overlayController
                                .showNecklaceManually()

                            return@onSuccess
                        }

                        overlayController.showNecklace(
                            placement
                        )
                    }
                    .onFailure {
                        showPoseNotDetected()

                        overlayController
                            .showNecklaceManually()
                    }
            }
        }
    }

    private fun showPoseNotDetected() {
        Toast.makeText(
            this,
            R.string.pose_not_detected,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showPoseDetectionError() {
        Toast.makeText(
            this,
            R.string.pose_detection_error,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun detectFaceAndPlaceEarrings(bitmap: Bitmap) {
        try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            val result = faceLandMarker?.detect(mpImage)

            if (result == null || result.faceLandmarks().isEmpty()) {
                Toast.makeText(
                    this,
                    "صورت در تصویر تشخیص داده نشد",
                    Toast.LENGTH_SHORT
                ).show()

                placeEarringsManually()
                return
            }

            val landmarks = result.faceLandmarks()[0]
            placeEarringsOnFace(landmarks)

        } catch (e: Exception) {
            e.printStackTrace()

            Toast.makeText(
                this,
                "خطا در تشخیص صورت برای گوشواره",
                Toast.LENGTH_SHORT
            ).show()

            placeEarringsManually()
        }
    }

    private fun placeEarringsOnFace(
        landmarks: List<NormalizedLandmark>
    ) {
        /*
         * نقاط استفاده‌شده:
         *
         * 234: کناره چپ صورت در تصویر
         * 172: بخش پایین فک در سمت چپ تصویر
         *
         * 454: کناره راست صورت در تصویر
         * 397: بخش پایین فک در سمت راست تصویر
         *
         * 33 و 263: کناره چشم‌ها برای محاسبه زاویه صورت
         */

        if (landmarks.size <= 454) {
            placeEarringsManually()
            return
        }

        val imageLeftSide = landmarkToParentPoint(landmarks[234])
        val imageLeftJaw = landmarkToParentPoint(landmarks[172])

        val imageRightSide = landmarkToParentPoint(landmarks[454])
        val imageRightJaw = landmarkToParentPoint(landmarks[397])

        val leftEye = landmarkToParentPoint(landmarks[33])
        val rightEye = landmarkToParentPoint(landmarks[263])

        val faceWidth = hypot(
            imageRightSide.x - imageLeftSide.x,
            imageRightSide.y - imageLeftSide.y
        ).coerceAtLeast(1f)

        /*
         * محل تقریبی لاله گوش با حرکت از کناره صورت
         * به سمت بخش پایین فک محاسبه می‌شود.
         */
        val jawInterpolation = 0.25f

        var leftEarX = lerp(
            imageLeftSide.x,
            imageLeftJaw.x,
            jawInterpolation
        )

        var leftEarY = lerp(
            imageLeftSide.y,
            imageLeftJaw.y,
            jawInterpolation
        )

        var rightEarX = lerp(
            imageRightSide.x,
            imageRightJaw.x,
            jawInterpolation
        )

        var rightEarY = lerp(
            imageRightSide.y,
            imageRightJaw.y,
            jawInterpolation
        )

        /*
         * کمی جابه‌جایی به بیرون صورت.
         */
        val outwardOffset = faceWidth * 0.05f

        leftEarX -= outwardOffset
        rightEarX += outwardOffset

        /*
         * کمی انتقال به پایین برای نزدیک‌شدن به لاله گوش.
         */
        val verticalOffset = faceWidth * -0.000f

        leftEarY += verticalOffset
        rightEarY += verticalOffset

        val faceAngle = Math.toDegrees(
            atan2(
                rightEye.y - leftEye.y,
                rightEye.x - leftEye.x
            ).toDouble()
        ).toFloat()

        showAndPositionEarrings(
            leftEarX = leftEarX,
            leftEarY = leftEarY,
            rightEarX = rightEarX,
            rightEarY = rightEarY,
            faceWidth = faceWidth,
            faceAngle = faceAngle
        )
    }

    private fun showAndPositionEarrings(
        leftEarX: Float,
        leftEarY: Float,
        rightEarX: Float,
        rightEarY: Float,
        faceWidth: Float,
        faceAngle: Float
    ) {
        binding.accessoryView.visibility = View.GONE

        binding.leftEarringView.visibility = View.VISIBLE
        binding.rightEarringView.visibility = View.VISIBLE

        binding.leftEarringView.post {
            val leftView = binding.leftEarringView
            val rightView = binding.rightEarringView

            val baseViewWidth = leftView.width
                .takeIf { it > 0 }
                ?: 1

            /*
             * عرض نمایشی هر گوشواره تقریباً ۱۸ درصد عرض صورت است.
             */
            autoScale = (faceWidth * 0.18f) / baseViewWidth

            applyAccessoryScale()

            /*
             * Pivot در بالای گوشواره قرار می‌گیرد تا نقطه اتصال
             * گوشواره روی محل تقریبی لاله گوش باقی بماند.
             */
            leftView.pivotX = leftView.width / 2f
            leftView.pivotY = 0f

            rightView.pivotX = rightView.width / 2f
            rightView.pivotY = 0f

            leftView.x = leftEarX - leftView.width / 2f
            leftView.y = leftEarY

            rightView.x = rightEarX - rightView.width / 2f
            rightView.y = rightEarY

            /*
             * مقدار کم زاویه برای هماهنگی نسبی با کجی سر.
             * برای گوشواره آویز بهتر است کاملاً برابر زاویه سر نباشد.
             */
            val earringRotation = faceAngle * 0.15f

            leftView.rotation = earringRotation
            rightView.rotation = earringRotation

            /*
             * تصویر سمت راست آینه می‌شود.
             * برای تصاویر کاملاً متقارن تفاوتی ایجاد نمی‌کند.
             */
            leftView.rotationY = 0f
            rightView.rotationY = 180f
        }
    }

    private fun placeEarringsManually() {
        binding.accessoryView.visibility = View.GONE

        binding.leftEarringView.visibility = View.VISIBLE
        binding.rightEarringView.visibility = View.VISIBLE

        binding.frameContainer.post {
            val parent = binding.frameContainer
            val leftView = binding.leftEarringView
            val rightView = binding.rightEarringView

            autoScale = 0.7f
            applyAccessoryScale()

            val centerX = parent.width / 2f
            val centerY = parent.height / 2f

            val horizontalDistance = parent.width * 0.22f

            leftView.x =
                centerX - horizontalDistance - leftView.width / 2f

            leftView.y =
                centerY - leftView.height / 2f

            rightView.x =
                centerX + horizontalDistance - rightView.width / 2f

            rightView.y =
                centerY - rightView.height / 2f

            leftView.rotation = 0f
            rightView.rotation = 0f

            leftView.rotationY = 0f
            rightView.rotationY = 180f
        }
    }

    private fun lerp(
        start: Float,
        end: Float,
        amount: Float
    ): Float {
        return start + (end - start) * amount
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
        showSingleAccessoryView()
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
        binding.leftEarringView.visibility = View.GONE
        binding.rightEarringView.visibility = View.GONE

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

        if (selectedAccessory == AccessoryType.EARRING) {
            binding.leftEarringView.scaleX = finalScale
            binding.leftEarringView.scaleY = finalScale

            binding.rightEarringView.scaleX = finalScale
            binding.rightEarringView.scaleY = finalScale
        } else {
            binding.accessoryView.scaleX = finalScale
            binding.accessoryView.scaleY = finalScale
        }
    }

    private fun showSingleAccessoryView() {
        binding.leftEarringView.visibility = View.GONE
        binding.rightEarringView.visibility = View.GONE
        binding.accessoryView.visibility = View.VISIBLE
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
        showSingleAccessoryView()
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
        showSingleAccessoryView()

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

}