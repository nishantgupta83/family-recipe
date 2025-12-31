import Foundation

// MARK: - Assistant Engine

/// Rule-based cooking assistant with context awareness
final class AssistantEngine {
    static let shared = AssistantEngine()

    private let knowledgeBase = KnowledgeBase()
    private let responseGenerator = ResponseGenerator()

    private init() {}

    // MARK: - Process Query

    func process(query: String, workstate: CookingWorkstate, recipe: Recipe) -> String {
        let intent = classifyIntent(query)
        return responseGenerator.generate(
            intent: intent,
            workstate: workstate,
            recipe: recipe,
            knowledgeBase: knowledgeBase
        )
    }

    // MARK: - Intent Classification

    func classifyIntent(_ query: String) -> AssistantIntent {
        let lowercased = query.lowercased().trimmingCharacters(in: .whitespacesAndNewlines)

        // Navigation intents
        if matchesAny(lowercased, patterns: ["next", "next step", "continue", "done", "go ahead"]) {
            return .nextStep
        }

        if matchesAny(lowercased, patterns: ["back", "previous", "go back", "last step"]) {
            return .previousStep
        }

        if matchesAny(lowercased, patterns: ["repeat", "again", "what", "say that again"]) {
            return .repeatStep
        }

        // Step number extraction
        if let stepMatch = extractNumber(from: lowercased, pattern: "(?:step|go to)\\s*(\\d+)") {
            return .goToStep(stepMatch)
        }

        // Timer intents
        if let duration = extractDuration(from: lowercased) {
            return .setTimer(duration)
        }

        if matchesAny(lowercased, patterns: ["cancel timer", "stop timer", "clear timer"]) {
            return .cancelTimer
        }

        if matchesAny(lowercased, patterns: ["how much time", "time left", "timer status"]) {
            return .checkTimer
        }

        // Substitution intent
        if let ingredient = extractIngredient(from: lowercased) {
            return .substituteIngredient(ingredient)
        }

        // Scaling intent
        if matchesAny(lowercased, patterns: ["double", "triple"]) {
            let multiplier = lowercased.contains("triple") ? 3 : 2
            return .scaleServings(multiplier)
        }

        if let servings = extractNumber(from: lowercased, pattern: "(\\d+)\\s*servings?") {
            return .scaleServings(servings)
        }

        // Information intents
        if matchesAny(lowercased, patterns: ["what ingredients", "ingredients list", "what do i need"]) {
            return .whatIngredients
        }

        if matchesAny(lowercased, patterns: ["what's next", "what's after", "upcoming"]) {
            return .whatIsNext
        }

        if matchesAny(lowercased, patterns: ["how long left", "how much longer", "eta", "time remaining"]) {
            return .howLongLeft
        }

        // Technique explanation
        if let technique = extractTechnique(from: lowercased) {
            return .explainTechnique(technique)
        }

        // Help
        if matchesAny(lowercased, patterns: ["help", "what can you do", "commands"]) {
            return .help
        }

        return .unknown(lowercased)
    }

    // MARK: - Pattern Matching Helpers

    private func matchesAny(_ text: String, patterns: [String]) -> Bool {
        patterns.contains { text.contains($0) }
    }

    private func extractNumber(from text: String, pattern: String) -> Int? {
        guard let regex = try? NSRegularExpression(pattern: pattern, options: .caseInsensitive) else {
            return nil
        }

        let range = NSRange(text.startIndex..., in: text)
        if let match = regex.firstMatch(in: text, options: [], range: range),
           let numberRange = Range(match.range(at: 1), in: text) {
            return Int(text[numberRange])
        }
        return nil
    }

    private func extractDuration(from text: String) -> TimeInterval? {
        // Match patterns like "5 minutes", "10 min", "30 seconds"
        let patterns = [
            ("(\\d+)\\s*(?:minute|min)s?", 60.0),
            ("(\\d+)\\s*(?:second|sec)s?", 1.0),
            ("(\\d+)\\s*(?:hour|hr)s?", 3600.0)
        ]

        for (pattern, multiplier) in patterns {
            if let value = extractNumber(from: text, pattern: pattern) {
                return TimeInterval(value) * multiplier
            }
        }

        return nil
    }

    private func extractIngredient(from text: String) -> String? {
        let patterns = [
            "substitute\\s+(?:for\\s+)?(.+?)(?:\\?|$)",
            "instead\\s+of\\s+(.+?)(?:\\?|$)",
            "replacement\\s+for\\s+(.+?)(?:\\?|$)",
            "don't have\\s+(.+?)(?:\\?|$)",
            "out of\\s+(.+?)(?:\\?|$)",
            "no\\s+(.+?)(?:\\?|$)"
        ]

        for pattern in patterns {
            if let regex = try? NSRegularExpression(pattern: pattern, options: .caseInsensitive),
               let match = regex.firstMatch(in: text, options: [], range: NSRange(text.startIndex..., in: text)),
               let range = Range(match.range(at: 1), in: text) {
                return String(text[range]).trimmingCharacters(in: .whitespaces)
            }
        }

        return nil
    }

    private func extractTechnique(from text: String) -> String? {
        let patterns = [
            "what\\s+(?:does|is)\\s+(.+?)(?:\\s+mean|\\?|$)",
            "how\\s+(?:do|to)\\s+(?:i\\s+)?(.+?)(?:\\?|$)",
            "explain\\s+(.+?)(?:\\?|$)"
        ]

        for pattern in patterns {
            if let regex = try? NSRegularExpression(pattern: pattern, options: .caseInsensitive),
               let match = regex.firstMatch(in: text, options: [], range: NSRange(text.startIndex..., in: text)),
               let range = Range(match.range(at: 1), in: text) {
                return String(text[range]).trimmingCharacters(in: .whitespaces)
            }
        }

        return nil
    }
}

// MARK: - Assistant Intent

enum AssistantIntent: Equatable {
    case nextStep
    case previousStep
    case repeatStep
    case goToStep(Int)
    case setTimer(TimeInterval)
    case cancelTimer
    case checkTimer
    case substituteIngredient(String)
    case scaleServings(Int)
    case whatIngredients
    case whatIsNext
    case howLongLeft
    case explainTechnique(String)
    case startCooking
    case pauseCooking
    case resumeCooking
    case endCooking
    case help
    case unknown(String)
}
