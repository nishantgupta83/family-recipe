package com.familyrecipe.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.familyrecipe.core.models.FamilyMember
import com.familyrecipe.core.models.FamilyRole
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyMemberDao {

    // MARK: - Queries

    @Query("SELECT * FROM family_members WHERE id = :id")
    fun getMemberById(id: String): Flow<FamilyMember?>

    @Query("SELECT * FROM family_members WHERE id = :id")
    suspend fun getMemberByIdOnce(id: String): FamilyMember?

    @Query("SELECT * FROM family_members WHERE familyId = :familyId ORDER BY name ASC")
    fun getMembersByFamily(familyId: String): Flow<List<FamilyMember>>

    @Query("SELECT * FROM family_members WHERE familyId = :familyId ORDER BY name ASC")
    suspend fun getMembersByFamilyOnce(familyId: String): List<FamilyMember>

    @Query("SELECT * FROM family_members WHERE familyId = :familyId AND role = :role")
    fun getMembersByRole(familyId: String, role: FamilyRole): Flow<List<FamilyMember>>

    @Query("SELECT * FROM family_members WHERE familyId = :familyId AND role = 'ADMIN' LIMIT 1")
    suspend fun getAdmin(familyId: String): FamilyMember?

    @Query("SELECT COUNT(*) FROM family_members WHERE familyId = :familyId")
    suspend fun getMemberCount(familyId: String): Int

    // MARK: - Insert/Update/Delete

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: FamilyMember)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(members: List<FamilyMember>)

    @Update
    suspend fun update(member: FamilyMember)

    @Delete
    suspend fun delete(member: FamilyMember)

    @Query("DELETE FROM family_members WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM family_members WHERE familyId = :familyId")
    suspend fun deleteAllByFamily(familyId: String)

    // MARK: - Activity

    @Query("UPDATE family_members SET lastActiveAt = :timestamp WHERE id = :memberId")
    suspend fun updateActivity(memberId: String, timestamp: Long = System.currentTimeMillis())

    // MARK: - Favorites

    @Query("UPDATE family_members SET favoriteRecipeIds = :favorites, lastActiveAt = :timestamp WHERE id = :memberId")
    suspend fun updateFavorites(memberId: String, favorites: String, timestamp: Long = System.currentTimeMillis())

    // MARK: - Created Recipes

    @Query("UPDATE family_members SET createdRecipeIds = :recipeIds, lastActiveAt = :timestamp WHERE id = :memberId")
    suspend fun updateCreatedRecipes(memberId: String, recipeIds: String, timestamp: Long = System.currentTimeMillis())

    // MARK: - Role

    @Query("UPDATE family_members SET role = :role WHERE id = :memberId")
    suspend fun updateRole(memberId: String, role: FamilyRole)
}
