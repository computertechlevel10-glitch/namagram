package com.elothar.namagram

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt

class DailyMasterGalleryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("NamagramPrefs", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode_enabled", false)
        val badgeDates = prefs.getStringSet("master_badge_history", emptySet())?.sortedDescending() ?: emptyList()

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
            setBackgroundColor(if (isDarkMode) "#0F172A".toColorInt() else ViewStyleUtility.COLOR_BACKGROUND_LIGHT.toColorInt())
        }

        val tvTitle = TextView(this).apply {
            text = getString(R.string.daily_master_gallery_title)
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor("#F39C12".toColorInt())
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 8)
        }
        rootLayout.addView(tvTitle)

        val tvSubtitle = TextView(this).apply {
            text = getString(R.string.daily_master_gallery_subtitle, badgeDates.size)
            textSize = 14f
            setTextColor(if (isDarkMode) "#94A3B8".toColorInt() else ViewStyleUtility.COLOR_TEXT_MUTED.toColorInt())
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 32)
        }
        rootLayout.addView(tvSubtitle)

        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f).apply {
                setMargins(0, 0, 0, 24)
            }
        }

        val gridLayout = GridLayout(this).apply {
            columnCount = 2
            alignmentMode = GridLayout.ALIGN_BOUNDS
            useDefaultMargins = true
        }

        if (badgeDates.isEmpty()) {
            val tvEmpty = TextView(this).apply {
                text = getString(R.string.daily_master_gallery_empty)
                textSize = 15f
                setTextColor(if (isDarkMode) Color.WHITE else Color.BLACK)
                gravity = Gravity.CENTER
                setPadding(32, 64, 32, 64)
            }
            rootLayout.addView(tvEmpty)
        } else {
            for (date in badgeDates) {
                val badgeCard = createBadgeCard(date, isDarkMode)
                gridLayout.addView(badgeCard)
            }
            scrollView.addView(gridLayout)
            rootLayout.addView(scrollView)
        }

        // Back Button
        val btnBack = Button(this).apply {
            text = getString(R.string.btn_back_to_hub)
            textSize = 16f
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            background = ViewStyleUtility.createRoundedDrawable(ViewStyleUtility.COLOR_PRIMARY_SLATE, 12f)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 130)
            setOnClickListener {
                finish()
            }
            ViewStyleUtility.attachButtonAnimateTouchListener(this)
        }
        rootLayout.addView(btnBack)

        setContentView(rootLayout)
    }

    private fun createBadgeCard(dateStamp: String, isDarkMode: Boolean): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 24, 24, 24)
            background = ViewStyleUtility.createRoundedDrawable(
                if (isDarkMode) "#1E293B" else "#FFFFFF",
                16f
            ).apply {
                setStroke(3, "#F1C40F".toColorInt()) // Gold Border
            }
            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1.0f)
                setMargins(12, 12, 12, 12)
            }

            val tvIcon = TextView(context).apply {
                text = "🏆"
                textSize = 32f
                gravity = Gravity.CENTER
            }
            addView(tvIcon)

            val tvName = TextView(context).apply {
                text = getString(R.string.daily_master_badge_card_title)
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                setTextColor("#F39C12".toColorInt())
                gravity = Gravity.CENTER
                setPadding(0, 8, 0, 4)
            }
            addView(tvName)

            val tvDate = TextView(context).apply {
                text = dateStamp
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                setTextColor(if (isDarkMode) "#2ECC71".toColorInt() else "#27AE60".toColorInt())
                gravity = Gravity.CENTER
            }
            addView(tvDate)
        }
    }
}