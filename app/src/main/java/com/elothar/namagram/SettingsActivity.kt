package com.elothar.namagram

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.graphics.toColorInt

class SettingsActivity : AppCompatActivity() {

    private var soundManager: SoundManager? = null

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        soundManager = try {
            SoundManager(this)
        } catch (_: Exception) {
            null
        }

        val displayMetrics = resources.displayMetrics
        fun dpToPx(dp: Int): Int = (dp * displayMetrics.density).toInt()

        val prefs = getSharedPreferences("NamagramPrefs", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode_enabled", false)

        // Dynamic Color Scheme based on Dark Mode
        val backgroundColor = if (isDarkMode) "#0F172A" else ViewStyleUtility.COLOR_BACKGROUND_LIGHT
        val cardBackgroundColor = if (isDarkMode) "#1E293B" else "#FFFFFF"
        val textColorPrimary = if (isDarkMode) Color.WHITE else ViewStyleUtility.COLOR_TEXT_DARK.toColorInt()
        val cardTitleColor = if (isDarkMode) Color.WHITE else ViewStyleUtility.COLOR_TEXT_DARK.toColorInt()
        val subtitleColor = if (isDarkMode) "#94A3B8" else "#64748B"

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            setBackgroundColor(backgroundColor.toColorInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Title Header
        val tvTitle = TextView(this).apply {
            text = getString(R.string.btn_settings)
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(textColorPrimary)
            setPadding(0, 0, 0, dpToPx(16))
            gravity = Gravity.CENTER_HORIZONTAL
        }
        rootLayout.addView(tvTitle)

        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0
            ).apply { weight = 1f }
            isFillViewport = true
            isVerticalScrollBarEnabled = false
        }

        val scrollContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        fun createSettingCard(titleText: String, subtitleText: String, controlView: android.view.View): LinearLayout {
            return LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12))
                background = ViewStyleUtility.createRoundedDrawable(cardBackgroundColor, 16f)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, dpToPx(10))
                }

                val row = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                val textLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }

                val tvCardTitle = TextView(context).apply {
                    text = titleText
                    textSize = 15f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    setTextColor(cardTitleColor)
                }
                textLayout.addView(tvCardTitle)

                val tvCardSubtitle = TextView(context).apply {
                    text = subtitleText
                    textSize = 12f
                    setTextColor(subtitleColor.toColorInt())
                    setPadding(0, dpToPx(2), 0, 0)
                }
                textLayout.addView(tvCardSubtitle)

                row.addView(textLayout)
                row.addView(controlView)
                addView(row)
            }
        }

        // 1. SOUND TOGGLE SETTING
        val isSoundEnabled = prefs.getBoolean("sound_enabled", true)
        val switchSound = Switch(this).apply {
            isChecked = isSoundEnabled
            setOnCheckedChangeListener { _, isChecked ->
                prefs.edit { putBoolean("sound_enabled", isChecked) }
                try {
                    soundManager?.setSoundEnabled(isChecked)
                } catch (_: Exception) {}
                Toast.makeText(this@SettingsActivity, if (isChecked) "🔊 Sound Enabled" else "🔇 Sound Disabled", Toast.LENGTH_SHORT).show()
            }
        }
        scrollContent.addView(createSettingCard("Sound Effects", "Enable or disable feedback sounds.", switchSound))

        // 2. DARK MODE THEME TOGGLE SETTING
        val switchTheme = Switch(this).apply {
            isChecked = isDarkMode
            setOnCheckedChangeListener { _, isChecked ->
                prefs.edit { putBoolean("dark_mode_enabled", isChecked) }
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                recreate() // Apply instantly
            }
        }
        scrollContent.addView(createSettingCard("Dark Mode Theme", "Switch between Light and Dark visual mode.", switchTheme))

        scrollView.addView(scrollContent)
        rootLayout.addView(scrollView)

        // Safe Back to Main Menu Button at Bottom
        Button(this).apply {
            text = getString(R.string.btn_return_main_menu)
            textSize = 15f
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            background = ViewStyleUtility.createRoundedDrawable("#475569", 12f)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(50)
            ).apply {
                setMargins(0, dpToPx(12), 0, dpToPx(12))
            }
            setOnClickListener {
                finish()
            }
            ViewStyleUtility.attachButtonAnimateTouchListener(this)
        }.also { rootLayout.addView(it) }

        setContentView(rootLayout)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            soundManager?.release()
        } catch (_: Exception) {}
    }
}