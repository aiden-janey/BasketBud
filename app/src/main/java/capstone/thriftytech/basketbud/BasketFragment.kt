package capstone.thriftytech.basketbud

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import capstone.thriftytech.basketbud.data.BasketItem
import capstone.thriftytech.basketbud.databinding.ActivityCameraBinding
import capstone.thriftytech.basketbud.databinding.FragmentBasketBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class BasketFragment : Fragment() {
    //private lateinit var binding: ActivityCameraBinding
    private val user = Firebase.auth.currentUser
    //initialize viewmodel to fragment
    private val viewModel: BasketItemViewModel by activityViewModels {
        BasketViewModelFactory(
            (activity?.application as BasketItemApplication).database.basketItemDao()
        )
    }
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
        //initialize adpater
        val adapter = BasketListAdapter {
            Log.d("onClick", it.itemName + " clicked")
        }
        //build recycler list items
        binding.recyclerView.adapter = adapter
        //use observable list for change in data
        viewModel.allItems.observe(this.viewLifecycleOwner) { items ->
            items.let {
                adapter.submitList(it)
            }
        }
        //add item to database
        binding.itemAdd.setOnClickListener {
            if (viewModel.isEntryValid(binding.addItemTextView.text.toString())) {
                viewModel.insertItem(BasketItem(0, binding.addItemTextView.text.toString()))
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
}