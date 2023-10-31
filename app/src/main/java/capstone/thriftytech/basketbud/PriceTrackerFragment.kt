package capstone.thriftytech.basketbud

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import capstone.thriftytech.basketbud.data.Product
import capstone.thriftytech.basketbud.data.Store
import capstone.thriftytech.basketbud.databinding.ActivityCameraBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class PriceTrackerFragment : Fragment() {
    private lateinit var binding: ActivityCameraBinding
    private val user = Firebase.auth.currentUser
    private val productsList = ArrayList<Product>()
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_price_tracker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showUserName()
        fetchUserProducts()

        searchView = requireView().findViewById(R.id.searchView)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })
    }

    private fun filterList(query : String?) {
        val recyclerView: RecyclerView =
            requireView().findViewById(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(context)
        val adapter = context?.let { ProductAdapter(it, productsList) }
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        if (query != null) {
            val filteredList = ArrayList<Product>()
            for (i in productsList) {
                if (i.prod_name?.lowercase(Locale.ROOT)?.contains(query) == true) {
                    filteredList.add(i)
                }
            }

            if (filteredList.isEmpty()) {
                Toast.makeText(context, "No Product Found", Toast.LENGTH_SHORT).show()
            } else {
                adapter?.setFilteredList(filteredList)
            }
        }
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

    private fun fetchUserProducts() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserUid != null) {
            val firestore = FirebaseFirestore.getInstance()

            firestore.collection("products")
                .whereEqualTo("user_id", currentUserUid)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        val product = document.toObject(Product::class.java)
                        val storeId = product?.store_id

                        if (storeId != null) {
                            firestore.collection("stores").document(storeId).get()
                                .addOnSuccessListener { storeDocument ->
                                    val store = storeDocument.toObject(Store::class.java)

                                    val productItem = Product(
                                        product.buy_date ?: "",
                                        product.prod_name ?: "",
                                        product.prod_price ?: "",
                                        store?.store_name ?: ""
                                    )

                                    productsList.add(productItem)

                                    val recyclerView: RecyclerView =
                                        requireView().findViewById(R.id.recyclerView)
                                    val layoutManager = LinearLayoutManager(context)
                                    val adapter = context?.let { ProductAdapter(it, productsList) }
                                    recyclerView.layoutManager = layoutManager
                                    recyclerView.adapter = adapter

                                    adapter?.notifyDataSetChanged()
                                }
                                .addOnFailureListener { exception ->
                                    val errorMessage = "Failed to retrieve store data: ${exception.message}"
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()

                                    Log.e("FirestoreError", "Failed to retrieve store data: ${exception.message}")
                                }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    val errorMessage = "Failed to retrieve product data: ${exception.message}"
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()

                    Log.e("FirestoreError", errorMessage)
                }
        }
    }
}