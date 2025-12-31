package com.familyrecipe.features.scan

import android.graphics.Bitmap
import com.familyrecipe.core.models.TextRange
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * OCR Service using ML Kit for text recognition
 */
class OCRService {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    data class OCRResult(
        val text: String,
        val confidence: Double,
        val lowConfidenceRanges: List<TextRange>,
        val processingTimeMs: Long
    )

    sealed class OCRError : Exception() {
        object NoTextFound : OCRError() {
            override val message = "No text was found in the scanned image."
        }
        data class ProcessingFailed(override val message: String) : OCRError()
        object Cancelled : OCRError() {
            override val message = "OCR processing was cancelled."
        }
    }

    /**
     * Process multiple images and combine OCR results
     */
    suspend fun processImages(images: List<Bitmap>): OCRResult = withContext(Dispatchers.Default) {
        if (images.isEmpty()) {
            throw OCRError.NoTextFound
        }

        val startTime = System.currentTimeMillis()
        val allText = StringBuilder()
        var totalConfidence = 0.0
        val allLowConfidenceRanges = mutableListOf<TextRange>()
        var currentPosition = 0

        images.forEachIndexed { index, bitmap ->
            val result = processImage(bitmap, currentPosition)

            if (allText.isNotEmpty() && result.text.isNotEmpty()) {
                allText.append("\n\n--- Page ${index + 2} ---\n\n")
                currentPosition = allText.length
            }

            allText.append(result.text)
            currentPosition = allText.length
            totalConfidence += result.confidence
            allLowConfidenceRanges.addAll(result.lowConfidenceRanges)
        }

        if (allText.isEmpty()) {
            throw OCRError.NoTextFound
        }

        val averageConfidence = totalConfidence / images.size
        val processingTime = System.currentTimeMillis() - startTime

        OCRResult(
            text = allText.toString(),
            confidence = averageConfidence,
            lowConfidenceRanges = allLowConfidenceRanges,
            processingTimeMs = processingTime
        )
    }

    /**
     * Process a single image using ML Kit
     */
    private suspend fun processImage(bitmap: Bitmap, startPosition: Int): OCRResult =
        suspendCoroutine { continuation ->
            val inputImage = InputImage.fromBitmap(bitmap, 0)

            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    val text = StringBuilder()
                    var totalConfidence = 0.0f
                    var blockCount = 0
                    val lowConfidenceRanges = mutableListOf<TextRange>()
                    var currentPosition = startPosition

                    for (block in visionText.textBlocks) {
                        for (line in block.lines) {
                            val lineText = line.text
                            // ML Kit doesn't provide per-line confidence, estimate from elements
                            val lineConfidence = line.elements
                                .mapNotNull { it.confidence }
                                .average()
                                .toFloat()
                                .takeIf { !it.isNaN() } ?: 0.8f

                            // Track low confidence text ranges
                            if (lineConfidence < 0.7f) {
                                lowConfidenceRanges.add(
                                    TextRange(
                                        id = UUID.randomUUID().toString(),
                                        start = currentPosition,
                                        end = currentPosition + lineText.length,
                                        confidence = lineConfidence.toDouble()
                                    )
                                )
                            }

                            text.append(lineText).append("\n")
                            currentPosition = startPosition + text.length
                            totalConfidence += lineConfidence
                            blockCount++
                        }
                    }

                    val averageConfidence = if (blockCount > 0) {
                        (totalConfidence / blockCount).toDouble()
                    } else {
                        0.0
                    }

                    continuation.resume(
                        OCRResult(
                            text = text.toString().trim(),
                            confidence = averageConfidence,
                            lowConfidenceRanges = lowConfidenceRanges,
                            processingTimeMs = 0
                        )
                    )
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(
                        OCRError.ProcessingFailed(e.message ?: "Unknown error")
                    )
                }
        }

    /**
     * Parse OCR text into recipe components
     */
    fun parseRecipeText(text: String): ParsedRecipe {
        var title = ""
        val ingredients = mutableListOf<String>()
        val instructions = mutableListOf<String>()
        var currentSection = ParseSection.UNKNOWN

        val lines = text.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        lines.forEachIndexed { index, line ->
            val lowercased = line.lowercase()

            // Detect sections
            when {
                lowercased.contains("ingredient") -> {
                    currentSection = ParseSection.INGREDIENTS
                    return@forEachIndexed
                }
                lowercased.contains("instruction") ||
                lowercased.contains("direction") ||
                lowercased.contains("step") ||
                lowercased.contains("method") -> {
                    currentSection = ParseSection.INSTRUCTIONS
                    return@forEachIndexed
                }
            }

            // First non-section line could be title
            if (index == 0 && title.isEmpty() && currentSection == ParseSection.UNKNOWN) {
                title = line
                return@forEachIndexed
            }

            // Parse based on current section
            when (currentSection) {
                ParseSection.INGREDIENTS -> {
                    if (isLikelyIngredient(line)) {
                        ingredients.add(cleanIngredientLine(line))
                    }
                }
                ParseSection.INSTRUCTIONS -> {
                    if (isLikelyInstruction(line)) {
                        instructions.add(cleanInstructionLine(line))
                    }
                }
                ParseSection.UNKNOWN -> {
                    // Try to guess based on content
                    when {
                        isLikelyIngredient(line) -> ingredients.add(cleanIngredientLine(line))
                        isLikelyInstruction(line) -> instructions.add(cleanInstructionLine(line))
                    }
                }
            }
        }

        return ParsedRecipe(
            title = title,
            ingredients = ingredients,
            instructions = instructions
        )
    }

    private enum class ParseSection {
        UNKNOWN,
        INGREDIENTS,
        INSTRUCTIONS
    }

    private fun isLikelyIngredient(line: String): Boolean {
        // Ingredients often start with numbers/fractions or bullet points
        val patterns = listOf(
            Regex("^[0-9½¼¾⅓⅔⅛⅜⅝⅞]"),  // Starts with number or fraction
            Regex("^[-•*]"),              // Starts with bullet
            Regex("cup|tablespoon|teaspoon|tbsp|tsp|oz|lb|gram|kg|ml|pinch|dash", RegexOption.IGNORE_CASE)
        )

        return patterns.any { it.containsMatchIn(line) }
    }

    private fun isLikelyInstruction(line: String): Boolean {
        val actionVerbs = listOf(
            "mix", "stir", "add", "combine", "heat", "cook", "bake", "preheat",
            "pour", "place", "let", "wait", "remove", "set", "prepare", "chop",
            "slice", "dice", "fold", "whisk", "beat", "simmer", "boil", "fry"
        )

        val lowercased = line.lowercase()

        // Check if starts with a number (step number)
        if (Regex("^\\d+[.)]").containsMatchIn(line)) {
            return true
        }

        // Check for action verbs
        if (actionVerbs.any { lowercased.contains(it) }) {
            return true
        }

        // Long sentences are likely instructions
        return line.length > 40
    }

    private fun cleanIngredientLine(line: String): String {
        // Remove leading bullets/dashes
        return line.replace(Regex("^[-•*]\\s*"), "").trim()
    }

    private fun cleanInstructionLine(line: String): String {
        // Remove leading step numbers
        return line.replace(Regex("^\\d+[.):]\\s*"), "").trim()
    }
}

/**
 * Parsed recipe structure from OCR text
 */
data class ParsedRecipe(
    val title: String,
    val ingredients: List<String>,
    val instructions: List<String>
) {
    val isEmpty: Boolean
        get() = title.isEmpty() && ingredients.isEmpty() && instructions.isEmpty()
}
