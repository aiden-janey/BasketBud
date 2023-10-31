package capstone.thriftytech.basketbud

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import capstone.thriftytech.basketbud.data.Product
import capstone.thriftytech.basketbud.data.Store
import capstone.thriftytech.basketbud.databinding.ActivityCameraBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.ArrayList

class PriceTrackerFragment : Fragment() {
    private lateinit var binding: ActivityCameraBinding
    private val user = Firebase.auth.currentUser
    private val originalProductList = ArrayList<Product>()
    private val filteredProductList = ArrayList<Product>()

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

        val searchView: SearchView = requireView().findViewById(R.id.searchView)

        searchView.queryHint = "Search Products"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterProducts(newText ?: "")
                return true
            }
        })
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
                    val productsList = ArrayList<Product>()

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

                    originalProductList.clear()
                    originalProductList.addAll(productsList)
                }
                .addOnFailureListener { exception ->
                    val errorMessage = "Failed to retrieve product data: ${exception.message}"
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()

                    Log.e("FirestoreError", errorMessage)
                }
        }
    }

    private fun filterProducts(query: String) {
        val recyclerView: RecyclerView = requireView().findViewById(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(context)

        if (query.isBlank()) {
            val adapter = context?.let { ProductAdapter(it, originalProductList) }
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
        } else {
            val filteredList = originalProductList.filter { product ->
                product.prod_name?.contains(query, ignoreCase = true) == true
            }
            val adapter = context?.let { ProductAdapter(it, filteredProductList) }
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
        }

        recyclerView.adapter?.notifyDataSetChanged()
    }

}