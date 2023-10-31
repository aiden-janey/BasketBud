package capstone.thriftytech.basketbud

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class ExpenseDetails : AppCompatActivity() {
    private lateinit var purchaseDateDetailsTV: TextView
    private lateinit var storeDetailsTV: TextView
    private lateinit var purchaseTotalDetailsTV: TextView
    private lateinit var receiptDetailsIV: ImageView
    private lateinit var backButton: Button
    private lateinit var deleteButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_details)

        initializeUIBindings()

        backButton.setOnClickListener { finish() }
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