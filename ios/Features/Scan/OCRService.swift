import Foundation
import Vision
import UIKit

// MARK: - OCR Service

/// Performs OCR on scanned images using Vision framework
@MainActor
final class OCRService: ObservableObject {

    // MARK: - Published State

    @Published private(set) var isProcessing = false
    @Published private(set) var progress: Double = 0
    @Published private(set) var error: OCRError?

    // MARK: - Types

    struct OCRResult {
        let text: String
        let confidence: Double
        let lowConfidenceRanges: [TextRange]
        let processingTime: TimeInterval
    }

    enum OCRError: LocalizedError {
        case noTextFound
        case processingFailed(String)
        case cancelled

        var errorDescription: String? {
            switch self {
            case .noTextFound:
                return "No text was found in the scanned image."
            case .processingFailed(let message):
                return "OCR processing failed: \(message)"
            case .cancelled:
                return "OCR processing was cancelled."
            }
        }
    }

    // MARK: - OCR Processing

    /// Process multiple images and combine OCR results
    func processImages(_ images: [UIImage]) async throws -> OCRResult {
        guard !images.isEmpty else {
            throw OCRError.noTextFound
        }

        isProcessing = true
        progress = 0
        error = nil

        defer {
            isProcessing = false
            progress = 1.0
        }

        let startTime = Date()
        var allText = ""
        var totalConfidence: Double = 0
        var allLowConfidenceRanges: [TextRange] = []
        var currentPosition = 0

        for (index, image) in images.enumerated() {
            let result = try await processImage(image, startPosition: currentPosition)

            if !allText.isEmpty && !result.text.isEmpty {
                allText += "\n\n--- Page \(index + 2) ---\n\n"
                currentPosition = allText.count
            }

            allText += result.text
            currentPosition = allText.count
            totalConfidence += result.confidence
            allLowConfidenceRanges.append(contentsOf: result.lowConfidenceRanges)

            progress = Double(index + 1) / Double(images.count)
        }

        guard !allText.isEmpty else {
            throw OCRError.noTextFound
        }

        let averageConfidence = totalConfidence / Double(images.count)
        let processingTime = Date().timeIntervalSince(startTime)

        return OCRResult(
            text: allText,
            confidence: averageConfidence,
            lowConfidenceRanges: allLowConfidenceRanges,
            processingTime: processingTime
        )
    }

    /// Process a single image
    private func processImage(_ image: UIImage, startPosition: Int) async throws -> OCRResult {
        guard let cgImage = image.cgImage else {
            throw OCRError.processingFailed("Could not get CGImage from UIImage")
        }

        return try await withCheckedThrowingContinuation { continuation in
            let request = VNRecognizeTextRequest { request, error in
                if let error = error {
                    continuation.resume(throwing: OCRError.processingFailed(error.localizedDescription))
                    return
                }

                guard let observations = request.results as? [VNRecognizedTextObservation] else {
                    continuation.resume(throwing: OCRError.noTextFound)
                    return
                }

                var text = ""
                var totalConfidence: Float = 0
                var lowConfidenceRanges: [TextRange] = []
                var currentPosition = startPosition

                for observation in observations {
                    guard let candidate = observation.topCandidates(1).first else { continue }

                    let lineText = candidate.string
                    let lineConfidence = candidate.confidence

                    // Track low confidence text ranges
                    if lineConfidence < 0.7 {
                        lowConfidenceRanges.append(TextRange(
                            start: currentPosition,
                            end: currentPosition + lineText.count,
                            confidence: Double(lineConfidence)
                        ))
                    }

                    text += lineText + "\n"
                    currentPosition = startPosition + text.count
                    totalConfidence += lineConfidence
                }

                let averageConfidence = observations.isEmpty ? 0 : Double(totalConfidence / Float(observations.count))

                continuation.resume(returning: OCRResult(
                    text: text.trimmingCharacters(in: .whitespacesAndNewlines),
                    confidence: averageConfidence,
                    lowConfidenceRanges: lowConfidenceRanges,
                    processingTime: 0 // Calculated in caller
                ))
            }

            // Configure for accurate recognition
            request.recognitionLevel = .accurate
            request.usesLanguageCorrection = true
            request.recognitionLanguages = ["en-US"]

            let handler = VNImageRequestHandler(cgImage: cgImage, options: [:])

            do {
                try handler.perform([request])
            } catch {
                continuation.resume(throwing: OCRError.processingFailed(error.localizedDescription))
            }
        }
    }

    // MARK: - Text Parsing

    /// Parse OCR text into recipe components
    func parseRecipeText(_ text: String) -> ParsedRecipe {
        var title = ""
        var ingredients: [String] = []
        var instructions: [String] = []
        var currentSection: ParseSection = .unknown

        let lines = text.components(separatedBy: .newlines)
            .map { $0.trimmingCharacters(in: .whitespaces) }
            .filter { !$0.isEmpty }

        for (index, line) in lines.enumerated() {
            let lowercased = line.lowercased()

            // Detect sections
            if lowercased.contains("ingredient") {
                currentSection = .ingredients
                continue
            } else if lowercased.contains("instruction") ||
                      lowercased.contains("direction") ||
                      lowercased.contains("step") ||
                      lowercased.contains("method") {
                currentSection = .instructions
                continue
            }

            // First non-section line could be title
            if index == 0 && title.isEmpty && currentSection == .unknown {
                title = line
                continue
            }

            // Parse based on current section
            switch currentSection {
            case .ingredients:
                if isLikelyIngredient(line) {
                    ingredients.append(cleanIngredientLine(line))
                }
            case .instructions:
                if isLikelyInstruction(line) {
                    instructions.append(cleanInstructionLine(line))
                }
            case .unknown:
                // Try to guess based on content
                if isLikelyIngredient(line) {
                    ingredients.append(cleanIngredientLine(line))
                } else if isLikelyInstruction(line) {
                    instructions.append(cleanInstructionLine(line))
                }
            }
        }

        return ParsedRecipe(
            title: title,
            ingredients: ingredients,
            instructions: instructions
        )
    }

    private enum ParseSection {
        case unknown
        case ingredients
        case instructions
    }

    private func isLikelyIngredient(_ line: String) -> Bool {
        // Ingredients often start with numbers/fractions or bullet points
        let patterns = [
            "^[0-9½¼¾⅓⅔⅛⅜⅝⅞]",  // Starts with number or fraction
            "^[-•*]",              // Starts with bullet
            "cup|tablespoon|teaspoon|tbsp|tsp|oz|lb|gram|kg|ml|pinch|dash"
        ]

        for pattern in patterns {
            if line.range(of: pattern, options: [.regularExpression, .caseInsensitive]) != nil {
                return true
            }
        }
        return false
    }

    private func isLikelyInstruction(_ line: String) -> Bool {
        // Instructions are typically longer and contain action verbs
        let actionVerbs = ["mix", "stir", "add", "combine", "heat", "cook", "bake", "preheat",
                          "pour", "place", "let", "wait", "remove", "set", "prepare", "chop",
                          "slice", "dice", "fold", "whisk", "beat", "simmer", "boil", "fry"]

        let lowercased = line.lowercased()

        // Check if starts with a number (step number)
        if line.range(of: "^\\d+[.)]", options: .regularExpression) != nil {
            return true
        }

        // Check for action verbs
        for verb in actionVerbs {
            if lowercased.contains(verb) {
                return true
            }
        }

        // Long sentences are likely instructions
        return line.count > 40
    }

    private func cleanIngredientLine(_ line: String) -> String {
        var cleaned = line

        // Remove leading bullets/dashes
        cleaned = cleaned.replacingOccurrences(of: "^[-•*]\\s*", with: "", options: .regularExpression)

        return cleaned.trimmingCharacters(in: .whitespaces)
    }

    private func cleanInstructionLine(_ line: String) -> String {
        var cleaned = line

        // Remove leading step numbers
        cleaned = cleaned.replacingOccurrences(of: "^\\d+[.):]\\s*", with: "", options: .regularExpression)

        return cleaned.trimmingCharacters(in: .whitespaces)
    }
}

// MARK: - Parsed Recipe

struct ParsedRecipe {
    var title: String
    var ingredients: [String]
    var instructions: [String]

    var isEmpty: Bool {
        title.isEmpty && ingredients.isEmpty && instructions.isEmpty
    }
}
