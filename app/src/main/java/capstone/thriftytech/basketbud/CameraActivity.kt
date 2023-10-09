package capstone.thriftytech.basketbud

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.camera.core.ImageCapture
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.Manifest
import android.content.ContentValues
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.core.Preview
import androidx.camera.core.CameraSelector
import android.util.Log
import android.widget.Button
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import capstone.thriftytech.basketbud.databinding.ActivityCameraBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Locale

typealias LumaListener = (luma: Double) -> Unit

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private val auth: FirebaseAuth = Firebase.auth
    private lateinit var txtUser: TextView
    private var user: FirebaseUser? = null
    private lateinit var drawer: DrawerLayout
    private lateinit var actionBar: ActionBarDrawerToggle
    private lateinit var navView: NavigationView
    private lateinit var scanBtn: Button
    private var imgCapture: ImageCapture? = null
    private lateinit var camExecutor: ExecutorService

    //Checks for Permissions to use the Camera before starting
    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted)
                Toast.makeText(baseContext, "Permission request denied", Toast.LENGTH_SHORT).show()
            else
                startCamera()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        scanBtn = binding.scanBtn

        if(permissionsGranted())
            startCamera()
        else
            activityResultLauncher.launch(REQUIRED_PERMISSIONS)

        scanBtn.setOnClickListener { scanReceipt() }
        camExecutor = Executors.newSingleThreadExecutor()

    }

    private fun permissionsGranted() = REQUIRED_PERMISSIONS.all{
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val camProv: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }
            imgCapture = ImageCapture.Builder().build()

            val imgAnalyze = ImageAnalysis.Builder().build().also{
                it.setAnalyzer(camExecutor, LuminosityAnalyzer{
                        luma->Log.d(TAG, "Average Luminosity $luma")
                })
            }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                camProv.unbindAll()
                camProv.bindToLifecycle(
                    this, cameraSelector, preview, imgCapture, imgAnalyze)
            } catch(err: Exception) {
                Log.e(TAG, "Use case binding failed", err)
            }
        }, ContextCompat.getMainExecutor(this))


    }

    private fun scanReceipt() {
        val imageCapture = imgCapture ?: return
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P)
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")

        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(err: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${err.message}", err)
                }
                override fun onImageSaved(output: ImageCapture.OutputFileResults){
                    val img: InputImage = InputImage.fromFilePath(baseContext, output.savedUri!!)

                    val result = recognizer.process(img)
                        .addOnSuccessListener {
                            val resultText = it.text
                            for (block in it.textBlocks) {
                                val blockText = block.text
                                val blockCornerPoints = block.cornerPoints
                                val blockFrame = block.boundingBox
                                for (line in block.lines) {
                                    val lineText = line.text
                                    val lineCornerPoints = line.cornerPoints
                                    val lineFrame = line.boundingBox
                                    for (element in line.elements) {
                                        val elementText = element.text
                                        val elementCornerPoints = element.cornerPoints
                                        val elementFrame = element.boundingBox
                                    }
                                }
                            }
                            Toast.makeText(baseContext, "Receipt Saved", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, resultText)
                        }.addOnFailureListener {
                            Toast.makeText(baseContext, "Fail Save", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        camExecutor.shutdown()
    }

    companion object {
        private const val TAG = "BasketBud"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P)
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }.toTypedArray()
    }
}

private class LuminosityAnalyzer(private val listener: LumaListener): ImageAnalysis.Analyzer{
    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        val data = ByteArray(remaining())
        get(data)
        return data
    }
    override fun analyze(image: ImageProxy) {
        val buffer = image.planes[0].buffer
        val data = buffer.toByteArray()
        val pixels = data.map { it.toInt() and 0xFF }
        val luma = pixels.average()
        listener(luma)
        image.close()
    }
}