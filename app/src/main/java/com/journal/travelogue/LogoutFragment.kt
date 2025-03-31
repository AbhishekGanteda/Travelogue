package com.journal.travelogue

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.journal.travelogue.api.RetrofitClient
import com.journal.travelogue.models.User
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class LogoutFragment : Fragment() {

    private lateinit var nameText: TextView
    private lateinit var profileImage: ImageView
    private lateinit var editButton: Button
    private lateinit var logoutButton: Button
    private lateinit var dialog: AlertDialog
    private lateinit var dialogView: View
    private var selectedImageFile: File? = null
    private var user: User? = null
    private lateinit var sharedPreferences: android.content.SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_logout, container, false)

        nameText = view.findViewById(R.id.userName)
        profileImage = view.findViewById(R.id.userImage)
        editButton = view.findViewById(R.id.edit)
        logoutButton = view.findViewById(R.id.logout)

        sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Activity.MODE_PRIVATE)

        loadUserData()
        updateUI()

        editButton.setOnClickListener { showEditProfileDialog() }
        logoutButton.setOnClickListener { logoutUser() }

        return view
    }

    private fun loadUserData() {
        val userJson = sharedPreferences.getString("USER", null)
        user = userJson?.let { Gson().fromJson(it, User::class.java) }
        Log.d("UserCheck", "Loaded User: ${Gson().toJson(user)}")
    }

    private fun updateUI() {
        user?.let {
            nameText.text = it.name
            Glide.with(requireContext())
                .load("http://10.123.34.196:5000" + it.profile_image)
                .placeholder(R.drawable.profile)
                .into(profileImage)
        }
    }

    private fun logoutUser() {
        if (isAdded) {
            sharedPreferences.edit().clear().apply()
            startActivity(Intent(requireContext(), MainActivity::class.java))
            requireActivity().finish()
        } else {
            showToast("Fragment not attached")
        }
    }

    private fun showEditProfileDialog() {
        dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null)
        dialog = AlertDialog.Builder(requireContext()).setView(dialogView).setCancelable(true).create()

        val editName = dialogView.findViewById<EditText>(R.id.editName)
        val editEmail = dialogView.findViewById<EditText>(R.id.editEmail)
        val selectedImage = dialogView.findViewById<ImageView>(R.id.selectedImage)
        val uploadButton = dialogView.findViewById<Button>(R.id.uploadImage)
        val submitButton = dialogView.findViewById<Button>(R.id.submit)

        uploadButton.setOnClickListener { openGallery() }
        submitButton.setOnClickListener {
            val name = editName.text.toString().trim()
            val email = editEmail.text.toString().trim()

            if (name.isEmpty() && email.isEmpty() && selectedImageFile == null) {
                showToast("Please edit at least one field")
                return@setOnClickListener
            }

            updateUserProfile(name.takeIf { it.isNotEmpty() }, email.takeIf { it.isNotEmpty() })
        }

        dialog.show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val selectedImageUri: Uri? = result.data?.data
            selectedImageUri?.let {
                selectedImageFile = uriToFile(it, requireContext())
                dialogView.findViewById<ImageView>(R.id.selectedImage).setImageURI(selectedImageUri)
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

    private fun getFileExtension(uri: Uri): String? {
        return requireContext().contentResolver.getType(uri)?.let { mimeType ->
            when (mimeType) {
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                else -> "jpg"
            }
        }
    }

    private fun updateUserProfile(name: String?, email: String?) {
        val userId = user?.id ?: run {
            showToast("User ID is missing!")
            return
        }

        val apiService = RetrofitClient.instance
        val nameBody = name?.toRequestBody("text/plain".toMediaTypeOrNull())
        val emailBody = email?.toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart = selectedImageFile?.let { file ->
            MultipartBody.Part.createFormData(
                "image", file.name, file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )
        }

        apiService.updateUserProfile(userId, nameBody, emailBody, imagePart).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val updatedUser = response.body()
                    sharedPreferences.edit().putString("USER", Gson().toJson(updatedUser)).apply()
                    loadUserData()
                    updateUI()
                    dialog.dismiss()
                    showToast("Profile updated!")
                } else {
                    showToast("Update failed: ${response.errorBody()?.string() ?: "Unknown error"}")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }

    private fun showToast(message: String) {
        context?.let {
            Toast.makeText(it, message, Toast.LENGTH_LONG).show()
        }
    }
}
