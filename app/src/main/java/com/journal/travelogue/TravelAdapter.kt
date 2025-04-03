package com.journal.travelogue

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.journal.travelogue.api.RetrofitClient
import com.journal.travelogue.models.Follow
import com.journal.travelogue.models.Like
import com.journal.travelogue.models.Save
import com.journal.travelogue.models.User
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TravelAdapter(private val travelList: MutableList<TravelItem>,
                    private val pageType: String ) :
    RecyclerView.Adapter<TravelAdapter.TravelViewHolder>() {
    private var user: User? = null
    private lateinit var sharedPreferences: android.content.SharedPreferences
    val ip = RetrofitClient.ip

    class TravelViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.card_view)
        val profileImage: ImageView = view.findViewById(R.id.profile)
        val userName: TextView = view.findViewById(R.id.user_name)
        val travelDescription: TextView = view.findViewById(R.id.travel_description)
        val placeImage: ImageView = view.findViewById(R.id.image)
        val placeName: TextView = view.findViewById(R.id.place_name)
        val likeIcon: ImageView = view.findViewById(R.id.like)
        val saveIcon: ImageView = view.findViewById(R.id.save)
        val editIcon: ImageView = view.findViewById(R.id.edit)
        val deleteIcon: ImageView = view.findViewById(R.id.delete)
        val locationIcon: ImageView = view.findViewById(R.id.location)
        val likeContainer: LinearLayout = view.findViewById(R.id.likeContainer)
        val savedContainer: LinearLayout = view.findViewById(R.id.savedContainer)
        val locationContainer: LinearLayout = view.findViewById(R.id.locationContainer)
        var likeText : TextView = view.findViewById(R.id.like_count)
        var saveText : TextView = view.findViewById(R.id.save_count)
        var followButton : ImageView = view.findViewById(R.id.followButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TravelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.travel_card, parent, false)
        sharedPreferences = view.context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        loadUserData()
        return TravelViewHolder(view)
    }

    override fun onBindViewHolder(holder: TravelViewHolder, position: Int) {
        val travelItem = travelList[position]

        holder.userName.text = travelItem.userName
        holder.travelDescription.text = travelItem.travelDescription?.let {
            if (it.length > 80) it.substring(0, 80) + "..." else it
        } ?: ""


        holder.placeName.text = travelItem.placeName
        holder.likeText.text = travelItem.likeCount.toString()
        holder.saveText.text = travelItem.savedCount.toString()

        Picasso.get()
            .load("http://$ip:5000" + travelItem.profileImageRes) // Use URL instead of resource ID
            .placeholder(R.drawable.profile)
            .error(R.drawable.profile)
            .into(holder.profileImage)

        Picasso.get()
            .load("http://$ip:5000/uploads/" + travelItem.placeImageRes) // Use URL instead of resource ID
            .placeholder(R.drawable.noimage)
            .error(R.drawable.noimage)
            .into(holder.placeImage)

        holder.profileImage.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.profile_border)

        // Toggle Like & Save icons dynamically
        holder.likeIcon.setImageResource(if (travelItem.isLiked ?: false) R.drawable.liked else R.drawable.like)
        holder.saveIcon.setImageResource(if (travelItem.isSaved ?: false) R.drawable.saved else R.drawable.save)
        holder.followButton.setImageResource(if (travelItem.isFollowed ?: false) R.drawable.followed else R.drawable.follow)

        // Handle Like action
        holder.likeIcon.setOnClickListener {
            travelItem.isLiked = !(travelItem.isLiked ?: false)

            // Update Like Count Immediately
            if (travelItem.isLiked == true) {
                travelItem.likeCount = (travelItem.likeCount ?: 0) + 1
                addToLikeTable(travelItem)
            } else {
                travelItem.likeCount = (travelItem.likeCount ?: 0) - 1
                removeFromLikeTable(travelItem)
            }

            // Update UI
            holder.likeIcon.setImageResource(if (travelItem.isLiked == true) R.drawable.liked else R.drawable.like)
            holder.likeText.text = travelItem.likeCount.toString() // Update TextView

            notifyDataSetChanged() // Refresh only the changed item
        }

        // Handle Save action
        holder.saveIcon.setOnClickListener {
            travelItem.isSaved = !(travelItem.isSaved ?: false)

            // Update Save Count Immediately
            if (travelItem.isSaved == true) {
                travelItem.savedCount = (travelItem.savedCount ?: 0) + 1
                addToSavedTable(travelItem)
            } else {
                travelItem.savedCount = (travelItem.savedCount ?: 0) - 1
                removeFromSavedTable(travelItem)
            }

            // Update UI
            holder.saveIcon.setImageResource(if (travelItem.isSaved == true) R.drawable.saved else R.drawable.save)
            holder.saveText.text = travelItem.savedCount.toString() // Update TextView

            notifyDataSetChanged() // Refresh only the changed item
        }

        holder.followButton.setOnClickListener {
            travelItem.isFollowed = !(travelItem.isFollowed ?: false)

            // Update Save Count Immediately
            if (travelItem.isFollowed == true) {
                addToFollowerTable(travelItem)
            } else {
                removeFromFollowerTable(travelItem)
            }

            // Update UI
            holder.followButton.setImageResource(if (travelItem.isFollowed == true) R.drawable.followed else R.drawable.follow)

            notifyDataSetChanged() // Refresh only the changed item
        }

        // Handle Edit action
        holder.editIcon.setOnClickListener {
            val context = it.context
            val dialogView = LayoutInflater.from(context).inflate(R.layout.edit_place_dialog, null)

            val editPlaceName = dialogView.findViewById<EditText>(R.id.editPlaceName)
            val editDescription = dialogView.findViewById<EditText>(R.id.editDescription)
            editDescription.movementMethod = ScrollingMovementMethod()
            val editLatitude = dialogView.findViewById<EditText>(R.id.editLatitude)
            val editLongitude = dialogView.findViewById<EditText>(R.id.editLongitude)
            val submitButton = dialogView.findViewById<Button>(R.id.submitEdit)

            // Set existing values in EditText
            editPlaceName.setText(holder.placeName.text.toString())
            editDescription.setText(travelItem.travelDescription)
            editLatitude.setText(travelItem.latitude.toString())
            editLongitude.setText(travelItem.longitude.toString())

            val dialog = AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(true)
                .create()

            submitButton.setOnClickListener {
                val updatedPlaceName = editPlaceName.text.toString().trim()
                val updatedDescription = editDescription.text.toString().trim()
                val updatedLatitude = editLatitude.text.toString().trim()
                val updatedLongitude = editLongitude.text.toString().trim()

                if (updatedDescription.length < 500) {
                    Toast.makeText(context, "Description must be at least 500 characters!", Toast.LENGTH_SHORT).show()
                } else {
                    holder.placeName.text = updatedPlaceName
                    holder.travelDescription.text = updatedDescription

                    travelItem.placeName = updatedPlaceName
                    travelItem.travelDescription = updatedDescription
                    travelItem.latitude = updatedLatitude.toDouble()
                    travelItem.longitude = updatedLongitude.toDouble()

                    editUserPost(travelItem, updatedPlaceName, updatedDescription, updatedLatitude, updatedLongitude)

                    notifyDataSetChanged()

                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                }
            }

            dialog.show()
        }



        // Handle Delete action
        holder.deleteIcon.setOnClickListener {
            val dialogView = LayoutInflater.from(it.context).inflate(R.layout.custom_dialog, null)
            val dialog = AlertDialog.Builder(it.context)
                .setView(dialogView)
                .setCancelable(false)
                .create()
            val btnYes = dialogView.findViewById<Button>(R.id.btnYes)
            val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

            btnYes.setOnClickListener {
                deleteFromPostsTable(travelItem)
                travelList.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, travelList.size)
                dialog.dismiss()
            }
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }


        // Open Google Maps on Location click
        holder.locationIcon.setOnClickListener {
            val uri = "https://www.google.com/maps/search/?api=1&query=${travelItem.latitude},${travelItem.longitude}"
            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            it.context.startActivity(mapIntent)
        }



        // Set visibility based on pageType
        if (pageType == "home" || pageType == "saved") {
            holder.followButton.visibility = View.VISIBLE
            holder.locationContainer.visibility = View.VISIBLE
            holder.likeContainer.visibility = View.VISIBLE
            holder.savedContainer.visibility = View.VISIBLE

            holder.editIcon.visibility = View.GONE
            holder.deleteIcon.visibility = View.GONE

        } else if (pageType == "profile") {
            holder.locationContainer.visibility = View.VISIBLE
            holder.editIcon.visibility = View.VISIBLE
            holder.deleteIcon.visibility = View.VISIBLE
            holder.likeContainer.visibility = View.VISIBLE
            holder.savedContainer.visibility = View.VISIBLE

            holder.followButton.visibility = View.GONE
        }

        holder.cardView.setOnClickListener {
            val intent = Intent(holder.itemView.context, FullPost::class.java)
            intent.putExtra("userName", travelItem.userName)
            intent.putExtra("profileImage", travelItem.profileImageRes)
            intent.putExtra("placeImage", travelItem.placeImageRes)
            intent.putExtra("placeName", travelItem.placeName)
            intent.putExtra("description", travelItem.travelDescription)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = travelList.size

    private fun loadUserData() {
        val userJson = sharedPreferences.getString("USER", null)
        user = userJson?.let { Gson().fromJson(it, User::class.java) }
        Log.d("UserCheck", "Loaded User: ${Gson().toJson(user)}")
    }

    private fun addToLikeTable(travelItem : TravelItem) {
        val apiService = RetrofitClient.instance
        val details = mapOf<String,Int?>(
            "user_id" to user?.id,
            "post_id" to travelItem.postId
        )
        apiService.addToLikeTable(details).enqueue(object : Callback<Like> {
            override fun onResponse(call: Call<Like>, response: Response<Like>) {
                if (response.isSuccessful) {
                    val like = response.body()
                } else {
                    println("Failed to fetch saved count")
                }
            }

            override fun onFailure(call: Call<Like>, t: Throwable) {
                println("Error: ${t.message}")
            }
        })
    }

    private fun removeFromLikeTable(travelItem : TravelItem) {
        val apiService = RetrofitClient.instance

        apiService.removeFromLikeTable(user?.id,travelItem.postId).enqueue(object : Callback<String> {
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

    private fun addToSavedTable(travelItem : TravelItem) {
        val apiService = RetrofitClient.instance
        val details = mapOf<String,Int?>(
            "user_id" to user?.id,
            "post_id" to travelItem.postId
        )
        apiService.addToSavedTable(details).enqueue(object : Callback<Save> {
            override fun onResponse(call: Call<Save>, response: Response<Save>) {
                if (response.isSuccessful) {
                    val save = response.body()
                } else {
                    println("Failed to fetch saved count")
                }
            }

            override fun onFailure(call: Call<Save>, t: Throwable) {
                println("Error: ${t.message}")
            }
        })
    }

    private fun removeFromSavedTable(travelItem : TravelItem) {
        val apiService = RetrofitClient.instance

        apiService.removeFromSavedTable(user?.id,travelItem.postId).enqueue(object : Callback<String> {
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

    private fun addToFollowerTable(travelItem: TravelItem) {
        val apiService = RetrofitClient.instance
        val details = mapOf<String,Int?>(
            "follower_id" to user?.id,
            "following_id" to travelItem.userId
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

    private fun removeFromFollowerTable(travelItem: TravelItem) {
        val apiService = RetrofitClient.instance

        apiService.removeFromFollowerTable(user?.id,travelItem.userId).enqueue(object : Callback<String> {
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

    private fun deleteFromPostsTable(travelItem: TravelItem) {
        val apiService = RetrofitClient.instance

        apiService.deleteFromPostsTable(travelItem.postId).enqueue(object : Callback<String> {
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

    private fun editUserPost(travelItem: TravelItem, placeName : String, description : String, latitude : String, longitude : String) {
        val apiService = RetrofitClient.instance

        val details = mapOf<String,String>(
            "place_name" to placeName,
            "description" to description,
            "latitude" to latitude,
            "longitude" to longitude
        )

        apiService.editUserPost(travelItem.postId,details).enqueue(object : Callback<String> {
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
}
