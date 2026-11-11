package com.elothar.namagram

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class UnifiedAIEvaluator(
    private val geminiKey: String = "",
    private val groqKey: String = "",
    private val openRouterKey: String = "",
    private val isDebug: Boolean = true
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .writeTimeout(6, TimeUnit.SECONDS)
        .build()

    private val tag = "UnifiedAIEvaluator"

    /**
     * Hybrid Evaluation Pipeline:
     * 1. Local Deterministic Checks: Character whitelisting, comma limits, sentence terminator guard,
     *    word frequency limits (max 2), and tile pool starting-letter validation.
     * 2. AI Fallback Chain (Gemini ➔ Groq ➔ OpenRouter): Validates semantics, grammar, tense consistency,
     *    proper/brand name limits, and real-world logic.
     */
    suspend fun evaluateSentence(sentence: String, targetLetters: List<String>): String =
        withContext(Dispatchers.IO) {
            val cleanSentence = sentence.trim()
            if (cleanSentence.isEmpty()) {
                return@withContext "Rejected: Sentence is empty."
            }

            // ── STEP 1: LOCAL KOTLIN DETERMINISTIC CHECKS ──

            // Rule Check: Whitelist special characters (allow letters, spaces, commas, apostrophes, quotes, and ending punctuation)
            if (!cleanSentence.matches(Regex("^[a-zA-Z\\s,'\".!?-]+$"))) {
                return@withContext "Rejected: Sentence contains unauthorized special characters, numbers, or symbols."
            }

            // Rule Check: Only one comma allowed
            val commaCount = cleanSentence.count { it == ',' }
            if (commaCount > 1) {
                return@withContext "Rejected: Too many commas. Only one comma is permitted per sentence."
            }

            // Rule Check: Sentence Terminator Guard (must have only one terminal punctuation mark at the very end)
            val internalPeriods = cleanSentence.dropLast(1).count { it == '.' || it == '!' || it == '?' }
            if (internalPeriods > 0 || !cleanSentence.matches(Regex(".*[.!?]$"))) {
                return@withContext "Rejected: Invalid sentence termination or multiple sentences detected."
            }

            // Extract words cleanly for frequency and tile validation
            val words = cleanSentence.replace(Regex("[\"',.!?]"), "").lowercase().split(Regex("\\s+")).filter { it.isNotEmpty() }
            if (words.isEmpty()) {
                return@withContext "Rejected: No valid words found."
            }

            // Rule Check: The same word cannot be used more than twice
            val wordCounts = words.groupingBy { it }.eachCount()
            for ((word, count) in wordCounts) {
                if (count > 2) {
                    return@withContext "Rejected: The word '$word' is used $count times. No word can be used more than twice."
                }
            }

            // Rule Check: Words must start with available sequence letters from the pool
            val availablePool = targetLetters.map { it.uppercase() }.toMutableList()
            for (word in words) {
                val firstLetter = word.take(1).uppercase()
                val indexInPool = availablePool.indexOf(firstLetter)
                if (indexInPool != -1) {
                    availablePool.removeAt(indexInPool)
                } else {
                    return@withContext "Rejected: Word '$word' starts with '$firstLetter', which is not available in your letter pool."
                }
            }

            // ── STEP 2: AI GAME RULE & LOGIC CHECK ──
            val prompt = """
                Evaluate this sentence based on these specific guidelines:
                1. Does the sentence make proper grammatical English and logical real-world sense without direct contradictions or slang gibberish?
                2. Does it use AT MOST ONE proper or personal name? (Note: Brand names and proper names are completely optional; having zero or one proper/personal name is fully permitted).
                3. Is the verb tense consistent throughout the entire sentence?
                4. Are pronouns used clearly without ambiguous overload?
                
                Sentence: "$cleanSentence"
            """.trimIndent()

            // 1. Attempt Gemini First
            try {
                return@withContext callGemini(geminiKey, prompt)
            } catch (geminiError: Exception) {
                val geminiMsg = geminiError.message ?: ""
                if (isDebug) Log.w(tag, "Gemini failed ($geminiMsg). Falling back to Groq...")

                // 2. Attempt Groq Fallback
                try {
                    return@withContext callGroq(groqKey, prompt)
                } catch (groqError: Exception) {
                    val groqMsg = groqError.message ?: ""
                    if (isDebug) Log.w(tag, "Groq failed ($groqMsg). Falling back to OpenRouter...")

                    // 3. Attempt OpenRouter Fallback
                    try {
                        return@withContext callOpenRouter(openRouterKey, prompt)
                    } catch (routerError: Exception) {
                        Log.e(tag, "All providers failed. Gemini: $geminiMsg | Groq: $groqMsg | OpenRouter: ${routerError.message}")
                        return@withContext "Rejected: All AI evaluation providers failed."
                    }
                }
            }
        }

    private fun callGemini(apiKey: String, prompt: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent?key=$apiKey"

        val rootJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", """
                            You are a strict game rule and grammar judge. Enforce consistent verb tense, a maximum of 1 proper name (brand names or proper names are optional; having 0 or 1 is completely fine), clear pronoun usage, and logical sense. 
                            If pass is true, the reason MUST be strictly: "The sentence is grammatically correct, logically sound and passes all checks!"
                            Reply ONLY with a raw JSON object, no markdown, no backticks. 
                            Format exactly: {"pass": true/false, "reason": "..."}
                            
                            $prompt
                        """.trimIndent()))
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.0)
                put("maxOutputTokens", 150)
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            val responseBodyString = response.body?.string()
            if (!response.isSuccessful) {
                throw RuntimeException("Gemini HTTP Error ${response.code}: $responseBodyString")
            }
            if (responseBodyString.isNullOrBlank()) {
                throw RuntimeException("Gemini empty response body")
            }

            val json = JSONObject(responseBodyString)
            val candidates = json.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                throw RuntimeException("Gemini returned no candidates")
            }

            val parts = candidates.getJSONObject(0)
                .optJSONObject("content")
                ?.optJSONArray("parts")

            val generatedText = if (parts != null && parts.length() > 0) {
                parts.getJSONObject(0).optString("text", "")
            } else {
                ""
            }

            if (generatedText.isBlank()) {
                throw RuntimeException("Gemini returned blank text block")
            }

            return parseJsonReason(generatedText)
        }
    }

    private fun callGroq(apiKey: String, prompt: String): String {
        val url = "https://api.groq.com/openai/v1/chat/completions"

        val jsonBody = JSONObject().apply {
            put("model", "llama-3.3-70b-versatile")
            put("messages", JSONArray().apply {
                put(JSONObject().put("role", "system").put("content", "You are a strict game rule and logic judge. Enforce consistent tense, max 1 proper name, and logic. If pass is true, the reason MUST be strictly: \"The sentence is grammatically correct, logically sound and passes all checks!\". Reply ONLY in short JSON format: {\"pass\": true/false, \"reason\": \"...\"}"))
                put(JSONObject().put("role", "user").put("content", prompt))
            })
            put("temperature", 0.0)
            put("max_tokens", 100)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val responseBodyString = response.body?.string()
            if (!response.isSuccessful) {
                throw RuntimeException("Groq HTTP Error ${response.code}: $responseBodyString")
            }
            if (responseBodyString.isNullOrBlank()) {
                throw RuntimeException("Groq empty response body")
            }

            val json = JSONObject(responseBodyString)
            val choices = json.optJSONArray("choices")
            if (choices == null || choices.length() == 0) {
                throw RuntimeException("Groq returned no choices")
            }

            val generatedText = choices.getJSONObject(0)
                .optJSONObject("message")
                ?.optString("content")
                ?: throw RuntimeException("Groq message content was null")

            return parseJsonReason(generatedText)
        }
    }

    private fun callOpenRouter(apiKey: String, prompt: String): String {
        val url = "https://openrouter.ai/api/v1/chat/completions"

        val jsonBody = JSONObject().apply {
            put("model", "openrouter/free")
            put("messages", JSONArray().apply {
                put(JSONObject().put("role", "system").put("content", "You are a strict game rule and logic judge. Enforce consistent tense, max 1 proper name, and logic. If pass is true, the reason MUST be strictly: \"The sentence is grammatically correct, logically sound and passes!\". Reply ONLY in short JSON format: {\"pass\": true/false, \"reason\": \"...\"}"))
                put(JSONObject().put("role", "user").put("content", prompt))
            })
            put("temperature", 0.0)
            put("max_tokens", 100)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Title", "Namagram")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val responseBodyString = response.body?.string()
            if (!response.isSuccessful) {
                throw RuntimeException("OpenRouter HTTP Error ${response.code}: $responseBodyString")
            }
            if (responseBodyString.isNullOrBlank()) {
                throw RuntimeException("OpenRouter empty response body")
            }

            val json = JSONObject(responseBodyString)
            val choices = json.optJSONArray("choices")
            if (choices == null || choices.length() == 0) {
                throw RuntimeException("OpenRouter returned no choices")
            }

            val generatedText = choices.getJSONObject(0)
                .optJSONObject("message")
                ?.optString("content")
                ?: throw RuntimeException("OpenRouter message content was null")

            return parseJsonReason(generatedText)
        }
    }

    private fun parseJsonReason(rawText: String): String {
        var cleaned = rawText.trim()

        try {
            val jsonMatch = Regex("""\{[\s\S]*\}""").find(cleaned)
            if (jsonMatch != null) {
                cleaned = jsonMatch.value
            }
        } catch (_: Exception) {}

        if (cleaned.startsWith("```")) {
            cleaned = cleaned.substringAfter("\n").substringBeforeLast("```").trim()
        }

        return try {
            val json = JSONObject(cleaned)
            val isPass = json.optBoolean("pass", false)
            val rawReason = json.optString("reason", "No reason provided.")

            if (isPass) {
                rawReason
            } else {
                "Rejected: $rawReason"
            }
        } catch (_: Exception) {
            "Rejected: Invalid JSON format from AI. Raw: $cleaned"
        }
    }
}