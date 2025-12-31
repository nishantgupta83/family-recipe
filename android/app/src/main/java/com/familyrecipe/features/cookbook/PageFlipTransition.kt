package com.familyrecipe.features.cookbook

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.absoluteValue

// MARK: - Page Flip Transition

/**
 * Modifier that applies 3D rotation effect during page transitions
 * Creates a book-like flip animation when swiping between pages
 *
 * @param pageOffset The offset of the page relative to current position
 *                   -1 = previous page, 0 = current, 1 = next
 */
fun Modifier.pageFlipTransition(
    pageOffset: Float
): Modifier = this.graphicsLayer {
    val absOffset = pageOffset.absoluteValue

    // Configuration
    val maxRotation = 45f
    val perspective = 0.5f

    // Only apply effects to visible pages (current and adjacent)
    if (absOffset <= 1f) {
        // 3D rotation angle
        rotationY = pageOffset * -maxRotation

        // Set camera distance for perspective
        cameraDistance = 8f * density

        // Anchor point - rotate from edge like book spine
        transformOrigin = if (pageOffset < 0) {
            androidx.compose.ui.graphics.TransformOrigin(1f, 0.5f) // Right edge
        } else {
            androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f) // Left edge
        }

        // Scale down slightly when not current
        scaleX = 1f - (absOffset * 0.05f)
        scaleY = 1f - (absOffset * 0.05f)

        // Fade adjacent pages slightly
        alpha = 1f - (absOffset * 0.3f)
    } else {
        // Hide pages beyond adjacent
        alpha = 0f
    }
}

// MARK: - Alternative Simple Transition

/**
 * Simpler page transition without 3D rotation
 * Use as fallback if 3D causes performance issues
 */
fun Modifier.simplePageTransition(
    pageOffset: Float
): Modifier = this.graphicsLayer {
    val absOffset = pageOffset.absoluteValue

    if (absOffset <= 1f) {
        // Simple scale and fade
        scaleX = 1f - (absOffset * 0.1f)
        scaleY = 1f - (absOffset * 0.1f)
        alpha = 1f - (absOffset * 0.5f)
    } else {
        alpha = 0f
    }
}

// MARK: - Page Shadow Modifier

/**
 * Adds realistic shadow during page transitions
 * Shadow intensifies as page lifts during flip
 */
fun Modifier.pageShadow(
    pageOffset: Float,
    isFlipping: Boolean
): Modifier = this.graphicsLayer {
    if (isFlipping && pageOffset.absoluteValue <= 1f) {
        // Shadow offset based on flip direction
        val shadowOffset = pageOffset * 8f
        translationX = shadowOffset
    }
}

// MARK: - Parallax Effect

/**
 * Parallax effect for content inside pages
 * Creates depth illusion during page transitions
 */
fun Modifier.parallaxContent(
    pageOffset: Float,
    parallaxFactor: Float = 0.5f
): Modifier = this.graphicsLayer {
    if (pageOffset.absoluteValue <= 1f) {
        translationX = pageOffset * 100f * parallaxFactor
    }
}
