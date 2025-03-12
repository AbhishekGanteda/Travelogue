package com.journal.travelogue

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class bottom : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bottom)
        val bottomnav=findViewById<BottomNavigationView>(R.id.bottom_nav_bar)
        loadFragment(HomeFragment())
        bottomnav.setOnItemSelectedListener {
                item->when(item.itemId){
            R.id.home->loadFragment(HomeFragment())
            R.id.profile->loadFragment(ProfileFragment())
            R.id.saved->loadFragment(SavedFragment())
            R.id.logout->loadFragment(LogoutFragment())

        }
            true
        }
    }

    private fun loadFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentloader,fragment)
            .commit()
        return true
    }
}