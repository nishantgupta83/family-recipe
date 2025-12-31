package com.familyrecipe.features.cookingmode

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.familyrecipe.core.models.CookingTimer
import com.familyrecipe.core.models.Recipe
import com.familyrecipe.designsystem.FamilyRecipeThemeTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingModeScreen(
    recipe: Recipe,
    onExit: () -> Unit,
    viewModel: CookingModeViewModel = viewModel(
        factory = CookingModeViewModel.Factory(recipe, LocalContext.current.applicationContext)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val tokens = FamilyRecipeThemeTokens.tokens

    var showTimerSheet by remember { mutableStateOf(false) }
    var showAssistantSheet by remember { mutableStateOf(false) }

    // Keep screen on
    DisposableEffect(Unit) {
        viewModel.startSession()
        onDispose {
            // Screen will turn off when leaving
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(tokens.palette.background)
    ) {
        // Header
        CookingHeader(
            recipeTitle = recipe.title,
            currentStep = uiState.currentStepIndex + 1,
            totalSteps = recipe.instructions.size,
            onClose = {
                viewModel.endSession()
                onExit()
            },
            onTimerClick = { showTimerSheet = true }
        )

        // Progress bar
        LinearProgressIndicator(
            progress = { uiState.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = tokens.palette.primary,
            trackColor = tokens.palette.divider
        )

        // Step content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(tokens.spacing.lg)
        ) {
            // Step badge
            Surface(
                shape = CircleShape,
                color = tokens.palette.primary
            ) {
                Text(
                    text = "Step ${uiState.currentStepIndex + 1}",
                    style = tokens.typography.labelLarge,
                    color = Color.White,
                    modifier = Modifier.padding(
                        horizontal = tokens.spacing.md,
                        vertical = tokens.spacing.sm
                    )
                )
            }

            Spacer(modifier = Modifier.height(tokens.spacing.lg))

            // Step text
            Text(
                text = uiState.currentStep?.text ?: "",
                style = tokens.typography.bodyLarge,
                color = tokens.palette.text
            )

            // Duration if available
            uiState.currentStep?.formattedDuration?.let { duration ->
                Spacer(modifier = Modifier.height(tokens.spacing.md))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = tokens.palette.secondary
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(tokens.spacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = tokens.palette.textSecondary
                            )
                            Spacer(modifier = Modifier.width(tokens.spacing.sm))
                            Text(
                                text = duration,
                                style = tokens.typography.bodyMedium,
                                color = tokens.palette.textSecondary
                            )
                        }

                        TextButton(
                            onClick = {
                                uiState.currentStep?.durationSeconds?.let { seconds ->
                                    viewModel.addTimer(seconds * 1000L)
                                }
                            }
                        ) {
                            Text("Start Timer", color = tokens.palette.primary)
                        }
                    }
                }
            }

            // Completed indicator
            if (uiState.isCurrentStepCompleted) {
                Spacer(modifier = Modifier.height(tokens.spacing.md))
                Text(
                    text = "✓ Step completed",
                    style = tokens.typography.labelMedium,
                    color = tokens.palette.success
                )
            }
        }

        // Active timers
        if (uiState.timers.isNotEmpty()) {
            TimerDisplay(
                timers = uiState.timers,
                onRemoveTimer = { viewModel.removeTimer(it) }
            )
        }

        // Navigation controls
        NavigationControls(
            canGoPrevious = uiState.canGoPrevious,
            isLastStep = uiState.isLastStep,
            onPrevious = { viewModel.previousStep() },
            onNext = {
                if (uiState.isLastStep) {
                    viewModel.endSession()
                    onExit()
                } else {
                    viewModel.nextStep()
                }
            }
        )

        // Assistant button
        TextButton(
            onClick = { showAssistantSheet = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = tokens.spacing.sm)
        ) {
            Icon(Icons.Default.Mic, contentDescription = null)
            Spacer(modifier = Modifier.width(tokens.spacing.sm))
            Text("Ask Assistant", color = tokens.palette.primary)
        }
    }

    // Timer sheet
    if (showTimerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTimerSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            TimerSheet(
                timers = uiState.timers,
                onAddTimer = { viewModel.addTimer(it) },
                onRemoveTimer = { viewModel.removeTimer(it) },
                onDismiss = { showTimerSheet = false }
            )
        }
    }

    // Assistant sheet
    if (showAssistantSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAssistantSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            AssistantSheet(
                recipe = recipe,
                workstate = uiState.workstate,
                onAction = { intent ->
                    viewModel.handleAssistantIntent(intent)
                },
                onDismiss = { showAssistantSheet = false }
            )
        }
    }
}

@Composable
private fun CookingHeader(
    recipeTitle: String,
    currentStep: Int,
    totalSteps: Int,
    onClose: () -> Unit,
    onTimerClick: () -> Unit
) {
    val tokens = FamilyRecipeThemeTokens.tokens

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(tokens.spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(Icons.Default.Close, contentDescription = "Close")
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = recipeTitle,
                style = tokens.typography.titleMedium,
                color = tokens.palette.text,
                maxLines = 1
            )
            Text(
                text = "Step $currentStep of $totalSteps",
                style = tokens.typography.caption,
                color = tokens.palette.textSecondary
            )
        }

        IconButton(onClick = onTimerClick) {
            Icon(
                Icons.Default.Timer,
                contentDescription = "Timer",
                tint = tokens.palette.primary
            )
        }
    }
}

@Composable
private fun TimerDisplay(
    timers: List<CookingTimer>,
    onRemoveTimer: (String) -> Unit
) {
    val tokens = FamilyRecipeThemeTokens.tokens

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(tokens.palette.surface)
            .padding(tokens.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm)
    ) {
        items(timers, key = { it.id }) { timer ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (timer.isCompleted)
                        tokens.palette.success.copy(alpha = 0.1f)
                    else tokens.palette.secondary
                )
            ) {
                Row(
                    modifier = Modifier.padding(tokens.spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = timer.label,
                            style = tokens.typography.caption,
                            color = tokens.palette.textSecondary
                        )
                        Text(
                            text = timer.formattedRemaining,
                            style = tokens.typography.titleMedium,
                            color = if (timer.isCompleted) tokens.palette.success else tokens.palette.text
                        )
                    }
                    Spacer(modifier = Modifier.width(tokens.spacing.sm))
                    IconButton(onClick = { onRemoveTimer(timer.id) }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = tokens.palette.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationControls(
    canGoPrevious: Boolean,
    isLastStep: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val tokens = FamilyRecipeThemeTokens.tokens

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(tokens.palette.surface)
            .padding(tokens.spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onPrevious,
            enabled = canGoPrevious
        ) {
            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null)
            Text("Previous")
        }

        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(
                containerColor = tokens.palette.primary
            )
        ) {
            Text(if (isLastStep) "Finish" else "Next")
            if (!isLastStep) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
            }
        }
    }
}

@Composable
private fun TimerSheet(
    timers: List<CookingTimer>,
    onAddTimer: (Long) -> Unit,
    onRemoveTimer: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val tokens = FamilyRecipeThemeTokens.tokens
    var selectedMinutes by remember { mutableStateOf(5) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(tokens.spacing.lg)
    ) {
        Text(
            text = "Set Timer",
            style = tokens.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(tokens.spacing.lg))

        // Quick time options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(1, 2, 5, 10, 15, 30).forEach { minutes ->
                OutlinedButton(
                    onClick = { selectedMinutes = minutes }
                ) {
                    Text("${minutes}m")
                }
            }
        }

        Spacer(modifier = Modifier.height(tokens.spacing.md))

        Button(
            onClick = {
                onAddTimer(selectedMinutes * 60 * 1000L)
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = tokens.palette.primary
            )
        ) {
            Text("Start $selectedMinutes min Timer")
        }

        if (timers.isNotEmpty()) {
            Spacer(modifier = Modifier.height(tokens.spacing.lg))
            Text(
                text = "Active Timers",
                style = tokens.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(tokens.spacing.sm))

            timers.forEach { timer ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = tokens.spacing.xs)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(tokens.spacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(timer.label)
                        Text(
                            timer.formattedRemaining,
                            style = tokens.typography.titleMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(tokens.spacing.xl))
    }
}

@Composable
private fun AssistantSheet(
    recipe: Recipe,
    workstate: com.familyrecipe.core.models.CookingWorkstate,
    onAction: (com.familyrecipe.core.services.assistant.AssistantIntent) -> Unit,
    onDismiss: () -> Unit
) {
    val tokens = FamilyRecipeThemeTokens.tokens
    val assistantEngine = remember { com.familyrecipe.core.services.assistant.AssistantEngine() }

    var query by remember { mutableStateOf("") }
    var response by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(tokens.spacing.lg)
    ) {
        Text(
            text = "Cooking Assistant",
            style = tokens.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        if (response.isNotEmpty()) {
            Spacer(modifier = Modifier.height(tokens.spacing.md))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = tokens.palette.secondary
                )
            ) {
                Text(
                    text = response,
                    style = tokens.typography.bodyLarge,
                    modifier = Modifier.padding(tokens.spacing.md)
                )
            }
        }

        Spacer(modifier = Modifier.height(tokens.spacing.lg))

        // Quick actions
        Text("Quick Actions", style = tokens.typography.labelMedium)
        Spacer(modifier = Modifier.height(tokens.spacing.sm))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm)) {
            val actions = listOf(
                "Next step" to "next",
                "Repeat" to "repeat",
                "What's next?" to "what's next",
                "How long left?" to "how long left"
            )
            items(actions) { (label, query) ->
                OutlinedButton(
                    onClick = {
                        val intent = assistantEngine.classifyIntent(query)
                        response = assistantEngine.generateResponse(intent, workstate, recipe)
                        onAction(intent)
                    }
                ) {
                    Text(label)
                }
            }
        }

        Spacer(modifier = Modifier.height(tokens.spacing.xl))
    }
}
