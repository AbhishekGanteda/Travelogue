package com.journal.travelogue

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.journal.travelogue.api.RetrofitClient
import com.journal.travelogue.models.Post
import com.journal.travelogue.models.User
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewPost : AppCompatActivity() {

    private lateinit var selectedImageView: ImageView
    private lateinit var uploadButton: Button
    private lateinit var btnSave : Button
    private lateinit var backButton : ImageView
    private lateinit var placeName : EditText
    private lateinit var description : EditText
    private lateinit var latitude : EditText
    private lateinit var longitude : EditText
    private var user: User? = null
    private lateinit var sharedPreferences: android.content.SharedPreferences
    private var selectedImageFile: File? = null

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

        selectedImageView = findViewById(R.id.ivSelectedImage)
        uploadButton = findViewById(R.id.btnUploadImage)
        btnSave = findViewById(R.id.btnSave)
        backButton = findViewById(R.id.backButton)
        placeName = findViewById(R.id.placeName)
        description = findViewById(R.id.description)
        latitude = findViewById(R.id.latitude)
        longitude = findViewById(R.id.longitude)

        sharedPreferences = getSharedPreferences("UserPrefs", Activity.MODE_PRIVATE)

        // Set click listener for Upload button
        uploadButton.setOnClickListener {
            openGallery()
        }

        // Set click listener for Save button
        btnSave.setOnClickListener {
            postTravelItem(placeName.text.toString().trim(), description.text.toString().trim(), latitude.text.toString().trim().toDouble(), latitude.text.toString().trim().toDouble())
        }

        // Set click listener for Back button
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadUserData() {
        val userJson = sharedPreferences.getString("USER", null)
        user = userJson?.let { Gson().fromJson(it, User::class.java) }
        Log.d("UserCheck", "Loaded User: ${Gson().toJson(user)}")
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
            selectedImageUri?.let {
                selectedImageFile = uriToFile(it, this)
                selectedImageView.setImageURI(selectedImageUri)
            }
        }
    }

    private fun getFileExtension(uri: Uri): String? {
        return this.contentResolver.getType(uri)?.let { mimeType ->
            when (mimeType) {
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                else -> "jpg"
            }
        }
    }

    private fun uriToFile(uri: Uri, context: Context): File {
        val contentResolver = context.contentResolver
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val extension = getFileExtension(uri) ?: "jpg"
        val file = File(context.cacheDir, "image_${System.currentTimeMillis()}.$extension")

        inputStream?.use { input ->
            FileOutputStream(file).use { output -> input.copyTo(output) }
        }
        return file
    }

    private fun postTravelItem(placeName : String, description : String, latitude : Double, longitude : Double){
        loadUserData()
        val userId = user?.id ?: run {
            showToast("User ID is missing!")
            return
        }
        val apiService = RetrofitClient.instance
        val placeNameRequestBody = placeName.toRequestBody("text/plain".toMediaTypeOrNull())
        val descriptionRequestBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val latitudeRequestBody = latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val longitudeRequestBody = longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart = selectedImageFile?.let { file ->
            MultipartBody.Part.createFormData(
                "image", file.name, file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )
        }

        apiService.postTravelItem(userId, placeNameRequestBody, descriptionRequestBody, latitudeRequestBody, longitudeRequestBody, imagePart).enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (response.isSuccessful) {
                    showToast("Successfully posted the travel item!")
                } else {
                    showToast("Failed to post travel item!")
                }
            }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })

    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
