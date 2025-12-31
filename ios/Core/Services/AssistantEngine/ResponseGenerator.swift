import Foundation

// MARK: - Response Generator

/// Generates natural language responses for assistant intents
struct ResponseGenerator {

    func generate(
        intent: AssistantIntent,
        workstate: CookingWorkstate,
        recipe: Recipe,
        knowledgeBase: KnowledgeBase
    ) -> String {
        switch intent {
        case .nextStep:
            return generateNextStepResponse(workstate: workstate, recipe: recipe)

        case .previousStep:
            return generatePreviousStepResponse(workstate: workstate, recipe: recipe)

        case .repeatStep:
            return generateRepeatStepResponse(workstate: workstate, recipe: recipe)

        case .goToStep(let step):
            return generateGoToStepResponse(step: step, recipe: recipe)

        case .setTimer(let duration):
            return generateSetTimerResponse(duration: duration)

        case .cancelTimer:
            return generateCancelTimerResponse(workstate: workstate)

        case .checkTimer:
            return generateCheckTimerResponse(workstate: workstate)

        case .substituteIngredient(let ingredient):
            return generateSubstitutionResponse(ingredient: ingredient, knowledgeBase: knowledgeBase)

        case .scaleServings(let newServings):
            return generateScaleResponse(newServings: newServings, recipe: recipe, workstate: workstate)

        case .whatIngredients:
            return generateIngredientsListResponse(recipe: recipe, workstate: workstate)

        case .whatIsNext:
            return generateWhatIsNextResponse(workstate: workstate, recipe: recipe)

        case .howLongLeft:
            return generateHowLongLeftResponse(workstate: workstate, recipe: recipe)

        case .explainTechnique(let technique):
            return generateTechniqueResponse(technique: technique, knowledgeBase: knowledgeBase)

        case .startCooking:
            return generateStartCookingResponse(recipe: recipe)

        case .pauseCooking:
            return "Pausing at step \(workstate.stepIndex + 1). Say 'continue' when you're ready."

        case .resumeCooking:
            return generateResumeResponse(workstate: workstate, recipe: recipe)

        case .endCooking:
            return "Great job! Enjoy your \(recipe.title)!"

        case .help:
            return generateHelpResponse()

        case .unknown(let query):
            return "I'm not sure how to help with \"\(query)\". Try saying \"help\" for available commands."
        }
    }

    // MARK: - Navigation Responses

    private func generateNextStepResponse(workstate: CookingWorkstate, recipe: Recipe) -> String {
        let nextIndex = workstate.stepIndex + 1

        if nextIndex >= recipe.instructions.count {
            return "That was the last step! Your \(recipe.title) should be ready. Enjoy!"
        }

        let nextStep = recipe.instructions[nextIndex]
        return "Step \(nextIndex + 1): \(nextStep.text)"
    }

    private func generatePreviousStepResponse(workstate: CookingWorkstate, recipe: Recipe) -> String {
        if workstate.stepIndex <= 0 {
            return "You're already at the first step."
        }

        let prevIndex = workstate.stepIndex - 1
        let prevStep = recipe.instructions[prevIndex]
        return "Going back. Step \(prevIndex + 1): \(prevStep.text)"
    }

    private func generateRepeatStepResponse(workstate: CookingWorkstate, recipe: Recipe) -> String {
        guard workstate.stepIndex < recipe.instructions.count else {
            return "No active step to repeat."
        }

        let step = recipe.instructions[workstate.stepIndex]
        return "Step \(workstate.stepIndex + 1): \(step.text)"
    }

    private func generateGoToStepResponse(step: Int, recipe: Recipe) -> String {
        let stepIndex = step - 1 // Convert to 0-based

        if stepIndex < 0 || stepIndex >= recipe.instructions.count {
            return "This recipe only has \(recipe.instructions.count) steps."
        }

        let instruction = recipe.instructions[stepIndex]
        return "Jumping to step \(step): \(instruction.text)"
    }

    // MARK: - Timer Responses

    private func generateSetTimerResponse(duration: TimeInterval) -> String {
        let formatted = formatDuration(duration)
        return "Timer set for \(formatted). I'll let you know when it's done."
    }

    private func generateCancelTimerResponse(workstate: CookingWorkstate) -> String {
        if workstate.timers.isEmpty {
            return "There's no active timer to cancel."
        }
        return "Timer cancelled."
    }

    private func generateCheckTimerResponse(workstate: CookingWorkstate) -> String {
        let activeTimers = workstate.timers.filter { !$0.isCompleted }

        if activeTimers.isEmpty {
            return "You don't have any active timers."
        }

        if activeTimers.count == 1 {
            return "\(activeTimers[0].formattedRemaining) remaining on your timer."
        }

        let timerList = activeTimers.map { "\($0.label): \($0.formattedRemaining)" }.joined(separator: ", ")
        return "Active timers: \(timerList)"
    }

    // MARK: - Substitution Response

    private func generateSubstitutionResponse(ingredient: String, knowledgeBase: KnowledgeBase) -> String {
        guard let substitutions = knowledgeBase.getSubstitutions(for: ingredient) else {
            return "I don't have a substitution for \(ingredient). Try searching online or skip it if it's optional."
        }

        let options = substitutions.map { "\($0.name) (\($0.ratio))" }.joined(separator: ", or ")
        return "You can substitute \(ingredient) with: \(options)"
    }

    // MARK: - Scale Response

    private func generateScaleResponse(newServings: Int, recipe: Recipe, workstate: CookingWorkstate) -> String {
        let scaleFactor = Double(newServings) / Double(recipe.servings)
        let formattedFactor = String(format: "%.1f", scaleFactor)

        return "Scaling from \(recipe.servings) to \(newServings) servings. Multiply all ingredients by \(formattedFactor)."
    }

    // MARK: - Ingredients Response

    private func generateIngredientsListResponse(recipe: Recipe, workstate: CookingWorkstate) -> String {
        let ingredientsList = recipe.ingredients.map { ingredient in
            var display = ingredient.displayString
            if workstate.scaleFactor != 1.0 {
                let scaled = ingredient.amount * workstate.scaleFactor
                let scaledStr = scaled.truncatingRemainder(dividingBy: 1) == 0
                    ? String(format: "%.0f", scaled)
                    : String(format: "%.1f", scaled)
                display = "\(scaledStr) \(ingredient.unit.abbreviation) \(ingredient.name)"
            }
            return display
        }.joined(separator: "\n• ")

        return "For \(recipe.title), you'll need:\n• \(ingredientsList)"
    }

    // MARK: - Preview Response

    private func generateWhatIsNextResponse(workstate: CookingWorkstate, recipe: Recipe) -> String {
        let nextIndex = workstate.stepIndex + 1

        if nextIndex >= recipe.instructions.count {
            return "After this step, you're done! Your \(recipe.title) will be ready."
        }

        let nextStep = recipe.instructions[nextIndex]
        return "After this step, you'll: \(nextStep.text)"
    }

    // MARK: - Time Remaining Response

    private func generateHowLongLeftResponse(workstate: CookingWorkstate, recipe: Recipe) -> String {
        let remainingSteps = recipe.instructions.count - workstate.stepIndex - 1

        if remainingSteps <= 0 {
            return "You're on the last step!"
        }

        // Estimate time from remaining steps
        var estimatedSeconds = 0
        for i in (workstate.stepIndex + 1)..<recipe.instructions.count {
            let step = recipe.instructions[i]
            if let duration = step.durationSeconds {
                estimatedSeconds += duration
            } else {
                estimatedSeconds += 120 // Assume 2 min per step without timing
            }
        }

        let estimatedTime = formatDuration(TimeInterval(estimatedSeconds))
        return "You have \(remainingSteps) step\(remainingSteps == 1 ? "" : "s") left. Estimated time: \(estimatedTime)."
    }

    // MARK: - Technique Response

    private func generateTechniqueResponse(technique: String, knowledgeBase: KnowledgeBase) -> String {
        guard let info = knowledgeBase.getTechnique(technique) else {
            return "I'm not sure about '\(technique)'. Try searching online for a demonstration."
        }

        return "\(info.explanation)\n\nTip: \(info.tips)"
    }

    // MARK: - Session Responses

    private func generateStartCookingResponse(recipe: Recipe) -> String {
        let firstStep = recipe.instructions.first?.text ?? "No steps available."
        let totalTime = formatDuration(TimeInterval(recipe.totalTimeMinutes * 60))

        return "Let's cook \(recipe.title)!\nIt makes \(recipe.servings) servings and takes about \(totalTime) total.\n\nStep 1: \(firstStep)"
    }

    private func generateResumeResponse(workstate: CookingWorkstate, recipe: Recipe) -> String {
        guard workstate.stepIndex < recipe.instructions.count else {
            return "Welcome back! You've completed all steps."
        }

        let step = recipe.instructions[workstate.stepIndex]
        return "Welcome back! You're on step \(workstate.stepIndex + 1): \(step.text)"
    }

    // MARK: - Help Response

    private func generateHelpResponse() -> String {
        return """
        I can help you cook! Try saying:
        • "Next step" or "Previous step"
        • "Set timer for 5 minutes"
        • "Substitute for eggs"
        • "Double the recipe"
        • "How long left?"
        • "What does sauté mean?"
        • "What ingredients do I need?"
        """
    }

    // MARK: - Helpers

    private func formatDuration(_ seconds: TimeInterval) -> String {
        let totalSeconds = Int(seconds)

        if totalSeconds < 60 {
            return "\(totalSeconds) seconds"
        } else if totalSeconds < 3600 {
            let minutes = totalSeconds / 60
            return "\(minutes) minute\(minutes == 1 ? "" : "s")"
        } else {
            let hours = totalSeconds / 3600
            let minutes = (totalSeconds % 3600) / 60
            if minutes == 0 {
                return "\(hours) hour\(hours == 1 ? "" : "s")"
            }
            return "\(hours) hour\(hours == 1 ? "" : "s") \(minutes) min"
        }
    }
}
