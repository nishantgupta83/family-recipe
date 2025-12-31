import SwiftUI
import SwiftData

// MARK: - Cookbook View

/// Main cookbook view with page-like browsing experience
/// Uses TabView with page style for horizontal paging between recipes
struct CookbookView: View {
    @Environment(\.modelContext) private var modelContext
    @Environment(\.templateTokens) private var tokens
    @EnvironmentObject private var appState: AppState

    @Query private var recipes: [Recipe]
    @State private var currentPage: Int = 0
    @State private var dragOffset: CGFloat = 0

    init(familyId: UUID? = nil) {
        // Filter by family if provided
        if let familyId = familyId {
            _recipes = Query(
                filter: #Predicate<Recipe> { $0.familyId == familyId },
                sort: [SortDescriptor(\.updatedAt, order: .reverse)]
            )
        } else {
            _recipes = Query(sort: [SortDescriptor(\.updatedAt, order: .reverse)])
        }
    }

    var body: some View {
        GeometryReader { geometry in
            ZStack {
                // Background
                tokens.palette.background
                    .ignoresSafeArea()

                if recipes.isEmpty {
                    emptyState
                } else {
                    // Book pages
                    TabView(selection: $currentPage) {
                        ForEach(Array(recipes.enumerated()), id: \.element.id) { index, recipe in
                            RecipePageView(
                                recipe: recipe,
                                pageNumber: index + 1,
                                totalPages: recipes.count
                            )
                            .tag(index)
                            .modifier(PageFlipModifier(
                                currentPage: currentPage,
                                pageIndex: index,
                                dragOffset: dragOffset
                            ))
                        }
                    }
                    .tabViewStyle(.page(indexDisplayMode: .never))
                    .gesture(
                        DragGesture()
                            .onChanged { value in
                                dragOffset = value.translation.width
                            }
                            .onEnded { _ in
                                withAnimation(.easeOut(duration: 0.3)) {
                                    dragOffset = 0
                                }
                            }
                    )

                    // Page indicator
                    VStack {
                        Spacer()
                        pageIndicator
                    }
                }
            }
        }
        .navigationTitle("Family Cookbook")
        .navigationBarTitleDisplayMode(.inline)
    }

    // MARK: - Empty State

    private var emptyState: some View {
        VStack(spacing: tokens.spacing.lg) {
            Image(systemName: "book.closed")
                .font(.system(size: 64))
                .foregroundStyle(tokens.palette.textSecondary)

            Text("No Recipes Yet")
                .font(tokens.typography.titleLarge)
                .foregroundStyle(tokens.palette.text)

            Text("Scan or add your first family recipe to get started")
                .font(tokens.typography.bodyMedium)
                .foregroundStyle(tokens.palette.textSecondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, tokens.spacing.xl)
        }
    }

    // MARK: - Page Indicator

    private var pageIndicator: some View {
        HStack(spacing: tokens.spacing.xs) {
            ForEach(0..<min(recipes.count, 10), id: \.self) { index in
                Circle()
                    .fill(index == currentPage ? tokens.palette.primary : tokens.palette.divider)
                    .frame(width: 8, height: 8)
            }

            if recipes.count > 10 {
                Text("...")
                    .font(tokens.typography.caption)
                    .foregroundStyle(tokens.palette.textSecondary)
            }
        }
        .padding(.vertical, tokens.spacing.md)
        .padding(.horizontal, tokens.spacing.lg)
        .background(
            Capsule()
                .fill(tokens.palette.surface.opacity(0.9))
                .shadow(color: tokens.decoration.shadowStyle.shadowColor,
                       radius: tokens.decoration.shadowStyle.radius,
                       y: tokens.decoration.shadowStyle.y)
        )
        .padding(.bottom, tokens.spacing.xl)
    }
}

// MARK: - Cookbook Navigation Entry

/// Entry point for cookbook from home screen
struct CookbookButton: View {
    @Environment(\.templateTokens) private var tokens

    var body: some View {
        NavigationLink(destination: CookbookView()) {
            HStack(spacing: tokens.spacing.md) {
                Image(systemName: "book.fill")
                    .font(.title2)
                    .foregroundStyle(tokens.palette.primary)

                VStack(alignment: .leading, spacing: 2) {
                    Text("Open Cookbook")
                        .font(tokens.typography.labelLarge)
                        .foregroundStyle(tokens.palette.text)

                    Text("Browse recipes like a book")
                        .font(tokens.typography.caption)
                        .foregroundStyle(tokens.palette.textSecondary)
                }

                Spacer()

                Image(systemName: "chevron.right")
                    .font(.caption)
                    .foregroundStyle(tokens.palette.textSecondary)
            }
            .padding(tokens.spacing.md)
            .background(tokens.palette.surface)
            .clipShape(RoundedRectangle(cornerRadius: tokens.shape.cornerRadiusMedium))
            .shadow(color: tokens.decoration.shadowStyle.shadowColor,
                   radius: tokens.decoration.shadowStyle.radius,
                   y: tokens.decoration.shadowStyle.y)
        }
    }
}

// MARK: - Preview

#Preview {
    NavigationStack {
        CookbookView()
    }
    .environmentObject(AppState())
}
