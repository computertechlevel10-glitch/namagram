package com.elothar.namagram

data class EvaluationResult(
    val isValid: Boolean,
    val score: Int,
    val reason: String,
    val ruleChecks: RuleChecks
)

data class RuleChecks(
    val makesSense: Boolean,              // Rule 1
    val properNameCountValid: Boolean,     // Rule 2 (Max 1 proper name)
    val singleComma: Boolean,              // Rule 3 (Only 1 comma)
    val noAbuttingSentences: Boolean,      // Rule 4 (No full stops to abut two sentences)
    val wordRepetitionValid: Boolean,      // Rule 5 (Word used max twice)
    val correctTense: Boolean,             // Rule 6 (Same tense throughout)
    val noSlang: Boolean,                  // Rule 7 (No slang or swear words)
    val brandNameValid: Boolean,           // Rule 8 (Brand names count as valid non-proper names)
    val invertedCommasValid: Boolean       // Rule 9 (Inverted commas allowed if called for)
)