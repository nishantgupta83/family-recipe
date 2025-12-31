import SwiftUI
import SwiftData

// MARK: - Home View

struct HomeView: View {
    @Environment(\.modelContext) private var modelContext
    @Environment(\.templateTokens) private var tokens
    @EnvironmentObject private var appState: AppState

    @StateObject private var viewModel = HomeViewModel()

    @Query private var recipes: [Recipe]
    @Query private var families: [Family]

    @State private var selectedCategory: RecipeCategory?
    @State private var showingAddRecipe = false

    init() {
        // Default query - will be filtered in view
        _recipes = Query(sort: \Recipe.updatedAt, order: .reverse)
        _families = Query()
    }

    private var currentFamily: Family? {
        families.first { $0.id == appState.currentFamilyId }
    }

    private var filteredRecipes: [Recipe] {
        guard let familyId = appState.currentFamilyId else { return [] }
        let familyRecipes = recipes.filter { $0.familyId == familyId }

        if let category = selectedCategory {
            return familyRecipes.filter { $0.category == category }
        }
        return familyRecipes
    }

    private var recentRecipes: [Recipe] {
        Array(filteredRecipes.prefix(5))
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: tokens.spacing.lg) {
                    // Header
                    headerSection

                    // Category Chips
                    categoryChips

                    // Recent Recipes
                    if !recentRecipes.isEmpty {
                        recentRecipesSection
                    }

                    // All Recipes Grid
                    allRecipesSection
                }
                .padding(.horizontal, tokens.spacing.md)
            }
            .background(tokens.palette.background)
            .navigationTitle(currentFamily?.name ?? "Family Recipes")
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button {
                        showingAddRecipe = true
                    } label: {
                        Image(systemName: "plus")
                            .foregroundColor(tokens.palette.primary)
                    }
                }
            }
            .sheet(isPresented: $showingAddRecipe) {
                AddRecipeView()
            }
        }
    }

    // MARK: - Header Section

    private var headerSection: some View {
        VStack(alignment: .leading, spacing: tokens.spacing.sm) {
            HStack {
                VStack(alignment: .leading, spacing: tokens.spacing.xs) {
                    Text("Welcome back!")
                        .font(tokens.typography.bodyMedium)
                        .foregroundColor(tokens.palette.textSecondary)

                    Text("What shall we cook today?")
                        .font(tokens.typography.titleLarge)
                        .foregroundColor(tokens.palette.text)
                }

                Spacer()

                // Family icon
                if let family = currentFamily {
                    Text(family.iconEmoji)
                        .font(.system(size: 40))
                }
            }
            .padding(.top, tokens.spacing.md)
        }
    }

    // MARK: - Category Chips

    private var categoryChips: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: tokens.spacing.sm) {
                // All category
                CategoryChip(
                    title: "All",
                    isSelected: selectedCategory == nil,
                    colors: nil,
                    tokens: tokens
                ) {
                    selectedCategory = nil
                }

                // Each category
                ForEach(RecipeCategory.allCases, id: \.self) { category in
                    let colors = CategoryColors.colors(for: category)
                    CategoryChip(
                        title: category.displayName,
                        isSelected: selectedCategory == category,
                        colors: colors,
                        tokens: tokens
                    ) {
                        selectedCategory = category
                    }
                }
            }
            .padding(.horizontal, tokens.spacing.xs)
        }
    }

    // MARK: - Recent Recipes Section

    private var recentRecipesSection: some View {
        VStack(alignment: .leading, spacing: tokens.spacing.sm) {
            Text("Recent Recipes")
                .font(tokens.typography.titleMedium)
                .foregroundColor(tokens.palette.text)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: tokens.spacing.md) {
                    ForEach(recentRecipes) { recipe in
                        NavigationLink(destination: RecipeDetailView(recipe: recipe)) {
                            RecipeCard(recipe: recipe, style: .horizontal, tokens: tokens)
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
        }
    }

    // MARK: - All Recipes Section

    private var allRecipesSection: some View {
        VStack(alignment: .leading, spacing: tokens.spacing.sm) {
            HStack {
                Text(selectedCategory?.displayName ?? "All Recipes")
                    .font(tokens.typography.titleMedium)
                    .foregroundColor(tokens.palette.text)

                Spacer()

                Text("\(filteredRecipes.count) recipes")
                    .font(tokens.typography.caption)
                    .foregroundColor(tokens.palette.textSecondary)
            }

            if filteredRecipes.isEmpty {
                emptyStateView
            } else {
                LazyVGrid(columns: [
                    GridItem(.flexible()),
                    GridItem(.flexible())
                ], spacing: tokens.spacing.md) {
                    ForEach(filteredRecipes) { recipe in
                        NavigationLink(destination: RecipeDetailView(recipe: recipe)) {
                            RecipeCard(recipe: recipe, style: .vertical, tokens: tokens)
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
        }
    }

    // MARK: - Empty State

    private var emptyStateView: some View {
        VStack(spacing: tokens.spacing.md) {
            Image(systemName: "book.closed")
                .font(.system(size: 48))
                .foregroundColor(tokens.palette.textSecondary)

            Text("No recipes yet")
                .font(tokens.typography.titleMedium)
                .foregroundColor(tokens.palette.text)

            Text("Add your first family recipe to get started!")
                .font(tokens.typography.bodyMedium)
                .foregroundColor(tokens.palette.textSecondary)
                .multilineTextAlignment(.center)

            Button {
                showingAddRecipe = true
            } label: {
                Text("Add Recipe")
                    .font(tokens.typography.labelLarge)
                    .foregroundColor(.white)
                    .padding(.horizontal, tokens.spacing.lg)
                    .padding(.vertical, tokens.spacing.sm)
                    .background(tokens.palette.primary)
                    .clipShape(tokens.shape.smallRoundedRect)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, tokens.spacing.xl)
    }
}

// MARK: - Category Chip

struct CategoryChip: View {
    let title: String
    let isSelected: Bool
    let colors: CategoryColors?
    let tokens: TemplateTokens
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(tokens.typography.labelMedium)
                .foregroundColor(isSelected ?
                    (colors?.foreground ?? .white) :
                    tokens.palette.textSecondary
                )
                .padding(.horizontal, tokens.spacing.md)
                .padding(.vertical, tokens.spacing.sm)
                .background(
                    isSelected ?
                        (colors?.background ?? tokens.palette.primary) :
                        tokens.palette.surface
                )
                .clipShape(Capsule())
                .overlay(
                    Capsule()
                        .stroke(
                            isSelected ? .clear : tokens.palette.divider,
                            lineWidth: 1
                        )
                )
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Recipe Card

struct RecipeCard: View {
    let recipe: Recipe
    let style: CardStyle
    let tokens: TemplateTokens

    enum CardStyle {
        case horizontal
        case vertical
    }

    var body: some View {
        Group {
            switch style {
            case .horizontal:
                horizontalCard
            case .vertical:
                verticalCard
            }
        }
    }

    private var horizontalCard: some View {
        VStack(alignment: .leading, spacing: tokens.spacing.sm) {
            // Image placeholder
            RoundedRectangle(cornerRadius: tokens.shape.cornerRadiusMedium)
                .fill(CategoryColors.colors(for: recipe.category).background)
                .frame(width: 160, height: 100)
                .overlay(
                    Image(systemName: recipe.category.icon)
                        .font(.system(size: 32))
                        .foregroundColor(CategoryColors.colors(for: recipe.category).foreground)
                )

            VStack(alignment: .leading, spacing: tokens.spacing.xs) {
                Text(recipe.title)
                    .font(tokens.typography.labelLarge)
                    .foregroundColor(tokens.palette.text)
                    .lineLimit(2)

                HStack(spacing: tokens.spacing.xs) {
                    Image(systemName: "clock")
                        .font(.caption2)
                    Text("\(recipe.totalTimeMinutes) min")
                        .font(tokens.typography.caption)
                }
                .foregroundColor(tokens.palette.textSecondary)
            }
        }
        .frame(width: 160)
        .padding(tokens.spacing.sm)
        .background(tokens.palette.surface)
        .clipShape(tokens.shape.mediumRoundedRect)
        .shadow(
            color: tokens.decoration.shadowStyle.shadowColor,
            radius: tokens.decoration.shadowStyle.radius,
            y: tokens.decoration.shadowStyle.y
        )
    }

    private var verticalCard: some View {
        VStack(alignment: .leading, spacing: tokens.spacing.sm) {
            // Image placeholder
            RoundedRectangle(cornerRadius: tokens.shape.cornerRadiusMedium)
                .fill(CategoryColors.colors(for: recipe.category).background)
                .frame(height: 100)
                .overlay(
                    Image(systemName: recipe.category.icon)
                        .font(.system(size: 28))
                        .foregroundColor(CategoryColors.colors(for: recipe.category).foreground)
                )

            VStack(alignment: .leading, spacing: tokens.spacing.xs) {
                Text(recipe.title)
                    .font(tokens.typography.labelLarge)
                    .foregroundColor(tokens.palette.text)
                    .lineLimit(2)

                HStack {
                    // Difficulty badge
                    let diffColors = DifficultyColors.colors(for: recipe.difficulty)
                    Text(diffColors.label)
                        .font(tokens.typography.caption)
                        .foregroundColor(diffColors.foreground)
                        .padding(.horizontal, tokens.spacing.sm)
                        .padding(.vertical, 2)
                        .background(diffColors.background)
                        .clipShape(Capsule())

                    Spacer()

                    // Time
                    HStack(spacing: 2) {
                        Image(systemName: "clock")
                            .font(.caption2)
                        Text("\(recipe.totalTimeMinutes)m")
                            .font(tokens.typography.caption)
                    }
                    .foregroundColor(tokens.palette.textSecondary)
                }
            }
        }
        .padding(tokens.spacing.sm)
        .background(tokens.palette.surface)
        .clipShape(tokens.shape.mediumRoundedRect)
        .shadow(
            color: tokens.decoration.shadowStyle.shadowColor,
            radius: tokens.decoration.shadowStyle.radius,
            y: tokens.decoration.shadowStyle.y
        )
    }
}

// MARK: - Placeholder Views

struct RecipeDetailView: View {
    let recipe: Recipe

    var body: some View {
        Text("Recipe: \(recipe.title)")
            .navigationTitle(recipe.title)
    }
}

// MARK: - Preview

#Preview {
    HomeView()
        .environmentObject(AppState())
        .environment(\.templateTokens, TemplateTokens.vintage)
}
