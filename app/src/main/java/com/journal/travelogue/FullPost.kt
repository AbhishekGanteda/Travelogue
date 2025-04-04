package com.journal.travelogue

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.journal.travelogue.api.RetrofitClient.ip
import com.squareup.picasso.Picasso

class FullPost : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_full_post)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Extracting data from Intent
        val userName = intent.getStringExtra("userName")
        val profileImage = intent.getStringExtra("profileImage")
        val placeImage = intent.getStringExtra("placeImage")
        val placeName = intent.getStringExtra("placeName")
        val description = intent.getStringExtra("description")

        // Reference UI elements
        val userNameTextView = findViewById<TextView>(R.id.user_name)
        val profileImageView = findViewById<ImageView>(R.id.profile)
        val placeImageView = findViewById<ImageView>(R.id.image)
        val placeNameTextView = findViewById<TextView>(R.id.place_name)
        val descriptionTextView = findViewById<TextView>(R.id.travel_description)

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
    }
}
