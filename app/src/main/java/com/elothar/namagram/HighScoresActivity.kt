package com.elothar.namagram

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt

class HighScoresActivity : AppCompatActivity() {

    private lateinit var scoreManager: ScoreManager
    private lateinit var achievementManager: AchievementManager
    private lateinit var soloContainer: LinearLayout
    private lateinit var multiContainer: LinearLayout

    // Dark mode state variables
    private var isDarkMode = false
    private var soloCardBg = "#EBF8FF"
    private var soloTextColor = "#1E3A8A"
    private var multiCardBg = "#D1FAE5"
    private var multiTextColor = "#064E3B"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scoreManager = ScoreManager(this)
        achievementManager = AchievementManager(this)

        // --- TRACK HIGH SCORES LEADERBOARD INSPECTION ---
        achievementManager.trackInspectHighScores()

        // 1. Check Dark Mode Preference
        val prefs = getSharedPreferences("NamagramPrefs", MODE_PRIVATE)
        isDarkMode = prefs.getBoolean("dark_mode_enabled", false)

        // Define dynamic colors based on dark mode state
        val backgroundColor = if (isDarkMode) "#0F172A" else "#FFFFFF"
        val titleColor = if (isDarkMode) Color.WHITE else ViewStyleUtility.COLOR_PRIMARY_SLATE.toColorInt()
        val dividerColor = if (isDarkMode) "#334155" else "#E2E8F0"

        soloCardBg = if (isDarkMode) "#1E293B" else "#EBF8FF"
        soloTextColor = if (isDarkMode) "#93C5FD" else "#1E3A8A"
        multiCardBg = if (isDarkMode) "#064E3B" else "#D1FAE5"
        multiTextColor = if (isDarkMode) "#A7F3D0" else "#064E3B"

        val displayMetrics = resources.displayMetrics
        fun dpToPx(dp: Int): Int = (dp * displayMetrics.density).toInt()

        val scrollView = ScrollView(this).apply {
            isFillViewport = true
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            setBackgroundColor(backgroundColor.toColorInt())
        }

        val scrollContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(16))
            gravity = Gravity.TOP
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        // 2. PAGE TITLE
        val tvTitle = TextView(this).apply {
            text = getString(R.string.high_scores_title)
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(titleColor)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dpToPx(8))
        }
        scrollContent.addView(tvTitle)


        // --- SECTION 1: SOLO TOP 5 ---
        val soloHeader = TextView(this).apply {
            text = getString(R.string.leaderboard_solo_header)
            textSize = 17.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(if (isDarkMode) "#60A5FA".toColorInt() else "#2980B9".toColorInt())
            gravity = Gravity.CENTER
            setPadding(0, dpToPx(6), 0, dpToPx(8))
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
        scrollContent.addView(soloHeader)

        soloContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
        scrollContent.addView(soloContainer)

        // Solo Clear Button
        val btnClearSolo = Button(this).apply {
            text = getString(R.string.btn_clear_solo_scores)
            textSize = 12f
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            background = ViewStyleUtility.createRoundedDrawable("#EF4444", 8f)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(40)).apply {
                setMargins(0, dpToPx(6), 0, 0)
            }
            setOnClickListener {
                scoreManager.clearSoloScores()
                Toast.makeText(this@HighScoresActivity, getString(R.string.toast_solo_cleared), Toast.LENGTH_SHORT).show()
                loadSoloScoresList()
            }
            ViewStyleUtility.attachButtonAnimateTouchListener(this)
        }
        scrollContent.addView(btnClearSolo)


        // --- SECTION SEPARATOR CONTAINER (HUGE GAP + DIVIDER LINE) ---
        val separatorContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(80)).apply {
                setMargins(0, dpToPx(16), 0, dpToPx(16))
            }
        }

        val dividerLine = View(this).apply {
            setBackgroundColor(dividerColor.toColorInt())
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(2))
        }
        separatorContainer.addView(dividerLine)
        scrollContent.addView(separatorContainer)


        // --- SECTION 2: MULTIPLAYER TOP 5 ---
        val multiHeader = TextView(this).apply {
            text = getString(R.string.leaderboard_multi_header)
            textSize = 17.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(if (isDarkMode) "#34D399".toColorInt() else "#27AE60".toColorInt())
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dpToPx(8))
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
        scrollContent.addView(multiHeader)

        multiContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
        scrollContent.addView(multiContainer)

        // Multiplayer Clear Button
        val btnClearMulti = Button(this).apply {
            text = getString(R.string.btn_clear_multi_scores)
            textSize = 12f
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            background = ViewStyleUtility.createRoundedDrawable("#EF4444", 8f)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(40)).apply {
                setMargins(0, dpToPx(6), 0, dpToPx(12))
            }
            setOnClickListener {
                scoreManager.clearMultiplayerScores()
                Toast.makeText(this@HighScoresActivity, getString(R.string.toast_multi_cleared), Toast.LENGTH_SHORT).show()
                loadMultiScoresList()
            }
            ViewStyleUtility.attachButtonAnimateTouchListener(this)
        }
        scrollContent.addView(btnClearMulti)

        // Populate initial data lists
        loadSoloScoresList()
        loadMultiScoresList()

        // 3. PERSISTENT MENU BUTTON
        ViewStyleUtility.addPersistentMenuButton(this, scrollContent)

        scrollView.addView(scrollContent)
        setContentView(scrollView)
    }

    private fun loadSoloScoresList() {
        val displayMetrics = resources.displayMetrics
        fun dpToPx(dp: Int): Int = (dp * displayMetrics.density).toInt()

        soloContainer.removeAllViews()
        val soloScores = scoreManager.getSoloHighScores()

        for (i in 1..5) {
            val rowView = TextView(this).apply {
                text = if (i <= soloScores.size) {
                    val entry = soloScores[i - 1]
                    "${i}.  ${entry.name}  —  ${entry.score} pts"
                } else {
                    "${i}.  ---"
                }
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                setTextColor(soloTextColor.toColorInt())
                setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8))
                background = ViewStyleUtility.createRoundedDrawable(soloCardBg, 8f)
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 0, 0, dpToPx(3))
                }
            }
            soloContainer.addView(rowView)
        }
    }

    private fun loadMultiScoresList() {
        val displayMetrics = resources.displayMetrics
        fun dpToPx(dp: Int): Int = (dp * displayMetrics.density).toInt()

        multiContainer.removeAllViews()
        val multiScores = scoreManager.getMultiplayerHighScores()

        for (i in 1..5) {
            val rowView = TextView(this).apply {
                text = if (i <= multiScores.size) {
                    val entry = multiScores[i - 1]
                    "${i}.  ${entry.name}  —  ${entry.score} pts"
                } else {
                    "${i}.  ---"
                }
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                setTextColor(multiTextColor.toColorInt())
                setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8))
                background = ViewStyleUtility.createRoundedDrawable(multiCardBg, 8f)
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 0, 0, dpToPx(3))
                }
            }
            multiContainer.addView(rowView)
        }
    }
}