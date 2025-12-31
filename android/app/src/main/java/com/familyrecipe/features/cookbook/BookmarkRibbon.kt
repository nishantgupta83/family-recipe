package com.familyrecipe.features.cookbook

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.familyrecipe.designsystem.LocalTemplateTokens

// MARK: - Bookmark Ribbon

/**
 * Visual indicator for favorited recipes
 * Displays as a ribbon extending from the top-right corner of the page
 */
@Composable
fun BookmarkRibbon(
    modifier: Modifier = Modifier,
    ribbonWidth: Dp = 32.dp,
    ribbonHeight: Dp = 56.dp,
    foldHeight: Dp = 12.dp
) {
    val tokens = LocalTemplateTokens.current
    val accentColor = tokens.palette.accent
    val foldColor = tokens.palette.accent.copy(alpha = 0.7f)

    Box(
        modifier = modifier
            .size(width = ribbonWidth, height = ribbonHeight + foldHeight)
            .offset(y = (-8).dp) // Slightly overlap with page edge
            .drawBehind {
                // Main ribbon body
                drawRect(
                    color = accentColor,
                    topLeft = Offset.Zero,
                    size = Size(ribbonWidth.toPx(), ribbonHeight.toPx())
                )

                // Folded bottom triangle
                val foldPath = Path().apply {
                    moveTo(0f, ribbonHeight.toPx())
                    lineTo(ribbonWidth.toPx() / 2, ribbonHeight.toPx() + foldHeight.toPx())
                    lineTo(ribbonWidth.toPx(), ribbonHeight.toPx())
                    close()
                }
                drawPath(
                    path = foldPath,
                    color = foldColor
                )
            },
        contentAlignment = Alignment.TopCenter
    ) {
        // Heart icon
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = "Favorited",
            modifier = Modifier
                .padding(top = 16.dp)
                .size(14.dp),
            tint = Color.White
        )
    }
}

// MARK: - Interactive Bookmark Ribbon

/**
 * Bookmark ribbon with tap-to-toggle functionality
 */
@Composable
fun InteractiveBookmarkRibbon(
    isFavorited: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    ribbonWidth: Dp = 32.dp,
    ribbonHeight: Dp = 56.dp,
    foldHeight: Dp = 12.dp
) {
    val tokens = LocalTemplateTokens.current

    // Animation state
    var isAnimating by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        finishedListener = { isAnimating = false },
        label = "heart_scale"
    )

    val ribbonColor = if (isFavorited) tokens.palette.accent else tokens.palette.divider
    val foldColor = ribbonColor.copy(alpha = 0.7f)
    val iconTint = if (isFavorited) Color.White else tokens.palette.textSecondary

    Box(
        modifier = modifier
            .size(width = ribbonWidth, height = ribbonHeight + foldHeight)
            .offset(y = (-8).dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isAnimating = true
                onToggle()
            }
            .drawBehind {
                // Main ribbon body
                drawRect(
                    color = ribbonColor,
                    topLeft = Offset.Zero,
                    size = Size(ribbonWidth.toPx(), ribbonHeight.toPx())
                )

                // Folded bottom triangle
                val foldPath = Path().apply {
                    moveTo(0f, ribbonHeight.toPx())
                    lineTo(ribbonWidth.toPx() / 2, ribbonHeight.toPx() + foldHeight.toPx())
                    lineTo(ribbonWidth.toPx(), ribbonHeight.toPx())
                    close()
                }
                drawPath(
                    path = foldPath,
                    color = foldColor
                )
            },
        contentAlignment = Alignment.TopCenter
    ) {
        Icon(
            imageVector = if (isFavorited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isFavorited) "Remove from favorites" else "Add to favorites",
            modifier = Modifier
                .padding(top = 16.dp)
                .size(14.dp)
                .scale(scale),
            tint = iconTint
        )
    }
}

// MARK: - Small Bookmark Badge

/**
 * Smaller bookmark indicator for list views
 */
@Composable
fun BookmarkBadge(
    modifier: Modifier = Modifier
) {
    val tokens = LocalTemplateTokens.current

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = tokens.palette.accent.copy(alpha = 0.15f)
    ) {
        Icon(
            imageVector = Icons.Filled.Bookmark,
            contentDescription = "Favorited",
            modifier = Modifier
                .padding(6.dp)
                .size(12.dp),
            tint = tokens.palette.accent
        )
    }
}
