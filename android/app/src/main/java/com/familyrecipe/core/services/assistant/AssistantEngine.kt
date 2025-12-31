package com.familyrecipe.core.services.assistant

import com.familyrecipe.core.models.CookingWorkstate
import com.familyrecipe.core.models.Recipe

// MARK: - Assistant Intent

sealed class AssistantIntent {
    object NextStep : AssistantIntent()
    object PreviousStep : AssistantIntent()
    object RepeatStep : AssistantIntent()
    data class GoToStep(val step: Int) : AssistantIntent()
    data class SetTimer(val durationMs: Long) : AssistantIntent()
    object CancelTimer : AssistantIntent()
    object CheckTimer : AssistantIntent()
    data class SubstituteIngredient(val ingredient: String) : AssistantIntent()
    data class ScaleServings(val newServings: Int) : AssistantIntent()
    object WhatIngredients : AssistantIntent()
    object WhatIsNext : AssistantIntent()
    object HowLongLeft : AssistantIntent()
    data class ExplainTechnique(val technique: String) : AssistantIntent()
    object Help : AssistantIntent()
    data class Unknown(val query: String) : AssistantIntent()
}

// MARK: - Assistant Engine

class AssistantEngine {

    private val knowledgeBase = KnowledgeBase()
    private val responseGenerator = ResponseGenerator()

    fun process(query: String, workstate: CookingWorkstate, recipe: Recipe): String {
        val intent = classifyIntent(query)
        return generateResponse(intent, workstate, recipe)
    }

    fun classifyIntent(query: String): AssistantIntent {
        val text = query.lowercase().trim()

        // Navigation
        if (matchesAny(text, listOf("next", "next step", "continue", "done", "go ahead"))) {
            return AssistantIntent.NextStep
        }

        if (matchesAny(text, listOf("back", "previous", "go back", "last step"))) {
            return AssistantIntent.PreviousStep
        }

        if (matchesAny(text, listOf("repeat", "again", "what", "say that again"))) {
            return AssistantIntent.RepeatStep
        }

        // Step number
        extractNumber(text, "(?:step|go to)\\s*(\\d+)")?.let {
            return AssistantIntent.GoToStep(it)
        }

        // Timer
        extractDuration(text)?.let {
            return AssistantIntent.SetTimer(it)
        }

        if (matchesAny(text, listOf("cancel timer", "stop timer", "clear timer"))) {
            return AssistantIntent.CancelTimer
        }

        if (matchesAny(text, listOf("how much time", "time left", "timer status"))) {
            return AssistantIntent.CheckTimer
        }

        // Substitution
        extractIngredient(text)?.let {
            return AssistantIntent.SubstituteIngredient(it)
        }

        // Scaling
        if (text.contains("double")) return AssistantIntent.ScaleServings(2)
        if (text.contains("triple")) return AssistantIntent.ScaleServings(3)

        extractNumber(text, "(\\d+)\\s*servings?")?.let {
            return AssistantIntent.ScaleServings(it)
        }

        // Information
        if (matchesAny(text, listOf("what ingredients", "ingredients list", "what do i need"))) {
            return AssistantIntent.WhatIngredients
        }

        if (matchesAny(text, listOf("what's next", "what's after", "upcoming"))) {
            return AssistantIntent.WhatIsNext
        }

        if (matchesAny(text, listOf("how long left", "how much longer", "eta"))) {
            return AssistantIntent.HowLongLeft
        }

        // Technique
        extractTechnique(text)?.let {
            return AssistantIntent.ExplainTechnique(it)
        }

        // Help
        if (matchesAny(text, listOf("help", "what can you do", "commands"))) {
            return AssistantIntent.Help
        }

        return AssistantIntent.Unknown(text)
    }

    fun generateResponse(intent: AssistantIntent, workstate: CookingWorkstate, recipe: Recipe): String {
        return responseGenerator.generate(intent, workstate, recipe, knowledgeBase)
    }

    private fun matchesAny(text: String, patterns: List<String>): Boolean {
        return patterns.any { text.contains(it) }
    }

    private fun extractNumber(text: String, pattern: String): Int? {
        val regex = Regex(pattern, RegexOption.IGNORE_CASE)
        return regex.find(text)?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    private fun extractDuration(text: String): Long? {
        val patterns = listOf(
            "(\\d+)\\s*(?:minute|min)s?" to 60_000L,
            "(\\d+)\\s*(?:second|sec)s?" to 1_000L,
            "(\\d+)\\s*(?:hour|hr)s?" to 3_600_000L
        )

        for ((pattern, multiplier) in patterns) {
            extractNumber(text, pattern)?.let {
                return it * multiplier
            }
        }
        return null
    }

    private fun extractIngredient(text: String): String? {
        val patterns = listOf(
            "substitute\\s+(?:for\\s+)?(.+?)(?:\\?|\$)",
            "instead\\s+of\\s+(.+?)(?:\\?|\$)",
            "don't have\\s+(.+?)(?:\\?|\$)",
            "out of\\s+(.+?)(?:\\?|\$)"
        )

        for (pattern in patterns) {
            val regex = Regex(pattern, RegexOption.IGNORE_CASE)
            regex.find(text)?.groupValues?.getOrNull(1)?.trim()?.let {
                return it
            }
        }
        return null
    }

    private fun extractTechnique(text: String): String? {
        val patterns = listOf(
            "what\\s+(?:does|is)\\s+(.+?)(?:\\s+mean|\\?|\$)",
            "how\\s+(?:do|to)\\s+(?:i\\s+)?(.+?)(?:\\?|\$)",
            "explain\\s+(.+?)(?:\\?|\$)"
        )

        for (pattern in patterns) {
            val regex = Regex(pattern, RegexOption.IGNORE_CASE)
            regex.find(text)?.groupValues?.getOrNull(1)?.trim()?.let {
                return it
            }
        }
        return null
    }
}
