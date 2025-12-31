package com.familyrecipe.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.familyrecipe.core.models.Recipe
import com.familyrecipe.core.models.RecipeCategory
import com.familyrecipe.designsystem.FamilyRecipeThemeTokens
import com.familyrecipe.designsystem.RecipeCategoryColors
import com.familyrecipe.designsystem.RecipeDifficultyColors

// MARK: - Home Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onRecipeClick: (String) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val tokens = FamilyRecipeThemeTokens.tokens

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Welcome back!",
                            style = tokens.typography.bodyMedium,
                            color = tokens.palette.textSecondary
                        )
                        Text(
                            text = uiState.familyName ?: "Family Recipes",
                            style = tokens.typography.titleLarge,
                            color = tokens.palette.text
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = tokens.palette.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Navigate to add recipe */ },
                containerColor = tokens.palette.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Recipe",
                    tint = Color.White
                )
            }
        },
        containerColor = tokens.palette.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category chips
            CategoryChips(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) },
                modifier = Modifier.padding(horizontal = tokens.spacing.md)
            )

            Spacer(modifier = Modifier.height(tokens.spacing.md))

            // Recent recipes section
            if (uiState.recentRecipes.isNotEmpty()) {
                RecentRecipesSection(
                    recipes = uiState.recentRecipes,
                    onRecipeClick = onRecipeClick,
                    modifier = Modifier.padding(horizontal = tokens.spacing.md)
                )

                Spacer(modifier = Modifier.height(tokens.spacing.lg))
            }

            // All recipes header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = uiState.selectedCategory?.displayName ?: "All Recipes",
                    style = tokens.typography.titleMedium,
                    color = tokens.palette.text
                )
                Text(
                    text = "${uiState.filteredRecipes.size} recipes",
                    style = tokens.typography.caption,
                    color = tokens.palette.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(tokens.spacing.sm))

            // Recipes grid
            if (uiState.filteredRecipes.isEmpty()) {
                EmptyState(
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = tokens.spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(tokens.spacing.md),
                    verticalArrangement = Arrangement.spacedBy(tokens.spacing.md),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.filteredRecipes, key = { it.id }) { recipe ->
                        RecipeCard(
                            recipe = recipe,
                            onClick = { onRecipeClick(recipe.id) }
                        )
                    }
                }
            }
        }
    }
}

// MARK: - Category Chips

@Composable
fun CategoryChips(
    selectedCategory: RecipeCategory?,
    onCategorySelected: (RecipeCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = FamilyRecipeThemeTokens.tokens
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier.horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm)
    ) {
        // All chip
        CategoryChip(
            title = "All",
            isSelected = selectedCategory == null,
            backgroundColor = if (selectedCategory == null) tokens.palette.primary else tokens.palette.surface,
            textColor = if (selectedCategory == null) Color.White else tokens.palette.textSecondary,
            onClick = { onCategorySelected(null) }
        )

        // Category chips
        RecipeCategory.values().forEach { category ->
            val colors = RecipeCategoryColors.getColors(category)
            CategoryChip(
                title = category.displayName,
                isSelected = selectedCategory == category,
                backgroundColor = if (selectedCategory == category) colors.background else tokens.palette.surface,
                textColor = if (selectedCategory == category) colors.foreground else tokens.palette.textSecondary,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
fun CategoryChip(
    title: String,
    isSelected: Boolean,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    val tokens = FamilyRecipeThemeTokens.tokens

    Surface(
        shape = CircleShape,
        color = backgroundColor,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = title,
            style = tokens.typography.labelMedium,
            color = textColor,
            modifier = Modifier.padding(
                horizontal = tokens.spacing.md,
                vertical = tokens.spacing.sm
            )
        )
    }
}

// MARK: - Recent Recipes Section

@Composable
fun RecentRecipesSection(
    recipes: List<Recipe>,
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = FamilyRecipeThemeTokens.tokens

    Column(modifier = modifier) {
        Text(
            text = "Recent Recipes",
            style = tokens.typography.titleMedium,
            color = tokens.palette.text
        )

        Spacer(modifier = Modifier.height(tokens.spacing.sm))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(tokens.spacing.md)
        ) {
            items(recipes, key = { it.id }) { recipe ->
                HorizontalRecipeCard(
                    recipe = recipe,
                    onClick = { onRecipeClick(recipe.id) }
                )
            }
        }
    }
}

// MARK: - Recipe Cards

@Composable
fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit
) {
    val tokens = FamilyRecipeThemeTokens.tokens
    val categoryColors = RecipeCategoryColors.getColors(recipe.category)
    val difficultyColors = RecipeDifficultyColors.getColors(recipe.difficulty)

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(tokens.shape.cornerRadiusMedium),
        colors = CardDefaults.cardColors(containerColor = tokens.palette.surface),
        elevation = CardDefaults.cardElevation(
            defaultElevation = tokens.decoration.shadowStyle.elevation
        )
    ) {
        Column(
            modifier = Modifier.padding(tokens.spacing.sm)
        ) {
            // Image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(tokens.shape.cornerRadiusMedium))
                    .background(categoryColors.background),
                contentAlignment = Alignment.Center
            ) {
                // Icon placeholder - in real app would be an image
                Text(
                    text = getCategoryEmoji(recipe.category),
                    style = MaterialTheme.typography.headlineLarge
                )
            }

            Spacer(modifier = Modifier.height(tokens.spacing.sm))

            Text(
                text = recipe.title,
                style = tokens.typography.labelLarge,
                color = tokens.palette.text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(tokens.spacing.xs))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Difficulty badge
                Surface(
                    shape = CircleShape,
                    color = difficultyColors.background
                ) {
                    Text(
                        text = difficultyColors.label,
                        style = tokens.typography.caption,
                        color = difficultyColors.foreground,
                        modifier = Modifier.padding(
                            horizontal = tokens.spacing.sm,
                            vertical = 2.dp
                        )
                    )
                }

                // Time
                Text(
                    text = "${recipe.totalTimeMinutes}m",
                    style = tokens.typography.caption,
                    color = tokens.palette.textSecondary
                )
            }
        }
    }
}

@Composable
fun HorizontalRecipeCard(
    recipe: Recipe,
    onClick: () -> Unit
) {
    val tokens = FamilyRecipeThemeTokens.tokens
    val categoryColors = RecipeCategoryColors.getColors(recipe.category)

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(tokens.shape.cornerRadiusMedium),
        colors = CardDefaults.cardColors(containerColor = tokens.palette.surface),
        elevation = CardDefaults.cardElevation(
            defaultElevation = tokens.decoration.shadowStyle.elevation
        ),
        modifier = Modifier.width(160.dp)
    ) {
        Column(
            modifier = Modifier.padding(tokens.spacing.sm)
        ) {
            // Image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(tokens.shape.cornerRadiusMedium))
                    .background(categoryColors.background),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getCategoryEmoji(recipe.category),
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Spacer(modifier = Modifier.height(tokens.spacing.sm))

            Text(
                text = recipe.title,
                style = tokens.typography.labelLarge,
                color = tokens.palette.text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(tokens.spacing.xs))

            Text(
                text = "${recipe.totalTimeMinutes} min",
                style = tokens.typography.caption,
                color = tokens.palette.textSecondary
            )
        }
    }
}

// MARK: - Empty State

@Composable
fun EmptyState(
    modifier: Modifier = Modifier
) {
    val tokens = FamilyRecipeThemeTokens.tokens

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "\uD83D\uDCD6", // Book emoji
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(tokens.spacing.md))

        Text(
            text = "No recipes yet",
            style = tokens.typography.titleMedium,
            color = tokens.palette.text
        )

        Spacer(modifier = Modifier.height(tokens.spacing.sm))

        Text(
            text = "Add your first family recipe to get started!",
            style = tokens.typography.bodyMedium,
            color = tokens.palette.textSecondary
        )
    }
}

// MARK: - Helpers

fun getCategoryEmoji(category: RecipeCategory): String = when (category) {
    RecipeCategory.BREAKFAST -> "\uD83C\uDF73" // 🍳
    RecipeCategory.BRUNCH -> "\uD83E\uDD50" // 🥐
    RecipeCategory.LUNCH -> "\uD83E\uDD57" // 🥗
    RecipeCategory.DINNER -> "\uD83C\uDF5D" // 🍝
    RecipeCategory.APPETIZER -> "\uD83E\uDDC0" // 🧀
    RecipeCategory.SNACK -> "\uD83C\uDF6A" // 🍪
    RecipeCategory.DESSERT -> "\uD83C\uDF70" // 🍰
    RecipeCategory.BEVERAGE -> "\uD83E\uDDC3" // 🧃
    RecipeCategory.SIDE -> "\uD83E\uDD66" // 🥦
}
