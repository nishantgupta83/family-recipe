import SwiftUI
import SwiftData

// MARK: - Review OCR View

/// Review and edit OCR-parsed recipe before saving
/// Rule: OCR output is NEVER auto-saved without review
struct ReviewOCRView: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(\.templateTokens) private var tokens
    @Environment(\.modelContext) private var modelContext

    @ObservedObject var viewModel: ScanRecipeViewModel

    let familyId: UUID
    let createdById: UUID

    // Editable state
    @State private var title: String = ""
    @State private var ingredients: [EditableIngredient] = []
    @State private var instructions: [EditableInstruction] = []
    @State private var selectedCategory: RecipeCategory = .dinner
    @State private var selectedDifficulty: Difficulty = .medium
    @State private var prepTime: Int = 15
    @State private var cookTime: Int = 30
    @State private var servings: Int = 4
    @State private var familyMemory: String = ""

    @State private var showRawText = false
    @State private var isSaving = false

    var body: some View {
        ZStack {
            tokens.palette.background
                .ignoresSafeArea()

            ScrollView {
                VStack(spacing: tokens.spacing.lg) {
                    // Confidence banner
                    if let result = viewModel.ocrResult {
                        confidenceBanner(confidence: result.confidence)
                    }

                    // Title section
                    titleSection

                    Divider()
                        .background(tokens.palette.divider)

                    // Ingredients section
                    ingredientsSection

                    Divider()
                        .background(tokens.palette.divider)

                    // Instructions section
                    instructionsSection

                    Divider()
                        .background(tokens.palette.divider)

                    // Metadata section
                    metadataSection

                    // Family memory
                    familyMemorySection

                    // Raw OCR text toggle
                    rawTextSection

                    Spacer(minLength: 100)
                }
                .padding(tokens.spacing.lg)
            }

            // Save button overlay
            VStack {
                Spacer()
                saveButton
            }
        }
        .navigationTitle("Review Recipe")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button("Edit Raw") {
                    showRawText.toggle()
                }
                .font(tokens.typography.labelMedium)
            }
        }
        .onAppear {
            loadParsedData()
        }
    }

    // MARK: - Sections

    private func confidenceBanner(confidence: Double) -> some View {
        let isLow = confidence < 0.7
        let color = isLow ? tokens.palette.error : tokens.palette.success

        return HStack(spacing: tokens.spacing.sm) {
            Image(systemName: isLow ? "exclamationmark.triangle.fill" : "checkmark.circle.fill")
                .foregroundStyle(color)

            VStack(alignment: .leading, spacing: 2) {
                Text(isLow ? "Review carefully" : "Good scan quality")
                    .font(tokens.typography.labelMedium)
                    .foregroundStyle(tokens.palette.text)

                Text("OCR Confidence: \(Int(confidence * 100))%")
                    .font(tokens.typography.caption)
                    .foregroundStyle(tokens.palette.textSecondary)
            }

            Spacer()
        }
        .padding(tokens.spacing.md)
        .background(color.opacity(0.1))
        .clipShape(tokens.shape.mediumRoundedRect)
    }

    private var titleSection: some View {
        VStack(alignment: .leading, spacing: tokens.spacing.sm) {
            sectionHeader("Recipe Title", required: true)

            TextField("Enter recipe title", text: $title)
                .font(tokens.typography.titleMedium)
                .textFieldStyle(.plain)
                .padding(tokens.spacing.md)
                .background(tokens.palette.surface)
                .clipShape(tokens.shape.smallRoundedRect)
                .overlay(
                    tokens.shape.smallRoundedRect
                        .stroke(tokens.palette.divider, lineWidth: 1)
                )
        }
    }

    private var ingredientsSection: some View {
        VStack(alignment: .leading, spacing: tokens.spacing.sm) {
            HStack {
                sectionHeader("Ingredients", required: true)
                Spacer()
                Button {
                    addIngredient()
                } label: {
                    Image(systemName: "plus.circle.fill")
                        .foregroundStyle(tokens.palette.primary)
                }
            }

            ForEach($ingredients) { $ingredient in
                ingredientRow(ingredient: $ingredient)
            }

            if ingredients.isEmpty {
                emptyState("No ingredients detected. Tap + to add.")
            }
        }
    }

    private func ingredientRow(ingredient: Binding<EditableIngredient>) -> some View {
        HStack(spacing: tokens.spacing.sm) {
            // Low confidence indicator
            if ingredient.wrappedValue.hasLowConfidence {
                Circle()
                    .fill(tokens.palette.error.opacity(0.6))
                    .frame(width: 8, height: 8)
            }

            TextField("Ingredient", text: ingredient.text)
                .font(tokens.typography.bodyMedium)
                .textFieldStyle(.plain)

            Button {
                removeIngredient(ingredient.wrappedValue)
            } label: {
                Image(systemName: "xmark.circle.fill")
                    .foregroundStyle(tokens.palette.textSecondary)
            }
        }
        .padding(tokens.spacing.sm)
        .background(
            ingredient.wrappedValue.hasLowConfidence
                ? tokens.palette.error.opacity(0.05)
                : tokens.palette.surface
        )
        .clipShape(tokens.shape.smallRoundedRect)
        .overlay(
            tokens.shape.smallRoundedRect
                .stroke(
                    ingredient.wrappedValue.hasLowConfidence
                        ? tokens.palette.error.opacity(0.3)
                        : tokens.palette.divider,
                    lineWidth: 1
                )
        )
    }

    private var instructionsSection: some View {
        VStack(alignment: .leading, spacing: tokens.spacing.sm) {
            HStack {
                sectionHeader("Instructions", required: true)
                Spacer()
                Button {
                    addInstruction()
                } label: {
                    Image(systemName: "plus.circle.fill")
                        .foregroundStyle(tokens.palette.primary)
                }
            }

            ForEach($instructions) { $instruction in
                instructionRow(instruction: $instruction)
            }

            if instructions.isEmpty {
                emptyState("No instructions detected. Tap + to add.")
            }
        }
    }

    private func instructionRow(instruction: Binding<EditableInstruction>) -> some View {
        HStack(alignment: .top, spacing: tokens.spacing.sm) {
            // Step number
            Text("\(instruction.wrappedValue.stepNumber)")
                .font(tokens.typography.labelLarge)
                .foregroundStyle(tokens.palette.primary)
                .frame(width: 24, height: 24)
                .background(tokens.palette.secondary)
                .clipShape(Circle())

            // Low confidence indicator
            if instruction.wrappedValue.hasLowConfidence {
                Circle()
                    .fill(tokens.palette.error.opacity(0.6))
                    .frame(width: 8, height: 8)
            }

            TextField("Instruction", text: instruction.text, axis: .vertical)
                .font(tokens.typography.bodyMedium)
                .textFieldStyle(.plain)
                .lineLimit(1...5)

            Button {
                removeInstruction(instruction.wrappedValue)
            } label: {
                Image(systemName: "xmark.circle.fill")
                    .foregroundStyle(tokens.palette.textSecondary)
            }
        }
        .padding(tokens.spacing.sm)
        .background(
            instruction.wrappedValue.hasLowConfidence
                ? tokens.palette.error.opacity(0.05)
                : tokens.palette.surface
        )
        .clipShape(tokens.shape.smallRoundedRect)
        .overlay(
            tokens.shape.smallRoundedRect
                .stroke(
                    instruction.wrappedValue.hasLowConfidence
                        ? tokens.palette.error.opacity(0.3)
                        : tokens.palette.divider,
                    lineWidth: 1
                )
        )
    }

    private var metadataSection: some View {
        VStack(alignment: .leading, spacing: tokens.spacing.md) {
            sectionHeader("Details", required: false)

            // Category picker
            HStack {
                Text("Category")
                    .font(tokens.typography.bodyMedium)
                    .foregroundStyle(tokens.palette.textSecondary)
                Spacer()
                Picker("Category", selection: $selectedCategory) {
                    ForEach(RecipeCategory.allCases, id: \.self) { category in
                        Text(category.displayName).tag(category)
                    }
                }
                .pickerStyle(.menu)
            }

            // Difficulty picker
            HStack {
                Text("Difficulty")
                    .font(tokens.typography.bodyMedium)
                    .foregroundStyle(tokens.palette.textSecondary)
                Spacer()
                Picker("Difficulty", selection: $selectedDifficulty) {
                    ForEach(Difficulty.allCases, id: \.self) { difficulty in
                        Text(difficulty.displayName).tag(difficulty)
                    }
                }
                .pickerStyle(.menu)
            }

            // Time pickers
            HStack(spacing: tokens.spacing.lg) {
                VStack(alignment: .leading) {
                    Text("Prep Time")
                        .font(tokens.typography.caption)
                        .foregroundStyle(tokens.palette.textSecondary)
                    Stepper("\(prepTime) min", value: $prepTime, in: 0...480, step: 5)
                        .font(tokens.typography.bodyMedium)
                }

                VStack(alignment: .leading) {
                    Text("Cook Time")
                        .font(tokens.typography.caption)
                        .foregroundStyle(tokens.palette.textSecondary)
                    Stepper("\(cookTime) min", value: $cookTime, in: 0...480, step: 5)
                        .font(tokens.typography.bodyMedium)
                }
            }

            // Servings
            HStack {
                Text("Servings")
                    .font(tokens.typography.bodyMedium)
                    .foregroundStyle(tokens.palette.textSecondary)
                Spacer()
                Stepper("\(servings)", value: $servings, in: 1...50)
                    .font(tokens.typography.bodyMedium)
            }
        }
        .padding(tokens.spacing.md)
        .background(tokens.palette.surface)
        .clipShape(tokens.shape.mediumRoundedRect)
    }

    private var familyMemorySection: some View {
        VStack(alignment: .leading, spacing: tokens.spacing.sm) {
            sectionHeader("Family Memory", required: false)
            Text("Add a personal note about this recipe")
                .font(tokens.typography.caption)
                .foregroundStyle(tokens.palette.textSecondary)

            TextField("e.g., Grandma used to make this every Sunday...", text: $familyMemory, axis: .vertical)
                .font(tokens.typography.handwritten)
                .textFieldStyle(.plain)
                .lineLimit(2...4)
                .padding(tokens.spacing.md)
                .background(tokens.palette.secondary.opacity(0.3))
                .clipShape(tokens.shape.smallRoundedRect)
        }
    }

    private var rawTextSection: some View {
        VStack(alignment: .leading, spacing: tokens.spacing.sm) {
            Button {
                withAnimation {
                    showRawText.toggle()
                }
            } label: {
                HStack {
                    Text("Raw OCR Text")
                        .font(tokens.typography.labelMedium)
                        .foregroundStyle(tokens.palette.textSecondary)
                    Spacer()
                    Image(systemName: showRawText ? "chevron.up" : "chevron.down")
                        .foregroundStyle(tokens.palette.textSecondary)
                }
            }

            if showRawText {
                Text(viewModel.rawOCRText)
                    .font(.system(.caption, design: .monospaced))
                    .foregroundStyle(tokens.palette.textSecondary)
                    .padding(tokens.spacing.md)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(tokens.palette.surface)
                    .clipShape(tokens.shape.smallRoundedRect)
            }
        }
    }

    private var saveButton: some View {
        Button {
            saveRecipe()
        } label: {
            HStack(spacing: tokens.spacing.sm) {
                if isSaving {
                    ProgressView()
                        .tint(.white)
                } else {
                    Image(systemName: "checkmark.circle.fill")
                }
                Text("Save Recipe")
            }
            .font(tokens.typography.labelLarge)
            .foregroundStyle(.white)
            .frame(maxWidth: .infinity)
            .padding(.vertical, tokens.spacing.md)
            .background(canSave ? tokens.palette.primary : tokens.palette.textSecondary)
            .clipShape(tokens.shape.mediumRoundedRect)
        }
        .disabled(!canSave || isSaving)
        .padding(tokens.spacing.lg)
        .background(
            LinearGradient(
                colors: [tokens.palette.background.opacity(0), tokens.palette.background],
                startPoint: .top,
                endPoint: .bottom
            )
        )
    }

    // MARK: - Helpers

    private func sectionHeader(_ text: String, required: Bool) -> some View {
        HStack(spacing: tokens.spacing.xs) {
            Text(text)
                .font(tokens.typography.labelLarge)
                .foregroundStyle(tokens.palette.text)

            if required {
                Text("*")
                    .foregroundStyle(tokens.palette.error)
            }
        }
    }

    private func emptyState(_ message: String) -> some View {
        Text(message)
            .font(tokens.typography.caption)
            .foregroundStyle(tokens.palette.textSecondary)
            .padding(tokens.spacing.md)
            .frame(maxWidth: .infinity)
            .background(tokens.palette.surface)
            .clipShape(tokens.shape.smallRoundedRect)
    }

    private var canSave: Bool {
        !title.trimmingCharacters(in: .whitespaces).isEmpty &&
        !ingredients.isEmpty &&
        !instructions.isEmpty
    }

    // MARK: - Actions

    private func loadParsedData() {
        guard let parsed = viewModel.parsedRecipe else { return }

        title = parsed.title

        // Convert strings to editable ingredients
        ingredients = parsed.ingredients.enumerated().map { index, text in
            EditableIngredient(
                id: UUID(),
                text: text,
                hasLowConfidence: checkLowConfidence(for: text)
            )
        }

        // Convert strings to editable instructions
        instructions = parsed.instructions.enumerated().map { index, text in
            EditableInstruction(
                id: UUID(),
                stepNumber: index + 1,
                text: text,
                hasLowConfidence: checkLowConfidence(for: text)
            )
        }
    }

    private func checkLowConfidence(for text: String) -> Bool {
        guard let result = viewModel.ocrResult else { return false }

        // Check if any low confidence range overlaps with this text
        let fullText = viewModel.rawOCRText
        guard let range = fullText.range(of: text) else { return false }

        let start = fullText.distance(from: fullText.startIndex, to: range.lowerBound)
        let end = fullText.distance(from: fullText.startIndex, to: range.upperBound)

        return result.lowConfidenceRanges.contains { textRange in
            textRange.start < end && textRange.end > start
        }
    }

    private func addIngredient() {
        ingredients.append(EditableIngredient(id: UUID(), text: "", hasLowConfidence: false))
    }

    private func removeIngredient(_ ingredient: EditableIngredient) {
        ingredients.removeAll { $0.id == ingredient.id }
    }

    private func addInstruction() {
        let nextStep = (instructions.map(\.stepNumber).max() ?? 0) + 1
        instructions.append(EditableInstruction(
            id: UUID(),
            stepNumber: nextStep,
            text: "",
            hasLowConfidence: false
        ))
    }

    private func removeInstruction(_ instruction: EditableInstruction) {
        instructions.removeAll { $0.id == instruction.id }
        // Renumber remaining instructions
        for (index, _) in instructions.enumerated() {
            instructions[index].stepNumber = index + 1
        }
    }

    private func saveRecipe() {
        isSaving = true

        // Convert editable items to model items
        let recipeIngredients = ingredients.map { editable in
            parseIngredient(from: editable.text)
        }

        let recipeInstructions = instructions.enumerated().map { index, editable in
            Instruction(
                stepNumber: index + 1,
                text: editable.text
            )
        }

        // Create scanned source
        var scannedSource: ScannedSource?
        if let result = viewModel.ocrResult {
            scannedSource = ScannedSource(
                images: [], // Images would be saved to local storage
                rawOCRText: viewModel.rawOCRText,
                confidenceScore: result.confidence,
                lowConfidenceRanges: result.lowConfidenceRanges
            )
        }

        // Create recipe
        let recipe = Recipe(
            title: title.trimmingCharacters(in: .whitespaces),
            recipeDescription: "",
            ingredients: recipeIngredients,
            instructions: recipeInstructions,
            category: selectedCategory,
            difficulty: selectedDifficulty,
            prepTimeMinutes: prepTime,
            cookTimeMinutes: cookTime,
            servings: servings,
            familyId: familyId,
            createdById: createdById,
            familyMemory: familyMemory.isEmpty ? nil : familyMemory,
            scannedSource: scannedSource
        )

        // Save to database
        modelContext.insert(recipe)

        do {
            try modelContext.save()
            isSaving = false
            dismiss()
        } catch {
            isSaving = false
            // Handle error
        }
    }

    private func parseIngredient(from text: String) -> Ingredient {
        // Simple parsing - try to extract amount and unit
        let pattern = "^([0-9½¼¾⅓⅔⅛⅜⅝⅞/\\.]+)?\\s*([a-zA-Z]+\\.?)?\\s*(.+)$"

        if let regex = try? NSRegularExpression(pattern: pattern),
           let match = regex.firstMatch(in: text, range: NSRange(text.startIndex..., in: text)) {

            let amountString = match.range(at: 1).location != NSNotFound
                ? String(text[Range(match.range(at: 1), in: text)!])
                : "1"

            let unitString = match.range(at: 2).location != NSNotFound
                ? String(text[Range(match.range(at: 2), in: text)!])
                : ""

            let name = match.range(at: 3).location != NSNotFound
                ? String(text[Range(match.range(at: 3), in: text)!])
                : text

            let amount = parseAmount(amountString)
            let unit = parseUnit(unitString)

            return Ingredient(
                name: name.trimmingCharacters(in: .whitespaces),
                amount: amount,
                unit: unit
            )
        }

        // Fallback
        return Ingredient(name: text, amount: 1, unit: .piece)
    }

    private func parseAmount(_ string: String) -> Double {
        let fractionMap: [String: Double] = [
            "½": 0.5, "¼": 0.25, "¾": 0.75,
            "⅓": 0.333, "⅔": 0.667,
            "⅛": 0.125, "⅜": 0.375, "⅝": 0.625, "⅞": 0.875
        ]

        for (fraction, value) in fractionMap {
            if string.contains(fraction) {
                let whole = string.replacingOccurrences(of: fraction, with: "")
                    .trimmingCharacters(in: .whitespaces)
                let wholeValue = Double(whole) ?? 0
                return wholeValue + value
            }
        }

        // Handle x/y fractions
        if string.contains("/") {
            let parts = string.split(separator: "/")
            if parts.count == 2,
               let numerator = Double(parts[0]),
               let denominator = Double(parts[1]),
               denominator != 0 {
                return numerator / denominator
            }
        }

        return Double(string) ?? 1
    }

    private func parseUnit(_ string: String) -> MeasurementUnit {
        let unitMap: [String: MeasurementUnit] = [
            "cup": .cup, "cups": .cup, "c": .cup,
            "tbsp": .tablespoon, "tablespoon": .tablespoon, "tablespoons": .tablespoon,
            "tsp": .teaspoon, "teaspoon": .teaspoon, "teaspoons": .teaspoon,
            "oz": .ounce, "ounce": .ounce, "ounces": .ounce,
            "lb": .pound, "lbs": .pound, "pound": .pound, "pounds": .pound,
            "g": .gram, "gram": .gram, "grams": .gram,
            "kg": .kilogram, "kilogram": .kilogram,
            "ml": .milliliter, "milliliter": .milliliter,
            "l": .liter, "liter": .liter,
            "clove": .clove, "cloves": .clove,
            "pinch": .pinch, "dash": .dash
        ]

        return unitMap[string.lowercased()] ?? .piece
    }
}

// MARK: - Editable Models

struct EditableIngredient: Identifiable {
    let id: UUID
    var text: String
    var hasLowConfidence: Bool
}

struct EditableInstruction: Identifiable {
    let id: UUID
    var stepNumber: Int
    var text: String
    var hasLowConfidence: Bool
}

// MARK: - Preview

#Preview {
    NavigationStack {
        ReviewOCRView(
            viewModel: {
                let vm = ScanRecipeViewModel()
                vm.rawOCRText = """
                Grandma's Pancakes

                Ingredients:
                2 cups flour
                1 tbsp sugar
                2 eggs
                1½ cups milk

                Instructions:
                1. Mix dry ingredients
                2. Add wet ingredients
                3. Cook on griddle until golden
                """
                vm.parsedRecipe = ParsedRecipe(
                    title: "Grandma's Pancakes",
                    ingredients: ["2 cups flour", "1 tbsp sugar", "2 eggs", "1½ cups milk"],
                    instructions: ["Mix dry ingredients", "Add wet ingredients", "Cook on griddle until golden"]
                )
                return vm
            }(),
            familyId: UUID(),
            createdById: UUID()
        )
    }
    .environmentObject(AppState())
}
