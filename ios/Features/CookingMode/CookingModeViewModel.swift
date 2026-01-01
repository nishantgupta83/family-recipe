import Foundation
import Combine

// MARK: - Cooking Mode ViewModel

@MainActor
class CookingModeViewModel: ObservableObject {
    @Published var workstate: CookingWorkstate
    @Published var isPlaying = false

    private let recipe: Recipe
    private var timerCancellable: AnyCancellable?

    init(recipe: Recipe) {
        self.recipe = recipe
        self.workstate = CookingWorkstate.load()

        // Start timer update loop
        startTimerUpdates()
    }

    deinit {
        timerCancellable?.cancel()
    }

    // MARK: - Computed Properties

    var currentStepIndex: Int {
        workstate.stepIndex
    }

    var currentStep: Instruction? {
        guard currentStepIndex < recipe.instructions.count else { return nil }
        return recipe.instructions[currentStepIndex]
    }

    var progress: Double {
        guard recipe.instructions.count > 0 else { return 0 }
        return Double(currentStepIndex + 1) / Double(recipe.instructions.count)
    }

    var canGoPrevious: Bool {
        currentStepIndex > 0
    }

    var canGoNext: Bool {
        currentStepIndex < recipe.instructions.count - 1
    }

    var isLastStep: Bool {
        currentStepIndex == recipe.instructions.count - 1
    }

    var isCurrentStepCompleted: Bool {
        workstate.isStepCompleted(currentStepIndex)
    }

    // MARK: - Session Management

    func startSession() {
        if workstate.activeRecipeId != recipe.id {
            workstate.startSession(recipeId: recipe.id)
        }
        saveWorkstate()
    }

    func endSession() {
        workstate.endSession()
        CookingWorkstate.clear()
    }

    func pauseSession() {
        workstate.pause()
        saveWorkstate()
    }

    func resumeSession() {
        workstate.resume()
        saveWorkstate()
    }

    // MARK: - Navigation

    func nextStep() {
        let totalSteps = recipe.instructions.count
        if workstate.goToNextStep(totalSteps: totalSteps) {
            saveWorkstate()
        }
    }

    func previousStep() {
        if workstate.goToPreviousStep() {
            saveWorkstate()
        }
    }

    func goToStep(_ step: Int) {
        let totalSteps = recipe.instructions.count
        if workstate.goToStep(step, totalSteps: totalSteps) {
            saveWorkstate()
        }
    }

    func completeCurrentStep() {
        workstate.completeCurrentStep()
        saveWorkstate()
    }

    // MARK: - Timer Management

    func addTimer(duration: TimeInterval, label: String? = nil) {
        var timer = workstate.addTimer(duration: duration, label: label)
        timer.start()
        // Update the timer in workstate
        if let index = workstate.timers.firstIndex(where: { $0.id == timer.id }) {
            workstate.timers[index] = timer
        }
        saveWorkstate()
    }

    func removeTimer(id: UUID) {
        workstate.removeTimer(id: id)
        saveWorkstate()
    }

    private func startTimerUpdates() {
        timerCancellable = Timer.publish(every: 1, on: .main, in: .common)
            .autoconnect()
            .sink { [weak self] _ in
                self?.updateTimers()
            }
    }

    private func updateTimers() {
        var updated = false
        for i in workstate.timers.indices {
            workstate.timers[i].updateRemainingTime()
            updated = true
        }
        if updated {
            objectWillChange.send()
        }
    }

    // MARK: - Scaling

    func setScale(_ factor: Double) {
        workstate.setScaleFactor(factor)
        saveWorkstate()
    }

    func scaleAmount(_ amount: Double) -> Double {
        workstate.scaleAmount(amount)
    }

    // MARK: - Persistence

    private func saveWorkstate() {
        workstate.save()
    }
}
