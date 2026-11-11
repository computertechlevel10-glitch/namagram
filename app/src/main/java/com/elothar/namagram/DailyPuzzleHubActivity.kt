package com.elothar.namagram

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import java.util.Calendar
import java.util.TimeZone
import java.text.SimpleDateFormat
import java.util.Locale
import android.widget.ScrollView

class DailyPuzzleHubActivity : AppCompatActivity() {

    private lateinit var soundManager: SoundManager
    private lateinit var prefs: SharedPreferences
    private lateinit var containerLayout: LinearLayout
    private var isDarkMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        soundManager = SoundManager(this)
        prefs = getSharedPreferences("NamagramPrefs", MODE_PRIVATE)

        // Sync stored nickname on startup
        val savedName = prefs.getString("player1_nickname", MatchSettings.player1Nickname) ?: "Player 1"
        MatchSettings.player1Nickname = savedName

        // Validate if player has a valid custom profile before opening the hub
        if (!hasValidProfile()) {
            showProfileRequiredDialog()
            return
        }

        isDarkMode = prefs.getBoolean("dark_mode_enabled", false)
        setupHubUI()
    }

    private fun hasValidProfile(): Boolean {
        val rawName = (MatchSettings.player1Nickname.takeIf { it.isNotBlank() } ?: "").trim()
        val lower = rawName.lowercase(Locale.ROOT)
        return rawName.isNotEmpty() &&
                lower != "player 1" &&
                lower != "player 2" &&
                lower != "guest" &&
                lower != "player1" &&
                lower != "player2"
    }

    private fun showProfileRequiredDialog() {
        val isCurrentDark = prefs.getBoolean("dark_mode_enabled", false)

        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            background = ViewStyleUtility.createRoundedDrawable(
                if (isCurrentDark) "#1E293B" else "#FFFFFF",
                24f
            ).apply {
                setStroke(4, "#3498DB".toColorInt())
            }
        }

        val tvHeader = TextView(this).apply {
            text = getString(R.string.profile_required_title)
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor("#3498DB".toColorInt())
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 12)
        }
        dialogView.addView(tvHeader)

        val tvMessage = TextView(this).apply {
            text = getString(R.string.profile_required_message)
            textSize = 14f
            setTextColor(if (isCurrentDark) Color.WHITE else Color.BLACK)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }
        dialogView.addView(tvMessage)

        val etNicknameInput = EditText(this).apply {
            ViewStyleUtility.applyEditTextStyle(this, "Enter your nickname...")
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 24)
            }
        }
        dialogView.addView(etNicknameInput)

        val buttonsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        val btnCancel = Button(this).apply {
            text = getString(android.R.string.cancel)
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            background = ViewStyleUtility.createRoundedDrawable("#94A3B8", 12f)
            layoutParams = LayoutParams(0, 110, 1.0f).apply {
                setMargins(0, 0, 8, 0)
            }
            setOnClickListener {
                soundManager.playClickSound()
                finish()
            }
            ViewStyleUtility.attachButtonAnimateTouchListener(this)
        }
        buttonsLayout.addView(btnCancel)

        val btnSave = Button(this).apply {
            text = getString(android.R.string.ok)
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            background = ViewStyleUtility.createRoundedDrawable("#3498DB", 12f)
            layoutParams = LayoutParams(0, 110, 1.0f).apply {
                setMargins(8, 0, 0, 0)
            }
            ViewStyleUtility.attachButtonAnimateTouchListener(this)
        }
        buttonsLayout.addView(btnSave)
        dialogView.addView(buttonsLayout)

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnSave.setOnClickListener {
            soundManager.playClickSound()
            val enteredName = etNicknameInput.text.toString().trim()

            val lower = enteredName.lowercase(Locale.ROOT)
            val isValid = enteredName.isNotEmpty() &&
                    lower != "player 1" &&
                    lower != "player 2" &&
                    lower != "guest" &&
                    lower != "player1" &&
                    lower != "player2"

            if (isValid) {
                MatchSettings.updatePlayerProfile(this, enteredName)

                Toast.makeText(this, "Profile saved! Welcome, $enteredName!", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()

                isDarkMode = prefs.getBoolean("dark_mode_enabled", false)
                setupHubUI()
            } else {
                Toast.makeText(this, "Please enter a valid unique nickname.", Toast.LENGTH_SHORT).show()
            }
        }

        alertDialog.show()
    }

    private fun setupHubUI() {
        val backgroundColor = if (isDarkMode) {
            "#0F172A".toColorInt()
        } else {
            ViewStyleUtility.COLOR_BACKGROUND_LIGHT.toColorInt()
        }
        val cardBackgroundColor = if (isDarkMode) "#1E293B" else "#FFFFFF"
        val textColorPrimary = if (isDarkMode) Color.WHITE else ViewStyleUtility.COLOR_TEXT_DARK.toColorInt()
        val cardStrokeColor = if (isDarkMode) "#334155" else "#E2E8F0"

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(48, 48, 48, 48)
            setBackgroundColor(backgroundColor)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }

        val tvTitle = TextView(this).apply {
            text = getString(R.string.daily_puzzle_title)
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor("#3498DB".toColorInt())
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 8)

            setOnLongClickListener {
                AchievementDebugHelper(this@DailyPuzzleHubActivity).showDebugDialog()
                true
            }
        }
        rootLayout.addView(tvTitle)

        val textMutedColor = if (isDarkMode) {
            "#94A3B8".toColorInt()
        } else {
            ViewStyleUtility.COLOR_TEXT_MUTED.toColorInt()
        }
        val tvSubtitle = TextView(this).apply {
            text = getString(R.string.daily_puzzle_subtitle)
            textSize = 14f
            setTextColor(textMutedColor)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }
        rootLayout.addView(tvSubtitle)

        val btnGallery = Button(this).apply {
            text = getString(R.string.btn_view_daily_master_gallery)
            textSize = 14f
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            background = ViewStyleUtility.createRoundedDrawable("#F39C12", 12f)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 110).apply {
                setMargins(0, 0, 0, 12)
            }
            setOnClickListener {
                soundManager.playClickSound()
                val intent = Intent(this@DailyPuzzleHubActivity, DailyMasterGalleryActivity::class.java)
                startActivity(intent)
            }
            ViewStyleUtility.attachButtonAnimateTouchListener(this)
        }
        rootLayout.addView(btnGallery)

// 👉 Dedicated Share Daily Challenge Results Button
        val btnShareDaily = Button(this).apply {
            text = getString(R.string.btn_share_daily_status) // 👈 Fixed: Using Android resource string
            textSize = 14f
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            background = ViewStyleUtility.createRoundedDrawable("#059669", 12f) // Emerald accent
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 110).apply {
                setMargins(0, 0, 0, 24)
            }
            setOnClickListener {
                soundManager.playClickSound()
                shareDailyChallengeStatus()
            }
            ViewStyleUtility.attachButtonAnimateTouchListener(this)
        }
        rootLayout.addView(btnShareDaily)

        val scrollView = ScrollView(this).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f).apply {
                setMargins(0, 0, 0, 16)
            }
            isFillViewport = false
            isVerticalScrollBarEnabled = true
        }

        val scrollContentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        containerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        scrollContentLayout.addView(containerLayout)
        scrollView.addView(scrollContentLayout)
        rootLayout.addView(scrollView)

        populateDailyTileOptions(containerLayout, cardBackgroundColor, textColorPrimary, cardStrokeColor)

        checkAndAwardDailyMasterAchievement()

        val btnBack = Button(this).apply {
            text = getString(R.string.btn_return_main_menu)
            textSize = 16f
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            background = ViewStyleUtility.createRoundedDrawable(ViewStyleUtility.COLOR_PRIMARY_SLATE, 12f)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 130).apply {
                setMargins(0, 16, 0, 0)
            }
            setOnClickListener {
                soundManager.playClickSound()
                finish()
            }
            ViewStyleUtility.attachButtonAnimateTouchListener(this)
        }
        rootLayout.addView(btnBack)

        setContentView(rootLayout)
    }

    private fun shareDailyChallengeStatus() {
        val currentName = prefs.getString("player1_nickname", "Player 1") ?: "Player 1"

        // Calculate current day's progress stats
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val dateKeyString = "${calendar.get(Calendar.YEAR)}_${calendar.get(Calendar.DAY_OF_YEAR)}"

        val completedCount = (3..10).count { tileCount ->
            val statusPrefKey = "daily_status_${dateKeyString}_${tileCount}"
            prefs.getString(statusPrefKey, "UNPLAYED") == "WON"
        }

        // 👉 Check if today's Master Badge was achieved and count total historical badges
        val masterBadgePrefKey = "daily_master_badge_${dateKeyString}"
        val hasMasterBadgeToday = prefs.getBoolean(masterBadgePrefKey, false)
        val badgeStatusEmoji = if (hasMasterBadgeToday) "🏆 Earned!" else "⏳ In Progress"

        val masterBadgeHistory = prefs.getStringSet("master_badge_history", emptySet()) ?: emptySet()
        val totalMasterBadges = masterBadgeHistory.size

        val dailyShareBody = """
            📅 Namagram Daily Challenge Progress 📅
            👤 Player: $currentName
            🧩 Completed Today: $completedCount / 8 Tile Puzzles
            ⭐ Today's Master Badge: $badgeStatusEmoji
            👑 Total Daily Master Badges: $totalMasterBadges
            
            Can you conquer today's Namagram challenges?
        """.trimIndent()

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "My Namagram Daily Challenge")
            putExtra(Intent.EXTRA_TEXT, dailyShareBody)
        }

        startActivity(Intent.createChooser(shareIntent, "Share Daily Challenge via"))
    }

    override fun onResume() {
        super.onResume()
        if (!hasValidProfile()) return

        prefs = getSharedPreferences("NamagramPrefs", MODE_PRIVATE)
        isDarkMode = prefs.getBoolean("dark_mode_enabled", false)
        val cardBackgroundColor = if (isDarkMode) "#1E293B" else "#FFFFFF"
        val textColorPrimary = if (isDarkMode) Color.WHITE else ViewStyleUtility.COLOR_TEXT_DARK.toColorInt()
        val cardStrokeColor = if (isDarkMode) "#334155" else "#E2E8F0"

        if (::containerLayout.isInitialized) {
            populateDailyTileOptions(containerLayout, cardBackgroundColor, textColorPrimary, cardStrokeColor)
            checkAndAwardDailyMasterAchievement()
        }
    }

    private fun populateDailyTileOptions(
        container: LinearLayout,
        cardBg: String,
        textColor: Int,
        strokeColor: String
    ) {
        container.removeAllViews()

        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val dateKeyString = "${calendar.get(Calendar.YEAR)}_${calendar.get(Calendar.DAY_OF_YEAR)}"

        for (tileCount in 3..10) {
            val statusPrefKey = "daily_status_${dateKeyString}_${tileCount}"
            val legacyPrefKey = "daily_completed_${dateKeyString}_${tileCount}"
            val legacyCompleted = prefs.getBoolean(legacyPrefKey, false)

            val currentStatus = prefs.getString(statusPrefKey, if (legacyCompleted) "WON" else "UNPLAYED") ?: "UNPLAYED"
            val isAttempted = currentStatus == "WON" || currentStatus == "FAILED"

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(24, 20, 24, 20)
                background = ViewStyleUtility.createRoundedDrawable(cardBg, 16f).apply {
                    setStroke(2, strokeColor.toColorInt())
                }
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 0, 0, 16)
                }
            }

            val infoLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f)
            }

            val tvCardTitle = TextView(this).apply {
                text = getString(R.string.daily_puzzle_tile_format, tileCount)
                textSize = 16f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                setTextColor(textColor)
            }
            infoLayout.addView(tvCardTitle)

            val tvStatusSubtitle = TextView(this).apply {
                text = when (currentStatus) {
                    "WON" -> getString(R.string.daily_puzzle_won_subtitle)
                    "FAILED" -> getString(R.string.daily_puzzle_failed_subtitle)
                    else -> getString(R.string.daily_puzzle_hidden_subtitle)
                }
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

                val subtitleColor = when (currentStatus) {
                    "WON" -> "#2ECC71".toColorInt()
                    "FAILED" -> "#E74C3C".toColorInt()
                    else -> "#94A3B8".toColorInt()
                }
                setTextColor(subtitleColor)
                setPadding(0, 4, 0, 0)
            }
            infoLayout.addView(tvStatusSubtitle)
            card.addView(infoLayout)

            val btnPlayDaily = Button(this).apply {
                text = when (currentStatus) {
                    "WON" -> getString(R.string.btn_won)
                    "FAILED" -> getString(R.string.btn_lost)
                    else -> getString(R.string.btn_play_daily)
                }
                textSize = 14f
                setTextColor(Color.WHITE)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

                val btnColor = when (currentStatus) {
                    "WON" -> "#27AE60"
                    "FAILED" -> "#C0392B"
                    else -> ViewStyleUtility.COLOR_SUCCESS_EMERALD
                }
                background = ViewStyleUtility.createRoundedDrawable(btnColor, 8f)

                isEnabled = !isAttempted
                layoutParams = LayoutParams(180, 100)

                setOnClickListener {
                    soundManager.playClickSound()

                    prefs.edit {
                        putString(statusPrefKey, "FAILED")
                    }

                    MatchSettings.selectedTileCount = tileCount
                    MatchSettings.totalRoundsSelected = 0

                    val intent = Intent(this@DailyPuzzleHubActivity, GameActivity::class.java).apply {
                        putExtra("IS_DAILY_MODE", true)
                        putExtra("IS_FROM_DAILY_HUB", true)
                    }
                    startActivity(intent)
                }
                if (!isAttempted) {
                    ViewStyleUtility.attachButtonAnimateTouchListener(this)
                }
            }
            card.addView(btnPlayDaily)
            container.addView(card)
        }
    }

    private fun checkAndAwardDailyMasterAchievement() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val year = calendar.get(Calendar.YEAR)
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val dateKeyString = "${year}_${dayOfYear}"

        val achievementManager = AchievementManager(this).apply {
            targetPlayerOverride = MatchSettings.player1Nickname.ifBlank { "Guest" }
        }

        var allWon = true
        for (tileCount in 3..10) {
            val statusPrefKey = "daily_status_${dateKeyString}_${tileCount}"
            val status = prefs.getString(statusPrefKey, "UNPLAYED")

            if (status == "WON") {
                achievementManager.unlockAchievement("daily_challenge_$tileCount")
            } else {
                allWon = false
            }
        }

        if (allWon) {
            val masterBadgePrefKey = "daily_master_badge_${dateKeyString}"
            val hasBadgeToday = prefs.getBoolean(masterBadgePrefKey, false)

            if (!hasBadgeToday) {
                prefs.edit {
                    putBoolean(masterBadgePrefKey, true)
                }

                achievementManager.unlockAchievement("daily_master_badge")

                val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
                dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                val dateFormatted = dateFormat.format(calendar.time)

                saveMasterBadgeDate(dateFormatted)
                showMasterBadgeDialog(dateFormatted)
            }
        }
    }

    private fun saveMasterBadgeDate(dateStr: String) {
        val existingDates = prefs.getStringSet("master_badge_history", emptySet()) ?: emptySet()
        val updatedDates = existingDates.toMutableSet().apply { add(dateStr) }
        prefs.edit {
            putStringSet("master_badge_history", updatedDates)
        }
    }

    private fun showMasterBadgeDialog(dateStamp: String) {
        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            background = ViewStyleUtility.createRoundedDrawable(
                if (isDarkMode) "#1E293B" else "#FFFFFF",
                24f
            ).apply {
                setStroke(4, "#F1C40F".toColorInt())
            }
        }

        val tvBadgeHeader = TextView(this).apply {
            text = getString(R.string.achievement_daily_master_title)
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor("#F39C12".toColorInt())
            gravity = Gravity.CENTER
        }
        dialogView.addView(tvBadgeHeader)

        val tvDateStamp = TextView(this).apply {
            text = dateStamp
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor("#2ECC71".toColorInt())
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 16)
        }
        dialogView.addView(tvDateStamp)

        val tvMessage = TextView(this).apply {
            text = getString(R.string.achievement_daily_master_desc)
            textSize = 14f
            setTextColor(if (isDarkMode) Color.WHITE else Color.BLACK)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }
        dialogView.addView(tvMessage)

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val btnClaim = Button(this).apply {
            text = getString(android.R.string.ok)
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            background = ViewStyleUtility.createRoundedDrawable("#F1C40F", 12f)
            setOnClickListener {
                soundManager.playClickSound()
                alertDialog.dismiss()
            }
            ViewStyleUtility.attachButtonAnimateTouchListener(this)
        }
        dialogView.addView(btnClaim)

        alertDialog.show()
    }
}