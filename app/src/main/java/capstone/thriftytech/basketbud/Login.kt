package capstone.thriftytech.basketbud

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import capstone.thriftytech.basketbud.databinding.ActivityLoginBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth: FirebaseAuth = Firebase.auth

    private var eTxtEmail: TextInputEditText? = null
    private var eTxtPass: TextInputEditText? = null
    private var progressBar: ProgressBar? = null

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        eTxtEmail = binding.email
        eTxtPass = binding.password
        progressBar = binding.progBar
    }

    fun logIntoAcct(view: View) {
        progressBar?.visibility = View.VISIBLE
        var email = ""
        var password = ""
        email = eTxtEmail?.text.toString()
        password = eTxtPass?.text.toString()

        //Check if fields are empty
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show()
        }
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show()
        }

        //Sign in
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                progressBar?.visibility = View.GONE
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(
                        this,
                        "Successful Login.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(
                        this,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

    fun toRegisterPage(view: View) {
        val intent = Intent(this, Register::class.java)
        startActivity(intent)
        finish()
    }
    fun forgotPassword(view: View) {}
}