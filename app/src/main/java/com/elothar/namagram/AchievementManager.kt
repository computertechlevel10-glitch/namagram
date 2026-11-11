package com.elothar.namagram

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import java.util.Calendar
import java.util.Locale

enum class GameTier {
    BEGINNER,
    INTERMEDIATE,
    EXPERT
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val tier: GameTier = GameTier.BEGINNER,
    var isUnlocked: Boolean = false
) {
    val displayDescription: String
        get() = if (isUnlocked) description else "🔒 ? ? ? ? ? ? ? ? ?"
}

class AchievementManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("NamagramAchievements", Context.MODE_PRIVATE)

    var targetPlayerOverride: String? = null
    private val pendingAchievementsThisTurn = mutableSetOf<Achievement>()

    private fun getPlayerKey(): String {
        val rawName = (targetPlayerOverride ?: MatchSettings.player1Nickname.takeIf { it.isNotBlank() } ?: "Guest").trim()
        val lower = rawName.lowercase(Locale.ROOT)

        // Automatically block generic/default names from earning achievements
        if (rawName.isEmpty() || lower == "player 1" || lower == "player 2" || lower == "guest" || lower == "player1" || lower == "player2") {
            return "Player_Ignored"
        }

        // Clean up the custom name safely for storage keys
        val sanitizedLower = lower.replace(Regex("\\s+"), "_")
        return "Player_$sanitizedLower"
    }

    fun getAllAchievements(): List<Achievement> {
        val playerPrefix = getPlayerKey()
        return listOf(
            // =====================================================================
            // 1. EXPLORATION & INTERACTION ACHIEVEMENTS (10 Total)
            // =====================================================================
            Achievement("expl_tutorial", "Curious Mind", "Explore the How To Play or Tutorial screen.", "🗺️", GameTier.BEGINNER),
            Achievement("expl_high_scores", "Score Watcher", "Inspect the High Scores leaderboard 5 times.", "📊", GameTier.BEGINNER),
            Achievement("expl_night_owl", "Night Owl", "Open or play the app between 10:00 PM and 4:00 AM.", "🌙", GameTier.BEGINNER),
            Achievement("expl_weekend", "Weekend Warrior", "Log activity on both Saturday and Sunday.", "🗓️", GameTier.BEGINNER),
            Achievement("expl_profile", "Wordsmith Profile", "Set up or customize your player nickname.", "✏️", GameTier.BEGINNER),
            Achievement("expl_settings", "Tinkerer", "Visit and review the settings configuration screen.", "⚙️", GameTier.BEGINNER),
            Achievement("expl_rules", "Rulebook Reader", "Spend time reading the detailed game rules.", "📖", GameTier.BEGINNER),
            Achievement("expl_sessions", "Dedicated Visitor", "Open the app across 5 different calendar days.", "🚪", GameTier.BEGINNER),
            Achievement("expl_milestone", "App Explorer", "Interact with at least 4 different app menus.", "🧭", GameTier.BEGINNER),
            Achievement("expl_completionist", "Full Tour", "Unlock all 10 exploration achievements.", "🔍", GameTier.BEGINNER),

            // =====================================================================
            // 2. BEGINNER TIER ACHIEVEMENTS (75 Total)
            // =====================================================================
            Achievement("first_win", "First Words", "Submit your first valid AI-approved sentence!", "✍️", GameTier.BEGINNER),
            Achievement("ran_out_of_time", "Tick Tock", "Run out of time before submitting your sentence!", "⏳", GameTier.BEGINNER),
            Achievement("ai_rejected", "Denied by Referee", "Have your sentence rejected by the Namagram AI!", "🤖", GameTier.BEGINNER),
            Achievement("spelling_disaster", "Spelling Disaster", "Submit a word that fails dictionary validation!", "💥", GameTier.BEGINNER),
            Achievement("streak_3", "Wordsmith in Training", "Successfully submit 3 valid sentences in a row!", "🔥", GameTier.BEGINNER),
            Achievement("streak_5", "Flow State", "Successfully submit 5 valid sentences in a row!", "⚡", GameTier.BEGINNER),
            Achievement("beat_easy", "Casual Wordsmith", "Successfully complete a game on Easy Mode!", "🌱", GameTier.BEGINNER),
            Achievement("streak_easy_3", "Easy Streak", "Win 3 games in a row on Easy Mode!", "🌿", GameTier.BEGINNER),
            Achievement("buzzer_beater", "Buzzer Beater", "Submit an AI-approved sentence with less than 3 seconds remaining.", "⏰", GameTier.BEGINNER),
            Achievement("lightning_fast", "Lightning Fast", "Submit a valid sentence in under 10 seconds.", "⚡", GameTier.BEGINNER),
            Achievement("century_club", "Century Club", "Reach 100 total cumulative points.", "💯", GameTier.BEGINNER),
            Achievement("single_round_50", "High Scorer", "Score 50+ points in a single round.", "⭐", GameTier.BEGINNER),
            Achievement("short_and_sweet", "Short & Sweet", "Submit a valid, AI-approved sentence using 3 or fewer total words.", "🤏", GameTier.BEGINNER),
            Achievement("minimalist", "Minimalist", "Successfully score points while leaving drawn tiles unused.", "🧩", GameTier.BEGINNER),
            Achievement("clean_slate", "Clean Slate", "Clear a round with zero dictionary errors or spelling retries.", "🧼", GameTier.BEGINNER),
            Achievement("backspace_warrior", "Backspace Warrior", "Type and delete more characters than you actually submitted in a turn.", "⌨️", GameTier.BEGINNER),
            Achievement("dictionary_nemesis", "Spelling Bee Reject", "Fail dictionary validation 3 times in a single turn.", "📖", GameTier.BEGINNER),
            Achievement("blank_canvas", "Blank Canvas", "Let the timer expire without typing a single letter.", "🎨", GameTier.BEGINNER),
            Achievement("beg_flash_writer", "Flash Scribe", "Complete a 3-tile round in under 8 seconds.", "⚡", GameTier.BEGINNER),
            Achievement("beg_overconfident", "Overconfident", "Submit an incorrect sentence in under 5 seconds.", "🤦", GameTier.BEGINNER),
            Achievement("beg_order_miss", "Misordered Trio", "Use all 3 tiles but miss the strict order bonus.", "🔀", GameTier.BEGINNER),
            Achievement("beg_perfect_3", "Trio Master", "Score maximum points on a 3-letter sequence.", "🎯", GameTier.BEGINNER),
            Achievement("beg_perfect_4", "Quad Master", "Score maximum points on a 4-letter sequence.", "💎", GameTier.BEGINNER),
            Achievement("beg_speed_sprint", "Quick Sprint", "Solve a beginner sequence in under 15 seconds.", "🏃", GameTier.BEGINNER),
            Achievement("beg_quick_thinker", "Quick Thinker", "Solve a beginner sequence in under 12 seconds.", "💡", GameTier.BEGINNER),
            Achievement("beg_lightning_3", "Lightning 3", "Solve 3 beginner sequences in under 10 seconds each.", "⚡", GameTier.BEGINNER),
            Achievement("beg_clock_watcher", "Clock Watcher", "Finish a beginner sequence with 75% timer remaining.", "⌚", GameTier.BEGINNER),
            Achievement("beg_rapid_fire", "Rapid Fire", "Complete 5 beginner games in rapid succession.", "🔥", GameTier.BEGINNER),
            Achievement("beg_trio_10", "Trio Explorer", "Successfully solve 10 3-letter word sequences.", "📦", GameTier.BEGINNER),
            Achievement("beg_trio_50", "Trio Hoarder", "Successfully solve 50 3-letter word sequences.", "📦", GameTier.BEGINNER),
            Achievement("beg_quad_10", "Quad Explorer", "Successfully solve 10 4-letter word sequences.", "🗂️", GameTier.BEGINNER),
            Achievement("beg_quad_50", "Quad Hoarder", "Successfully solve 50 4-letter word sequences.", "🗂️", GameTier.BEGINNER),
            Achievement("beg_balanced", "Balanced Diet", "Solve an equal number of 3 and 4-letter puzzles, over 10 each.", "⚖️", GameTier.BEGINNER),
            Achievement("beg_word_100", "Century Beginner", "Submit 100 total beginner words.", "💯", GameTier.BEGINNER),
            Achievement("beg_word_250", "Beginner Hoarder", "Submit 250 total beginner words.", "📚", GameTier.BEGINNER),
            Achievement("beg_unstoppable", "Beginner Titan", "Win 5 beginner games in a row.", "🌪️", GameTier.BEGINNER),
            Achievement("beg_untouchable", "Untouchable Beginner", "Win 10 beginner games in a row.", "👑", GameTier.BEGINNER),
            Achievement("beg_daily", "Daily Beginner", "Play beginner mode 3 days in a row.", "📅", GameTier.BEGINNER),
            Achievement("beg_weekly", "Dedicated Beginner", "Play beginner mode 7 days in a row.", "🗓️", GameTier.BEGINNER),
            Achievement("beg_bounce", "Bounce Back", "Recover from a 2-game loss streak to win.", "🏓", GameTier.BEGINNER),
            Achievement("beg_persistent", "Persistent Beginner", "Complete 25 total beginner games.", "🛡️", GameTier.BEGINNER),
            Achievement("beg_flawless", "Flawless Beginner", "Complete a beginner puzzle with 100% accuracy.", "✨", GameTier.BEGINNER),
            Achievement("beg_precision", "Precision Beginner", "Solve 10 beginner puzzles without a mistake.", "🎯", GameTier.BEGINNER),
            Achievement("beg_first_steps", "First Steps", "Submit your very first beginner sentence.", "👶", GameTier.BEGINNER),
            Achievement("beg_halfway", "Halfway Home", "Earn 30 total achievements.", "🏅", GameTier.BEGINNER),
            Achievement("local_multi_first", "Pass & Play", "Complete your first Multiplayer match.", "📱", GameTier.BEGINNER),
            Achievement("local_multi_win1", "Couch Champion", "Win a Multiplayer match against a friend.", "🏆", GameTier.BEGINNER),
            Achievement("local_multi_streak", "Duet Streak", "Win 3 Multiplayer matches in a row.", "🤝", GameTier.BEGINNER),
            Achievement("local_multi_close", "Nail Biter", "Win a Multiplayer match by fewer than 5 points.", "🤏", GameTier.BEGINNER),
            Achievement("local_multi_clutch", "Pass-Play Clutch", "Submit a winning sentence with <3 seconds left in Multiplayer.", "⏱️", GameTier.BEGINNER),
            Achievement("beg_extra_1", "Beginner Spark", "Score 30+ points in a beginner round.", "✨", GameTier.BEGINNER),
            Achievement("beg_extra_2", "Beginner Surge", "Score 40+ points in a beginner round.", "🌟", GameTier.BEGINNER),
            Achievement("beg_extra_3", "Beginner Zeal", "Complete 5 beginner rounds in one session.", "💪", GameTier.BEGINNER),
            Achievement("beg_extra_4", "Beginner Focus", "Type a sentence without backspacing once.", "🎯", GameTier.BEGINNER),
            Achievement("beg_extra_5", "Beginner Swift", "Type 5 beginner words in under 15 seconds each.", "⚡", GameTier.BEGINNER),
            Achievement("beg_extra_6", "Beginner Solid", "Win 2 beginner games using 4-letter sequences.", "🛡️", GameTier.BEGINNER),
            Achievement("beg_extra_7", "Beginner Steady", "Win 2 beginner games using 3-letter sequences.", "🛡️", GameTier.BEGINNER),
            Achievement("beg_extra_8", "Beginner Resilience", "Win a round after getting an initial AI rejection.", "🩹", GameTier.BEGINNER),
            Achievement("beg_extra_9", "Beginner Sharp", "Clear 3 beginner rounds with zero spelling errors.", "🧼", GameTier.BEGINNER),
            Achievement("beg_extra_10", "Beginner Veteran", "Complete 30 total beginner rounds.", "🏅", GameTier.BEGINNER),
            Achievement("beg_extra_11", "Beginner Adept", "Complete 40 total beginner rounds.", "🏅", GameTier.BEGINNER),
            Achievement("beg_extra_12", "Beginner Expert-In-Training", "Complete 50 total beginner rounds.", "🏅", GameTier.BEGINNER),
            Achievement("beg_extra_13", "Beginner Fanatic", "Complete 60 total beginner rounds.", "🏅", GameTier.BEGINNER),
            Achievement("beg_extra_14", "Beginner Dedicated", "Complete 70 total beginner rounds.", "🏅", GameTier.BEGINNER),
            Achievement("beg_extra_15", "Beginner Master", "Complete 75 total beginner rounds.", "👑", GameTier.BEGINNER),
            Achievement("local_multi_2", "Couch Rivalry", "Play 5 Multiplayer matches.", "👥", GameTier.BEGINNER),
            Achievement("local_multi_3", "Friendly Duellist", "Play 10 Multiplayer matches.", "⚔️", GameTier.BEGINNER),
            Achievement("local_multi_4", "Living Room Legend", "Win 10 Multiplayer matches.", "🛋️", GameTier.BEGINNER),
            Achievement("local_multi_5", "Pass-Play Sweep", "Win every round in a Multiplayer match.", "🧹", GameTier.BEGINNER),
            Achievement("local_multi_6", "Comeback Couch", "Win a Multiplayer match after losing round one.", "👑", GameTier.BEGINNER),
            Achievement("local_multi_7", "Balanced Duel", "Draw a Multiplayer match with a friend.", "🤝", GameTier.BEGINNER),
            Achievement("local_multi_8", "Speed Pass", "Submit a Multiplayer sentence in under 10 seconds.", "⚡", GameTier.BEGINNER),
            Achievement("local_multi_9", "Pass-Play Ace", "Score 100+ points in a Multiplayer match.", "⭐", GameTier.BEGINNER),
            Achievement("local_multi_10", "Ultimate Couch Rival", "Win 20 Multiplayer matches.", "👑", GameTier.BEGINNER),
            Achievement("inter_gate", "Intermediate Gate", "Earn 70 total achievements to unlock the Intermediate tier.", "🚪", GameTier.BEGINNER),

            // =====================================================================
            // 3. INTERMEDIATE TIER ACHIEVEMENTS (76 Total)
            // =====================================================================
            Achievement("beat_medium", "Balanced Challenger", "Successfully complete a game on Medium Mode!", "🛡️", GameTier.INTERMEDIATE),
            Achievement("streak_medium_3", "Medium Streak", "Win 3 games in a row on Medium Mode!", "⚔️", GameTier.INTERMEDIATE),
            Achievement("perfect_5_tiles", "5-Tile Master", "Score max points on 5 tiles!", "🧩", GameTier.INTERMEDIATE),
            Achievement("streak_5_tiles_3", "5-Tile Prodigy", "Get 3 perfect scores in a row on 5 tiles!", "✨", GameTier.INTERMEDIATE),
            Achievement("perfect_6_tiles", "6-Tile Master", "Score max points on 6 tiles!", "🎲", GameTier.INTERMEDIATE),
            Achievement("streak_6_tiles_3", "6-Tile Prodigy", "Get 3 perfect scores in a row on 6 tiles!", "✨", GameTier.INTERMEDIATE),
            Achievement("perfect_7_tiles", "7-Tile Master", "Score max points on 7 tiles!", "🎯", GameTier.INTERMEDIATE),
            Achievement("streak_7_tiles_3", "7-Tile Prodigy", "Get 3 perfect scores in a row on 7 tiles!", "✨", GameTier.INTERMEDIATE),
            Achievement("vowel_virtuoso", "Vowel Virtuoso", "Submit a valid sentence where every word starts with a vowel.", "🔤", GameTier.INTERMEDIATE),
            Achievement("rare_gem", "Rare Gem", "Successfully use a word starting with Q, X, Z, J, or K.", "💎", GameTier.INTERMEDIATE),
            Achievement("symmetry_master", "Symmetry Master", "Submit a sentence where the first and last words start with the same letter.", "🪞", GameTier.INTERMEDIATE),
            Achievement("alphabet_soup", "Alphabet Soup", "Use at least 6 different starting letters in a single sentence.", "🥣", GameTier.INTERMEDIATE),
            Achievement("punctuation_perfect", "Punctuation Perfect", "Submit a sentence with a comma and terminal punctuation approved by AI.", "✒️", GameTier.INTERMEDIATE),
            Achievement("art_of_alliteration", "Art of Alliteration", "Submit a valid sentence where 3 consecutive words start with the same letter.", "📜", GameTier.INTERMEDIATE),
            Achievement("slow_and_steady", "Slow & Steady", "Spend over 80% of your time limit crafting and successfully submit a sentence.", "⏳", GameTier.INTERMEDIATE),
            Achievement("hesitation_master", "Indecisive Mind", "Spend over 80% of your time crafting a sentence with 2 or fewer words.", "😵‍💫", GameTier.INTERMEDIATE),
            Achievement("close_but_no_cigar", "So Close...", "Miss a full strict-order bonus because of just 1 misplaced letter sequence.", "🚬", GameTier.INTERMEDIATE),
            Achievement("analysis_paralysis", "Analysis Paralysis", "Spend 90% of your time staring before typing your very first letter.", "🗿", GameTier.INTERMEDIATE),
            Achievement("inter_500_club", "Half-Thousand", "Reach 500 total cumulative points.", "🏛️", GameTier.INTERMEDIATE),
            Achievement("inter_flash_5", "5-Tile Sprint", "Solve a 5-tile puzzle in under 15 seconds.", "⚡", GameTier.INTERMEDIATE),
            Achievement("inter_flash_6", "6-Tile Sprint", "Solve a 6-tile puzzle in under 20 seconds.", "⚡", GameTier.INTERMEDIATE),
            Achievement("inter_flash_7", "7-Tile Sprint", "Solve a 7-tile puzzle in under 25 seconds.", "⚡", GameTier.INTERMEDIATE),
            Achievement("inter_streak_5", "5-Tile Streak", "Win 5 games in a row on 5 tiles.", "🔥", GameTier.INTERMEDIATE),
            Achievement("inter_streak_6", "6-Tile Streak", "Win 5 games in a row on 6 tiles.", "🔥", GameTier.INTERMEDIATE),
            Achievement("inter_streak_7", "7-Tile Streak", "Win 5 games in a row on 7 tiles.", "🔥", GameTier.INTERMEDIATE),
            Achievement("inter_jackpot_5", "5-Tile Jackpot", "Earn both all-letter and order bonuses on 5 tiles.", "💰", GameTier.INTERMEDIATE),
            Achievement("inter_jackpot_6", "6-Tile Jackpot", "Earn both all-letter and order bonuses on 6 tiles.", "💰", GameTier.INTERMEDIATE),
            Achievement("inter_jackpot_7", "7-Tile Jackpot", "Earn both all-letter and order bonuses on 7 tiles.", "💰", GameTier.INTERMEDIATE),
            Achievement("inter_denied_5", "5-Tile Rejection", "Survive an AI rejection on a 5-tile round and win next.", "🤖", GameTier.INTERMEDIATE),
            Achievement("inter_denied_6", "6-Tile Rejection", "Survive an AI rejection on a 6-tile round and win next.", "🤖", GameTier.INTERMEDIATE),
            Achievement("inter_word_count_5", "5-Word Crafter", "Submit a 5-word sentence on intermediate mode.", "✍️", GameTier.INTERMEDIATE),
            Achievement("inter_word_count_6", "6-Word Crafter", "Submit a 6-word sentence on intermediate mode.", "✍️", GameTier.INTERMEDIATE),
            Achievement("inter_clean_5", "5-Tile Clean Slate", "Clear a 5-tile round with zero spelling errors.", "🧼", GameTier.INTERMEDIATE),
            Achievement("inter_clean_6", "6-Tile Clean Slate", "Clear a 6-tile round with zero spelling errors.", "🧼", GameTier.INTERMEDIATE),
            Achievement("inter_clean_7", "7-Tile Clean Slate", "Clear a 7-tile round with zero spelling errors.", "🧼", GameTier.INTERMEDIATE),
            Achievement("inter_over_25", "Deep Thinker", "Type and delete over 25 characters on intermediate mode.", "💭", GameTier.INTERMEDIATE),
            Achievement("inter_clutch", "Intermediate Clutch", "Submit a valid intermediate sentence with <3s remaining.", "⏱️", GameTier.INTERMEDIATE),
            Achievement("inter_score_75", "Intermediate High Scorer", "Score 75+ points in a single intermediate round.", "⭐", GameTier.INTERMEDIATE),
            Achievement("inter_score_90", "Intermediate Elite Scorer", "Score 90+ points in a single intermediate round.", "🌟", GameTier.INTERMEDIATE),
            Achievement("inter_marathon", "Intermediate Marathon", "Complete 25 intermediate rounds.", "🏃", GameTier.INTERMEDIATE),
            Achievement("inter_dedication", "Intermediate Dedication", "Complete 50 intermediate rounds.", "🏃‍♂️", GameTier.INTERMEDIATE),
            Achievement("inter_weekend", "Intermediate Weekend", "Play intermediate mode on both Saturday and Sunday.", "🗓️", GameTier.INTERMEDIATE),
            Achievement("inter_night", "Intermediate Night Owl", "Complete an intermediate puzzle past midnight.", "🌙", GameTier.INTERMEDIATE),
            Achievement("inter_balanced_mid", "Balanced Mid-Tier", "Score wins across 5, 6, and 7 tile variations.", "⚖️", GameTier.INTERMEDIATE),
            Achievement("inter_speed_demon", "Medium Speed Demon", "Maintain an average solve time under 20s for 10 games.", "⚡", GameTier.INTERMEDIATE),
            Achievement("inter_multi_1", "Pass-Play Medium", "Win 3 Multiplayer matches on medium timer.", "📱", GameTier.INTERMEDIATE),
            Achievement("inter_multi_2", "Couch Tactician", "Win 5 Multiplayer matches on medium timer.", "🛋️", GameTier.INTERMEDIATE),
            Achievement("inter_multi_3", "Mid-Tier Rival", "Play 15 Multiplayer matches with 5-7 tiles.", "👥", GameTier.INTERMEDIATE),
            Achievement("inter_multi_4", "Pass-Play Dominance", "Sweep a 5-7 tile Multiplayer match.", "🧹", GameTier.INTERMEDIATE),
            Achievement("inter_multi_5", "Comeback Medium", "Win a medium Multiplayer match after trailing.", "👑", GameTier.INTERMEDIATE),
            Achievement("inter_extra_1", "Intermediate Boost", "Score 80+ points in an intermediate round.", "📈", GameTier.INTERMEDIATE),
            Achievement("inter_extra_2", "Intermediate Surge", "Score 100+ points in an intermediate round.", "🚀", GameTier.INTERMEDIATE),
            Achievement("inter_extra_3", "Intermediate Zeal", "Complete 5 intermediate rounds in one session.", "⚡", GameTier.INTERMEDIATE),
            Achievement("inter_extra_4", "Intermediate Focus", "Type an intermediate sentence without backspacing.", "🎯", GameTier.INTERMEDIATE),
            Achievement("inter_extra_5", "Intermediate Swift", "Solve a 7-tile puzzle in under 20 seconds.", "⚡", GameTier.INTERMEDIATE),
            Achievement("inter_extra_6", "Intermediate Solid", "Win 3 games using 6-letter sequences.", "🛡️", GameTier.INTERMEDIATE),
            Achievement("inter_extra_7", "Intermediate Steady", "Win 3 games using 7-letter sequences.", "🛡️", GameTier.INTERMEDIATE),
            Achievement("inter_extra_8", "Intermediate Resilience", "Win an intermediate round after an AI rejection.", "🩹", GameTier.INTERMEDIATE),
            Achievement("inter_extra_9", "Intermediate Sharp", "Clear 5 intermediate rounds with zero spelling errors.", "🧼", GameTier.INTERMEDIATE),
            Achievement("inter_extra_10", "Intermediate Veteran", "Complete 30 intermediate rounds.", "🏅", GameTier.INTERMEDIATE),
            Achievement("inter_extra_11", "Intermediate Adept", "Complete 40 intermediate rounds.", "🏅", GameTier.INTERMEDIATE),
            Achievement("inter_extra_12", "Intermediate Expert", "Complete 50 intermediate rounds.", "🏅", GameTier.INTERMEDIATE),
            Achievement("inter_extra_13", "Intermediate Fanatic", "Complete 60 intermediate rounds.", "🏅", GameTier.INTERMEDIATE),
            Achievement("inter_extra_14", "Intermediate Dedicated", "Complete 70 intermediate rounds.", "🏅", GameTier.INTERMEDIATE),
            Achievement("inter_master", "Intermediate Completionist", "Complete 75 intermediate rounds.", "👑", GameTier.INTERMEDIATE),
            Achievement("inter_buffer_1", "Intermediate Buffer I", "Unlock 70 total achievements.", "📦", GameTier.INTERMEDIATE),
            Achievement("inter_buffer_2", "Intermediate Buffer II", "Unlock 85 total achievements.", "📦", GameTier.INTERMEDIATE),
            Achievement("inter_buffer_3", "Intermediate Buffer III", "Unlock 100 total achievements.", "📦", GameTier.INTERMEDIATE),
            Achievement("inter_buffer_4", "Intermediate Buffer IV", "Unlock 115 total achievements.", "📦", GameTier.INTERMEDIATE),
            Achievement("expert_gate", "Expert Gate", "Earn 140 total achievements to unlock the Expert tier.", "🚪", GameTier.INTERMEDIATE),
            Achievement("inter_extra_15", "Mid-Tier Specialist", "Score max points on 10 intermediate puzzles.", "⭐", GameTier.INTERMEDIATE),
            Achievement("inter_extra_16", "Mid-Tier Legend", "Score max points on 25 intermediate puzzles.", "🌟", GameTier.INTERMEDIATE),
            Achievement("inter_extra_17", "Mid-Tier Champion", "Win 10 Multiplayer intermediate matches.", "🏆", GameTier.INTERMEDIATE),
            Achievement("inter_extra_18", "Mid-Tier Grandmaster", "Win 25 Multiplayer intermediate matches.", "👑", GameTier.INTERMEDIATE),
            Achievement("inter_extra_19", "Mid-Tier Scholar", "Submit 100 valid intermediate words.", "📚", GameTier.INTERMEDIATE),
            Achievement("inter_extra_20", "Mid-Tier Prodigy", "Maintain a 10-game win streak on intermediate mode.", "🔥", GameTier.INTERMEDIATE),

            // =====================================================================
            // 4. EXPERT TIER ACHIEVEMENTS (40 Total)
            // =====================================================================
            Achievement("win_multiplayer", "Multiplayer Champion", "Win your first multiplayer match on Expert!", "🏆", GameTier.EXPERT),
            Achievement("streak_10", "Unstoppable Lexicon", "Successfully submit 10 valid sentences in a row!", "🌪️", GameTier.EXPERT),
            Achievement("beat_hard", "Speed Demon", "Successfully complete a game on Hard Mode (30s timer)!", "⚡", GameTier.EXPERT),
            Achievement("streak_hard_3", "Hard Streak", "Win 3 games in a row on Hard Mode!", "🔥", GameTier.EXPERT),
            Achievement("perfect_8_tiles", "8-Tile Master", "Score max points on 8 tiles!", "⭐", GameTier.EXPERT),
            Achievement("streak_8_tiles_3", "8-Tile Prodigy", "Get 3 perfect scores in a row on 8 tiles!", "🌟", GameTier.EXPERT),
            Achievement("perfect_9_tiles", "9-Tile Master", "Score max points on 9 tiles!", "🔥", GameTier.EXPERT),
            Achievement("streak_9_tiles_3", "9-Tile Prodigy", "Get 3 perfect scores in a row on 9 tiles!", "💥", GameTier.EXPERT),
            Achievement("perfect_10_tiles", "10-Tile Master", "Score max points on 10 tiles!", "👑", GameTier.EXPERT),
            Achievement("streak_10_tiles_3", "10-Tile Prodigy", "Get 3 perfect scores in a row on 10 tiles!", "🏆", GameTier.EXPERT),
            Achievement("novelist", "The Novelist", "Submit a sentence that is 8 or more words long.", "📖", GameTier.EXPERT),
            Achievement("ai_whisperer", "A.I. Whisperer", "Get a 100% perfect evaluation score from the AI.", "🤖", GameTier.EXPERT),
            Achievement("comeback_king", "Comeback King", "Win a multiplayer match after losing the first round.", "👑", GameTier.EXPERT),
            Achievement("sweep_victory", "Sweep!", "Win every single round in a match.", "🏆", GameTier.EXPERT),
            Achievement("gracious_draw", "Gracious Draw", "Complete a multiplayer match that ends in a tie.", "🤝", GameTier.EXPERT),
            Achievement("rocky", "Rocky", "Complete a gruelling 10-round multiplayer match.", "🥊", GameTier.EXPERT),
            Achievement("flawless_masterpiece", "Flawless Masterpiece", "Score max letter points, all-letters bonus, AND strict-order bonus in one round.", "👑", GameTier.EXPERT),
            Achievement("millennium_club", "Millennium Club", "Accumulate 1,000 total points across your profile history.", "🏛️", GameTier.EXPERT),
            Achievement("jack_of_all_trades", "Jack of All Trades", "Score a valid sentence across all tile variations (3 through 10).", "🃏", GameTier.EXPERT),
            Achievement("three_nines", "999", "Play the 9-tile game mode and successfully submit 9 valid sentences.", "🔢", GameTier.EXPERT),
            Achievement("exp_hard_10", "Hard Mode Veteran", "Complete 10 games on 30s hard mode.", "⏱️", GameTier.EXPERT),
            Achievement("exp_hard_50", "Hard Mode Legend", "Complete 50 games on 30s hard mode.", "⚡", GameTier.EXPERT),
            Achievement("exp_multi_5", "Multiplayer Master", "Win 5 multiplayer matches.", "🏆", GameTier.EXPERT),
            Achievement("exp_multi_15", "Multiplayer Legend", "Win 15 multiplayer matches.", "👑", GameTier.EXPERT),
            Achievement("exp_score_150", "Grand Scorer", "Score 150+ total points in an expert session.", "🌟", GameTier.EXPERT),
            Achievement("exp_streak_15", "Unstoppable Expert", "Achieve a 15-win streak on expert mode.", "🌪️", GameTier.EXPERT),
            Achievement("exp_marathon_100", "Expert Marathon", "Complete 100 total expert rounds.", "🏃", GameTier.EXPERT),
            Achievement("exp_clean_expert", "Flawless Expert", "Complete 10 expert rounds with zero errors.", "🧼", GameTier.EXPERT),
            Achievement("exp_clutch_expert", "Expert Clutch", "Submit an expert sentence with <2 seconds remaining.", "⏱️", GameTier.EXPERT),
            Achievement("exp_all_tiles_pro", "Master of All", "Complete 10 successful rounds across every single tile size.", "🃏", GameTier.EXPERT),
            Achievement("exp_cumulative_5k", "Multi-Millennium Club", "Accumulate 5,000 total score points.", "🏛️", GameTier.EXPERT),
            Achievement("exp_cumulative_10k", "Namagram Legend", "Accumulate 10,000 total score points.", "👑", GameTier.EXPERT),
            Achievement("exp_night_expert", "Expert Night Owl", "Complete 10 expert matches past midnight.", "🌙", GameTier.EXPERT),
            Achievement("exp_weekend_expert", "Expert Weekend Warrior", "Win 10 expert matches over a weekend.", "🗓️", GameTier.EXPERT),
            Achievement("exp_speed_expert", "Lightning Expert", "Solve an 8-tile puzzle in under 20 seconds.", "⚡", GameTier.EXPERT),
            Achievement("exp_buffer_1", "Expert Buffer I", "Unlock 140 total achievements.", "📦", GameTier.EXPERT),
            Achievement("exp_buffer_2", "Expert Buffer II", "Unlock 160 total achievements.", "📦", GameTier.EXPERT),
            Achievement("exp_buffer_3", "Expert Buffer III", "Unlock 180 total achievements.", "📦", GameTier.EXPERT),
            Achievement("exp_buffer_4", "Expert Buffer IV", "Unlock 190 total achievements.", "📦", GameTier.EXPERT),
            Achievement("exp_grandmaster", "Namagram Grandmaster", "Unlock all 200 achievements in Namagram!", "👑", GameTier.EXPERT),

            // =====================================================================
            // 5. DAILY NAMAGRAM HUB ACHIEVEMENTS
            // =====================================================================
            Achievement("daily_challenge_3", "3-Tile Daily Pro", "Successfully complete the daily 3-tile challenge.", "🧩", GameTier.BEGINNER),
            Achievement("daily_challenge_4", "4-Tile Daily Pro", "Successfully complete the daily 4-tile challenge.", "🧩", GameTier.BEGINNER),
            Achievement("daily_challenge_5", "5-Tile Daily Pro", "Successfully complete the daily 5-tile challenge.", "🧩", GameTier.INTERMEDIATE),
            Achievement("daily_challenge_6", "6-Tile Daily Pro", "Successfully complete the daily 6-tile challenge.", "🧩", GameTier.INTERMEDIATE),
            Achievement("daily_challenge_7", "7-Tile Daily Pro", "Successfully complete the daily 7-tile challenge.", "🧩", GameTier.INTERMEDIATE),
            Achievement("daily_challenge_8", "8-Tile Daily Pro", "Successfully complete the daily 8-tile challenge.", "⭐", GameTier.EXPERT),
            Achievement("daily_challenge_9", "9-Tile Daily Pro", "Successfully complete the daily 9-tile challenge.", "🔥", GameTier.EXPERT),
            Achievement("daily_challenge_10", "10-Tile Daily Pro", "Successfully complete the daily 10-tile challenge.", "👑", GameTier.EXPERT),
            Achievement("daily_master_badge", "Daily Namagram Master", "Conquered all 8 daily tile challenges (3–10) in a single day!", "🏆", GameTier.EXPERT)
        ).onEach {
            it.isUnlocked = prefs.getBoolean("${playerPrefix}_${it.id}", false)
        }
    }

    fun getUnlockedCount(): Int {
        return getAllAchievements().count { it.isUnlocked }
    }

    // --- DIAGNOSTIC TRIGGER AUDIT METHOD ---
    fun auditAllAchievementTriggers(): Map<String, Boolean> {
        val playerPrefix = getPlayerKey()
        return getAllAchievements().associate { achievement ->
            val storageKey = "${playerPrefix}_${achievement.id}"
            val canAccess = prefs.contains(storageKey) || achievement.id.isNotBlank()
            achievement.id to canAccess
        }
    }

    fun unlockAchievement(achievementId: String): Boolean {
        val playerPrefix = getPlayerKey()

        if (playerPrefix == "Player_Ignored") {
            return false
        }

        val achievement = getAllAchievements().find { it.id == achievementId } ?: return false

        // 👉 Note: Tier gate checks removed here so all achievements are visible and trackable roadmap items!

        val storageKey = "${playerPrefix}_$achievementId"
        val isAlreadyUnlocked = prefs.getBoolean(storageKey, false)

        if (!isAlreadyUnlocked) {
            if (pendingAchievementsThisTurn.isNotEmpty()) {
                if (!pendingAchievementsThisTurn.any { it.id == achievementId }) {
                    pendingAchievementsThisTurn.add(achievement)
                }
            } else {
                pendingAchievementsThisTurn.add(achievement)
                val handler = android.os.Handler(android.os.Looper.getMainLooper())
                handler.post {
                    if (pendingAchievementsThisTurn.size == 1) {
                        val single = pendingAchievementsThisTurn.first()
                        pendingAchievementsThisTurn.clear() // 👈 Corrected reference here
                        commitSingleAchievement(single)
                    }
                }
            }
            return true
        }
        return false
    }

    fun registerTileCompleted(tileCount: Int) {
        val playerPrefix = getPlayerKey()
        val key = "${playerPrefix}_completed_tile_$tileCount"
        prefs.edit { putBoolean(key, true) }

        val allDone = (3..10).all { prefs.getBoolean("${playerPrefix}_completed_tile_$it", false) }
        if (allDone) {
            unlockAchievement("jack_of_all_trades")
        }
    }

    fun trackExplorationAction(achievementId: String) {
        unlockAchievement(achievementId)
        checkExplorationCompletionist()
    }

    fun trackInspectHighScores() {
        val playerPrefix = getPlayerKey()
        val key = "${playerPrefix}_high_score_inspect_count"
        val currentCount = prefs.getInt(key, 0) + 1
        prefs.edit { putInt(key, currentCount) }

        if (currentCount >= 5) {
            unlockAchievement("expl_high_scores")
            checkExplorationCompletionist()
        }
    }

    fun trackNightOwlAndWeekend() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        if (hour in 22..23 || hour in 0..3) {
            unlockAchievement("expl_night_owl")
        }

        val playerPrefix = getPlayerKey()
        if (dayOfWeek == Calendar.SATURDAY) {
            prefs.edit { putBoolean("${playerPrefix}_logged_sat", true) }
        } else if (dayOfWeek == Calendar.SUNDAY) {
            if (prefs.getBoolean("${playerPrefix}_logged_sat", false)) {
                unlockAchievement("expl_weekend")
            }
        }
        checkExplorationCompletionist()
    }

    fun trackAppSettingsMenu(menuName: String) {
        val playerPrefix = getPlayerKey()
        val key = "${playerPrefix}_explored_menus"

        val savedSet = prefs.getStringSet(key, emptySet()) ?: emptySet()
        val exploredMenus = savedSet.toMutableSet()

        if (exploredMenus.add(menuName)) {
            prefs.edit { putStringSet(key, exploredMenus.toSet()) }
            if (exploredMenus.size >= 4) {
                unlockAchievement("expl_milestone")
            }
        }
        checkExplorationCompletionist()
    }

    private fun checkExplorationCompletionist() {
        val explorationIds = listOf(
            "expl_tutorial", "expl_high_scores", "expl_night_owl",
            "expl_weekend", "expl_profile", "expl_settings",
            "expl_rules", "expl_sessions", "expl_milestone"
        )
        val playerPrefix = getPlayerKey()
        val allDone = explorationIds.all { prefs.getBoolean("${playerPrefix}_$it", false) }
        if (allDone) {
            unlockAchievement("expl_completionist")
        }
    }

    fun registerGameCompletion(timeLimitSeconds: Int, isSuccess: Boolean, activeTileCount: Int = 4) {
        if (!isSuccess) return

        val playerPrefix = getPlayerKey()
        registerTileCompleted(activeTileCount)

        when (timeLimitSeconds) {
            90 -> {
                unlockAchievement("beat_easy")
                incrementAndCheckStreak("easy_mode_streak", "streak_easy_3")
            }
            60 -> {
                unlockAchievement("beat_medium")
                incrementAndCheckStreak("medium_mode_streak", "streak_medium_3")
            }
            30 -> {
                unlockAchievement("beat_hard")
                incrementAndCheckStreak("hard_mode_streak", "streak_hard_3")
                incrementAndCheckThreshold("${playerPrefix}_hard_wins", listOf("exp_hard_10" to 10, "exp_hard_50" to 50))
            }
        }

        incrementAndCheckThreshold("${playerPrefix}_total_rounds_completed", listOf(
            "beg_persistent" to 25, "beg_extra_10" to 30, "beg_extra_11" to 40,
            "beg_extra_12" to 50, "beg_extra_13" to 60, "beg_extra_14" to 70,
            "beg_extra_15" to 75, "inter_marathon" to 25, "inter_dedication" to 50,
            "inter_extra_10" to 30, "inter_extra_11" to 40, "inter_extra_12" to 50,
            "inter_extra_13" to 60, "inter_extra_14" to 70, "inter_master" to 75,
            "exp_marathon_100" to 100
        ))
    }

    fun checkMultiplayerOutcomeAchievements(
        wonAfterLosingFirst: Boolean = false,
        wonAllRounds: Boolean = false,
        isDraw: Boolean = false,
        didWin: Boolean = false,
        @Suppress("UNUSED_PARAMETER") vararg args: Any?
    ) {
        val playerPrefix = getPlayerKey()

        if (didWin || wonAllRounds || wonAfterLosingFirst) {
            unlockAchievement("win_multiplayer")
            unlockAchievement("local_multi_win1")

            incrementAndCheckThreshold("${playerPrefix}_multi_wins", listOf(
                "exp_multi_5" to 5, "exp_multi_15" to 15,
                "local_multi_4" to 10, "local_multi_10" to 20
            ))

            if (wonAfterLosingFirst) {
                unlockAchievement("comeback_king")
                unlockAchievement("local_multi_6")
            }
            if (wonAllRounds) {
                unlockAchievement("sweep_victory")
                unlockAchievement("local_multi_5")
            }
        }

        if (isDraw) {
            unlockAchievement("gracious_draw")
            unlockAchievement("local_multi_7")
        }

        incrementAndCheckThreshold("${playerPrefix}_multi_matches_played", listOf(
            "local_multi_first" to 1, "local_multi_2" to 5,
            "local_multi_3" to 10, "inter_multi_3" to 15
        ))
    }

    private fun incrementAndCheckStreak(streakKey: String, achievementId: String) {
        val playerPrefix = getPlayerKey()
        val storageKey = "${playerPrefix}_$streakKey"
        val currentStreak = prefs.getInt(storageKey, 0) + 1
        prefs.edit { putInt(storageKey, currentStreak) }

        if (currentStreak >= 3) {
            unlockAchievement(achievementId)
        }

        if (streakKey.contains("mode_streak") && currentStreak >= 10) {
            unlockAchievement("streak_10")
        }
    }

    private fun incrementAndCheckThreshold(counterKey: String, thresholds: List<Pair<String, Int>>, incrementBy: Int = 1) {
        val currentCount = prefs.getInt(counterKey, 0) + incrementBy
        prefs.edit { putInt(counterKey, currentCount) }

        for ((achievementId, targetValue) in thresholds) {
            if (currentCount >= targetValue) {
                unlockAchievement(achievementId)
            }
        }
    }

    fun checkTimingAchievements(currentSecondsRemaining: Int, totalTimeLimit: Int) {
        if (currentSecondsRemaining < 3) {
            unlockAchievement("buzzer_beater")
        }
        val elapsed = totalTimeLimit - currentSecondsRemaining
        if (elapsed < 10) {
            unlockAchievement("lightning_fast")
        }
    }

    fun checkAdvancedSentenceStructure(words: List<String>, rawSentence: String) {
        if (words.size <= 3) {
            unlockAchievement("short_and_sweet")
        }
        if (words.size >= 8) {
            unlockAchievement("novelist")
        }

        if (rawSentence.contains(",") && (rawSentence.endsWith(".") || rawSentence.endsWith("!") || rawSentence.endsWith("?"))) {
            unlockAchievement("punctuation_perfect")
        }
    }

    fun registerCumulativeMilestones(roundScore: Int) {
        val playerPrefix = getPlayerKey()
        val scoreKey = "${playerPrefix}_cumulative_score"
        val currentTotal = prefs.getInt(scoreKey, 0) + roundScore
        prefs.edit { putInt(scoreKey, currentTotal) }

        if (currentTotal >= 100) unlockAchievement("century_club")
        if (currentTotal >= 500) unlockAchievement("inter_500_club")
        if (currentTotal >= 1000) unlockAchievement("millennium_club")
        if (currentTotal >= 5000) unlockAchievement("exp_cumulative_5k")
        if (currentTotal >= 10000) unlockAchievement("exp_cumulative_10k")

        if (roundScore >= 50) unlockAchievement("single_round_50")
        if (roundScore >= 75) unlockAchievement("inter_score_75")
        if (roundScore >= 90) unlockAchievement("inter_score_90")
        if (roundScore >= 150) unlockAchievement("exp_score_150")
    }

    fun registerNineTileSuccess() {
        unlockAchievement("perfect_9_tiles")
    }

    fun checkTurnAchievements(
        tileCount: Int = 0,
        matchedCount: Int = 0,
        usedAllLetters: Boolean = false,
        isStrictInOrder: Boolean = false,
        hasDictionaryErrors: Boolean = false,
        @Suppress("UNUSED_PARAMETER") vararg args: Any?
    ) {
        val playerPrefix = getPlayerKey()

        if (tileCount == 3) {
            unlockAchievement("beg_perfect_3")
            incrementAndCheckThreshold("${playerPrefix}_trio_count", listOf("beg_trio_10" to 10, "beg_trio_50" to 50))
        } else if (tileCount == 4) {
            unlockAchievement("beg_perfect_4")
            incrementAndCheckThreshold("${playerPrefix}_quad_count", listOf("beg_quad_10" to 10, "beg_quad_50" to 50))
        }

        checkTileAndBonusMastery(tileCount, matchedCount, usedAllLetters, isStrictInOrder, hasDictionaryErrors)
    }

    fun checkSentenceCompositionAchievements(
        words: List<String> = emptyList(),
        @Suppress("UNUSED_PARAMETER") vararg args: Any?
    ) {
        if (words.isEmpty()) return

        val playerPrefix = getPlayerKey()

        if (words.size == 5) {
            unlockAchievement("inter_word_count_5")
        } else if (words.size == 6) {
            unlockAchievement("inter_word_count_6")
        }

        val vowels = setOf('a', 'e', 'i', 'o', 'u')
        if (words.isNotEmpty() && words.all { it.lowercase(Locale.ROOT).firstOrNull() in vowels }) {
            unlockAchievement("vowel_virtuoso")
        }

        val rareLetters = setOf('q', 'x', 'z', 'j', 'k')
        if (words.any { it.lowercase(Locale.ROOT).firstOrNull() in rareLetters }) {
            unlockAchievement("rare_gem")
        }

        val firstChar = words.first().lowercase(Locale.ROOT).firstOrNull()
        val lastChar = words.last().lowercase(Locale.ROOT).firstOrNull()
        if (firstChar != null && firstChar == lastChar) {
            unlockAchievement("symmetry_master")
        }

        val distinctInitials = words.mapNotNull { it.lowercase(Locale.ROOT).firstOrNull() }.toSet()
        if (distinctInitials.size >= 6) {
            unlockAchievement("alphabet_soup")
        }

        incrementAndCheckThreshold("${playerPrefix}_total_words_submitted", listOf(
            "beg_word_100" to 100,
            "beg_word_250" to 250,
            "inter_extra_19" to 100
        ), incrementBy = words.size)
    }

    fun checkTileAndBonusMastery(
        tileCount: Int = 0,
        matchedCount: Int = 0,
        usedAllLetters: Boolean = false,
        isStrictInOrder: Boolean = false,
        hasDictionaryErrors: Boolean = false,
        @Suppress("UNUSED_PARAMETER") vararg args: Any?
    ) {
        if (!hasDictionaryErrors) {
            unlockAchievement("clean_slate")
            if (tileCount in 5..7) {
                when (tileCount) {
                    5 -> unlockAchievement("inter_clean_5")
                    6 -> unlockAchievement("inter_clean_6")
                    7 -> unlockAchievement("inter_clean_7")
                }
            }
        }

        when (tileCount) {
            5 -> {
                unlockAchievement("perfect_5_tiles")
                if (usedAllLetters && isStrictInOrder) unlockAchievement("inter_jackpot_5")
            }
            6 -> {
                unlockAchievement("perfect_6_tiles")
                if (usedAllLetters && isStrictInOrder) unlockAchievement("inter_jackpot_6")
            }
            7 -> {
                unlockAchievement("perfect_7_tiles")
                if (usedAllLetters && isStrictInOrder) unlockAchievement("inter_jackpot_7")
            }
            8 -> unlockAchievement("perfect_8_tiles")
            9 -> unlockAchievement("perfect_9_tiles")
            10 -> unlockAchievement("perfect_10_tiles")
        }

        if (usedAllLetters && isStrictInOrder && matchedCount >= tileCount) {
            unlockAchievement("flawless_masterpiece")
        }
    }

    fun checkAlliterationAndStyle(
        words: List<String>,
        @Suppress("UNUSED_PARAMETER") vararg args: Any?
    ) {
        if (words.size >= 3) {
            for (i in 0..words.size - 3) {
                val c1 = words[i].firstOrNull()?.lowercaseChar()
                val c2 = words[i + 1].firstOrNull()?.lowercaseChar()
                val c3 = words[i + 2].firstOrNull()?.lowercaseChar()
                if (c1 != null && c1 == c2 && c1 == c3) {
                    unlockAchievement("art_of_alliteration")
                    break
                }
            }
        }
    }

    fun checkScoreAndAIAchievements(
        roundScore: Int = 0,
        @Suppress("UNUSED_PARAMETER") cumulativeScore: Int = 0,
        isPerfectAI: Boolean = false,
        @Suppress("UNUSED_PARAMETER") isNewRecord: Boolean = false,
        @Suppress("UNUSED_PARAMETER") vararg args: Any?
    ) {
        if (isPerfectAI) {
            unlockAchievement("ai_whisperer")
        }
        if (roundScore >= 50) {
            unlockAchievement("single_round_50")
        }
        if (roundScore >= 75) {
            unlockAchievement("inter_score_75")
        }
        if (roundScore >= 90) {
            unlockAchievement("inter_score_90")
        }
        if (roundScore >= 150) {
            unlockAchievement("exp_score_150")
        }
    }

    fun processPendingAchievements(onComplete: () -> Unit = {}) {
        val pendingList = pendingAchievementsThisTurn.toList()
        pendingAchievementsThisTurn.clear()

        if (pendingList.isEmpty()) {
            onComplete()
            return
        }

        if (pendingList.size == 1) {
            commitSingleAchievement(pendingList.first())
            onComplete()
        } else {
            val itemDisplayTexts = pendingList.map {
                "${it.iconEmoji} ${it.title} - ${it.description}"
            }.toTypedArray()

            var selectedIndex = 0

            AlertDialog.Builder(context)
                .setTitle("🏆 Choose 1 Achievement (${pendingList.size} Unlocked!)")
                .setSingleChoiceItems(itemDisplayTexts, 0) { _, which ->
                    selectedIndex = which
                }
                .setCancelable(false)
                .setPositiveButton("Claim Selected") { dialog, _ ->
                    val chosen = pendingList[selectedIndex]
                    commitSingleAchievement(chosen)
                    dialog.dismiss()
                    onComplete()
                }
                .show()
        }
    }

    fun checkFailedAttemptAchievements(
        @Suppress("UNUSED_PARAMETER") vararg args: Any?
    ) {
        unlockAchievement("spelling_disaster")
        unlockAchievement("ai_rejected")
    }

    fun checkAdvancedTimingAchievements(
        currentSecondsRemaining: Int,
        totalTimeLimit: Int,
        @Suppress("UNUSED_PARAMETER") vararg args: Any?
    ) {
        checkTimingAchievements(currentSecondsRemaining, totalTimeLimit)
        if (currentSecondsRemaining <= 2) {
            unlockAchievement("inter_clutch")
        }
    }

    private fun commitSingleAchievement(achievement: Achievement) {
        val playerPrefix = getPlayerKey()
        val storageKey = "${playerPrefix}_${achievement.id}"
        prefs.edit { putBoolean(storageKey, true) }

        Toast.makeText(
            context,
            "🏆 ACHIEVEMENT UNLOCKED!\n${achievement.iconEmoji} ${achievement.title}",
            Toast.LENGTH_LONG
        ).show()
    }
}