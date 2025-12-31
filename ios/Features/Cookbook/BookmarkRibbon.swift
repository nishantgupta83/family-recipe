import SwiftUI

// MARK: - Bookmark Ribbon

/// Visual indicator for favorited recipes
/// Displays as a ribbon extending from the top-right corner of the page
struct BookmarkRibbon: View {
    @Environment(\.templateTokens) private var tokens

    // Configuration
    private let ribbonWidth: CGFloat = 32
    private let ribbonHeight: CGFloat = 56
    private let foldHeight: CGFloat = 12

    var body: some View {
        VStack(spacing: 0) {
            // Main ribbon body
            ribbonBody

            // Folded bottom triangle
            ribbonFold
        }
        .frame(width: ribbonWidth, height: ribbonHeight + foldHeight)
        .offset(y: -8) // Slightly overlap with page edge
    }

    // MARK: - Ribbon Body

    private var ribbonBody: some View {
        ZStack {
            // Ribbon shape
            Rectangle()
                .fill(tokens.palette.accent)
                .frame(width: ribbonWidth, height: ribbonHeight)

            // Heart icon
            Image(systemName: "heart.fill")
                .font(.system(size: 14, weight: .semibold))
                .foregroundStyle(.white)
                .offset(y: 8)
        }
        .shadow(color: .black.opacity(0.2), radius: 2, x: -1, y: 2)
    }

    // MARK: - Ribbon Fold

    private var ribbonFold: some View {
        Path { path in
            path.move(to: CGPoint(x: 0, y: 0))
            path.addLine(to: CGPoint(x: ribbonWidth / 2, y: foldHeight))
            path.addLine(to: CGPoint(x: ribbonWidth, y: 0))
            path.closeSubpath()
        }
        .fill(tokens.palette.accent.opacity(0.7))
        .frame(width: ribbonWidth, height: foldHeight)
    }
}

// MARK: - Interactive Bookmark Ribbon

/// Bookmark ribbon with tap-to-toggle functionality
struct InteractiveBookmarkRibbon: View {
    @Binding var isFavorited: Bool
    @Environment(\.templateTokens) private var tokens

    // Animation state
    @State private var isAnimating = false

    private let ribbonWidth: CGFloat = 32
    private let ribbonHeight: CGFloat = 56
    private let foldHeight: CGFloat = 12

    var body: some View {
        Button(action: toggleFavorite) {
            VStack(spacing: 0) {
                // Main ribbon body
                ZStack {
                    Rectangle()
                        .fill(isFavorited ? tokens.palette.accent : tokens.palette.divider)
                        .frame(width: ribbonWidth, height: ribbonHeight)

                    Image(systemName: isFavorited ? "heart.fill" : "heart")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundStyle(isFavorited ? .white : tokens.palette.textSecondary)
                        .offset(y: 8)
                        .scaleEffect(isAnimating ? 1.3 : 1.0)
                }
                .shadow(color: .black.opacity(0.2), radius: 2, x: -1, y: 2)

                // Folded bottom
                Path { path in
                    path.move(to: CGPoint(x: 0, y: 0))
                    path.addLine(to: CGPoint(x: ribbonWidth / 2, y: foldHeight))
                    path.addLine(to: CGPoint(x: ribbonWidth, y: 0))
                    path.closeSubpath()
                }
                .fill((isFavorited ? tokens.palette.accent : tokens.palette.divider).opacity(0.7))
                .frame(width: ribbonWidth, height: foldHeight)
            }
            .frame(width: ribbonWidth, height: ribbonHeight + foldHeight)
            .offset(y: -8)
        }
        .buttonStyle(.plain)
        .accessibilityLabel(isFavorited ? "Remove from favorites" : "Add to favorites")
    }

    private func toggleFavorite() {
        // Animate heart
        withAnimation(.spring(response: 0.3, dampingFraction: 0.5)) {
            isAnimating = true
        }

        // Toggle state
        withAnimation(.easeInOut(duration: 0.2)) {
            isFavorited.toggle()
        }

        // Reset animation
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
            withAnimation(.spring(response: 0.2, dampingFraction: 0.7)) {
                isAnimating = false
            }
        }
    }
}

// MARK: - Small Bookmark Badge

/// Smaller bookmark indicator for list views
struct BookmarkBadge: View {
    @Environment(\.templateTokens) private var tokens

    var body: some View {
        Image(systemName: "bookmark.fill")
            .font(.system(size: 12, weight: .semibold))
            .foregroundStyle(tokens.palette.accent)
            .padding(6)
            .background(
                Circle()
                    .fill(tokens.palette.accent.opacity(0.15))
            )
    }
}

// MARK: - Preview

#Preview("Bookmark Ribbon") {
    ZStack {
        Color(red: 0.95, green: 0.95, blue: 0.92)
            .ignoresSafeArea()

        RoundedRectangle(cornerRadius: 16)
            .fill(.white)
            .frame(width: 300, height: 400)
            .overlay(
                BookmarkRibbon()
                    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topTrailing)
                    .padding(.trailing, 24)
            )
            .shadow(radius: 8)
    }
}

#Preview("Interactive Ribbon") {
    struct PreviewWrapper: View {
        @State private var isFavorited = false

        var body: some View {
            ZStack {
                Color(red: 0.95, green: 0.95, blue: 0.92)
                    .ignoresSafeArea()

                RoundedRectangle(cornerRadius: 16)
                    .fill(.white)
                    .frame(width: 300, height: 400)
                    .overlay(
                        InteractiveBookmarkRibbon(isFavorited: $isFavorited)
                            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topTrailing)
                            .padding(.trailing, 24)
                    )
                    .shadow(radius: 8)

                Text("Tap ribbon to toggle")
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .offset(y: 220)
            }
        }
    }

    return PreviewWrapper()
}

#Preview("Bookmark Badge") {
    HStack(spacing: 20) {
        BookmarkBadge()
        Text("Grandma's Apple Pie")
    }
    .padding()
}
