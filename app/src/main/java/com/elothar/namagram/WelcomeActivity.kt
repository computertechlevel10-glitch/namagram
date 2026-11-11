package com.elothar.namagram

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import java.util.Locale

class WelcomeActivity : AppCompatActivity() {

    private lateinit var etNickname: EditText

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Check Dark Mode Preference
        val prefs = getSharedPreferences("NamagramPrefs", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode_enabled", false)

        // Define dynamic colors based on dark mode state
        val backgroundColor = if (isDarkMode) "#0F172A" else ViewStyleUtility.COLOR_BACKGROUND_LIGHT
        val titleColor = if (isDarkMode) Color.WHITE else ViewStyleUtility.COLOR_PRIMARY_SLATE.toColorInt()
        val subtitleColor = if (isDarkMode) "#94A3B8" else ViewStyleUtility.COLOR_TEXT_MUTED

        // Default to empty string instead of "Player 1" so the input box starts clean
        val savedName = prefs.getString("player1_nickname", "") ?: ""
        MatchSettings.player1Nickname = savedName

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(48, 48, 48, 48)
            setBackgroundColor(backgroundColor.toColorInt())
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }

        val scrollView = ScrollView(this).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f)
            isFillViewport = true
            isVerticalScrollBarEnabled = false
        }

        val mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        // 2. Logo (sized correctly to match the main menu)
        val logoImage = ImageView(this).apply {
            setImageResource(R.drawable.namagram_logo)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER

            layoutParams = LayoutParams(dpToPx(140), dpToPx(140)).apply {
                setMargins(0, dpToPx(16), 0, dpToPx(8))
            }
        }
        mainContainer.addView(logoImage) // Fixed reference from ivLogo to logoImage

        // 3. Title & Subtitle Header
        val tvTitle = TextView(this).apply {
            text = getString(R.string.welcome_title)
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(titleColor)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 8)
        }
        mainContainer.addView(tvTitle)

        val tvSubtitle = TextView(this).apply {
            text = getString(R.string.welcome_subtitle)
            textSize = 13.5f
            setTextColor(subtitleColor.toColorInt())
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 32)
        }
        mainContainer.addView(tvSubtitle)

        // 4. Profile Input Box with "Enter your nickname" hint
        etNickname = EditText(this).apply {
            ViewStyleUtility.applyEditTextStyle(this, "Enter your nickname")
            if (savedName.isNotEmpty() && savedName != "Player 1") {
                setText(savedName)
            }
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 24)
            }
        }
        mainContainer.addView(etNickname)

        // 5. Continue Button with Mandatory Profile Enforcement
        val btnContinue = Button(this).apply {
            text = getString(R.string.btn_start_playing)
            textSize = 15f
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            background = ViewStyleUtility.createRoundedDrawable(ViewStyleUtility.COLOR_SUCCESS_EMERALD, 12f)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 110)
            setOnClickListener {
                val enteredName = etNickname.text.toString().trim()
                val lower = enteredName.lowercase(Locale.ROOT)

                // Enforce mandatory custom name validation
                val isValid = enteredName.isNotEmpty() &&
                        lower != "enter your nickname" &&
                        lower != "player 1" &&
                        lower != "player 2" &&
                        lower != "guest" &&
                        lower != "player1" &&
                        lower != "player2"

                if (isValid) {
                    MatchSettings.updatePlayerProfile(this@WelcomeActivity, enteredName)
                    AchievementManager(this@WelcomeActivity).unlockAchievement("expl_profile")

                    startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
                    finish()
                } else {
                    android.widget.Toast.makeText(
                        this@WelcomeActivity,
                        "Please enter a valid unique nickname to play!",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
            ViewStyleUtility.attachButtonAnimateTouchListener(this)
        }
        mainContainer.addView(btnContinue)

        scrollView.addView(mainContainer)
        rootLayout.addView(scrollView)
        setContentView(rootLayout)
    }
}