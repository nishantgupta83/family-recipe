import Foundation
import SwiftData

// MARK: - Family Member Repository Protocol

protocol FamilyMemberRepositoryProtocol {
    func getMember(id: UUID) async throws -> FamilyMember?
    func getMembers(familyId: UUID) async throws -> [FamilyMember]
    func getAdmin(familyId: UUID) async throws -> FamilyMember?
    func createMember(_ member: FamilyMember) async throws
    func updateMember(_ member: FamilyMember) async throws
    func deleteMember(_ member: FamilyMember) async throws
    func updateActivity(memberId: UUID) async throws
    func toggleFavorite(memberId: UUID, recipeId: UUID) async throws
}

// MARK: - Family Member Repository Implementation

@MainActor
final class FamilyMemberRepository: FamilyMemberRepositoryProtocol {
    private let modelContext: ModelContext

    init(modelContext: ModelContext) {
        self.modelContext = modelContext
    }

    func getMember(id: UUID) async throws -> FamilyMember? {
        let descriptor = FetchDescriptor<FamilyMember>(
            predicate: #Predicate { $0.id == id }
        )
        return try modelContext.fetch(descriptor).first
    }

    func getMembers(familyId: UUID) async throws -> [FamilyMember] {
        let descriptor = FetchDescriptor<FamilyMember>(
            predicate: #Predicate { $0.familyId == familyId },
            sortBy: [SortDescriptor(\.name)]
        )
        return try modelContext.fetch(descriptor)
    }

    func getAdmin(familyId: UUID) async throws -> FamilyMember? {
        let descriptor = FetchDescriptor<FamilyMember>(
            predicate: #Predicate { $0.familyId == familyId && $0.role == .admin }
        )
        return try modelContext.fetch(descriptor).first
    }

    func createMember(_ member: FamilyMember) async throws {
        modelContext.insert(member)
        try modelContext.save()
    }

    func updateMember(_ member: FamilyMember) async throws {
        member.updateActivity()
        try modelContext.save()
    }

    func deleteMember(_ member: FamilyMember) async throws {
        modelContext.delete(member)
        try modelContext.save()
    }

    func updateActivity(memberId: UUID) async throws {
        guard let member = try await getMember(id: memberId) else {
            throw FamilyMemberRepositoryError.memberNotFound
        }

        member.updateActivity()
        try modelContext.save()
    }

    func toggleFavorite(memberId: UUID, recipeId: UUID) async throws {
        guard let member = try await getMember(id: memberId) else {
            throw FamilyMemberRepositoryError.memberNotFound
        }

        member.toggleFavorite(recipeId)
        try modelContext.save()
    }
}

// MARK: - Additional Methods

extension FamilyMemberRepository {
    func addCreatedRecipe(memberId: UUID, recipeId: UUID) async throws {
        guard let member = try await getMember(id: memberId) else {
            throw FamilyMemberRepositoryError.memberNotFound
        }

        member.addCreatedRecipe(recipeId)
        try modelContext.save()
    }

    func getMemberCount(familyId: UUID) async throws -> Int {
        try await getMembers(familyId: familyId).count
    }

    func getActiveMembers(familyId: UUID) async throws -> [FamilyMember] {
        let members = try await getMembers(familyId: familyId)
        return members.filter { $0.isActive }
    }

    func updateRole(memberId: UUID, role: FamilyRole) async throws {
        guard let member = try await getMember(id: memberId) else {
            throw FamilyMemberRepositoryError.memberNotFound
        }

        member.role = role
        member.updateActivity()
        try modelContext.save()
    }
}

// MARK: - Errors

enum FamilyMemberRepositoryError: Error {
    case memberNotFound
    case cannotDeleteAdmin
}
