package com.elothar.namagram

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AchievementsActivity : AppCompatActivity() {

    private lateinit var achievementManager: AchievementManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var etPlayerNameSearch: EditText

    // Cached dark mode variable for the adapter to use
    private var isDarkMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        achievementManager = AchievementManager(this)

        // 1. Check Dark Mode Preference
        val prefs = getSharedPreferences("NamagramPrefs", MODE_PRIVATE)
        isDarkMode = prefs.getBoolean("dark_mode_enabled", false)

        // Define dynamic colors based on dark mode state
        val backgroundColor = if (isDarkMode) "#0F172A" else ViewStyleUtility.COLOR_BACKGROUND_LIGHT
        val cardBackgroundColor = if (isDarkMode) "#1E293B" else "#FFFFFF"
        val textColorPrimary = if (isDarkMode) Color.WHITE else ViewStyleUtility.COLOR_PRIMARY_SLATE.toColorInt()

        val defaultName = MatchSettings.player1Nickname.ifBlank { "Guest" }
        achievementManager.targetPlayerOverride = defaultName

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            setBackgroundColor(backgroundColor.toColorInt())
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }

        // --- TITLE ---
        val tvTitle = TextView(this).apply {
            text = getString(R.string.achievements_title)
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(textColorPrimary)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }
        rootLayout.addView(tvTitle)

        // --- PLAYER SEARCH BAR CONTAINER ---
        val searchCard = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16, 16, 16, 16)
            background = ViewStyleUtility.createRoundedDrawable(cardBackgroundColor, 12f)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 24)
            }
        }

        etPlayerNameSearch = EditText(this).apply {
            ViewStyleUtility.applyEditTextStyle(this, "Enter player nickname...")
            setText(defaultName)
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(0, 0, 16, 0)
            }
        }
        searchCard.addView(etPlayerNameSearch)

        val btnSearch = Button(this).apply {
            text = getString(R.string.btn_view_label)
            textSize = 14f
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            background = ViewStyleUtility.createRoundedDrawable(ViewStyleUtility.COLOR_SUCCESS_EMERALD, 8f)
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, 110)
            setOnClickListener {
                val searchName = etPlayerNameSearch.text.toString().trim()
                if (searchName.isNotEmpty()) {
                    achievementManager.targetPlayerOverride = searchName
                    reloadAchievementsList()
                }
            }
            ViewStyleUtility.attachButtonAnimateTouchListener(this)
        }
        searchCard.addView(btnSearch)
        rootLayout.addView(searchCard)

        // --- RECYCLER VIEW FOR ACHIEVEMENTS ---
        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@AchievementsActivity)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f).apply {
                setMargins(0, 0, 0, 24)
            }
        }
        rootLayout.addView(recyclerView)

        // --- RETURN TO MAIN MENU BUTTON ---
        val btnReturnToMenu = Button(this).apply {
            text = getString(R.string.btn_return_main_menu)
            textSize = 15f
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            background = ViewStyleUtility.createRoundedDrawable(ViewStyleUtility.COLOR_PRIMARY_SLATE, 12f)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 120)
            setOnClickListener {
                finish()
            }
            ViewStyleUtility.attachButtonAnimateTouchListener(this)
        }
        rootLayout.addView(btnReturnToMenu)

        reloadAchievementsList()
        setContentView(rootLayout)
    }

    private fun reloadAchievementsList() {
        val achievementsList = achievementManager.getAllAchievements()
        recyclerView.adapter = AchievementsAdapter(achievementsList)
    }

    // --- RECYCLERVIEW ADAPTER FOR RENDERING ACHIEVEMENTS ---
    private inner class AchievementsAdapter(private val items: List<Achievement>) :
        RecyclerView.Adapter<AchievementsAdapter.ViewHolder>() {

        inner class ViewHolder(val cardView: LinearLayout, val tvEmoji: TextView, val tvTitle: TextView, val tvDesc: TextView) : RecyclerView.ViewHolder(cardView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val card = LinearLayout(parent.context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(24, 24, 24, 24)
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 0, 0, 16)
                }
            }

            val emoji = TextView(parent.context).apply {
                textSize = 28f
                setPadding(0, 0, 24, 0)
                gravity = Gravity.CENTER
            }
            card.addView(emoji)

            val textContainer = LinearLayout(parent.context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            }

            val title = TextView(parent.context).apply {
                textSize = 16f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            textContainer.addView(title)

            val desc = TextView(parent.context).apply {
                textSize = 13f
                setTextColor(if (isDarkMode) "#94A3B8".toColorInt() else ViewStyleUtility.COLOR_TEXT_MUTED.toColorInt())
            }
            textContainer.addView(desc)
            card.addView(textContainer)

            return ViewHolder(card, emoji, title, desc)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.tvEmoji.text = if (item.isUnlocked) item.iconEmoji else "🔒"
            holder.tvTitle.text = item.title // 👉 Always displays the full title as a roadmap placeholder
            holder.tvDesc.text = item.displayDescription // 👉 Displays description or "🔒 ? ? ? ? ?" if locked

            if (item.isUnlocked) {
                holder.cardView.background = ViewStyleUtility.createRoundedDrawable(if (isDarkMode) "#1E293B" else "#FFFFFF", 14f)
                holder.tvTitle.setTextColor(if (isDarkMode) Color.WHITE else ViewStyleUtility.COLOR_PRIMARY_SLATE.toColorInt())
                holder.tvEmoji.alpha = 1.0f
            } else {
                holder.cardView.background = ViewStyleUtility.createRoundedDrawable(if (isDarkMode) "#0F172A" else "#E2E8F0", 14f)
                holder.tvTitle.setTextColor("#64748B".toColorInt())
                holder.tvEmoji.alpha = 0.6f
            }
        }

        override fun getItemCount() = items.size
    }
}