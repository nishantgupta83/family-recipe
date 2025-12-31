package com.familyrecipe.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.familyrecipe.core.models.Family
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyDao {

    // MARK: - Queries

    @Query("SELECT * FROM families WHERE id = :id")
    fun getFamilyById(id: String): Flow<Family?>

    @Query("SELECT * FROM families WHERE id = :id")
    suspend fun getFamilyByIdOnce(id: String): Family?

    @Query("SELECT * FROM families WHERE inviteCode = :code")
    suspend fun getFamilyByInviteCode(code: String): Family?

    @Query("SELECT * FROM families ORDER BY lastActivityAt DESC")
    fun getAllFamilies(): Flow<List<Family>>

    @Query("SELECT * FROM families WHERE :memberId IN (memberIds) ORDER BY lastActivityAt DESC")
    fun getFamiliesForMember(memberId: String): Flow<List<Family>>

    @Query("SELECT COUNT(*) FROM families")
    suspend fun getFamilyCount(): Int

    // MARK: - Insert/Update/Delete

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(family: Family)

    @Update
    suspend fun update(family: Family)

    @Delete
    suspend fun delete(family: Family)

    @Query("DELETE FROM families WHERE id = :id")
    suspend fun deleteById(id: String)

    // MARK: - Member Management

    @Query("UPDATE families SET memberIds = :memberIds, lastActivityAt = :timestamp WHERE id = :familyId")
    suspend fun updateMembers(familyId: String, memberIds: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE families SET adminMemberId = :newAdminId, lastActivityAt = :timestamp WHERE id = :familyId")
    suspend fun transferAdmin(familyId: String, newAdminId: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE families SET inviteCode = :newCode, lastActivityAt = :timestamp WHERE id = :familyId")
    suspend fun regenerateInviteCode(familyId: String, newCode: String, timestamp: Long = System.currentTimeMillis())

    // MARK: - Settings

    @Query("UPDATE families SET theme = :theme, lastActivityAt = :timestamp WHERE id = :familyId")
    suspend fun updateTheme(familyId: String, theme: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE families SET language = :language, lastActivityAt = :timestamp WHERE id = :familyId")
    suspend fun updateLanguage(familyId: String, language: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE families SET units = :units, lastActivityAt = :timestamp WHERE id = :familyId")
    suspend fun updateUnits(familyId: String, units: String, timestamp: Long = System.currentTimeMillis())
}
