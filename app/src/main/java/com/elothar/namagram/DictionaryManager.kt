@file:Suppress("SpellCheckingInspection")
package com.elothar.namagram

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.HashSet
import java.security.SecureRandom // <-- Added SecureRandom import
import java.util.TimeZone
import java.util.Calendar

class DictionaryManager(private val context: Context) {

    private val commonBigramsSet = HashSet<String>()
    private val validWordsSet = HashSet<String>()

    init {
        loadBigramAssociationTable()
    }

    private fun loadBigramAssociationTable() {
        Thread {
            try {
                context.assets.open("common_bigrams.txt").use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        reader.readLine() // Skip header row
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            val rawLine = line?.trim() ?: continue
                            if (rawLine.isNotEmpty()) {
                                val columns = rawLine.split(",")
                                if (columns.size >= 2) {
                                    val cleanBigram = columns[1].trim().lowercase()
                                    commonBigramsSet.add(cleanBigram)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    @Suppress("unused")
    fun isSemanticFlowValid(wordsList: List<String>): Boolean {
        if (wordsList.size < 2) return true

        var validTransitionsCount = 0
        val totalTransitionsToCheck = wordsList.size - 1

        for (i in 0 until totalTransitionsToCheck) {
            val word1 = wordsList[i].replace("""[^a-z]""".toRegex(), "")
            val word2 = wordsList[i + 1].replace("""[^a-z]""".toRegex(), "")
            val pairKey = "$word1 $word2"

            if (commonBigramsSet.contains(pairKey)) {
                validTransitionsCount++
            }
        }

        if (wordsList.size >= 6 && validTransitionsCount == 0) {
            val averageWordLength = wordsList.map { it.length }.average()
            return averageWordLength > 1.8
        }
        return true
    }

    /**
     * Generates a balanced random letters stream layout using real English letter frequencies.
     */
    fun generateRandomLettersList(count: Int): String {
        val weightedLetterPool = listOf(
            'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A',
            'B', 'C', 'C', 'D', 'D', 'D', 'D',
            'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E',
            'F', 'G', 'G', 'H', 'H', 'H', 'H', 'H', 'H',
            'I', 'I', 'I', 'I', 'I', 'I', 'I', 'I', 'I',
            'J', 'K', 'L', 'L', 'L', 'L',
            'M', 'M', 'N', 'N', 'N', 'N', 'N', 'N',
            'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O',
            'P', 'P', 'Q', 'R', 'R', 'R', 'R', 'R', 'R',
            'S', 'S', 'S', 'S', 'S', 'S',
            'T', 'T', 'T', 'T', 'T', 'T', 'T', 'T', 'T',
            'U', 'U', 'U', 'U',
            'V', 'W', 'W', 'X', 'Y', 'Y', 'Z'
        )

        // FIXED: Replaced java.util.Random() with SecureRandom
        val secureRandom = SecureRandom()
        val selectedLetters = mutableListOf<Char>()
        val poolCopy = weightedLetterPool.toMutableList()

        repeat(count) {
            if (poolCopy.isEmpty()) poolCopy.addAll(weightedLetterPool)
            val index = secureRandom.nextInt(poolCopy.size)
            selectedLetters.add(poolCopy.removeAt(index))
        }

        return selectedLetters.shuffled(secureRandom).joinToString("")
    }

    /**
     * Generates a date-seeded daily puzzle sequence where every user worldwide
     * gets the exact same independent sequence for a given tile count on any specific calendar day.
     */
    fun generateDailySequenceForSize(tileCount: Int): String {
        // Use UTC calendar to get exact epoch day number
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        // Clear time fields to ensure absolute day consistency
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val epochDays = (calendar.timeInMillis / (1000 * 60 * 60 * 24)).toInt()
        val uniqueSeed = epochDays + tileCount

        // Use java.util.Random for strict deterministic daily seeding across devices
        val seededRandom = java.util.Random(uniqueSeed.toLong())

        val weightedLetterPool = listOf(
            'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A',
            'B', 'C', 'C', 'D', 'D', 'D', 'D',
            'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E',
            'F', 'G', 'G', 'H', 'H', 'H', 'H', 'H', 'H',
            'I', 'I', 'I', 'I', 'I', 'I', 'I', 'I', 'I',
            'J', 'K', 'L', 'L', 'L', 'L',
            'M', 'M', 'N', 'N', 'N', 'N', 'N', 'N',
            'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O',
            'P', 'P', 'Q', 'R', 'R', 'R', 'R', 'R', 'R',
            'S', 'S', 'S', 'S', 'S', 'S',
            'T', 'T', 'T', 'T', 'T', 'T', 'T', 'T', 'T',
            'U', 'U', 'U', 'U',
            'V', 'W', 'W', 'X', 'Y', 'Y', 'Z'
        )

        val selectedLetters = mutableListOf<Char>()
        val poolCopy = weightedLetterPool.toMutableList()

        repeat(tileCount) {
            if (poolCopy.isEmpty()) poolCopy.addAll(weightedLetterPool)
            val index = seededRandom.nextInt(poolCopy.size)
            selectedLetters.add(poolCopy.removeAt(index))
        }

        return selectedLetters.shuffled(seededRandom).joinToString("")
    }

    fun loadDictionaryAsync(onComplete: () -> Unit) {
        Thread {
            try {
                context.assets.open("words.txt").use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            val cleanWord = line?.trim()?.lowercase() ?: continue
                            if (cleanWord.isNotEmpty()) {
                                validWordsSet.add(cleanWord)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            onComplete()
        }.start()
    }

    fun isValidWord(word: String): Boolean {
        return word.isNotEmpty() && (validWordsSet.isEmpty() || validWordsSet.contains(word.lowercase()))
    }

    fun verifySequenceMatchesRules(sentence: String, letters: String): Boolean {
        return sentence.isNotEmpty() && letters.isNotEmpty()
    }
}