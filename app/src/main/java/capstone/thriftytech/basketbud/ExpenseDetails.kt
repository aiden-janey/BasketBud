package capstone.thriftytech.basketbud

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale

class ExpenseDetails : AppCompatActivity() {
    private lateinit var purchaseDateDetailsTV: TextView
    private lateinit var storeDetailsTV: TextView
    private lateinit var purchaseTotalDetailsTV: TextView
    private lateinit var receiptDetailsIV: ImageView
    private lateinit var backButton: ImageButton
    private lateinit var deleteButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_details)

        initializeUIBindings()

        backButton.setOnClickListener { finish() }

        setExpenseDetailsView()

        deleteExpenseConfirmation()
    }

    private fun deleteExpenseFromFirebase(expenseID: String) {
        val user = Firebase.auth.currentUser
        val uid = user?.uid

        if (uid != null) {
            val firestore = FirebaseFirestore.getInstance()
            val expensesCollection = firestore.collection("expenses").document(uid)

            expensesCollection.collection("expenses")
                .document(expenseID)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Transaction Data Deleted", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Deleting Err ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun deleteExpenseConfirmation() {
        val alertBox = AlertDialog.Builder(this)
        deleteButton.setOnClickListener {
            alertBox.setTitle("Are you sure?")
            alertBox.setMessage("Do you want to delete this expense?")
            alertBox.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                deleteExpenseFromFirebase(intent.getStringExtra("expenseID").toString())
            }
            alertBox.setNegativeButton("No") { _: DialogInterface, _: Int -> }
            alertBox.show()
        }
    }

    private fun setExpenseDetailsView() {
        val imageUrl = intent.getStringExtra("imageUrl")
        val purchaseDate = intent.getStringExtra("purchaseDate")
        val purchaseTotal = intent.getDoubleExtra("purchaseTotal", 0.0)
        val store = intent.getStringExtra("store")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val parsedDate = dateFormat.parse(purchaseDate)

        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormatter.format(parsedDate)

        purchaseDateDetailsTV.text = formattedDate

        storeDetailsTV.text = store
        purchaseTotalDetailsTV.text = purchaseTotal.toString()

        Picasso.get().load(imageUrl).rotate(90f).into(receiptDetailsIV)
    }

    private fun initializeUIBindings(){
        purchaseDateDetailsTV = findViewById(R.id.purchaseDateDetailsTV)
        storeDetailsTV = findViewById(R.id.storeDetailsTV)
        purchaseTotalDetailsTV = findViewById(R.id.purchaseTotalDetailsTV)
        receiptDetailsIV = findViewById(R.id.receiptDetailsIV)
        backButton = findViewById(R.id.expense_details_back_button)
        deleteButton = findViewById(R.id.expense_details_delete_button)
    }
}