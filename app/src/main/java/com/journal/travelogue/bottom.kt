package com.journal.travelogue

import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class Bottom : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bottom)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav_bar)
        val fab = findViewById<FloatingActionButton>(R.id.fab_middle)

        bottomNav.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val navHeight = bottomNav.height
                fab.translationY = -(navHeight / 2).toFloat()
                bottomNav.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        loadFragment(HomeFragment())

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> loadFragment(HomeFragment())
                R.id.profile -> loadFragment(ProfileFragment())
                R.id.saved -> loadFragment(SavedFragment())
                R.id.settings -> loadFragment(LogoutFragment())
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentloader, fragment)
            .commit()
        return true
    }
}
