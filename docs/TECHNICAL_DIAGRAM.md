# Technical Diagrams

## System Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          FAMILY RECIPE NATIVE                               │
├─────────────────────────────────┬───────────────────────────────────────────┤
│           iOS App               │              Android App                  │
│         (SwiftUI)               │           (Jetpack Compose)               │
├─────────────────────────────────┼───────────────────────────────────────────┤
│                                 │                                           │
│  ┌───────────────────────────┐  │  ┌───────────────────────────────────┐   │
│  │      Feature Modules      │  │  │        Feature Modules            │   │
│  │  ┌─────┐ ┌─────┐ ┌─────┐  │  │  │  ┌─────┐ ┌─────┐ ┌─────────┐     │   │
│  │  │Home │ │Rcpe │ │Cook │  │  │  │  │Home │ │Rcpe │ │Cooking  │     │   │
│  │  │View │ │Detl │ │Mode │  │  │  │  │Scrn │ │Detl │ │Mode     │     │   │
│  │  └─────┘ └─────┘ └─────┘  │  │  │  └─────┘ └─────┘ └─────────┘     │   │
│  └───────────────────────────┘  │  └───────────────────────────────────┘   │
│              │                  │                    │                      │
│              ▼                  │                    ▼                      │
│  ┌───────────────────────────┐  │  ┌───────────────────────────────────┐   │
│  │       ViewModels          │  │  │          ViewModels               │   │
│  │  @Observable + @Published │  │  │    StateFlow + MutableState       │   │
│  └───────────────────────────┘  │  └───────────────────────────────────┘   │
│              │                  │                    │                      │
│              ▼                  │                    ▼                      │
│  ┌───────────────────────────┐  │  ┌───────────────────────────────────┐   │
│  │      Repositories         │  │  │         Repositories              │   │
│  │   RecipeRepository        │  │  │      RecipeRepository             │   │
│  │   FamilyRepository        │  │  │      FamilyRepository             │   │
│  └───────────────────────────┘  │  └───────────────────────────────────┘   │
│              │                  │                    │                      │
│              ▼                  │                    ▼                      │
│  ┌───────────────────────────┐  │  ┌───────────────────────────────────┐   │
│  │      Persistence          │  │  │         Persistence               │   │
│  │   SwiftData + UserDef     │  │  │      Room + DataStore             │   │
│  └───────────────────────────┘  │  └───────────────────────────────────┘   │
│                                 │                                           │
└─────────────────────────────────┴───────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           CORE CONTRACT                                     │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────────────┐  │
│  │  data-models    │  │ assistant-      │  │     design-tokens           │  │
│  │     .yaml       │  │ intents.yaml    │  │        .yaml                │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Data Model Relationships

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              FAMILY                                         │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │  id: UUID                                                            │   │
│  │  name: String                                                        │   │
│  │  inviteCode: String (6-char)                                         │   │
│  │  adminMemberId: UUID ──────────────────────────────────────┐        │   │
│  │  theme: TemplateKey                                         │        │   │
│  │  language: LanguageCode                                     │        │   │
│  │  units: UnitSystem                                          │        │   │
│  └─────────────────────────────────────────────────────────────│────────┘   │
│                                                                 │            │
│                          1:N                                    │            │
│                           │                                     │            │
│                           ▼                                     ▼            │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                        FAMILY MEMBER                                 │   │
│  │  ┌─────────────────────────────────────────────────────────────┐    │   │
│  │  │  id: UUID                                                    │    │   │
│  │  │  name: String                                                │    │   │
│  │  │  avatarEmoji: String                                         │    │   │
│  │  │  role: MemberRole (admin | member)                           │    │   │
│  │  │  familyId: UUID ◄────────────────────────────────────────────│────┘   │
│  │  │  createdRecipeIds: [UUID] ───────────────────────┐           │        │
│  │  │  favoriteRecipeIds: [UUID] ──────────────────────│───┐       │        │
│  │  └──────────────────────────────────────────────────│───│───────┘        │
│  └─────────────────────────────────────────────────────│───│────────────────┘
│                                                         │   │                │
│                                    N:1                  │   │   N:N          │
│                                     │                   │   │    │           │
│                                     ▼                   ▼   ▼    │           │
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │                            RECIPE                                     │   │
│  │  ┌────────────────────────────────────────────────────────────────┐  │   │
│  │  │  id: UUID                                                       │  │   │
│  │  │  title: String                                                  │  │   │
│  │  │  recipeDescription: String                                      │  │   │
│  │  │  category: RecipeCategory                                       │  │   │
│  │  │  difficulty: RecipeDifficulty                                   │  │   │
│  │  │  prepTimeMinutes: Int                                           │  │   │
│  │  │  cookTimeMinutes: Int                                           │  │   │
│  │  │  servings: Int                                                  │  │   │
│  │  │  familyId: UUID ◄───────────────────────────────────────────────│──┘   │
│  │  │  createdById: UUID ◄────────────────────────────────────────────│──────┘
│  │  │  familyMemory: String?                                          │      │
│  │  │  favoritedBy: [UUID]                                            │      │
│  │  │  ingredients: [Ingredient] ────────┐                            │      │
│  │  │  instructions: [Instruction] ──────│────┐                       │      │
│  │  └────────────────────────────────────│────│───────────────────────┘      │
│  └───────────────────────────────────────│────│──────────────────────────────┘
│                                           │    │                              │
│                          1:N              │    │  1:N                         │
│                           │               │    │   │                          │
│                           ▼               ▼    ▼   ▼                          │
│  ┌──────────────────────────────┐  ┌──────────────────────────────────────┐  │
│  │        INGREDIENT            │  │           INSTRUCTION                 │  │
│  │  ┌────────────────────────┐  │  │  ┌────────────────────────────────┐  │  │
│  │  │  id: UUID              │  │  │  │  id: UUID                      │  │  │
│  │  │  name: String          │  │  │  │  stepNumber: Int               │  │  │
│  │  │  quantity: Double      │  │  │  │  text: String                  │  │  │
│  │  │  unit: String          │  │  │  │  durationSeconds: Int?         │  │  │
│  │  │  notes: String?        │  │  │  │  imageUrl: String?             │  │  │
│  │  └────────────────────────┘  │  │  └────────────────────────────────┘  │  │
│  └──────────────────────────────┘  └──────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## Cooking Mode Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           COOKING MODE FLOW                                 │
└─────────────────────────────────────────────────────────────────────────────┘

User taps "Start Cooking" on RecipeDetailView
                    │
                    ▼
    ┌───────────────────────────────┐
    │   Load/Create Workstate       │
    │   ┌─────────────────────┐     │
    │   │ activeRecipeId      │     │
    │   │ stepIndex: 0        │     │
    │   │ timers: []          │     │
    │   │ completedSteps: []  │     │
    │   └─────────────────────┘     │
    └───────────────────────────────┘
                    │
                    ▼
    ┌───────────────────────────────┐
    │      COOKING MODE VIEW        │
    │  ┌─────────────────────────┐  │
    │  │   Progress Indicator    │  │
    │  │   Step 1 of N           │  │
    │  ├─────────────────────────┤  │
    │  │                         │  │
    │  │   Step Content          │  │
    │  │   "Preheat oven..."     │  │
    │  │                         │  │
    │  ├─────────────────────────┤  │
    │  │   Active Timers         │  │
    │  │   [05:32] Baking        │  │
    │  ├─────────────────────────┤  │
    │  │  [Prev] [Next] [Timer]  │  │
    │  │        [Assistant]      │  │
    │  └─────────────────────────┘  │
    └───────────────────────────────┘
                    │
        ┌───────────┴───────────┐
        │                       │
        ▼                       ▼
┌───────────────┐      ┌───────────────┐
│  Navigation   │      │   Assistant   │
│               │      │               │
│ • Next Step   │      │ Voice Input   │
│ • Prev Step   │      │     │         │
│ • Go to Step  │      │     ▼         │
│               │      │ Intent Router │
└───────────────┘      │     │         │
        │              │     ▼         │
        │              │ Response Gen  │
        │              │     │         │
        │              │     ▼         │
        │              │ Voice Output  │
        │              └───────────────┘
        │                       │
        └───────────┬───────────┘
                    │
                    ▼
    ┌───────────────────────────────┐
    │    Update & Persist State     │
    │    (UserDefaults/DataStore)   │
    └───────────────────────────────┘
                    │
                    ▼
    ┌───────────────────────────────┐
    │  Last step? → Completion UI   │
    │  Exit → Save for resume later │
    └───────────────────────────────┘
```

---

## Assistant Engine Pipeline

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        ASSISTANT ENGINE PIPELINE                            │
└─────────────────────────────────────────────────────────────────────────────┘

                         User Voice/Text Input
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           VOICE SERVICE                                     │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │  iOS: SFSpeechRecognizer          Android: SpeechRecognizer         │   │
│  │  ─────────────────────────        ────────────────────────          │   │
│  │  • Request authorization          • Check availability              │   │
│  │  • Start audio session            • Create recognizer intent        │   │
│  │  • Recognize speech               • Start listening                 │   │
│  │  • Return transcription           • Handle results callback         │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
                         Raw Text: "next step"
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          INTENT ROUTER                                      │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │  Pattern Matching (Regex + Keywords)                                 │   │
│  │  ─────────────────────────────────────                               │   │
│  │                                                                      │   │
│  │  Input: "next step"                                                  │   │
│  │                                                                      │   │
│  │  Patterns checked:                                                   │   │
│  │  ┌────────────────────────────────────────────────────────────────┐ │   │
│  │  │ "next|forward|continue"           → .nextStep          ✓       │ │   │
│  │  │ "previous|back|go back"           → .previousStep              │ │   │
│  │  │ "repeat|again|say that"           → .repeatStep                │ │   │
│  │  │ "go to step (\\d+)"               → .goToStep(n)               │ │   │
│  │  │ "set timer.*(\\d+).*(minute|sec)" → .setTimer(duration)        │ │   │
│  │  │ "substitute.*(\\w+)"              → .substituteIngredient(x)   │ │   │
│  │  │ "double|triple|half"              → .scaleServings(n)          │ │   │
│  │  │ "what does (\\w+) mean"           → .explainTechnique(x)       │ │   │
│  │  └────────────────────────────────────────────────────────────────┘ │   │
│  │                                                                      │   │
│  │  Output: AssistantIntent.nextStep                                    │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
                    AssistantIntent.nextStep
                                  │
                    ┌─────────────┴─────────────┐
                    │                           │
                    ▼                           ▼
    ┌───────────────────────┐    ┌───────────────────────────────┐
    │     WORKSTATE         │    │       KNOWLEDGE BASE          │
    │  ┌─────────────────┐  │    │  ┌─────────────────────────┐  │
    │  │ activeRecipeId  │  │    │  │    SUBSTITUTIONS        │  │
    │  │ stepIndex: 2    │  │    │  │  eggs → applesauce...   │  │
    │  │ timers: [...]   │  │    │  │  butter → coconut oil   │  │
    │  │ recipe: Recipe  │  │    │  │  milk → oat milk...     │  │
    │  └─────────────────┘  │    │  ├─────────────────────────┤  │
    └───────────────────────┘    │  │    TECHNIQUES           │  │
                    │            │  │  sauté → "Cook quickly  │  │
                    │            │  │    in a small amount..."│  │
                    │            │  │  fold → "Gently combine │  │
                    │            │  │    by cutting down..."  │  │
                    │            │  └─────────────────────────┘  │
                    │            └───────────────────────────────┘
                    │                           │
                    └─────────────┬─────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        RESPONSE GENERATOR                                   │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │  Intent: .nextStep                                                   │   │
│  │  Context: step 2 of 5, recipe "Apple Pie"                            │   │
│  │                                                                      │   │
│  │  Logic:                                                              │   │
│  │  ─────────                                                           │   │
│  │  nextIndex = stepIndex + 1 = 3                                       │   │
│  │  if (nextIndex < instructions.count) {                               │   │
│  │      step = instructions[nextIndex]                                  │   │
│  │      return "Step 4: \(step.text)"                                   │   │
│  │  } else {                                                            │   │
│  │      return "That was the last step! Your Apple Pie is ready."      │   │
│  │  }                                                                   │   │
│  │                                                                      │   │
│  │  Output: "Step 4: Pour filling into prepared crust."                 │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
                "Step 4: Pour filling into prepared crust."
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           VOICE SERVICE (TTS)                               │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │  iOS: AVSpeechSynthesizer         Android: TextToSpeech             │   │
│  │  ─────────────────────────        ─────────────────────             │   │
│  │  • Create utterance               • Initialize TTS engine           │   │
│  │  • Set rate, pitch, voice         • Set language, rate, pitch       │   │
│  │  • Speak utterance                • speak() with utterance ID       │   │
│  │  • Handle completion delegate     • Handle UtteranceProgressListener│   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
                         Audio Output to User
```

---

## Design Token System

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          DESIGN TOKEN SYSTEM                                │
└─────────────────────────────────────────────────────────────────────────────┘

                    ┌─────────────────────────────┐
                    │   core-contract/tokens/     │
                    │   design-tokens.yaml        │
                    └─────────────────────────────┘
                                  │
                    ┌─────────────┴─────────────┐
                    │                           │
                    ▼                           ▼
    ┌───────────────────────────┐   ┌───────────────────────────┐
    │         iOS               │   │        Android            │
    │  TemplateTokens.swift     │   │    Theme.kt               │
    │  ─────────────────────    │   │  ─────────────────────    │
    │  struct TemplateTokens {  │   │  object ThemeTokens {     │
    │    typography: Typography │   │    typography: Typography │
    │    spacing: Spacing       │   │    spacing: Spacing       │
    │    palette: ColorPalette  │   │    palette: ColorPalette  │
    │    shape: ShapeTokens     │   │    shape: ShapeTokens     │
    │  }                        │   │  }                        │
    └───────────────────────────┘   └───────────────────────────┘
                    │                           │
                    ▼                           ▼
    ┌───────────────────────────┐   ┌───────────────────────────┐
    │  Environment Injection    │   │  CompositionLocal         │
    │  ─────────────────────    │   │  ─────────────────────    │
    │  @Environment(\.tokens)   │   │  LocalTokens.current      │
    │  private var tokens       │   │  val tokens = ...         │
    └───────────────────────────┘   └───────────────────────────┘
                    │                           │
                    ▼                           ▼
    ┌───────────────────────────────────────────────────────────┐
    │                    TEMPLATE VARIANTS                       │
    │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐    │
    │  │   VINTAGE   │  │   MODERN    │  │    PLAYFUL      │    │
    │  ├─────────────┤  ├─────────────┤  ├─────────────────┤    │
    │  │ Serif fonts │  │ Sans-serif  │  │ Rounded fonts   │    │
    │  │ Warm browns │  │ Clean white │  │ Bright colors   │    │
    │  │ Book style  │  │ Minimal     │  │ Fun shapes      │    │
    │  │ Ornate      │  │ Sharp edges │  │ Extra rounded   │    │
    │  └─────────────┘  └─────────────┘  └─────────────────┘    │
    └───────────────────────────────────────────────────────────┘
```

---

## Navigation Structure

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          NAVIGATION GRAPH                                   │
└─────────────────────────────────────────────────────────────────────────────┘

                              ┌───────────┐
                              │   App     │
                              │  Launch   │
                              └─────┬─────┘
                                    │
                    ┌───────────────┼───────────────┐
                    │               │               │
                    ▼               ▼               ▼
            ┌───────────┐   ┌───────────┐   ┌───────────┐
            │ Onboarding│   │   Home    │   │  Family   │
            │  (First   │   │ (Main Tab)│   │  Setup    │
            │   Run)    │   │           │   │           │
            └─────┬─────┘   └─────┬─────┘   └───────────┘
                  │               │
                  └───────┬───────┘
                          │
                          ▼
    ┌─────────────────────────────────────────────────────────────────┐
    │                        MAIN TAB BAR                             │
    │  ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐    │
    │  │   Home    │  │  Search   │  │   Add     │  │ Settings  │    │
    │  │  (List)   │  │           │  │  Recipe   │  │           │    │
    │  └─────┬─────┘  └───────────┘  └─────┬─────┘  └───────────┘    │
    │        │                             │                          │
    │        ▼                             ▼                          │
    │  ┌───────────┐                 ┌───────────┐                   │
    │  │  Recipe   │────────────────▶│  Recipe   │                   │
    │  │  Detail   │  (edit action)  │   Form    │                   │
    │  └─────┬─────┘                 └───────────┘                   │
    │        │                                                        │
    │        │ Start Cooking                                          │
    │        ▼                                                        │
    │  ┌───────────────────────────────────────────┐                 │
    │  │            COOKING MODE                    │                 │
    │  │  (Full Screen Cover / Overlay)            │                 │
    │  │  ┌─────────────────────────────────────┐  │                 │
    │  │  │  Step View + Timers + Assistant     │  │                 │
    │  │  └─────────────────────────────────────┘  │                 │
    │  │                  │                        │                 │
    │  │                  ▼                        │                 │
    │  │  ┌─────────────────────────────────────┐  │                 │
    │  │  │     Assistant Sheet (Modal)         │  │                 │
    │  │  └─────────────────────────────────────┘  │                 │
    │  └───────────────────────────────────────────┘                 │
    └─────────────────────────────────────────────────────────────────┘
```

---

## State Flow (Android)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                      ANDROID STATE FLOW PATTERN                             │
└─────────────────────────────────────────────────────────────────────────────┘

    ┌─────────────────────────────────────────────────────────────────────┐
    │                          COMPOSE UI                                  │
    │  ┌───────────────────────────────────────────────────────────────┐  │
    │  │  @Composable                                                   │  │
    │  │  fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) { │  │
    │  │      val uiState by viewModel.uiState.collectAsStateWithLife() │  │
    │  │                                                                │  │
    │  │      when (uiState) {                                          │  │
    │  │          is Loading → LoadingIndicator()                       │  │
    │  │          is Success → RecipeList(uiState.recipes)             │  │
    │  │          is Error → ErrorMessage(uiState.message)             │  │
    │  │      }                                                         │  │
    │  │                                                                │  │
    │  │      // User action triggers event                             │  │
    │  │      Button(onClick = { viewModel.onRefresh() })              │  │
    │  │  }                                                             │  │
    │  └───────────────────────────────────────────────────────────────┘  │
    └─────────────────────────────────────────────────────────────────────┘
                                      │
                           ┌──────────┴──────────┐
                           │  collectAsState()   │
                           │  (Recomposition)    │
                           └──────────┬──────────┘
                                      │
    ┌─────────────────────────────────────────────────────────────────────┐
    │                         VIEW MODEL                                   │
    │  ┌───────────────────────────────────────────────────────────────┐  │
    │  │  @HiltViewModel                                                │  │
    │  │  class HomeViewModel @Inject constructor(                      │  │
    │  │      private val recipeRepo: RecipeRepository                  │  │
    │  │  ) : ViewModel() {                                             │  │
    │  │                                                                │  │
    │  │      private val _uiState = MutableStateFlow<UiState>(Loading) │  │
    │  │      val uiState: StateFlow<UiState> = _uiState.asStateFlow() │  │
    │  │                                                                │  │
    │  │      fun onRefresh() {                                         │  │
    │  │          viewModelScope.launch {                               │  │
    │  │              _uiState.value = Loading                          │  │
    │  │              val recipes = recipeRepo.getAllRecipes()          │  │
    │  │              _uiState.value = Success(recipes)                 │  │
    │  │          }                                                     │  │
    │  │      }                                                         │  │
    │  │  }                                                             │  │
    │  └───────────────────────────────────────────────────────────────┘  │
    └─────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
    ┌─────────────────────────────────────────────────────────────────────┐
    │                        REPOSITORY                                    │
    │  ┌───────────────────────────────────────────────────────────────┐  │
    │  │  class RecipeRepository @Inject constructor(                   │  │
    │  │      private val recipeDao: RecipeDao                          │  │
    │  │  ) {                                                           │  │
    │  │      suspend fun getAllRecipes(): List<Recipe> {               │  │
    │  │          return recipeDao.getAllRecipes()                      │  │
    │  │      }                                                         │  │
    │  │                                                                │  │
    │  │      fun observeRecipes(): Flow<List<Recipe>> {                │  │
    │  │          return recipeDao.observeAllRecipes()                  │  │
    │  │      }                                                         │  │
    │  │  }                                                             │  │
    │  └───────────────────────────────────────────────────────────────┘  │
    └─────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
    ┌─────────────────────────────────────────────────────────────────────┐
    │                           ROOM DAO                                   │
    │  ┌───────────────────────────────────────────────────────────────┐  │
    │  │  @Dao                                                          │  │
    │  │  interface RecipeDao {                                         │  │
    │  │      @Query("SELECT * FROM recipes")                           │  │
    │  │      suspend fun getAllRecipes(): List<RecipeEntity>           │  │
    │  │                                                                │  │
    │  │      @Query("SELECT * FROM recipes")                           │  │
    │  │      fun observeAllRecipes(): Flow<List<RecipeEntity>>         │  │
    │  │  }                                                             │  │
    │  └───────────────────────────────────────────────────────────────┘  │
    └─────────────────────────────────────────────────────────────────────┘
```

---

## File Organization

```
family-recipe-native/
│
├── core-contract/
│   ├── schemas/
│   │   └── data-models.yaml           # Recipe, Family, Member, Workstate
│   ├── intents/
│   │   └── assistant-intents.yaml     # 15+ assistant intents
│   └── tokens/
│       └── design-tokens.yaml         # Typography, colors, spacing, shapes
│
├── ios/
│   ├── App/
│   │   ├── FamilyRecipeApp.swift      # @main entry point
│   │   └── AppState.swift             # Global observable state
│   │
│   ├── Core/
│   │   ├── Models/
│   │   │   ├── Recipe.swift           # SwiftData @Model
│   │   │   ├── Family.swift
│   │   │   └── FamilyMember.swift
│   │   │
│   │   ├── Repositories/
│   │   │   ├── RecipeRepository.swift
│   │   │   └── FamilyRepository.swift
│   │   │
│   │   └── Services/
│   │       └── AssistantEngine/
│   │           ├── AssistantEngine.swift      # Intent classification
│   │           ├── KnowledgeBase.swift        # Substitutions, techniques
│   │           ├── ResponseGenerator.swift   # Natural language responses
│   │           └── VoiceService.swift         # TTS + STT
│   │
│   ├── DesignSystem/
│   │   ├── TemplateTokens.swift       # Token definitions
│   │   ├── Colors.swift               # Color palettes
│   │   └── Components/                # Reusable UI components
│   │
│   ├── Features/
│   │   ├── Home/
│   │   │   ├── HomeView.swift
│   │   │   └── HomeViewModel.swift
│   │   ├── RecipeDetail/
│   │   │   └── RecipeDetailView.swift
│   │   ├── CookingMode/
│   │   │   ├── CookingModeView.swift
│   │   │   └── CookingModeViewModel.swift
│   │   ├── AddRecipe/
│   │   ├── Family/
│   │   └── Settings/
│   │
│   └── Navigation/
│       └── AppCoordinator.swift
│
├── android/
│   └── app/src/main/
│       ├── java/com/familyrecipe/
│       │   ├── FamilyRecipeApp.kt     # Application class
│       │   ├── MainActivity.kt        # Single activity
│       │   ├── AppViewModel.kt        # Global state
│       │   │
│       │   ├── core/
│       │   │   ├── models/
│       │   │   │   ├── Recipe.kt
│       │   │   │   ├── Family.kt
│       │   │   │   └── FamilyMember.kt
│       │   │   │
│       │   │   ├── database/
│       │   │   │   ├── AppDatabase.kt
│       │   │   │   └── RecipeDao.kt
│       │   │   │
│       │   │   ├── repositories/
│       │   │   │   └── RecipeRepository.kt
│       │   │   │
│       │   │   └── services/
│       │   │       └── assistant/
│       │   │           ├── AssistantEngine.kt
│       │   │           ├── KnowledgeBase.kt
│       │   │           ├── ResponseGenerator.kt
│       │   │           └── VoiceService.kt
│       │   │
│       │   ├── designsystem/
│       │   │   ├── Theme.kt
│       │   │   └── Components.kt
│       │   │
│       │   ├── features/
│       │   │   ├── home/
│       │   │   │   ├── HomeScreen.kt
│       │   │   │   └── HomeViewModel.kt
│       │   │   ├── recipedetail/
│       │   │   │   └── RecipeDetailScreen.kt
│       │   │   ├── cookingmode/
│       │   │   │   ├── CookingModeScreen.kt
│       │   │   │   └── CookingModeViewModel.kt
│       │   │   ├── addrecipe/
│       │   │   ├── family/
│       │   │   └── settings/
│       │   │
│       │   └── navigation/
│       │       └── AppNavigation.kt
│       │
│       └── res/
│           ├── values/
│           └── drawable/
│
└── docs/
    ├── ARCHITECTURE.md
    └── TECHNICAL_DIAGRAM.md
```
