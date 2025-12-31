package com.familyrecipe.features.scan

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Portrait
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familyrecipe.designsystem.LocalTemplateTokens
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.graphics.BitmapFactory
import android.content.ContentResolver

/**
 * Entry point for scanning recipes using ML Kit Document Scanner
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanRecipeScreen(
    onNavigateBack: () -> Unit,
    onNavigateToReview: (ScanRecipeViewModel) -> Unit,
    viewModel: ScanRecipeViewModel = remember { ScanRecipeViewModel() }
) {
    val tokens = LocalTemplateTokens.current
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.processScannedImages(uris, context.contentResolver)
        }
    }

    // Navigate to review when ready
    LaunchedEffect(uiState.showReview) {
        if (uiState.showReview) {
            onNavigateToReview(viewModel)
            viewModel.resetReviewFlag()
        }
    }

    // Error dialog
    if (uiState.showError) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissError() },
            title = { Text("Scanning Error") },
            text = { Text(uiState.errorMessage) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissError() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Recipe") },
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
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Icon
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = tokens.palette.primary
            )

            Spacer(modifier = Modifier.height(tokens.spacing.lg))

            // Title and description
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Scan Your Recipe",
                    style = tokens.typography.titleLarge,
                    color = tokens.palette.text
                )

                Spacer(modifier = Modifier.height(tokens.spacing.sm))

                Text(
                    text = "Take a photo of a handwritten or printed recipe to digitize it",
                    style = tokens.typography.bodyMedium,
                    color = tokens.palette.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = tokens.spacing.xl)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Processing indicator
            if (uiState.isProcessing) {
                Column(
                    modifier = Modifier.padding(vertical = tokens.spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = { uiState.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = tokens.spacing.xl),
                        color = tokens.palette.primary,
                        trackColor = tokens.palette.divider
                    )

                    Spacer(modifier = Modifier.height(tokens.spacing.md))

                    Text(
                        text = "Processing scan...",
                        style = tokens.typography.caption,
                        color = tokens.palette.textSecondary
                    )
                }
            }

            // Action button
            Button(
                onClick = {
                    imagePickerLauncher.launch("image/*")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isProcessing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = tokens.palette.primary
                ),
                shape = RoundedCornerShape(tokens.shape.cornerRadiusMedium)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(tokens.spacing.sm))
                Text(
                    text = "Select Images",
                    style = tokens.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(tokens.spacing.md))

            // Tips section
            TipsSection(tokens)

            Spacer(modifier = Modifier.height(tokens.spacing.xl))
        }
    }
}

@Composable
private fun TipsSection(tokens: com.familyrecipe.designsystem.TemplateTokens) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.shape.cornerRadiusMedium))
            .background(tokens.palette.secondary.copy(alpha = 0.5f))
            .padding(tokens.spacing.md)
    ) {
        Text(
            text = "Tips for best results:",
            style = tokens.typography.labelMedium,
            color = tokens.palette.textSecondary
        )

        Spacer(modifier = Modifier.height(tokens.spacing.sm))

        TipRow(
            icon = Icons.Default.LightMode,
            text = "Ensure good lighting",
            tokens = tokens
        )
        TipRow(
            icon = Icons.Default.Portrait,
            text = "Keep the recipe flat",
            tokens = tokens
        )
        TipRow(
            icon = Icons.Default.PanTool,
            text = "Hold steady while scanning",
            tokens = tokens
        )
        TipRow(
            icon = Icons.Default.FileCopy,
            text = "Select multiple pages if needed",
            tokens = tokens
        )
    }
}

@Composable
private fun TipRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    tokens: com.familyrecipe.designsystem.TemplateTokens
) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = tokens.palette.primary
        )
        Spacer(modifier = Modifier.width(tokens.spacing.sm))
        Text(
            text = text,
            style = tokens.typography.caption,
            color = tokens.palette.text
        )
    }
}

/**
 * ViewModel for scan recipe flow
 */
class ScanRecipeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ScanRecipeUiState())
    val uiState: StateFlow<ScanRecipeUiState> = _uiState.asStateFlow()

    private val ocrService = OCRService()

    // Results for ReviewOCRScreen
    var ocrResult: OCRService.OCRResult? = null
        private set
    var parsedRecipe: ParsedRecipe? = null
        private set
    var rawOCRText: String = ""
        private set
    var scannedBitmaps: List<Bitmap> = emptyList()
        private set

    fun processScannedImages(uris: List<Uri>, contentResolver: ContentResolver) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                progress = 0f
            )

            try {
                // Convert URIs to bitmaps
                val bitmaps = uris.mapNotNull { uri ->
                    try {
                        contentResolver.openInputStream(uri)?.use { inputStream ->
                            BitmapFactory.decodeStream(inputStream)
                        }
                    } catch (e: Exception) {
                        null
                    }
                }

                if (bitmaps.isEmpty()) {
                    throw OCRService.OCRError.ProcessingFailed("Could not load images")
                }

                scannedBitmaps = bitmaps
                _uiState.value = _uiState.value.copy(progress = 0.3f)

                // Process OCR
                val result = ocrService.processImages(bitmaps)
                ocrResult = result
                rawOCRText = result.text
                _uiState.value = _uiState.value.copy(progress = 0.7f)

                // Parse into recipe structure
                parsedRecipe = ocrService.parseRecipeText(result.text)
                _uiState.value = _uiState.value.copy(progress = 1f)

                // Show review screen
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    showReview = true
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    showError = true,
                    errorMessage = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(showError = false)
    }

    fun resetReviewFlag() {
        _uiState.value = _uiState.value.copy(showReview = false)
    }

    fun reset() {
        ocrResult = null
        parsedRecipe = null
        rawOCRText = ""
        scannedBitmaps = emptyList()
        _uiState.value = ScanRecipeUiState()
    }
}

data class ScanRecipeUiState(
    val isProcessing: Boolean = false,
    val progress: Float = 0f,
    val showError: Boolean = false,
    val errorMessage: String = "",
    val showReview: Boolean = false
)
