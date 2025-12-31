package com.familyrecipe.core.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Calendar
import java.util.Date
import java.util.UUID

// MARK: - Family Member Model

@Entity(tableName = "family_members")
@TypeConverters(FamilyMemberConverters::class)
data class FamilyMember(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val avatarEmoji: String = "\uD83D\uDC64", // 👤
    val role: FamilyRole = FamilyRole.MEMBER,
    val familyId: String,
    val createdRecipeIds: List<String> = emptyList(),
    val favoriteRecipeIds: List<String> = emptyList(),
    val preferredLanguage: LanguageCode? = null,
    val joinedAt: Date = Date(),
    val lastActiveAt: Date = Date()
) {
    // Computed properties
    val isActive: Boolean
        get() {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -30)
            return lastActiveAt.after(calendar.time)
        }

    val recipesCreatedCount: Int
        get() = createdRecipeIds.size

    val favoriteCount: Int
        get() = favoriteRecipeIds.size

    // MARK: - Methods

    fun withUpdatedActivity(): FamilyMember = copy(lastActiveAt = Date())

    fun withCreatedRecipe(recipeId: String): FamilyMember {
        if (createdRecipeIds.contains(recipeId)) return this
        return copy(
            createdRecipeIds = createdRecipeIds + recipeId,
            lastActiveAt = Date()
        )
    }

    fun withAddedFavorite(recipeId: String): FamilyMember {
        if (favoriteRecipeIds.contains(recipeId)) return this
        return copy(
            favoriteRecipeIds = favoriteRecipeIds + recipeId,
            lastActiveAt = Date()
        )
    }

    fun withRemovedFavorite(recipeId: String): FamilyMember = copy(
        favoriteRecipeIds = favoriteRecipeIds.filter { it != recipeId },
        lastActiveAt = Date()
    )

    fun withToggledFavorite(recipeId: String): FamilyMember {
        return if (favoriteRecipeIds.contains(recipeId)) {
            withRemovedFavorite(recipeId)
        } else {
            withAddedFavorite(recipeId)
        }
    }

    fun isFavorite(recipeId: String): Boolean = favoriteRecipeIds.contains(recipeId)

    // MARK: - Permissions

    val canCreateRecipes: Boolean
        get() = role == FamilyRole.ADMIN || role == FamilyRole.MEMBER

    val canEditRecipes: Boolean
        get() = role == FamilyRole.ADMIN || role == FamilyRole.MEMBER

    val canManageMembers: Boolean
        get() = role == FamilyRole.ADMIN

    val canDeleteFamily: Boolean
        get() = role == FamilyRole.ADMIN

    companion object {
        val availableEmojis: List<String> = listOf(
            // People
            "\uD83D\uDC68\u200D\uD83C\uDF73", // 👨‍🍳
            "\uD83D\uDC69\u200D\uD83C\uDF73", // 👩‍🍳
            "\uD83D\uDC68", // 👨
            "\uD83D\uDC69", // 👩
            "\uD83E\uDDD1", // 🧑
            "\uD83D\uDC74", // 👴
            "\uD83D\uDC75", // 👵
            "\uD83E\uDDD3", // 🧓
            "\uD83D\uDC66", // 👦
            "\uD83D\uDC67", // 👧
            "\uD83E\uDDD2", // 🧒
            "\uD83D\uDC76", // 👶

            // Food related
            "\uD83C\uDF73", // 🍳
            "\uD83E\uDD58", // 🥘
            "\uD83C\uDF72", // 🍲
            "\uD83E\uDD57", // 🥗
            "\uD83C\uDF70", // 🍰
            "\uD83E\uDDC1", // 🧁
            "\uD83C\uDF6A", // 🍪
            "\uD83E\uDD50", // 🥐
            "\uD83C\uDF55", // 🍕
            "\uD83C\uDF2E", // 🌮

            // Fun
            "\uD83E\uDD8A", // 🦊
            "\uD83D\uDC31", // 🐱
            "\uD83D\uDC36", // 🐶
            "\uD83D\uDC3C", // 🐼
            "\uD83E\uDD81", // 🦁
            "\uD83C\uDF38", // 🌸
            "\uD83C\uDF3B", // 🌻
            "\u2B50",       // ⭐
            "\uD83C\uDF08", // 🌈
            "\uD83C\uDF82"  // 🎂
        )

        val randomEmoji: String
            get() = availableEmojis.random()
    }
}

// MARK: - Family Role

enum class FamilyRole(val displayName: String, val description: String) {
    ADMIN(
        displayName = "Admin",
        description = "Can manage family members and settings"
    ),
    MEMBER(
        displayName = "Member",
        description = "Can create and edit recipes"
    )
}

// MARK: - Room Type Converters

class FamilyMemberConverters {
    private val gson = com.google.gson.Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromFamilyRole(value: FamilyRole): String = value.name

    @TypeConverter
    fun toFamilyRole(value: String): FamilyRole = FamilyRole.valueOf(value)

    @TypeConverter
    fun fromLanguageCode(value: LanguageCode?): String? = value?.name

    @TypeConverter
    fun toLanguageCode(value: String?): LanguageCode? = value?.let { LanguageCode.valueOf(it) }

    @TypeConverter
    fun fromDate(value: Date): Long = value.time

    @TypeConverter
    fun toDate(value: Long): Date = Date(value)
}

// MARK: - Sample Members

object FamilyMemberSamples {
    fun sampleMembers(familyId: String, adminId: String): List<FamilyMember> = listOf(
        FamilyMember(
            id = adminId,
            name = "Mom",
            avatarEmoji = "\uD83D\uDC69\u200D\uD83C\uDF73",
            role = FamilyRole.ADMIN,
            familyId = familyId
        ),
        FamilyMember(
            name = "Dad",
            avatarEmoji = "\uD83D\uDC68\u200D\uD83C\uDF73",
            role = FamilyRole.MEMBER,
            familyId = familyId
        ),
        FamilyMember(
            name = "Grandma",
            avatarEmoji = "\uD83D\uDC75",
            role = FamilyRole.MEMBER,
            familyId = familyId
        )
    )
}
