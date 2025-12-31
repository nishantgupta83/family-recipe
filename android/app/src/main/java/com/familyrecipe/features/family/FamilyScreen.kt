package com.familyrecipe.features.family

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.familyrecipe.core.models.FamilyMember
import com.familyrecipe.designsystem.FamilyRecipeThemeTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyScreen() {
    val tokens = FamilyRecipeThemeTokens.tokens

    // Sample data for display
    val sampleMembers = listOf(
        FamilyMember(name = "Mom", avatarEmoji = "\uD83D\uDC69\u200D\uD83C\uDF73", familyId = "sample"),
        FamilyMember(name = "Dad", avatarEmoji = "\uD83D\uDC68\u200D\uD83C\uDF73", familyId = "sample"),
        FamilyMember(name = "Grandma", avatarEmoji = "\uD83D\uDC75", familyId = "sample")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Family") },
                actions = {
                    IconButton(onClick = { /* Share invite code */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Invite")
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
            // Invite code section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = tokens.palette.secondary
                )
            ) {
                Column(
                    modifier = Modifier.padding(tokens.spacing.md)
                ) {
                    Text(
                        text = "Invite Code",
                        style = tokens.typography.labelMedium,
                        color = tokens.palette.textSecondary
                    )
                    Spacer(modifier = Modifier.height(tokens.spacing.xs))
                    Text(
                        text = "ABC123",
                        style = tokens.typography.displayMedium,
                        color = tokens.palette.primary
                    )
                    Spacer(modifier = Modifier.height(tokens.spacing.xs))
                    Text(
                        text = "Share this code with family members to join",
                        style = tokens.typography.caption,
                        color = tokens.palette.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(tokens.spacing.lg))

            Text(
                text = "Members",
                style = tokens.typography.titleMedium,
                color = tokens.palette.text
            )

            Spacer(modifier = Modifier.height(tokens.spacing.sm))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(tokens.spacing.sm)
            ) {
                items(sampleMembers) { member ->
                    MemberCard(member = member)
                }
            }
        }
    }
}

@Composable
fun MemberCard(member: FamilyMember) {
    val tokens = FamilyRecipeThemeTokens.tokens

    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Text(
                text = member.avatarEmoji,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.size(48.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = tokens.spacing.md)
            ) {
                Text(
                    text = member.name,
                    style = tokens.typography.titleMedium,
                    color = tokens.palette.text
                )
                Text(
                    text = member.role.displayName,
                    style = tokens.typography.caption,
                    color = tokens.palette.textSecondary
                )
            }

            Text(
                text = "${member.recipesCreatedCount} recipes",
                style = tokens.typography.labelMedium,
                color = tokens.palette.textSecondary
            )
        }
    }
}
