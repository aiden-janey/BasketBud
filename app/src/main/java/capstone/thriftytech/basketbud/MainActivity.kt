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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val auth: FirebaseAuth = Firebase.auth
    var txtUser: TextView? = null
    var user: FirebaseUser? = null
    lateinit var drawer: DrawerLayout
    lateinit var actionBar: ActionBarDrawerToggle
    lateinit var navView: NavigationView
    lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        txtUser = binding.userDetails
        user = auth.currentUser
        drawer = binding.drawLayout
        actionBar = ActionBarDrawerToggle(this, drawer, R.string.nav_open, R.string.nav_close)
        navView = binding.navView
        bottomNavigationView = binding.bottomNavigation

        replaceFragment(HomeFragment())

        bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.home -> replaceFragment(HomeFragment())
                R.id.basket -> replaceFragment(BasketFragment())
                R.id.monthly_budget -> replaceFragment(MonthlyBudgetFragment())
                R.id.notification -> replaceFragment(NotificationFragment())
            }
            true
        }

        drawer.addDrawerListener(actionBar)
        actionBar.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if(user == null){
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }else {
            txtUser?.text = user?.email
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.commit()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBar.onOptionsItemSelected(item)){
            true
        }else if(item.itemId == R.id.nav_logout){
            logoutOfAcct()
            true
        } else
            super.onOptionsItemSelected(item)
    }

    private fun logoutOfAcct() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
        Toast.makeText(this, "Successful Logout", Toast.LENGTH_SHORT).show()
    }

    private fun openCamera(){
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
        Toast.makeText(this, "Successful Logout", Toast.LENGTH_SHORT).show()
    }
}