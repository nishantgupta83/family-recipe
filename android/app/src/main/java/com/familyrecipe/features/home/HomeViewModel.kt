package com.familyrecipe.features.home

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.familyrecipe.FamilyRecipeApp
import com.familyrecipe.core.models.Recipe
import com.familyrecipe.core.models.RecipeCategory
import com.familyrecipe.core.repositories.FamilyRepository
import com.familyrecipe.core.repositories.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// MARK: - UI State

data class HomeUiState(
    val isLoading: Boolean = false,
    val familyName: String? = null,
    val familyEmoji: String = "\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66",
    val recipes: List<Recipe> = emptyList(),
    val selectedCategory: RecipeCategory? = null,
    val searchQuery: String = "",
    val error: String? = null
) {
    val filteredRecipes: List<Recipe>
        get() {
            var result = recipes

            if (selectedCategory != null) {
                result = result.filter { it.category == selectedCategory }
            }

            if (searchQuery.isNotBlank()) {
                val query = searchQuery.lowercase()
                result = result.filter {
                    it.title.lowercase().contains(query) ||
                    it.recipeDescription.lowercase().contains(query) ||
                    it.tags.any { tag -> tag.lowercase().contains(query) }
                }
            }

            return result
        }

    val recentRecipes: List<Recipe>
        get() = recipes.take(5)
}

// MARK: - Home View Model

class HomeViewModel(
    private val recipeRepository: RecipeRepository,
    private val familyRepository: FamilyRepository,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val prefs = application.getSharedPreferences("app_state", Context.MODE_PRIVATE)
    private val currentFamilyId: String? = prefs.getString("current_family_id", null)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Load family info
                currentFamilyId?.let { familyId ->
                    familyRepository.getFamilyOnce(familyId)?.let { family ->
                        _uiState.update {
                            it.copy(
                                familyName = family.name,
                                familyEmoji = family.iconEmoji
                            )
                        }
                    }
                }

                // Load recipes
                currentFamilyId?.let { familyId ->
                    recipeRepository.getRecipes(familyId).collect { recipes ->
                        _uiState.update {
                            it.copy(
                                recipes = recipes,
                                isLoading = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectCategory(category: RecipeCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun refresh() {
        loadData()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                val app = application as FamilyRecipeApp
                HomeViewModel(
                    recipeRepository = app.recipeRepository,
                    familyRepository = app.familyRepository,
                    application = application
                )
            }
        }
    }
}
