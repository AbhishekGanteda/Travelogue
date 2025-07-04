package com.journal.travelogue

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.journal.travelogue.api.RetrofitClient
import com.journal.travelogue.api.RetrofitClient.ip
import com.journal.travelogue.models.Follow
import com.journal.travelogue.models.Post
import com.journal.travelogue.models.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FullProfile : AppCompatActivity() {
    private var user: User? = null
    private lateinit var backButton : ImageView

    private lateinit var sharedPreferences: android.content.SharedPreferences
    private lateinit var recyclerView2: RecyclerView
    private lateinit var postAdapter: ProfilePostAdapter
    private lateinit var postList: MutableList<ProfilePostItem>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_full_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        loadUserData()

        recyclerView2 = findViewById(R.id.postsGrid)
        recyclerView2.layoutManager = GridLayoutManager(this, 2)
        // Create sample data
        postList = mutableListOf()

        // Initialize Adapter with pageType "home"
        postAdapter = ProfilePostAdapter(postList)
        recyclerView2.adapter = postAdapter

        val userId = intent.getIntExtra("userId", -1)
        val userName = intent.getStringExtra("userName")
        val profileImage = intent.getStringExtra("profileImage")
        val postsCount = intent.getStringExtra("postsCount")
        val follow = intent.getBooleanExtra("follow",true)

        getAllPosts(userId)

        val userNameTV = findViewById<TextView>(R.id.userName)
        val profileImageV = findViewById<ImageView>(R.id.userImage)
        val followersCountTV = findViewById<TextView>(R.id.followersCount)
        val followingCountTV = findViewById<TextView>(R.id.followingCount)
        val postsCountTV = findViewById<TextView>(R.id.posts)
        val followButton = findViewById<Button>(R.id.follow)
        backButton = findViewById(R.id.backButton)

        userNameTV.text = userName
        getFollowersCount(userId) { count ->
            followersCountTV.text = count.toString()
        }

        getFollowingCount(userId) { count ->
            followingCountTV.text = count.toString()
        }

        backButton.setOnClickListener {
            finish()
        }

        postsCountTV.text = postsCount

        Glide.with(this)
            .load("http://$ip:5000"+profileImage)
            .placeholder(R.drawable.profile)
            .error(R.drawable.profile)
            .into(profileImageV)

        if (follow) {
            followButton.text = "Unfollow"
        }
        else {
            followButton.text = "Follow"
        }

        followButton.setOnClickListener {
            if (followButton.text.toString().equals("Unfollow", ignoreCase = true)){
                removeFromFollowerTable(userId)
                followButton.text = "Follow"
            }
            else{
                addToFollowerTable(userId)
                followButton.text = "Unfollow"
            }

        }
    }

    private fun loadUserData() {
        val userJson = sharedPreferences.getString("USER", null)
        user = userJson?.let { Gson().fromJson(it, User::class.java) }
        Log.d("UserCheck", "Loaded User: ${Gson().toJson(user)}")
    }

    private fun addToFollowerTable(friendId : Int?) {
        val apiService = RetrofitClient.instance
        val details = mapOf<String,Int?>(
            "follower_id" to user?.id,
            "following_id" to friendId
        )
        apiService.addToFollowerTable(details).enqueue(object : Callback<Follow> {
            override fun onResponse(call: Call<Follow>, response: Response<Follow>) {
                if (response.isSuccessful) {
                    val follow = response.body()
                } else {
                    println("Failed to fetch saved count")
                }
            }

            override fun onFailure(call: Call<Follow>, t: Throwable) {
                println("Error: ${t.message}")
            }
        })
    }

    private fun removeFromFollowerTable(friendId : Int?) {
        val apiService = RetrofitClient.instance

        apiService.removeFromFollowerTable(user?.id,friendId).enqueue(object :
            Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val message = response.body()
                } else {
                    println("Failed to fetch saved count")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                println("Error: ${t.message}")
            }
        })
    }

    private fun getAllPosts(userId: Int?) {
        val apiService = RetrofitClient.instance
        apiService.getAllUserPosts(userId).enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                if (response.isSuccessful) {
                    val posts = response.body()
                    posts?.let {
                        for (post in it) {
                            updatePostList(post)
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

    private fun updatePostList(post: Post) {
        val newTravelItem = ProfilePostItem(
            postId = post.id,
            placeImageRes = post.image,
            placeName = post.place_name,
        )

        postList.add(newTravelItem)
        postAdapter.notifyDataSetChanged()
    }

    private fun getFollowersCount(userId: Int, callback: (Int) -> Unit){
        val apiService = RetrofitClient.instance

        apiService.getFollowersCount(userId).enqueue(object : Callback<Int> {
            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                if (response.isSuccessful) {
                    val count : Int = response.body() ?: 0
                    callback(count)
                } else {
                    println("Failed to fetch followers count")
                    callback(0)
                }
            }

            override fun onFailure(call: Call<Int>, t: Throwable) {
                println("Error: ${t.message}")
                callback(0)
            }
        })
    }

    private fun getFollowingCount(userId: Int, callback: (Int) -> Unit){
        val apiService = RetrofitClient.instance

        apiService.getFollowingCount(userId).enqueue(object : Callback<Int> {
            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                if (response.isSuccessful) {
                    val count : Int = response.body() ?: 0
                    callback(count)
                } else {
                    println("Failed to fetch following count")
                    callback(0)
                }
            }

            override fun onFailure(call: Call<Int>, t: Throwable) {
                println("Error: ${t.message}")
                callback(0)
            }
        })
    }
}