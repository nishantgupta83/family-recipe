package com.familyrecipe.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.familyrecipe.core.models.Recipe
import com.familyrecipe.core.models.RecipeCategory
import com.familyrecipe.core.models.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    // MARK: - Queries

    @Query("SELECT * FROM recipes WHERE familyId = :familyId ORDER BY updatedAt DESC")
    fun getRecipesByFamily(familyId: String): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE familyId = :familyId ORDER BY updatedAt DESC")
    suspend fun getRecipesByFamilyOnce(familyId: String): List<Recipe>

    @Query("SELECT * FROM recipes WHERE id = :id")
    fun getRecipeById(id: String): Flow<Recipe?>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeByIdOnce(id: String): Recipe?

    @Query("SELECT * FROM recipes WHERE familyId = :familyId AND category = :category ORDER BY title ASC")
    fun getRecipesByCategory(familyId: String, category: RecipeCategory): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE createdById = :memberId ORDER BY createdAt DESC")
    fun getRecipesByCreator(memberId: String): Flow<List<Recipe>>

    @Query("""
        SELECT * FROM recipes
        WHERE familyId = :familyId
        AND (title LIKE '%' || :query || '%'
             OR recipeDescription LIKE '%' || :query || '%'
             OR tags LIKE '%' || :query || '%')
        ORDER BY updatedAt DESC
    """)
    fun searchRecipes(familyId: String, query: String): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE syncStatus != 'SYNCED'")
    suspend fun getUnsyncedRecipes(): List<Recipe>

    @Query("SELECT COUNT(*) FROM recipes WHERE familyId = :familyId")
    suspend fun getRecipeCount(familyId: String): Int

    @Query("SELECT * FROM recipes WHERE familyId = :familyId ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentRecipes(familyId: String, limit: Int = 10): Flow<List<Recipe>>

    // MARK: - Favorites

    @Query("""
        SELECT * FROM recipes
        WHERE familyId = :familyId
        AND favoritedBy LIKE '%' || :memberId || '%'
        ORDER BY title ASC
    """)
    fun getFavoriteRecipes(familyId: String, memberId: String): Flow<List<Recipe>>

    // MARK: - Insert/Update/Delete

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: Recipe)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<Recipe>)

    @Update
    suspend fun update(recipe: Recipe)

    @Delete
    suspend fun delete(recipe: Recipe)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM recipes WHERE familyId = :familyId")
    suspend fun deleteAllByFamily(familyId: String)

    // MARK: - Sync

    @Query("UPDATE recipes SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncStatus)

    @Query("UPDATE recipes SET syncStatus = 'SYNCED' WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)
}
