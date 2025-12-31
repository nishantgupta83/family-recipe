package com.familyrecipe.features.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.familyrecipe.core.models.FamilySamples
import com.familyrecipe.designsystem.FamilyRecipeThemeTokens

@Composable
fun OnboardingScreen(
    onComplete: (memberId: String, familyId: String) -> Unit
) {
    val tokens = FamilyRecipeThemeTokens.tokens

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(tokens.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App icon/illustration
        Text(
            text = "\uD83D\uDCD6\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66",
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(tokens.spacing.xl))

        Text(
            text = "Family Recipes",
            style = tokens.typography.displayMedium,
            color = tokens.palette.text,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(tokens.spacing.md))

        Text(
            text = "Preserve your family's culinary heritage. Share recipes across generations.",
            style = tokens.typography.bodyLarge,
            color = tokens.palette.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(tokens.spacing.xl))

        // Create Family button
        Button(
            onClick = {
                // Create sample family for demo
                val (family, member) = FamilySamples.sampleFamily()
                onComplete(member.id, family.id)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = tokens.palette.primary
            )
        ) {
            Text(
                text = "Create Family",
                style = tokens.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(tokens.spacing.md))

        // Join Family button
        OutlinedButton(
            onClick = {
                // For demo, use sample family
                val (family, member) = FamilySamples.sampleFamily()
                onComplete(member.id, family.id)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Join with Invite Code",
                style = tokens.typography.labelLarge,
                color = tokens.palette.primary
            )
        }
    }
}
