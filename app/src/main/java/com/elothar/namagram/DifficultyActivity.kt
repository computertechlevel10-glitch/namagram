package com.elothar.namagram

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt

class DifficultyActivity : AppCompatActivity() {

    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        soundManager = SoundManager(this)

        // 1. Check Dark Mode Preference
        val prefs = getSharedPreferences("NamagramPrefs", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode_enabled", false)

        // Define dynamic colors based on dark mode state
        val backgroundColor = if (isDarkMode) "#0F172A" else ViewStyleUtility.COLOR_BACKGROUND_LIGHT
        val cardBackgroundColor = if (isDarkMode) "#1E293B" else "#FFFFFF"
        val cardSelectedColor = if (isDarkMode) "#172554" else "#EFF6FF"
        val textColorPrimary = if (isDarkMode) "#FFFFFF" else "#374151"
        val textColorDesc = if (isDarkMode) "#94A3B8" else "#4B5563"

        val displayMetrics = resources.displayMetrics
        fun dpToPx(dp: Int): Int = (dp * displayMetrics.density).toInt()

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12))
            setBackgroundColor(backgroundColor.toColorInt())
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }

        // Title Header
        val tvTitle = TextView(this).apply {
            ViewStyleUtility.applyHeaderStyle(this, getString(R.string.diff_title), isMainTitle = true)
            setPadding(0, 0, 0, dpToPx(8))
            gravity = Gravity.CENTER_HORIZONTAL
        }
        rootLayout.addView(tvTitle)

        // Middle container that holds all 3 cards snugly without scrolling
        val contentContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0).apply {
                weight = 1.0f
            }
            gravity = Gravity.CENTER_VERTICAL
        }

        val currentSavedTime = DifficultySettings.getTimeLimit(this)
        val cardViews = mutableListOf<LinearLayout>()

        fun createDifficultyCard(
            titleResId: Int,
            timeSeconds: Int,
            badgeColorHex: String,
            descResId: Int
        ) {
            val isSelected = (currentSavedTime == timeSeconds)

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dpToPx(14), dpToPx(10), dpToPx(14), dpToPx(10))

                background = ViewStyleUtility.createRoundedDrawable(
                    if (isSelected) cardSelectedColor else cardBackgroundColor, 14f
                )
                elevation = if (isSelected) 5f else 2f

                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 0, 0, dpToPx(8))
                }

                setOnClickListener {
                    DifficultySettings.saveTimeLimit(this@DifficultyActivity, timeSeconds)
                    Toast.makeText(this@DifficultyActivity, getString(R.string.toast_saved), Toast.LENGTH_SHORT).show()

                    for (c in cardViews) {
                        c.background = ViewStyleUtility.createRoundedDrawable(cardBackgroundColor, 14f)
                        c.elevation = 2f
                    }

                    this.background = ViewStyleUtility.createRoundedDrawable(cardSelectedColor, 14f)
                    this.elevation = 5f

                    rootLayout.postDelayed({ finish() }, 250)
                }
            }

            cardViews.add(card)

            val headerRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 0, 0, dpToPx(2))
                }
            }

            val tvCardTitle = TextView(this).apply {
                text = getString(titleResId)
                textSize = 15f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                setTextColor(badgeColorHex.toColorInt())
                layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            }
            headerRow.addView(tvCardTitle)

            if (isSelected) {
                val tvCheck = TextView(this).apply {
                    text = getString(R.string.diff_selected_badge)
                    textSize = 11.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    setTextColor(badgeColorHex.toColorInt())
                }
                headerRow.addView(tvCheck)
            }

            card.addView(headerRow)

            val tvTime = TextView(this).apply {
                text = getString(R.string.diff_time_format, timeSeconds)
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                setTextColor(textColorPrimary.toColorInt())
                setPadding(0, 0, 0, dpToPx(2))
            }
            card.addView(tvTime)

            val tvDesc = TextView(this).apply {
                text = getString(descResId)
                textSize = 11.5f
                setTextColor(textColorDesc.toColorInt())
                setLineSpacing(0f, 1.1f)
            }
            card.addView(tvDesc)

            contentContainer.addView(card)
        }

        // Add Easy, Medium, and Hard cards
        createDifficultyCard(
            titleResId = R.string.diff_card_easy,
            timeSeconds = 90,
            badgeColorHex = "#059669",
            descResId = R.string.diff_desc_easy
        )

        createDifficultyCard(
            titleResId = R.string.diff_card_medium,
            timeSeconds = 60,
            badgeColorHex = "#2563EB",
            descResId = R.string.diff_desc_medium
        )

        createDifficultyCard(
            titleResId = R.string.diff_card_hard,
            timeSeconds = 30,
            badgeColorHex = "#DC2626",
            descResId = R.string.diff_desc_hard
        )

        rootLayout.addView(contentContainer)

        // Persistent Return to Main Menu button pinned at bottom
        ViewStyleUtility.addPersistentMenuButton(this, rootLayout)

        setContentView(rootLayout)
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}