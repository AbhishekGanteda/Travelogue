package com.journal.travelogue

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.journal.travelogue.api.RetrofitClient
import com.journal.travelogue.models.Post
import com.journal.travelogue.models.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var searchList: MutableList<SearchItem>
    private lateinit var filteredList: MutableList<SearchItem>
    private var user: User? = null
    private lateinit var sharedPreferences: android.content.SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Activity.MODE_PRIVATE)
        loadUserData()

        searchView = view.findViewById(R.id.searchView)
        recyclerView = view.findViewById(R.id.recyclerView)

        val activity = activity as? AppCompatActivity
        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav_bar)?.let { bottomNav ->
            bottomNav.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val navHeight = bottomNav.height
                    val params = recyclerView.layoutParams as ViewGroup.MarginLayoutParams
                    params.bottomMargin = navHeight
                    recyclerView.layoutParams = params
                    bottomNav.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        searchList = mutableListOf()
        filteredList = mutableListOf()
        searchAdapter = SearchAdapter(filteredList)
        recyclerView.adapter = searchAdapter

        getAllPosts(user?.id)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterSearchItems(newText.orEmpty())
                return true
            }
        })

        return view
    }

    private fun loadUserData() {
        val userJson = sharedPreferences.getString("USER", null)
        user = userJson?.let { Gson().fromJson(it, User::class.java) }
        Log.d("UserCheck", "Loaded User: ${Gson().toJson(user)}")
    }

    private fun getAllPosts(userId: Int?) {
        val apiService = RetrofitClient.instance
        apiService.getAllPosts(userId).enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                if (response.isSuccessful) {
                    response.body()?.forEach { post ->
                        fetchUserDetails(post)
                    }
                } else {
                    Log.e("ProfileFragment", "Failed to fetch posts")
                }
            }

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                Log.e("ProfileFragment", "Error: ${t.message}")
            }
        })
    }

    private fun fetchUserDetails(post: Post) {
        val apiService = RetrofitClient.instance
        apiService.getUserById(post.user_id).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        updateTravelList(post, user)
                    }
                } else {
                    Log.e("ProfileFragment", "Failed to fetch user for userId: ${post.user_id}")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("ProfileFragment", "Error fetching user: ${t.message}")
            }
        })
    }

    private fun updateTravelList(post: Post, postUser: User) {
        val newSearchItem = SearchItem(
            userId = postUser.id,
            postId = post.id,
            name = postUser.name,
            profile = postUser.profile_image,
            place_name = post.place_name,
            description = post.description,
            placeImage = post.image
        )

        searchList.add(newSearchItem)
        filterSearchItems(searchView.query.toString())
    }

    private fun filterSearchItems(query: String) {
        val lowerQuery = query.lowercase()
        filteredList.clear()
        filteredList.addAll(
            searchList.filter {
                (it.name?.lowercase()?.contains(lowerQuery) == true) ||
                        (it.place_name?.lowercase()?.contains(lowerQuery) == true)
            }
        )
        searchAdapter.notifyDataSetChanged()
    }
}
