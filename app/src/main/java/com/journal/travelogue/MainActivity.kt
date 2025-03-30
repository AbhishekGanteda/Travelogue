package com.journal.travelogue

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.journal.travelogue.api.RetrofitClient
import com.journal.travelogue.models.LoginResponse
import com.journal.travelogue.models.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private var isAnimating = false
    var signInContainer: View? = null
    var signUpContainer: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val signInButton: Button = findViewById(R.id.signInButton)
        val signUpButton: Button = findViewById(R.id.signUpButton)
        signInContainer = findViewById(R.id.signInContainer)
        signUpContainer = findViewById(R.id.signUpContainer)
        val backSignIn: ImageButton = findViewById(R.id.backSignIn)
        val backSignUp: ImageButton = findViewById(R.id.backSignUp)
        val signInConfirmButton : Button = findViewById(R.id.signInConfirmButton)
        val signUpConfirmButton : Button = findViewById(R.id.signUpConfirmButton)
        val nameSignUp : EditText = findViewById(R.id.nameSignUp)
        val emailSignUp : EditText = findViewById(R.id.emailSignUp)
        val passwordSignUp : EditText = findViewById(R.id.passwordSignUp)
        val confirmPasswordSignUp : EditText = findViewById(R.id.confirmPasswordSignUp)
        val emailSignIn : EditText = findViewById(R.id.emailSignIn)
        val passwordSignIn : EditText = findViewById(R.id.passwordSignIn)


        signInButton.setOnClickListener {
            if (signInContainer!!.visibility == View.GONE && !isAnimating) {
                signInContainer!!.visibility = View.INVISIBLE // Prevent flickering
                signInContainer!!.post {
                    slideUp(signInContainer!!)
                }
            }
        }

        signUpButton.setOnClickListener {
            if (signUpContainer!!.visibility == View.GONE && !isAnimating) {
                signUpContainer!!.visibility = View.INVISIBLE // Prevent flickering
                signUpContainer!!.post {
                    slideUp(signUpContainer!!)
                }
            }
        }


        backSignIn.setOnClickListener {
            if (signInContainer!!.visibility == View.VISIBLE && !isAnimating) {
                slideDown(signInContainer!!)
            }
        }

        backSignUp.setOnClickListener {
            if (signUpContainer!!.visibility == View.VISIBLE && !isAnimating) {
                slideDown(signUpContainer!!)
            }
        }

        signInConfirmButton.setOnClickListener {
            // Retrieve input values
            val email = emailSignIn.text.toString().trim()
            val password = passwordSignIn.text.toString().trim()

            // Validate inputs
            if (email.isEmpty()) {
                emailSignUp.error = "Email is required"
                emailSignUp.requestFocus()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailSignUp.error = "Enter a valid email address"
                emailSignUp.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordSignUp.error = "Password is required"
                passwordSignUp.requestFocus()
                return@setOnClickListener
            }

            // Create a User object
            val user = User(email = email, password = password)

            // Call the loginUser function
            loginUser(user)
        }

        signUpConfirmButton.setOnClickListener {
            // Retrieve input values
            val name = nameSignUp.text.toString().trim()
            val email = emailSignUp.text.toString().trim()
            val password = passwordSignUp.text.toString().trim()
            val confirmPassword = confirmPasswordSignUp.text.toString().trim()

            // Validate inputs
            if (name.isEmpty()) {
                nameSignUp.error = "Name is required"
                nameSignUp.requestFocus()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                emailSignUp.error = "Email is required"
                emailSignUp.requestFocus()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailSignUp.error = "Enter a valid email address"
                emailSignUp.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordSignUp.error = "Password is required"
                passwordSignUp.requestFocus()
                return@setOnClickListener
            }

            if (password.length < 6) {
                passwordSignUp.error = "Password must be at least 6 characters"
                passwordSignUp.requestFocus()
                return@setOnClickListener
            }

            if (confirmPassword.isEmpty()) {
                confirmPasswordSignUp.error = "Please confirm your password"
                confirmPasswordSignUp.requestFocus()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                confirmPasswordSignUp.error = "Passwords do not match"
                confirmPasswordSignUp.requestFocus()
                return@setOnClickListener
            }

            val user = User(name = name, email = email, password = password)
            registerUser(user)
        }

    }

    private fun slideUp(view: View, onEnd: (() -> Unit)? = null) {
        isAnimating = true
        view.post {
            val height = view.measuredHeight.toFloat()
            val animate = TranslateAnimation(0f, 0f, height, 0f).apply {
                duration = 1000
                setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                    override fun onAnimationStart(animation: android.view.animation.Animation?) {
                        view.visibility = View.VISIBLE
                    }
                    override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                        isAnimating = false
                        view.translationY = 0f
                        onEnd?.invoke() // Executes the callback after animation ends
                    }
                    override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
                })
            }
            view.startAnimation(animate)
        }
    }


    private fun slideDown(view: View, onEnd: (() -> Unit)? = null) {
        isAnimating = true
        view.post {
            val height = view.measuredHeight.toFloat()
            val animate = TranslateAnimation(0f, 0f, 0f, height).apply {
                duration = 1000
                setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                    override fun onAnimationStart(animation: android.view.animation.Animation?) {}

                    override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                        isAnimating = false
                        view.visibility = View.GONE
                        onEnd?.invoke() // Executes the callback after animation ends
                    }

                    override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
                })
            }
            view.startAnimation(animate)
        }
    }

    private fun registerUser(user: User) {
        RetrofitClient.instance.registerUser(user).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Registration successful", Toast.LENGTH_SHORT).show()
                    slideDown(signUpContainer!!) {
                        slideUp(signInContainer!!)
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Registration failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loginUser(credentials: User) {
        RetrofitClient.instance.loginUser(credentials).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    val token = loginResponse?.token
                    val user = loginResponse?.user

                    // Save token in SharedPreferences
                    saveToken(token)

                    // Save user information if needed
                    val intent = Intent(this@MainActivity, Bottom::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@MainActivity, "Invalid Credentials: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveToken(token: String?) {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        token?.let {
            editor.putString("TOKEN", it)
            editor.apply()
        }
    }

}