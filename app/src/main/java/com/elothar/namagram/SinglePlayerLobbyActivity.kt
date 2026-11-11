package com.elothar.namagram

import android.annotation.SuppressLint
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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import java.util.Locale
import androidx.core.content.edit
class SinglePlayerLobbyActivity : AppCompatActivity() {

    private lateinit var soundManager: SoundManager
    private lateinit var etProfileName: EditText

    // Dark mode state variables
    private var isDarkMode = false
    private var cardBackgroundColor = "#FFFFFF"
    private var cardSelectedColor = "#EFF6FF"
    private var textColorPrimary = "#374151"
    private var textMutedColor = "#718096"
    private var textDescColor = "#4B5563"
    private var achievementCardBg = "#EBF8FF"
    private var achievementTextColor = "#1E3A8A"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        soundManager = SoundManager(this)

        // 1. Check Dark Mode Preference
        val prefs = getSharedPreferences("NamagramPrefs", MODE_PRIVATE)
        isDarkMode = prefs.getBoolean("dark_mode_enabled", false)

        // Define dynamic colors based on dark mode state
        val backgroundColor = if (isDarkMode) "#0F172A" else ViewStyleUtility.COLOR_BACKGROUND_LIGHT
        cardBackgroundColor = if (isDarkMode) "#1E293B" else "#FFFFFF"
        cardSelectedColor = if (isDarkMode) "#172554" else "#EFF6FF"
        textColorPrimary = if (isDarkMode) "#FFFFFF" else "#374151"
        textMutedColor = if (isDarkMode) "#94A3B8" else ViewStyleUtility.COLOR_TEXT_MUTED
        textDescColor = if (isDarkMode) "#94A3B8" else "#4B5563"
        achievementCardBg = if (isDarkMode) "#064E3B" else "#EBF8FF"
        achievementTextColor = if (isDarkMode) "#A7F3D0" else "#1E3A8A"

        val displayMetrics = resources.displayMetrics
        fun dpToPx(dp: Int): Int = (dp * displayMetrics.density).toInt()

        // Unified key check
        val savedName = prefs.getString("player1_nickname", prefs.getString("user_profile_name", MatchSettings.player1Nickname)) ?: "Player 1"
        MatchSettings.updatePlayerProfile(this, savedName)

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12))
            setBackgroundColor(backgroundColor.toColorInt())
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }

        // Title Header
        val tvTitle = TextView(this).apply {
            ViewStyleUtility.applyHeaderStyle(this, "SINGLE PLAYER LOBBY", isMainTitle = true)
            setPadding(0, 0, 0, dpToPx(8))
            gravity = Gravity.CENTER_HORIZONTAL
        }
        rootLayout.addView(tvTitle)

        val scrollView = ScrollView(this).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0).apply {
                weight = 1f
            }
            isFillViewport = true
            isVerticalScrollBarEnabled = false
        }

        val scrollContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        // Helper to create wrapper cards for our 3 distinct blocks
        fun createSectionContainer(bgHex: String, strokeHex: String): LinearLayout {
            return LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12))
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(bgHex.toColorInt())
                    cornerRadius = dpToPx(16).toFloat()
                    setStroke(2, strokeHex.toColorInt())
                }
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 0, 0, dpToPx(14))
                }
            }
        }

        // ════════════════════════════════════════════════════════════════
        // BLOCK 1: PLAYER PROFILE, NICKNAME & ACHIEVEMENT PROGRESS
        // Distinct Tint: Soft Indigo / Blue
        // ════════════════════════════════════════════════════════════════
        val block1Bg = if (isDarkMode) "#1E1B4B" else "#EEF2FF"
        val block1Stroke = if (isDarkMode) "#3730A3" else "#C7D2FE"
        val block1Card = createSectionContainer(block1Bg, block1Stroke)

        @SuppressLint("SetTextI18n")
        val tvCardHeader = TextView(this).apply {
            text = "👤 YOUR PROFILE ACCOUNT"
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(textMutedColor.toColorInt())
            setPadding(2, 0, 0, 6)
        }
        block1Card.addView(tvCardHeader)

        etProfileName = EditText(this).apply {
            ViewStyleUtility.applyEditTextStyle(this, "Enter Nickname...")
            setText(MatchSettings.player1Nickname)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
        block1Card.addView(etProfileName)

        val btnSaveName = Button(this).apply {
            text = getString(R.string.save_nickname_btn)
            textSize = 12f
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            background = ViewStyleUtility.createRoundedDrawable("#2563EB", 8f)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(36)).apply {
                setMargins(0, dpToPx(8), 0, dpToPx(10))
            }
            setOnClickListener {
                val newName = etProfileName.text.toString().trim()
                if (newName.isNotEmpty()) {
                    MatchSettings.updatePlayerProfile(this@SinglePlayerLobbyActivity, newName)

                    val lobbyPrefs = getSharedPreferences("NamagramPrefs", MODE_PRIVATE)
                    lobbyPrefs.edit {
                        putString("player1_nickname", newName)
                    }

                    val achievementManager = AchievementManager(this@SinglePlayerLobbyActivity)
                    achievementManager.trackExplorationAction("expl_profile")

                    Toast.makeText(this@SinglePlayerLobbyActivity, "Profile updated to '$newName'!", Toast.LENGTH_SHORT).show()
                    recreate()
                } else {
                    Toast.makeText(this@SinglePlayerLobbyActivity, "Nickname cannot be empty.", Toast.LENGTH_SHORT).show()
                }
            }
            ViewStyleUtility.attachButtonAnimateTouchListener(this)
        }
        block1Card.addView(btnSaveName)

        // --- CHECK ACHIEVEMENT STATUS & GATE VARIABLES ---
        val achievementManager = AchievementManager(this)
        val allAchievements = achievementManager.getAllAchievements()
        val isInterUnlocked = allAchievements.find { it.id == "inter_gate" }?.isUnlocked == true
        val isExpertUnlocked = allAchievements.find { it.id == "expert_gate" }?.isUnlocked == true

        // 👉 Fetch lifetime cumulative score from ScoreManager
        val scoreManager = ScoreManager(this)
        val cumulativeScore = scoreManager.getCumulativeScore()

        // --- 1.5 ACHIEVEMENT STATUS & CUMULATIVE SCORE SUMMARY CARD ---
        val unlockedCount = achievementManager.getUnlockedCount()
        val nextGateText = when {
            !isInterUnlocked -> "🔒 Intermediate Gate at 70 ($unlockedCount/70)"
            !isExpertUnlocked -> "🔒 Expert Gate at 140 ($unlockedCount/140)"
            else -> "👑 All Gates Unlocked! ($unlockedCount/210)"
        }

        @SuppressLint("SetTextI18n")
        val achievementSummaryCard = TextView(this).apply {
            text = "🏆 Achievement Progress: $unlockedCount / 210 Unlocked\n⭐ Cumulative Score: $cumulativeScore pts\n$nextGateText"
            // ...
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(achievementTextColor.toColorInt())
            setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10))
            background = ViewStyleUtility.createRoundedDrawable(achievementCardBg, 12f)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            setLineSpacing(0f, 1.2f)
        }
        block1Card.addView(achievementSummaryCard)
        scrollContent.addView(block1Card)


        // ════════════════════════════════════════════════════════════════
        // BLOCK 2: TIME LIMIT / DIFFICULTY SELECTION
        // Distinct Tint: Soft Teal / Cyan (High contrast in light mode)
        // ════════════════════════════════════════════════════════════════
        val block2Bg = if (isDarkMode) "#042F2E" else "#F0FDFA"
        val block2Stroke = if (isDarkMode) "#115E59" else "#99F6E4"
        val block2Card = createSectionContainer(block2Bg, block2Stroke)

        @SuppressLint("SetTextI18n")
        val tvDiffSectionLabel = TextView(this).apply {
            text = "⏱️ CHOOSE TIME LIMIT"
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(textMutedColor.toColorInt())
            setPadding(2, 0, 0, dpToPx(8))
        }
        block2Card.addView(tvDiffSectionLabel)

        var selectedTimeLimit = DifficultySettings.getTimeLimit(this)

        fun createDifficultyCard(
            titleResId: Int,
            timeSeconds: Int,
            badgeColorHex: String,
            descResId: Int
        ) {
            val isSelected = (selectedTimeLimit == timeSeconds)
            val currentBg = if (isSelected) {
                if (isDarkMode) "#1E293B" else "#FFFFFF"
            } else {
                cardBackgroundColor
            }

            val strokeWidthPx = if (isSelected) dpToPx(3) else dpToPx(1)
            val strokeColorHex = if (isSelected) badgeColorHex else if (isDarkMode) "#334155" else "#CBD5E1"

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10))

                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(currentBg.toColorInt())
                    cornerRadius = dpToPx(12).toFloat()
                    setStroke(strokeWidthPx, strokeColorHex.toColorInt())
                }

                elevation = if (isSelected) 4f else 1f

                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 0, 0, dpToPx(8))
                }

                setOnClickListener {
                    selectedTimeLimit = timeSeconds
                    DifficultySettings.saveTimeLimit(this@SinglePlayerLobbyActivity, timeSeconds)
                    MatchSettings.roundTimeLimitSeconds = timeSeconds
                    recreate()
                }
            }

            val headerRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            }

            val titleText = getString(titleResId) + if (isSelected) "  ✓ [ACTIVE]" else ""
            val tvCardTitle = TextView(this).apply {
                text = titleText
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, if (isSelected) Typeface.BOLD else Typeface.NORMAL)
                setTextColor(badgeColorHex.toColorInt())
                layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            }
            headerRow.addView(tvCardTitle)

            val tvTime = TextView(this).apply {
                text = getString(R.string.diff_time_format, timeSeconds)
                textSize = 12.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                setTextColor(textColorPrimary.toColorInt())
            }
            headerRow.addView(tvTime)
            card.addView(headerRow)

            val tvDesc = TextView(this).apply {
                text = getString(descResId)
                textSize = 11f
                setTextColor(textDescColor.toColorInt())
                setPadding(0, dpToPx(2), 0, 0)
            }
            card.addView(tvDesc)

            block2Card.addView(card)
        }

        createDifficultyCard(R.string.diff_card_easy, 90, "#059669", R.string.diff_desc_easy)
        createDifficultyCard(R.string.diff_card_medium, 60, "#2563EB", R.string.diff_desc_medium)
        createDifficultyCard(R.string.diff_card_hard, 30, "#DC2626", R.string.diff_desc_hard)

        scrollContent.addView(block2Card)


        // ════════════════════════════════════════════════════════════════
        // BLOCK 3: CHOOSE NUMBER OF TILES TO START
        // Distinct Tint: Warm Amber / Gold
        // ════════════════════════════════════════════════════════════════
        val block3Bg = if (isDarkMode) "#451A03" else "#FFFBEB"
        val block3Stroke = if (isDarkMode) "#B45309" else "#FDE68A"
        val block3Card = createSectionContainer(block3Bg, block3Stroke)

        @SuppressLint("SetTextI18n")
        val tvTileSectionLabel = TextView(this).apply {
            text = "🧩 CHOOSE NUMBER OF TILES TO START"
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(textMutedColor.toColorInt())
            setPadding(2, 0, 0, dpToPx(8))
        }
        block3Card.addView(tvTileSectionLabel)

        val maxTileAllowed = when {
            isExpertUnlocked -> 10
            isInterUnlocked -> 7
            else -> 4
        }

        val tileIcons = listOf("🧩", "🎲", "🎯", "⭐", "🔥", "👑")

        for (i in 3..maxTileAllowed) {
            val iconEmoji = tileIcons[(i - 3) % tileIcons.size]
            val btnOption = Button(this).apply {
                text = getString(R.string.tile_option_format, iconEmoji, i)
                textSize = 14f
                setTextColor(Color.WHITE)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                background = ViewStyleUtility.createRoundedDrawable(ViewStyleUtility.COLOR_PRIMARY_SLATE, 10f)

                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(42)).apply {
                    setMargins(0, 0, 0, dpToPx(6))
                }

                setOnClickListener {
                    saveProfileNicknameToDisk()

                    val currentName = etProfileName.text.toString().trim()
                    val lower = currentName.lowercase(Locale.ROOT)
                    val isDefaultName = lower.isEmpty() ||
                            lower == "player 1" ||
                            lower == "player 2" ||
                            lower == "guest" ||
                            lower == "player1" ||
                            lower == "player2"

                    if (isDefaultName) {
                        val inputEditText = EditText(this@SinglePlayerLobbyActivity).apply {
                            hint = "Enter custom nickname"
                            setPadding(40, 30, 40, 30)
                        }

                        AlertDialog.Builder(this@SinglePlayerLobbyActivity)
                            .setTitle("✏️ Custom Profile Required")
                            .setMessage("To track achievements and high scores, please enter a custom player name:")
                            .setView(inputEditText)
                            .setPositiveButton("Save & Play") { dialog, _ ->
                                val enteredName = inputEditText.text.toString().trim()
                                val enteredLower = enteredName.lowercase(Locale.ROOT)
                                val isValid = enteredLower.isNotEmpty() &&
                                        enteredLower != "player 1" &&
                                        enteredLower != "player 2" &&
                                        enteredLower != "guest" &&
                                        enteredLower != "player1" &&
                                        enteredLower != "player2"

                                if (isValid) {
                                    etProfileName.setText(enteredName)
                                    MatchSettings.updatePlayerProfile(this@SinglePlayerLobbyActivity, enteredName)
                                    saveProfileNicknameToDisk()
                                    dialog.dismiss()
                                    launchGameWithTileCount(i)
                                } else {
                                    Toast.makeText(this@SinglePlayerLobbyActivity, "Please enter a valid unique nickname.", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .setNegativeButton("Play as Guest (No Achievements)") { dialog, _ ->
                                dialog.dismiss()
                                MatchSettings.updatePlayerProfile(this@SinglePlayerLobbyActivity, currentName.ifEmpty { "Player 1" })
                                launchGameWithTileCount(i)
                            }
                            .show()
                    } else {
                        MatchSettings.updatePlayerProfile(this@SinglePlayerLobbyActivity, currentName)
                        launchGameWithTileCount(i)
                    }
                }
                ViewStyleUtility.attachButtonAnimateTouchListener(this)
            }
            block3Card.addView(btnOption)
        }

        scrollContent.addView(block3Card)

        scrollView.addView(scrollContent)
        rootLayout.addView(scrollView)

        // Persistent Return to Main Menu button pinned cleanly at the bottom
        ViewStyleUtility.addPersistentMenuButton(this, rootLayout)

        setContentView(rootLayout)
    }

    private fun launchGameWithTileCount(tileCount: Int) {
        val currentProfileName = etProfileName.text.toString().trim().ifEmpty { "Player 1" }
        MatchSettings.updatePlayerProfile(this, currentProfileName)
        MatchSettings.setupSinglePlayerEngine(currentProfileName)
        MatchSettings.totalLettersToDraw = tileCount
        MatchSettings.selectedTileCount = tileCount
        MatchSettings.totalRoundsSelected = 0

        val intent = Intent(this@SinglePlayerLobbyActivity, GameActivity::class.java)
        startActivity(intent)
    }

    private fun saveProfileNicknameToDisk() {
        val currentName = etProfileName.text.toString().trim()
        if (currentName.isNotEmpty()) {
            MatchSettings.updatePlayerProfile(this, currentName)
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("NamagramPrefs", MODE_PRIVATE)
        val savedName = prefs.getString("player1_nickname", prefs.getString("user_profile_name", "Player 1")) ?: "Player 1"
        MatchSettings.updatePlayerProfile(this, savedName)
        if (::etProfileName.isInitialized) {
            etProfileName.setText(MatchSettings.player1Nickname)
        }
    }

    override fun onPause() {
        saveProfileNicknameToDisk()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}