package com.familyrecipe.core.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.Date
import java.util.UUID

// MARK: - Family Model

@Entity(tableName = "families")
@TypeConverters(FamilyConverters::class)
data class Family(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val familyDescription: String = "",
    val inviteCode: String = generateInviteCode(),
    val adminMemberId: String,
    val memberIds: List<String> = listOf(),
    val theme: TemplateKey = TemplateKey.VINTAGE,
    val language: LanguageCode = LanguageCode.EN,
    val units: UnitSystem = UnitSystem.IMPERIAL,
    val iconEmoji: String = "\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66", // Family emoji
    val createdAt: Date = Date(),
    val lastActivityAt: Date = Date()
) {
    val memberCount: Int
        get() = memberIds.size

    val canAddMembers: Boolean
        get() = memberIds.size < 20

    fun withNewInviteCode(): Family = copy(
        inviteCode = generateInviteCode(),
        lastActivityAt = Date()
    )

    fun withNewMember(memberId: String): Family? {
        if (!canAddMembers || memberIds.contains(memberId)) return null
        return copy(
            memberIds = memberIds + memberId,
            lastActivityAt = Date()
        )
    }

    fun withoutMember(memberId: String): Family? {
        if (memberId == adminMemberId) return null // Can't remove admin
        return copy(
            memberIds = memberIds.filter { it != memberId },
            lastActivityAt = Date()
        )
    }

    fun isAdmin(memberId: String): Boolean = memberId == adminMemberId

    fun withTransferredAdmin(newAdminId: String): Family? {
        if (!memberIds.contains(newAdminId)) return null
        return copy(
            adminMemberId = newAdminId,
            lastActivityAt = Date()
        )
    }

    fun withUpdatedActivity(): Family = copy(lastActivityAt = Date())

    companion object {
        private const val INVITE_CODE_CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

        fun generateInviteCode(): String {
            return (1..6)
                .map { INVITE_CODE_CHARACTERS.random() }
                .joinToString("")
        }
    }
}

// MARK: - Enums

enum class TemplateKey(val displayName: String, val description: String) {
    VINTAGE(
        displayName = "Vintage Cookbook",
        description = "Warm, nostalgic, like grandma's recipe cards"
    ),
    MODERN(
        displayName = "Modern Kitchen",
        description = "Clean, minimal, contemporary design"
    ),
    PLAYFUL(
        displayName = "Playful Family",
        description = "Fun, colorful, perfect for cooking with kids"
    )
}

enum class LanguageCode(val displayName: String) {
    EN("English"),
    ES("Español"),
    HI("हिन्दी")
}

enum class UnitSystem(val displayName: String) {
    IMPERIAL("Imperial (cups, °F)"),
    METRIC("Metric (ml, °C)")
}

// MARK: - Room Type Converters

class FamilyConverters {
    private val gson = com.google.gson.Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromTemplateKey(value: TemplateKey): String = value.name

    @TypeConverter
    fun toTemplateKey(value: String): TemplateKey = TemplateKey.valueOf(value)

    @TypeConverter
    fun fromLanguageCode(value: LanguageCode): String = value.name

    @TypeConverter
    fun toLanguageCode(value: String): LanguageCode = LanguageCode.valueOf(value)

    @TypeConverter
    fun fromUnitSystem(value: UnitSystem): String = value.name

    @TypeConverter
    fun toUnitSystem(value: String): UnitSystem = UnitSystem.valueOf(value)

    @TypeConverter
    fun fromDate(value: Date): Long = value.time

    @TypeConverter
    fun toDate(value: Long): Date = Date(value)
}

// MARK: - Sample Data

object FamilySamples {
    fun sampleFamily(): Pair<Family, FamilyMember> {
        val memberId = UUID.randomUUID().toString()
        val familyId = UUID.randomUUID().toString()

        val family = Family(
            id = familyId,
            name = "The Recipe Testers",
            familyDescription = "A family that loves to cook together!",
            adminMemberId = memberId,
            memberIds = listOf(memberId),
            theme = TemplateKey.VINTAGE,
            language = LanguageCode.EN,
            units = UnitSystem.IMPERIAL,
            iconEmoji = "\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66"
        )

        val member = FamilyMember(
            id = memberId,
            name = "Chef",
            avatarEmoji = "\uD83D\uDC68\u200D\uD83C\uDF73",
            role = FamilyRole.ADMIN,
            familyId = familyId
        )

        return Pair(family, member)
    }
}
