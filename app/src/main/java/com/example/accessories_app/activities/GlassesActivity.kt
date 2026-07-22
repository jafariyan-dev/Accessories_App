package com.example.accessories_app.activities

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.accessories_app.R
import com.example.accessories_app.databinding.ActivityGlassesBinding
import com.example.accessories_app.domain.Product
import com.example.accessories_app.tryon.detector.FaceLandmarkDetector
import com.example.accessories_app.tryon.detector.HandLandmarkDetector
import com.example.accessories_app.tryon.detector.PoseLandmarkDetector
import com.example.accessories_app.tryon.image.TryOnImageRepository
import com.example.accessories_app.tryon.model.AccessoryType
import com.example.accessories_app.tryon.model.AccessoryTypeMapper
import com.example.accessories_app.tryon.placement.BraceletPlacementCalculator
import com.example.accessories_app.tryon.placement.EarringPlacementCalculator
import com.example.accessories_app.tryon.placement.GlassesPlacementCalculator
import com.example.accessories_app.tryon.placement.NecklacePlacementCalculator
import com.example.accessories_app.tryon.placement.RingPlacementCalculator
import com.example.accessories_app.tryon.ui.AccessoryOverlayController
import com.example.accessories_app.tryon.ui.DragTouchListener
import com.example.accessories_app.tryon.ui.LandmarkCoordinateMapper
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import java.util.concurrent.Executors

class GlassesActivity : AppCompatActivity() {

    private lateinit var binding:
            ActivityGlassesBinding

    private lateinit var overlayController:
            AccessoryOverlayController

    private lateinit var coordinateMapper:
            LandmarkCoordinateMapper

    private lateinit var imageRepository:
            TryOnImageRepository

    private var faceDetector:
            FaceLandmarkDetector? = null

    private var handDetector:
            HandLandmarkDetector? = null

    private var poseDetector:
            PoseLandmarkDetector? = null

    private val glassesPlacementCalculator =
        GlassesPlacementCalculator()

    private val earringPlacementCalculator =
        EarringPlacementCalculator()

    private val necklacePlacementCalculator =
        NecklacePlacementCalculator()

    private val braceletPlacementCalculator =
        BraceletPlacementCalculator()

    private val ringPlacementCalculator =
        RingPlacementCalculator()

    private var selectedAccessory =
        AccessoryType.UNKNOWN

    private val detectionExecutor =
        Executors.newSingleThreadExecutor()

    private val pickImageLauncher =
        registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            uri?.let(::loadSelectedImage)
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityGlassesBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)

        val product =
            readSelectedProduct()

        if (product == null) {
            Toast.makeText(
                this,
                R.string.product_not_selected,
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        selectedAccessory =
            AccessoryTypeMapper.from(product)

        setupTryOnComponents()
        setupScaleSeekBar()
        setupClickListeners()
        loadProductImage(product)

        /*
         * اگر نمی‌خواهی انتخاب عکس هنگام ورود
         * خودکار باز شود، این بخش را حذف کن.
         */
        binding.root.post {
            openImagePicker()
        }
    }

    private fun setupTryOnComponents() {
        overlayController =
            AccessoryOverlayController(
                container =
                binding.frameContainer,
                singleAccessoryView =
                binding.accessoryView,
                leftEarringView =
                binding.leftEarringView,
                rightEarringView =
                binding.rightEarringView
            )

        coordinateMapper =
            LandmarkCoordinateMapper(
                binding.mainImage
            )

        imageRepository =
            TryOnImageRepository(this)

        setupDetectorsSafely()
        setupDraggableViews()
    }

    private fun setupDetectorsSafely() {
        try {
            when (selectedAccessory) {
                AccessoryType.GLASSES,
                AccessoryType.EARRING -> {
                    faceDetector =
                        FaceLandmarkDetector(this)
                }

                AccessoryType.RING,
                AccessoryType.BRACELET,
                AccessoryType.WATCH -> {
                    handDetector =
                        HandLandmarkDetector(this)
                }

                AccessoryType.NECKLACE -> {
                    poseDetector =
                        PoseLandmarkDetector(this)
                }

                AccessoryType.UNKNOWN -> Unit
            }
        } catch (error: UnsatisfiedLinkError) {
            error.printStackTrace()
        } catch (error: Exception) {
            error.printStackTrace()
        }
    }

    private fun setupDraggableViews() {
        binding.accessoryView.setOnTouchListener(
            DragTouchListener()
        )

        binding.leftEarringView.setOnTouchListener(
            DragTouchListener()
        )

        binding.rightEarringView.setOnTouchListener(
            DragTouchListener()
        )
    }

    private fun setupScaleSeekBar() {
        /*
         * با فرمول پایین، progress برابر 50
         * یعنی scale برابر 1.
         */
        binding.sizeSeekBar.progress = 50

        binding.sizeSeekBar
            .setOnSeekBarChangeListener(
                object :
                    SeekBar.OnSeekBarChangeListener {

                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        val scale =
                            0.5f +
                                    progress / 100f

                        overlayController
                            .setUserScale(scale)
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

    private fun setupClickListeners() {
        binding.pickButton.setOnClickListener {
            openImagePicker()
        }
    }

    private fun openImagePicker() {
        pickImageLauncher.launch(
            PickVisualMediaRequest(
                ActivityResultContracts
                    .PickVisualMedia
                    .ImageOnly
            )
        )
    }

    private fun loadSelectedImage(
        uri: Uri
    ) {
        detectionExecutor.execute {
            val result = runCatching {
                imageRepository
                    .copyAndDecode(uri)
            }

            runOnUiThread {
                if (isFinishing || isDestroyed) {
                    return@runOnUiThread
                }

                result
                    .onSuccess { bitmap ->
                        binding.mainImage
                            .setImageBitmap(bitmap)

                        binding.mainImage.post {
                            processImage(bitmap)
                        }
                    }
                    .onFailure { error ->
                        error.printStackTrace()

                        Toast.makeText(
                            this,
                            R.string.unknown_error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }

    private fun processImage(
        bitmap: Bitmap
    ) {
        when (selectedAccessory) {
            AccessoryType.GLASSES ->
                processGlasses(bitmap)

            AccessoryType.EARRING ->
                processEarring(bitmap)

            AccessoryType.NECKLACE ->
                processNecklace(bitmap)

            AccessoryType.RING ->
                processRing(bitmap)

            AccessoryType.BRACELET,
            AccessoryType.WATCH ->
                processBracelet(bitmap)

            AccessoryType.UNKNOWN ->
                overlayController
                    .showSingleManually()
        }
    }

    private fun processGlasses(
        bitmap: Bitmap
    ) {
        val detector = faceDetector

        if (detector == null) {
            showFaceNotDetected()
            overlayController.showSingleManually()
            return
        }

        executeLandmarkDetection(
            detect = {
                detector.detect(bitmap)
            },
            onSuccess = { landmarks ->
                val placement =
                    glassesPlacementCalculator.calculate(
                        landmarks = landmarks,
                        mapLandmark =
                        coordinateMapper::map
                    )

                if (placement == null) {
                    showFaceNotDetected()
                    overlayController
                        .showSingleManually()
                } else {
                    overlayController
                        .showSingleAccessory(
                            placement
                        )
                }
            },
            onFailure = {
                showFaceNotDetected()
                overlayController
                    .showSingleManually()
            }
        )
    }

    private fun processEarring(
        bitmap: Bitmap
    ) {
        val detector = faceDetector

        if (detector == null) {
            showFaceNotDetected()
            overlayController
                .showEarringsManually()
            return
        }

        executeLandmarkDetection(
            detect = {
                detector.detect(bitmap)
            },
            onSuccess = { landmarks ->
                val placement =
                    earringPlacementCalculator.calculate(
                        landmarks = landmarks,
                        mapLandmark =
                        coordinateMapper::map
                    )

                if (placement == null) {
                    showFaceNotDetected()
                    overlayController
                        .showEarringsManually()
                } else {
                    overlayController
                        .showEarrings(placement)
                }
            },
            onFailure = {
                showFaceNotDetected()
                overlayController
                    .showEarringsManually()
            }
        )
    }

    private fun processNecklace(
        bitmap: Bitmap
    ) {
        val detector = poseDetector

        if (detector == null) {
            showPoseNotDetected()
            overlayController
                .showNecklaceManually()
            return
        }

        executeLandmarkDetection(
            detect = {
                detector.detect(bitmap)
            },
            onSuccess = { landmarks ->
                val placement =
                    necklacePlacementCalculator.calculate(
                        landmarks = landmarks,
                        mapLandmark =
                        coordinateMapper::map
                    )

                if (placement == null) {
                    showPoseNotDetected()
                    overlayController
                        .showNecklaceManually()
                } else {
                    overlayController
                        .showNecklace(placement)
                }
            },
            onFailure = {
                showPoseNotDetected()
                overlayController
                    .showNecklaceManually()
            }
        )
    }

    private fun processRing(
        bitmap: Bitmap
    ) {
        val detector = handDetector

        if (detector == null) {
            showHandNotDetected()
            overlayController
                .showSingleManually()
            return
        }

        executeLandmarkDetection(
            detect = {
                detector.detect(bitmap)
            },
            onSuccess = { landmarks ->
                val placement =
                    ringPlacementCalculator.calculate(
                        landmarks = landmarks,
                        mapLandmark =
                        coordinateMapper::map
                    )

                if (placement == null) {
                    showHandNotDetected()
                    overlayController
                        .showSingleManually()
                } else {
                    overlayController
                        .showSingleAccessory(
                            placement
                        )
                }
            },
            onFailure = {
                showHandNotDetected()
                overlayController
                    .showSingleManually()
            }
        )
    }

    private fun processBracelet(
        bitmap: Bitmap
    ) {
        val detector = handDetector

        if (detector == null) {
            showHandNotDetected()
            overlayController
                .showSingleManually()
            return
        }

        executeLandmarkDetection(
            detect = {
                detector.detect(bitmap)
            },
            onSuccess = { landmarks ->
                val placement =
                    braceletPlacementCalculator.calculate(
                        landmarks = landmarks,
                        mapLandmark =
                        coordinateMapper::map
                    )

                if (placement == null) {
                    showHandNotDetected()
                    overlayController
                        .showSingleManually()
                } else {
                    overlayController
                        .showSingleAccessory(
                            placement
                        )
                }
            },
            onFailure = {
                showHandNotDetected()
                overlayController
                    .showSingleManually()
            }
        )
    }

    private fun executeLandmarkDetection(
        detect:
            () -> Result<
                List<NormalizedLandmark>
                >,
        onSuccess:
            (List<NormalizedLandmark>) -> Unit,
        onFailure:
            (Throwable) -> Unit
    ) {
        detectionExecutor.execute {
            val result = try {
                detect()
            } catch (error: Throwable) {
                Result.failure(error)
            }

            runOnUiThread {
                if (isFinishing || isDestroyed) {
                    return@runOnUiThread
                }

                result.fold(
                    onSuccess = onSuccess,
                    onFailure = onFailure
                )
            }
        }
    }

    private fun loadProductImage(
        product: Product
    ) {
        if (
            selectedAccessory ==
            AccessoryType.EARRING
        ) {
            binding.accessoryView.visibility =
                View.GONE

            binding.leftEarringView.visibility =
                View.INVISIBLE

            binding.rightEarringView.visibility =
                View.INVISIBLE

            Glide.with(this)
                .load(product.PhotoUrl)
                .into(binding.leftEarringView)

            Glide.with(this)
                .load(product.PhotoUrl)
                .into(binding.rightEarringView)
        } else {
            binding.leftEarringView.visibility =
                View.GONE

            binding.rightEarringView.visibility =
                View.GONE

            binding.accessoryView.visibility =
                View.VISIBLE

            Glide.with(this)
                .load(product.PhotoUrl)
                .into(binding.accessoryView)
        }
    }

    private fun showFaceNotDetected() {
        Toast.makeText(
            this,
            R.string.face_not_detected,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showHandNotDetected() {
        Toast.makeText(
            this,
            R.string.hand_not_detected,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showPoseNotDetected() {
        Toast.makeText(
            this,
            R.string.pose_not_detected,
            Toast.LENGTH_SHORT
        ).show()
    }

    @Suppress("DEPRECATION")
    private fun readSelectedProduct():
            Product? {
        return if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {
            intent.getSerializableExtra(
                EXTRA_PRODUCT,
                Product::class.java
            )
        } else {
            intent.getSerializableExtra(
                EXTRA_PRODUCT
            ) as? Product
        }
    }

    override fun onDestroy() {
        faceDetector?.close()
        faceDetector = null

        handDetector?.close()
        handDetector = null

        poseDetector?.close()
        poseDetector = null

        detectionExecutor.shutdownNow()

        super.onDestroy()
    }

    private companion object {
        const val EXTRA_PRODUCT = "product"
    }
}