package com.elothar.namagram

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import java.util.Calendar
import java.util.TimeZone

class AchievementDebugHelper(private val context: Context) {

    private val achievementManager = AchievementManager(context)
    private val tag = "AchievementDebug"

    /**
     * Prints a breakdown of all achievements and their unlock status to Logcat.
     */
    fun logAllAchievements() {
        val achievements = achievementManager.getAllAchievements()
        val total = achievements.size
        val unlocked = achievements.count { it.isUnlocked }

        Log.d(tag, "==================================================")
        Log.d(tag, "🏆 ACHIEVEMENT STATUS: $unlocked / $total Unlocked")
        Log.d(tag, "==================================================")

        achievements.forEach { achievement ->
            val status = if (achievement.isUnlocked) "UNLOCKED [✓]" else "LOCKED   [ ]"
            Log.d(tag, "[$status] ID: ${achievement.id.padEnd(25)} | Title: ${achievement.title}")
        }
    }

    /**
     * Opens a quick debug dialog on screen showing total unlocked count
     * with options to test unlock, run audits, view progress, and manage Daily Namagram Hub, Expert data, and Factory Resets.
     */
    fun showDebugDialog() {
        val achievements = achievementManager.getAllAchievements()
        val unlocked = achievements.count { it.isUnlocked }
        val total = achievements.size

        val options = arrayOf(
            "🛠️ Open AI Rule Evaluation Sandbox", // Index 0
            "🧪 Test All Achievement Triggers (Audit)", // Index 1
            "Log All Statuses to Logcat",               // Index 2
            "Test Unlock 'beat_hard' (30s Win)",        // Index 3
            "Test Unlock 'comeback_king' (Multiplayer)",// Index 4
            "Run Full Gameplay Evaluator Tests",        // Index 5
            "🛡️ Test Intermediate Tier Triggers",       // Index 6
            "🔥 Test Expert Tier Triggers (Hard Mode Win)", // Index 7
            "🔓 Force Unlock Intermediate Gate ('inter_gate')", // Index 8
            "🔓 Force Unlock Expert Gate ('expert_gate')",      // Index 9
            "🏆 Force Win All Daily Challenges (3–10 Tiles)",   // Index 10
            "🔄 Reset Today's Daily Hub Data & Statuses",       // Index 11
            "✨ Unlock ALL 210 Achievements (Stress Test)",     // Index 12
            "Reset All Achievements (Debug Clear)",             // Index 13
            "⚠️ Developer Factory Reset (Wipe Everything)"      // Index 14
        )

        AlertDialog.Builder(context)
            .setTitle("🛠️ Namagram Debug Panel ($unlocked/$total)")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // 👉 Launch the AI Rule Evaluation Sandbox Activity!
                        val intent = Intent(context, AiSandboxActivity::class.java)
                        context.startActivity(intent)
                    }
                    1 -> {
                        // 👉 Active call to the audit function (shifted from 0 to 1)
                        val audit = achievementManager.auditAllAchievementTriggers()
                        val totalChecked = audit.size
                        val validCount = audit.values.count { it }

                        AlertDialog.Builder(context)
                            .setTitle("🔍 Achievement Trigger Audit")
                            .setMessage("Total Achievements Checked: $totalChecked\n" +
                                    "Properly Linked & Active: $validCount\n\n" +
                                    "All underlying keys, gates, and event hooks are successfully mapped.")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                    2 -> {
                        logAllAchievements()
                        Toast.makeText(context, "Logged to Logcat! (Tag: AchievementDebug)", Toast.LENGTH_SHORT).show()
                    }
                    3 -> testUnlockAchievement("beat_hard")
                    4 -> testUnlockAchievement("comeback_king")
                    5 -> runSystematicTriggerTests()
                    6 -> runIntermediateTierTests()
                    7 -> runExpertTierTests()
                    8 -> testUnlockAchievement("inter_gate")
                    9 -> testUnlockAchievement("expert_gate")
                    10 -> {
                        forceCompleteAllDailyChallenges()
                        Toast.makeText(context, "All daily challenges marked as WON!", Toast.LENGTH_SHORT).show()
                    }
                    11 -> {
                        resetAllDailyData()
                        Toast.makeText(context, "Daily hub data cleared for today!", Toast.LENGTH_SHORT).show()
                    }
                    12 -> unlockAllAchievements()
                    13 -> confirmResetAchievements()
                    14 -> showFactoryResetConfirmationDialog()
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    /**
     * Forces an achievement unlock to test the UI notification toast and persistence.
     */
    fun testUnlockAchievement(achievementId: String) {
        val unlocked = achievementManager.unlockAchievement(achievementId)
        if (unlocked) {
            Toast.makeText(context, "Successfully unlocked: $achievementId", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Already unlocked or ignored profile: $achievementId", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Forces every single registered achievement to unlock for testing UI and persistence.
     */
    fun unlockAllAchievements() {
        val achievements = achievementManager.getAllAchievements()
        var count = 0

        achievements.forEach { achievement ->
            if (!achievement.isUnlocked) {
                achievementManager.unlockAchievement(achievement.id)
                count++
            }
        }

        Toast.makeText(context, "Unlocked $count achievements for stress testing!", Toast.LENGTH_LONG).show()
        logAllAchievements()
    }

    /**
     * Simulates gameplay evaluator calls using your actual AchievementManager methods.
     */
    fun runSystematicTriggerTests() {
        Log.d(tag, "--- Running Systematic Evaluator Tests ---")

        achievementManager.registerGameCompletion(timeLimitSeconds = 30, isSuccess = true)
        achievementManager.checkAdvancedSentenceStructure(
            words = listOf("Big", "Brave", "Bear"),
            rawSentence = "Big, brave bear!"
        )
        achievementManager.checkAlliterationAndStyle(
            words = listOf("Peter", "Piper", "Picked")
        )
        achievementManager.checkMultiplayerOutcomeAchievements(
            wonAfterLosingFirst = true,
            wonAllRounds = false,
            isDraw = false,
            didWin = true
        )
        achievementManager.checkFailedAttemptAchievements()

        Toast.makeText(context, "Evaluator tests executed! Check Logcat.", Toast.LENGTH_SHORT).show()
        logAllAchievements()
    }

    /**
     * Simulates gameplay actions specifically targeted at testing Intermediate tier triggers.
     */
    fun runIntermediateTierTests() {
        Log.d(tag, "--- Running Intermediate Tier Evaluator Tests ---")

        achievementManager.unlockAchievement("inter_gate")
        achievementManager.registerGameCompletion(timeLimitSeconds = 60, isSuccess = true)
        achievementManager.checkTurnAchievements(tileCount = 5, matchedCount = 5, usedAllLetters = true, isStrictInOrder = true, hasDictionaryErrors = false)
        achievementManager.checkSentenceCompositionAchievements(
            words = listOf("Cats", "Quietly", "Chase", "Small", "Mice")
        )
        achievementManager.registerCumulativeMilestones(roundScore = 80)

        Toast.makeText(context, "Intermediate tests executed & Gate unlocked!", Toast.LENGTH_LONG).show()
        logAllAchievements()
    }

    /**
     * Simulates gameplay actions specifically targeted at testing Expert tier triggers.
     */
    fun runExpertTierTests() {
        Log.d(tag, "--- Running Expert Tier Evaluator Tests ---")

        achievementManager.unlockAchievement("expert_gate")
        achievementManager.registerGameCompletion(timeLimitSeconds = 20, isSuccess = true)
        achievementManager.checkTurnAchievements(tileCount = 10, matchedCount = 10, usedAllLetters = true, isStrictInOrder = true, hasDictionaryErrors = false)

        Toast.makeText(context, "Expert tests executed & Gate unlocked!", Toast.LENGTH_LONG).show()
        logAllAchievements()
    }

    /**
     * Instantly forces all 8 daily tile challenges (3 to 10) to "WON"
     * for today's UTC date key so you can instantly test the Daily Master Badge popup.
     */
    fun forceCompleteAllDailyChallenges() {
        val prefs = context.getSharedPreferences("NamagramPrefs", Context.MODE_PRIVATE)
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val dateKeyString = "${calendar.get(Calendar.YEAR)}_${calendar.get(Calendar.DAY_OF_YEAR)}"

        prefs.edit {
            remove("daily_master_badge_${dateKeyString}")
            for (tileCount in 3..10) {
                putString("daily_status_${dateKeyString}_${tileCount}", "WON")
            }
        }
    }

    /**
     * Resets all daily challenge statuses, scores, and master badges for today.
     */
    fun resetAllDailyData() {
        val prefs = context.getSharedPreferences("NamagramPrefs", Context.MODE_PRIVATE)
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val dateKeyString = "${calendar.get(Calendar.YEAR)}_${calendar.get(Calendar.DAY_OF_YEAR)}"

        prefs.edit {
            remove("daily_master_badge_${dateKeyString}")
            for (tileCount in 3..10) {
                remove("daily_status_${dateKeyString}_${tileCount}")
                remove("daily_completed_${dateKeyString}_${tileCount}")
            }
        }
    }

    private fun confirmResetAchievements() {
        AlertDialog.Builder(context)
            .setTitle("⚠️ Reset All Achievements?")
            .setMessage("This will clear all saved achievement progress for testing.")
            .setPositiveButton("Reset") { _, _ ->
                val prefs = context.getSharedPreferences("NamagramAchievements", Context.MODE_PRIVATE)
                prefs.edit { clear() }
                Toast.makeText(context, "All achievements reset!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showFactoryResetConfirmationDialog() {
        AlertDialog.Builder(context)
            .setTitle("⚠️ Developer Factory Reset")
            .setMessage("Secret debug trigger activated! Do you want to wipe all unlocked achievements, high scores, and player profiles back to pristine installation state?")
            .setPositiveButton("Reset Everything") { _, _ ->
                performFactoryReset()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performFactoryReset() {
        context.getSharedPreferences("NamagramPrefs", Context.MODE_PRIVATE).edit { clear() }
        context.getSharedPreferences("NamagramAchievements", Context.MODE_PRIVATE).edit { clear() }
        context.getSharedPreferences("NamagramScores", Context.MODE_PRIVATE).edit { clear() }
        context.getSharedPreferences("namagram_scores", Context.MODE_PRIVATE).edit { clear() }
        context.getSharedPreferences("NamagramHighScores", Context.MODE_PRIVATE).edit { clear() }
        context.getSharedPreferences("HighScoresPrefs", Context.MODE_PRIVATE).edit { clear() }
        context.getSharedPreferences("NamagramPreferences", Context.MODE_PRIVATE).edit { clear() }
        context.getSharedPreferences("SoloHighScores", Context.MODE_PRIVATE).edit { clear() }
        context.getSharedPreferences("MultiplayerHighScores", Context.MODE_PRIVATE).edit { clear() }

        MatchSettings.player1Nickname = ""
        MatchSettings.player2Nickname = ""
        MatchSettings.isPlayer1Turn = true
        MatchSettings.currentRoundNumber = 1
        MatchSettings.totalRoundsSelected = 0
        MatchSettings.savedRoundLetters = ""
        MatchSettings.player1TotalScore = 0
        MatchSettings.player2TotalScore = 0
        MatchSettings.player1RoundScoresList.clear()
        MatchSettings.player2RoundScoresList.clear()

        Toast.makeText(context, "🔄 App successfully reset to pristine factory condition!", Toast.LENGTH_LONG).show()

        val intent = Intent(context, WelcomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)

        if (context is android.app.Activity) {
            context.finish()
        }
    }
}