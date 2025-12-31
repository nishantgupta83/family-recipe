import SwiftUI
import SwiftData

@main
struct FamilyRecipeApp: App {
    let modelContainer: ModelContainer

    @StateObject private var appState = AppState()

    init() {
        do {
            let schema = Schema([
                Recipe.self,
                Family.self,
                FamilyMember.self
            ])

            let modelConfiguration = ModelConfiguration(
                schema: schema,
                isStoredInMemoryOnly: false,
                allowsSave: true
            )

            modelContainer = try ModelContainer(
                for: schema,
                configurations: [modelConfiguration]
            )

            // Insert sample data if first launch
            insertSampleDataIfNeeded()

        } catch {
            fatalError("Could not initialize ModelContainer: \(error)")
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(appState)
                .environment(\.templateTokens, appState.currentTemplate)
        }
        .modelContainer(modelContainer)
    }

    private func insertSampleDataIfNeeded() {
        let context = modelContainer.mainContext

        // Check if we already have data
        let familyDescriptor = FetchDescriptor<Family>()
        let existingFamilies = (try? context.fetch(familyDescriptor)) ?? []

        guard existingFamilies.isEmpty else { return }

        // Create sample family and member
        let (sampleFamily, sampleMember) = Family.sampleFamily()

        context.insert(sampleFamily)
        context.insert(sampleMember)

        // Create sample recipes
        let sampleRecipes = Recipe.sampleRecipes(
            familyId: sampleFamily.id,
            createdById: sampleMember.id
        )

        for recipe in sampleRecipes {
            context.insert(recipe)
        }

        try? context.save()
    }
}

// MARK: - Content View

struct ContentView: View {
    @EnvironmentObject private var appState: AppState

    var body: some View {
        Group {
            if appState.isOnboarded {
                MainTabView()
            } else {
                OnboardingView()
            }
        }
        .animation(.easeInOut, value: appState.isOnboarded)
    }
}

// MARK: - App State

@MainActor
class AppState: ObservableObject {
    @Published var currentMemberId: UUID?
    @Published var currentFamilyId: UUID?
    @Published var isOnboarded: Bool
    @Published var currentTemplate: TemplateTokens
    @Published var cookingWorkstate: CookingWorkstate

    private let defaults = UserDefaults.standard

    init() {
        // Load persisted state
        self.isOnboarded = defaults.bool(forKey: "isOnboarded")
        self.currentMemberId = defaults.string(forKey: "currentMemberId").flatMap { UUID(uuidString: $0) }
        self.currentFamilyId = defaults.string(forKey: "currentFamilyId").flatMap { UUID(uuidString: $0) }

        // Load template based on family settings or default
        let templateKey = defaults.string(forKey: "templateKey") ?? "vintage"
        self.currentTemplate = TemplateTokens.template(for: TemplateKey(rawValue: templateKey) ?? .vintage)

        // Load cooking workstate
        self.cookingWorkstate = CookingWorkstate.load()
    }

    func completeOnboarding(memberId: UUID, familyId: UUID) {
        currentMemberId = memberId
        currentFamilyId = familyId
        isOnboarded = true

        defaults.set(true, forKey: "isOnboarded")
        defaults.set(memberId.uuidString, forKey: "currentMemberId")
        defaults.set(familyId.uuidString, forKey: "currentFamilyId")
    }

    func updateTemplate(_ key: TemplateKey) {
        currentTemplate = TemplateTokens.template(for: key)
        defaults.set(key.rawValue, forKey: "templateKey")
    }

    func logout() {
        currentMemberId = nil
        currentFamilyId = nil
        isOnboarded = false

        defaults.removeObject(forKey: "isOnboarded")
        defaults.removeObject(forKey: "currentMemberId")
        defaults.removeObject(forKey: "currentFamilyId")

        cookingWorkstate.endSession()
        CookingWorkstate.clear()
    }

    func saveCookingWorkstate() {
        cookingWorkstate.save()
    }
}

// MARK: - Placeholder Views (to be implemented)

struct MainTabView: View {
    var body: some View {
        TabView {
            HomeView()
                .tabItem {
                    Label("Home", systemImage: "house")
                }

            SearchView()
                .tabItem {
                    Label("Search", systemImage: "magnifyingglass")
                }

            AddRecipeView()
                .tabItem {
                    Label("Add", systemImage: "plus.circle.fill")
                }

            FamilyView()
                .tabItem {
                    Label("Family", systemImage: "person.3")
                }

            SettingsView()
                .tabItem {
                    Label("Settings", systemImage: "gearshape")
                }
        }
    }
}

struct OnboardingView: View {
    var body: some View {
        Text("Onboarding")
    }
}

struct HomeView: View {
    var body: some View {
        NavigationStack {
            Text("Home")
                .navigationTitle("Family Recipes")
        }
    }
}

struct SearchView: View {
    var body: some View {
        NavigationStack {
            Text("Search")
                .navigationTitle("Search")
        }
    }
}

struct AddRecipeView: View {
    @Environment(\.templateTokens) private var tokens
    @State private var showScanSheet = false
    @State private var showManualEntry = false

    var body: some View {
        NavigationStack {
            VStack(spacing: tokens.spacing.xl) {
                Spacer()

                // Scan option
                Button {
                    showScanSheet = true
                } label: {
                    VStack(spacing: tokens.spacing.md) {
                        Image(systemName: "doc.viewfinder")
                            .font(.system(size: 48))
                            .foregroundStyle(tokens.palette.primary)

                        Text("Scan Recipe")
                            .font(tokens.typography.titleMedium)
                            .foregroundStyle(tokens.palette.text)

                        Text("Take a photo of a handwritten or printed recipe")
                            .font(tokens.typography.caption)
                            .foregroundStyle(tokens.palette.textSecondary)
                            .multilineTextAlignment(.center)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(tokens.spacing.xl)
                    .background(tokens.palette.surface)
                    .clipShape(tokens.shape.mediumRoundedRect)
                    .shadow(color: tokens.decoration.shadowStyle.shadowColor, radius: tokens.decoration.shadowStyle.radius, y: tokens.decoration.shadowStyle.y)
                }

                // Manual entry option
                Button {
                    showManualEntry = true
                } label: {
                    VStack(spacing: tokens.spacing.md) {
                        Image(systemName: "square.and.pencil")
                            .font(.system(size: 48))
                            .foregroundStyle(tokens.palette.accent)

                        Text("Enter Manually")
                            .font(tokens.typography.titleMedium)
                            .foregroundStyle(tokens.palette.text)

                        Text("Type in a recipe from memory or another source")
                            .font(tokens.typography.caption)
                            .foregroundStyle(tokens.palette.textSecondary)
                            .multilineTextAlignment(.center)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(tokens.spacing.xl)
                    .background(tokens.palette.surface)
                    .clipShape(tokens.shape.mediumRoundedRect)
                    .shadow(color: tokens.decoration.shadowStyle.shadowColor, radius: tokens.decoration.shadowStyle.radius, y: tokens.decoration.shadowStyle.y)
                }

                Spacer()
            }
            .padding(tokens.spacing.lg)
            .background(tokens.palette.background)
            .navigationTitle("New Recipe")
            .fullScreenCover(isPresented: $showScanSheet) {
                ScanRecipeView()
            }
            .sheet(isPresented: $showManualEntry) {
                ManualRecipeEntryView()
            }
        }
    }
}

struct ManualRecipeEntryView: View {
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            Text("Manual Entry Coming Soon")
                .navigationTitle("New Recipe")
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Cancel") { dismiss() }
                    }
                }
        }
    }
}

struct FamilyView: View {
    var body: some View {
        NavigationStack {
            Text("Family")
                .navigationTitle("Family")
        }
    }
}

struct SettingsView: View {
    var body: some View {
        NavigationStack {
            Text("Settings")
                .navigationTitle("Settings")
        }
    }
}

// MARK: - Preview

#Preview {
    ContentView()
        .environmentObject(AppState())
}
