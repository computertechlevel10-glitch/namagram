package com.elothar.namagram

import android.content.Context
import androidx.core.content.edit

object DifficultySettings {
    private const val PREFS_NAME = "namagram_difficulty_cache"
    private const val KEY_TIME_LIMIT = "single_player_time_limit"

    /**
     * Persists the selected single-player countdown limit to disk using clean KTX extensions.
     */
    fun saveTimeLimit(context: Context, seconds: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putInt(KEY_TIME_LIMIT, seconds)
        }
    }

    /**
     * Reads the saved single-player time limit from storage.
     * Defaults to 60 seconds (Medium) if no choice has been saved yet.
     */
    fun getTimeLimit(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_TIME_LIMIT, 60)
    }
}
