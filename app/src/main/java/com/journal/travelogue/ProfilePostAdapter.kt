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
import com.journal.travelogue.FriendsAdapter.FriendViewHolder
import com.journal.travelogue.api.RetrofitClient
import com.journal.travelogue.models.Post
import com.journal.travelogue.models.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfilePostAdapter(private val postList: MutableList<ProfilePostItem>):
    RecyclerView.Adapter<ProfilePostAdapter.ProfilePostViewHolder>(){

    private var user: User? = null
    private lateinit var sharedPreferences: android.content.SharedPreferences
    val ip = RetrofitClient.ip

    class ProfilePostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val postContainer: LinearLayout = view.findViewById(R.id.profile_post)
        val placeImage: ImageView = view.findViewById(R.id.place_image)
        val placeName : TextView = view.findViewById(R.id.place_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilePostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.profile_post, parent, false)
        sharedPreferences = view.context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        loadUserData()
        return ProfilePostViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfilePostViewHolder, position: Int) {
        val postItem = postList[position]

        Glide.with(holder.itemView.context)
            .load("http://$ip:5000/uploads/" + postItem.placeImageRes)
            .placeholder(R.drawable.noimage)
            .error(R.drawable.noimage)
            .into(holder.placeImage)

        holder.placeName.text = postItem.placeName

        getPostById(postItem.postId) { post ->
            getUserById(post.user_id) { postUser ->
                holder.postContainer.setOnClickListener {
                    val intent = Intent(holder.itemView.context, SelfFullPost::class.java)
                    intent.putExtra("postId", post.id)
                    intent.putExtra("userName", postUser.name)
                    intent.putExtra("profileImage", postUser.profile_image)
                    intent.putExtra("placeImage", post.image)
                    intent.putExtra("placeName", post.place_name)
                    intent.putExtra("description", post.description)
                    intent.putExtra("latitude", post.latitude)
                    intent.putExtra("longitude", post.longitude)

                    holder.itemView.context.startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount(): Int = postList.size

    private fun loadUserData() {
        val userJson = sharedPreferences.getString("USER", null)
        user = userJson?.let { Gson().fromJson(it, User::class.java) }
        Log.d("UserCheck", "Loaded User: ${Gson().toJson(user)}")
    }

    private fun getPostById(id: Int?, callback: (Post) -> Unit) {
        val apiService = RetrofitClient.instance
        apiService.getPostById(id).enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (response.isSuccessful) {
                    response.body()?.let { post ->
                        callback(post)
                    }
                } else {
                    Log.e("API", "Failed to fetch post")
                }
            }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                Log.e("API", "Error: ${t.message}")
            }
        })
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


}