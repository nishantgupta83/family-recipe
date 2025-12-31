# Family Recipe Native App

A native iOS and Android family recipe app with context-aware cooking assistant. Private family cookbooks with step-by-step cooking guidance and voice-enabled assistance.

## Project Structure

```
family-recipe-native/
├── core-contract/              # Shared specifications
│   ├── schemas/                # Data model definitions (YAML)
│   │   └── data-models.yaml    # Recipe, Family, FamilyMember, CookingWorkstate
│   ├── intents/                # Assistant intent specifications
│   │   └── assistant-intents.yaml
│   └── tokens/                 # Design token definitions
│       └── design-tokens.yaml
├── ios/                        # Native iOS app (SwiftUI + SwiftData)
│   ├── App/                    # App entry point
│   ├── Core/
│   │   ├── Models/             # SwiftData models
│   │   ├── Repositories/       # Data access layer
│   │   ├── Services/           # Auth, Sync services
│   │   │   └── AssistantEngine/  # Context-aware assistant
│   │   └── Persistence/        # Local storage
│   ├── DesignSystem/           # Template tokens & theming
│   ├── Features/               # Feature modules
│   │   ├── Home/               # Recipe list & family selection
│   │   ├── RecipeDetail/       # Full recipe view
│   │   ├── CookingMode/        # Step-by-step cooking
│   │   ├── AddRecipe/          # Recipe creation
│   │   ├── Family/             # Family management
│   │   └── Settings/           # App settings
│   └── Navigation/             # Coordinator pattern
├── android/                    # Native Android app (Kotlin + Compose)
│   └── app/src/main/java/com/familyrecipe/
│       ├── core/
│       │   ├── models/         # Room entities
│       │   ├── database/       # Room DAOs & database
│       │   ├── repositories/   # Data access layer
│       │   └── services/
│       │       └── assistant/  # Context-aware assistant
│       ├── designsystem/       # Theme & tokens
│       ├── features/           # Feature screens
│       │   ├── home/
│       │   ├── recipedetail/
│       │   ├── cookingmode/
│       │   ├── addrecipe/
│       │   ├── family/
│       │   └── settings/
│       └── navigation/         # Navigation Compose setup
└── docs/                       # Documentation
    ├── ARCHITECTURE.md         # System architecture
    └── TECHNICAL_DIAGRAM.md    # Visual diagrams
```

## Key Features

### 1. Family-First Design
- Private family groups with 6-character invite codes
- Role-based permissions (Admin/Member)
- Per-family theme customization
- Member profiles with emoji avatars

### 2. Recipe Management
- Full CRUD operations for recipes
- Categories: Appetizer, Main, Dessert, Beverage, Breakfast, Snack
- Difficulty levels: Easy, Medium, Hard, Expert
- Family memory notes for sentimental context
- Favorites per family member

### 3. Cooking Mode
- Step-by-step instruction navigation
- Integrated timers with notifications
- Progress tracking and completion
- Keep-screen-awake during cooking
- Resume capability via workstate persistence

### 4. Context-Aware Assistant
- **Navigation**: "next step", "previous step", "go to step 3"
- **Timers**: "set timer for 5 minutes", "check timer"
- **Substitutions**: "substitute for eggs" (10+ ingredients)
- **Techniques**: "what does sauté mean?" (13+ techniques)
- **Scaling**: "double the recipe"
- **Voice I/O**: Platform-native TTS and speech recognition

### 5. Design Token System
- 3 built-in templates: Vintage, Modern, Playful
- Consistent theming across platforms
- Typography, spacing, colors, shapes
- Handwritten font for family memories

## Architecture

### iOS
| Layer | Technology |
|-------|------------|
| UI | SwiftUI |
| Architecture | MVVM + Repository |
| Persistence | SwiftData (iOS 17+) |
| Navigation | NavigationStack + Coordinator |
| Voice | AVSpeechSynthesizer + SFSpeechRecognizer |

### Android
| Layer | Technology |
|-------|------------|
| UI | Jetpack Compose |
| Architecture | ViewModel + StateFlow |
| Persistence | Room + DataStore |
| Navigation | Navigation Compose |
| Voice | TextToSpeech + SpeechRecognizer |

## Getting Started

### iOS

```bash
# Requirements: Xcode 15+, iOS 17+

# 1. Open project
open ios/FamilyRecipe.xcodeproj

# 2. Select target device/simulator

# 3. Build and run (Cmd + R)
```

### Android

```bash
# Requirements: Android Studio Hedgehog+, API 26+

# 1. Open android/ folder in Android Studio

# 2. Sync Gradle
./gradlew build

# 3. Run on device/emulator
./gradlew installDebug
```

## Core Contract

The `core-contract/` directory defines shared specifications that both platforms implement:

### Data Models (`schemas/data-models.yaml`)
- **Recipe**: title, ingredients, instructions, category, difficulty, times, servings
- **Family**: name, inviteCodeHash, admin, members, customization
- **FamilyMember**: name, avatar, role, status, preferences
- **FamilyCustomization**: templateKey + override layers (accent, fontScale, reducedMotion)
- **MemberPreferences**: personal accessibility overrides (fontScale, voiceSpeed, highContrast)
- **CookingWorkstate**: activeRecipe, stepIndex, timers, mode, constraints, lastIntent
- **CookingConstraints**: dietary restrictions, allergies, timeBudget, skillLevel

### Assistant Intents (`intents/assistant-intents.yaml`)
15+ intents: navigation, timers, substitutions, scaling, technique explanations

### Design Tokens (`tokens/design-tokens.yaml`)
Typography, spacing, colors, shapes for 3 template themes

## Cloud Sync (Phase 4)

See `docs/SYNC_RULES.md` for detailed specifications:
- **Collections**: `/families/{familyId}/members`, `/families/{familyId}/recipes`
- **Conflict Resolution**: Last-write-wins + merge-by-id for arrays
- **Invite Code Security**: SHA-256 hash storage, rate limiting, 7-day expiration
- **Membership Lifecycle**: active → removed/left, admin transfer protocol

## Development Phases

- [x] **Phase 0**: Core contract specifications
- [x] **Phase 1**: Family + Recipes + UI (MVP)
- [x] **Phase 2**: Cooking Mode + Workstate
- [x] **Phase 3**: Context-Aware Assistant
- [ ] **Phase 4**: Cloud Sync MVP (families actually share)
- [ ] **Phase 5**: Templates + Polish (animations, haptics)
- [ ] **Phase 6**: Advanced Features (Siri Shortcuts, Widgets)

## Assistant Knowledge Base

### Ingredient Substitutions
| Ingredient | Substitutes |
|------------|-------------|
| Eggs | Applesauce, flax egg, mashed banana, chia egg |
| Butter | Coconut oil, olive oil, applesauce, Greek yogurt |
| Milk | Oat milk, almond milk, coconut milk |
| Flour | Almond flour, oat flour, coconut flour |
| Sugar | Honey, maple syrup, stevia |
| Heavy Cream | Coconut cream, milk + butter |
| Sour Cream | Greek yogurt, blended cottage cheese |
| Garlic | Garlic powder |
| Onion | Onion powder, shallots |

### Cooking Techniques
sauté, fold, blanch, braise, deglaze, dice, julienne, mince, simmer, whisk, cream, reduce, sear

## File Highlights

### iOS
- `ios/Features/CookingMode/CookingModeView.swift` - Step-by-step UI
- `ios/Core/Services/AssistantEngine/AssistantEngine.swift` - Intent classification
- `ios/Core/Services/AssistantEngine/KnowledgeBase.swift` - Substitutions & techniques
- `ios/DesignSystem/TemplateTokens.swift` - Theme system

### Android
- `android/.../features/cookingmode/CookingModeScreen.kt` - Compose cooking UI
- `android/.../core/services/assistant/AssistantEngine.kt` - Intent classification
- `android/.../core/services/assistant/KnowledgeBase.kt` - Substitutions & techniques
- `android/.../designsystem/Theme.kt` - Material 3 theming

## License

Proprietary - All rights reserved.
