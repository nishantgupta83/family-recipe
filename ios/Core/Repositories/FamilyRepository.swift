import Foundation
import SwiftData

// MARK: - Family Repository Protocol

protocol FamilyRepositoryProtocol {
    func getFamily(id: UUID) async throws -> Family?
    func getFamilyByInviteCode(_ code: String) async throws -> Family?
    func createFamily(_ family: Family) async throws
    func updateFamily(_ family: Family) async throws
    func deleteFamily(_ family: Family) async throws
    func regenerateInviteCode(familyId: UUID) async throws -> String
    func addMember(familyId: UUID, memberId: UUID) async throws -> Bool
    func removeMember(familyId: UUID, memberId: UUID) async throws -> Bool
    func transferAdmin(familyId: UUID, newAdminId: UUID) async throws -> Bool
}

// MARK: - Family Repository Implementation

@MainActor
final class FamilyRepository: FamilyRepositoryProtocol {
    private let modelContext: ModelContext

    init(modelContext: ModelContext) {
        self.modelContext = modelContext
    }

    func getFamily(id: UUID) async throws -> Family? {
        let descriptor = FetchDescriptor<Family>(
            predicate: #Predicate { $0.id == id }
        )
        return try modelContext.fetch(descriptor).first
    }

    func getFamilyByInviteCode(_ code: String) async throws -> Family? {
        let descriptor = FetchDescriptor<Family>(
            predicate: #Predicate { $0.inviteCode == code }
        )
        return try modelContext.fetch(descriptor).first
    }

    func createFamily(_ family: Family) async throws {
        modelContext.insert(family)
        try modelContext.save()
    }

    func updateFamily(_ family: Family) async throws {
        family.updateActivity()
        try modelContext.save()
    }

    func deleteFamily(_ family: Family) async throws {
        modelContext.delete(family)
        try modelContext.save()
    }

    func regenerateInviteCode(familyId: UUID) async throws -> String {
        guard let family = try await getFamily(id: familyId) else {
            throw FamilyRepositoryError.familyNotFound
        }

        family.regenerateInviteCode()
        try modelContext.save()
        return family.inviteCode
    }

    func addMember(familyId: UUID, memberId: UUID) async throws -> Bool {
        guard let family = try await getFamily(id: familyId) else {
            throw FamilyRepositoryError.familyNotFound
        }

        let success = family.addMember(memberId)
        if success {
            try modelContext.save()
        }
        return success
    }

    func removeMember(familyId: UUID, memberId: UUID) async throws -> Bool {
        guard let family = try await getFamily(id: familyId) else {
            throw FamilyRepositoryError.familyNotFound
        }

        let success = family.removeMember(memberId)
        if success {
            try modelContext.save()
        }
        return success
    }

    func transferAdmin(familyId: UUID, newAdminId: UUID) async throws -> Bool {
        guard let family = try await getFamily(id: familyId) else {
            throw FamilyRepositoryError.familyNotFound
        }

        let success = family.transferAdmin(to: newAdminId)
        if success {
            try modelContext.save()
        }
        return success
    }
}

// MARK: - Settings Updates

extension FamilyRepository {
    func updateTheme(familyId: UUID, theme: TemplateKey) async throws {
        guard let family = try await getFamily(id: familyId) else {
            throw FamilyRepositoryError.familyNotFound
        }

        family.theme = theme
        family.updateActivity()
        try modelContext.save()
    }

    func updateLanguage(familyId: UUID, language: LanguageCode) async throws {
        guard let family = try await getFamily(id: familyId) else {
            throw FamilyRepositoryError.familyNotFound
        }

        family.language = language
        family.updateActivity()
        try modelContext.save()
    }

    func updateUnits(familyId: UUID, units: UnitSystem) async throws {
        guard let family = try await getFamily(id: familyId) else {
            throw FamilyRepositoryError.familyNotFound
        }

        family.units = units
        family.updateActivity()
        try modelContext.save()
    }
}

// MARK: - Errors

enum FamilyRepositoryError: Error {
    case familyNotFound
    case invalidInviteCode
    case memberAlreadyExists
    case cannotRemoveAdmin
}
