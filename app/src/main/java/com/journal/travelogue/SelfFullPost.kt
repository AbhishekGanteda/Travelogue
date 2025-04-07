package com.journal.travelogue

import android.app.AlertDialog
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.journal.travelogue.api.RetrofitClient
import com.journal.travelogue.api.RetrofitClient.ip
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SelfFullPost : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_self_full_post)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val postId = intent.getIntExtra("postId", -1)
        val userName = intent.getStringExtra("userName")
        val profileImage = intent.getStringExtra("profileImage")
        val placeImage = intent.getStringExtra("placeImage")
        val placeName = intent.getStringExtra("placeName")
        val description = intent.getStringExtra("description")
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)

        // Reference UI elements
        val userNameTextView = findViewById<TextView>(R.id.user_name)
        val profileImageView = findViewById<ImageView>(R.id.profile)
        val placeImageView = findViewById<ImageView>(R.id.image)
        val placeNameTextView = findViewById<TextView>(R.id.place_name)
        val descriptionTextView = findViewById<TextView>(R.id.travel_description)
        val editIcon = findViewById<ImageView>(R.id.edit)
        val deleteIcon = findViewById<ImageView>(R.id.delete)

        // Set values to UI
        userNameTextView.text = userName
        placeNameTextView.text = placeName
        descriptionTextView.text = description

        // Load images using Glide (if URLs are passed)
        Glide.with(this)
            .load("http://$ip:5000"+profileImage)
            .placeholder(R.drawable.profile)
            .error(R.drawable.profile)
            .into(profileImageView)
        Glide.with(this)
            .load("http://$ip:5000/uploads/"+placeImage)
            .placeholder(R.drawable.noimage)
            .error(R.drawable.noimage)
            .into(placeImageView)

        editIcon.setOnClickListener {
            val context = it.context
            val dialogView = LayoutInflater.from(context).inflate(R.layout.edit_place_dialog, null)

            val editPlaceName = dialogView.findViewById<EditText>(R.id.editPlaceName)
            val editDescription = dialogView.findViewById<EditText>(R.id.editDescription)
            editDescription.movementMethod = ScrollingMovementMethod()
            val editLatitude = dialogView.findViewById<EditText>(R.id.editLatitude)
            val editLongitude = dialogView.findViewById<EditText>(R.id.editLongitude)
            val submitButton = dialogView.findViewById<Button>(R.id.submitEdit)

            // Set existing values in EditText
            editPlaceName.setText(placeName)
            editDescription.setText(description)
            editLatitude.setText(latitude.toString())
            editLongitude.setText(longitude.toString())

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
                    placeNameTextView.text = updatedPlaceName
                    descriptionTextView.text = updatedDescription

                    editUserPost(postId, updatedPlaceName, updatedDescription, updatedLatitude, updatedLongitude)

                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                }
            }

            dialog.show()
        }

        deleteIcon.setOnClickListener {
            val dialogView = LayoutInflater.from(it.context).inflate(R.layout.custom_dialog, null)
            val dialog = AlertDialog.Builder(it.context)
                .setView(dialogView)
                .setCancelable(false)
                .create()
            val btnYes = dialogView.findViewById<Button>(R.id.btnYes)
            val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

            btnYes.setOnClickListener {
                deleteFromPostsTable(postId)
                dialog.dismiss()
            }
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun editUserPost(postId : Int?, placeName : String, description : String, latitude : String, longitude : String) {
        val apiService = RetrofitClient.instance

        val details = mapOf<String,String>(
            "place_name" to placeName,
            "description" to description,
            "latitude" to latitude,
            "longitude" to longitude
        )

        apiService.editUserPost(postId,details).enqueue(object : Callback<String> {
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

    private fun deleteFromPostsTable(postId: Int?) {
        val apiService = RetrofitClient.instance

        apiService.deleteFromPostsTable(postId).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val message = response.body()
                    Toast.makeText(this@SelfFullPost, "Post deleted successfully!", Toast.LENGTH_SHORT).show()

                    finish()
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