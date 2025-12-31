import SwiftUI

// MARK: - Recipe Page View

/// Single recipe displayed as a book page with template styling
struct RecipePageView: View {
    let recipe: Recipe
    let pageNumber: Int
    let totalPages: Int

    @Environment(\.templateTokens) private var tokens
    @EnvironmentObject private var appState: AppState

    var body: some View {
        GeometryReader { geometry in
            ZStack {
                // Paper background
                paperBackground

                // Border overlay
                borderOverlay

                // Content
                ScrollView(showsIndicators: false) {
                    VStack(alignment: .leading, spacing: tokens.spacing.lg) {
                        // Header
                        recipeHeader

                        Divider()
                            .background(tokens.palette.divider)

                        // Metadata row
                        metadataRow

                        // Ingredients
                        ingredientsSection

                        // Instructions
                        instructionsSection

                        // Family memory
                        if let memory = recipe.familyMemory, !memory.isEmpty {
                            familyMemorySection(memory)
                        }

                        // Page number
                        pageNumberFooter
                    }
                    .padding(tokens.spacing.xl)
                    .padding(.top, tokens.spacing.md)
                }

                // Bookmark ribbon (if favorited)
                if isFavorited {
                    BookmarkRibbon()
                        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topTrailing)
                        .padding(.trailing, tokens.spacing.lg)
                }
            }
            .clipShape(RoundedRectangle(cornerRadius: tokens.shape.cornerRadiusMedium))
            .shadow(color: tokens.decoration.shadowStyle.shadowColor,
                   radius: tokens.decoration.shadowStyle.radius,
                   y: tokens.decoration.shadowStyle.y)
            .padding(.horizontal, tokens.spacing.md)
            .padding(.vertical, tokens.spacing.lg)
        }
    }

    // MARK: - Computed Properties

    private var isFavorited: Bool {
        guard let memberId = appState.currentMemberId else { return false }
        return recipe.favoritedBy.contains(memberId)
    }

    // MARK: - Paper Background

    private var paperBackground: some View {
        ZStack {
            // Base paper color
            tokens.palette.background

            // Paper texture overlay (subtle noise pattern)
            if tokens.decoration.ornamentStyle == .flourish {
                // Vintage: cream with subtle texture
                LinearGradient(
                    colors: [
                        tokens.palette.background,
                        tokens.palette.secondary.opacity(0.3)
                    ],
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
            }
        }
    }

    // MARK: - Border Overlay

    @ViewBuilder
    private var borderOverlay: some View {
        switch tokens.decoration.borderStyle {
        case .double:
            // Vintage double border
            ZStack {
                RoundedRectangle(cornerRadius: tokens.shape.cornerRadiusMedium)
                    .strokeBorder(tokens.palette.primary.opacity(0.3), lineWidth: 2)
                    .padding(8)

                RoundedRectangle(cornerRadius: tokens.shape.cornerRadiusMedium - 4)
                    .strokeBorder(tokens.palette.primary.opacity(0.2), lineWidth: 1)
                    .padding(16)

                // Corner flourishes
                cornerFlourishes
            }

        case .single:
            RoundedRectangle(cornerRadius: tokens.shape.cornerRadiusMedium)
                .strokeBorder(tokens.palette.divider, lineWidth: 1)
                .padding(8)

        case .dashed:
            RoundedRectangle(cornerRadius: tokens.shape.cornerRadiusMedium)
                .strokeBorder(style: StrokeStyle(lineWidth: 2, dash: [8, 4]))
                .foregroundStyle(tokens.palette.primary.opacity(0.5))
                .padding(8)

        case .none:
            EmptyView()
        }
    }

    // MARK: - Corner Flourishes

    private var cornerFlourishes: some View {
        GeometryReader { geometry in
            let size: CGFloat = 24

            // Top left
            Image(systemName: "leaf.fill")
                .font(.system(size: size))
                .foregroundStyle(tokens.palette.primary.opacity(0.2))
                .rotationEffect(.degrees(-45))
                .position(x: 24, y: 24)

            // Top right
            Image(systemName: "leaf.fill")
                .font(.system(size: size))
                .foregroundStyle(tokens.palette.primary.opacity(0.2))
                .rotationEffect(.degrees(45))
                .position(x: geometry.size.width - 24, y: 24)

            // Bottom left
            Image(systemName: "leaf.fill")
                .font(.system(size: size))
                .foregroundStyle(tokens.palette.primary.opacity(0.2))
                .rotationEffect(.degrees(-135))
                .position(x: 24, y: geometry.size.height - 24)

            // Bottom right
            Image(systemName: "leaf.fill")
                .font(.system(size: size))
                .foregroundStyle(tokens.palette.primary.opacity(0.2))
                .rotationEffect(.degrees(135))
                .position(x: geometry.size.width - 24, y: geometry.size.height - 24)
        }
    }

    // MARK: - Recipe Header

    private var recipeHeader: some View {
        VStack(alignment: .leading, spacing: tokens.spacing.sm) {
            // Category chip
            let categoryColors = CategoryColors.colors(for: recipe.category)
            HStack(spacing: tokens.spacing.xs) {
                Image(systemName: recipe.category.icon)
                    .font(.caption)
                Text(recipe.category.displayName)
                    .font(tokens.typography.labelMedium)
            }
            .foregroundStyle(categoryColors.foreground)
            .padding(.horizontal, tokens.spacing.sm)
            .padding(.vertical, tokens.spacing.xs)
            .background(categoryColors.background)
            .clipShape(Capsule())

            // Title
            Text(recipe.title)
                .font(tokens.typography.displayMedium)
                .foregroundStyle(tokens.palette.text)
                .lineLimit(3)

            // Description
            if !recipe.recipeDescription.isEmpty {
                Text(recipe.recipeDescription)
                    .font(tokens.typography.bodyMedium)
                    .foregroundStyle(tokens.palette.textSecondary)
                    .lineLimit(2)
            }
        }
    }

    // MARK: - Metadata Row

    private var metadataRow: some View {
        HStack(spacing: tokens.spacing.lg) {
            // Prep time
            metadataItem(icon: "clock", label: "Prep", value: "\(recipe.prepTimeMinutes)m")

            // Cook time
            metadataItem(icon: "flame", label: "Cook", value: "\(recipe.cookTimeMinutes)m")

            // Servings
            metadataItem(icon: "person.2", label: "Serves", value: "\(recipe.servings)")

            // Difficulty
            let difficultyColors = DifficultyColors.colors(for: recipe.difficulty)
            HStack(spacing: tokens.spacing.xs) {
                Circle()
                    .fill(difficultyColors.foreground)
                    .frame(width: 8, height: 8)
                Text(difficultyColors.label)
                    .font(tokens.typography.caption)
                    .foregroundStyle(tokens.palette.textSecondary)
            }
        }
    }

    private func metadataItem(icon: String, label: String, value: String) -> some View {
        VStack(spacing: 2) {
            Image(systemName: icon)
                .font(.caption)
                .foregroundStyle(tokens.palette.primary)
            Text(value)
                .font(tokens.typography.labelMedium)
                .foregroundStyle(tokens.palette.text)
            Text(label)
                .font(tokens.typography.caption)
                .foregroundStyle(tokens.palette.textSecondary)
        }
    }

    // MARK: - Ingredients Section

    private var ingredientsSection: some View {
        VStack(alignment: .leading, spacing: tokens.spacing.sm) {
            Text("Ingredients")
                .font(tokens.typography.titleMedium)
                .foregroundStyle(tokens.palette.text)

            VStack(alignment: .leading, spacing: tokens.spacing.xs) {
                ForEach(recipe.ingredients) { ingredient in
                    HStack(alignment: .top, spacing: tokens.spacing.sm) {
                        Circle()
                            .fill(tokens.palette.primary)
                            .frame(width: 6, height: 6)
                            .padding(.top, 6)

                        Text(ingredient.displayString)
                            .font(tokens.typography.bodyMedium)
                            .foregroundStyle(tokens.palette.text)
                    }
                }
            }
        }
    }

    // MARK: - Instructions Section

    private var instructionsSection: some View {
        VStack(alignment: .leading, spacing: tokens.spacing.sm) {
            Text("Instructions")
                .font(tokens.typography.titleMedium)
                .foregroundStyle(tokens.palette.text)

            VStack(alignment: .leading, spacing: tokens.spacing.md) {
                ForEach(recipe.instructions) { instruction in
                    HStack(alignment: .top, spacing: tokens.spacing.sm) {
                        // Step number
                        Text("\(instruction.stepNumber)")
                            .font(tokens.typography.labelLarge)
                            .foregroundStyle(tokens.palette.primary)
                            .frame(width: 24, height: 24)
                            .background(tokens.palette.secondary)
                            .clipShape(Circle())

                        VStack(alignment: .leading, spacing: tokens.spacing.xs) {
                            Text(instruction.text)
                                .font(tokens.typography.bodyMedium)
                                .foregroundStyle(tokens.palette.text)

                            // Timer hint if applicable
                            if let duration = instruction.formattedDuration {
                                HStack(spacing: tokens.spacing.xs) {
                                    Image(systemName: "timer")
                                        .font(.caption2)
                                    Text(duration)
                                        .font(tokens.typography.caption)
                                }
                                .foregroundStyle(tokens.palette.accent)
                            }
                        }
                    }
                }
            }
        }
    }

    // MARK: - Family Memory

    private func familyMemorySection(_ memory: String) -> some View {
        VStack(alignment: .leading, spacing: tokens.spacing.sm) {
            HStack(spacing: tokens.spacing.xs) {
                Image(systemName: "heart.fill")
                    .font(.caption)
                    .foregroundStyle(tokens.palette.accent)
                Text("Family Memory")
                    .font(tokens.typography.labelMedium)
                    .foregroundStyle(tokens.palette.textSecondary)
            }

            Text(memory)
                .font(tokens.typography.handwritten)
                .foregroundStyle(tokens.palette.text)
                .padding(tokens.spacing.md)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(tokens.palette.secondary.opacity(0.3))
                .clipShape(RoundedRectangle(cornerRadius: tokens.shape.cornerRadiusSmall))
        }
    }

    // MARK: - Page Number

    private var pageNumberFooter: some View {
        HStack {
            Spacer()
            Text("Page \(pageNumber) of \(totalPages)")
                .font(tokens.typography.caption)
                .foregroundStyle(tokens.palette.textSecondary)
            Spacer()
        }
        .padding(.top, tokens.spacing.lg)
    }
}

// MARK: - Preview

#Preview {
    RecipePageView(
        recipe: Recipe.sampleRecipes(familyId: UUID(), createdById: UUID()).first!,
        pageNumber: 1,
        totalPages: 5
    )
    .environmentObject(AppState())
}
