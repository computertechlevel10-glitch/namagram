package com.elothar.namagram

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

class ScoreManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("namagram_scores", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SOLO_SCORES_LIST = "solo_scores_list_json"
        private const val KEY_MULTIPLAYER_SCORES_LIST = "multiplayer_scores_list_json"
        private const val KEY_CUMULATIVE_SCORE = "cumulative_total_score" // 👉 Added key for lifetime total
        private const val DEFAULT_NICKNAME = "Player"
        private const val MAX_LEADERBOARD_SIZE = 5
    }

    data class ScoreEntry(val name: String, val score: Int)

    // Solo High Scores (Top 5, nickname filtered) -> Returns true if new high score recorded
    fun updateSoloHighScoreIfNeeded(nickname: String, score: Int): Boolean {
        // 👉 Respects your rule: skips default or blank nicknames
        if (nickname.isBlank() || nickname.equals(DEFAULT_NICKNAME, ignoreCase = true) || score <= 0) {
            return false
        }

        // 👉 Automatically add to cumulative lifetime score whenever a valid score is earned
        addCumulativeScore(score)

        val currentList = getSoloHighScores().toMutableList()
        currentList.add(ScoreEntry(nickname, score))
        // Sort descending by score and keep top 5
        val sortedList = currentList.sortedByDescending { it.score }.take(MAX_LEADERBOARD_SIZE)
        saveScoresList(KEY_SOLO_SCORES_LIST, sortedList)

        return sortedList.any { it.name == nickname && it.score == score }
    }

    fun getSoloHighScores(): List<ScoreEntry> {
        return loadScoresList(KEY_SOLO_SCORES_LIST)
    }

    // Multiplayer High Scores (Top 5) -> Returns true if new high score recorded
    fun updateMultiplayerHighScoreIfNeeded(winnerName: String, totalScore: Int): Boolean {
        // Handle ties gracefully by recording them or fallback name instead of discarding
        val normalizedWinner = if (winnerName.isBlank() || winnerName.equals(DEFAULT_NICKNAME, ignoreCase = true)) {
            "Player"
        } else {
            winnerName
        }

        // If you want to allow 0-score games, change <= 0 to < 0.
        // If ties should be logged, we remove the "It's a tie!" rejection check.
        if (totalScore < 0) {
            return false
        }

        val currentList = getMultiplayerHighScores().toMutableList()
        currentList.add(ScoreEntry(normalizedWinner, totalScore))
        val sortedList = currentList.sortedByDescending { it.score }.take(MAX_LEADERBOARD_SIZE)
        saveScoresList(KEY_MULTIPLAYER_SCORES_LIST, sortedList)

        return sortedList.any { it.name == normalizedWinner && it.score == totalScore }
    }

    fun getMultiplayerHighScores(): List<ScoreEntry> {
        return loadScoresList(KEY_MULTIPLAYER_SCORES_LIST)
    }

    // 👉 Cumulative Lifetime Score Methods
    fun addCumulativeScore(points: Int) {
        if (points <= 0) return
        val currentTotal = getCumulativeScore()
        prefs.edit {
            putInt(KEY_CUMULATIVE_SCORE, currentTotal + points)
        }
    }

    fun getCumulativeScore(): Int {
        return prefs.getInt(KEY_CUMULATIVE_SCORE, 0)
    }

    private fun saveScoresList(key: String, list: List<ScoreEntry>) {
        val jsonArray = JSONArray()
        for ((name, score) in list) {
            val jsonObject = JSONObject().apply {
                put("name", name)
                put("score", score)
            }
            jsonArray.put(jsonObject)
        }
        prefs.edit {
            putString(key, jsonArray.toString())
        }
    }

    private fun loadScoresList(key: String): List<ScoreEntry> {
        val jsonString = prefs.getString(key, null) ?: return emptyList()
        val list = mutableListOf<ScoreEntry>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(ScoreEntry(obj.getString("name"), obj.getInt("score")))
            }
        } catch (_: Exception) { }
        return list
    }

    // Clear only solo scores
    fun clearSoloScores() {
        prefs.edit {
            remove(KEY_SOLO_SCORES_LIST)
        }
    }

    // Clear only multiplayer scores
    fun clearMultiplayerScores() {
        prefs.edit {
            remove(KEY_MULTIPLAYER_SCORES_LIST)
        }
    }
}