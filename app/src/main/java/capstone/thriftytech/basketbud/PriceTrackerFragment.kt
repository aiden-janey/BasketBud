package capstone.thriftytech.basketbud

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import capstone.thriftytech.basketbud.databinding.ActivityCameraBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class PriceTrackerFragment : Fragment() {
    private lateinit var binding: ActivityCameraBinding
    private val user = Firebase.auth.currentUser

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