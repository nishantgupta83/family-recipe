package com.familyrecipe.core.repositories

import com.familyrecipe.core.database.RecipeDao
import com.familyrecipe.core.models.Recipe
import com.familyrecipe.core.models.RecipeCategory
import com.familyrecipe.core.models.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Date

// MARK: - Recipe Repository

class RecipeRepository(
    private val recipeDao: RecipeDao
) {
    // MARK: - Read Operations

    fun getRecipes(familyId: String): Flow<List<Recipe>> =
        recipeDao.getRecipesByFamily(familyId)

    suspend fun getRecipesOnce(familyId: String): List<Recipe> =
        recipeDao.getRecipesByFamilyOnce(familyId)

    fun getRecipe(id: String): Flow<Recipe?> =
        recipeDao.getRecipeById(id)

    suspend fun getRecipeOnce(id: String): Recipe? =
        recipeDao.getRecipeByIdOnce(id)

    fun getRecipesByCategory(familyId: String, category: RecipeCategory): Flow<List<Recipe>> =
        recipeDao.getRecipesByCategory(familyId, category)

    fun getRecentRecipes(familyId: String, limit: Int = 10): Flow<List<Recipe>> =
        recipeDao.getRecentRecipes(familyId, limit)

    fun getFavoriteRecipes(familyId: String, memberId: String): Flow<List<Recipe>> =
        recipeDao.getFavoriteRecipes(familyId, memberId)

    fun searchRecipes(familyId: String, query: String): Flow<List<Recipe>> =
        recipeDao.searchRecipes(familyId, query)

    fun getRecipesByCreator(memberId: String): Flow<List<Recipe>> =
        recipeDao.getRecipesByCreator(memberId)

    suspend fun getRecipeCount(familyId: String): Int =
        recipeDao.getRecipeCount(familyId)

    // MARK: - Write Operations

    suspend fun createRecipe(recipe: Recipe) {
        recipeDao.insert(recipe)
    }

    suspend fun updateRecipe(recipe: Recipe) {
        recipeDao.update(recipe.copy(updatedAt = Date()))
    }

    suspend fun deleteRecipe(recipe: Recipe) {
        recipeDao.delete(recipe)
    }

    suspend fun deleteRecipeById(id: String) {
        recipeDao.deleteById(id)
    }

    // MARK: - Favorites

    suspend fun toggleFavorite(recipeId: String, memberId: String) {
        val recipe = recipeDao.getRecipeByIdOnce(recipeId) ?: return
        val updatedFavorites = if (recipe.favoritedBy.contains(memberId)) {
            recipe.favoritedBy.filter { it != memberId }
        } else {
            recipe.favoritedBy + memberId
        }
        recipeDao.update(recipe.copy(
            favoritedBy = updatedFavorites,
            updatedAt = Date()
        ))
    }

    suspend fun isFavorite(recipeId: String, memberId: String): Boolean {
        val recipe = recipeDao.getRecipeByIdOnce(recipeId) ?: return false
        return recipe.favoritedBy.contains(memberId)
    }

    // MARK: - Sync

    suspend fun getUnsyncedRecipes(): List<Recipe> =
        recipeDao.getUnsyncedRecipes()

    suspend fun updateSyncStatus(id: String, status: SyncStatus) {
        recipeDao.updateSyncStatus(id, status)
    }

    suspend fun markAsSynced(ids: List<String>) {
        recipeDao.markAsSynced(ids)
    }

    // MARK: - Bulk Operations

    suspend fun insertAll(recipes: List<Recipe>) {
        recipeDao.insertAll(recipes)
    }

    suspend fun deleteAllByFamily(familyId: String) {
        recipeDao.deleteAllByFamily(familyId)
    }

    // MARK: - Statistics

    suspend fun getRecipeCountByCategory(familyId: String): Map<RecipeCategory, Int> {
        val recipes = getRecipesOnce(familyId)
        return RecipeCategory.values().associateWith { category ->
            recipes.count { it.category == category }
        }
    }
}
