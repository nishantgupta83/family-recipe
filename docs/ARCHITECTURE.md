# Architecture Documentation

## Overview

Family Recipe Native is a dual-platform native application built for iOS (SwiftUI) and Android (Jetpack Compose). Both platforms implement the same core contract specifications independently using platform-native components.

## Design Principles

1. **Native First**: No cross-platform frameworks - pure SwiftUI and Compose
2. **Offline First**: Local database is source of truth
3. **Contract Driven**: Shared YAML specs ensure feature parity
4. **Family Centric**: Privacy and family sharing are core concerns
5. **Context Aware**: Assistant knows cooking state at all times

---

## iOS Architecture

### Pattern: MVVM + Repository

```
┌─────────────────────────────────────────────────────────────┐
│                         View Layer                          │
│   SwiftUI Views (declarative, reactive to ViewModel state)  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      ViewModel Layer                         │
│   @Observable classes with @Published state                 │
│   Handles UI logic, calls Repository methods                │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Repository Layer                         │
│   Abstracts data sources (SwiftData, Network, Cache)        │
│   Single source of truth for data operations                │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Persistence Layer                        │
│   SwiftData ModelContainer + ModelContext                   │
│   UserDefaults for lightweight state (workstate)            │
└─────────────────────────────────────────────────────────────┘
```

### Key Components

| Component | Location | Responsibility |
|-----------|----------|----------------|
| `FamilyRecipeApp` | `App/` | App entry, environment setup |
| `AppState` | `App/` | Global state (current family, member) |
| `TemplateTokens` | `DesignSystem/` | Theme tokens via Environment |
| `RecipeRepository` | `Core/Repositories/` | Recipe CRUD operations |
| `AssistantEngine` | `Core/Services/AssistantEngine/` | Intent classification |
| `VoiceService` | `Core/Services/AssistantEngine/` | TTS + STT |

### Navigation

NavigationStack with path-based routing:

```swift
@State private var path = NavigationPath()

NavigationStack(path: $path) {
    HomeView()
        .navigationDestination(for: Recipe.self) { recipe in
            RecipeDetailView(recipe: recipe)
        }
}
```

### Environment Values

```swift
// Theme tokens available everywhere
@Environment(\.templateTokens) private var tokens

// App state for family context
@EnvironmentObject private var appState: AppState
```

---

## Android Architecture

### Pattern: ViewModel + StateFlow + Repository

```
┌─────────────────────────────────────────────────────────────┐
│                         UI Layer                             │
│   Compose Screens (collect StateFlow, emit events)          │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      ViewModel Layer                         │
│   Hilt-injected ViewModels with StateFlow<UiState>          │
│   Processes UI events, updates state                        │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Repository Layer                         │
│   Abstracts Room DAO + Network + DataStore                  │
│   Returns Flow<T> for reactive updates                      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Persistence Layer                        │
│   Room Database (SQLite) + DataStore (preferences)          │
└─────────────────────────────────────────────────────────────┘
```

### Key Components

| Component | Location | Responsibility |
|-----------|----------|----------------|
| `FamilyRecipeApp` | `FamilyRecipeApp.kt` | Application class, DI setup |
| `MainActivity` | `MainActivity.kt` | Single activity, Compose host |
| `AppViewModel` | `AppViewModel.kt` | Global state (family, member) |
| `FamilyRecipeTheme` | `designsystem/Theme.kt` | Material 3 + custom tokens |
| `RecipeRepository` | `core/repositories/` | Recipe data access |
| `AssistantEngine` | `core/services/assistant/` | Intent classification |
| `VoiceService` | `core/services/assistant/` | TTS + STT |

### Navigation

Navigation Compose with sealed class destinations:

```kotlin
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object RecipeDetail : Screen("recipe/{id}")
    object CookingMode : Screen("cooking/{id}")
}

NavHost(navController, startDestination = Screen.Home.route) {
    composable(Screen.Home.route) { HomeScreen(...) }
    composable(Screen.RecipeDetail.route) { RecipeDetailScreen(...) }
}
```

### State Management

```kotlin
// ViewModel exposes StateFlow
class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}

// Compose collects state
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
}
```

---

## Data Layer

### Core Models

Both platforms implement identical model structures:

```yaml
Recipe:
  id: UUID
  title: String
  recipeDescription: String
  ingredients: [Ingredient]
  instructions: [Instruction]
  category: RecipeCategory
  difficulty: RecipeDifficulty
  prepTimeMinutes: Int
  cookTimeMinutes: Int
  servings: Int
  familyId: UUID
  createdById: UUID
  familyMemory: String?
  favoritedBy: [UUID]
  createdAt: Timestamp
  updatedAt: Timestamp

Ingredient:
  id: UUID
  name: String
  quantity: Double
  unit: String
  notes: String?

Instruction:
  id: UUID
  stepNumber: Int
  text: String
  durationSeconds: Int?
  imageUrl: String?
```

### Persistence Strategy

| Platform | Primary Storage | Lightweight State |
|----------|-----------------|-------------------|
| iOS | SwiftData | UserDefaults |
| Android | Room | DataStore |

### Workstate Persistence

Cooking session state is stored separately for quick access. Expanded for context-aware assistant:

```swift
// iOS - UserDefaults
struct CookingWorkstate: Codable {
    // Core session
    var activeRecipeId: UUID?
    var stepIndex: Int
    var timers: [CookingTimer]
    var completedSteps: Set<Int>
    var scaleFactor: Double
    var servingsTarget: Int?

    // App context (for smarter assistant)
    var mode: AppMode              // .browsing, .cooking, .planning, .shopping
    var activeScreen: ScreenContext?

    // Assistant tracking
    var lastIntent: String?
    var lastAssistantQuery: String?
    var lastAssistantResponse: String?
    var lastAssistantResponseAt: Date?

    // User constraints
    var constraints: CookingConstraints?

    // Timestamps
    var lastUserActionAt: Date
    var sessionStartedAt: Date?
    var sessionPausedAt: Date?
}

struct CookingConstraints: Codable {
    var dietary: [DietaryRestriction]
    var allergies: [String]
    var timeBudgetMinutes: Int?
    var skillLevel: Difficulty?
    var equipmentAvailable: [String]
}
```

```kotlin
// Android - DataStore
data class CookingWorkstate(
    // Core session
    val activeRecipeId: String?,
    val stepIndex: Int,
    val timers: List<CookingTimer>,
    val completedSteps: Set<Int>,
    val scaleFactor: Double,
    val servingsTarget: Int?,

    // App context
    val mode: AppMode,
    val activeScreen: ScreenContext?,

    // Assistant tracking
    val lastIntent: String?,
    val lastAssistantQuery: String?,
    val lastAssistantResponse: String?,
    val lastAssistantResponseAt: Long?,

    // User constraints
    val constraints: CookingConstraints?,

    // Timestamps
    val lastUserActionAt: Long,
    val sessionStartedAt: Long?,
    val sessionPausedAt: Long?
)
```

---

## Assistant Engine Architecture

### Flow

```
┌──────────┐    ┌──────────────┐    ┌──────────────┐    ┌────────────┐
│  Voice   │───▶│   Intent     │───▶│   Response   │───▶│   Voice    │
│  Input   │    │   Router     │    │  Generator   │    │   Output   │
└──────────┘    └──────────────┘    └──────────────┘    └────────────┘
                      │                     │
                      ▼                     ▼
               ┌──────────────┐    ┌──────────────┐
               │  Workstate   │    │  Knowledge   │
               │   Context    │    │    Base      │
               └──────────────┘    └──────────────┘
```

### Intent Classification

Rule-based pattern matching (not ML):

```swift
enum AssistantIntent {
    case nextStep
    case previousStep
    case repeatStep
    case goToStep(Int)
    case setTimer(TimeInterval)
    case cancelTimer
    case checkTimer
    case substituteIngredient(String)
    case scaleServings(Int)
    case whatIngredients
    case whatIsNext
    case howLongLeft
    case explainTechnique(String)
    case help
    case unknown(String)
}
```

Pattern examples:
- `"next"` → `.nextStep`
- `"substitute for eggs"` → `.substituteIngredient("eggs")`
- `"set timer for 5 minutes"` → `.setTimer(300)`
- `"what does sauté mean"` → `.explainTechnique("sauté")`

### Knowledge Base

Static data embedded in app (no network required):

- **Substitutions**: 10+ common ingredients with ratios
- **Techniques**: 13+ cooking methods with explanations
- **Tips**: Context-specific cooking advice

### Voice Services

| Platform | TTS | STT |
|----------|-----|-----|
| iOS | AVSpeechSynthesizer | SFSpeechRecognizer |
| Android | TextToSpeech | SpeechRecognizer |

---

## Design System

### Template Tokens

Both platforms render the same design tokens natively:

```yaml
TemplateTokens:
  typography:
    displayLarge: Font
    displayMedium: Font
    titleLarge: Font
    titleMedium: Font
    bodyLarge: Font
    bodyMedium: Font
    labelLarge: Font
    labelMedium: Font
    caption: Font
    handwritten: Font  # For family memories

  spacing:
    xs: 4
    sm: 8
    md: 16
    lg: 24
    xl: 32

  palette:
    primary: Color
    secondary: Color
    accent: Color
    background: Color
    surface: Color
    text: Color
    textSecondary: Color

  shape:
    cornerRadiusSmall: 8
    cornerRadiusMedium: 12
    cornerRadiusLarge: 16
```

### Theme Variants

| Theme | Characteristics |
|-------|-----------------|
| Vintage | Warm browns, serif fonts, ornate corners |
| Modern | Clean whites, sans-serif, minimal |
| Playful | Bright colors, rounded shapes, fun fonts |

---

## Feature Modules

### Module Structure

Each feature follows consistent structure:

```
Feature/
├── {Feature}View.swift       # iOS SwiftUI view
├── {Feature}ViewModel.swift  # iOS ViewModel
├── {Feature}Screen.kt        # Android Compose screen
└── {Feature}ViewModel.kt     # Android ViewModel
```

### Feature List

| Feature | Description |
|---------|-------------|
| Home | Recipe list, category filter, family selector |
| RecipeDetail | Full recipe view, start cooking action |
| CookingMode | Step-by-step instructions, timers, assistant |
| AddRecipe | Recipe creation/edit form |
| Family | Create/join family, member management |
| Settings | Theme, language, units preferences |

---

## Future Considerations

### Phase 4: Cloud Sync MVP
See `docs/SYNC_RULES.md` for full specifications:
- Firebase Authentication (Apple Sign-In, Google Sign-In)
- Firestore collections: `/families/{familyId}/members`, `/families/{familyId}/recipes`
- Conflict resolution: last-write-wins + merge-by-id for arrays
- Invite code security: SHA-256 hash, rate limiting, 7-day expiration
- Membership lifecycle: active/removed/left states, admin transfer

### Phase 5: Templates + Polish
- Page curl animations for recipe browsing
- Haptic feedback on step completion
- Accessibility audit (VoiceOver, TalkBack)
- Family customization override layers

### Phase 6: Platform Extras
- iOS: Siri Shortcuts, Widgets, Live Activities
- Android: App Shortcuts, Widgets, richer notifications
