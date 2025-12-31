package com.familyrecipe.features.recipedetail

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.familyrecipe.core.models.Recipe
import com.familyrecipe.designsystem.FamilyRecipeThemeTokens
import com.familyrecipe.designsystem.RecipeCategoryColors
import com.familyrecipe.designsystem.RecipeDifficultyColors
import com.familyrecipe.features.home.getCategoryEmoji

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    onBack: () -> Unit,
    onStartCooking: () -> Unit
) {
    val tokens = FamilyRecipeThemeTokens.tokens
    var isFavorite by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isFavorite = !isFavorite }) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else tokens.palette.text
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = tokens.palette.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Hero section
                HeroSection(recipe = recipe)

                Column(modifier = Modifier.padding(tokens.spacing.md)) {
                    // Title
                    Text(
                        text = recipe.title,
                        style = tokens.typography.displayMedium,
                        color = tokens.palette.text
                    )

                    if (recipe.recipeDescription.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(tokens.spacing.sm))
                        Text(
                            text = recipe.recipeDescription,
                            style = tokens.typography.bodyMedium,
                            color = tokens.palette.textSecondary
                        )
                    }

                    // Difficulty
                    Spacer(modifier = Modifier.height(tokens.spacing.sm))
                    val diffColors = RecipeDifficultyColors.getColors(recipe.difficulty)
                    Surface(
                        shape = CircleShape,
                        color = diffColors.background
                    ) {
                        Text(
                            text = diffColors.label,
                            style = tokens.typography.labelMedium,
                            color = diffColors.foreground,
                            modifier = Modifier.padding(
                                horizontal = tokens.spacing.md,
                                vertical = tokens.spacing.xs
                            )
                        )
                    }

                    // Stats
                    Spacer(modifier = Modifier.height(tokens.spacing.lg))
                    StatsSection(recipe = recipe)

                    // Family memory
                    recipe.familyMemory?.takeIf { it.isNotEmpty() }?.let { memory ->
                        Spacer(modifier = Modifier.height(tokens.spacing.lg))
                        FamilyMemorySection(memory = memory)
                    }

                    // Ingredients
                    Spacer(modifier = Modifier.height(tokens.spacing.lg))
                    IngredientsSection(recipe = recipe)

                    // Instructions
                    Spacer(modifier = Modifier.height(tokens.spacing.lg))
                    InstructionsSection(recipe = recipe)

                    // Space for button
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }

            // Start cooking button
            Button(
                onClick = onStartCooking,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(tokens.spacing.md),
                colors = ButtonDefaults.buttonColors(
                    containerColor = tokens.palette.primary
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(tokens.spacing.sm))
                Text("Start Cooking", style = tokens.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun HeroSection(recipe: Recipe) {
    val tokens = FamilyRecipeThemeTokens.tokens
    val categoryColors = RecipeCategoryColors.getColors(recipe.category)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(tokens.spacing.md)
            .clip(MaterialTheme.shapes.large)
            .background(categoryColors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = getCategoryEmoji(recipe.category),
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(tokens.spacing.sm))
            Text(
                text = recipe.category.displayName,
                style = tokens.typography.labelLarge,
                color = categoryColors.foreground
            )
        }
    }
}

@Composable
private fun StatsSection(recipe: Recipe) {
    val tokens = FamilyRecipeThemeTokens.tokens

    Card(
        colors = CardDefaults.cardColors(containerColor = tokens.palette.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.spacing.md),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(label = "Prep", value = "${recipe.prepTimeMinutes} min")
            StatItem(label = "Cook", value = "${recipe.cookTimeMinutes} min")
            StatItem(label = "Servings", value = "${recipe.servings}")
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    val tokens = FamilyRecipeThemeTokens.tokens

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = tokens.typography.titleMedium,
            color = tokens.palette.text
        )
        Text(
            text = label,
            style = tokens.typography.caption,
            color = tokens.palette.textSecondary
        )
    }
}

@Composable
private fun FamilyMemorySection(memory: String) {
    val tokens = FamilyRecipeThemeTokens.tokens

    Column {
        Text(
            text = "Family Memory",
            style = tokens.typography.labelLarge,
            color = tokens.palette.text
        )
        Spacer(modifier = Modifier.height(tokens.spacing.sm))
        Card(
            colors = CardDefaults.cardColors(containerColor = tokens.palette.secondary)
        ) {
            Text(
                text = memory,
                style = tokens.typography.handwritten,
                color = tokens.palette.text,
                modifier = Modifier.padding(tokens.spacing.md)
            )
        }
    }
}

@Composable
private fun IngredientsSection(recipe: Recipe) {
    val tokens = FamilyRecipeThemeTokens.tokens

    Column {
        Text(
            text = "Ingredients",
            style = tokens.typography.titleLarge,
            color = tokens.palette.text
        )
        Spacer(modifier = Modifier.height(tokens.spacing.sm))
        Card(
            colors = CardDefaults.cardColors(containerColor = tokens.palette.surface)
        ) {
            Column(modifier = Modifier.padding(tokens.spacing.md)) {
                recipe.ingredients.forEach { ingredient ->
                    Row(
                        modifier = Modifier.padding(vertical = tokens.spacing.xs),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .size(6.dp)
                                .background(tokens.palette.primary, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(tokens.spacing.sm))
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
}

@Composable
private fun InstructionsSection(recipe: Recipe) {
    val tokens = FamilyRecipeThemeTokens.tokens

    Column {
        Text(
            text = "Instructions",
            style = tokens.typography.titleLarge,
            color = tokens.palette.text
        )
        Spacer(modifier = Modifier.height(tokens.spacing.sm))
        Card(
            colors = CardDefaults.cardColors(containerColor = tokens.palette.surface)
        ) {
            Column(modifier = Modifier.padding(tokens.spacing.md)) {
                recipe.instructions.forEach { instruction ->
                    Row(
                        modifier = Modifier.padding(vertical = tokens.spacing.sm),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = tokens.palette.primary,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${instruction.stepNumber}",
                                    style = tokens.typography.labelLarge,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(tokens.spacing.md))
                        Column {
                            Text(
                                text = instruction.text,
                                style = tokens.typography.bodyMedium,
                                color = tokens.palette.text
                            )
                            instruction.formattedDuration?.let { duration ->
                                Spacer(modifier = Modifier.height(tokens.spacing.xs))
                                Text(
                                    text = duration,
                                    style = tokens.typography.caption,
                                    color = tokens.palette.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
