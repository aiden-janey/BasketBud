package capstone.thriftytech.basketbud

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.content.ContentValues
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import capstone.thriftytech.basketbud.data.Product
import capstone.thriftytech.basketbud.data.Store
import capstone.thriftytech.basketbud.tools.ProductTools
import capstone.thriftytech.basketbud.tools.StoreTools
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import capstone.thriftytech.basketbud.data.Expense
import capstone.thriftytech.basketbud.databinding.ActivityCameraBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.io.File
import com.google.mlkit.vision.common.InputImage
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Locale

typealias LumaListener = (luma: Double) -> Unit

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore
    private lateinit var scanBtn: Button
    private var imgCapture: ImageCapture? = null
    private lateinit var camExecutor: ExecutorService
    private lateinit var storeTools: StoreTools
    private lateinit var productTools: ProductTools
    
    private lateinit var confirmButton: Button
    private lateinit var imagePreview: ImageView
    private lateinit var viewFinder: PreviewView
    private lateinit var backButton: Button

    // Set up the camera
    private lateinit var outputDirectory: File
    
   

    // Initialize FirebaseFirestore
    //private val db = FirebaseFirestore.getInstance()

    // Initialize FirebaseStorage
    val storage = FirebaseStorage.getInstance()
    val storageReference = storage.reference

    //    private lateinit var storeTools: StoreTools

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
        confirmButton = binding.confirmButton
        imagePreview = binding.imagePreview
        viewFinder = binding.viewFinder
        backButton = binding.backButton

        if(permissionsGranted())
            startCamera()
        else
            activityResultLauncher.launch(REQUIRED_PERMISSIONS)

        outputDirectory = getOutputDirectory()

        scanBtn.setOnClickListener { scanReceipt() }
        backButton.setOnClickListener {
            navigateToHome()
        }
        
        camExecutor = Executors.newSingleThreadExecutor() 
    }

    private fun navigateToHome() {
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun uploadExpenseToFirebase(expense: Expense) {
        val expensesCollection = db.collection("expenses")
8
        // Add a new document with a generated ID
        expensesCollection
            .add(expense)
            .addOnSuccessListener { documentReference ->
                // Document added successfully
                println("Expense added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                // Error adding document
                println("Error adding expense: $e")
            }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
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

    private fun uploadImageToFirebase(imageUri: Uri, userID: String) {
        val imageRef = storageReference.child("receipts/${imageUri.lastPathSegment}")
        val uploadTask = imageRef.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            // Image uploaded successfully
            // You can get the download URL if needed
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                // Handle successful upload
                Log.d(TAG, "ImageURL: $downloadUrl")

                // Create an Expense object
                val expense = Expense(
                    imageUrl = downloadUrl,
                    store = "NO FRILLS",
                    purchaseDate = "04/12/2023",
                    purchaseTotal = 24.06,
                    userID = userID
                )

                uploadExpenseToFirebase(expense)
            }
        }.addOnFailureListener {
            // Handle unsuccessful upload
            Log.e(TAG, "Image upload failed")
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, imageFile: File): Bitmap {
        val exif = ExifInterface(imageFile.absolutePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun scanReceipt() {
        val imageCapture = imgCapture ?: return
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())

        val photoFile = File(outputDirectory, "$name.jpg")

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P)
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
        }

        //val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            .build()
        

        // imageCapture.takePicture(
        //     outputOptions,
        //     ContextCompat.getMainExecutor(this),
        //     object : ImageCapture.OnImageSavedCallback {
        //         override fun onError(err: ImageCaptureException) {
        //             Log.e(TAG, "Photo capture failed: ${err.message}", err)
        //         }
        //         override fun onImageSaved(output: ImageCapture.OutputFileResults){
        //             val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
        //             val takenImageBitMap = BitmapFactory.decodeFile(photoFile.absolutePath)
        //             val rotatedBitmap = rotateBitmap(takenImageBitMap, photoFile)
        //             viewFinder.visibility = View.GONE
        //             scanBtn.visibility = View.GONE
        //             imagePreview.visibility = View.VISIBLE
        //             imagePreview.setImageBitmap(rotatedBitmap)
        //             confirmButton.visibility = View.VISIBLE

        //             confirmButton.setOnClickListener {
        //                 uploadImageToFirebase(savedUri, auth.currentUser!!.uid)
        //                 Toast.makeText(baseContext, "Receipt Saved", Toast.LENGTH_SHORT).show()
        //                 navigateToHome()
        //             }
        //         }
        //     }
        // )
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(err: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${err.message}", err)
                }
                override fun onImageSaved(output: ImageCapture.OutputFileResults){
                    val img: InputImage = InputImage.fromFilePath(baseContext, output.savedUri!!)

                    recognizer.process(img)
                        .addOnSuccessListener {
                            Log.d("Receipt Data", it.text)
                            val store = Store(
                                storeTools.findAddress(it.text),
                                storeTools.findCity(it.text),
                                storeTools.findStore(it.text),
                                storeTools.findProv(it.text)
                            )

                            var date = "2023/10/29"
                            addStore(store)

                            val userId = if(auth.currentUser != null){
                                auth.currentUser
                            }else{
                                "No User Found"
                            }

                            for (block in it.textBlocks) {
                                for (line in block.lines) {
                                    val lineText = line.text
                                    date = productTools.findDate(lineText)
                                    val product = Product(
                                        date,
                                        productTools.findName(lineText),
                                        productTools.findPrice(lineText),
                                        getStoreId(store),
                                        userId.toString()
                                    )
                                    addProduct(product)
                                }
                            }
                            Toast.makeText(baseContext, "Receipt Saved", Toast.LENGTH_SHORT).show()
                            goToMain()
                        }.addOnFailureListener {
                            Toast.makeText(baseContext, "Save Failed", Toast.LENGTH_SHORT).show()
                            goToMain()
                        }
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        camExecutor.shutdown()
    }

    private fun goToMain(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun addStore(store: Store){
        db.collection("stores").add(store).addOnSuccessListener {
            Toast.makeText(this, "${store.store_name} is Added.", Toast.LENGTH_SHORT)
        }.addOnFailureListener {
            Toast.makeText(this, "Unable to add ${store.store_name}.", Toast.LENGTH_SHORT)
        }
    }

    fun getStoreId(store: Store): String{
        db.collection("stores").whereEqualTo("store_name", store.store_name).get().addOnSuccessListener {
            //get id
        }
        return "store123"
    }

    private fun addProduct(product: Product){
        db.collection("products").add(product).addOnSuccessListener {
            Toast.makeText(this, "${product.prod_name} is Added.", Toast.LENGTH_SHORT)
        }.addOnFailureListener {
            Toast.makeText(this, "Unable to add ${product.prod_name}.", Toast.LENGTH_SHORT)
        }
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
