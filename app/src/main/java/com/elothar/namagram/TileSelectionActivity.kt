package com.elothar.namagram

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt

class TileSelectionActivity : AppCompatActivity() {

    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        soundManager = SoundManager(this)

        // 1. Check Dark Mode Preference
        val prefs = getSharedPreferences("NamagramPrefs", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode_enabled", false)

        // Define dynamic colors based on dark mode state
        val backgroundColor = if (isDarkMode) "#0F172A" else ViewStyleUtility.COLOR_BACKGROUND_LIGHT
        val titleTextColor = if (isDarkMode) android.graphics.Color.WHITE else ViewStyleUtility.COLOR_TEXT_DARK.toColorInt()

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(32, 32, 32, 32)
            setBackgroundColor(backgroundColor.toColorInt())
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }

        // Header style using string resource
        val tvTitle = TextView(this).apply {
            text = getString(R.string.tile_selection_title)
            textSize = 22f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            setTextColor(titleTextColor)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 16)
        }
        rootLayout.addView(tvTitle)

        // Container to evenly space the buttons vertically using weights
        val buttonsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0).apply {
                weight = 1f
                setMargins(0, 0, 0, 16)
            }
            gravity = Gravity.CENTER_VERTICAL
        }

        val tileIcons = listOf("🧩", "🎲", "🎯", "⭐", "🔥", "👑")

        for (i in 5..10) {
            val iconEmoji = tileIcons[(i - 5) % tileIcons.size]
            val btnOption = Button(this).apply {
                text = getString(R.string.tile_option_format, iconEmoji, i)
                textSize = 15f
                setTextColor(android.graphics.Color.WHITE)
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                background = ViewStyleUtility.createRoundedDrawable(ViewStyleUtility.COLOR_PRIMARY_SLATE, 10f)

                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0).apply {
                    weight = 1f
                    setMargins(0, 6, 0, 6)
                }

                setOnClickListener {
                    MatchSettings.totalLettersToDraw = i
                    val intent = Intent(this@TileSelectionActivity, GameActivity::class.java)
                    startActivity(intent)
                }
                ViewStyleUtility.attachButtonAnimateTouchListener(this)
            }
            buttonsContainer.addView(btnOption)
        }

        rootLayout.addView(buttonsContainer)

        // Persistent Return to Main Menu button at the bottom
        ViewStyleUtility.addPersistentMenuButton(this, rootLayout)

        setContentView(rootLayout)
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}