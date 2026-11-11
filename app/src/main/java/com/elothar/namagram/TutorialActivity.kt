package com.elothar.namagram

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Html
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt

class TutorialActivity : AppCompatActivity() {

    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        soundManager = SoundManager(this)

        // 1. Check Dark Mode Preference
        val prefs = getSharedPreferences("NamagramPrefs", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode_enabled", false)

        // Define dynamic colors based on dark mode state
        val backgroundColor = if (isDarkMode) "#0F172A" else ViewStyleUtility.COLOR_BACKGROUND_LIGHT
        val cardBackgroundColor = if (isDarkMode) "#1E293B" else "#FFFFFF"
        val textColorPrimary = if (isDarkMode) Color.WHITE else ViewStyleUtility.COLOR_TEXT_DARK.toColorInt()
        val tileBgColor = if (isDarkMode) "#1E293B" else "#EBF8FF"
        val tileTextColor = if (isDarkMode) "#93C5FD" else "#1E3A8A"
        val tileStrokeColor = if (isDarkMode) "#3B82F6" else "#93C5FD"

        val scrollView = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(backgroundColor.toColorInt())
        }

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 24)
            gravity = Gravity.TOP
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        // 2. PAGE TITLE
        val tvTitle = TextView(this).apply {
            ViewStyleUtility.applyHeaderStyle(this, "INTERACTIVE TUTORIAL", isMainTitle = true)
            setTextColor(textColorPrimary)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 16)
        }
        rootLayout.addView(tvTitle)

        // 3. INSTRUCTION TEXT CARD (Includes new A.I. note)
        val instructionCard = TextView(this).apply {
            text = getString(R.string.tutorial_instruction_text)
            textSize = 13.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(textColorPrimary)
            setPadding(24, 16, 24, 16)
            background = ViewStyleUtility.createRoundedDrawable(cardBackgroundColor, 12f)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 16)
            }
            setLineSpacing(0f, 1.2f)
        }
        rootLayout.addView(instructionCard)

        // 4. TARGET LETTER SEQUENCE DISPLAY AS GAME TILES
        val targetLetters = listOf("N", "A", "M", "A", "G", "R", "A", "M", "I", "C")

        val tilesContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 4, 0, 20)
            }
        }

        for (letter in targetLetters) {
            val tileView = TextView(this).apply {
                text = letter
                textSize = 15f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                setTextColor(tileTextColor.toColorInt())
                gravity = Gravity.CENTER
                elevation = 6f
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(tileBgColor.toColorInt())
                    cornerRadius = 10f
                    setStroke(3, tileStrokeColor.toColorInt())
                }
                layoutParams = LayoutParams(58, 68).apply {
                    setMargins(3, 0, 3, 0)
                }
            }
            tilesContainer.addView(tileView)
        }
        rootLayout.addView(tilesContainer)

        // 5. THREE EXAMPLE SENTENCES CARD
        val examplesCard = TextView(this).apply {
            text = Html.fromHtml(
                "<b>Example Sentences:</b><br>" +
                        "1. <font color=\"#2563EB\"><b>G</b></font>iraffes <font color=\"#2563EB\"><b>M</b></font>ake <font color=\"#2563EB\"><b>A</b></font>nimals <font color=\"#2563EB\"><b>R</b></font>un <font color=\"#2563EB\"><b>A</b></font>way<br>" +
                        "2. <font color=\"#2563EB\"><b>N</b></font>ever <font color=\"#2563EB\"><b>A</b></font>sk <font color=\"#2563EB\"><b>M</b></font>onkeys <font color=\"#2563EB\"><b>A</b></font>bout <font color=\"#2563EB\"><b>G</b></font>orillas<br>" +
                        "3. <font color=\"#2563EB\"><b>M</b></font>y <font color=\"#2563EB\"><b>C</b></font>ousin <font color=\"#2563EB\"><b>I</b></font>s <font color=\"#2563EB\"><b>A</b></font> <font color=\"#2563EB\"><b>G</b></font>reat <font color=\"#2563EB\"><b>M</b></font>an",
                Html.FROM_HTML_MODE_LEGACY
            )
            textSize = 13f
            setTextColor(textColorPrimary)
            setPadding(24, 16, 24, 16)
            background = ViewStyleUtility.createRoundedDrawable(cardBackgroundColor, 12f)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 16)
            }
            setLineSpacing(0f, 1.25f)
        }
        rootLayout.addView(examplesCard)

        // 6. USER INPUT EDIT TEXT
        val etTutorialInput = EditText(this).apply {
            ViewStyleUtility.applyEditTextStyle(this, "Type your sentence here...")
            textSize = 14f
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 16)
            }
        }
        rootLayout.addView(etTutorialInput)

        // 7. TEST RESULT FEEDBACK BOX
        val tvFeedbackBox = TextView(this).apply {
            visibility = android.view.View.GONE
            textSize = 13.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setPadding(24, 16, 24, 16)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 16)
            }
            setLineSpacing(0f, 1.15f)
        }

        // 8. TEST MY NAMAGRAM BUTTON
        val btnTestSentence = Button(this).apply {
            text = getString(R.string.btn_test_namagram)
            textSize = 14f
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            background = ViewStyleUtility.createRoundedDrawable(ViewStyleUtility.COLOR_SUCCESS_EMERALD, 8f)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 120).apply {
                setMargins(0, 0, 0, 16)
            }
            setOnClickListener {
                val userInput = etTutorialInput.text.toString().trim()
                if (userInput.isEmpty()) {
                    tvFeedbackBox.visibility = android.view.View.VISIBLE
                    tvFeedbackBox.text = getString(R.string.tutorial_error_empty)
                    tvFeedbackBox.setTextColor("#991B1B".toColorInt())
                    tvFeedbackBox.background = ViewStyleUtility.createRoundedDrawable("#FEE2E2", 10f)
                    return@setOnClickListener
                }

                // Sentence validation logic against target sequence "NAMAGRAMIC"
                val words = userInput.split("\\s+".toRegex()).filter { it.isNotEmpty() }
                val startLetters = words.map { it.first().uppercaseChar().toString() }

                val matchedLetters = mutableListOf<String>()
                val remainingTarget = targetLetters.toMutableList()

                for (letter in startLetters) {
                    if (remainingTarget.contains(letter)) {
                        matchedLetters.add(letter)
                        remainingTarget.remove(letter)
                    }
                }

                val score = matchedLetters.size * 10
                tvFeedbackBox.visibility = android.view.View.VISIBLE

                if (matchedLetters.isNotEmpty()) {
                    tvFeedbackBox.text = Html.fromHtml(
                        "✅ <b>Great attempt!</b><br>" +
                                "Used Letters: <b>${matchedLetters.joinToString(", ")}</b> (${matchedLetters.size}/10)<br>" +
                                "Tutorial Score: <b>$score Points</b>!",
                        Html.FROM_HTML_MODE_LEGACY
                    )
                    tvFeedbackBox.setTextColor("#064E3B".toColorInt())
                    tvFeedbackBox.background = ViewStyleUtility.createRoundedDrawable("#D1FAE5", 10f)
                } else {
                    tvFeedbackBox.text = getString(R.string.tutorial_error_no_match)
                    tvFeedbackBox.setTextColor("#991B1B".toColorInt())
                    tvFeedbackBox.background = ViewStyleUtility.createRoundedDrawable("#FEE2E2", 10f)
                }
            }
            ViewStyleUtility.attachButtonAnimateTouchListener(this)
        }
        rootLayout.addView(btnTestSentence)
        rootLayout.addView(tvFeedbackBox)

        // 9. PRO-TIP BOX WITH CORRECT FULL-SEQUENCE EXAMPLE & BONUS SUMMARY
        val proTipCard = TextView(this).apply {
            text = Html.fromHtml(
                "💡 <b>Pro-Tip for Maximum Points:</b><br>" +
                        "Using words in the exact tile sequence order shown gives you extra bonus points!<br><br>" +
                        "<i>Example using all 10 letters in order:</i><br>" +
                        "<font color=\"#FF0000\"><b>N</b></font>ew <font color=\"#FF0000\"><b>A</b></font>nagram <font color=\"#FF0000\"><b>M</b></font>asters <font color=\"#FF0000\"><b>A</b></font>lways <font color=\"#FF0000\"><b>G</b></font>enerate <font color=\"#FF0000\"><b>R</b></font>eally <font color=\"#FF0000\"><b>A</b></font>wesome <font color=\"#FF0000\"><b>M</b></font>essages <font color=\"#FF0000\"><b>I</b></font>n <font color=\"#FF0000\"><b>C</b></font>ode\"<br><br>" +
                        "All letters used - bonus points!<br>" +
                        "All letters used <font color=\"#FF0000\"><b>IN ORDER</b></font> - extra bonus (maximum) points!",
                Html.FROM_HTML_MODE_LEGACY
            )
            textSize = 13f
            setTextColor(if (isDarkMode) "#A7F3D0".toColorInt() else "#064E3B".toColorInt())
            setPadding(24, 16, 24, 16)
            background = ViewStyleUtility.createRoundedDrawable(if (isDarkMode) "#064E3B" else "#D1FAE5", 10f)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 20)
            }
            setLineSpacing(0f, 1.15f)
        }
        rootLayout.addView(proTipCard)

        // 10. PERSISTENT MENU BUTTON AT BOTTOM
        ViewStyleUtility.addPersistentMenuButton(this, rootLayout)

        scrollView.addView(rootLayout)
        setContentView(scrollView)
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}