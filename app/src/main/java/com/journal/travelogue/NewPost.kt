package com.journal.travelogue

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class NewPost : AppCompatActivity() {

    private lateinit var selectedImageView: ImageView
    private lateinit var uploadButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_post)

        // Apply Window Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        selectedImageView = findViewById(R.id.ivSelectedImage)  // ImageView for displaying image
        uploadButton = findViewById(R.id.btnUploadImage) // Button to upload image
        val btnSave = findViewById<Button>(R.id.btnSave)
        val backButton = findViewById<ImageView>(R.id.backButton)

        // Set click listener for Upload button
        uploadButton.setOnClickListener {
            openGallery()
        }

        // Set click listener for Save button
        btnSave.setOnClickListener {
            showSuccessDialog()
        }

        // Set click listener for Back button
        backButton.setOnClickListener {
            finish()
        }
    }

    // Open Gallery
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    // Handle image selection result
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val selectedImageUri: Uri? = result.data!!.data
            selectedImageView.setImageURI(selectedImageUri)
        }
    }

    // Function to show success dialog
    private fun showSuccessDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Success")
            .setMessage("Your travel item has been successfully submitted!")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
