package com.familyrecipe.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.familyrecipe.core.models.TemplateKey
import com.familyrecipe.designsystem.FamilyRecipeThemeTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val tokens = FamilyRecipeThemeTokens.tokens

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = tokens.palette.background
                )
            )
        },
        containerColor = tokens.palette.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(tokens.spacing.md)
        ) {
            // Appearance section
            item {
                Text(
                    text = "Appearance",
                    style = tokens.typography.labelMedium,
                    color = tokens.palette.textSecondary
                )
                Spacer(modifier = Modifier.height(tokens.spacing.sm))
            }

            item {
                SettingsCard(
                    title = "Theme",
                    subtitle = "Vintage Cookbook",
                    onClick = { }
                )
            }

            // Preferences section
            item {
                Spacer(modifier = Modifier.height(tokens.spacing.lg))
                Text(
                    text = "Preferences",
                    style = tokens.typography.labelMedium,
                    color = tokens.palette.textSecondary
                )
                Spacer(modifier = Modifier.height(tokens.spacing.sm))
            }

            item {
                SettingsCard(
                    title = "Language",
                    subtitle = "English",
                    onClick = { }
                )
            }

            item {
                Spacer(modifier = Modifier.height(tokens.spacing.sm))
                SettingsCard(
                    title = "Units",
                    subtitle = "Imperial (cups, °F)",
                    onClick = { }
                )
            }

            // Cooking Mode section
            item {
                Spacer(modifier = Modifier.height(tokens.spacing.lg))
                Text(
                    text = "Cooking Mode",
                    style = tokens.typography.labelMedium,
                    color = tokens.palette.textSecondary
                )
                Spacer(modifier = Modifier.height(tokens.spacing.sm))
            }

            item {
                SettingsCard(
                    title = "Voice Assistant",
                    subtitle = "Off",
                    onClick = { }
                )
            }

            item {
                Spacer(modifier = Modifier.height(tokens.spacing.sm))
                SettingsCard(
                    title = "Keep Screen On",
                    subtitle = "Enabled",
                    onClick = { }
                )
            }

            // About section
            item {
                Spacer(modifier = Modifier.height(tokens.spacing.lg))
                Text(
                    text = "About",
                    style = tokens.typography.labelMedium,
                    color = tokens.palette.textSecondary
                )
                Spacer(modifier = Modifier.height(tokens.spacing.sm))
            }

            item {
                SettingsCard(
                    title = "Version",
                    subtitle = "1.0.0",
                    onClick = { }
                )
            }
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val tokens = FamilyRecipeThemeTokens.tokens

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = tokens.palette.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = tokens.typography.bodyLarge,
                    color = tokens.palette.text
                )
                Text(
                    text = subtitle,
                    style = tokens.typography.bodyMedium,
                    color = tokens.palette.textSecondary
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = tokens.palette.textSecondary
            )
        }
    }
}
