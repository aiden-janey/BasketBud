package capstone.thriftytech.basketbud

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import capstone.thriftytech.basketbud.data.BasketItem
import capstone.thriftytech.basketbud.data.BasketListBank
import capstone.thriftytech.basketbud.databinding.FragmentBasketBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage


class BasketFragment : Fragment() {
    //private lateinit var binding: ActivityCameraBinding
    private val user = Firebase.auth.currentUser
    //initialize viewmodel to fragment
    private val viewModel: BasketItemViewModel by activityViewModels {
        BasketViewModelFactory(
            (activity?.application as BasketItemApplication).database.basketItemDao()
        )
    }
    //initialize work bank array
    private var basketListBank = BasketListBank().listArray

    //initialize view binding for Basket Fragment
    private var _binding: FragmentBasketBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_basket, container, false)
        _binding = FragmentBasketBinding.inflate(inflater, container, false)
        return binding.root
    }
    //set user on fragment access, set recycler view adapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showUserName()
        //initialize recyclerView adapter
        val listAdapter = BasketListAdapter {

        }
        //build recycler list items
        binding.recyclerView.adapter = listAdapter
        //use observable list for change in data
        viewModel.allItems.observe(this.viewLifecycleOwner) { items ->
            items.let {
                listAdapter.submitList(it)
            }
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this.context)
        //add item to database
        binding.itemAdd.setOnClickListener {
            if (viewModel.isEntryValid(binding.addItemTextView.text.toString())) {
                viewModel.insertItem(BasketItem(0, binding.addItemTextView.text.toString()))
                //reset textview
                binding.addItemTextView.text.clear()
            }
        }
        //adapter for autocomplete
        val bankAdapter = ArrayAdapter(
            requireContext(),
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            basketListBank
        )
        binding.addItemTextView.setAdapter(bankAdapter)
        //delete items
        binding.deleteSelected.setOnClickListener {
            if (!listAdapter.isSelectable) {
                context?.let { it1 ->
                    MaterialAlertDialogBuilder(it1)
                        .setTitle("No items selected")
                        .setMessage("Select items first")
                        .setNegativeButton("Ok"){_,_ ->}
                        .show()
                }
                Log.d("Debug", "Show Toast")
            } else {
                context?.let { it1 ->
                    MaterialAlertDialogBuilder(it1)
                        .setTitle("Delete item permanently")
                        .setMessage("Are you sure you want to delete these item?")
                        .setPositiveButton("Yes"){_,_ ->
                            for (item in listAdapter.selectedItems) {
                                viewModel.deleteItem(item)
                            }
                            //disable select mode
                            listAdapter.isSelectable = false
                        }
                        .setNegativeButton("No"){_,_ ->
                            Toast.makeText(context,"canceled" ,Toast.LENGTH_SHORT).show()
                        }
                        .show()
                }
            }
        }
    }

    //set show delete
    private fun showDelete(boolean: Boolean) {
        if (boolean) {
            binding.deleteSelected.isEnabled = true
            binding.deleteSelected.visibility = View.VISIBLE
        } else {
            binding.deleteSelected.isEnabled = false
            binding.deleteSelected.visibility = View.GONE
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
}
