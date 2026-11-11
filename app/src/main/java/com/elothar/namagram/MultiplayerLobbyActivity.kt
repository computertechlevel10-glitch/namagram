package com.elothar.namagram

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt

class MultiplayerLobbyActivity : AppCompatActivity() {

    private lateinit var etPlayer1Name: EditText
    private lateinit var etPlayer2Name: EditText

    // Centralized choice value labels displayed between the buttons
    private lateinit var tvRoundsValue: TextView
    private lateinit var tvTilesValue: TextView
    private lateinit var tvTimerValue: TextView

    private lateinit var btnLaunch: Button

    private lateinit var soundManager: SoundManager

    // Dynamic tile restrictions based on player tier
    private var minTiles = 3
    private var maxTiles = 4 // Default cap for beginners

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        soundManager = SoundManager(this)

        // 1. Check Dark Mode Preference
        val prefs = getSharedPreferences("NamagramPrefs", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode_enabled", false)

        // Define dynamic colors based on dark mode state
        val backgroundColor = if (isDarkMode) "#0F172A" else ViewStyleUtility.COLOR_BACKGROUND_LIGHT
        val textColorPrimary = if (isDarkMode) Color.WHITE else ViewStyleUtility.COLOR_TEXT_DARK.toColorInt()
        val textMutedColor = if (isDarkMode) "#94A3B8" else ViewStyleUtility.COLOR_TEXT_MUTED
        val selectorContainerBg = if (isDarkMode) "#1E293B" else "#F1F5F9"

        // Block 1 colors (Player Names): Soft Indigo
        val block1Bg = if (isDarkMode) "#1E1B4B" else "#EEF2FF"
        val block1Stroke = if (isDarkMode) "#3730A3" else "#C7D2FE"

        // Block 2 colors (Match Settings / Selectors): Distinct Soft Teal (High contrast against white background)
        val block2Bg = if (isDarkMode) "#042F2E" else "#F0FDFA"
        val block2Stroke = if (isDarkMode) "#115E59" else "#99F6E4"

        // Determine player tier permissions using AchievementManager gate IDs (70 & 140 thresholds)
        val achievementManager = AchievementManager(this)
        val allAchievements = achievementManager.getAllAchievements()
        val isInterUnlocked = allAchievements.find { it.id == "inter_gate" }?.isUnlocked == true
        val isExpertUnlocked = allAchievements.find { it.id == "expert_gate" }?.isUnlocked == true

        minTiles = 3
        maxTiles = when {
            isExpertUnlocked -> 10
            isInterUnlocked -> 7
            else -> 4
        }

        // Initialize tile count safely within bounds if uninitialized
        if (MatchSettings.selectedTileCount !in minTiles..maxTiles) {
            MatchSettings.selectedTileCount = minTiles
        }
        if (MatchSettings.totalRoundsSelected == 0) {
            MatchSettings.totalRoundsSelected = 3
        }

        // Ensure default timer is safely initialized to one of the valid options (e.g., 60 seconds)
        if (MatchSettings.roundTimeLimitSeconds != 30 && MatchSettings.roundTimeLimitSeconds != 60 && MatchSettings.roundTimeLimitSeconds != 90) {
            MatchSettings.roundTimeLimitSeconds = 60
        }

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(32, 24, 32, 16)
            setBackgroundColor(backgroundColor.toColorInt())
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

            alpha = 0f
            translationY = 20f
            animate().alpha(1f).translationY(0f).setDuration(400).start()
        }

        val tvTitle = TextView(this).apply {
            ViewStyleUtility.applyHeaderStyle(this, getString(R.string.lobby_title), isMainTitle = true)
            setTextColor(textColorPrimary)
            setPadding(0, 0, 0, 12)
        }
        rootLayout.addView(tvTitle)

        val scrollView = ScrollView(this).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0).apply { weight = 1.0f }
            isFillViewport = true
            isVerticalScrollBarEnabled = false
        }

        val contentContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        // ================================================================
        // BLOCK 1: PLAYER NICKNAMES
        // ================================================================
        val block1Card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 24, 28, 24)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(block1Bg.toColorInt())
                cornerRadius = 24f
                setStroke(2, block1Stroke.toColorInt())
            }
            // 5x top margin space between the header "Multiplayer Lobby" and Block 1
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 60, 0, 0)
            }
        }

        val tvCardHeader = TextView(this).apply {
            text = getString(R.string.lobby_card_setup_header)
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(textColorPrimary)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }
        block1Card.addView(tvCardHeader)

        val tvP1Title = TextView(this).apply {
            text = getString(R.string.lobby_p1_nickname_label)
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(textMutedColor.toColorInt())
            setPadding(2, 0, 0, 6)
        }
        block1Card.addView(tvP1Title)

        etPlayer1Name = EditText(this).apply {
            ViewStyleUtility.applyEditTextStyle(this, "e.g., WordMaster")
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 16) }
        }
        block1Card.addView(etPlayer1Name)

        val tvP2Title = TextView(this).apply {
            text = getString(R.string.lobby_p2_nickname_label)
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(textMutedColor.toColorInt())
            setPadding(2, 0, 0, 6)
        }
        block1Card.addView(tvP2Title)

        etPlayer2Name = EditText(this).apply {
            ViewStyleUtility.applyEditTextStyle(this, "e.g., LetterKing")
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
        block1Card.addView(etPlayer2Name)

        contentContainer.addView(block1Card)


        // ================================================================
        // BLOCK 2: MATCH CHOICES (ROUNDS, TILES, TIMER) & START BUTTON
        // ================================================================
        val block2Card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 24, 28, 24)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(block2Bg.toColorInt())
                cornerRadius = 24f
                setStroke(2, block2Stroke.toColorInt())
            }
            // 5x top margin space between Block 1 and Block 2
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 100, 0, 16)
            }
        }

        // --- ROUNDS SELECTOR ---
        val tvRoundsTitle = TextView(this).apply {
            text = getString(R.string.lobby_rounds_title)
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(textMutedColor.toColorInt())
            setPadding(2, 0, 0, 6)
        }
        block2Card.addView(tvRoundsTitle)

        val roundsSelectorContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8, 8, 8, 8)
            background = ViewStyleUtility.createRoundedDrawable(selectorContainerBg, 12f)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 20) }
        }

        val btnMinusRound = Button(this).apply {
            text = "–"
            textSize = 15f
            setTextColor(Color.WHITE)
            background = ViewStyleUtility.createRoundedDrawable(ViewStyleUtility.COLOR_PRIMARY_SLATE, 6f)
            layoutParams = LayoutParams(100, 85)
            setOnClickListener {
                if (MatchSettings.totalRoundsSelected > 1) {
                    MatchSettings.totalRoundsSelected--
                    updateLobbyUIMetricsDisplay()
                }
            }
            ViewStyleUtility.attachButtonAnimateTouchListener(this)
        }
        roundsSelectorContainer.addView(btnMinusRound)

        tvRoundsValue = TextView(this).apply {
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor("#DC2626".toColorInt())
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        }
        roundsSelectorContainer.addView(tvRoundsValue)

        val btnPlusRound = Button(this).apply {
            text = "+"
            textSize = 15f
            setTextColor(Color.WHITE)
            background = ViewStyleUtility.createRoundedDrawable(ViewStyleUtility.COLOR_PRIMARY_SLATE, 6f)
            layoutParams = LayoutParams(100, 85)
            setOnClickListener {
                if (MatchSettings.totalRoundsSelected < 10) {
                    MatchSettings.totalRoundsSelected++
                    updateLobbyUIMetricsDisplay()
                }
            }
            ViewStyleUtility.attachButtonAnimateTouchListener(this)
        }
        roundsSelectorContainer.addView(btnPlusRound)
        block2Card.addView(roundsSelectorContainer)

        // --- TILE COUNT SELECTOR ---
        val tvTilesTitle = TextView(this).apply {
            text = getString(R.string.lobby_target_tiles_title, minTiles, maxTiles)
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(textMutedColor.toColorInt())
            setPadding(2, 0, 0, 6)
        }
        block2Card.addView(tvTilesTitle)

        val tilesSelectorContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8, 8, 8, 8)
            background = ViewStyleUtility.createRoundedDrawable(selectorContainerBg, 12f)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 20) }
        }

        val btnMinusTile = Button(this).apply {
            text = "–"
            textSize = 15f
            setTextColor(Color.WHITE)
            background = ViewStyleUtility.createRoundedDrawable(ViewStyleUtility.COLOR_PRIMARY_SLATE, 6f)
            layoutParams = LayoutParams(100, 85)
            setOnClickListener {
                if (MatchSettings.selectedTileCount > minTiles) {
                    MatchSettings.selectedTileCount--
                    updateLobbyUIMetricsDisplay()
                }
            }
            ViewStyleUtility.attachButtonAnimateTouchListener(this)
        }
        tilesSelectorContainer.addView(btnMinusTile)

        tvTilesValue = TextView(this).apply {
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor("#DC2626".toColorInt())
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        }
        tilesSelectorContainer.addView(tvTilesValue)

        val btnPlusTile = Button(this).apply {
            text = "+"
            textSize = 15f
            setTextColor(Color.WHITE)
            background = ViewStyleUtility.createRoundedDrawable(ViewStyleUtility.COLOR_PRIMARY_SLATE, 6f)
            layoutParams = LayoutParams(100, 85)
            setOnClickListener {
                if (MatchSettings.selectedTileCount < maxTiles) {
                    MatchSettings.selectedTileCount++
                    updateLobbyUIMetricsDisplay()
                }
            }
            ViewStyleUtility.attachButtonAnimateTouchListener(this)
        }
        tilesSelectorContainer.addView(btnPlusTile)
        block2Card.addView(tilesSelectorContainer)

        // --- TIMER SELECTOR (30 / 60 / 90 seconds) ---
        val tvTimerTitle = TextView(this).apply {
            text = getString(R.string.lobby_timer_title)
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(textMutedColor.toColorInt())
            setPadding(2, 0, 0, 6)
        }
        block2Card.addView(tvTimerTitle)

        val timerSelectorContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8, 8, 8, 8)
            background = ViewStyleUtility.createRoundedDrawable(selectorContainerBg, 12f)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 16) }
        }

        val allowedTimers = listOf(30, 60, 90)

        val btnMinusTimer = Button(this).apply {
            text = "◄"
            textSize = 15f
            setTextColor(Color.WHITE)
            background = ViewStyleUtility.createRoundedDrawable(ViewStyleUtility.COLOR_PRIMARY_SLATE, 6f)
            layoutParams = LayoutParams(100, 85)
            setOnClickListener {
                val currentIndex = allowedTimers.indexOf(MatchSettings.roundTimeLimitSeconds)
                val newIndex = if (currentIndex > 0) currentIndex - 1 else allowedTimers.size - 1
                MatchSettings.roundTimeLimitSeconds = allowedTimers[newIndex]
                updateLobbyUIMetricsDisplay()
            }
            ViewStyleUtility.attachButtonAnimateTouchListener(this)
        }
        timerSelectorContainer.addView(btnMinusTimer)

        tvTimerValue = TextView(this).apply {
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor("#DC2626".toColorInt())
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        }
        timerSelectorContainer.addView(tvTimerValue)

        val btnPlusTimer = Button(this).apply {
            text = "►"
            textSize = 15f
            setTextColor(Color.WHITE)
            background = ViewStyleUtility.createRoundedDrawable(ViewStyleUtility.COLOR_PRIMARY_SLATE, 6f)
            layoutParams = LayoutParams(100, 85)
            setOnClickListener {
                val currentIndex = allowedTimers.indexOf(MatchSettings.roundTimeLimitSeconds)
                val newIndex = if (currentIndex < allowedTimers.size - 1) currentIndex + 1 else 0
                MatchSettings.roundTimeLimitSeconds = allowedTimers[newIndex]
                updateLobbyUIMetricsDisplay()
            }
            ViewStyleUtility.attachButtonAnimateTouchListener(this)
        }
        timerSelectorContainer.addView(btnPlusTimer)
        block2Card.addView(timerSelectorContainer)

        // Populate initial values into the center text views
        updateLobbyUIMetricsDisplay()

        // START MATCH BUTTON
        btnLaunch = Button(this).apply {
            text = getString(R.string.lobby_start_match_btn)
            textSize = 16f
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            background = ViewStyleUtility.createRoundedDrawable(ViewStyleUtility.COLOR_SUCCESS_EMERALD, 8f)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 120).apply { setMargins(0, 16, 0, 4) }
            setOnClickListener { launchTournamentGameActivity() }
            ViewStyleUtility.attachButtonAnimateTouchListener(this)
        }
        block2Card.addView(btnLaunch)

        contentContainer.addView(block2Card)

        scrollView.addView(contentContainer)
        rootLayout.addView(scrollView)

        // RETURN TO MAIN MENU BUTTON
        val btnMenu = Button(this).apply {
            text = getString(R.string.btn_return_main_menu)
            textSize = 15f
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            background = ViewStyleUtility.createRoundedDrawable(ViewStyleUtility.COLOR_PRIMARY_SLATE, 8f)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 120).apply { setMargins(0, 8, 0, 8) }
            setOnClickListener { finish() }
            ViewStyleUtility.attachButtonAnimateTouchListener(this)
        }
        rootLayout.addView(btnMenu)

        setContentView(rootLayout)
    }

    private fun updateLobbyUIMetricsDisplay() {
        tvRoundsValue.text = getString(R.string.lobby_rounds_value_format, MatchSettings.totalRoundsSelected)
        tvTilesValue.text = getString(R.string.lobby_tiles_value_format, MatchSettings.selectedTileCount)
        tvTimerValue.text = getString(R.string.lobby_timer_value_format, MatchSettings.roundTimeLimitSeconds)
    }

    private fun launchTournamentGameActivity() {
        val p1Name = etPlayer1Name.text.toString().trim()
        val p2Name = etPlayer2Name.text.toString().trim()
        MatchSettings.player1Nickname = p1Name.ifEmpty { "Player 1" }
        MatchSettings.player2Nickname = p2Name.ifEmpty { "Player 2" }
        MatchSettings.currentRoundNumber = 1
        MatchSettings.isPlayer1Turn = true
        MatchSettings.player1TotalScore = 0
        MatchSettings.player2TotalScore = 0
        MatchSettings.player1RoundScoresList.clear()
        MatchSettings.player2RoundScoresList.clear()

        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
        finish()
    }
}