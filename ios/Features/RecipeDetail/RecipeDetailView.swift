import SwiftUI
import SwiftData

// MARK: - Recipe Detail View

struct RecipeDetailView: View {
    @Environment(\.templateTokens) private var tokens
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject private var appState: AppState

    let recipe: Recipe

    @State private var showingCookingMode = false
    @State private var isFavorite = false

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: tokens.spacing.lg) {
                // Hero image/placeholder
                heroSection

                // Title and meta
                titleSection

                // Quick stats
                statsSection

                // Family memory
                if let memory = recipe.familyMemory, !memory.isEmpty {
                    familyMemorySection(memory)
                }

                // Ingredients
                ingredientsSection

                // Instructions
                instructionsSection

                Spacer(minLength: 100)
            }
            .padding(.horizontal, tokens.spacing.md)
        }
        .background(tokens.palette.background)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button {
                    isFavorite.toggle()
                } label: {
                    Image(systemName: isFavorite ? "heart.fill" : "heart")
                        .foregroundColor(isFavorite ? .red : tokens.palette.text)
                }
            }
        }
        .overlay(alignment: .bottom) {
            startCookingButton
        }
        .fullScreenCover(isPresented: $showingCookingMode) {
            CookingModeView(recipe: recipe)
        }
        .onAppear {
            if let memberId = appState.currentMemberId {
                isFavorite = recipe.favoritedBy.contains(memberId)
            }
        }
    }

    // MARK: - Hero Section

    private var heroSection: some View {
        let categoryColors = CategoryColors.colors(for: recipe.category)

        return ZStack {
            RoundedRectangle(cornerRadius: tokens.shape.cornerRadiusLarge)
                .fill(categoryColors.background)
                .frame(height: 200)

            VStack {
                Image(systemName: recipe.category.icon)
                    .font(.system(size: 48))
                    .foregroundColor(categoryColors.foreground)

                Text(recipe.category.displayName)
                    .font(tokens.typography.labelLarge)
                    .foregroundColor(categoryColors.foreground)
            }
        }
    }

    // MARK: - Title Section

    private var titleSection: some View {
        VStack(alignment: .leading, spacing: tokens.spacing.sm) {
            Text(recipe.title)
                .font(tokens.typography.displayMedium)
                .foregroundColor(tokens.palette.text)

            if !recipe.recipeDescription.isEmpty {
                Text(recipe.recipeDescription)
                    .font(tokens.typography.bodyMedium)
                    .foregroundColor(tokens.palette.textSecondary)
            }

            // Difficulty badge
            let diffColors = DifficultyColors.colors(for: recipe.difficulty)
            HStack {
                Text(diffColors.label)
                    .font(tokens.typography.labelMedium)
                    .foregroundColor(diffColors.foreground)
                    .padding(.horizontal, tokens.spacing.md)
                    .padding(.vertical, tokens.spacing.xs)
                    .background(diffColors.background)
                    .clipShape(Capsule())

                Spacer()
            }
        }
    }

    // MARK: - Stats Section

    private var statsSection: some View {
        HStack(spacing: tokens.spacing.lg) {
            StatItem(
                icon: "clock",
                label: "Prep",
                value: "\(recipe.prepTimeMinutes) min",
                tokens: tokens
            )

            StatItem(
                icon: "flame",
                label: "Cook",
                value: "\(recipe.cookTimeMinutes) min",
                tokens: tokens
            )

            StatItem(
                icon: "person.2",
                label: "Servings",
                value: "\(recipe.servings)",
                tokens: tokens
            )
        }
        .padding(tokens.spacing.md)
        .background(tokens.palette.surface)
        .clipShape(tokens.shape.mediumRoundedRect)
    }

    // MARK: - Family Memory

    private func familyMemorySection(_ memory: String) -> some View {
        VStack(alignment: .leading, spacing: tokens.spacing.sm) {
            HStack {
                Image(systemName: "heart.text.square")
                    .foregroundColor(tokens.palette.accent)
                Text("Family Memory")
                    .font(tokens.typography.labelLarge)
                    .foregroundColor(tokens.palette.text)
            }

            Text(memory)
                .font(tokens.typography.handwritten)
                .foregroundColor(tokens.palette.text)
                .padding(tokens.spacing.md)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(tokens.palette.secondary)
                .clipShape(tokens.shape.mediumRoundedRect)
        }
    }

    // MARK: - Ingredients Section

    private var ingredientsSection: some View {
        VStack(alignment: .leading, spacing: tokens.spacing.md) {
            Text("Ingredients")
                .font(tokens.typography.titleLarge)
                .foregroundColor(tokens.palette.text)

            VStack(alignment: .leading, spacing: tokens.spacing.sm) {
                ForEach(recipe.ingredients) { ingredient in
                    HStack(alignment: .top) {
                        Image(systemName: "circle.fill")
                            .font(.system(size: 6))
                            .foregroundColor(tokens.palette.primary)
                            .padding(.top, 8)

                        Text(ingredient.displayString)
                            .font(tokens.typography.bodyMedium)
                            .foregroundColor(tokens.palette.text)
                    }
                }
            }
            .padding(tokens.spacing.md)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(tokens.palette.surface)
            .clipShape(tokens.shape.mediumRoundedRect)
        }
    }

    // MARK: - Instructions Section

    private var instructionsSection: some View {
        VStack(alignment: .leading, spacing: tokens.spacing.md) {
            Text("Instructions")
                .font(tokens.typography.titleLarge)
                .foregroundColor(tokens.palette.text)

            VStack(alignment: .leading, spacing: tokens.spacing.md) {
                ForEach(recipe.instructions) { instruction in
                    HStack(alignment: .top, spacing: tokens.spacing.md) {
                        Text("\(instruction.stepNumber)")
                            .font(tokens.typography.labelLarge)
                            .foregroundColor(.white)
                            .frame(width: 28, height: 28)
                            .background(tokens.palette.primary)
                            .clipShape(Circle())

                        VStack(alignment: .leading, spacing: tokens.spacing.xs) {
                            Text(instruction.text)
                                .font(tokens.typography.bodyMedium)
                                .foregroundColor(tokens.palette.text)

                            if let duration = instruction.formattedDuration {
                                HStack {
                                    Image(systemName: "clock")
                                        .font(.caption)
                                    Text(duration)
                                        .font(tokens.typography.caption)
                                }
                                .foregroundColor(tokens.palette.textSecondary)
                            }
                        }
                    }
                }
            }
            .padding(tokens.spacing.md)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(tokens.palette.surface)
            .clipShape(tokens.shape.mediumRoundedRect)
        }
    }

    // MARK: - Start Cooking Button

    private var startCookingButton: some View {
        Button {
            showingCookingMode = true
        } label: {
            HStack {
                Image(systemName: "play.fill")
                Text("Start Cooking")
            }
            .font(tokens.typography.labelLarge)
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .padding()
            .background(tokens.palette.primary)
            .clipShape(tokens.shape.mediumRoundedRect)
        }
        .padding(.horizontal, tokens.spacing.md)
        .padding(.bottom, tokens.spacing.lg)
        .background(
            LinearGradient(
                colors: [tokens.palette.background.opacity(0), tokens.palette.background],
                startPoint: .top,
                endPoint: .bottom
            )
        )
    }
}

// MARK: - Stat Item

struct StatItem: View {
    let icon: String
    let label: String
    let value: String
    let tokens: TemplateTokens

    var body: some View {
        VStack(spacing: tokens.spacing.xs) {
            Image(systemName: icon)
                .font(.title3)
                .foregroundColor(tokens.palette.primary)

            Text(value)
                .font(tokens.typography.titleMedium)
                .foregroundColor(tokens.palette.text)

            Text(label)
                .font(tokens.typography.caption)
                .foregroundColor(tokens.palette.textSecondary)
        }
        .frame(maxWidth: .infinity)
    }
}
