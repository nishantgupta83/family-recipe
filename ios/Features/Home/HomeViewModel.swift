import Foundation
import SwiftData
import Combine

// MARK: - Home View Model

@MainActor
class HomeViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var error: Error?
    @Published var selectedCategory: RecipeCategory?
    @Published var searchQuery = ""

    private var cancellables = Set<AnyCancellable>()

    init() {
        setupSearchDebounce()
    }

    private func setupSearchDebounce() {
        $searchQuery
            .debounce(for: .milliseconds(300), scheduler: RunLoop.main)
            .removeDuplicates()
            .sink { [weak self] query in
                self?.performSearch(query: query)
            }
            .store(in: &cancellables)
    }

    private func performSearch(query: String) {
        // Search logic will be handled by SwiftData Query
        // This is here for future extensibility
    }

    func selectCategory(_ category: RecipeCategory?) {
        selectedCategory = category
    }

    func clearError() {
        error = nil
    }
}

// MARK: - Recipe Statistics

struct RecipeStatistics {
    let totalCount: Int
    let categoryBreakdown: [RecipeCategory: Int]
    let recentCount: Int
    let favoriteCount: Int

    static let empty = RecipeStatistics(
        totalCount: 0,
        categoryBreakdown: [:],
        recentCount: 0,
        favoriteCount: 0
    )
}

extension HomeViewModel {
    func calculateStatistics(recipes: [Recipe], memberId: UUID) -> RecipeStatistics {
        var categoryBreakdown: [RecipeCategory: Int] = [:]

        for category in RecipeCategory.allCases {
            categoryBreakdown[category] = recipes.filter { $0.category == category }.count
        }

        let favoriteCount = recipes.filter { $0.favoritedBy.contains(memberId) }.count

        return RecipeStatistics(
            totalCount: recipes.count,
            categoryBreakdown: categoryBreakdown,
            recentCount: min(recipes.count, 5),
            favoriteCount: favoriteCount
        )
    }
}
