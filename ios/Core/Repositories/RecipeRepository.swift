import Foundation
import SwiftData

// MARK: - Recipe Repository Protocol

protocol RecipeRepositoryProtocol {
    func getRecipes(familyId: UUID) async throws -> [Recipe]
    func getRecipe(id: UUID) async throws -> Recipe?
    func getRecipesByCategory(familyId: UUID, category: RecipeCategory) async throws -> [Recipe]
    func getRecentRecipes(familyId: UUID, limit: Int) async throws -> [Recipe]
    func getFavoriteRecipes(familyId: UUID, memberId: UUID) async throws -> [Recipe]
    func searchRecipes(familyId: UUID, query: String) async throws -> [Recipe]
    func createRecipe(_ recipe: Recipe) async throws
    func updateRecipe(_ recipe: Recipe) async throws
    func deleteRecipe(_ recipe: Recipe) async throws
    func toggleFavorite(recipeId: UUID, memberId: UUID) async throws
}

// MARK: - Recipe Repository Implementation

@MainActor
final class RecipeRepository: RecipeRepositoryProtocol {
    private let modelContext: ModelContext

    init(modelContext: ModelContext) {
        self.modelContext = modelContext
    }

    func getRecipes(familyId: UUID) async throws -> [Recipe] {
        let descriptor = FetchDescriptor<Recipe>(
            predicate: #Predicate { $0.familyId == familyId },
            sortBy: [SortDescriptor(\.updatedAt, order: .reverse)]
        )
        return try modelContext.fetch(descriptor)
    }

    func getRecipe(id: UUID) async throws -> Recipe? {
        let descriptor = FetchDescriptor<Recipe>(
            predicate: #Predicate { $0.id == id }
        )
        return try modelContext.fetch(descriptor).first
    }

    func getRecipesByCategory(familyId: UUID, category: RecipeCategory) async throws -> [Recipe] {
        let descriptor = FetchDescriptor<Recipe>(
            predicate: #Predicate { $0.familyId == familyId && $0.category == category },
            sortBy: [SortDescriptor(\.title)]
        )
        return try modelContext.fetch(descriptor)
    }

    func getRecentRecipes(familyId: UUID, limit: Int) async throws -> [Recipe] {
        var descriptor = FetchDescriptor<Recipe>(
            predicate: #Predicate { $0.familyId == familyId },
            sortBy: [SortDescriptor(\.createdAt, order: .reverse)]
        )
        descriptor.fetchLimit = limit
        return try modelContext.fetch(descriptor)
    }

    func getFavoriteRecipes(familyId: UUID, memberId: UUID) async throws -> [Recipe] {
        let allRecipes = try await getRecipes(familyId: familyId)
        return allRecipes.filter { $0.favoritedBy.contains(memberId) }
    }

    func searchRecipes(familyId: UUID, query: String) async throws -> [Recipe] {
        let lowercasedQuery = query.lowercased()
        let allRecipes = try await getRecipes(familyId: familyId)

        return allRecipes.filter { recipe in
            recipe.title.lowercased().contains(lowercasedQuery) ||
            recipe.recipeDescription.lowercased().contains(lowercasedQuery) ||
            recipe.tags.contains { $0.lowercased().contains(lowercasedQuery) }
        }
    }

    func createRecipe(_ recipe: Recipe) async throws {
        modelContext.insert(recipe)
        try modelContext.save()
    }

    func updateRecipe(_ recipe: Recipe) async throws {
        recipe.updatedAt = Date()
        try modelContext.save()
    }

    func deleteRecipe(_ recipe: Recipe) async throws {
        modelContext.delete(recipe)
        try modelContext.save()
    }

    func toggleFavorite(recipeId: UUID, memberId: UUID) async throws {
        guard let recipe = try await getRecipe(id: recipeId) else { return }

        if recipe.favoritedBy.contains(memberId) {
            recipe.favoritedBy.removeAll { $0 == memberId }
        } else {
            recipe.favoritedBy.append(memberId)
        }

        recipe.updatedAt = Date()
        try modelContext.save()
    }
}

// MARK: - Recipe Count by Category

extension RecipeRepository {
    func getRecipeCountByCategory(familyId: UUID) async throws -> [RecipeCategory: Int] {
        let allRecipes = try await getRecipes(familyId: familyId)
        var counts: [RecipeCategory: Int] = [:]

        for category in RecipeCategory.allCases {
            counts[category] = allRecipes.filter { $0.category == category }.count
        }

        return counts
    }

    func getTotalRecipeCount(familyId: UUID) async throws -> Int {
        try await getRecipes(familyId: familyId).count
    }
}
