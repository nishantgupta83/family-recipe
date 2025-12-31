package com.familyrecipe.core.services.assistant

import com.familyrecipe.core.models.CookingWorkstate
import com.familyrecipe.core.models.Recipe

class ResponseGenerator {

    fun generate(
        intent: AssistantIntent,
        workstate: CookingWorkstate,
        recipe: Recipe,
        knowledgeBase: KnowledgeBase
    ): String {
        return when (intent) {
            is AssistantIntent.NextStep -> generateNextStep(workstate, recipe)
            is AssistantIntent.PreviousStep -> generatePreviousStep(workstate, recipe)
            is AssistantIntent.RepeatStep -> generateRepeatStep(workstate, recipe)
            is AssistantIntent.GoToStep -> generateGoToStep(intent.step, recipe)
            is AssistantIntent.SetTimer -> generateSetTimer(intent.durationMs)
            is AssistantIntent.CancelTimer -> generateCancelTimer(workstate)
            is AssistantIntent.CheckTimer -> generateCheckTimer(workstate)
            is AssistantIntent.SubstituteIngredient -> generateSubstitution(intent.ingredient, knowledgeBase)
            is AssistantIntent.ScaleServings -> generateScale(intent.newServings, recipe)
            is AssistantIntent.WhatIngredients -> generateIngredientsList(recipe, workstate)
            is AssistantIntent.WhatIsNext -> generateWhatIsNext(workstate, recipe)
            is AssistantIntent.HowLongLeft -> generateHowLongLeft(workstate, recipe)
            is AssistantIntent.ExplainTechnique -> generateTechnique(intent.technique, knowledgeBase)
            is AssistantIntent.Help -> generateHelp()
            is AssistantIntent.Unknown -> "I'm not sure how to help with \"${intent.query}\". Try saying \"help\"."
        }
    }

    private fun generateNextStep(workstate: CookingWorkstate, recipe: Recipe): String {
        val nextIndex = workstate.stepIndex + 1
        if (nextIndex >= recipe.instructions.size) {
            return "That was the last step! Your ${recipe.title} should be ready. Enjoy!"
        }
        val step = recipe.instructions[nextIndex]
        return "Step ${nextIndex + 1}: ${step.text}"
    }

    private fun generatePreviousStep(workstate: CookingWorkstate, recipe: Recipe): String {
        if (workstate.stepIndex <= 0) {
            return "You're already at the first step."
        }
        val prevIndex = workstate.stepIndex - 1
        val step = recipe.instructions[prevIndex]
        return "Going back. Step ${prevIndex + 1}: ${step.text}"
    }

    private fun generateRepeatStep(workstate: CookingWorkstate, recipe: Recipe): String {
        if (workstate.stepIndex >= recipe.instructions.size) {
            return "No active step to repeat."
        }
        val step = recipe.instructions[workstate.stepIndex]
        return "Step ${workstate.stepIndex + 1}: ${step.text}"
    }

    private fun generateGoToStep(step: Int, recipe: Recipe): String {
        val stepIndex = step - 1
        if (stepIndex < 0 || stepIndex >= recipe.instructions.size) {
            return "This recipe only has ${recipe.instructions.size} steps."
        }
        val instruction = recipe.instructions[stepIndex]
        return "Jumping to step $step: ${instruction.text}"
    }

    private fun generateSetTimer(durationMs: Long): String {
        val formatted = formatDuration(durationMs)
        return "Timer set for $formatted. I'll let you know when it's done."
    }

    private fun generateCancelTimer(workstate: CookingWorkstate): String {
        return if (workstate.timers.isEmpty()) {
            "There's no active timer to cancel."
        } else {
            "Timer cancelled."
        }
    }

    private fun generateCheckTimer(workstate: CookingWorkstate): String {
        val activeTimers = workstate.timers.filter { !it.isCompleted }
        if (activeTimers.isEmpty()) {
            return "You don't have any active timers."
        }
        if (activeTimers.size == 1) {
            return "${activeTimers[0].formattedRemaining} remaining on your timer."
        }
        val timerList = activeTimers.joinToString(", ") { "${it.label}: ${it.formattedRemaining}" }
        return "Active timers: $timerList"
    }

    private fun generateSubstitution(ingredient: String, knowledgeBase: KnowledgeBase): String {
        val subs = knowledgeBase.getSubstitutions(ingredient)
            ?: return "I don't have a substitution for $ingredient. Try searching online."

        val options = subs.joinToString(", or ") { "${it.name} (${it.ratio})" }
        return "You can substitute $ingredient with: $options"
    }

    private fun generateScale(newServings: Int, recipe: Recipe): String {
        val scaleFactor = newServings.toDouble() / recipe.servings
        return "Scaling from ${recipe.servings} to $newServings servings. Multiply all ingredients by ${String.format("%.1f", scaleFactor)}."
    }

    private fun generateIngredientsList(recipe: Recipe, workstate: CookingWorkstate): String {
        val list = recipe.ingredients.joinToString("\n• ") { it.displayString }
        return "For ${recipe.title}, you'll need:\n• $list"
    }

    private fun generateWhatIsNext(workstate: CookingWorkstate, recipe: Recipe): String {
        val nextIndex = workstate.stepIndex + 1
        if (nextIndex >= recipe.instructions.size) {
            return "After this step, you're done!"
        }
        val nextStep = recipe.instructions[nextIndex]
        return "After this step, you'll: ${nextStep.text}"
    }

    private fun generateHowLongLeft(workstate: CookingWorkstate, recipe: Recipe): String {
        val remainingSteps = recipe.instructions.size - workstate.stepIndex - 1
        if (remainingSteps <= 0) {
            return "You're on the last step!"
        }

        var estimatedSeconds = 0
        for (i in (workstate.stepIndex + 1) until recipe.instructions.size) {
            val step = recipe.instructions[i]
            estimatedSeconds += step.durationSeconds ?: 120
        }

        val time = formatDuration(estimatedSeconds * 1000L)
        return "You have $remainingSteps step${if (remainingSteps == 1) "" else "s"} left. Estimated time: $time."
    }

    private fun generateTechnique(technique: String, knowledgeBase: KnowledgeBase): String {
        val info = knowledgeBase.getTechnique(technique)
            ?: return "I'm not sure about '$technique'. Try searching online."

        return "${info.explanation}\n\nTip: ${info.tips}"
    }

    private fun generateHelp(): String {
        return """
            I can help you cook! Try saying:
            • "Next step" or "Previous step"
            • "Set timer for 5 minutes"
            • "Substitute for eggs"
            • "Double the recipe"
            • "How long left?"
            • "What does sauté mean?"
        """.trimIndent()
    }

    private fun formatDuration(ms: Long): String {
        val totalSeconds = (ms / 1000).toInt()
        return when {
            totalSeconds < 60 -> "$totalSeconds seconds"
            totalSeconds < 3600 -> {
                val minutes = totalSeconds / 60
                "$minutes minute${if (minutes == 1) "" else "s"}"
            }
            else -> {
                val hours = totalSeconds / 3600
                val minutes = (totalSeconds % 3600) / 60
                if (minutes == 0) "$hours hour${if (hours == 1) "" else "s"}"
                else "$hours hour${if (hours == 1) "" else "s"} $minutes min"
            }
        }
    }
}
