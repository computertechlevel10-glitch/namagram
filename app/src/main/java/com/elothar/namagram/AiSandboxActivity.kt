package com.elothar.namagram

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("SetTextI18n")
class AiSandboxActivity : AppCompatActivity() {

    private val tileInputBoxes = mutableListOf<EditText>()

    // Initialize the evaluator using your current keys from BuildConfig
    private val aiEvaluator = UnifiedAIEvaluator(
        geminiKey = BuildConfig.GEMINI_API_KEY,
        groqKey = BuildConfig.GROQ_API_KEY,
        openRouterKey = BuildConfig.OPENROUTER_API_KEY,
        isDebug = true
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            setBackgroundColor("#0F172A".toColorInt())
        }

        val title = TextView(this).apply {
            text = "🛠️ AI Rule Enforcement Sandbox"
            textSize = 20f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(0, 0, 0, 24)
        }
        rootLayout.addView(title)

        // Instructions for 10-Tile Rack
        val tileInstruction = TextView(this).apply {
            text = "Enter 10 Letter Tiles:"
            textSize = 14f
            setTextColor(android.graphics.Color.LTGRAY)
            setPadding(0, 0, 0, 8)
        }
        rootLayout.addView(tileInstruction)

        // Horizontal Row Container for 10 Letter Boxes
        val tileRowLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(0, 0, 0, 24)
        }

        // 10 default test letters
        val defaultLetters = listOf("U", "L", "O", "X", "A", "B", "C", "D", "E", "F")

        for (i in 0 until 10) {
            val letterBox = EditText(this).apply {
                layoutParams = LinearLayout.LayoutParams(80, 110).apply {
                    setMargins(3, 0, 3, 0)
                }
                gravity = Gravity.CENTER
                textSize = 16f
                setTextColor(android.graphics.Color.WHITE)
                setHintTextColor(android.graphics.Color.GRAY)
                filters = arrayOf(android.text.InputFilter.LengthFilter(1))
                setText(defaultLetters[i])
            }
            tileInputBoxes.add(letterBox)
            tileRowLayout.addView(letterBox)
        }
        rootLayout.addView(tileRowLayout)

        // Sentence Input
        val inputSentence = EditText(this).apply {
            setText("ugly lads own xylophones")
            setHintTextColor(android.graphics.Color.GRAY)
            setTextColor(android.graphics.Color.WHITE)
            setPadding(16, 16, 16, 16)
        }
        rootLayout.addView(inputSentence)

        // Result Output Box
        val tvOutput = TextView(this).apply {
            text = "Results will appear here..."
            setTextColor(android.graphics.Color.GREEN)
            setPadding(16, 16, 16, 16)
            textSize = 14f
        }

        // Test Button triggering real AI evaluation chain
        val btnRun = Button(this).apply {
            text = "Run Live AI Evaluation"
            setOnClickListener {
                val collectedTiles = tileInputBoxes.map { it.text.toString().trim() }
                val sentence = inputSentence.text.toString()

                tvOutput.text = "⏳ Querying evaluator and fallbacks..."
                tvOutput.setTextColor(android.graphics.Color.YELLOW)

                lifecycleScope.launch {
                    try {
                        val evaluationReason = aiEvaluator.evaluateSentence(sentence, collectedTiles)
                        val isPass = !evaluationReason.startsWith("Rejected:")

                        withContext(Dispatchers.Main) {
                            tvOutput.setTextColor(if (isPass) android.graphics.Color.GREEN else android.graphics.Color.RED)
                            tvOutput.text = """
                                Status: ${if (isPass) "✅ PASS" else "❌ FAIL"}
                                Reason: $evaluationReason
                            """.trimIndent()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            tvOutput.setTextColor(android.graphics.Color.RED)
                            tvOutput.text = "❌ Error: ${e.localizedMessage ?: "Unknown error"}"
                        }
                    }
                }
            }
        }
        rootLayout.addView(btnRun)
        rootLayout.addView(tvOutput)

        val scrollView = ScrollView(this).apply {
            addView(rootLayout)
        }
        setContentView(scrollView)
    }
}