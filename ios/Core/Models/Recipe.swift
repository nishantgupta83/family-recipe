import Foundation
import SwiftData

// MARK: - Recipe Model

@Model
final class Recipe {
    @Attribute(.unique) var id: UUID
    var title: String
    var recipeDescription: String
    var ingredients: [Ingredient]
    var instructions: [Instruction]
    var category: RecipeCategory
    var difficulty: Difficulty
    var prepTimeMinutes: Int
    var cookTimeMinutes: Int
    var servings: Int

    // Family-specific
    var familyId: UUID
    var createdById: UUID
    var familyMemory: String?
    var favoritedBy: [UUID]
    var imageUrl: String?
    var tags: [String]

    // Timestamps
    var createdAt: Date
    var updatedAt: Date

    // Sync
    var syncStatus: SyncStatus

    // Scanned source (when imported via OCR)
    var scannedSource: ScannedSource?

    // Computed
    var totalTimeMinutes: Int {
        prepTimeMinutes + cookTimeMinutes
    }

    var isFavorited: Bool {
        // Will be set based on current member
        false
    }

    init(
        id: UUID = UUID(),
        title: String,
        recipeDescription: String = "",
        ingredients: [Ingredient] = [],
        instructions: [Instruction] = [],
        category: RecipeCategory = .dinner,
        difficulty: Difficulty = .medium,
        prepTimeMinutes: Int = 15,
        cookTimeMinutes: Int = 30,
        servings: Int = 4,
        familyId: UUID,
        createdById: UUID,
        familyMemory: String? = nil,
        favoritedBy: [UUID] = [],
        imageUrl: String? = nil,
        tags: [String] = [],
        createdAt: Date = Date(),
        updatedAt: Date = Date(),
        syncStatus: SyncStatus = .local,
        scannedSource: ScannedSource? = nil
    ) {
        self.id = id
        self.title = title
        self.recipeDescription = recipeDescription
        self.ingredients = ingredients
        self.instructions = instructions
        self.category = category
        self.difficulty = difficulty
        self.prepTimeMinutes = prepTimeMinutes
        self.cookTimeMinutes = cookTimeMinutes
        self.servings = servings
        self.familyId = familyId
        self.createdById = createdById
        self.familyMemory = familyMemory
        self.favoritedBy = favoritedBy
        self.imageUrl = imageUrl
        self.tags = tags
        self.createdAt = createdAt
        self.updatedAt = updatedAt
        self.syncStatus = syncStatus
        self.scannedSource = scannedSource
    }
}

// MARK: - Scanned Source (OCR Import Data)

struct ScannedSource: Codable, Equatable {
    let images: [String]           // URLs to original scanned images (preserved forever)
    let rawOCRText: String         // Raw extracted text before parsing
    let importedAt: Date
    let confidenceScore: Double?   // Overall OCR confidence (0-1)
    let lowConfidenceRanges: [TextRange]  // Ranges with low OCR confidence

    init(
        images: [String] = [],
        rawOCRText: String,
        importedAt: Date = Date(),
        confidenceScore: Double? = nil,
        lowConfidenceRanges: [TextRange] = []
    ) {
        self.images = images
        self.rawOCRText = rawOCRText
        self.importedAt = importedAt
        self.confidenceScore = confidenceScore
        self.lowConfidenceRanges = lowConfidenceRanges
    }
}

struct TextRange: Codable, Equatable, Identifiable {
    let id: UUID
    let start: Int
    let end: Int
    let confidence: Double

    init(id: UUID = UUID(), start: Int, end: Int, confidence: Double) {
        self.id = id
        self.start = start
        self.end = end
        self.confidence = confidence
    }

    var isLowConfidence: Bool {
        confidence < 0.7
    }
}

// MARK: - Ingredient

struct Ingredient: Codable, Identifiable, Equatable, Hashable {
    let id: UUID
    var name: String
    var amount: Double
    var unit: MeasurementUnit
    var notes: String?
    var isOptional: Bool

    init(
        id: UUID = UUID(),
        name: String,
        amount: Double,
        unit: MeasurementUnit,
        notes: String? = nil,
        isOptional: Bool = false
    ) {
        self.id = id
        self.name = name
        self.amount = amount
        self.unit = unit
        self.notes = notes
        self.isOptional = isOptional
    }

    var displayString: String {
        let amountStr = amount.truncatingRemainder(dividingBy: 1) == 0
            ? String(format: "%.0f", amount)
            : String(format: "%.1f", amount)

        let unitStr = unit == .toTaste ? "" : " \(unit.abbreviation)"
        let notesStr = notes.map { " (\($0))" } ?? ""
        let optionalStr = isOptional ? " (optional)" : ""

        return "\(amountStr)\(unitStr) \(name)\(notesStr)\(optionalStr)"
    }
}

// MARK: - Instruction

struct Instruction: Codable, Identifiable, Equatable, Hashable {
    let id: UUID
    var stepNumber: Int
    var text: String
    var durationSeconds: Int?
    var imageUrl: String?

    init(
        id: UUID = UUID(),
        stepNumber: Int,
        text: String,
        durationSeconds: Int? = nil,
        imageUrl: String? = nil
    ) {
        self.id = id
        self.stepNumber = stepNumber
        self.text = text
        self.durationSeconds = durationSeconds
        self.imageUrl = imageUrl
    }

    var hasTiming: Bool {
        durationSeconds != nil && durationSeconds! > 0
    }

    var formattedDuration: String? {
        guard let seconds = durationSeconds, seconds > 0 else { return nil }

        if seconds < 60 {
            return "\(seconds) seconds"
        } else if seconds < 3600 {
            let minutes = seconds / 60
            return "\(minutes) minute\(minutes == 1 ? "" : "s")"
        } else {
            let hours = seconds / 3600
            let minutes = (seconds % 3600) / 60
            if minutes == 0 {
                return "\(hours) hour\(hours == 1 ? "" : "s")"
            }
            return "\(hours) hour\(hours == 1 ? "" : "s") \(minutes) min"
        }
    }
}

// MARK: - Enums

enum RecipeCategory: String, Codable, CaseIterable {
    case breakfast
    case brunch
    case lunch
    case dinner
    case appetizer
    case snack
    case dessert
    case beverage
    case side

    var displayName: String {
        rawValue.capitalized
    }

    var icon: String {
        switch self {
        case .breakfast: return "sunrise"
        case .brunch: return "sun.and.horizon"
        case .lunch: return "leaf"
        case .dinner: return "moon.stars"
        case .appetizer: return "fork.knife"
        case .snack: return "carrot"
        case .dessert: return "birthday.cake"
        case .beverage: return "cup.and.saucer"
        case .side: return "leaf.circle"
        }
    }
}

enum Difficulty: String, Codable, CaseIterable {
    case easy
    case medium
    case hard

    var displayName: String {
        switch self {
        case .easy: return "Easy"
        case .medium: return "Medium"
        case .hard: return "Challenging"
        }
    }
}

enum MeasurementUnit: String, Codable, CaseIterable {
    // Volume
    case cup
    case tablespoon
    case teaspoon
    case fluidOunce
    case milliliter
    case liter

    // Weight
    case gram
    case kilogram
    case ounce
    case pound

    // Count
    case piece
    case slice
    case clove
    case pinch
    case dash
    case toTaste

    var abbreviation: String {
        switch self {
        case .cup: return "cup"
        case .tablespoon: return "tbsp"
        case .teaspoon: return "tsp"
        case .fluidOunce: return "fl oz"
        case .milliliter: return "ml"
        case .liter: return "L"
        case .gram: return "g"
        case .kilogram: return "kg"
        case .ounce: return "oz"
        case .pound: return "lb"
        case .piece: return "pc"
        case .slice: return "slice"
        case .clove: return "clove"
        case .pinch: return "pinch"
        case .dash: return "dash"
        case .toTaste: return "to taste"
        }
    }
}

enum SyncStatus: String, Codable {
    case local
    case synced
    case pendingUpload
    case pendingDownload
    case conflict
}

// MARK: - Sample Data

extension Recipe {
    static func sampleRecipes(familyId: UUID, createdById: UUID) -> [Recipe] {
        [
            Recipe(
                title: "Grandma's Chocolate Chip Cookies",
                recipeDescription: "The most delicious chocolate chip cookies - a family favorite for generations!",
                ingredients: [
                    Ingredient(name: "all-purpose flour", amount: 2, unit: .cup),
                    Ingredient(name: "butter, softened", amount: 1, unit: .cup),
                    Ingredient(name: "granulated sugar", amount: 0.75, unit: .cup),
                    Ingredient(name: "brown sugar", amount: 0.75, unit: .cup),
                    Ingredient(name: "eggs", amount: 2, unit: .piece),
                    Ingredient(name: "vanilla extract", amount: 1, unit: .teaspoon),
                    Ingredient(name: "baking soda", amount: 1, unit: .teaspoon),
                    Ingredient(name: "salt", amount: 1, unit: .teaspoon),
                    Ingredient(name: "chocolate chips", amount: 2, unit: .cup)
                ],
                instructions: [
                    Instruction(stepNumber: 1, text: "Preheat oven to 375°F (190°C)."),
                    Instruction(stepNumber: 2, text: "Mix flour, baking soda, and salt in a bowl. Set aside."),
                    Instruction(stepNumber: 3, text: "Cream butter and both sugars until light and fluffy, about 3 minutes."),
                    Instruction(stepNumber: 4, text: "Beat in eggs one at a time, then add vanilla."),
                    Instruction(stepNumber: 5, text: "Gradually mix in flour mixture until just combined."),
                    Instruction(stepNumber: 6, text: "Stir in chocolate chips."),
                    Instruction(stepNumber: 7, text: "Drop rounded tablespoons onto ungreased baking sheets."),
                    Instruction(stepNumber: 8, text: "Bake 9-11 minutes until golden brown.", durationSeconds: 600),
                    Instruction(stepNumber: 9, text: "Cool on baking sheet for 2 minutes, then transfer to wire rack.")
                ],
                category: .dessert,
                difficulty: .easy,
                prepTimeMinutes: 15,
                cookTimeMinutes: 10,
                servings: 24,
                familyId: familyId,
                createdById: createdById,
                familyMemory: "Grandma used to make these every Sunday. The secret is using room temperature butter!"
            ),
            Recipe(
                title: "Mom's Classic Pasta",
                recipeDescription: "Simple, comforting pasta with garlic and olive oil.",
                ingredients: [
                    Ingredient(name: "spaghetti", amount: 1, unit: .pound),
                    Ingredient(name: "olive oil", amount: 0.25, unit: .cup),
                    Ingredient(name: "garlic cloves, minced", amount: 4, unit: .clove),
                    Ingredient(name: "red pepper flakes", amount: 0.5, unit: .teaspoon, isOptional: true),
                    Ingredient(name: "fresh parsley, chopped", amount: 0.25, unit: .cup),
                    Ingredient(name: "parmesan cheese", amount: 0.5, unit: .cup),
                    Ingredient(name: "salt", amount: 1, unit: .pinch, notes: "to taste")
                ],
                instructions: [
                    Instruction(stepNumber: 1, text: "Bring a large pot of salted water to boil."),
                    Instruction(stepNumber: 2, text: "Cook pasta according to package directions. Reserve 1 cup pasta water before draining."),
                    Instruction(stepNumber: 3, text: "While pasta cooks, heat olive oil in a large skillet over medium heat."),
                    Instruction(stepNumber: 4, text: "Add garlic and red pepper flakes. Cook until fragrant, about 1 minute."),
                    Instruction(stepNumber: 5, text: "Add drained pasta to skillet. Toss with oil and garlic."),
                    Instruction(stepNumber: 6, text: "Add pasta water as needed to create a light sauce."),
                    Instruction(stepNumber: 7, text: "Remove from heat. Add parsley and parmesan. Toss and serve.")
                ],
                category: .dinner,
                difficulty: .easy,
                prepTimeMinutes: 5,
                cookTimeMinutes: 15,
                servings: 4,
                familyId: familyId,
                createdById: createdById,
                familyMemory: "Mom made this on busy weeknights. We'd fight over who got the crispy garlic bits!"
            )
        ]
    }
}
