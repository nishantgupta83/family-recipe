package com.familyrecipe.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familyrecipe.core.models.TemplateKey

// MARK: - Template Tokens

data class TemplateTokens(
    val key: TemplateKey,
    val displayName: String,
    val description: String,
    val typography: TypographyTokens,
    val palette: ColorPalette,
    val shape: ShapeTokens,
    val spacing: SpacingTokens,
    val decoration: DecorationTokens
)

// MARK: - Typography Tokens

data class TypographyTokens(
    val headingFontFamily: FontFamily,
    val bodyFontFamily: FontFamily,
    val handwrittenFontFamily: FontFamily,
    val fontScale: Float = 1.0f
) {
    val displayLarge: TextStyle
        get() = TextStyle(
            fontFamily = headingFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (36 * fontScale).sp,
            lineHeight = (43.2).sp,
            letterSpacing = (-0.5).sp
        )

    val displayMedium: TextStyle
        get() = TextStyle(
            fontFamily = headingFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (28 * fontScale).sp,
            lineHeight = (35).sp,
            letterSpacing = (-0.25).sp
        )

    val titleLarge: TextStyle
        get() = TextStyle(
            fontFamily = headingFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (22 * fontScale).sp,
            lineHeight = (28.6).sp
        )

    val titleMedium: TextStyle
        get() = TextStyle(
            fontFamily = headingFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (18 * fontScale).sp,
            lineHeight = (24.3).sp,
            letterSpacing = 0.15.sp
        )

    val bodyLarge: TextStyle
        get() = TextStyle(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (16 * fontScale).sp,
            lineHeight = (24).sp,
            letterSpacing = 0.5.sp
        )

    val bodyMedium: TextStyle
        get() = TextStyle(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (14 * fontScale).sp,
            lineHeight = (20.3).sp,
            letterSpacing = 0.25.sp
        )

    val labelLarge: TextStyle
        get() = TextStyle(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (14 * fontScale).sp,
            lineHeight = (19.6).sp,
            letterSpacing = 0.1.sp
        )

    val labelMedium: TextStyle
        get() = TextStyle(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (12 * fontScale).sp,
            lineHeight = (16.2).sp,
            letterSpacing = 0.5.sp
        )

    val caption: TextStyle
        get() = TextStyle(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (12 * fontScale).sp,
            lineHeight = (15.6).sp,
            letterSpacing = 0.4.sp
        )

    val handwritten: TextStyle
        get() = TextStyle(
            fontFamily = handwrittenFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (18 * fontScale).sp,
            lineHeight = (24).sp
        )
}

// MARK: - Color Palette

data class ColorPalette(
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val background: Color,
    val surface: Color,
    val text: Color,
    val textSecondary: Color,
    val divider: Color,
    val error: Color,
    val success: Color
)

// MARK: - Shape Tokens

data class ShapeTokens(
    val cornerRadiusSmall: Dp,
    val cornerRadiusMedium: Dp,
    val cornerRadiusLarge: Dp
)

// MARK: - Spacing Tokens

data class SpacingTokens(
    val scale: Dp = 4.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp
)

// MARK: - Decoration Tokens

data class DecorationTokens(
    val ornamentStyle: OrnamentStyle,
    val shadowStyle: ShadowStyle,
    val borderStyle: BorderStyle
)

enum class OrnamentStyle {
    NONE,
    FLOURISH,
    ICONS
}

enum class ShadowStyle(
    val elevation: Dp,
    val color: Color
) {
    NONE(0.dp, Color.Transparent),
    SOFT(4.dp, Color(0x1A8B4513)),
    ELEVATION(2.dp, Color(0x1A000000)),
    COLORFUL(4.dp, Color(0x33F97316))
}

enum class BorderStyle(
    val width: Dp,
    val isDashed: Boolean = false
) {
    NONE(0.dp),
    SINGLE(1.dp),
    DOUBLE(2.dp),
    DASHED(2.dp, true)
}

// MARK: - Template Definitions

object Templates {
    fun getTemplate(key: TemplateKey): TemplateTokens = when (key) {
        TemplateKey.VINTAGE -> vintage
        TemplateKey.MODERN -> modern
        TemplateKey.PLAYFUL -> playful
    }

    val vintage = TemplateTokens(
        key = TemplateKey.VINTAGE,
        displayName = "Vintage Cookbook",
        description = "Like grandma's handwritten recipe cards - warm, nostalgic, personal",
        typography = TypographyTokens(
            headingFontFamily = FontFamily.Cursive, // Replace with custom font
            bodyFontFamily = FontFamily.Serif,
            handwrittenFontFamily = FontFamily.Cursive,
            fontScale = 1.0f
        ),
        palette = ColorPalette(
            primary = Color(0xFF8B4513),
            secondary = Color(0xFFF5E6D3),
            accent = Color(0xFFD4AF37),
            background = Color(0xFFFFFEF7),
            surface = Color(0xFFFFFFFF),
            text = Color(0xFF4A4A4A),
            textSecondary = Color(0xFF7A7A7A),
            divider = Color(0xFFE0D4C4),
            error = Color(0xFFC53030),
            success = Color(0xFF38A169)
        ),
        shape = ShapeTokens(
            cornerRadiusSmall = 8.dp,
            cornerRadiusMedium = 16.dp,
            cornerRadiusLarge = 24.dp
        ),
        spacing = SpacingTokens(),
        decoration = DecorationTokens(
            ornamentStyle = OrnamentStyle.FLOURISH,
            shadowStyle = ShadowStyle.SOFT,
            borderStyle = BorderStyle.DOUBLE
        )
    )

    val modern = TemplateTokens(
        key = TemplateKey.MODERN,
        displayName = "Modern Kitchen",
        description = "Clean, minimal, and contemporary - Material Design inspired",
        typography = TypographyTokens(
            headingFontFamily = FontFamily.SansSerif,
            bodyFontFamily = FontFamily.SansSerif,
            handwrittenFontFamily = FontFamily.SansSerif,
            fontScale = 1.0f
        ),
        palette = ColorPalette(
            primary = Color(0xFF2563EB),
            secondary = Color(0xFFF1F5F9),
            accent = Color(0xFF3B82F6),
            background = Color(0xFFFFFFFF),
            surface = Color(0xFFF8FAFC),
            text = Color(0xFF1E293B),
            textSecondary = Color(0xFF64748B),
            divider = Color(0xFFE2E8F0),
            error = Color(0xFFDC2626),
            success = Color(0xFF16A34A)
        ),
        shape = ShapeTokens(
            cornerRadiusSmall = 4.dp,
            cornerRadiusMedium = 8.dp,
            cornerRadiusLarge = 16.dp
        ),
        spacing = SpacingTokens(),
        decoration = DecorationTokens(
            ornamentStyle = OrnamentStyle.NONE,
            shadowStyle = ShadowStyle.ELEVATION,
            borderStyle = BorderStyle.NONE
        )
    )

    val playful = TemplateTokens(
        key = TemplateKey.PLAYFUL,
        displayName = "Playful Family",
        description = "Fun, colorful, and kid-friendly - perfect for cooking with children",
        typography = TypographyTokens(
            headingFontFamily = FontFamily.SansSerif,
            bodyFontFamily = FontFamily.SansSerif,
            handwrittenFontFamily = FontFamily.SansSerif,
            fontScale = 1.05f
        ),
        palette = ColorPalette(
            primary = Color(0xFFF97316),
            secondary = Color(0xFFFEF3C7),
            accent = Color(0xFF22C55E),
            background = Color(0xFFFFFBEB),
            surface = Color(0xFFFFFFFF),
            text = Color(0xFF292524),
            textSecondary = Color(0xFF78716C),
            divider = Color(0xFFFDE68A),
            error = Color(0xFFEF4444),
            success = Color(0xFF10B981)
        ),
        shape = ShapeTokens(
            cornerRadiusSmall = 12.dp,
            cornerRadiusMedium = 20.dp,
            cornerRadiusLarge = 28.dp
        ),
        spacing = SpacingTokens(),
        decoration = DecorationTokens(
            ornamentStyle = OrnamentStyle.ICONS,
            shadowStyle = ShadowStyle.COLORFUL,
            borderStyle = BorderStyle.DASHED
        )
    )
}

// MARK: - Composition Local

val LocalTemplateTokens = staticCompositionLocalOf { Templates.vintage }

// MARK: - Theme

@Composable
fun FamilyRecipeTheme(
    templateTokens: TemplateTokens = Templates.vintage,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = templateTokens.palette.primary,
            secondary = templateTokens.palette.secondary,
            tertiary = templateTokens.palette.accent,
            background = templateTokens.palette.background,
            surface = templateTokens.palette.surface,
            error = templateTokens.palette.error,
            onPrimary = Color.White,
            onSecondary = templateTokens.palette.text,
            onTertiary = Color.White,
            onBackground = templateTokens.palette.text,
            onSurface = templateTokens.palette.text,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = templateTokens.palette.primary,
            secondary = templateTokens.palette.secondary,
            tertiary = templateTokens.palette.accent,
            background = templateTokens.palette.background,
            surface = templateTokens.palette.surface,
            error = templateTokens.palette.error,
            onPrimary = Color.White,
            onSecondary = templateTokens.palette.text,
            onTertiary = Color.White,
            onBackground = templateTokens.palette.text,
            onSurface = templateTokens.palette.text,
            onError = Color.White
        )
    }

    CompositionLocalProvider(LocalTemplateTokens provides templateTokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}

// MARK: - Convenience Extensions

object FamilyRecipeThemeTokens {
    val tokens: TemplateTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalTemplateTokens.current
}

// MARK: - Category Colors

data class CategoryColors(
    val background: Color,
    val foreground: Color,
    val icon: String
)

object RecipeCategoryColors {
    fun getColors(category: com.familyrecipe.core.models.RecipeCategory): CategoryColors = when (category) {
        com.familyrecipe.core.models.RecipeCategory.BREAKFAST -> CategoryColors(
            background = Color(0xFFFEF3C7),
            foreground = Color(0xFFD97706),
            icon = "sunrise"
        )
        com.familyrecipe.core.models.RecipeCategory.BRUNCH -> CategoryColors(
            background = Color(0xFFFECACA),
            foreground = Color(0xFFDC2626),
            icon = "sun_and_horizon"
        )
        com.familyrecipe.core.models.RecipeCategory.LUNCH -> CategoryColors(
            background = Color(0xFFD1FAE5),
            foreground = Color(0xFF059669),
            icon = "leaf"
        )
        com.familyrecipe.core.models.RecipeCategory.DINNER -> CategoryColors(
            background = Color(0xFFDBEAFE),
            foreground = Color(0xFF2563EB),
            icon = "moon_stars"
        )
        com.familyrecipe.core.models.RecipeCategory.APPETIZER -> CategoryColors(
            background = Color(0xFFE0E7FF),
            foreground = Color(0xFF4F46E5),
            icon = "fork_knife"
        )
        com.familyrecipe.core.models.RecipeCategory.SNACK -> CategoryColors(
            background = Color(0xFFFCE7F3),
            foreground = Color(0xFFDB2777),
            icon = "carrot"
        )
        com.familyrecipe.core.models.RecipeCategory.DESSERT -> CategoryColors(
            background = Color(0xFFFDE68A),
            foreground = Color(0xFFB45309),
            icon = "birthday_cake"
        )
        com.familyrecipe.core.models.RecipeCategory.BEVERAGE -> CategoryColors(
            background = Color(0xFFCFFAFE),
            foreground = Color(0xFF0891B2),
            icon = "cup_and_saucer"
        )
        com.familyrecipe.core.models.RecipeCategory.SIDE -> CategoryColors(
            background = Color(0xFFDCFCE7),
            foreground = Color(0xFF16A34A),
            icon = "leaf_circle"
        )
    }
}

// MARK: - Difficulty Colors

data class DifficultyColors(
    val background: Color,
    val foreground: Color,
    val label: String
)

object RecipeDifficultyColors {
    fun getColors(difficulty: com.familyrecipe.core.models.Difficulty): DifficultyColors = when (difficulty) {
        com.familyrecipe.core.models.Difficulty.EASY -> DifficultyColors(
            background = Color(0xFFD1FAE5),
            foreground = Color(0xFF059669),
            label = "Easy"
        )
        com.familyrecipe.core.models.Difficulty.MEDIUM -> DifficultyColors(
            background = Color(0xFFFEF3C7),
            foreground = Color(0xFFD97706),
            label = "Medium"
        )
        com.familyrecipe.core.models.Difficulty.HARD -> DifficultyColors(
            background = Color(0xFFFEE2E2),
            foreground = Color(0xFFDC2626),
            label = "Challenging"
        )
    }
}
