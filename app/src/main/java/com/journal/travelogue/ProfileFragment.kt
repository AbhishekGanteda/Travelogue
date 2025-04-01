package com.journal.travelogue

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var travelAdapter: TravelAdapter
    private lateinit var travelList: MutableList<TravelItem>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        val activity = activity as? AppCompatActivity

        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav_bar)?.let { bottomNav ->
            bottomNav.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val navHeight = bottomNav.height

                    // Set bottom margin dynamically
                    val params = recyclerView.layoutParams as ViewGroup.MarginLayoutParams
                    params.bottomMargin = navHeight + 100
                    recyclerView.layoutParams = params

                    bottomNav.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }

        // Initialize RecyclerView with Horizontal Scrolling
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // Create sample data
        travelList = mutableListOf(

        )

        // Initialize Adapter with pageType "profile"
        travelAdapter = TravelAdapter(travelList, "profile")
        recyclerView.adapter = travelAdapter

        return view
    }
}
