package com.elothar.namagram

import android.content.Context
import androidx.core.content.edit
import java.util.ArrayList

object MatchSettings {

    var totalRoundsSelected: Int = 3
    var roundTimeLimitSeconds: Int = 60
    var currentRoundNumber: Int = 1
    var isPlayer1Turn: Boolean = true
    var savedRoundLetters: String = ""

    // Changed default from "Player 1" to "Enter your nickname"
    var player1Nickname: String = "Enter your nickname"
    var player1TotalScore: Int = 0
    val player1RoundScoresList = ArrayList<Int>()

    var player2Nickname: String = "Player 2"
    var player2TotalScore: Int = 0
    val player2RoundScoresList = ArrayList<Int>()

    // Flexible layout tile bounds tracker & player custom selection
    var totalLettersToDraw: Int = 10
    var selectedTileCount: Int = 4

    // 👉 Global helper to sync profile changes across all screens and persistent storage
    fun updatePlayerProfile(context: Context, newName: String) {
        val trimmed = newName.trim()
        player1Nickname = trimmed

        val prefs = context.getSharedPreferences("NamagramPrefs", Context.MODE_PRIVATE)
        prefs.edit {
            putString("player1_nickname", trimmed)
        }
    }

    @Suppress("unused")
    fun flushMatchEngineState(p1SavedName: String) {
        totalRoundsSelected = 3
        roundTimeLimitSeconds = 60
        currentRoundNumber = 1
        isPlayer1Turn = true
        savedRoundLetters = ""

        player1Nickname = p1SavedName // Retains your persistent profile username!
        player1TotalScore = 0
        player1RoundScoresList.clear()

        player2Nickname = "Player 2"
        player2TotalScore = 0
        player2RoundScoresList.clear()
        selectedTileCount = 4
    }

    fun setupSinglePlayerEngine(p1SavedName: String) {
        totalRoundsSelected = 0
        currentRoundNumber = 1
        isPlayer1Turn = true
        savedRoundLetters = ""

        player1Nickname = p1SavedName // Retains your persistent profile username!
        player1TotalScore = 0
        player1RoundScoresList.clear()
    }
}