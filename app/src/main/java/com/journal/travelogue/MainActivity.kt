package com.journal.travelogue

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var isAnimating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val signInButton: Button = findViewById(R.id.signInButton)
        val signUpButton: Button = findViewById(R.id.signUpButton)
        val signInContainer: View = findViewById(R.id.signInContainer)
        val signUpContainer: View = findViewById(R.id.signUpContainer)
        val backSignIn: ImageButton = findViewById(R.id.backSignIn)
        val backSignUp: ImageButton = findViewById(R.id.backSignUp)
        val signInConfirmButton : Button = findViewById(R.id.signInConfirmButton)
        val signUpConfirmButton : Button = findViewById(R.id.signUpConfirmButton)

        signInButton.setOnClickListener {
            if (signInContainer.visibility == View.GONE && !isAnimating) {
                signInContainer.visibility = View.INVISIBLE // Prevent flickering
                signInContainer.post {
                    slideUp(signInContainer)
                }
            }
        }

        signUpButton.setOnClickListener {
            if (signUpContainer.visibility == View.GONE && !isAnimating) {
                signUpContainer.visibility = View.INVISIBLE // Prevent flickering
                signUpContainer.post {
                    slideUp(signUpContainer)
                }
            }
        }

        backSignIn.setOnClickListener {
            if (signInContainer.visibility == View.VISIBLE && !isAnimating) {
                slideDown(signInContainer)
            }
        }

        backSignUp.setOnClickListener {
            if (signUpContainer.visibility == View.VISIBLE && !isAnimating) {
                slideDown(signUpContainer)
            }
        }

        signInConfirmButton.setOnClickListener {
            val intent = Intent(this, Bottom::class.java)
            startActivity(intent)
        }

        signUpConfirmButton.setOnClickListener {
            if (signUpContainer.visibility == View.VISIBLE && !isAnimating) {
                slideDown(signUpContainer) {
                    if (signInContainer.visibility == View.GONE && !isAnimating) {
                        signInContainer.post {
                            slideUp(signInContainer)
                        }
                    }
                }
            }
        }

    }

    private fun slideUp(view: View) {
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


}
