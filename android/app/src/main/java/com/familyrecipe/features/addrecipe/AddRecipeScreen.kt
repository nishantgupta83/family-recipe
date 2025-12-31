package com.familyrecipe.features.addrecipe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.familyrecipe.designsystem.LocalTemplateTokens

/**
 * Add Recipe Screen with options to scan or manually enter recipes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    onNavigateToScan: () -> Unit,
    onNavigateToManualEntry: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val tokens = LocalTemplateTokens.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Recipe") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = tokens.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Scan option
            AddRecipeOptionCard(
                icon = Icons.Default.CameraAlt,
                title = "Scan Recipe",
                description = "Take a photo of a handwritten or printed recipe",
                iconTint = tokens.palette.primary,
                onClick = onNavigateToScan,
                tokens = tokens
            )

            Spacer(modifier = Modifier.height(tokens.spacing.lg))

            // Manual entry option
            AddRecipeOptionCard(
                icon = Icons.Default.Edit,
                title = "Enter Manually",
                description = "Type in a recipe from memory or another source",
                iconTint = tokens.palette.accent,
                onClick = onNavigateToManualEntry,
                tokens = tokens
            )
        }
    }
}

@Composable
private fun AddRecipeOptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    iconTint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    tokens: com.familyrecipe.designsystem.TemplateTokens
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = tokens.decoration.shadowStyle.elevation,
                shape = RoundedCornerShape(tokens.shape.cornerRadiusMedium)
            )
            .clip(RoundedCornerShape(tokens.shape.cornerRadiusMedium))
            .clickable(onClick = onClick),
        color = tokens.palette.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = iconTint
            )

            Spacer(modifier = Modifier.height(tokens.spacing.md))

            Text(
                text = title,
                style = tokens.typography.titleMedium,
                color = tokens.palette.text
            )

            Spacer(modifier = Modifier.height(tokens.spacing.xs))

            Text(
                text = description,
                style = tokens.typography.caption,
                color = tokens.palette.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Manual Recipe Entry Screen (placeholder)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualRecipeEntryScreen(
    onNavigateBack: () -> Unit,
    onSave: () -> Unit
) {
    val tokens = LocalTemplateTokens.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Recipe") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(tokens.spacing.md)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Recipe Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(tokens.spacing.md))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Full manual entry coming soon...",
                style = tokens.typography.bodyMedium,
                color = tokens.palette.textSecondary,
                modifier = Modifier.padding(vertical = tokens.spacing.md)
            )

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = tokens.palette.primary
                )
            ) {
                Text("Save Recipe")
            }
        }
    }
}
