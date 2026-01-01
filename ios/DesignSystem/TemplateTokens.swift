import SwiftUI

// MARK: - Template Key Enum

enum TemplateKey: String, Codable, CaseIterable {
    case vintage
    case modern
    case playful

    var displayName: String {
        switch self {
        case .vintage: return "Vintage Cookbook"
        case .modern: return "Modern Kitchen"
        case .playful: return "Playful Family"
        }
    }

    var description: String {
        switch self {
        case .vintage: return "Warm, nostalgic, like grandma's recipe cards"
        case .modern: return "Clean, minimal, contemporary design"
        case .playful: return "Fun, colorful, perfect for cooking with kids"
        }
    }
}

// MARK: - Template Tokens

/// Design tokens that define a visual template - matches core-contract/tokens/design-tokens.yaml
struct TemplateTokens {
    let key: TemplateKey
    let displayName: String
    let description: String

    // Typography
    let typography: TypographyTokens

    // Colors
    let palette: ColorPalette

    // Shape
    let shape: ShapeTokens

    // Spacing
    let spacing: SpacingTokens

    // Decoration
    let decoration: DecorationTokens
}

// MARK: - Typography Tokens

struct TypographyTokens {
    let headingFontFamily: String
    let bodyFontFamily: String
    let handwrittenFontFamily: String
    let fontScale: Double

    // Pre-computed fonts
    var displayLarge: Font {
        Font.custom(headingFontFamily, size: 36 * fontScale)
    }

    var displayMedium: Font {
        Font.custom(headingFontFamily, size: 28 * fontScale)
    }

    var titleLarge: Font {
        Font.custom(headingFontFamily, size: 22 * fontScale)
    }

    var titleMedium: Font {
        Font.custom(headingFontFamily, size: 18 * fontScale)
    }

    var bodyLarge: Font {
        Font.custom(bodyFontFamily, size: 16 * fontScale)
    }

    var bodyMedium: Font {
        Font.custom(bodyFontFamily, size: 14 * fontScale)
    }

    var labelLarge: Font {
        Font.custom(bodyFontFamily, size: 14 * fontScale).weight(.medium)
    }

    var labelMedium: Font {
        Font.custom(bodyFontFamily, size: 12 * fontScale).weight(.medium)
    }

    var caption: Font {
        Font.custom(bodyFontFamily, size: 12 * fontScale)
    }

    var handwritten: Font {
        Font.custom(handwrittenFontFamily, size: 18 * fontScale)
    }
}

// MARK: - Color Palette

struct ColorPalette {
    let primary: Color
    let secondary: Color
    let accent: Color
    let background: Color
    let surface: Color
    let text: Color
    let textSecondary: Color
    let divider: Color
    let error: Color
    let success: Color
}

// MARK: - Shape Tokens

struct ShapeTokens {
    let cornerRadiusSmall: CGFloat
    let cornerRadiusMedium: CGFloat
    let cornerRadiusLarge: CGFloat

    var smallRoundedRect: RoundedRectangle {
        RoundedRectangle(cornerRadius: cornerRadiusSmall)
    }

    var mediumRoundedRect: RoundedRectangle {
        RoundedRectangle(cornerRadius: cornerRadiusMedium)
    }

    var largeRoundedRect: RoundedRectangle {
        RoundedRectangle(cornerRadius: cornerRadiusLarge)
    }
}

// MARK: - Spacing Tokens

struct SpacingTokens {
    let scale: CGFloat
    let xs: CGFloat
    let sm: CGFloat
    let md: CGFloat
    let lg: CGFloat
    let xl: CGFloat

    init(scale: CGFloat = 4) {
        self.scale = scale
        self.xs = scale * 1
        self.sm = scale * 2
        self.md = scale * 4
        self.lg = scale * 6
        self.xl = scale * 8
    }
}

// MARK: - Decoration Tokens

struct DecorationTokens {
    let ornamentStyle: OrnamentStyle
    let shadowStyle: ShadowStyle
    let borderStyle: BorderStyle
}

enum OrnamentStyle: String {
    case none
    case flourish
    case icons

    @ViewBuilder
    var ornamentView: some View {
        switch self {
        case .none:
            EmptyView()
        case .flourish:
            Image(systemName: "leaf.fill")
                .font(.system(size: 20))
                .foregroundStyle(.tertiary)
        case .icons:
            Image(systemName: "fork.knife")
                .font(.system(size: 20))
                .foregroundStyle(.tertiary)
        }
    }
}

enum ShadowStyle {
    case none
    case soft
    case elevation
    case colorful

    var shadowColor: Color {
        switch self {
        case .none: return .clear
        case .soft: return Color(hex: "8B4513").opacity(0.1)
        case .elevation: return Color.black.opacity(0.1)
        case .colorful: return Color(hex: "F97316").opacity(0.2)
        }
    }

    var radius: CGFloat {
        switch self {
        case .none: return 0
        case .soft: return 8
        case .elevation: return 4
        case .colorful: return 12
        }
    }

    var y: CGFloat {
        switch self {
        case .none: return 0
        case .soft: return 4
        case .elevation: return 2
        case .colorful: return 4
        }
    }
}

enum BorderStyle {
    case none
    case single
    case double
    case dashed

    var width: CGFloat {
        switch self {
        case .none: return 0
        case .single: return 1
        case .double: return 2
        case .dashed: return 2
        }
    }
}

// MARK: - Template Definitions

extension TemplateTokens {
    static func template(for key: TemplateKey) -> TemplateTokens {
        switch key {
        case .vintage:
            return vintage
        case .modern:
            return modern
        case .playful:
            return playful
        }
    }

    // MARK: Vintage Template

    static let vintage = TemplateTokens(
        key: .vintage,
        displayName: "Vintage Cookbook",
        description: "Like grandma's handwritten recipe cards - warm, nostalgic, personal",
        typography: TypographyTokens(
            headingFontFamily: "DancingScript-Bold",
            bodyFontFamily: "Kalam-Regular",
            handwrittenFontFamily: "DancingScript-Regular",
            fontScale: 1.0
        ),
        palette: ColorPalette(
            primary: Color(hex: "8B4513"),
            secondary: Color(hex: "F5E6D3"),
            accent: Color(hex: "D4AF37"),
            background: Color(hex: "FFFEF7"),
            surface: Color(hex: "FFFFFF"),
            text: Color(hex: "4A4A4A"),
            textSecondary: Color(hex: "7A7A7A"),
            divider: Color(hex: "E0D4C4"),
            error: Color(hex: "C53030"),
            success: Color(hex: "38A169")
        ),
        shape: ShapeTokens(
            cornerRadiusSmall: 8,
            cornerRadiusMedium: 16,
            cornerRadiusLarge: 24
        ),
        spacing: SpacingTokens(scale: 4),
        decoration: DecorationTokens(
            ornamentStyle: .flourish,
            shadowStyle: .soft,
            borderStyle: .double
        )
    )

    // MARK: Modern Template

    static let modern = TemplateTokens(
        key: .modern,
        displayName: "Modern Kitchen",
        description: "Clean, minimal, and contemporary - Material Design inspired",
        typography: TypographyTokens(
            headingFontFamily: "Inter-Bold",
            bodyFontFamily: "Inter-Regular",
            handwrittenFontFamily: "Inter-Medium",
            fontScale: 1.0
        ),
        palette: ColorPalette(
            primary: Color(hex: "2563EB"),
            secondary: Color(hex: "F1F5F9"),
            accent: Color(hex: "3B82F6"),
            background: Color(hex: "FFFFFF"),
            surface: Color(hex: "F8FAFC"),
            text: Color(hex: "1E293B"),
            textSecondary: Color(hex: "64748B"),
            divider: Color(hex: "E2E8F0"),
            error: Color(hex: "DC2626"),
            success: Color(hex: "16A34A")
        ),
        shape: ShapeTokens(
            cornerRadiusSmall: 4,
            cornerRadiusMedium: 8,
            cornerRadiusLarge: 16
        ),
        spacing: SpacingTokens(scale: 4),
        decoration: DecorationTokens(
            ornamentStyle: .none,
            shadowStyle: .elevation,
            borderStyle: .none
        )
    )

    // MARK: Playful Template

    static let playful = TemplateTokens(
        key: .playful,
        displayName: "Playful Family",
        description: "Fun, colorful, and kid-friendly - perfect for cooking with children",
        typography: TypographyTokens(
            headingFontFamily: "Fredoka-Bold",
            bodyFontFamily: "Nunito-Regular",
            handwrittenFontFamily: "Fredoka-Medium",
            fontScale: 1.05
        ),
        palette: ColorPalette(
            primary: Color(hex: "F97316"),
            secondary: Color(hex: "FEF3C7"),
            accent: Color(hex: "22C55E"),
            background: Color(hex: "FFFBEB"),
            surface: Color(hex: "FFFFFF"),
            text: Color(hex: "292524"),
            textSecondary: Color(hex: "78716C"),
            divider: Color(hex: "FDE68A"),
            error: Color(hex: "EF4444"),
            success: Color(hex: "10B981")
        ),
        shape: ShapeTokens(
            cornerRadiusSmall: 12,
            cornerRadiusMedium: 20,
            cornerRadiusLarge: 28
        ),
        spacing: SpacingTokens(scale: 4),
        decoration: DecorationTokens(
            ornamentStyle: .icons,
            shadowStyle: .colorful,
            borderStyle: .dashed
        )
    )
}

// MARK: - Environment Key

private struct TemplateTokensKey: EnvironmentKey {
    static let defaultValue = TemplateTokens.vintage
}

extension EnvironmentValues {
    var templateTokens: TemplateTokens {
        get { self[TemplateTokensKey.self] }
        set { self[TemplateTokensKey.self] = newValue }
    }
}

// MARK: - Color Extension

extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 0, 0)
        }
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}

// MARK: - Category Colors

struct CategoryColors {
    let background: Color
    let foreground: Color
    let icon: String

    static func colors(for category: RecipeCategory) -> CategoryColors {
        switch category {
        case .breakfast:
            return CategoryColors(
                background: Color(hex: "FEF3C7"),
                foreground: Color(hex: "D97706"),
                icon: "sunrise"
            )
        case .brunch:
            return CategoryColors(
                background: Color(hex: "FECACA"),
                foreground: Color(hex: "DC2626"),
                icon: "sun.and.horizon"
            )
        case .lunch:
            return CategoryColors(
                background: Color(hex: "D1FAE5"),
                foreground: Color(hex: "059669"),
                icon: "leaf"
            )
        case .dinner:
            return CategoryColors(
                background: Color(hex: "DBEAFE"),
                foreground: Color(hex: "2563EB"),
                icon: "moon.stars"
            )
        case .appetizer:
            return CategoryColors(
                background: Color(hex: "E0E7FF"),
                foreground: Color(hex: "4F46E5"),
                icon: "fork.knife"
            )
        case .snack:
            return CategoryColors(
                background: Color(hex: "FCE7F3"),
                foreground: Color(hex: "DB2777"),
                icon: "carrot"
            )
        case .dessert:
            return CategoryColors(
                background: Color(hex: "FDE68A"),
                foreground: Color(hex: "B45309"),
                icon: "birthday.cake"
            )
        case .beverage:
            return CategoryColors(
                background: Color(hex: "CFFAFE"),
                foreground: Color(hex: "0891B2"),
                icon: "cup.and.saucer"
            )
        case .side:
            return CategoryColors(
                background: Color(hex: "DCFCE7"),
                foreground: Color(hex: "16A34A"),
                icon: "leaf.circle"
            )
        }
    }
}

// MARK: - Difficulty Colors

struct DifficultyColors {
    let background: Color
    let foreground: Color
    let label: String

    static func colors(for difficulty: Difficulty) -> DifficultyColors {
        switch difficulty {
        case .easy:
            return DifficultyColors(
                background: Color(hex: "D1FAE5"),
                foreground: Color(hex: "059669"),
                label: "Easy"
            )
        case .medium:
            return DifficultyColors(
                background: Color(hex: "FEF3C7"),
                foreground: Color(hex: "D97706"),
                label: "Medium"
            )
        case .hard:
            return DifficultyColors(
                background: Color(hex: "FEE2E2"),
                foreground: Color(hex: "DC2626"),
                label: "Challenging"
            )
        }
    }
}
