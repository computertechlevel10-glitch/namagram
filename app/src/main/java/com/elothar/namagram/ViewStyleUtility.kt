package com.elothar.namagram

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt

object ViewStyleUtility {

    // Cohesive Modern Polish Color Palette Hex Codes
    const val COLOR_PRIMARY_SLATE = "#2C3E50"
    const val COLOR_SUCCESS_EMERALD = "#2ECC71"
    const val COLOR_BACKGROUND_LIGHT = "#F8F9FA"
    const val COLOR_TEXT_DARK = "#2C3E50"
    const val COLOR_TEXT_MUTED = "#7F8C8D"

    /**
     * Programmatically builds a customized rounded background drawable canvas layer.
     */
    fun createRoundedDrawable(hexColor: String, cornerRadiusDp: Float): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(hexColor.toColorInt())
            cornerRadius = cornerRadiusDp * 3f
        }
    }

    /**
     * Programmatically styles a text field box with clear font weights.
     */
    fun applyHeaderStyle(textView: TextView, textString: String, isMainTitle: Boolean = false) {
        textView.apply {
            text = textString
            textSize = if (isMainTitle) 32f else 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(COLOR_TEXT_DARK.toColorInt())
            gravity = Gravity.CENTER
        }
    }

    /**
     * Programmatically injects a sleek rounded outline style directly to EditText inputs.
     */
    fun applyEditTextStyle(editText: EditText, hintString: String) {
        editText.apply {
            hint = hintString
            textSize = 16f
            setPadding(32, 24, 32, 24)
            setTextColor(COLOR_TEXT_DARK.toColorInt())

            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setStroke(2, "#BDC3C7".toColorInt())
                setColor(Color.WHITE)
                cornerRadius = 16f
            }
        }
    }

    /**
     * Attaches a real-time compression, dimming, and sound effect onto press actions.
     * Accessibility-compliant: Handles TalkBack layouts by invoking performClick natively.
     */
    fun attachButtonAnimateTouchListener(view: View) {
        val soundManager = SoundManager(view.context)
        val prefs = view.context.getSharedPreferences("NamagramPrefs", Context.MODE_PRIVATE)
        val isSoundEnabled = prefs.getBoolean("sound_enabled", true)

        // Disable Android's built-in system click sound effect if sound is muted
        view.isSoundEffectsEnabled = isSoundEnabled

        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Check if sound effects are enabled in SharedPreferences before playing
                    if (isSoundEnabled) {
                        soundManager.playClickSound()
                    }

                    v.animate().scaleX(0.94f).scaleY(0.94f).alpha(0.8f).setDuration(80).start()
                }
                MotionEvent.ACTION_UP -> {
                    v.animate().scaleX(1.0f).scaleY(1.0f).alpha(1.0f).setDuration(80).start()
                    v.performClick()
                }
                MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1.0f).scaleY(1.0f).alpha(1.0f).setDuration(80).start()
                }
            }
            true
        }
    }

    /**
     * Programmatically attaches a unified rounded back-to-menu button layout
     * directly onto the bottom of any layout container hierarchy tracking tree.
     */
    fun addPersistentMenuButton(activityContext: Activity, targetLayout: LinearLayout) {
        val btnBack = Button(activityContext).apply {
            text = activityContext.getString(R.string.btn_return_menu)
            textSize = 15f
            setTextColor(Color.WHITE)
            background = createRoundedDrawable(COLOR_PRIMARY_SLATE, 8f)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                130
            ).apply {
                setMargins(0, 16, 0, 0)
            }

            setOnClickListener {
                isEnabled = false
                val intent = Intent(activityContext, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                activityContext.startActivity(intent)
                activityContext.finish()
            }
            attachButtonAnimateTouchListener(this)
        }
        targetLayout.addView(btnBack)
    }
}