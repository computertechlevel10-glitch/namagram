package com.elothar.namagram

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt

class SummaryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Check Dark Mode Preference
        val prefs = getSharedPreferences("NamagramPrefs", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode_enabled", false)

        // Define dynamic colors based on dark mode state
        val backgroundColor = if (isDarkMode) "#0F172A" else ViewStyleUtility.COLOR_BACKGROUND_LIGHT
        val cardBackgroundColor = if (isDarkMode) "#1E293B" else "#FFFFFF"
        val totalsCardBgColor = if (isDarkMode) "#1E293B" else "#F1F5F9"
        val textColorPrimary = if (isDarkMode) Color.WHITE else ViewStyleUtility.COLOR_TEXT_DARK.toColorInt()
        val textMutedColor = if (isDarkMode) "#94A3B8" else ViewStyleUtility.COLOR_TEXT_MUTED
        val strokeColor = if (isDarkMode) "#334155" else "#E2E8F0"

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(44, 48, 44, 32)
            setBackgroundColor(backgroundColor.toColorInt())
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }

        val tvTitle = TextView(this).apply {
            ViewStyleUtility.applyHeaderStyle(this, getString(R.string.stat_panel_header), isMainTitle = true)
            setTextColor(textColorPrimary)
            setPadding(0, 0, 0, 32)
        }
        rootLayout.addView(tvTitle)

        val winnerCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 24, 32, 24)
            background = ViewStyleUtility.createRoundedDrawable(ViewStyleUtility.COLOR_PRIMARY_SLATE, 14f)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 40)
            }
        }

        val tvWinnerDeclaration = TextView(this).apply {
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER

            val p1Score = MatchSettings.player1TotalScore
            val p2Score = MatchSettings.player2TotalScore

            text = when {
                p1Score > p2Score -> getString(R.string.multiplayer_winner_declaration_p1, MatchSettings.player1Nickname)
                p2Score > p1Score -> getString(R.string.multiplayer_winner_declaration_p2, MatchSettings.player2Nickname)
                else -> getString(R.string.multiplayer_winner_declaration_draw)
            }
        }
        winnerCard.addView(tvWinnerDeclaration)
        rootLayout.addView(winnerCard)

        val scrollView = ScrollView(this).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f)
            isFillViewport = true
        }

        val listContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        val totalRounds = MatchSettings.totalRoundsSelected
        for (i in 0 until totalRounds) {
            val r1Score = MatchSettings.player1RoundScoresList.getOrNull(i) ?: 0
            val r2Score = MatchSettings.player2RoundScoresList.getOrNull(i) ?: 0

            val roundCard = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(28, 24, 28, 24)
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(cardBackgroundColor.toColorInt())
                    cornerRadius = 16f
                    setStroke(2, strokeColor.toColorInt())
                }
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 0, 0, 24)
                }
            }

            val tvRoundNum = TextView(this).apply {
                text = getString(R.string.multiplayer_round_breakdown_title, i + 1)
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                setTextColor(textMutedColor.toColorInt())
                setPadding(0, 0, 0, 12)
            }
            roundCard.addView(tvRoundNum)

            val tvP1Performance = TextView(this).apply {
                text = getString(R.string.multiplayer_score_display_format, MatchSettings.player1Nickname, r1Score)
                textSize = 15f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                setTextColor(textColorPrimary)
            }
            roundCard.addView(tvP1Performance)

            val tvP2Performance = TextView(this).apply {
                text = getString(R.string.multiplayer_score_display_format, MatchSettings.player2Nickname, r2Score)
                textSize = 15f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                setTextColor(textColorPrimary)
                setPadding(0, 4, 0, 0)
            }
            roundCard.addView(tvP2Performance)

            listContainer.addView(roundCard)
        }

        val totalsCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 24, 28, 24)
            background = ViewStyleUtility.createRoundedDrawable(totalsCardBgColor, 16f)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 16, 0, 32)
            }
        }

        val tvTotalLabel = TextView(this).apply {
            text = getString(R.string.multiplayer_aggregate_totals_header)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(textMutedColor.toColorInt())
            setPadding(0, 0, 0, 8)
        }
        totalsCard.addView(tvTotalLabel)

        val tvFinalScores = TextView(this).apply {
            val p1Text = getString(R.string.multiplayer_score_display_format, MatchSettings.player1Nickname, MatchSettings.player1TotalScore)
            val p2Text = getString(R.string.multiplayer_score_display_format, MatchSettings.player2Nickname, MatchSettings.player2TotalScore)

            text = getString(R.string.multiplayer_aggregate_totals_display, p1Text, p2Text)

            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(textColorPrimary)
            setLineSpacing(0f, 1.2f)
        }
        totalsCard.addView(tvFinalScores)
        listContainer.addView(totalsCard)

        scrollView.addView(listContainer)
        rootLayout.addView(scrollView)

        val btnHome = Button(this).apply {
            text = getString(R.string.btn_return_menu)
            textSize = 16f
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            background = ViewStyleUtility.createRoundedDrawable(ViewStyleUtility.COLOR_SUCCESS_EMERALD, 8f)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 130)
            setOnClickListener { finish() }
            ViewStyleUtility.attachButtonAnimateTouchListener(this)
        }
        rootLayout.addView(btnHome)

        setContentView(rootLayout)
    }
}