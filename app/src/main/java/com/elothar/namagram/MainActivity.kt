package com.elothar.namagram

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.toColorInt
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var soundManager: SoundManager
    private lateinit var achievementManager: AchievementManager
    private var debugTapCount = 0

    private var lastKnownDarkModeState: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("NamagramPrefs", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode_enabled", false)

        // Keep track of the initial state so onResume can compare against it
        lastKnownDarkModeState = isDarkMode

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        super.onCreate(savedInstanceState)
        soundManager = SoundManager(this)
        achievementManager = AchievementManager(this)

        achievementManager.trackNightOwlAndWeekend()

        val displayMetrics = resources.displayMetrics
        fun dpToPx(dp: Int): Int = (dp * displayMetrics.density).toInt()

        // Dynamic Theme Colors
        val backgroundColor = if (isDarkMode) "#0F172A" else ViewStyleUtility.COLOR_BACKGROUND_LIGHT
        val cardBackgroundColor = if (isDarkMode) "#1E293B" else "#FFFFFF"
        val cardStrokeColor = if (isDarkMode) "#334155" else "#E2E8F0"
        val subtitleColor = if (isDarkMode) "#94A3B8" else ViewStyleUtility.COLOR_TEXT_MUTED

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            setBackgroundColor(backgroundColor.toColorInt())
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }

        val scrollView = ScrollView(this).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0).apply { weight = 1f }
            isFillViewport = true
            isVerticalScrollBarEnabled = false
        }

        val scrollContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val logoImage = ImageView(this).apply {
            setImageResource(R.drawable.namagram_logo)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            // Reverted back to the original compact 140dp size for the menu
            layoutParams = LayoutParams(dpToPx(140), dpToPx(140)).apply {
                setMargins(0, dpToPx(8), 0, dpToPx(4))
            }
            setOnClickListener {
                debugTapCount++
                if (debugTapCount >= 7) {
                    debugTapCount = 0
                    achievementManager.trackAppSettingsMenu("debug_menu")
                    Toast.makeText(this@MainActivity, "🔓 Secret Debug Menu Unlocked!", Toast.LENGTH_SHORT).show()
                    AchievementDebugHelper(this@MainActivity).showDebugDialog()
                }
            }
        }
        scrollContent.addView(logoImage)

        val tvSubtitle = TextView(this).apply {
            text = getString(R.string.main_menu_subtitle)
            textSize = 13f
            setTextColor(subtitleColor.toColorInt())
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(0, 0, 0, dpToPx(16))
        }
        scrollContent.addView(tvSubtitle)

        fun createSectionCard(): LinearLayout {
            return LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10))
                background = ViewStyleUtility.createRoundedDrawable(cardBackgroundColor, 16f)
                (background as? android.graphics.drawable.GradientDrawable)?.setStroke(2, cardStrokeColor.toColorInt())
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 0, 0, dpToPx(12))
                }
            }
        }

        fun addCardButton(card: LinearLayout, text: String, bgColor: String, onClick: () -> Unit) {
            Button(this).apply {
                this.text = text
                textSize = 14.5f
                setTextColor(Color.WHITE)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                background = ViewStyleUtility.createRoundedDrawable(bgColor, 12f)
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(48)).apply {
                    setMargins(0, 0, 0, dpToPx(8))
                }
                setOnClickListener { onClick() }
                ViewStyleUtility.attachButtonAnimateTouchListener(this)
            }.also { card.addView(it) }
        }

        // --- CARD 1: TUTORIAL & RULES ---
        val card1 = createSectionCard()
        addCardButton(card1, getString(R.string.btn_how_to_play), "#228B22") {
            achievementManager.trackExplorationAction("expl_tutorial")
            achievementManager.trackAppSettingsMenu("how_to_play")
            startActivity(Intent(this, HowToPlayActivity::class.java))
        }
        addCardButton(card1, getString(R.string.btn_game_rules), "#DC2626") {
            achievementManager.trackAppSettingsMenu("game_rules")
            startActivity(Intent(this, GameRulesActivity::class.java))
        }
        scrollContent.addView(card1)

        // --- CARD 2: GAME MODES ---
        val card2 = createSectionCard()
        addCardButton(card2, getString(R.string.btn_single_player_mode), "#10B981") {
            achievementManager.trackAppSettingsMenu("single_player")
            startActivity(Intent(this, SinglePlayerLobbyActivity::class.java))
        }
        addCardButton(card2, "📅 Daily Namagram Challenge", "#3498DB") {
            achievementManager.trackAppSettingsMenu("daily_puzzle_hub")
            startActivity(Intent(this, DailyPuzzleHubActivity::class.java))
        }
        addCardButton(card2, getString(R.string.btn_multiplayer), "#F59E0B") {
            achievementManager.trackAppSettingsMenu("multiplayer")
            startActivity(Intent(this, MultiplayerLobbyActivity::class.java))
        }
        scrollContent.addView(card2)

        // --- CARD 3: SCORES & ACHIEVEMENTS ---
        val card3 = createSectionCard()
        addCardButton(card3, getString(R.string.btn_high_scores), "#3B82F6") {
            val currentName = MatchSettings.player1Nickname
            val lower = currentName.trim().lowercase(Locale.ROOT)
            val isValidName = lower.isNotEmpty() &&
                    lower != "player 1" &&
                    lower != "player 2" &&
                    lower != "guest" &&
                    lower != "player1" &&
                    lower != "player2"

            if (isValidName) {
                achievementManager.targetPlayerOverride = currentName
                achievementManager.trackInspectHighScores()
            }

            achievementManager.trackAppSettingsMenu("high_scores")
            startActivity(Intent(this, HighScoresActivity::class.java))
        }
        addCardButton(card3, getString(R.string.btn_achievements), "#8B5CF6") {
            achievementManager.trackAppSettingsMenu("achievements")
            startActivity(Intent(this, AchievementsActivity::class.java))
        }

// 👉 Share Profile & Stats Button
        addCardButton(card3, "📤 Share Profile & Stats", "#059669") {
            achievementManager.trackAppSettingsMenu("share_profile")

            val menuPrefs = getSharedPreferences("NamagramPrefs", MODE_PRIVATE)
            val currentName = menuPrefs.getString("player1_nickname", menuPrefs.getString("user_profile_name", "Player 1")) ?: "Player 1"

            // 👉 Pull cumulative score directly from ScoreManager
            val scoreManager = ScoreManager(this)
            val cumulativeScore = scoreManager.getCumulativeScore()

            val unlockedCount = achievementManager.getUnlockedCount()
            val playerTier = getPlayerTierName(achievementManager)

            val shareBody = """
                🎮 My Namagram Profile Stats 🎮
                👤 Player: $currentName
                🎖️ Level: $playerTier
                🏆 Achievements Unlocked: $unlockedCount / 210
                ⭐ Cumulative Score: $cumulativeScore pts
                
                Can you beat my Namagram score?
            """.trimIndent()

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "My Namagram Profile & Stats")
                putExtra(Intent.EXTRA_TEXT, shareBody)
            }

            startActivity(Intent.createChooser(shareIntent, "Share Namagram Profile via"))
        }

        scrollContent.addView(card3)

        // --- CARD 4: SETTINGS ---
        val card4 = createSectionCard()
        addCardButton(card4, getString(R.string.btn_settings), "#475569") {
            achievementManager.trackAppSettingsMenu("settings")
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        scrollContent.addView(card4)

        scrollView.addView(scrollContent)
        rootLayout.addView(scrollView)
        setContentView(rootLayout)
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("NamagramPrefs", MODE_PRIVATE)
        val currentDarkModeSetting = prefs.getBoolean("dark_mode_enabled", false)

        if (currentDarkModeSetting != lastKnownDarkModeState) {
            lastKnownDarkModeState = currentDarkModeSetting
            recreate()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
// 👉 Place the helper function here
private fun getPlayerTierName(achievementManager: AchievementManager): String {
    val allAchievements = achievementManager.getAllAchievements()
    val isExpertUnlocked = allAchievements.find { it.id == "expert_gate" }?.isUnlocked == true
    val isInterUnlocked = allAchievements.find { it.id == "inter_gate" }?.isUnlocked == true

    return when {
        isExpertUnlocked -> "🏆 Expert Wordsmith"
        isInterUnlocked -> "⚔️ Intermediate Challenger"
        else -> "🌱 Beginner Scribe"
    }
}
