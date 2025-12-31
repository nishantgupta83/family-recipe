import SwiftUI

// MARK: - Page Flip Modifier

/// Applies 3D rotation effect during page transitions
/// Creates a book-like flip animation when swiping between pages
struct PageFlipModifier: ViewModifier {
    let currentPage: Int
    let pageIndex: Int
    let dragOffset: CGFloat

    // Animation configuration
    private let maxRotation: Double = 45
    private let perspective: CGFloat = 0.5

    func body(content: Content) -> some View {
        content
            .rotation3DEffect(
                .degrees(rotationAngle),
                axis: (x: 0, y: 1, z: 0),
                anchor: anchor,
                perspective: perspective
            )
            .opacity(opacity)
            .zIndex(zIndex)
    }

    // MARK: - Computed Properties

    /// Calculate rotation angle based on current page and drag offset
    private var rotationAngle: Double {
        let pageDiff = pageIndex - currentPage

        if pageDiff == 0 {
            // Current page - rotate based on drag
            let normalizedOffset = Double(dragOffset) / 300.0
            return normalizedOffset * maxRotation
        } else if pageDiff == -1 {
            // Previous page (left)
            let normalizedOffset = max(0, Double(dragOffset) / 300.0)
            return -maxRotation + (normalizedOffset * maxRotation)
        } else if pageDiff == 1 {
            // Next page (right)
            let normalizedOffset = min(0, Double(dragOffset) / 300.0)
            return maxRotation + (normalizedOffset * maxRotation)
        }

        return 0
    }

    /// Anchor point for rotation - creates book spine effect
    private var anchor: UnitPoint {
        let pageDiff = pageIndex - currentPage

        if pageDiff == 0 {
            return dragOffset < 0 ? .trailing : .leading
        } else if pageDiff < 0 {
            return .trailing
        } else {
            return .leading
        }
    }

    /// Opacity for depth effect
    private var opacity: Double {
        let pageDiff = abs(pageIndex - currentPage)

        if pageDiff == 0 {
            return 1.0
        } else if pageDiff == 1 {
            // Adjacent pages slightly visible during transition
            let transitionProgress = abs(dragOffset) / 300.0
            return min(1.0, transitionProgress * 0.8)
        }

        return 0
    }

    /// Z-index for proper layering
    private var zIndex: Double {
        let pageDiff = pageIndex - currentPage

        if pageDiff == 0 {
            return 2
        } else if abs(pageDiff) == 1 {
            return 1
        }

        return 0
    }
}

// MARK: - View Extension

extension View {
    /// Apply page flip effect
    func pageFlip(
        currentPage: Int,
        pageIndex: Int,
        dragOffset: CGFloat
    ) -> some View {
        self.modifier(PageFlipModifier(
            currentPage: currentPage,
            pageIndex: pageIndex,
            dragOffset: dragOffset
        ))
    }
}

// MARK: - Simple Page Turn Modifier (Alternative)

/// Simpler page turn effect without full 3D rotation
/// Use this as fallback if 3D causes performance issues
struct SimplePageTurnModifier: ViewModifier {
    let currentPage: Int
    let pageIndex: Int

    func body(content: Content) -> some View {
        content
            .opacity(pageIndex == currentPage ? 1 : 0)
            .scaleEffect(pageIndex == currentPage ? 1 : 0.95)
            .animation(.easeInOut(duration: 0.3), value: currentPage)
    }
}

// MARK: - Page Shadow Modifier

/// Adds realistic shadow during page flip
struct PageShadowModifier: ViewModifier {
    let dragOffset: CGFloat
    let isFlipping: Bool

    func body(content: Content) -> some View {
        content
            .shadow(
                color: .black.opacity(shadowOpacity),
                radius: shadowRadius,
                x: shadowX,
                y: 0
            )
    }

    private var shadowOpacity: Double {
        guard isFlipping else { return 0.1 }
        return min(0.3, abs(Double(dragOffset)) / 500.0)
    }

    private var shadowRadius: CGFloat {
        guard isFlipping else { return 4 }
        return min(12, abs(dragOffset) / 20)
    }

    private var shadowX: CGFloat {
        dragOffset > 0 ? -4 : 4
    }
}

// MARK: - Preview

#Preview {
    struct PageFlipPreview: View {
        @State private var currentPage = 1
        @State private var dragOffset: CGFloat = 0

        var body: some View {
            VStack {
                ZStack {
                    ForEach(0..<3, id: \.self) { index in
                        RoundedRectangle(cornerRadius: 16)
                            .fill(Color.blue.opacity(0.1 + Double(index) * 0.2))
                            .frame(width: 300, height: 400)
                            .overlay(
                                Text("Page \(index + 1)")
                                    .font(.title)
                            )
                            .modifier(PageFlipModifier(
                                currentPage: currentPage,
                                pageIndex: index,
                                dragOffset: dragOffset
                            ))
                    }
                }
                .gesture(
                    DragGesture()
                        .onChanged { value in
                            dragOffset = value.translation.width
                        }
                        .onEnded { value in
                            withAnimation(.easeOut(duration: 0.3)) {
                                if value.translation.width < -50 && currentPage < 2 {
                                    currentPage += 1
                                } else if value.translation.width > 50 && currentPage > 0 {
                                    currentPage -= 1
                                }
                                dragOffset = 0
                            }
                        }
                )

                Text("Current page: \(currentPage + 1)")
                    .padding()
            }
        }
    }

    return PageFlipPreview()
}
