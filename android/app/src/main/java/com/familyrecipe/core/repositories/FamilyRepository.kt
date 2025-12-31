package com.familyrecipe.core.repositories

import com.familyrecipe.core.database.FamilyDao
import com.familyrecipe.core.models.Family
import com.familyrecipe.core.models.LanguageCode
import com.familyrecipe.core.models.TemplateKey
import com.familyrecipe.core.models.UnitSystem
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import java.util.Date

// MARK: - Family Repository

class FamilyRepository(
    private val familyDao: FamilyDao
) {
    private val gson = Gson()

    // MARK: - Read Operations

    fun getFamily(id: String): Flow<Family?> =
        familyDao.getFamilyById(id)

    suspend fun getFamilyOnce(id: String): Family? =
        familyDao.getFamilyByIdOnce(id)

    suspend fun getFamilyByInviteCode(code: String): Family? =
        familyDao.getFamilyByInviteCode(code)

    fun getAllFamilies(): Flow<List<Family>> =
        familyDao.getAllFamilies()

    fun getFamiliesForMember(memberId: String): Flow<List<Family>> =
        familyDao.getFamiliesForMember(memberId)

    suspend fun getFamilyCount(): Int =
        familyDao.getFamilyCount()

    // MARK: - Write Operations

    suspend fun createFamily(family: Family) {
        familyDao.insert(family)
    }

    suspend fun updateFamily(family: Family) {
        familyDao.update(family.withUpdatedActivity())
    }

    suspend fun deleteFamily(family: Family) {
        familyDao.delete(family)
    }

    suspend fun deleteFamilyById(id: String) {
        familyDao.deleteById(id)
    }

    // MARK: - Invite Code

    suspend fun regenerateInviteCode(familyId: String): String? {
        val family = getFamilyOnce(familyId) ?: return null
        val newFamily = family.withNewInviteCode()
        familyDao.regenerateInviteCode(familyId, newFamily.inviteCode)
        return newFamily.inviteCode
    }

    // MARK: - Member Management

    suspend fun addMember(familyId: String, memberId: String): Boolean {
        val family = getFamilyOnce(familyId) ?: return false
        val updatedFamily = family.withNewMember(memberId) ?: return false
        familyDao.updateMembers(familyId, gson.toJson(updatedFamily.memberIds))
        return true
    }

    suspend fun removeMember(familyId: String, memberId: String): Boolean {
        val family = getFamilyOnce(familyId) ?: return false
        val updatedFamily = family.withoutMember(memberId) ?: return false
        familyDao.updateMembers(familyId, gson.toJson(updatedFamily.memberIds))
        return true
    }

    suspend fun transferAdmin(familyId: String, newAdminId: String): Boolean {
        val family = getFamilyOnce(familyId) ?: return false
        if (!family.memberIds.contains(newAdminId)) return false
        familyDao.transferAdmin(familyId, newAdminId)
        return true
    }

    fun isAdmin(family: Family, memberId: String): Boolean =
        family.isAdmin(memberId)

    // MARK: - Settings

    suspend fun updateTheme(familyId: String, theme: TemplateKey) {
        familyDao.updateTheme(familyId, theme.name)
    }

    suspend fun updateLanguage(familyId: String, language: LanguageCode) {
        familyDao.updateLanguage(familyId, language.name)
    }

    suspend fun updateUnits(familyId: String, units: UnitSystem) {
        familyDao.updateUnits(familyId, units.name)
    }
}

// MARK: - Errors

sealed class FamilyRepositoryError : Exception() {
    object FamilyNotFound : FamilyRepositoryError()
    object InvalidInviteCode : FamilyRepositoryError()
    object MemberAlreadyExists : FamilyRepositoryError()
    object CannotRemoveAdmin : FamilyRepositoryError()
}
