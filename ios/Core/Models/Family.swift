import Foundation
import SwiftData

// MARK: - Family Model

@Model
final class Family {
    @Attribute(.unique) var id: UUID
    var name: String
    var familyDescription: String
    var inviteCode: String
    var adminMemberId: UUID
    var memberIds: [UUID]

    // Customization
    var theme: TemplateKey
    var language: LanguageCode
    var units: UnitSystem
    var iconEmoji: String

    // Timestamps
    var createdAt: Date
    var lastActivityAt: Date

    // Computed
    var memberCount: Int {
        memberIds.count
    }

    var canAddMembers: Bool {
        memberIds.count < 20
    }

    init(
        id: UUID = UUID(),
        name: String,
        familyDescription: String = "",
        inviteCode: String? = nil,
        adminMemberId: UUID,
        memberIds: [UUID]? = nil,
        theme: TemplateKey = .vintage,
        language: LanguageCode = .en,
        units: UnitSystem = .imperial,
        iconEmoji: String = "👨‍👩‍👧‍👦",
        createdAt: Date = Date(),
        lastActivityAt: Date = Date()
    ) {
        self.id = id
        self.name = name
        self.familyDescription = familyDescription
        self.inviteCode = inviteCode ?? Family.generateInviteCode()
        self.adminMemberId = adminMemberId
        self.memberIds = memberIds ?? [adminMemberId]
        self.theme = theme
        self.language = language
        self.units = units
        self.iconEmoji = iconEmoji
        self.createdAt = createdAt
        self.lastActivityAt = lastActivityAt
    }

    // MARK: - Methods

    static func generateInviteCode() -> String {
        let characters = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // Excluding similar chars (0/O, 1/I)
        return String((0..<6).map { _ in characters.randomElement()! })
    }

    func regenerateInviteCode() {
        inviteCode = Family.generateInviteCode()
        lastActivityAt = Date()
    }

    func addMember(_ memberId: UUID) -> Bool {
        guard canAddMembers, !memberIds.contains(memberId) else { return false }
        memberIds.append(memberId)
        lastActivityAt = Date()
        return true
    }

    func removeMember(_ memberId: UUID) -> Bool {
        guard memberId != adminMemberId else { return false } // Can't remove admin
        memberIds.removeAll { $0 == memberId }
        lastActivityAt = Date()
        return true
    }

    func isAdmin(_ memberId: UUID) -> Bool {
        memberId == adminMemberId
    }

    func transferAdmin(to newAdminId: UUID) -> Bool {
        guard memberIds.contains(newAdminId) else { return false }
        adminMemberId = newAdminId
        lastActivityAt = Date()
        return true
    }

    func updateActivity() {
        lastActivityAt = Date()
    }
}

// MARK: - Enums (TemplateKey moved to TemplateTokens.swift)

enum LanguageCode: String, Codable, CaseIterable {
    case en
    case es
    case hi

    var displayName: String {
        switch self {
        case .en: return "English"
        case .es: return "Español"
        case .hi: return "हिन्दी"
        }
    }
}

enum UnitSystem: String, Codable, CaseIterable {
    case imperial
    case metric

    var displayName: String {
        switch self {
        case .imperial: return "Imperial (cups, °F)"
        case .metric: return "Metric (ml, °C)"
        }
    }
}

// MARK: - Sample Data

extension Family {
    static func sampleFamily() -> (family: Family, member: FamilyMember) {
        let memberId = UUID()
        let familyId = UUID()

        let family = Family(
            id: familyId,
            name: "The Recipe Testers",
            familyDescription: "A family that loves to cook together!",
            adminMemberId: memberId,
            memberIds: [memberId],
            theme: .vintage,
            language: .en,
            units: .imperial,
            iconEmoji: "👨‍👩‍👧‍👦"
        )

        let member = FamilyMember(
            id: memberId,
            name: "Chef",
            avatarEmoji: "👨‍🍳",
            role: .admin,
            familyId: familyId
        )

        return (family, member)
    }
}
