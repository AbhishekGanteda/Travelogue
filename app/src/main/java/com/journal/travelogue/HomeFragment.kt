package com.journal.travelogue

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import android.widget.ImageView

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.journal.travelogue.api.RetrofitClient
import com.journal.travelogue.models.Follow
import com.journal.travelogue.models.Post
import com.journal.travelogue.models.Save
import com.journal.travelogue.models.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {
    private lateinit var backButton: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var travelAdapter: TravelAdapter
    private lateinit var travelList: MutableList<TravelItem>
    private var user: User? = null
    private lateinit var sharedPreferences: android.content.SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        backButton = view.findViewById(R.id.backButton)

        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }



        sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Activity.MODE_PRIVATE)
        loadUserData()

        recyclerView = view.findViewById(R.id.recyclerView)
        val activity = activity as? AppCompatActivity

        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav_bar)?.let { bottomNav ->
            bottomNav.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val navHeight = bottomNav.height

                    // Set bottom margin dynamically
                    val params = recyclerView.layoutParams as ViewGroup.MarginLayoutParams
                    params.bottomMargin = navHeight
                    recyclerView.layoutParams = params

                    bottomNav.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }

        // Initialize RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        getAllPosts(user?.id)
        // Create sample data
        travelList = mutableListOf()

        // Initialize Adapter with pageType "home"
        travelAdapter = TravelAdapter(travelList, "home")
        recyclerView.adapter = travelAdapter

        return view
    }

    private fun loadUserData() {
        val userJson = sharedPreferences.getString("USER", null)
        user = userJson?.let { Gson().fromJson(it, User::class.java) }
        Log.d("UserCheck", "Loaded User: ${Gson().toJson(user)}")
    }

    private fun showToast(message: String) {
        context?.let {
            Toast.makeText(it, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun getAllPosts(userId: Int?) {
        val apiService = RetrofitClient.instance
        apiService.getAllPosts(userId).enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                if (response.isSuccessful) {
                    val posts = response.body()
                    posts?.let {
                        for (post in it) {
                            fetchUserDetails(post)
                        }
                    }
                } else {
                    println("Failed to fetch posts")
                }
            }
            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                println("Error: ${t.message}")
            }
        })
    }

    private fun fetchUserDetails(post: Post) {
        val apiService = RetrofitClient.instance
        apiService.getUserById(post.user_id).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val postUser = response.body()
                    postUser?.let {
                        postLikedOrNot(post.id, user?.id) { isLiked ->
                            postSavedOrNot(post.id, user?.id) { isSaved ->
                                getSavedCount(post.id) { savedCount ->
                                    getLikesCount(post.id) { likesCount ->
                                        checkFollow(it) { isFollow ->
                                            updateTravelList(post, it, isLiked, isSaved, savedCount, likesCount, isFollow)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    println("Failed to fetch user for userId: ${post.user_id}")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                println("Error fetching user: ${t.message}")
            }
        })
    }

    private fun updateTravelList(post: Post, user: User, isLiked: Boolean, isSaved: Boolean, savedCount: Int, likesCount: Int, isFollow : Boolean) {
            val newTravelItem = TravelItem(
                userId = user.id,
                postId = post.id,
                userName = user.name,
                profileImageRes = user.profile_image,
                travelDescription = post.description,
                placeImageRes = post.image,
                placeName = post.place_name,
                isFollowed = isFollow,
                isLiked = isLiked,
                likeCount = likesCount,
                isSaved = isSaved,
                savedCount = savedCount,
                latitude = post.latitude,
                longitude = post.longitude
            )

            // Add the new item to the travel list and refresh RecyclerView
            travelList.add(newTravelItem)
            travelAdapter.notifyDataSetChanged() // Refresh RecyclerView
    }


    private fun postLikedOrNot(postId: Int?, userId: Int?, callback: (Boolean) -> Unit) {
        val apiService = RetrofitClient.instance

        apiService.postLikedOrNot(postId, userId).enqueue(object : Callback<Map<String, Boolean>> {
            override fun onResponse(call: Call<Map<String, Boolean>>, response: Response<Map<String, Boolean>>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val isLiked = responseBody?.get("isLiked") ?: false
                    callback(isLiked)
                } else {
                    println("Failed to check like status")
                    callback(false)
                }
            }

            override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) {
                println("Error: ${t.message}")
                callback(false)
            }
        })
    }

    private fun postSavedOrNot(postId: Int?, userId: Int?, callback: (Boolean) -> Unit) {
        val apiService = RetrofitClient.instance

        apiService.postSavedOrNot(postId, userId).enqueue(object : Callback<Map<String, Boolean>> {
            override fun onResponse(call: Call<Map<String, Boolean>>, response: Response<Map<String, Boolean>>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val isSaved = responseBody?.get("isSaved") ?: false
                    callback(isSaved)
                } else {
                    println("Failed to check like status")
                    callback(false)
                }
            }

            override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) {
                println("Error: ${t.message}")
                callback(false) // Return false if there's an error
            }
        })
    }

    private fun getLikesCount(postId: Int?, callback: (Int) -> Unit) {
        val apiService = RetrofitClient.instance

        apiService.getLikesCount(postId).enqueue(object : Callback<Int> {
            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                if (response.isSuccessful) {
                    val likesCount : Int = response.body() ?: 0 // Default to 0 if body is null
                    println("Likes Count: $likesCount")
                    callback(likesCount) // Pass the like count to the callback
                } else {
                    println("Failed to fetch like count")
                    callback(0) // Return 0 in case of failure
                }
            }

            override fun onFailure(call: Call<Int>, t: Throwable) {
                println("Error: ${t.message}")
                callback(0) // Return 0 if there's an error
            }
        })
    }

    private fun getSavedCount(postId: Int?, callback: (Int) -> Unit) {
        val apiService = RetrofitClient.instance

        apiService.getSavedCount(postId).enqueue(object : Callback<Int> {
            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                if (response.isSuccessful) {
                    val savedCount : Int = response.body() ?: 0 // Default to 0 if body is null
                    println("Saved Count: $savedCount")
                    callback(savedCount) // Pass the like count to the callback
                } else {
                    println("Failed to fetch saved count")
                    callback(0) // Return 0 in case of failure
                }
            }

            override fun onFailure(call: Call<Int>, t: Throwable) {
                println("Error: ${t.message}")
                callback(0) // Return 0 if there's an error
            }
        })
    }

    private fun checkFollow(postUser: User, callback: (Boolean) -> Unit) {
        val apiService = RetrofitClient.instance

        apiService.checkFollow(user?.id,postUser.id).enqueue(object : Callback<Map<String, Boolean>> {
            override fun onResponse(call: Call<Map<String, Boolean>>, response: Response<Map<String, Boolean>>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val isFollow = responseBody?.get("isFollow") ?: false
                    callback(isFollow)
                } else {
                    println("Failed to fetch saved count")
                    callback(false)
                }
            }

            override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) {
                println("Error: ${t.message}")
                callback(false)
            }
        })
    }

}
