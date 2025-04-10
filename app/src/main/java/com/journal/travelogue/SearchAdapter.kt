package com.journal.travelogue

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.journal.travelogue.api.RetrofitClient
import com.journal.travelogue.models.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchAdapter(private val searchList: MutableList<SearchItem>):
    RecyclerView.Adapter<SearchAdapter.SearchViewHolder>(){
    private var user: User? = null
    private lateinit var sharedPreferences: android.content.SharedPreferences
    val ip = RetrofitClient.ip

    class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val searchItemContainer : LinearLayout = view.findViewById(R.id.search_item)
        val profile: ImageView = view.findViewById(R.id.profile)
        val name: TextView = view.findViewById(R.id.name)
        val place: TextView = view.findViewById(R.id.place)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.search_item, parent, false)
        sharedPreferences = view.context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        loadUserData()
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val searchItem = searchList[position]

        Glide.with(holder.itemView.context)
            .load("http://$ip:5000" + searchItem.profile)
            .placeholder(R.drawable.profile)
            .error(R.drawable.profile)
            .into(holder.profile)

        holder.name.text = searchItem.name
        holder.place.text = searchItem.place_name

        getUserById(searchItem.userId) { postUser ->
            holder.searchItemContainer.setOnClickListener {
                val intent = Intent(holder.itemView.context, FullPost::class.java)
                intent.putExtra("postId", searchItem.postId)
                intent.putExtra("userName", postUser.name)
                intent.putExtra("profileImage", postUser.profile_image)
                intent.putExtra("placeImage", searchItem.placeImage)
                intent.putExtra("placeName", searchItem.place_name)
                intent.putExtra("description", searchItem.description)

                holder.itemView.context.startActivity(intent)
            }
        }
        holder.profile.setOnClickListener {
            checkFollow(searchItem.userId) { follow ->
                fetchFriendDetails(searchItem) { friend ->
                    getUserPostsCount(friend) { postsCount ->
                        val intent = Intent(holder.itemView.context, FullProfile::class.java).apply {
                            putExtra("userId", friend.id)
                            putExtra("userName", friend.name)
                            putExtra("profileImage", friend.profile_image)
                            putExtra("postsCount", postsCount.toString())
                            putExtra("follow", follow)
                        }
                        holder.itemView.context.startActivity(intent)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = searchList.size

    private fun loadUserData() {
        val userJson = sharedPreferences.getString("USER", null)
        user = userJson?.let { Gson().fromJson(it, User::class.java) }
        Log.d("UserCheck", "Loaded User: ${Gson().toJson(user)}")
    }

    private fun getUserById(id: Int?, callback: (User) -> Unit) {
        val apiService = RetrofitClient.instance
        apiService.getUserById(id).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    response.body()?.let { post ->
                        callback(post)
                    }
                } else {
                    Log.e("API", "Failed to fetch post")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("API", "Error: ${t.message}")
            }
        })
    }

    private fun fetchFriendDetails(searchItem: SearchItem, callback: (User) -> Unit) {
        val apiService = RetrofitClient.instance
        apiService.getUserById(searchItem.userId).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val postUser = response.body()
                    if (postUser != null) {
                        callback(postUser)
                    } else {
                        println("Failed to fetch user for userId: ${searchItem.userId}")
                    }
                } else {
                    println("Failed to fetch user for userId: ${searchItem.userId}")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                println("Error fetching user: ${t.message}")
            }
        })
    }


    private fun getUserPostsCount(user: User, callback: (Int) -> Unit){
        val apiService = RetrofitClient.instance

        apiService.getUserPostsCount(user.id).enqueue(object : Callback<Int> {
            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                if (response.isSuccessful) {
                    val count : Int = response.body() ?: 0
                    callback(count)
                } else {
                    println("Failed to fetch posts count")
                    callback(0)
                }
            }

            override fun onFailure(call: Call<Int>, t: Throwable) {
                println("Error: ${t.message}")
                callback(0)
            }
        })
    }

    private fun checkFollow(friendId : Int?, callback: (Boolean) -> Unit) {
        val apiService = RetrofitClient.instance

        apiService.checkFollow(user?.id,friendId).enqueue(object : Callback<Map<String, Boolean>> {
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