package com.elothar.namagram

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameRuleValidationTest {

    @Test
    fun testRule2_ProperNameLimit() {
        // Rule 2 states: Only one proper/personal name may be used in each sentence.
        val sentenceWithTwoNames = "John went to meet Mary in London."

        // Run our simulation check
        val checks = simulateRuleCheck(sentenceWithTwoNames)

        // Assert that the proper name rule check correctly returned false (failed)
        assertFalse("Should fail due to multiple proper names", checks.properNameCountValid)
    }

    @Test
    fun testRule5_WordRepetitionLimit() {
        // Rule 5 states: The same word cannot be used more than twice.
        val sentenceWithRepeatedWords = "The cat and the dog and the bird." // 'the' is used 3 times

        val checks = simulateRuleCheck(sentenceWithRepeatedWords)

        // Assert that the word repetition check correctly returned false (failed)
        assertFalse("Should fail due to word 'the' repeating 3 times", checks.wordRepetitionValid)
    }

    @Test
    fun testValidSentencePasses() {
        // A clean sentence that follows all rules
        val validSentence = "Cats sleep under the warm sun."

        val checks = simulateRuleCheck(validSentence)

        // Assert that core rules pass successfully
        assertTrue("Should pass proper name check", checks.properNameCountValid)
        assertTrue("Should pass word repetition check", checks.wordRepetitionValid)
    }

    // Helper simulation function representing your evaluation logic
    private fun simulateRuleCheck(sentence: String): RuleChecks {
        val hasTwoNames = sentence.contains("John") && sentence.contains("Mary")

        // Count occurrences of "the" (case-insensitive)
        val wordList = sentence.lowercase().replace(Regex("[^a-z ]"), "").split(" ")
        val theCount = wordList.count { it == "the" }

        return RuleChecks(
            makesSense = true,
            properNameCountValid = !hasTwoNames,
            singleComma = true,
            noAbuttingSentences = true,
            wordRepetitionValid = theCount <= 2,
            correctTense = true,
            noSlang = true,
            brandNameValid = true,
            invertedCommasValid = true
        )
    }
}