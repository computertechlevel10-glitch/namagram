package com.elothar.namagram

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Html
import android.text.Spanned
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

class HowToPlayActivity : AppCompatActivity() {

    private lateinit var soundManager: SoundManager
    private var secretTapCount = 0

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

        // 2. ScrollView wrapping the entire screen content
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

        // 3. PAGE TITLE (with 10-tap secret code listener)
        val tvTitle = TextView(this).apply {
            ViewStyleUtility.applyHeaderStyle(this, "HOW TO PLAY", isMainTitle = true)
            setTextColor(textColorPrimary)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)

            setOnClickListener {
                secretTapCount++
                if (secretTapCount >= 10) {
                    secretTapCount = 0
                    showSecretCodeDialog()
                }
            }
        }
        rootLayout.addView(tvTitle)

        // Helper to parse HTML strings directly for bold/italics
        fun parseHtml(htmlText: String): Spanned {
            return Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)
        }

        // 4. CONTENT CARD
        val textCard = TextView(this).apply {
            text = parseHtml(
                "Welcome to <font color=\"#008000\"><b>Namagram</b></font>, the game that's easy to play, but hard to master!<br><br>" +
                        "<font color=\"#FF0000\"><b>SINGLE PLAYER</b></font><br>" +
                        "At the <font color=\"#008000\"><b>Beginner Level</b></font>, you will be given a choice between 3 and 4 tile, random letter sequences. You must use those randomly generated letters as the starting letter of words, in order to make the longest <font color=\"#800080\"><b>VALID</b></font> sentence you can. You can choose between difficulty of <b>Easy/Medium/Hard</b> and the corresponding time limits of <font color=\"#FF0000\"><b>90</b>/<b>60</b>/<b>30</b></font> seconds.<br><br>" +
                        "As an example of how to play, if your generated letters on a 4 letter sequence were <font color=\"#FFA500\"><b>D</b></font> - <font color=\"#FFA500\"><b>L</b></font> - <font color=\"#FFA500\"><b>P</b></font> - <font color=\"#FFA500\"><b>I</b></font>, some of the sentences you could make would be:<br><br>"+
                        "1. <font color=\"#FFA500\"><b>P</b></font>ets <font color=\"#FFA500\"><b>L</b></font>ick <font color=\"#FFA500\"><b>I</b></font>ce : (Using <font color=\"#FFA500\"><b>P</b></font>, <font color=\"#FFA500\"><b>L</b></font>, and <font color=\"#FFA500\"><b>I</b></font>)<br><br>" +
                        "2. <font color=\"#FFA500\"><b>D</b></font>o <font color=\"#FFA500\"><b>P</b></font>ets <font color=\"#FFA500\"><b>L</b></font>ick <font color=\"#FFA500\"><b>I</b></font>ce : (Using <font color=\"#FFA500\"><b>D</b></font>, <font color=\"#FFA500\"><b>P</b></font>, <font color=\"#FFA500\"><b>L</b></font>, and <font color=\"#FFA500\"><b>I</b></font>):(Using <font color=\"#800080\"><b>ALL</b></font> letters)<br><br>" +
                        "3. <font color=\"#FFA500\"><b>D</b></font>on't <font color=\"#FFA500\"><b>L</b></font>et <font color=\"#FFA500\"><b>P</b></font>eople <font color=\"#FFA500\"><b>I</b></font>n. (Using <font color=\"#800080\"><b>ALL</b></font> letters <font color=\"#800080\"><b>IN ORDER</b></font>)<br><br>" +
                        "Once you have submitted a sentence, or the round timer has run out, your sentence will be evaluated online by the <font color=\"#008000\"><b>Namagram A.I.</b></font>. The A.I. will give you a score based on the <i>Rules of the Game</i>, (which can be found on the main page). The AI will only score <font color=\"#800080\"><b>VALID</b></font> sentences. Invalid sentences will not score, neither will partial sentences, so keep that in mind as the timer starts to run out. You will get points for every letter that is used as the start of a word in your <font color=\"#800080\"><b>VALID</b></font> sentence. You will get extra points if you use <font color=\"#800080\"><b>ALL</b></font> letters in your tile sequence. You will get maximum points if you can use all letters from your sequence <font color=\"#800080\"><b>IN ORDER</b></font> that they appear.<br><br><br>" +
                        "<font color=\"#FF0000\"><b>ACHIEVEMENTS</b></font><br>" +
                        "As you progress you will earn achievements. You can earn multiple achievements in a single game, but can only claim 1 achievement each time. Unlocking a certain amount of achievements, <font color=\"#FFA500\"><b>(70)</b></font>, will open up <font color=\"#008000\"><b>Intermediate Level</b></font> where you can choose letter sequences up to 7 letters long. After gaining more achievements, <font color=\"#FFA500\"><b>(140)</b></font> , you will finally unlock <font color=\"#008000\"><b>Expert Level</b></font>, where letter sequences can go up to 10 letters long. In total there are <b>210 Achievements</b> in the game, though some of them are near impossible to gain. \uD83D\uDE09<br><br><br>"+
                        "<font color=\"#FF0000\"><b>MULTIPLAYER</b></font><br>" +
                        "There is a local Multiplayer mode where you can play against another person by passing the device back and forth. Both players receive the same letter sequences per round and try to score higher than each other. <i>TIMER</i>, number of <i>ROUNDS</i> and number of <i>TILES</i> can all be chosen at the start of the match, but only at the level you currently hold on your single player profile, either <font color=\"#008000\"><b>Beginner, Intermediate or Expert</b></font>. Achievements, and scores, gained in Multiplayer mode <font color=\"#FF0000\"><b>do not</b></font> attach onto your single player profile, but there is a separate Multiplayer <b>'High Score'</b> board.<br><br><br>" +
                        "<font color=\"#FF0000\"><b>DAILY</b></font> <font color=\"#008000\"><b>NAMAGRAM</b></font> <font color=\"#FF0000\"><b>CHALLENGE</b></font><br>" +
                        "Everyday the system will create the <font color=\"#FF0000\"><b>Daily</b></font> <font color=\"#008000\"><b>Namagram</b></font> <font color=\"#FF0000\"><b>Challenge</b></font>. This is a set of games with tile sequences from 3 tiles to 10 tiles, each sequence under a 60 second timer. All players, all over the world, will receive the <font color=\"#800080\"><b>SAME</b></font> sequence of letters in the challenges. Players will have <font color=\"#FF0000\">ONE<b></b></font> chance at each tile difficulty. Success in <font color=\"#800080\"><b>ALL</b></font> of the tile challenges for that day will earn a, very hard to get, special dated badge on your personal <font color=\"#FFA500\"><b>Daily Master Gallery</b></font> page.<br><br><br>" +
                        "<font color=\"#FF0000\"><b>SHARING</b></font><br>" +
                        "Players can share their progress and stats in <font color=\"#008000\"><b>NAMAGRAM</b></font>, as well as share their <font color=\"#FF0000\"><b>Daily</b></font> <font color=\"#008000\"><b>Namagram</b></font> <font color=\"#FF0000\"><b>Challenge</b></font> stats, by using the sharing buttons found in the <b>'Single Player Lobby'</b>, and the <b>'Daily Namagram Challenge'</b> pages respectively.<br><br><br>"+
                        "Below is a semi-interactive tutorial of the game principles if you want to try it out before playing."
            )
            textSize = 13.5f
            setTextColor(textColorPrimary)
            setPadding(28, 22, 28, 22)
            background = ViewStyleUtility.createRoundedDrawable(cardBackgroundColor, 12f)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 16)
            }
            setLineSpacing(0f, 1.2f)
        }
        rootLayout.addView(textCard)

        // 5. INTERACTIVE TUTORIAL BUTTON
        val btnInteractiveTutorial = Button(this).apply {
            text = getString(R.string.btn_interactive_tutorial)
            textSize = 14f
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            background = ViewStyleUtility.createRoundedDrawable(ViewStyleUtility.COLOR_SUCCESS_EMERALD, 8f)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 120).apply {
                setMargins(0, 0, 0, 16)
            }
            setOnClickListener {
                startActivity(Intent(this@HowToPlayActivity, TutorialActivity::class.java))
            }
            ViewStyleUtility.attachButtonAnimateTouchListener(this)
        }
        rootLayout.addView(btnInteractiveTutorial)

        // 6. PERSISTENT MENU BUTTON
        ViewStyleUtility.addPersistentMenuButton(this, rootLayout)

        scrollView.addView(rootLayout)
        setContentView(scrollView)
    }

    private fun showSecretCodeDialog() {
        soundManager.playVictoryChime()
        val inputEditText = EditText(this).apply {
            hint = "Enter Secret Code..."
            setPadding(40, 30, 40, 30)
        }

        AlertDialog.Builder(this)
            .setTitle("🔓 Secret Developer Code")
            .setMessage("Enter your code to unlock progression gates instantly:")
            .setView(inputEditText)
            .setPositiveButton("Apply") { dialog, _ ->
                val code = inputEditText.text.toString().trim().uppercase(Locale.ROOT)
                val achievementManager = AchievementManager(this)

                when (code) {
                    "UNLOCKINTERMEDIATE", "INTERMEDIATE" -> {
                        achievementManager.unlockAchievement("inter_gate")
                        Toast.makeText(this, "⚔️ Intermediate Gate Unlocked!", Toast.LENGTH_SHORT).show()
                    }
                    "UNLOCKTHEEXPERTLEVEL1", "EXPERT" -> {
                        achievementManager.unlockAchievement("inter_gate")
                        achievementManager.unlockAchievement("expert_gate")
                        Toast.makeText(this, "👑 Expert Gates Unlocked!", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(this, "❌ Invalid Secret Code", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}