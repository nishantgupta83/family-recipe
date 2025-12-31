package com.familyrecipe.features.cookbook

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.familyrecipe.core.models.Recipe
import com.familyrecipe.designsystem.LocalTemplateTokens
import kotlin.math.absoluteValue

// MARK: - Cookbook Screen

/**
 * Main cookbook view with page-like browsing experience
 * Uses HorizontalPager for horizontal paging between recipes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookbookScreen(
    recipes: List<Recipe>,
    currentMemberId: String?,
    onRecipeClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onToggleFavorite: (Recipe) -> Unit
) {
    val tokens = LocalTemplateTokens.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Family Cookbook") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = tokens.palette.background
                )
            )
        },
        containerColor = tokens.palette.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (recipes.isEmpty()) {
                CookbookEmptyState(
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                CookbookPager(
                    recipes = recipes,
                    currentMemberId = currentMemberId,
                    onRecipeClick = onRecipeClick,
                    onToggleFavorite = onToggleFavorite
                )
            }
        }
    }
}

// MARK: - Cookbook Pager

@Composable
private fun CookbookPager(
    recipes: List<Recipe>,
    currentMemberId: String?,
    onRecipeClick: (String) -> Unit,
    onToggleFavorite: (Recipe) -> Unit
) {
    val tokens = LocalTemplateTokens.current
    val pagerState = rememberPagerState(pageCount = { recipes.size })

    Box(modifier = Modifier.fillMaxSize()) {
        // Pages
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            pageSpacing = tokens.spacing.md,
            beyondViewportPageCount = 1 // Pre-render adjacent pages
        ) { pageIndex ->
            val recipe = recipes[pageIndex]

            // Calculate page offset for 3D flip effect
            val pageOffset by remember {
                derivedStateOf {
                    (pagerState.currentPage - pageIndex + pagerState.currentPageOffsetFraction)
                }
            }

            RecipePageComposable(
                recipe = recipe,
                pageNumber = pageIndex + 1,
                totalPages = recipes.size,
                isFavorited = currentMemberId?.let { recipe.favoritedBy.contains(it) } ?: false,
                pageOffset = pageOffset,
                onClick = { onRecipeClick(recipe.id) },
                onToggleFavorite = { onToggleFavorite(recipe) },
                modifier = Modifier
                    .fillMaxSize()
                    .pageFlipTransition(pageOffset)
            )
        }

        // Page indicator
        if (recipes.isNotEmpty()) {
            PageIndicator(
                pageCount = recipes.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = tokens.spacing.xl)
            )
        }
    }
}

// MARK: - Page Indicator

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    val tokens = LocalTemplateTokens.current
    val displayCount = minOf(pageCount, 10)

    Surface(
        modifier = modifier
            .shadow(
                elevation = tokens.decoration.shadowStyle.elevation,
                shape = RoundedCornerShape(50)
            ),
        shape = RoundedCornerShape(50),
        color = tokens.palette.surface.copy(alpha = 0.9f)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = tokens.spacing.lg,
                vertical = tokens.spacing.md
            ),
            horizontalArrangement = Arrangement.spacedBy(tokens.spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(displayCount) { index ->
                val isSelected = index == currentPage
                val size by animateDpAsState(
                    targetValue = if (isSelected) 10.dp else 8.dp,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "indicator_size"
                )

                Box(
                    modifier = Modifier
                        .size(size)
                        .background(
                            color = if (isSelected) tokens.palette.primary else tokens.palette.divider,
                            shape = CircleShape
                        )
                )
            }

            if (pageCount > 10) {
                Text(
                    text = "...",
                    style = tokens.typography.caption,
                    color = tokens.palette.textSecondary
                )
            }
        }
    }
}

// MARK: - Empty State

@Composable
private fun CookbookEmptyState(
    modifier: Modifier = Modifier
) {
    val tokens = LocalTemplateTokens.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.MenuBook,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = tokens.palette.textSecondary
        )

        Spacer(modifier = Modifier.height(tokens.spacing.lg))

        Text(
            text = "No Recipes Yet",
            style = tokens.typography.titleLarge,
            color = tokens.palette.text
        )

        Spacer(modifier = Modifier.height(tokens.spacing.sm))

        Text(
            text = "Scan or add your first family recipe to get started",
            style = tokens.typography.bodyMedium,
            color = tokens.palette.textSecondary
        )
    }
}
