package com.familyrecipe

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.familyrecipe.core.models.CookingWorkstate
import com.familyrecipe.core.models.TemplateKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// MARK: - App State

data class AppState(
    val isOnboarded: Boolean = false,
    val currentMemberId: String? = null,
    val currentFamilyId: String? = null,
    val templateKey: TemplateKey = TemplateKey.VINTAGE,
    val cookingWorkstate: CookingWorkstate = CookingWorkstate()
)

// MARK: - App ViewModel

class AppViewModel(
    private val application: Application
) : ViewModel() {

    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _appState = MutableStateFlow(loadAppState())
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    private fun loadAppState(): AppState {
        return AppState(
            isOnboarded = prefs.getBoolean(KEY_ONBOARDED, false),
            currentMemberId = prefs.getString(KEY_MEMBER_ID, null),
            currentFamilyId = prefs.getString(KEY_FAMILY_ID, null),
            templateKey = prefs.getString(KEY_TEMPLATE, null)
                ?.let { TemplateKey.valueOf(it) }
                ?: TemplateKey.VINTAGE,
            cookingWorkstate = CookingWorkstate.load(application)
        )
    }

    fun completeOnboarding(memberId: String, familyId: String) {
        viewModelScope.launch {
            prefs.edit()
                .putBoolean(KEY_ONBOARDED, true)
                .putString(KEY_MEMBER_ID, memberId)
                .putString(KEY_FAMILY_ID, familyId)
                .apply()

            _appState.update {
                it.copy(
                    isOnboarded = true,
                    currentMemberId = memberId,
                    currentFamilyId = familyId
                )
            }
        }
    }

    fun updateTemplate(templateKey: TemplateKey) {
        viewModelScope.launch {
            prefs.edit()
                .putString(KEY_TEMPLATE, templateKey.name)
                .apply()

            _appState.update {
                it.copy(templateKey = templateKey)
            }
        }
    }

    fun updateCookingWorkstate(workstate: CookingWorkstate) {
        viewModelScope.launch {
            CookingWorkstate.save(application, workstate)
            _appState.update {
                it.copy(cookingWorkstate = workstate)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            prefs.edit()
                .remove(KEY_ONBOARDED)
                .remove(KEY_MEMBER_ID)
                .remove(KEY_FAMILY_ID)
                .apply()

            CookingWorkstate.clear(application)

            _appState.update {
                AppState()
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "app_state"
        private const val KEY_ONBOARDED = "is_onboarded"
        private const val KEY_MEMBER_ID = "current_member_id"
        private const val KEY_FAMILY_ID = "current_family_id"
        private const val KEY_TEMPLATE = "template_key"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                AppViewModel(application)
            }
        }
    }
}
