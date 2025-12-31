package com.familyrecipe.features.scan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.familyrecipe.core.models.*
import com.familyrecipe.designsystem.LocalTemplateTokens
import java.util.Date
import java.util.UUID

/**
 * Review and edit OCR-parsed recipe before saving
 * Rule: OCR output is NEVER auto-saved without review
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewOCRScreen(
    viewModel: ScanRecipeViewModel,
    familyId: String,
    createdById: String,
    onNavigateBack: () -> Unit,
    onSaveComplete: () -> Unit
) {
    val tokens = LocalTemplateTokens.current

    // Editable state
    var title by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf<List<EditableIngredient>>(emptyList()) }
    var instructions by remember { mutableStateOf<List<EditableInstruction>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf(RecipeCategory.DINNER) }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.MEDIUM) }
    var prepTime by remember { mutableIntStateOf(15) }
    var cookTime by remember { mutableIntStateOf(30) }
    var servings by remember { mutableIntStateOf(4) }
    var familyMemory by remember { mutableStateOf("") }
    var showRawText by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Load parsed data on first composition
    LaunchedEffect(Unit) {
        viewModel.parsedRecipe?.let { parsed ->
            title = parsed.title
            ingredients = parsed.ingredients.mapIndexed { index, text ->
                EditableIngredient(
                    id = UUID.randomUUID().toString(),
                    text = text,
                    hasLowConfidence = checkLowConfidence(text, viewModel)
                )
            }
            instructions = parsed.instructions.mapIndexed { index, text ->
                EditableInstruction(
                    id = UUID.randomUUID().toString(),
                    stepNumber = index + 1,
                    text = text,
                    hasLowConfidence = checkLowConfidence(text, viewModel)
                )
            }
        }
    }

    val canSave = title.trim().isNotEmpty() && ingredients.isNotEmpty() && instructions.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Recipe") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { showRawText = !showRawText }) {
                        Text(
                            "Edit Raw",
                            style = tokens.typography.labelMedium,
                            color = tokens.palette.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = tokens.palette.background
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = tokens.palette.background,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        isSaving = true
                        val recipe = createRecipe(
                            title = title,
                            ingredients = ingredients,
                            instructions = instructions,
                            category = selectedCategory,
                            difficulty = selectedDifficulty,
                            prepTime = prepTime,
                            cookTime = cookTime,
                            servings = servings,
                            familyMemory = familyMemory,
                            familyId = familyId,
                            createdById = createdById,
                            viewModel = viewModel
                        )
                        // In a real app, save to repository here
                        onSaveComplete()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(tokens.spacing.lg)
                        .height(56.dp),
                    enabled = canSave && !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canSave) tokens.palette.primary else tokens.palette.textSecondary
                    ),
                    shape = RoundedCornerShape(tokens.shape.cornerRadiusMedium)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(tokens.spacing.sm))
                    Text("Save Recipe", style = tokens.typography.labelLarge)
                }
            }
        },
        containerColor = tokens.palette.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = tokens.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(tokens.spacing.lg)
        ) {
            // Confidence banner
            item {
                viewModel.ocrResult?.let { result ->
                    ConfidenceBanner(confidence = result.confidence, tokens = tokens)
                }
            }

            // Title section
            item {
                TitleSection(
                    title = title,
                    onTitleChange = { title = it },
                    tokens = tokens
                )
            }

            item { Divider(color = tokens.palette.divider) }

            // Ingredients section
            item {
                SectionHeader(
                    title = "Ingredients",
                    required = true,
                    onAdd = {
                        ingredients = ingredients + EditableIngredient(
                            id = UUID.randomUUID().toString(),
                            text = "",
                            hasLowConfidence = false
                        )
                    },
                    tokens = tokens
                )
            }

            if (ingredients.isEmpty()) {
                item {
                    EmptyState(message = "No ingredients detected. Tap + to add.", tokens = tokens)
                }
            } else {
                itemsIndexed(ingredients, key = { _, item -> item.id }) { index, ingredient ->
                    IngredientRow(
                        ingredient = ingredient,
                        onTextChange = { newText ->
                            ingredients = ingredients.mapIndexed { i, item ->
                                if (i == index) item.copy(text = newText) else item
                            }
                        },
                        onRemove = {
                            ingredients = ingredients.filterIndexed { i, _ -> i != index }
                        },
                        tokens = tokens
                    )
                }
            }

            item { Divider(color = tokens.palette.divider) }

            // Instructions section
            item {
                SectionHeader(
                    title = "Instructions",
                    required = true,
                    onAdd = {
                        val nextStep = (instructions.maxOfOrNull { it.stepNumber } ?: 0) + 1
                        instructions = instructions + EditableInstruction(
                            id = UUID.randomUUID().toString(),
                            stepNumber = nextStep,
                            text = "",
                            hasLowConfidence = false
                        )
                    },
                    tokens = tokens
                )
            }

            if (instructions.isEmpty()) {
                item {
                    EmptyState(message = "No instructions detected. Tap + to add.", tokens = tokens)
                }
            } else {
                itemsIndexed(instructions, key = { _, item -> item.id }) { index, instruction ->
                    InstructionRow(
                        instruction = instruction,
                        onTextChange = { newText ->
                            instructions = instructions.mapIndexed { i, item ->
                                if (i == index) item.copy(text = newText) else item
                            }
                        },
                        onRemove = {
                            instructions = instructions.filterIndexed { i, _ -> i != index }
                                .mapIndexed { i, item -> item.copy(stepNumber = i + 1) }
                        },
                        tokens = tokens
                    )
                }
            }

            item { Divider(color = tokens.palette.divider) }

            // Metadata section
            item {
                MetadataSection(
                    category = selectedCategory,
                    onCategoryChange = { selectedCategory = it },
                    difficulty = selectedDifficulty,
                    onDifficultyChange = { selectedDifficulty = it },
                    prepTime = prepTime,
                    onPrepTimeChange = { prepTime = it },
                    cookTime = cookTime,
                    onCookTimeChange = { cookTime = it },
                    servings = servings,
                    onServingsChange = { servings = it },
                    tokens = tokens
                )
            }

            // Family memory section
            item {
                FamilyMemorySection(
                    familyMemory = familyMemory,
                    onFamilyMemoryChange = { familyMemory = it },
                    tokens = tokens
                )
            }

            // Raw text section
            item {
                RawTextSection(
                    rawText = viewModel.rawOCRText,
                    isExpanded = showRawText,
                    onToggle = { showRawText = !showRawText },
                    tokens = tokens
                )
            }

            // Bottom spacing for FAB
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun ConfidenceBanner(
    confidence: Double,
    tokens: com.familyrecipe.designsystem.TemplateTokens
) {
    val isLow = confidence < 0.7
    val color = if (isLow) tokens.palette.error else tokens.palette.success

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.shape.cornerRadiusMedium))
            .background(color.copy(alpha = 0.1f))
            .padding(tokens.spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isLow) Icons.Default.Warning else Icons.Default.CheckCircle,
            contentDescription = null,
            tint = color
        )
        Spacer(modifier = Modifier.width(tokens.spacing.sm))
        Column {
            Text(
                text = if (isLow) "Review carefully" else "Good scan quality",
                style = tokens.typography.labelMedium,
                color = tokens.palette.text
            )
            Text(
                text = "OCR Confidence: ${(confidence * 100).toInt()}%",
                style = tokens.typography.caption,
                color = tokens.palette.textSecondary
            )
        }
    }
}

@Composable
private fun TitleSection(
    title: String,
    onTitleChange: (String) -> Unit,
    tokens: com.familyrecipe.designsystem.TemplateTokens
) {
    Column {
        SectionLabel(text = "Recipe Title", required = true, tokens = tokens)
        Spacer(modifier = Modifier.height(tokens.spacing.sm))
        BasicTextField(
            value = title,
            onValueChange = onTitleChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(tokens.shape.cornerRadiusSmall))
                .background(tokens.palette.surface)
                .border(1.dp, tokens.palette.divider, RoundedCornerShape(tokens.shape.cornerRadiusSmall))
                .padding(tokens.spacing.md),
            textStyle = tokens.typography.titleMedium.copy(color = tokens.palette.text),
            cursorBrush = SolidColor(tokens.palette.primary),
            decorationBox = { innerTextField ->
                if (title.isEmpty()) {
                    Text(
                        text = "Enter recipe title",
                        style = tokens.typography.titleMedium,
                        color = tokens.palette.textSecondary
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    required: Boolean,
    onAdd: () -> Unit,
    tokens: com.familyrecipe.designsystem.TemplateTokens
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SectionLabel(text = title, required = required, tokens = tokens)
        IconButton(onClick = onAdd) {
            Icon(
                Icons.Default.AddCircle,
                contentDescription = "Add",
                tint = tokens.palette.primary
            )
        }
    }
}

@Composable
private fun SectionLabel(
    text: String,
    required: Boolean,
    tokens: com.familyrecipe.designsystem.TemplateTokens
) {
    Row {
        Text(
            text = text,
            style = tokens.typography.labelLarge,
            color = tokens.palette.text
        )
        if (required) {
            Text(
                text = " *",
                style = tokens.typography.labelLarge,
                color = tokens.palette.error
            )
        }
    }
}

@Composable
private fun EmptyState(
    message: String,
    tokens: com.familyrecipe.designsystem.TemplateTokens
) {
    Text(
        text = message,
        style = tokens.typography.caption,
        color = tokens.palette.textSecondary,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.shape.cornerRadiusSmall))
            .background(tokens.palette.surface)
            .padding(tokens.spacing.md)
    )
}

@Composable
private fun IngredientRow(
    ingredient: EditableIngredient,
    onTextChange: (String) -> Unit,
    onRemove: () -> Unit,
    tokens: com.familyrecipe.designsystem.TemplateTokens
) {
    val borderColor = if (ingredient.hasLowConfidence) {
        tokens.palette.error.copy(alpha = 0.3f)
    } else {
        tokens.palette.divider
    }
    val bgColor = if (ingredient.hasLowConfidence) {
        tokens.palette.error.copy(alpha = 0.05f)
    } else {
        tokens.palette.surface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.shape.cornerRadiusSmall))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(tokens.shape.cornerRadiusSmall))
            .padding(tokens.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (ingredient.hasLowConfidence) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(tokens.palette.error.copy(alpha = 0.6f))
            )
            Spacer(modifier = Modifier.width(tokens.spacing.sm))
        }

        BasicTextField(
            value = ingredient.text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            textStyle = tokens.typography.bodyMedium.copy(color = tokens.palette.text),
            cursorBrush = SolidColor(tokens.palette.primary),
            decorationBox = { innerTextField ->
                if (ingredient.text.isEmpty()) {
                    Text(
                        text = "Ingredient",
                        style = tokens.typography.bodyMedium,
                        color = tokens.palette.textSecondary
                    )
                }
                innerTextField()
            }
        )

        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Default.Cancel,
                contentDescription = "Remove",
                tint = tokens.palette.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun InstructionRow(
    instruction: EditableInstruction,
    onTextChange: (String) -> Unit,
    onRemove: () -> Unit,
    tokens: com.familyrecipe.designsystem.TemplateTokens
) {
    val borderColor = if (instruction.hasLowConfidence) {
        tokens.palette.error.copy(alpha = 0.3f)
    } else {
        tokens.palette.divider
    }
    val bgColor = if (instruction.hasLowConfidence) {
        tokens.palette.error.copy(alpha = 0.05f)
    } else {
        tokens.palette.surface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.shape.cornerRadiusSmall))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(tokens.shape.cornerRadiusSmall))
            .padding(tokens.spacing.sm),
        verticalAlignment = Alignment.Top
    ) {
        // Step number
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(tokens.palette.secondary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = instruction.stepNumber.toString(),
                style = tokens.typography.labelLarge,
                color = tokens.palette.primary
            )
        }

        Spacer(modifier = Modifier.width(tokens.spacing.sm))

        if (instruction.hasLowConfidence) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(tokens.palette.error.copy(alpha = 0.6f))
            )
            Spacer(modifier = Modifier.width(tokens.spacing.sm))
        }

        BasicTextField(
            value = instruction.text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            textStyle = tokens.typography.bodyMedium.copy(color = tokens.palette.text),
            cursorBrush = SolidColor(tokens.palette.primary),
            decorationBox = { innerTextField ->
                if (instruction.text.isEmpty()) {
                    Text(
                        text = "Instruction",
                        style = tokens.typography.bodyMedium,
                        color = tokens.palette.textSecondary
                    )
                }
                innerTextField()
            }
        )

        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Default.Cancel,
                contentDescription = "Remove",
                tint = tokens.palette.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MetadataSection(
    category: RecipeCategory,
    onCategoryChange: (RecipeCategory) -> Unit,
    difficulty: Difficulty,
    onDifficultyChange: (Difficulty) -> Unit,
    prepTime: Int,
    onPrepTimeChange: (Int) -> Unit,
    cookTime: Int,
    onCookTimeChange: (Int) -> Unit,
    servings: Int,
    onServingsChange: (Int) -> Unit,
    tokens: com.familyrecipe.designsystem.TemplateTokens
) {
    var categoryExpanded by remember { mutableStateOf(false) }
    var difficultyExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.shape.cornerRadiusMedium))
            .background(tokens.palette.surface)
            .padding(tokens.spacing.md),
        verticalArrangement = Arrangement.spacedBy(tokens.spacing.md)
    ) {
        Text(
            text = "Details",
            style = tokens.typography.labelLarge,
            color = tokens.palette.text
        )

        // Category dropdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Category", style = tokens.typography.bodyMedium, color = tokens.palette.textSecondary)
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                TextButton(
                    onClick = { },
                    modifier = Modifier.menuAnchor()
                ) {
                    Text(category.displayName)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    RecipeCategory.entries.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.displayName) },
                            onClick = {
                                onCategoryChange(cat)
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Difficulty dropdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Difficulty", style = tokens.typography.bodyMedium, color = tokens.palette.textSecondary)
            ExposedDropdownMenuBox(
                expanded = difficultyExpanded,
                onExpandedChange = { difficultyExpanded = !difficultyExpanded }
            ) {
                TextButton(
                    onClick = { },
                    modifier = Modifier.menuAnchor()
                ) {
                    Text(difficulty.displayName)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
                ExposedDropdownMenu(
                    expanded = difficultyExpanded,
                    onDismissRequest = { difficultyExpanded = false }
                ) {
                    Difficulty.entries.forEach { diff ->
                        DropdownMenuItem(
                            text = { Text(diff.displayName) },
                            onClick = {
                                onDifficultyChange(diff)
                                difficultyExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Time pickers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.spacing.lg)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Prep Time", style = tokens.typography.caption, color = tokens.palette.textSecondary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (prepTime > 0) onPrepTimeChange(prepTime - 5) }) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    }
                    Text("$prepTime min", style = tokens.typography.bodyMedium)
                    IconButton(onClick = { if (prepTime < 480) onPrepTimeChange(prepTime + 5) }) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text("Cook Time", style = tokens.typography.caption, color = tokens.palette.textSecondary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (cookTime > 0) onCookTimeChange(cookTime - 5) }) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    }
                    Text("$cookTime min", style = tokens.typography.bodyMedium)
                    IconButton(onClick = { if (cookTime < 480) onCookTimeChange(cookTime + 5) }) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }
                }
            }
        }

        // Servings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Servings", style = tokens.typography.bodyMedium, color = tokens.palette.textSecondary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (servings > 1) onServingsChange(servings - 1) }) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                }
                Text("$servings", style = tokens.typography.bodyMedium)
                IconButton(onClick = { if (servings < 50) onServingsChange(servings + 1) }) {
                    Icon(Icons.Default.Add, contentDescription = "Increase")
                }
            }
        }
    }
}

@Composable
private fun FamilyMemorySection(
    familyMemory: String,
    onFamilyMemoryChange: (String) -> Unit,
    tokens: com.familyrecipe.designsystem.TemplateTokens
) {
    Column {
        Text(
            text = "Family Memory",
            style = tokens.typography.labelLarge,
            color = tokens.palette.text
        )
        Text(
            text = "Add a personal note about this recipe",
            style = tokens.typography.caption,
            color = tokens.palette.textSecondary
        )
        Spacer(modifier = Modifier.height(tokens.spacing.sm))
        BasicTextField(
            value = familyMemory,
            onValueChange = onFamilyMemoryChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(tokens.shape.cornerRadiusSmall))
                .background(tokens.palette.secondary.copy(alpha = 0.3f))
                .padding(tokens.spacing.md),
            textStyle = tokens.typography.handwritten.copy(color = tokens.palette.text),
            cursorBrush = SolidColor(tokens.palette.primary),
            decorationBox = { innerTextField ->
                if (familyMemory.isEmpty()) {
                    Text(
                        text = "e.g., Grandma used to make this every Sunday...",
                        style = tokens.typography.handwritten,
                        color = tokens.palette.textSecondary
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
private fun RawTextSection(
    rawText: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    tokens: com.familyrecipe.designsystem.TemplateTokens
) {
    Column {
        TextButton(onClick = onToggle) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Raw OCR Text",
                    style = tokens.typography.labelMedium,
                    color = tokens.palette.textSecondary
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = tokens.palette.textSecondary
                )
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Text(
                text = rawText,
                style = tokens.typography.caption.copy(fontFamily = FontFamily.Monospace),
                color = tokens.palette.textSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(tokens.shape.cornerRadiusSmall))
                    .background(tokens.palette.surface)
                    .padding(tokens.spacing.md)
            )
        }
    }
}

// MARK: - Helper Types

data class EditableIngredient(
    val id: String,
    val text: String,
    val hasLowConfidence: Boolean
)

data class EditableInstruction(
    val id: String,
    val stepNumber: Int,
    val text: String,
    val hasLowConfidence: Boolean
)

private fun checkLowConfidence(text: String, viewModel: ScanRecipeViewModel): Boolean {
    val result = viewModel.ocrResult ?: return false
    val fullText = viewModel.rawOCRText
    val start = fullText.indexOf(text)
    if (start == -1) return false
    val end = start + text.length

    return result.lowConfidenceRanges.any { range ->
        range.start < end && range.end > start
    }
}

private fun createRecipe(
    title: String,
    ingredients: List<EditableIngredient>,
    instructions: List<EditableInstruction>,
    category: RecipeCategory,
    difficulty: Difficulty,
    prepTime: Int,
    cookTime: Int,
    servings: Int,
    familyMemory: String,
    familyId: String,
    createdById: String,
    viewModel: ScanRecipeViewModel
): Recipe {
    val recipeIngredients = ingredients.map { editable ->
        parseIngredient(editable.text)
    }

    val recipeInstructions = instructions.mapIndexed { index, editable ->
        Instruction(
            stepNumber = index + 1,
            text = editable.text
        )
    }

    val scannedSource = viewModel.ocrResult?.let { result ->
        ScannedSource(
            images = emptyList(),
            rawOCRText = viewModel.rawOCRText,
            confidenceScore = result.confidence,
            lowConfidenceRanges = result.lowConfidenceRanges
        )
    }

    return Recipe(
        title = title.trim(),
        recipeDescription = "",
        ingredients = recipeIngredients,
        instructions = recipeInstructions,
        category = category,
        difficulty = difficulty,
        prepTimeMinutes = prepTime,
        cookTimeMinutes = cookTime,
        servings = servings,
        familyId = familyId,
        createdById = createdById,
        familyMemory = familyMemory.takeIf { it.isNotEmpty() },
        scannedSource = scannedSource
    )
}

private fun parseIngredient(text: String): Ingredient {
    // Simple parsing
    val pattern = Regex("^([0-9½¼¾⅓⅔⅛⅜⅝⅞/.]+)?\\s*([a-zA-Z]+\\.?)?\\s*(.+)$")
    val match = pattern.find(text)

    return if (match != null) {
        val amountString = match.groupValues[1].ifEmpty { "1" }
        val unitString = match.groupValues[2]
        val name = match.groupValues[3].ifEmpty { text }

        Ingredient(
            name = name.trim(),
            amount = parseAmount(amountString),
            unit = parseUnit(unitString)
        )
    } else {
        Ingredient(name = text, amount = 1.0, unit = MeasurementUnit.PIECE)
    }
}

private fun parseAmount(string: String): Double {
    val fractionMap = mapOf(
        "½" to 0.5, "¼" to 0.25, "¾" to 0.75,
        "⅓" to 0.333, "⅔" to 0.667,
        "⅛" to 0.125, "⅜" to 0.375, "⅝" to 0.625, "⅞" to 0.875
    )

    for ((fraction, value) in fractionMap) {
        if (string.contains(fraction)) {
            val whole = string.replace(fraction, "").trim()
            val wholeValue = whole.toDoubleOrNull() ?: 0.0
            return wholeValue + value
        }
    }

    if (string.contains("/")) {
        val parts = string.split("/")
        if (parts.size == 2) {
            val numerator = parts[0].toDoubleOrNull()
            val denominator = parts[1].toDoubleOrNull()
            if (numerator != null && denominator != null && denominator != 0.0) {
                return numerator / denominator
            }
        }
    }

    return string.toDoubleOrNull() ?: 1.0
}

private fun parseUnit(string: String): MeasurementUnit {
    val unitMap = mapOf(
        "cup" to MeasurementUnit.CUP, "cups" to MeasurementUnit.CUP, "c" to MeasurementUnit.CUP,
        "tbsp" to MeasurementUnit.TABLESPOON, "tablespoon" to MeasurementUnit.TABLESPOON,
        "tsp" to MeasurementUnit.TEASPOON, "teaspoon" to MeasurementUnit.TEASPOON,
        "oz" to MeasurementUnit.OUNCE, "ounce" to MeasurementUnit.OUNCE,
        "lb" to MeasurementUnit.POUND, "pound" to MeasurementUnit.POUND,
        "g" to MeasurementUnit.GRAM, "gram" to MeasurementUnit.GRAM,
        "kg" to MeasurementUnit.KILOGRAM,
        "ml" to MeasurementUnit.MILLILITER,
        "l" to MeasurementUnit.LITER,
        "clove" to MeasurementUnit.CLOVE,
        "pinch" to MeasurementUnit.PINCH, "dash" to MeasurementUnit.DASH
    )

    return unitMap[string.lowercase()] ?: MeasurementUnit.PIECE
}
