package com.journal.travelogue

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.material.bottomnavigation.BottomNavigationView

class SavedFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_saved, container, false)

        val scrollView = view.findViewById<ScrollView>(R.id.scroll_view_saved) // Update with your ScrollView ID
        val activity = activity as? AppCompatActivity

        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav_bar)?.let { bottomNav ->
            bottomNav.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val navHeight = bottomNav.height

                    // Set bottom margin dynamically
                    val params = scrollView.layoutParams as ViewGroup.MarginLayoutParams
                    params.bottomMargin = navHeight+100
                    scrollView.layoutParams = params

                    bottomNav.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }

        return view
    }
}
