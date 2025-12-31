import Foundation
import SwiftData

// MARK: - Family Member Model

@Model
final class FamilyMember {
    @Attribute(.unique) var id: UUID
    var name: String
    var avatarEmoji: String
    var role: FamilyRole
    var familyId: UUID

    // Recipe tracking
    var createdRecipeIds: [UUID]
    var favoriteRecipeIds: [UUID]

    // Preferences
    var preferredLanguage: LanguageCode?

    // Timestamps
    var joinedAt: Date
    var lastActiveAt: Date

    // Computed
    var isActive: Bool {
        let thirtyDaysAgo = Calendar.current.date(byAdding: .day, value: -30, to: Date())!
        return lastActiveAt > thirtyDaysAgo
    }

    var recipesCreatedCount: Int {
        createdRecipeIds.count
    }

    var favoriteCount: Int {
        favoriteRecipeIds.count
    }

    init(
        id: UUID = UUID(),
        name: String,
        avatarEmoji: String = "👤",
        role: FamilyRole = .member,
        familyId: UUID,
        createdRecipeIds: [UUID] = [],
        favoriteRecipeIds: [UUID] = [],
        preferredLanguage: LanguageCode? = nil,
        joinedAt: Date = Date(),
        lastActiveAt: Date = Date()
    ) {
        self.id = id
        self.name = name
        self.avatarEmoji = avatarEmoji
        self.role = role
        self.familyId = familyId
        self.createdRecipeIds = createdRecipeIds
        self.favoriteRecipeIds = favoriteRecipeIds
        self.preferredLanguage = preferredLanguage
        self.joinedAt = joinedAt
        self.lastActiveAt = lastActiveAt
    }

    // MARK: - Methods

    func updateActivity() {
        lastActiveAt = Date()
    }

    func addCreatedRecipe(_ recipeId: UUID) {
        if !createdRecipeIds.contains(recipeId) {
            createdRecipeIds.append(recipeId)
            updateActivity()
        }
    }

    func addFavorite(_ recipeId: UUID) {
        if !favoriteRecipeIds.contains(recipeId) {
            favoriteRecipeIds.append(recipeId)
            updateActivity()
        }
    }

    func removeFavorite(_ recipeId: UUID) {
        favoriteRecipeIds.removeAll { $0 == recipeId }
        updateActivity()
    }

    func toggleFavorite(_ recipeId: UUID) {
        if favoriteRecipeIds.contains(recipeId) {
            removeFavorite(recipeId)
        } else {
            addFavorite(recipeId)
        }
    }

    func isFavorite(_ recipeId: UUID) -> Bool {
        favoriteRecipeIds.contains(recipeId)
    }

    // MARK: - Permissions

    var canCreateRecipes: Bool {
        role == .admin || role == .member
    }

    var canEditRecipes: Bool {
        role == .admin || role == .member
    }

    var canManageMembers: Bool {
        role == .admin
    }

    var canDeleteFamily: Bool {
        role == .admin
    }
}

// MARK: - Family Role

enum FamilyRole: String, Codable, CaseIterable {
    case admin
    case member

    var displayName: String {
        switch self {
        case .admin: return "Admin"
        case .member: return "Member"
        }
    }

    var description: String {
        switch self {
        case .admin: return "Can manage family members and settings"
        case .member: return "Can create and edit recipes"
        }
    }
}

// MARK: - Avatar Emojis

extension FamilyMember {
    static let availableEmojis: [String] = [
        // People
        "👨‍🍳", "👩‍🍳", "👨", "👩", "🧑",
        "👴", "👵", "🧓",
        "👦", "👧", "🧒",
        "👶",

        // Food related
        "🍳", "🥘", "🍲", "🥗", "🍰",
        "🧁", "🍪", "🥐", "🍕", "🌮",

        // Fun
        "🦊", "🐱", "🐶", "🐼", "🦁",
        "🌸", "🌻", "⭐", "🌈", "🎂"
    ]

    static var randomEmoji: String {
        availableEmojis.randomElement() ?? "👤"
    }
}

// MARK: - Sample Members

extension FamilyMember {
    static func sampleMembers(familyId: UUID, adminId: UUID) -> [FamilyMember] {
        [
            FamilyMember(
                id: adminId,
                name: "Mom",
                avatarEmoji: "👩‍🍳",
                role: .admin,
                familyId: familyId
            ),
            FamilyMember(
                name: "Dad",
                avatarEmoji: "👨‍🍳",
                role: .member,
                familyId: familyId
            ),
            FamilyMember(
                name: "Grandma",
                avatarEmoji: "👵",
                role: .member,
                familyId: familyId
            )
        ]
    }
}
