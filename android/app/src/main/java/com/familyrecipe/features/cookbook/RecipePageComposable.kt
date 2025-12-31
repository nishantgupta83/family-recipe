package com.familyrecipe.features.cookbook

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.familyrecipe.core.models.Recipe
import com.familyrecipe.designsystem.BorderStyle
import com.familyrecipe.designsystem.LocalTemplateTokens
import com.familyrecipe.designsystem.OrnamentStyle
import com.familyrecipe.designsystem.RecipeCategoryColors
import com.familyrecipe.designsystem.RecipeDifficultyColors

// MARK: - Recipe Page Composable

/**
 * Single recipe displayed as a book page with template styling
 */
@Composable
fun RecipePageComposable(
    recipe: Recipe,
    pageNumber: Int,
    totalPages: Int,
    isFavorited: Boolean,
    pageOffset: Float,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalTemplateTokens.current

    Box(
        modifier = modifier
            .padding(horizontal = tokens.spacing.md, vertical = tokens.spacing.lg)
            .shadow(
                elevation = tokens.decoration.shadowStyle.elevation,
                shape = RoundedCornerShape(tokens.shape.cornerRadiusMedium)
            )
            .clip(RoundedCornerShape(tokens.shape.cornerRadiusMedium))
            .background(tokens.palette.background)
            .clickable(onClick = onClick)
    ) {
        // Paper texture gradient (for vintage template)
        if (tokens.decoration.ornamentStyle == OrnamentStyle.FLOURISH) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                tokens.palette.background,
                                tokens.palette.secondary.copy(alpha = 0.3f)
                            ),
                            start = Offset.Zero,
                            end = Offset.Infinite
                        )
                    )
            )
        }

        // Border overlay
        BorderOverlay(modifier = Modifier.fillMaxSize())

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(tokens.spacing.xl)
                .padding(top = tokens.spacing.md)
        ) {
            // Header
            RecipeHeader(recipe = recipe)

            Spacer(modifier = Modifier.height(tokens.spacing.md))

            Divider(color = tokens.palette.divider)

            Spacer(modifier = Modifier.height(tokens.spacing.md))

            // Metadata row
            MetadataRow(recipe = recipe)

            Spacer(modifier = Modifier.height(tokens.spacing.lg))

            // Ingredients
            IngredientsSection(recipe = recipe)

            Spacer(modifier = Modifier.height(tokens.spacing.lg))

            // Instructions
            InstructionsSection(recipe = recipe)

            // Family memory
            recipe.familyMemory?.takeIf { it.isNotEmpty() }?.let { memory ->
                Spacer(modifier = Modifier.height(tokens.spacing.lg))
                FamilyMemorySection(memory = memory)
            }

            Spacer(modifier = Modifier.height(tokens.spacing.lg))

            // Page number footer
            PageNumberFooter(
                pageNumber = pageNumber,
                totalPages = totalPages
            )
        }

        // Bookmark ribbon (if favorited)
        if (isFavorited) {
            InteractiveBookmarkRibbon(
                isFavorited = true,
                onToggle = onToggleFavorite,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = tokens.spacing.lg)
            )
        }
    }
}

// MARK: - Border Overlay

@Composable
private fun BorderOverlay(
    modifier: Modifier = Modifier
) {
    val tokens = LocalTemplateTokens.current
    val borderStyle = tokens.decoration.borderStyle
    val primaryColor = tokens.palette.primary

    when (borderStyle) {
        BorderStyle.DOUBLE -> {
            // Vintage double border
            Box(
                modifier = modifier
                    .padding(8.dp)
                    .border(
                        width = 2.dp,
                        color = primaryColor.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(tokens.shape.cornerRadiusMedium)
                    )
                    .padding(8.dp)
                    .border(
                        width = 1.dp,
                        color = primaryColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(tokens.shape.cornerRadiusMedium - 4.dp)
                    )
            ) {
                // Corner flourishes would go here (using icons or custom drawing)
                if (tokens.decoration.ornamentStyle == OrnamentStyle.FLOURISH) {
                    CornerFlourishes()
                }
            }
        }
        BorderStyle.SINGLE -> {
            Box(
                modifier = modifier
                    .padding(8.dp)
                    .border(
                        width = 1.dp,
                        color = tokens.palette.divider,
                        shape = RoundedCornerShape(tokens.shape.cornerRadiusMedium)
                    )
            )
        }
        BorderStyle.DASHED -> {
            Box(
                modifier = modifier
                    .padding(8.dp)
                    .drawBehind {
                        val cornerRadius = tokens.shape.cornerRadiusMedium.toPx()
                        drawRoundRect(
                            color = primaryColor.copy(alpha = 0.5f),
                            style = Stroke(
                                width = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 8f))
                            ),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
                        )
                    }
            )
        }
        BorderStyle.NONE -> { /* No border */ }
    }
}

// MARK: - Corner Flourishes

@Composable
private fun CornerFlourishes() {
    val tokens = LocalTemplateTokens.current

    Box(modifier = Modifier.fillMaxSize()) {
        // Simple leaf decorations at corners using emoji/icons
        // In production, use custom vector assets
        Text(
            text = "\uD83C\uDF3F", // Leaf
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp),
            color = tokens.palette.primary.copy(alpha = 0.2f)
        )
        Text(
            text = "\uD83C\uDF3F",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp),
            color = tokens.palette.primary.copy(alpha = 0.2f)
        )
        Text(
            text = "\uD83C\uDF3F",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(4.dp),
            color = tokens.palette.primary.copy(alpha = 0.2f)
        )
        Text(
            text = "\uD83C\uDF3F",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp),
            color = tokens.palette.primary.copy(alpha = 0.2f)
        )
    }
}

// MARK: - Recipe Header

@Composable
private fun RecipeHeader(recipe: Recipe) {
    val tokens = LocalTemplateTokens.current
    val categoryColors = RecipeCategoryColors.getColors(recipe.category)

    Column(
        verticalArrangement = Arrangement.spacedBy(tokens.spacing.sm)
    ) {
        // Category chip
        Surface(
            shape = CircleShape,
            color = categoryColors.background
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = tokens.spacing.sm,
                    vertical = tokens.spacing.xs
                ),
                horizontalArrangement = Arrangement.spacedBy(tokens.spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getCategoryEmoji(recipe.category),
                    style = tokens.typography.caption
                )
                Text(
                    text = recipe.category.displayName,
                    style = tokens.typography.labelMedium,
                    color = categoryColors.foreground
                )
            }
        }

        // Title
        Text(
            text = recipe.title,
            style = tokens.typography.displayMedium,
            color = tokens.palette.text,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        // Description
        if (recipe.recipeDescription.isNotEmpty()) {
            Text(
                text = recipe.recipeDescription,
                style = tokens.typography.bodyMedium,
                color = tokens.palette.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// MARK: - Metadata Row

@Composable
private fun MetadataRow(recipe: Recipe) {
    val tokens = LocalTemplateTokens.current
    val difficultyColors = RecipeDifficultyColors.getColors(recipe.difficulty)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(tokens.spacing.lg)
    ) {
        // Prep time
        MetadataItem(
            icon = Icons.Default.AccessTime,
            label = "Prep",
            value = "${recipe.prepTimeMinutes}m"
        )

        // Cook time
        MetadataItem(
            icon = Icons.Default.LocalFireDepartment,
            label = "Cook",
            value = "${recipe.cookTimeMinutes}m"
        )

        // Servings
        MetadataItem(
            icon = Icons.Default.People,
            label = "Serves",
            value = "${recipe.servings}"
        )

        // Difficulty
        Row(
            horizontalArrangement = Arrangement.spacedBy(tokens.spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(difficultyColors.foreground, CircleShape)
            )
            Text(
                text = difficultyColors.label,
                style = tokens.typography.caption,
                color = tokens.palette.textSecondary
            )
        }
    }
}

@Composable
private fun MetadataItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    val tokens = LocalTemplateTokens.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = tokens.palette.primary
        )
        Text(
            text = value,
            style = tokens.typography.labelMedium,
            color = tokens.palette.text
        )
        Text(
            text = label,
            style = tokens.typography.caption,
            color = tokens.palette.textSecondary
        )
    }
}

// MARK: - Ingredients Section

@Composable
private fun IngredientsSection(recipe: Recipe) {
    val tokens = LocalTemplateTokens.current

    Column(
        verticalArrangement = Arrangement.spacedBy(tokens.spacing.sm)
    ) {
        Text(
            text = "Ingredients",
            style = tokens.typography.titleMedium,
            color = tokens.palette.text
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(tokens.spacing.xs)
        ) {
            recipe.ingredients.forEach { ingredient ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(6.dp)
                            .background(tokens.palette.primary, CircleShape)
                    )
                    Text(
                        text = ingredient.displayString,
                        style = tokens.typography.bodyMedium,
                        color = tokens.palette.text
                    )
                }
            }
        }
    }
}

// MARK: - Instructions Section

@Composable
private fun InstructionsSection(recipe: Recipe) {
    val tokens = LocalTemplateTokens.current

    Column(
        verticalArrangement = Arrangement.spacedBy(tokens.spacing.sm)
    ) {
        Text(
            text = "Instructions",
            style = tokens.typography.titleMedium,
            color = tokens.palette.text
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(tokens.spacing.md)
        ) {
            recipe.instructions.forEach { instruction ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
                    verticalAlignment = Alignment.Top
                ) {
                    // Step number
                    Surface(
                        shape = CircleShape,
                        color = tokens.palette.secondary
                    ) {
                        Text(
                            text = "${instruction.stepNumber}",
                            style = tokens.typography.labelLarge,
                            color = tokens.palette.primary,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(4.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(tokens.spacing.xs)
                    ) {
                        Text(
                            text = instruction.text,
                            style = tokens.typography.bodyMedium,
                            color = tokens.palette.text
                        )

                        // Timer hint
                        instruction.formattedDuration?.let { duration ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(tokens.spacing.xs),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = tokens.palette.accent
                                )
                                Text(
                                    text = duration,
                                    style = tokens.typography.caption,
                                    color = tokens.palette.accent
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// MARK: - Family Memory Section

@Composable
private fun FamilyMemorySection(memory: String) {
    val tokens = LocalTemplateTokens.current

    Column(
        verticalArrangement = Arrangement.spacedBy(tokens.spacing.sm)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(tokens.spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "\u2764\uFE0F", // Heart
                style = tokens.typography.caption
            )
            Text(
                text = "Family Memory",
                style = tokens.typography.labelMedium,
                color = tokens.palette.textSecondary
            )
        }

        Surface(
            shape = RoundedCornerShape(tokens.shape.cornerRadiusSmall),
            color = tokens.palette.secondary.copy(alpha = 0.3f)
        ) {
            Text(
                text = memory,
                style = tokens.typography.handwritten,
                color = tokens.palette.text,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.spacing.md)
            )
        }
    }
}

// MARK: - Page Number Footer

@Composable
private fun PageNumberFooter(
    pageNumber: Int,
    totalPages: Int
) {
    val tokens = LocalTemplateTokens.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = tokens.spacing.lg),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Page $pageNumber of $totalPages",
            style = tokens.typography.caption,
            color = tokens.palette.textSecondary
        )
    }
}

// MARK: - Helper

private fun getCategoryEmoji(category: com.familyrecipe.core.models.RecipeCategory): String =
    when (category) {
        com.familyrecipe.core.models.RecipeCategory.BREAKFAST -> "\uD83C\uDF73"
        com.familyrecipe.core.models.RecipeCategory.BRUNCH -> "\uD83E\uDD50"
        com.familyrecipe.core.models.RecipeCategory.LUNCH -> "\uD83E\uDD57"
        com.familyrecipe.core.models.RecipeCategory.DINNER -> "\uD83C\uDF5D"
        com.familyrecipe.core.models.RecipeCategory.APPETIZER -> "\uD83E\uDDC0"
        com.familyrecipe.core.models.RecipeCategory.SNACK -> "\uD83C\uDF6A"
        com.familyrecipe.core.models.RecipeCategory.DESSERT -> "\uD83C\uDF70"
        com.familyrecipe.core.models.RecipeCategory.BEVERAGE -> "\uD83E\uDDC3"
        com.familyrecipe.core.models.RecipeCategory.SIDE -> "\uD83E\uDD66"
    }
