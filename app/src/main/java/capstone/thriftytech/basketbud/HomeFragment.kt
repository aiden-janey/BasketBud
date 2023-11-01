package capstone.thriftytech.basketbud

import android.content.Intent
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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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

    private val expensesCollection = firestore.collection("expenses")
    private val pageSize = 7L // Adjust this based on your requirements
    private var lastVisibleDocument: DocumentSnapshot? = null

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

        getInitialExpenseData()

        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh)
        swipeRefreshLayout.setOnRefreshListener {
            getInitialExpenseData()
            swipeRefreshLayout.isRefreshing = false
        }

        // Implement infinite scrolling to load more expenses as the user scrolls
        expenseRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                // Load more expenses when reaching the end of the list
                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    loadMoreExpenseData()
                }
            }
        })
    }

    private fun getInitialExpenseData() {
        val uid = user?.uid
        val query = expensesCollection
            .whereEqualTo("userID", uid)
            .orderBy("purchaseDate", Query.Direction.DESCENDING)
            .limit(pageSize)

        query.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // Handle the error
                return@addSnapshotListener
            }

            expenseList.clear()

            snapshot?.documents?.forEach { documentSnapshot ->
                val data = documentSnapshot.data
                val imageUrl = data?.get("imageUrl") as String
                val purchaseDate = data["purchaseDate"] as String
                val purchaseTotal = (data["purchaseTotal"] as Double?) ?: 0.0
                val store = data["store"] as String

                val expense = Expense(imageUrl, purchaseDate, purchaseTotal, store, uid)
                expenseList.add(expense)
            }

            if (expenseList.isEmpty()) {
                noReceiptTV.visibility = View.VISIBLE
            } else {
                val adapter = ExpenseAdapter(expenseList)
                adapter.setOnItemClickListener(object : ExpenseAdapter.OnItemClickListener {
                    override fun onItemClick(expense: Expense) {
                        val intent = Intent(requireContext(), ExpenseDetails::class.java)
                        intent.putExtra("imageUrl", expense.imageUrl)
                        intent.putExtra("purchaseDate", expense.purchaseDate)
                        intent.putExtra("purchaseTotal", expense.purchaseTotal)
                        intent.putExtra("store", expense.store)
                        startActivity(intent)
                    }
                })

                expenseRecyclerView.adapter = adapter
                expenseRecyclerView.visibility = View.VISIBLE
            }

            if (snapshot != null && !snapshot.isEmpty) {
                lastVisibleDocument = snapshot.documents[snapshot.size() - 1]
            }
        }
    }

    private fun loadMoreExpenseData() {
        val uid = user?.uid
        val query = expensesCollection
            .whereEqualTo("userID", uid)
            .orderBy("purchaseDate", Query.Direction.DESCENDING)
            .startAfter(lastVisibleDocument)
            .limit(pageSize)

        query.get().addOnSuccessListener { querySnapshot ->
            val newExpenses = querySnapshot.documents.mapNotNull { document ->
                val data = document.data
                val imageUrl = data?.get("imageUrl") as String
                val purchaseDate = data["purchaseDate"] as String
                val purchaseTotal = (data["purchaseTotal"] as Double?) ?: 0.0
                val store = data["store"] as String

                Expense(imageUrl, purchaseDate, purchaseTotal, store, uid)
            }

            expenseList.addAll(newExpenses)
            lastVisibleDocument = querySnapshot.documents.lastOrNull()

            // Notify the adapter that the data set has changed
            expenseRecyclerView.adapter?.notifyDataSetChanged()
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