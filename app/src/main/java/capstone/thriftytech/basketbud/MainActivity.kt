package capstone.thriftytech.basketbud

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import capstone.thriftytech.basketbud.databinding.ActivityMainBinding
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val auth: FirebaseAuth = Firebase.auth
    private lateinit var txtUser: TextView
    private var user: FirebaseUser? = null
//    private lateinit var drawer: DrawerLayout
//    private lateinit var actionBar: ActionBarDrawerToggle
    private lateinit var navView: NavigationView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var scanBtn: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomNavigationView = binding.bottomNavigationView
        bottomNavigationView.background = null
        bottomNavigationView.menu.getItem(2).isEnabled = false

//        txtUser = binding.userDetails
        user = auth.currentUser
//        drawer = binding.drawLayout
//        actionBar = ActionBarDrawerToggle(this, drawer, R.string.nav_open, R.string.nav_close)
//        navView = binding.navView
        scanBtn = binding.scanBtn


//        drawer.addDrawerListener(actionBar)
//        actionBar.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Check if user is logged in
        if (user == null) {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }

        scanBtn.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
            finish()
        }

        replaceFragment(HomeFragment())

        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> replaceFragment(HomeFragment())
                R.id.basket -> replaceFragment(BasketFragment())
                R.id.priceTracker -> replaceFragment(PriceTrackerFragment())
                R.id.account -> replaceFragment(AccountFragment())
            }
            true
        }

    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.commit()
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        var result = if (actionBar.onOptionsItemSelected(item)) {
//            true
//        }else{
//            super.onOptionsItemSelected(item)
//        }
//        return result
//    }

    fun logoutOfAcct(view: View) {
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(this, "Successful Logout", Toast.LENGTH_SHORT).show()
    }
}