package com.elothar.namagram

@Suppress("unused")
object GameSettings {
    enum class Difficulty { EASY, MEDIUM, HARD }

    var currentDifficulty: Difficulty = Difficulty.MEDIUM
    var totalRounds: Int = 1 // Options: 1, 3, or 5
    var currentRound: Int = 1

    // Returns time in milliseconds based on chosen difficulty
    fun getTimerDuration(): Long {
        return when (currentDifficulty) {
            Difficulty.EASY -> 90000L    // 90 seconds
            Difficulty.MEDIUM -> 60000L  // 60 seconds
            Difficulty.HARD -> 30000L    // 30 seconds
        }
    }
}
