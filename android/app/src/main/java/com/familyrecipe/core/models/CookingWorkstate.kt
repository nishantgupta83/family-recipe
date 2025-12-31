package com.familyrecipe.core.models

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import java.util.Date
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

// MARK: - Cooking Workstate

/**
 * Tracks the current cooking session state for context-aware assistant
 */
data class CookingWorkstate(
    val activeRecipeId: String? = null,
    val stepIndex: Int = 0,
    val completedSteps: Set<Int> = emptySet(),
    val timers: List<CookingTimer> = emptyList(),
    val scaleFactor: Double = 1.0,
    val isPaused: Boolean = false,
    val preferences: CookingPreferences = CookingPreferences(),
    val lastAction: Date = Date(),
    val sessionStartedAt: Date? = null
) {
    // MARK: - Computed Properties

    val hasActiveSession: Boolean
        get() = activeRecipeId != null

    val activeTimersCount: Int
        get() = timers.count { it.isRunning }

    val completedStepsCount: Int
        get() = completedSteps.size

    // MARK: - Session Management

    fun startSession(recipeId: String): CookingWorkstate = CookingWorkstate(
        activeRecipeId = recipeId,
        stepIndex = 0,
        completedSteps = emptySet(),
        timers = emptyList(),
        scaleFactor = 1.0,
        isPaused = false,
        preferences = preferences,
        sessionStartedAt = Date(),
        lastAction = Date()
    )

    fun endSession(): CookingWorkstate = CookingWorkstate(
        activeRecipeId = null,
        stepIndex = 0,
        completedSteps = emptySet(),
        timers = emptyList(),
        scaleFactor = 1.0,
        isPaused = false,
        preferences = preferences,
        sessionStartedAt = null,
        lastAction = Date()
    )

    fun pause(): CookingWorkstate = copy(
        isPaused = true,
        lastAction = Date()
    )

    fun resume(): CookingWorkstate = copy(
        isPaused = false,
        lastAction = Date()
    )

    // MARK: - Step Navigation

    fun goToNextStep(totalSteps: Int): Pair<CookingWorkstate, Boolean> {
        if (stepIndex >= totalSteps - 1) return Pair(this, false)
        return Pair(
            copy(
                stepIndex = stepIndex + 1,
                completedSteps = completedSteps + stepIndex,
                lastAction = Date()
            ),
            true
        )
    }

    fun goToPreviousStep(): Pair<CookingWorkstate, Boolean> {
        if (stepIndex <= 0) return Pair(this, false)
        return Pair(
            copy(
                stepIndex = stepIndex - 1,
                lastAction = Date()
            ),
            true
        )
    }

    fun goToStep(step: Int, totalSteps: Int): Pair<CookingWorkstate, Boolean> {
        if (step < 0 || step >= totalSteps) return Pair(this, false)
        return Pair(
            copy(
                stepIndex = step,
                lastAction = Date()
            ),
            true
        )
    }

    fun completeCurrentStep(): CookingWorkstate = copy(
        completedSteps = completedSteps + stepIndex,
        lastAction = Date()
    )

    fun isStepCompleted(step: Int): Boolean = completedSteps.contains(step)

    // MARK: - Timer Management

    fun addTimer(duration: Long, label: String? = null): Pair<CookingWorkstate, CookingTimer> {
        val timer = CookingTimer(
            duration = duration,
            label = label ?: "Step ${stepIndex + 1}",
            associatedStep = stepIndex
        )
        return Pair(
            copy(
                timers = timers + timer,
                lastAction = Date()
            ),
            timer
        )
    }

    fun removeTimer(id: String): CookingWorkstate = copy(
        timers = timers.filter { it.id != id },
        lastAction = Date()
    )

    fun removeAllTimers(): CookingWorkstate = copy(
        timers = emptyList(),
        lastAction = Date()
    )

    // MARK: - Scaling

    fun setScaleFactor(factor: Double): CookingWorkstate = copy(
        scaleFactor = max(0.25, min(4.0, factor)),
        lastAction = Date()
    )

    fun scaleAmount(amount: Double): Double = amount * scaleFactor

    // MARK: - Persistence

    companion object {
        private const val PREFS_NAME = "cooking_workstate"
        private const val KEY_WORKSTATE = "workstate"

        fun save(context: Context, workstate: CookingWorkstate) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val json = Gson().toJson(workstate)
            prefs.edit().putString(KEY_WORKSTATE, json).apply()
        }

        fun load(context: Context): CookingWorkstate {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val json = prefs.getString(KEY_WORKSTATE, null) ?: return CookingWorkstate()
            return try {
                Gson().fromJson(json, CookingWorkstate::class.java)
            } catch (e: Exception) {
                CookingWorkstate()
            }
        }

        fun clear(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().remove(KEY_WORKSTATE).apply()
        }
    }
}

// MARK: - Cooking Timer

data class CookingTimer(
    val id: String = UUID.randomUUID().toString(),
    val duration: Long, // milliseconds
    val remainingTime: Long = duration,
    val label: String = "Timer",
    val associatedStep: Int? = null,
    val startedAt: Long? = null,
    val pausedAt: Long? = null
) {
    val isRunning: Boolean
        get() = startedAt != null && pausedAt == null && remainingTime > 0

    val isCompleted: Boolean
        get() = remainingTime <= 0

    val isPaused: Boolean
        get() = pausedAt != null

    val formattedRemaining: String
        get() {
            val totalSeconds = remainingTime / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%d:%02d", minutes, seconds)
        }

    fun start(): CookingTimer = if (startedAt == null) {
        copy(startedAt = System.currentTimeMillis())
    } else if (pausedAt != null) {
        // Resuming from pause
        copy(pausedAt = null)
    } else {
        this
    }

    fun pause(): CookingTimer = if (isRunning) {
        copy(pausedAt = System.currentTimeMillis())
    } else {
        this
    }

    fun stop(): CookingTimer = copy(
        startedAt = null,
        pausedAt = null,
        remainingTime = duration
    )

    fun updateRemainingTime(): CookingTimer {
        val started = startedAt ?: return this
        if (pausedAt != null) return this
        val elapsed = System.currentTimeMillis() - started
        return copy(remainingTime = max(0, duration - elapsed))
    }
}

// MARK: - Cooking Preferences

data class CookingPreferences(
    val keepScreenOn: Boolean = true,
    val voiceEnabled: Boolean = false,
    val voiceSpeed: Float = 1.0f,
    val autoAdvance: Boolean = false,
    val hapticFeedback: Boolean = true,
    val timerSoundEnabled: Boolean = true
)

// MARK: - Session Summary

data class CookingSessionSummary(
    val recipeId: String,
    val recipeTitle: String,
    val startedAt: Date,
    val completedAt: Date,
    val totalSteps: Int,
    val completedSteps: Int,
    val scaleFactor: Double
) {
    val duration: Long
        get() = completedAt.time - startedAt.time

    val completionPercentage: Double
        get() = if (totalSteps > 0) {
            completedSteps.toDouble() / totalSteps * 100
        } else 0.0

    val formattedDuration: String
        get() {
            val minutes = (duration / 1000 / 60).toInt()
            return if (minutes < 60) {
                "$minutes min"
            } else {
                val hours = minutes / 60
                val remainingMinutes = minutes % 60
                "${hours}h ${remainingMinutes}m"
            }
        }
}
