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
import com.google.firebase.auth.ktx.auth
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
import java.text.ParseException
import java.util.Date

typealias LumaListener = (luma: Double) -> Unit

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore
    private lateinit var scanBtn: Button
    private var imgCapture: ImageCapture? = null
    private lateinit var camExecutor: ExecutorService
    private var storeTools = StoreTools()
    private var productTools = ProductTools()
    
    private lateinit var confirmButton: Button
    private lateinit var imagePreview: ImageView
    private lateinit var viewFinder: PreviewView
    private lateinit var backButton: Button

    // Initialize FirebaseStorage
    val storage = FirebaseStorage.getInstance()
    val storageReference = storage.reference


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

        scanBtn.setOnClickListener { scanReceipt() }

        backButton.setOnClickListener { navigateToHome() }
        
        camExecutor = Executors.newSingleThreadExecutor() 
    }

    private fun navigateToHome() {
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun uploadExpenseToFirebase(expense: Expense) {
        val expensesCollection = db.collection("expenses")

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
                        //luma->Log.d(TAG, "Average Luminosity $luma")
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

    private fun uploadImageToFirebase(imageUri: Uri, expense: Expense) {
        val imageRef = storageReference.child("receipts/${expense.receiptName}")
        val uploadTask = imageRef.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            // Image uploaded successfully
            // You can get the download URL if needed
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                // Handle successful upload
                Log.d(TAG, "ImageURL: $downloadUrl")

                // Create an Expense object
                val thisExpense = Expense(
                    expenseID = expense.expenseID,
                    receiptName = expense.receiptName,
                    imageUrl = downloadUrl,
                    purchaseDate = expense.purchaseDate,
                    purchaseTotal = expense.purchaseTotal,
                    store = expense.store,
                    userID = expense.userID
                )

                Log.d("Expense Data", "Expense Data (before Upload) $thisExpense")

                uploadExpenseToFirebase(thisExpense)
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

    private fun getRealPathFromURI(uri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                return it.getString(columnIndex)
            }
        }
        return uri.path ?: ""
    }

    private fun scanReceipt() {
        val imageCapture = imgCapture ?: return
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        val receiptName = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, receiptName)
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
                    //Text Extract
                    val img: InputImage = InputImage.fromFilePath(baseContext, output.savedUri!!)

                    recognizer.process(img)
                        .addOnSuccessListener {
                            Log.d("Receipt Data", it.text)
                            val text = it.text
                            var purchaseDate = ""
                            var storeName = ""
                            var purchaseTotal = 0.0

                            val store = Store(
                                storeTools.findAddress(it.text),
                                storeTools.findCity(it.text),
                                storeTools.findStore(it.text),
                                storeTools.findProv(it.text)
                            )

                            var date = productTools.findDate(text)
                            storeName = store.store_name.toString()
                            var storeId = addStore(store)

                            val userId = if(auth.currentUser != null){
                                auth.currentUser!!.uid
                            }else{
                                "No User Found"
                            }

                            for (block in it.textBlocks) {
                                for (line in block.lines) {
                                    val lineText = line.text

                                    Log.d("LineByLine", lineText)

                                    if(lineText.contains("SUBTOTAL", true) || lineText.contains("SUBTOTAL:"))
                                        break

                                    val product = Product(
                                        date,
                                        productTools.findName(lineText),
                                        productTools.findPrice(lineText),
                                        storeId,
                                        userId
                                    )

                                    addProduct(product)

                                }
                            }

                            //Expense Tracking
                            //Recognizing Purchase Total
                            val totalPattern = Regex("""(\d+\.\s?\d{2})\s?(CAD|\$|CAD\$\s?)?""") // Total amount pattern "(xx.xx CAD)", "($xx.xx)", or "CAD$ xx.xx"
                            val totalMatches = totalPattern.findAll(text)

                            var lastMatchedValue: String? = null

                            for (matchResult in totalMatches) {
                                lastMatchedValue = matchResult.groups[1]?.value
                            }

                            // Remove non-numeric characters and convert to a double
                            purchaseTotal = lastMatchedValue?.replace(Regex("[^\\d.]"), "")?.toDoubleOrNull() ?: 0.0

                            // Recognizing Purchase Date
                            val outputDateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)

                            var datePattern = Regex("""\b(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) \d{1,2}, \d{4}\b""", RegexOption.IGNORE_CASE) // Oct 29, 2023
                            var dateMatcher = datePattern.find(text)

                            if (dateMatcher == null) {
                                datePattern = Regex("""\d{2}/\d{2}/\d{4}""") // MM/dd/yyyy
                                dateMatcher = datePattern.find(text)
                            }

                            if (dateMatcher == null) {
                                datePattern = Regex("""\d{2}/\d{2}/\d{2}""") // yy/MM/dd
                                dateMatcher = datePattern.find(text)
                            }

                            var parsedDate: Date? = null

                            if (dateMatcher != null) {
                                val originalDate = dateMatcher.value
                                val inputDateFormat = if (originalDate.length == 10) {
                                    SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                                } else {
                                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                }

                                try {
                                    parsedDate = inputDateFormat.parse(originalDate)
                                    Log.d("Parsed Date", "Parsed Date: $parsedDate")
                                } catch (e: ParseException) {
                                    parsedDate = null
                                }
                            }

                            if (parsedDate != null) {
                                purchaseDate = outputDateFormat.format(parsedDate)
                            } else {
                                val currentDate = Date()
                                purchaseDate = outputDateFormat.format(currentDate)
                            }

                            Log.d("Purchase Total Captured", "Captured: $lastMatchedValue")
                            Log.d("Receipt Data After Scan", "Store Name: $storeName")
                            Log.d("Receipt Data After Scan", "Purchase Date: $purchaseDate")
                            Log.d("Receipt Data After Scan", "Purchase Total: $purchaseTotal")

                            val expense = Expense(null, receiptName, "image_url", purchaseDate, purchaseTotal, storeName, auth.currentUser?.uid)

                            //Confirm Receipt
                            val savedUri = output.savedUri!!

                            val imageFile = File(getRealPathFromURI(savedUri))
                            val takenImageBitMap = BitmapFactory.decodeFile(imageFile.absolutePath)
                            val rotatedBitmap = rotateBitmap(takenImageBitMap, imageFile)

                            viewFinder.visibility = View.GONE
                            scanBtn.visibility = View.GONE

                            imagePreview.visibility = View.VISIBLE
                            imagePreview.setImageBitmap(rotatedBitmap)

                            confirmButton.visibility = View.VISIBLE

                            confirmButton.setOnClickListener {
                                uploadImageToFirebase(savedUri, expense)
                                Toast.makeText(baseContext, "Receipt Saved", Toast.LENGTH_SHORT).show()
                                navigateToHome()
                            }

                            Log.d(TAG, "Receipt Data Stored")
                        }.addOnFailureListener {
                            Log.d(TAG, "Can't Store Receipt Data")
                        }
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        camExecutor.shutdown()
    }

    private fun addStore(store: Store): String{
        var storeId = "No StoreID Found"
        db.collection("stores").add(store).addOnSuccessListener {
            Log.d("Add Store", "${store.store_name} is Added.")
            storeId = it.id
        }.addOnFailureListener {
            Log.d("Add Store", "Cant Add ${store.store_name}.")
        }
        return storeId
    }

    private fun addProduct(product: Product){
        db.collection("products").add(product).addOnSuccessListener {
            Log.d("Add Product", "${product.prod_name} is Added.")
        }.addOnFailureListener {
            Log.d("Add Product", "Can't Add ${product.prod_name}.")
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
