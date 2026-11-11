package com.elothar.namagram

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class GameActivity : AppCompatActivity() {

    private lateinit var tvTimer: TextView
    private lateinit var tvLettersHeader: TextView
    private lateinit var lettersCard: LinearLayout
    private lateinit var lettersTileContainer: LinearLayout
    private lateinit var etSentence: EditText
    private lateinit var btnGenerate: Button
    private lateinit var btnSubmit: Button
    private lateinit var tvFeedbackBoard: TextView

    private var countDownTimer: CountDownTimer? = null
    private var generatedLetters = ""
    private var isGameActive = false
    private var isInOrder = true
    private var isTimerRunning = false
    private var hasDictionaryErrors = false

    private var dictionaryErrorCountThisTurn = 0
    private var firstKeystrokeTimestamp = 0L

    private lateinit var dictionaryManager: DictionaryManager
    private lateinit var scoreManager: ScoreManager
    private lateinit var soundManager: SoundManager
    private lateinit var achievementManager: AchievementManager

    private val aiEvaluator = UnifiedAIEvaluator(
        geminiKey = BuildConfig.GEMINI_API_KEY,
        groqKey = BuildConfig.GROQ_API_KEY,
        openRouterKey = BuildConfig.OPENROUTER_API_KEY,
        isDebug = true
    )
    private var localMirrorScoreP1 = 0
    private var localMirrorScoreP2 = 0
    private val roundScoresP1 = mutableListOf<Int>()
    private val roundScoresP2 = mutableListOf<Int>()

    private var isFinalScorecardReady = false

    // Dark mode state variables
    private var isDarkMode = false
    private var cardBackgroundColor = "#FFFFFF"
    private var textColorPrimary = Color.WHITE
    private var textMutedColor = "#718096"
    private var cardStrokeColor = "#E2E8F0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dictionaryManager = DictionaryManager(this)
        scoreManager = ScoreManager(this)
        soundManager = SoundManager(this)
        achievementManager = AchievementManager(this).apply {
            targetPlayerOverride = MatchSettings.player1Nickname.ifBlank { "Guest" }
        }

        // 1. Check Dark Mode Preference
        val prefs = getSharedPreferences("NamagramPrefs", MODE_PRIVATE)
        isDarkMode = prefs.getBoolean("dark_mode_enabled", false)

        // Define dynamic colors based on dark mode state
        val backgroundColor = if (isDarkMode) "#0F172A" else ViewStyleUtility.COLOR_BACKGROUND_LIGHT
        cardBackgroundColor = if (isDarkMode) "#1E293B" else "#FFFFFF"
        textColorPrimary = if (isDarkMode) Color.WHITE else ViewStyleUtility.COLOR_TEXT_DARK.toColorInt()
        textMutedColor = if (isDarkMode) "#94A3B8" else ViewStyleUtility.COLOR_TEXT_MUTED
        cardStrokeColor = if (isDarkMode) "#334155" else "#E2E8F0"

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(48, 48, 48, 48)
            setBackgroundColor(backgroundColor.toColorInt())
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }

        // 👉 Check if launched from Daily Hub and display distinct banner at index 0
        val isFromDailyHub = intent.getBooleanExtra("IS_FROM_DAILY_HUB", false)
        if (isFromDailyHub) {
            val dailyBanner = TextView(this).apply {
                text = getString(R.string.daily_challenge_mode_banner, MatchSettings.selectedTileCount)
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setPadding(24, 20, 24, 20)

                // Gold/Amber background to make daily challenge games stand out
                background = ViewStyleUtility.createRoundedDrawable("#F39C12", 12f)

                layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 24)
                }
            }
            rootLayout.addView(dailyBanner)
        }

        val timerCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 24, 32, 24)
            background = ViewStyleUtility.createRoundedDrawable(ViewStyleUtility.COLOR_PRIMARY_SLATE, 12f)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 16, 0, 48)
            }
        }

        tvTimer = TextView(this).apply {
            text = if (MatchSettings.roundTimeLimitSeconds > 0) {
                MatchSettings.roundTimeLimitSeconds.toString()
            } else {
                DifficultySettings.getTimeLimit(this@GameActivity).toString()
            }
            textSize = 54f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        timerCard.addView(tvTimer)
        rootLayout.addView(timerCard)

        lettersCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(cardBackgroundColor.toColorInt())
                cornerRadius = 24f
                setStroke(3, cardStrokeColor.toColorInt())
            }
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 48)
            }
        }

        tvLettersHeader = TextView(this).apply {
            text = getString(R.string.game_board_letters_header)
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(textMutedColor.toColorInt())
            gravity = Gravity.CENTER
            setPadding(0, 0, 24, 0)
        }
        lettersCard.addView(tvLettersHeader)

        lettersTileContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
        lettersCard.addView(lettersTileContainer)
        rootLayout.addView(lettersCard)

        renderLetterTiles(getString(R.string.default_letters_placeholder))

        val inputLabel = TextView(this).apply {
            text = getString(R.string.game_board_input_header)
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(textMutedColor.toColorInt())
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.START
                setMargins(12, 0, 0, 16)
            }
        }
        rootLayout.addView(inputLabel)

        etSentence = EditText(this).apply {
            ViewStyleUtility.applyEditTextStyle(this, "Type your sentence here....")
            isEnabled = false
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 48)
            }

            addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (firstKeystrokeTimestamp == 0L && !s.isNullOrEmpty()) {
                        firstKeystrokeTimestamp = System.currentTimeMillis()
                    }
                }
                override fun afterTextChanged(s: android.text.Editable?) {}
            })
        }
        rootLayout.addView(etSentence)

        btnGenerate = Button(this).apply {
            text = getString(R.string.btn_generate)
            textSize = 16f
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            background = ViewStyleUtility.createRoundedDrawable(ViewStyleUtility.COLOR_SUCCESS_EMERALD, 8f)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 130).apply {
                setMargins(0, 0, 0, 24)
            }
            setOnClickListener {
                soundManager.playClickSound()
                if (isFinalScorecardReady) {
                    renderDedicatedScorecardPage()
                } else {
                    startNewTurn()
                }
            }
            ViewStyleUtility.attachButtonAnimateTouchListener(this)
        }
        rootLayout.addView(btnGenerate)

        btnSubmit = Button(this).apply {
            text = getString(R.string.btn_submit)
            textSize = 16f
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isVisible = false
            background = ViewStyleUtility.createRoundedDrawable(ViewStyleUtility.COLOR_SUCCESS_EMERALD, 8f)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 130).apply {
                setMargins(0, 0, 0, 24)
            }
            setOnClickListener {
                soundManager.playClickSound()
                evaluateSubmittedSentence()
            }
            ViewStyleUtility.attachButtonAnimateTouchListener(this)
        }
        rootLayout.addView(btnSubmit)

        tvFeedbackBoard = TextView(this).apply {
            text = ""
            textSize = 16f
            setTextColor(textColorPrimary)
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
        rootLayout.addView(tvFeedbackBoard)

        dictionaryManager.loadDictionaryAsync {
            runOnUiThread {
                updateFeedbackHeaderDisplay()
            }
        }

        // Conditionally render the bottom menu button based on whether we are in Daily Mode
        if (isFromDailyHub) {
            val btnReturnToHub = Button(this).apply {
                text = getString(R.string.btn_return_to_daily_hub)
                textSize = 16f
                setTextColor(Color.WHITE)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                background = ViewStyleUtility.createRoundedDrawable("#3498DB", 12f)
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 130).apply {
                    setMargins(0, 24, 0, 0)
                }
                setOnClickListener {
                    soundManager.playClickSound()
                    finish() // Pops GameActivity and returns right back to DailyPuzzleHubActivity
                }
                ViewStyleUtility.attachButtonAnimateTouchListener(this)
            }
            rootLayout.addView(btnReturnToHub)
        } else {
            ViewStyleUtility.addPersistentMenuButton(this, rootLayout)
        }

        setContentView(rootLayout)
    }

    private fun isValidCustomName(name: String): Boolean {
        // 👉 Disables achievement tracking entirely if a multiplayer match is active (totalRoundsSelected > 0)
        if (MatchSettings.totalRoundsSelected > 0) {
            return false
        }

        val lower = name.trim().lowercase(Locale.ROOT)
        return lower.isNotEmpty() &&
                lower != "player 1" &&
                lower != "player 2" &&
                lower != "guest" &&
                lower != "player1" &&
                lower != "player2"
    }

    private fun updateFeedbackHeaderDisplay() {
        if (MatchSettings.totalRoundsSelected > 0) {
            val activeName = if (MatchSettings.isPlayer1Turn) MatchSettings.player1Nickname else MatchSettings.player2Nickname
            tvFeedbackBoard.text = getString(R.string.multiplayer_round_header_label, MatchSettings.currentRoundNumber, MatchSettings.totalRoundsSelected, activeName)
            tvFeedbackBoard.setTextColor("#3498DB".toColorInt())
        } else {
            tvFeedbackBoard.text = ""
        }
    }

    private fun renderLetterTiles(letters: String) {
        lettersTileContainer.removeAllViews()

        val count = letters.length
        // Destructure into distinct local variable names to avoid property collisions
        val (tileSize, tileTextSize, margin) = when {
            count >= 9 -> Triple(48, 16f, 3)     // Compact sizing for 9-10 tiles
            count >= 7 -> Triple(56, 18f, 4)     // Medium sizing for 7-8 tiles
            else -> Triple(75, 22f, 6)           // Standard sizing for 3-6 tiles
        }

        for (char in letters) {
            val tvTile = TextView(this).apply {
                text = char.toString()
                textSize = tileTextSize // Explicitly using the local variable
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                gravity = Gravity.CENTER

                if (char != '-') {
                    setTextColor(Color.WHITE)
                    background = ViewStyleUtility.createRoundedDrawable(ViewStyleUtility.COLOR_PRIMARY_SLATE, 6f)
                } else {
                    setTextColor(if (isDarkMode) "#64748B".toColorInt() else "#BDC3C7".toColorInt())
                    background = ViewStyleUtility.createRoundedDrawable(if (isDarkMode) "#0F172A" else "#F1F5F9", 6f)
                }

                layoutParams = LayoutParams(tileSize, (tileSize * 1.25).toInt()).apply {
                    setMargins(margin, 0, margin, 0)
                }

                scaleX = 0.8f
                scaleY = 0.8f
                animate().scaleX(1.0f).scaleY(1.0f).setDuration(60).start()
            }
            lettersTileContainer.addView(tvTile)
        }
    }

    private fun evaluateSubmittedSentence() {
        val rawSentence = etSentence.text.toString().trim()
        val totalTimeLimit = if (MatchSettings.roundTimeLimitSeconds > 0) {
            MatchSettings.roundTimeLimitSeconds
        } else {
            DifficultySettings.getTimeLimit(this)
        }
        val currentSecondsRemaining = tvTimer.text.toString().toIntOrNull() ?: totalTimeLimit

        if (rawSentence.isEmpty()) {
            Toast.makeText(this, "Please enter a sentence first!", Toast.LENGTH_SHORT).show()
            etSentence.isEnabled = true
            btnSubmit.isEnabled = true
            return
        }

        val individualWords = rawSentence.split("""\s+""".toRegex())
        for (word in individualWords) {
            val cleanWord = word.replace("""[^A-Za-z-]""".toRegex(), "").trim('-')
            if (cleanWord.isNotEmpty() && !dictionaryManager.isValidWord(cleanWord)) {
                hasDictionaryErrors = true
                dictionaryErrorCountThisTurn++

                val activeName = if (MatchSettings.totalRoundsSelected > 0) {
                    if (MatchSettings.isPlayer1Turn) MatchSettings.player1Nickname else MatchSettings.player2Nickname
                } else {
                    MatchSettings.player1Nickname
                }

                if (isValidCustomName(activeName)) {
                    achievementManager.targetPlayerOverride = activeName
                    achievementManager.unlockAchievement("spelling_disaster")

                    val firstKeystrokeElapsed = if (firstKeystrokeTimestamp > 0L) ((System.currentTimeMillis() - firstKeystrokeTimestamp) / 1000).toInt() else totalTimeLimit
                    achievementManager.checkFailedAttemptAchievements(rawSentence, dictionaryErrorCountThisTurn, currentSecondsRemaining, totalTimeLimit, firstKeystrokeElapsed)
                }

                Toast.makeText(this, getString(R.string.toast_invalid_word_msg, word), Toast.LENGTH_LONG).show()
                etSentence.isEnabled = true
                btnSubmit.isEnabled = true
                return
            }
        }

        etSentence.isEnabled = false
        btnSubmit.isEnabled = false
        tvFeedbackBoard.text = getString(R.string.checking_sentence_logic)
        tvFeedbackBoard.setTextColor("#3498DB".toColorInt())

        val evaluationStartTime = System.currentTimeMillis()

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val targetLettersPool =
                    if (MatchSettings.totalRoundsSelected > 0) MatchSettings.savedRoundLetters else generatedLetters
                val targetLettersList = targetLettersPool.replace(" ", "").chunked(1)

                val refereeReason = aiEvaluator.evaluateSentence(rawSentence, targetLettersList)
                val passesEvaluation = !refereeReason.startsWith("Rejected:", ignoreCase = true) &&
                        !refereeReason.contains("failed", ignoreCase = true)

                val evaluationDurationSeconds =
                    ((System.currentTimeMillis() - evaluationStartTime) / 1000).toInt()

                if (passesEvaluation) {
                    isTimerRunning = false
                    countDownTimer?.cancel()
                    countDownTimer = null
                    soundManager.stopCountdownSounds()
                    isGameActive = false

                    soundManager.playVictoryChime()

                    isInOrder =
                        dictionaryManager.verifySequenceMatchesRules(rawSentence, targetLettersPool)
                    val cleanGeneratedLetters =
                        targetLettersPool.replace(" ", "").uppercase(Locale.ROOT).toList()
                    val startLettersOfSubmission = individualWords.mapNotNull {
                        it.replace("[^A-Za-z]".toRegex(), "").firstOrNull()?.uppercaseChar()
                    }
                    var finalScore = 0
                    val scoringBreakdown = java.lang.StringBuilder()
                    var matchedPoolCount = 0
                    val mutablePool = cleanGeneratedLetters.toMutableList()
                    for (startChar in startLettersOfSubmission) {
                        if (mutablePool.contains(startChar)) {
                            mutablePool.remove(startChar)
                            matchedPoolCount++
                        }
                    }
                    val basePointsPerLetter = 10
                    val factor1Score = matchedPoolCount * basePointsPerLetter
                    finalScore += factor1Score
                    scoringBreakdown.append(
                        getString(
                            R.string.score_factor_letters_used,
                            matchedPoolCount,
                            cleanGeneratedLetters.size,
                            factor1Score
                        )
                    )
                    val usedAllLetters = matchedPoolCount == cleanGeneratedLetters.size
                    if (usedAllLetters) {
                        val allLettersBonus = 50
                        finalScore += allLettersBonus
                        scoringBreakdown.append(getString(R.string.score_factor_all_used))
                    }
                    var isStrictInOrder = false
                    if (usedAllLetters) {
                        isStrictInOrder = true
                        if (startLettersOfSubmission.size >= cleanGeneratedLetters.size) {
                            for (i in cleanGeneratedLetters.indices) {
                                if (startLettersOfSubmission[i] != cleanGeneratedLetters[i]) {
                                    isStrictInOrder = false
                                    break
                                }
                            }
                        } else {
                            isStrictInOrder = false
                        }
                        if (isStrictInOrder) {
                            val perfectSequenceBonus = 100
                            finalScore += perfectSequenceBonus
                            scoringBreakdown.append(getString(R.string.score_factor_strict_order))
                        }
                    }
                    scoringBreakdown.append("\nReferee: ").append(refereeReason)

                    val activeName = if (MatchSettings.totalRoundsSelected > 0) {
                        if (MatchSettings.isPlayer1Turn) MatchSettings.player1Nickname else MatchSettings.player2Nickname
                    } else {
                        MatchSettings.player1Nickname
                    }

                    if (isValidCustomName(activeName)) {
                        achievementManager.targetPlayerOverride = activeName

                        if (MatchSettings.selectedTileCount == 9) {
                            achievementManager.registerNineTileSuccess()
                        }

                        achievementManager.unlockAchievement("first_win")
                        achievementManager.checkTurnAchievements(
                            tileCount = MatchSettings.selectedTileCount,
                            usedAllLetters = usedAllLetters,
                            isStrictInOrder = isStrictInOrder
                        )

                        achievementManager.checkSentenceCompositionAchievements(individualWords)
                        achievementManager.checkTimingAchievements(
                            currentSecondsRemaining,
                            totalTimeLimit
                        )
                        achievementManager.checkAdvancedSentenceStructure(
                            individualWords,
                            rawSentence
                        )
                        achievementManager.checkAdvancedTimingAchievements(
                            currentSecondsRemaining,
                            totalTimeLimit,
                            0,
                            evaluationDurationSeconds
                        )
                        achievementManager.checkTileAndBonusMastery(
                            cleanGeneratedLetters.size,
                            matchedPoolCount,
                            usedAllLetters,
                            isStrictInOrder,
                            hasDictionaryErrors
                        )
                        achievementManager.checkAlliterationAndStyle(
                            individualWords,
                            matchedPoolCount
                        )

                        val cumulativeTotal = if (MatchSettings.totalRoundsSelected > 0) {
                            if (MatchSettings.isPlayer1Turn) localMirrorScoreP1 + finalScore else localMirrorScoreP2 + finalScore
                        } else {
                            finalScore
                        }
                        val isPerfectAI = refereeReason.contains(
                            "perfect",
                            ignoreCase = true
                        ) || finalScore >= 150

                        achievementManager.checkScoreAndAIAchievements(
                            roundScore = finalScore,
                            cumulativeScore = cumulativeTotal,
                            isPerfectAI = isPerfectAI,
                            isNewRecord = false
                        )

                        achievementManager.registerCumulativeMilestones(finalScore)
                    }

                    processTurnConclusionState(finalScore, scoringBreakdown.toString(), true)
                    achievementManager.processPendingAchievements()
                } else {
                    val activeName = if (MatchSettings.totalRoundsSelected > 0) {
                        if (MatchSettings.isPlayer1Turn) MatchSettings.player1Nickname else MatchSettings.player2Nickname
                    } else {
                        MatchSettings.player1Nickname
                    }

                    if (isValidCustomName(activeName)) {
                        achievementManager.targetPlayerOverride = activeName
                        achievementManager.unlockAchievement("ai_rejected")
                    }

                    if (!isTimerRunning) {
                        processTurnConclusionState(
                            0,
                            "Time expired and sentence failed evaluation: $refereeReason",
                            false
                        )
                        achievementManager.processPendingAchievements()
                    } else {
                        btnSubmit.isEnabled = true
                        etSentence.isEnabled = true
                        tvFeedbackBoard.text =
                            getString(R.string.score_rejection_format, refereeReason)
                        tvFeedbackBoard.setTextColor("#E74C3C".toColorInt())
                        soundManager.playClickSound()
                    }
                }
            }catch (e: Exception) {
                    etSentence.isEnabled = true
                    btnSubmit.isEnabled = true
                    tvFeedbackBoard.text = getString(R.string.evaluation_error_format, e.localizedMessage ?: "Unknown error")
                    tvFeedbackBoard.setTextColor("#E74C3C".toColorInt())
                }
        }
    }

    private fun processTurnConclusionState(calculatedScore: Int, explanation: String, isSuccess: Boolean = false) {
        isTimerRunning = false
        countDownTimer?.cancel()
        countDownTimer = null
        soundManager.stopCountdownSounds()
        isGameActive = false

        btnSubmit.isVisible = false
        etSentence.isEnabled = false

        val activeTimeLimit = if (MatchSettings.totalRoundsSelected > 0) {
            MatchSettings.roundTimeLimitSeconds
        } else {
            DifficultySettings.getTimeLimit(this)
        }
        val isDailyMode = intent.getBooleanExtra("IS_DAILY_MODE", false)
        if (isDailyMode && isSuccess) {
            val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
            val dateKeyString = "${calendar.get(java.util.Calendar.YEAR)}_${calendar.get(java.util.Calendar.DAY_OF_YEAR)}"
            val statusPrefKey = "daily_status_${dateKeyString}_${MatchSettings.selectedTileCount}"

            getSharedPreferences("NamagramPrefs", MODE_PRIVATE).edit {
                putString(statusPrefKey, "WON")
            }
        }
        val activeName = if (MatchSettings.totalRoundsSelected > 0) {
            if (!MatchSettings.isPlayer1Turn) MatchSettings.player1Nickname else MatchSettings.player2Nickname
        } else {
            MatchSettings.player1Nickname
        }

        if (isValidCustomName(activeName)) {
            achievementManager.targetPlayerOverride = activeName
            achievementManager.registerGameCompletion(activeTimeLimit, isSuccess)
        }

        if (isSuccess || calculatedScore == 0) {
            tvFeedbackBoard.text = getString(R.string.score_display_format, calculatedScore, explanation)
            tvFeedbackBoard.setTextColor(if (isSuccess) ViewStyleUtility.COLOR_SUCCESS_EMERALD.toColorInt() else "#E74C3C".toColorInt())
        } else {
            tvFeedbackBoard.text = getString(R.string.score_rejection_format, explanation)
            tvFeedbackBoard.setTextColor("#E74C3C".toColorInt())
        }

        if (MatchSettings.totalRoundsSelected > 0) {
            if (MatchSettings.isPlayer1Turn) {
                localMirrorScoreP1 += calculatedScore
                roundScoresP1.add(calculatedScore)
                MatchSettings.isPlayer1Turn = false
                btnGenerate.text = getString(R.string.btn_next_players_turn)
                btnGenerate.isVisible = true
            } else {
                localMirrorScoreP2 += calculatedScore
                roundScoresP2.add(calculatedScore)

                if (MatchSettings.currentRoundNumber >= MatchSettings.totalRoundsSelected) {
                    isFinalScorecardReady = true
                    btnGenerate.text = getString(R.string.btn_view_final_match_scores)
                    btnGenerate.isVisible = true
                } else {
                    MatchSettings.currentRoundNumber++
                    MatchSettings.isPlayer1Turn = true
                    btnGenerate.text = getString(R.string.btn_next_players_turn)
                    btnGenerate.isVisible = true
                }
            }
        } else {
            val currentNickname = MatchSettings.player1Nickname
            val isNewHighScore = scoreManager.updateSoloHighScoreIfNeeded(currentNickname, calculatedScore)
            if (isNewHighScore) {
                soundManager.playVictoryChime()
                if (isValidCustomName(currentNickname)) {
                    achievementManager.targetPlayerOverride = currentNickname
                    achievementManager.unlockAchievement("personal_best")
                }
            }

            val isFromDailyHub = intent.getBooleanExtra("IS_FROM_DAILY_HUB", false)
            btnGenerate.text = if (isFromDailyHub) {
                "Return to Daily Hub"
            } else {
                getString(R.string.btn_generate)
            }

            btnGenerate.setOnClickListener {
                soundManager.playClickSound()
                if (isFromDailyHub) {
                    finish()
                } else {
                    startNewTurn()
                }
            }

            btnGenerate.isVisible = true
        }
    }

    private fun renderDedicatedScorecardPage() {
        val rootGroup = tvFeedbackBoard.parent as LinearLayout
        rootGroup.removeAllViews()

        val scorecardContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(32, 48, 32, 48)
            background = ViewStyleUtility.createRoundedDrawable(if (isDarkMode) "#1E293B" else ViewStyleUtility.COLOR_PRIMARY_SLATE, 20f)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
                setMargins(24, 24, 24, 24)
            }
        }

        val tvTitle = TextView(this).apply {
            text = getString(R.string.scorecard_title)
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor("#F1C40F".toColorInt())
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 32)
        }
        scorecardContainer.addView(tvTitle)

        for (i in 0 until MatchSettings.totalRoundsSelected) {
            val p1Score = if (i < roundScoresP1.size) roundScoresP1[i] else 0
            val p2Score = if (i < roundScoresP2.size) roundScoresP2[i] else 0
            val tvRoundRow = TextView(this).apply {
                text = getString(
                    R.string.scorecard_round_row,
                    i + 1,
                    MatchSettings.player1Nickname,
                    p1Score,
                    MatchSettings.player2Nickname,
                    p2Score
                )
                textSize = 16f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setPadding(0, 8, 0, 8)
            }
            scorecardContainer.addView(tvRoundRow)
        }

        val winnerName = if (localMirrorScoreP1 > localMirrorScoreP2) {
            MatchSettings.player1Nickname
        } else if (localMirrorScoreP2 > localMirrorScoreP1) {
            MatchSettings.player2Nickname
        } else {
            "It's a tie!"
        }

        val winningTotal = maxOf(localMirrorScoreP1, localMirrorScoreP2)
        val isNewMultiHighScore = scoreManager.updateMultiplayerHighScoreIfNeeded(winnerName, winningTotal)

        if (winnerName != "It's a tie!" && (winningTotal > 0 || isNewMultiHighScore)) {
            soundManager.playVictoryChime()
            if (isValidCustomName(winnerName)) {
                achievementManager.targetPlayerOverride = winnerName
                achievementManager.unlockAchievement("win_multiplayer")
            }
        }

        if (MatchSettings.totalRoundsSelected == 10) {
            val achievementNames = listOf(MatchSettings.player1Nickname, MatchSettings.player2Nickname)
            for (name in achievementNames) {
                if (isValidCustomName(name)) {
                    achievementManager.targetPlayerOverride = name
                    achievementManager.unlockAchievement("rocky")
                }
            }
        }

        val p1WonFirstRound = roundScoresP1.isNotEmpty() && roundScoresP2.isNotEmpty() && roundScoresP1[0] > roundScoresP2[0]
        val didComeback = !p1WonFirstRound && localMirrorScoreP1 > localMirrorScoreP2
        val didSweep = roundScoresP1.isNotEmpty() && roundScoresP1.all { it > 0 } && localMirrorScoreP1 > localMirrorScoreP2
        val isDraw = localMirrorScoreP1 == localMirrorScoreP2

        if (isValidCustomName(MatchSettings.player1Nickname)) {
            achievementManager.targetPlayerOverride = MatchSettings.player1Nickname
            achievementManager.checkMultiplayerOutcomeAchievements(
                wonAfterLosingFirst = didComeback,
                wonAllRounds = didSweep,
                isDraw = isDraw
            )
        }

        val totalsHeader = getString(R.string.scorecard_totals_header)
        val p1TotalText = getString(R.string.scorecard_player_total, MatchSettings.player1Nickname, localMirrorScoreP1)
        val p2TotalText = getString(R.string.scorecard_player_total, MatchSettings.player2Nickname, localMirrorScoreP2)
        val winnerText = getString(R.string.scorecard_winner_announcement, winnerName)

        val tvTotals = TextView(this).apply {
            text = getString(R.string.scorecard_full_totals_block, totalsHeader, p1TotalText, p2TotalText, winnerText)
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor("#2ECC71".toColorInt())
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 32)
        }
        scorecardContainer.addView(tvTotals)

        val btnBackToMenu = Button(this).apply {
            val isFromDailyHub = intent.getBooleanExtra("IS_FROM_DAILY_HUB", false)
            text = if (isFromDailyHub) "Return to Daily Hub" else getString(R.string.return_to_main_menu)
            textSize = 16f
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            background = ViewStyleUtility.createRoundedDrawable(ViewStyleUtility.COLOR_SUCCESS_EMERALD, 12f)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 130).apply {
                setMargins(0, 32, 0, 0)
            }
            setOnClickListener {
                finish()
            }
            ViewStyleUtility.attachButtonAnimateTouchListener(this)
        }
        scorecardContainer.addView(btnBackToMenu)

        rootGroup.addView(scorecardContainer)
    }

    private fun startNewTurn() {
        if (isTimerRunning) {
            countDownTimer?.cancel()
            countDownTimer = null
            isTimerRunning = false
        }

        hasDictionaryErrors = false
        dictionaryErrorCountThisTurn = 0
        firstKeystrokeTimestamp = 0L

        val rootGroup = tvFeedbackBoard.parent as LinearLayout
        val oldPanel = rootGroup.findViewWithTag("score_panel_tag") as? LinearLayout
        if (oldPanel != null) {
            rootGroup.removeView(oldPanel)
        }
        etSentence.text.clear()
        etSentence.isEnabled = false
        tvFeedbackBoard.text = ""
        this@GameActivity.btnGenerate.isVisible = false
        btnSubmit.isVisible = false

        if (MatchSettings.totalRoundsSelected > 0) {
            val placeholderCount = if (MatchSettings.selectedTileCount > 0) MatchSettings.selectedTileCount else 5
            renderLetterTiles("-".repeat(placeholderCount))

            val activeName = if (MatchSettings.isPlayer1Turn) MatchSettings.player1Nickname else MatchSettings.player2Nickname
            val handoffCard = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(32, 48, 32, 48)
                tag = "handoff_panel_tag"
                background = ViewStyleUtility.createRoundedDrawable(if (isDarkMode) "#1E293B" else ViewStyleUtility.COLOR_PRIMARY_SLATE, 16f)
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 16, 0, 32)
                }
            }
            val tvHandoffLabel = TextView(this).apply {
                text = getString(R.string.multiplayer_turn_announcement_header)
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                setTextColor(if (isDarkMode) "#94A3B8".toColorInt() else "#BDC3C7".toColorInt())
                gravity = Gravity.CENTER
                setPadding(0, 0, 12, 0)
            }
            handoffCard.addView(tvHandoffLabel)
            val tvPlayerNameCallout = TextView(this).apply {
                text = activeName.uppercase(Locale.ROOT)
                textSize = 32f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setPadding(0, 0, 32, 0)
            }
            handoffCard.addView(tvPlayerNameCallout)
            val btnConfirmTurnStart = Button(this).apply {
                text = getString(R.string.btn_start_my_turn_label)
                textSize = 15f
                setTextColor(Color.WHITE)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                background = ViewStyleUtility.createRoundedDrawable(ViewStyleUtility.COLOR_SUCCESS_EMERALD, 8f)
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 115)
                setOnClickListener {
                    soundManager.playClickSound()
                    rootGroup.removeView(handoffCard)
                    if (MatchSettings.isPlayer1Turn) {
                        val isDailyMode = false
                        generatedLetters = if (isDailyMode) {
                            dictionaryManager.generateDailySequenceForSize(MatchSettings.selectedTileCount)
                        } else {
                            dictionaryManager.generateRandomLettersList(MatchSettings.selectedTileCount)
                        }
                        MatchSettings.savedRoundLetters = generatedLetters
                    } else {
                        generatedLetters = MatchSettings.savedRoundLetters
                    }
                    executeScrambleAndStartTimerSequence()
                }
                ViewStyleUtility.attachButtonAnimateTouchListener(this)
            }
            handoffCard.addView(btnConfirmTurnStart)
            val insertionIndex = rootGroup.indexOfChild(lettersCard)
            rootGroup.addView(handoffCard, insertionIndex)
        } else {
            executeScrambleAndStartTimerSequence()
            val isDailyMode = intent.getBooleanExtra("IS_DAILY_MODE", false)
            generatedLetters = if (isDailyMode) {
                dictionaryManager.generateDailySequenceForSize(MatchSettings.selectedTileCount)
            } else {
                dictionaryManager.generateRandomLettersList(MatchSettings.selectedTileCount)
            }
        }
    }

    private fun executeScrambleAndStartTimerSequence() {
        btnSubmit.isVisible = true
        btnSubmit.isEnabled = true
        updateFeedbackHeaderDisplay()
        soundManager.playTilesDrawSound()
        val animationDurationMs = 800L
        val framesPerSecond = 12
        val frameInterval = 1000L / framesPerSecond
        val totalFrames = (animationDurationMs / frameInterval).toInt()
        var currentFrame = 0
        val handler = Handler(Looper.getMainLooper())
        val scrambleRunnable = object : Runnable {
            override fun run() {
                if (currentFrame < totalFrames) {
                    val placeholderScramble = dictionaryManager.generateRandomLettersList(MatchSettings.selectedTileCount)
                    renderLetterTiles(placeholderScramble)
                    currentFrame++
                    handler.postDelayed(this, frameInterval)
                } else {
                    renderLetterTiles(generatedLetters)
                    etSentence.isEnabled = true
                    isGameActive = true
                    isTimerRunning = true
                    tvTimer.text = if (MatchSettings.roundTimeLimitSeconds > 0) {
                        MatchSettings.roundTimeLimitSeconds.toString()
                    } else {
                        DifficultySettings.getTimeLimit(this@GameActivity).toString()
                    }
                    startCountdownTimerPipeline()
                }
            }
        }
        handler.post(scrambleRunnable)
    }

    private fun startCountdownTimerPipeline() {
        countDownTimer?.cancel()
        countDownTimer = null
        isTimerRunning = true
        tvTimer.setTextColor(Color.WHITE)
        tvTimer.scaleX = 1.0f
        tvTimer.scaleY = 1.0f

        val totalTimerMillis = if (MatchSettings.roundTimeLimitSeconds > 0) {
            MatchSettings.roundTimeLimitSeconds * 1000L
        } else {
            DifficultySettings.getTimeLimit(this) * 1000L
        }

        countDownTimer = object : CountDownTimer(totalTimerMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (!this@GameActivity.isGameActive || !this@GameActivity.isTimerRunning) {
                    cancel()
                    return
                }
                val secondsRemaining = (millisUntilFinished + 999) / 1000
                if (secondsRemaining <= 0) return
                tvTimer.text = secondsRemaining.toString()
                if (secondsRemaining <= 10) {
                    tvTimer.setTextColor("#FF4757".toColorInt())
                    if (this@GameActivity.isGameActive) {
                        soundManager.playTickSound()
                    }
                    tvTimer.scaleX = 1.2f
                    tvTimer.scaleY = 1.2f
                    tvTimer.animate().scaleX(1.0f).scaleY(1.0f).setDuration(400).start()
                } else {
                    tvTimer.setTextColor(Color.WHITE)
                    tvTimer.scaleX = 1.0f
                    tvTimer.scaleY = 1.0f
                }
            }

            override fun onFinish() {
                this@GameActivity.isGameActive = false
                this@GameActivity.isTimerRunning = false
                soundManager.stopCountdownSounds()
                tvTimer.text = "0"
                tvTimer.setTextColor("#FF4757".toColorInt())
                soundManager.playTimeoutSound()
                Toast.makeText(this@GameActivity, getString(R.string.times_up), Toast.LENGTH_SHORT).show()

                val activeName = if (MatchSettings.totalRoundsSelected > 0) {
                    if (MatchSettings.isPlayer1Turn) MatchSettings.player1Nickname else MatchSettings.player2Nickname
                } else {
                    MatchSettings.player1Nickname
                }

                if (isValidCustomName(activeName)) {
                    achievementManager.targetPlayerOverride = activeName
                    achievementManager.unlockAchievement("ran_out_of_time")
                }

                etSentence.isEnabled = false
                btnSubmit.isVisible = false

                val rawSentence = etSentence.text.toString().trim()
                if (rawSentence.isEmpty()) {
                    processTurnConclusionState(0, "Time expired! No sentence was submitted.", false)
                    achievementManager.processPendingAchievements()
                } else {
                    tvFeedbackBoard.text = getString(R.string.checking_sentence_logic)
                    tvFeedbackBoard.setTextColor("#3498DB".toColorInt())

                    val individualWords = rawSentence.split("""\s+""".toRegex())

                    lifecycleScope.launch(Dispatchers.Main) {
                        try {
                            val targetLettersPool = if (MatchSettings.totalRoundsSelected > 0) MatchSettings.savedRoundLetters else generatedLetters
                            val targetLettersList = targetLettersPool.replace(" ", "").chunked(1)

                            val refereeReason = aiEvaluator.evaluateSentence(rawSentence, targetLettersList)
                            val passesEvaluation = !refereeReason.contains("reject", ignoreCase = true) &&
                                    !refereeReason.contains("invalid", ignoreCase = true)

                            if (passesEvaluation) {
                                val cleanGeneratedLetters = targetLettersPool.replace(" ", "").uppercase(Locale.ROOT).toList()
                                val startLettersOfSubmission = individualWords.mapNotNull {
                                    it.replace("[^A-Za-z]".toRegex(), "").firstOrNull()?.uppercaseChar()
                                }
                                var finalScore = 0
                                val scoringBreakdown = StringBuilder()
                                var matchedPoolCount = 0
                                val mutablePool = cleanGeneratedLetters.toMutableList()
                                for (startChar in startLettersOfSubmission) {
                                    if (mutablePool.contains(startChar)) {
                                        mutablePool.remove(startChar)
                                        matchedPoolCount++
                                    }
                                }
                                val basePointsPerLetter = 10
                                val calculatedScore = matchedPoolCount * basePointsPerLetter
                                finalScore += calculatedScore
                                scoringBreakdown.append(getString(R.string.score_factor_letters_used, matchedPoolCount, cleanGeneratedLetters.size, calculatedScore))

                                val usedAllLetters = matchedPoolCount == cleanGeneratedLetters.size
                                var isStrictInOrder = false
                                if (usedAllLetters) {
                                    finalScore += 50
                                    scoringBreakdown.append(getString(R.string.score_factor_all_used))

                                    isStrictInOrder = true
                                    if (startLettersOfSubmission.size >= cleanGeneratedLetters.size) {
                                        for (i in cleanGeneratedLetters.indices) {
                                            if (startLettersOfSubmission[i] != cleanGeneratedLetters[i]) {
                                                isStrictInOrder = false
                                                break
                                            }
                                        }
                                    } else {
                                        isStrictInOrder = false
                                    }
                                    if (isStrictInOrder) {
                                        finalScore += 100
                                        scoringBreakdown.append(getString(R.string.score_factor_strict_order))
                                    }
                                }
                                scoringBreakdown.append("\nReferee: ").append(refereeReason)

                                if (isValidCustomName(activeName)) {
                                    achievementManager.targetPlayerOverride = activeName
                                    achievementManager.unlockAchievement("first_win")
                                    achievementManager.checkTurnAchievements(
                                        tileCount = MatchSettings.selectedTileCount,
                                        usedAllLetters = usedAllLetters,
                                        isStrictInOrder = isStrictInOrder
                                    )
                                }

                                processTurnConclusionState(finalScore, scoringBreakdown.toString(), true)
                                achievementManager.processPendingAchievements()
                            } else {
                                if (isValidCustomName(activeName)) {
                                    achievementManager.targetPlayerOverride = activeName
                                    achievementManager.unlockAchievement("ai_rejected")
                                }
                                processTurnConclusionState(0, "Time expired and sentence failed evaluation: $refereeReason", false)
                                achievementManager.processPendingAchievements()
                            }
                        } catch (e: Exception) {
                            processTurnConclusionState(0, getString(R.string.evaluation_error_format, e.localizedMessage ?: "Unknown error"), false)
                            achievementManager.processPendingAchievements()
                        }
                    }
                }
            }
        }.start()
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        soundManager.release()
        super.onDestroy()
    }
}