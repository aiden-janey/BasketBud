package capstone.thriftytech.basketbud

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import capstone.thriftytech.basketbud.data.Expense
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {
    //Firebase
    private val user = Firebase.auth.currentUser
    private val firestore = FirebaseFirestore.getInstance()

    //UI's
    private lateinit var noReceiptTV: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var expenseRecyclerView: RecyclerView

    //Class
    private lateinit var expenseList: ArrayList<Expense>

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //UI housekeeping
        showUserName()
        initializeUIBindings()

        //RecyclerView
        expenseRecyclerView = view.findViewById(R.id.expenseRV)
        expenseRecyclerView.layoutManager = LinearLayoutManager(this.activity)

        expenseList = arrayListOf<Expense>()

        getExpenseData()

        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh)
        swipeRefreshLayout.setOnRefreshListener {
            getExpenseData()
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun getExpenseData() {
//        expenseRecyclerView.visibility = View.GONE

        val uid = user?.uid
        val expensesCollection = firestore.collection("expenses")
        val query = expensesCollection.whereEqualTo("userID", uid)

        query.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // Handle the error
                return@addSnapshotListener
            }

            expenseList.clear()

            snapshot?.forEach { documentSnapshot ->
                val data = documentSnapshot.data
                val imageUrl = data["imageUrl"] as String
                val purchaseDate = data["purchaseDate"] as String
                val purchaseTotal = (data["purchaseTotal"] as Double?) ?: 0.0
                val store = data["store"] as String

                val expense = Expense(imageUrl, purchaseDate, purchaseTotal, store, uid)
                expenseList.add(expense)
            }

            if(expenseList.isEmpty()) {
                noReceiptTV.visibility = View.GONE
            }
            else {
                val adapter = ExpenseAdapter(expenseList)
                expenseRecyclerView.adapter = adapter
                expenseRecyclerView.visibility = View.VISIBLE
            }
        }
    }

    private fun initializeUIBindings() {
        noReceiptTV = requireView().findViewById(R.id.noReceiptTV)
    }

    private fun showUserName() {
        user?.reload()
        val userNameTV: TextView = requireView().findViewById(R.id.userNameTV)
        val email = user!!.email
        val userName = user.displayName

        val name = if (userName == null || userName == ""){
            val splitValue = email?.split("@")
            splitValue?.get(0).toString()
        }else{
            userName
        }

        userNameTV.text = "Hello, ${name}"
    }
}