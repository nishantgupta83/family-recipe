package com.familyrecipe.features.cookingmode

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.familyrecipe.core.models.CookingTimer
import com.familyrecipe.core.models.CookingWorkstate
import com.familyrecipe.core.models.Instruction
import com.familyrecipe.core.models.Recipe
import com.familyrecipe.core.services.assistant.AssistantIntent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// MARK: - UI State

data class CookingModeUiState(
    val workstate: CookingWorkstate = CookingWorkstate(),
    val currentStep: Instruction? = null,
    val isPlaying: Boolean = false
) {
    val currentStepIndex: Int get() = workstate.stepIndex

    val progress: Float
        get() = if (workstate.hasActiveSession) {
            (currentStepIndex + 1).toFloat() / workstate.stepIndex.coerceAtLeast(1)
        } else 0f

    val canGoPrevious: Boolean get() = currentStepIndex > 0

    val isLastStep: Boolean
        get() = currentStep != null && currentStepIndex >= 0

    val isCurrentStepCompleted: Boolean
        get() = workstate.isStepCompleted(currentStepIndex)

    val timers: List<CookingTimer> get() = workstate.timers
}

// MARK: - ViewModel

class CookingModeViewModel(
    private val recipe: Recipe,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(CookingModeUiState())
    val uiState: StateFlow<CookingModeUiState> = _uiState.asStateFlow()

    init {
        loadWorkstate()
        startTimerUpdates()
    }

    private fun loadWorkstate() {
        val workstate = CookingWorkstate.load(context)
        updateState(workstate)
    }

    private fun updateState(workstate: CookingWorkstate) {
        val currentStep = if (workstate.stepIndex < recipe.instructions.size) {
            recipe.instructions[workstate.stepIndex]
        } else null

        _uiState.update {
            it.copy(
                workstate = workstate,
                currentStep = currentStep
            )
        }
    }

    private fun saveWorkstate() {
        CookingWorkstate.save(context, _uiState.value.workstate)
    }

    // MARK: - Session Management

    fun startSession() {
        if (_uiState.value.workstate.activeRecipeId != recipe.id) {
            val newWorkstate = _uiState.value.workstate.startSession(recipe.id)
            updateState(newWorkstate)
            saveWorkstate()
        }
    }

    fun endSession() {
        val newWorkstate = _uiState.value.workstate.endSession()
        updateState(newWorkstate)
        CookingWorkstate.clear(context)
    }

    // MARK: - Navigation

    fun nextStep() {
        val (newWorkstate, success) = _uiState.value.workstate.goToNextStep(recipe.instructions.size)
        if (success) {
            updateState(newWorkstate)
            saveWorkstate()
        }
    }

    fun previousStep() {
        val (newWorkstate, success) = _uiState.value.workstate.goToPreviousStep()
        if (success) {
            updateState(newWorkstate)
            saveWorkstate()
        }
    }

    fun goToStep(step: Int) {
        val (newWorkstate, success) = _uiState.value.workstate.goToStep(step, recipe.instructions.size)
        if (success) {
            updateState(newWorkstate)
            saveWorkstate()
        }
    }

    // MARK: - Timer Management

    fun addTimer(durationMs: Long, label: String? = null) {
        val (newWorkstate, timer) = _uiState.value.workstate.addTimer(
            duration = durationMs,
            label = label
        )
        val startedTimer = timer.start()
        val workstateWithStartedTimer = newWorkstate.copy(
            timers = newWorkstate.timers.map {
                if (it.id == timer.id) startedTimer else it
            }
        )
        updateState(workstateWithStartedTimer)
        saveWorkstate()
    }

    fun removeTimer(id: String) {
        val newWorkstate = _uiState.value.workstate.removeTimer(id)
        updateState(newWorkstate)
        saveWorkstate()
    }

    private fun startTimerUpdates() {
        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                updateTimers()
            }
        }
    }

    private fun updateTimers() {
        val currentTimers = _uiState.value.workstate.timers
        if (currentTimers.isEmpty()) return

        val updatedTimers = currentTimers.map { it.updateRemainingTime() }
        val newWorkstate = _uiState.value.workstate.copy(timers = updatedTimers)
        _uiState.update { it.copy(workstate = newWorkstate) }
    }

    // MARK: - Assistant Integration

    fun handleAssistantIntent(intent: AssistantIntent) {
        when (intent) {
            is AssistantIntent.NextStep -> nextStep()
            is AssistantIntent.PreviousStep -> previousStep()
            is AssistantIntent.GoToStep -> goToStep(intent.step - 1)
            is AssistantIntent.SetTimer -> addTimer(intent.durationMs)
            is AssistantIntent.CancelTimer -> {
                _uiState.value.workstate.timers.firstOrNull()?.let {
                    removeTimer(it.id)
                }
            }
            else -> { /* Other intents don't affect state */ }
        }
    }

    // MARK: - Factory

    class Factory(
        private val recipe: Recipe,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CookingModeViewModel(recipe, context) as T
        }
    }
}
