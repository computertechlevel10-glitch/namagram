package com.elothar.namagram

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.elothar.namagram.R.string.scoring_point_4
import androidx.core.text.HtmlCompat
class GameRulesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Check Dark Mode Preference
        val prefs = getSharedPreferences("NamagramPrefs", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode_enabled", false)

        // Define dynamic colors based on dark mode state
        val backgroundColor = if (isDarkMode) "#0F172A" else "#FFFFFF"
        val ruleCardBg = if (isDarkMode) "#1E293B" else "#EBF8FF"
        val ruleTextColor = if (isDarkMode) "#93C5FD" else "#1E3A8A"
        val scoringCardBg = if (isDarkMode) "#064E3B" else "#D1FAE5"
        val scoringTextColor = if (isDarkMode) "#A7F3D0" else "#064E3B"
        val titleColor = if (isDarkMode) Color.WHITE else ViewStyleUtility.COLOR_PRIMARY_SLATE.toColorInt()

        val scrollView = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(backgroundColor.toColorInt())
        }

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 24)
            setBackgroundColor(backgroundColor.toColorInt())
            gravity = Gravity.TOP
        }

        // 2. PAGE TITLE
        val tvTitle = TextView(this).apply {
            text = getString(R.string.rules_header)
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(titleColor)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }
        rootLayout.addView(tvTitle)

// 3. RULES CARDS
        val rulesResourceIds = listOf(
            R.string.rule_1,
            R.string.rule_2,
            R.string.rule_3,
            R.string.rule_4,
            R.string.rule_5,
            R.string.rule_6,
            R.string.rule_7,
            R.string.rule_8,
        )

        for (resId in rulesResourceIds) {
            val ruleBox = TextView(this).apply {
                // 👉 Parse HTML tags so the <b> tags render as bold highlights
                text = HtmlCompat.fromHtml(getString(resId), HtmlCompat.FROM_HTML_MODE_LEGACY)
                textSize = 13.5f
                // ❌ Ensure typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) is REMOVED here
                setTextColor(ruleTextColor.toColorInt())
                setPadding(28, 18, 28, 18)
                background = ViewStyleUtility.createRoundedDrawable(ruleCardBg, 8f)
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 0, 0, 12)
                }
                setLineSpacing(0f, 1.15f)
            }
            rootLayout.addView(ruleBox)
        }

        // 4. SCORING SECTION HEADER
        val tvScoringHeader = TextView(this).apply {
            text = getString(R.string.scoring_header)
            textSize = 17f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(titleColor)
            setPadding(8, 0, 0, 12)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 36, 0, 0)
            }
        }
        rootLayout.addView(tvScoringHeader)

// 5. SCORING CARDS
        val scoringResourceIds = listOf(
            R.string.scoring_point_1,
            R.string.scoring_point_2,
            R.string.scoring_point_3,
            scoring_point_4
        )

        for (resId in scoringResourceIds) {
            val scoringBox = TextView(this).apply {
                text = HtmlCompat.fromHtml(getString(resId), HtmlCompat.FROM_HTML_MODE_LEGACY)
                textSize = 13.5f
                // ❌ Removed global typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) here
                setTextColor(scoringTextColor.toColorInt())
                setPadding(28, 18, 28, 18)
                background = ViewStyleUtility.createRoundedDrawable(scoringCardBg, 8f)
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 0, 0, 12)
                }
                setLineSpacing(0f, 1.15f)
            }
            rootLayout.addView(scoringBox)
        }

        // 6. PERSISTENT MENU BUTTON
        ViewStyleUtility.addPersistentMenuButton(this, rootLayout)

        scrollView.addView(rootLayout)
        setContentView(scrollView)
    }

    override fun onResume() {
        super.onResume()
        // 👉 Guarantees the Rulebook Reader achievement fires every time the screen becomes active
        val manager = AchievementManager(this)
        manager.trackExplorationAction("expl_rules")
        manager.processPendingAchievements()
    }
}