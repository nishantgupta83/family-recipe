package com.familyrecipe.core.repositories

import com.familyrecipe.core.database.FamilyMemberDao
import com.familyrecipe.core.models.FamilyMember
import com.familyrecipe.core.models.FamilyRole
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import java.util.Date

// MARK: - Family Member Repository

class FamilyMemberRepository(
    private val familyMemberDao: FamilyMemberDao
) {
    private val gson = Gson()

    // MARK: - Read Operations

    fun getMember(id: String): Flow<FamilyMember?> =
        familyMemberDao.getMemberById(id)

    suspend fun getMemberOnce(id: String): FamilyMember? =
        familyMemberDao.getMemberByIdOnce(id)

    fun getMembers(familyId: String): Flow<List<FamilyMember>> =
        familyMemberDao.getMembersByFamily(familyId)

    suspend fun getMembersOnce(familyId: String): List<FamilyMember> =
        familyMemberDao.getMembersByFamilyOnce(familyId)

    fun getMembersByRole(familyId: String, role: FamilyRole): Flow<List<FamilyMember>> =
        familyMemberDao.getMembersByRole(familyId, role)

    suspend fun getAdmin(familyId: String): FamilyMember? =
        familyMemberDao.getAdmin(familyId)

    suspend fun getMemberCount(familyId: String): Int =
        familyMemberDao.getMemberCount(familyId)

    // MARK: - Write Operations

    suspend fun createMember(member: FamilyMember) {
        familyMemberDao.insert(member)
    }

    suspend fun updateMember(member: FamilyMember) {
        familyMemberDao.update(member.withUpdatedActivity())
    }

    suspend fun deleteMember(member: FamilyMember) {
        familyMemberDao.delete(member)
    }

    suspend fun deleteMemberById(id: String) {
        familyMemberDao.deleteById(id)
    }

    // MARK: - Activity

    suspend fun updateActivity(memberId: String) {
        familyMemberDao.updateActivity(memberId)
    }

    // MARK: - Favorites

    suspend fun toggleFavorite(memberId: String, recipeId: String) {
        val member = getMemberOnce(memberId) ?: return
        val updatedMember = member.withToggledFavorite(recipeId)
        familyMemberDao.updateFavorites(memberId, gson.toJson(updatedMember.favoriteRecipeIds))
    }

    suspend fun addFavorite(memberId: String, recipeId: String) {
        val member = getMemberOnce(memberId) ?: return
        if (!member.isFavorite(recipeId)) {
            val updatedMember = member.withAddedFavorite(recipeId)
            familyMemberDao.updateFavorites(memberId, gson.toJson(updatedMember.favoriteRecipeIds))
        }
    }

    suspend fun removeFavorite(memberId: String, recipeId: String) {
        val member = getMemberOnce(memberId) ?: return
        if (member.isFavorite(recipeId)) {
            val updatedMember = member.withRemovedFavorite(recipeId)
            familyMemberDao.updateFavorites(memberId, gson.toJson(updatedMember.favoriteRecipeIds))
        }
    }

    fun isFavorite(member: FamilyMember, recipeId: String): Boolean =
        member.isFavorite(recipeId)

    // MARK: - Created Recipes

    suspend fun addCreatedRecipe(memberId: String, recipeId: String) {
        val member = getMemberOnce(memberId) ?: return
        val updatedMember = member.withCreatedRecipe(recipeId)
        familyMemberDao.updateCreatedRecipes(memberId, gson.toJson(updatedMember.createdRecipeIds))
    }

    // MARK: - Role

    suspend fun updateRole(memberId: String, role: FamilyRole) {
        familyMemberDao.updateRole(memberId, role)
    }

    // MARK: - Bulk Operations

    suspend fun insertAll(members: List<FamilyMember>) {
        familyMemberDao.insertAll(members)
    }

    suspend fun deleteAllByFamily(familyId: String) {
        familyMemberDao.deleteAllByFamily(familyId)
    }

    // MARK: - Helpers

    suspend fun getActiveMembers(familyId: String): List<FamilyMember> {
        return getMembersOnce(familyId).filter { it.isActive }
    }
}

// MARK: - Errors

sealed class FamilyMemberRepositoryError : Exception() {
    object MemberNotFound : FamilyMemberRepositoryError()
    object CannotDeleteAdmin : FamilyMemberRepositoryError()
}
