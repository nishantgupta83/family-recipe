import Foundation

// MARK: - Cooking Workstate

/// Tracks the current cooking session state for context-aware assistant
struct CookingWorkstate: Codable {
    var activeRecipeId: UUID?
    var stepIndex: Int
    var completedSteps: Set<Int>
    var timers: [CookingTimer]
    var scaleFactor: Double
    var isPaused: Bool
    var preferences: CookingPreferences
    var lastAction: Date
    var sessionStartedAt: Date?

    init(
        activeRecipeId: UUID? = nil,
        stepIndex: Int = 0,
        completedSteps: Set<Int> = [],
        timers: [CookingTimer] = [],
        scaleFactor: Double = 1.0,
        isPaused: Bool = false,
        preferences: CookingPreferences = CookingPreferences(),
        lastAction: Date = Date(),
        sessionStartedAt: Date? = nil
    ) {
        self.activeRecipeId = activeRecipeId
        self.stepIndex = stepIndex
        self.completedSteps = completedSteps
        self.timers = timers
        self.scaleFactor = scaleFactor
        self.isPaused = isPaused
        self.preferences = preferences
        self.lastAction = lastAction
        self.sessionStartedAt = sessionStartedAt
    }

    // MARK: - Computed Properties

    var hasActiveSession: Bool {
        activeRecipeId != nil
    }

    var activeTimersCount: Int {
        timers.filter { $0.isRunning }.count
    }

    var completedStepsCount: Int {
        completedSteps.count
    }

    // MARK: - Session Management

    mutating func startSession(recipeId: UUID) {
        activeRecipeId = recipeId
        stepIndex = 0
        completedSteps = []
        timers = []
        scaleFactor = 1.0
        isPaused = false
        sessionStartedAt = Date()
        lastAction = Date()
    }

    mutating func endSession() {
        activeRecipeId = nil
        stepIndex = 0
        completedSteps = []
        timers.forEach { timer in
            // Cancel all timers
        }
        timers = []
        scaleFactor = 1.0
        isPaused = false
        sessionStartedAt = nil
        lastAction = Date()
    }

    mutating func pause() {
        isPaused = true
        lastAction = Date()
    }

    mutating func resume() {
        isPaused = false
        lastAction = Date()
    }

    // MARK: - Step Navigation

    mutating func goToNextStep(totalSteps: Int) -> Bool {
        guard stepIndex < totalSteps - 1 else { return false }
        completeCurrentStep()
        stepIndex += 1
        lastAction = Date()
        return true
    }

    mutating func goToPreviousStep() -> Bool {
        guard stepIndex > 0 else { return false }
        stepIndex -= 1
        lastAction = Date()
        return true
    }

    mutating func goToStep(_ step: Int, totalSteps: Int) -> Bool {
        guard step >= 0 && step < totalSteps else { return false }
        stepIndex = step
        lastAction = Date()
        return true
    }

    mutating func completeCurrentStep() {
        completedSteps.insert(stepIndex)
        lastAction = Date()
    }

    func isStepCompleted(_ step: Int) -> Bool {
        completedSteps.contains(step)
    }

    // MARK: - Timer Management

    mutating func addTimer(duration: TimeInterval, label: String? = nil) -> CookingTimer {
        let timer = CookingTimer(
            duration: duration,
            label: label ?? "Step \(stepIndex + 1)",
            associatedStep: stepIndex
        )
        timers.append(timer)
        lastAction = Date()
        return timer
    }

    mutating func removeTimer(id: UUID) {
        timers.removeAll { $0.id == id }
        lastAction = Date()
    }

    mutating func removeAllTimers() {
        timers = []
        lastAction = Date()
    }

    // MARK: - Scaling

    mutating func setScaleFactor(_ factor: Double) {
        scaleFactor = max(0.25, min(4.0, factor))
        lastAction = Date()
    }

    func scaleAmount(_ amount: Double) -> Double {
        amount * scaleFactor
    }

    // MARK: - Persistence

    static let userDefaultsKey = "cooking_workstate"

    func save() {
        if let encoded = try? JSONEncoder().encode(self) {
            UserDefaults.standard.set(encoded, forKey: Self.userDefaultsKey)
        }
    }

    static func load() -> CookingWorkstate {
        guard let data = UserDefaults.standard.data(forKey: userDefaultsKey),
              let workstate = try? JSONDecoder().decode(CookingWorkstate.self, from: data) else {
            return CookingWorkstate()
        }
        return workstate
    }

    static func clear() {
        UserDefaults.standard.removeObject(forKey: userDefaultsKey)
    }
}

// MARK: - Cooking Timer

struct CookingTimer: Codable, Identifiable, Equatable {
    let id: UUID
    var duration: TimeInterval
    var remainingTime: TimeInterval
    var label: String
    var associatedStep: Int?
    var startedAt: Date?
    var pausedAt: Date?

    var isRunning: Bool {
        startedAt != nil && pausedAt == nil && remainingTime > 0
    }

    var isCompleted: Bool {
        remainingTime <= 0
    }

    var isPaused: Bool {
        pausedAt != nil
    }

    var formattedRemaining: String {
        let minutes = Int(remainingTime) / 60
        let seconds = Int(remainingTime) % 60
        return String(format: "%d:%02d", minutes, seconds)
    }

    init(
        id: UUID = UUID(),
        duration: TimeInterval,
        label: String = "Timer",
        associatedStep: Int? = nil
    ) {
        self.id = id
        self.duration = duration
        self.remainingTime = duration
        self.label = label
        self.associatedStep = associatedStep
        self.startedAt = nil
        self.pausedAt = nil
    }

    mutating func start() {
        if startedAt == nil {
            startedAt = Date()
        } else if pausedAt != nil {
            // Resuming from pause
            pausedAt = nil
        }
    }

    mutating func pause() {
        guard isRunning else { return }
        pausedAt = Date()
    }

    mutating func stop() {
        startedAt = nil
        pausedAt = nil
        remainingTime = duration
    }

    mutating func updateRemainingTime() {
        guard let started = startedAt, pausedAt == nil else { return }
        let elapsed = Date().timeIntervalSince(started)
        remainingTime = max(0, duration - elapsed)
    }
}

// MARK: - Cooking Preferences

struct CookingPreferences: Codable, Equatable {
    var keepScreenOn: Bool
    var voiceEnabled: Bool
    var voiceSpeed: Double
    var autoAdvance: Bool
    var hapticFeedback: Bool
    var timerSoundEnabled: Bool

    init(
        keepScreenOn: Bool = true,
        voiceEnabled: Bool = false,
        voiceSpeed: Double = 1.0,
        autoAdvance: Bool = false,
        hapticFeedback: Bool = true,
        timerSoundEnabled: Bool = true
    ) {
        self.keepScreenOn = keepScreenOn
        self.voiceEnabled = voiceEnabled
        self.voiceSpeed = voiceSpeed
        self.autoAdvance = autoAdvance
        self.hapticFeedback = hapticFeedback
        self.timerSoundEnabled = timerSoundEnabled
    }
}

// MARK: - Session Summary

struct CookingSessionSummary: Codable {
    let recipeId: UUID
    let recipeTitle: String
    let startedAt: Date
    let completedAt: Date
    let totalSteps: Int
    let completedSteps: Int
    let scaleFactor: Double

    var duration: TimeInterval {
        completedAt.timeIntervalSince(startedAt)
    }

    var completionPercentage: Double {
        guard totalSteps > 0 else { return 0 }
        return Double(completedSteps) / Double(totalSteps) * 100
    }

    var formattedDuration: String {
        let minutes = Int(duration) / 60
        if minutes < 60 {
            return "\(minutes) min"
        } else {
            let hours = minutes / 60
            let remainingMinutes = minutes % 60
            return "\(hours)h \(remainingMinutes)m"
        }
    }
}
