package com.familyrecipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.familyrecipe.designsystem.FamilyRecipeTheme
import com.familyrecipe.designsystem.Templates
import com.familyrecipe.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val appViewModel: AppViewModel = viewModel(
                factory = AppViewModel.Factory
            )

            val appState by appViewModel.appState.collectAsState()
            val templateTokens = Templates.getTemplate(appState.templateKey)

            FamilyRecipeTheme(templateTokens = templateTokens) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppNavigation(
                        isOnboarded = appState.isOnboarded,
                        onCompleteOnboarding = { memberId, familyId ->
                            appViewModel.completeOnboarding(memberId, familyId)
                        }
                    )
                }
            }
        }
    }
}
