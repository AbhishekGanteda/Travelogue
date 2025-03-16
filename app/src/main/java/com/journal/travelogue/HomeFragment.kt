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

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var travelAdapter: TravelAdapter
    private lateinit var travelList: MutableList<TravelItem>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

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

        // Initialize RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Create sample data
        travelList = mutableListOf(
            TravelItem(
                userName = "John Doe",
                profileImageRes = R.drawable.profile1,
                travelDescription = "Exploring the beauty of Paris!",
                placeImageRes = R.drawable.paris,
                placeName = "Eiffel Tower, Paris",
                isLiked = false,
                isSaved = false,
                latitude = 48.8584,
                longitude = 2.2945
            ),
            TravelItem(
                userName = "Alice Smith",
                profileImageRes = R.drawable.profile2,
                travelDescription = "World's tallest building!",
                placeImageRes = R.drawable.dubai,
                placeName = "Burj Khalifa, Dubai",
                isLiked = true,
                isSaved = false,
                latitude = 34.0259,
                longitude = -118.7798
            )
        )

        // Initialize Adapter with pageType "home"
        travelAdapter = TravelAdapter(travelList, "home")
        recyclerView.adapter = travelAdapter

        return view
    }
}
