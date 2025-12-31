import SwiftUI
import SwiftData

// MARK: - Cooking Mode View

struct CookingModeView: View {
    @Environment(\.modelContext) private var modelContext
    @Environment(\.templateTokens) private var tokens
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject private var appState: AppState

    let recipe: Recipe

    @StateObject private var viewModel: CookingModeViewModel

    @State private var showingTimerSheet = false
    @State private var showingAssistant = false

    init(recipe: Recipe) {
        self.recipe = recipe
        _viewModel = StateObject(wrappedValue: CookingModeViewModel(recipe: recipe))
    }

    var body: some View {
        ZStack {
            tokens.palette.background.ignoresSafeArea()

            VStack(spacing: 0) {
                // Header
                cookingHeader

                // Progress bar
                progressBar

                // Step content
                stepContent

                // Timer display (if active)
                if !viewModel.workstate.timers.isEmpty {
                    timerDisplay
                }

                // Navigation controls
                navigationControls

                // Assistant button
                assistantButton
            }
        }
        .navigationBarHidden(true)
        .onAppear {
            viewModel.startSession()
            UIApplication.shared.isIdleTimerDisabled = true
        }
        .onDisappear {
            UIApplication.shared.isIdleTimerDisabled = false
        }
        .sheet(isPresented: $showingTimerSheet) {
            TimerSheet(viewModel: viewModel, tokens: tokens)
        }
        .sheet(isPresented: $showingAssistant) {
            AssistantSheet(viewModel: viewModel, recipe: recipe, tokens: tokens)
        }
    }

    // MARK: - Header

    private var cookingHeader: some View {
        HStack {
            Button {
                viewModel.endSession()
                dismiss()
            } label: {
                Image(systemName: "xmark")
                    .font(.title3)
                    .foregroundColor(tokens.palette.text)
            }

            Spacer()

            VStack {
                Text(recipe.title)
                    .font(tokens.typography.titleMedium)
                    .foregroundColor(tokens.palette.text)
                    .lineLimit(1)

                Text("Step \(viewModel.currentStepIndex + 1) of \(recipe.instructions.count)")
                    .font(tokens.typography.caption)
                    .foregroundColor(tokens.palette.textSecondary)
            }

            Spacer()

            Button {
                showingTimerSheet = true
            } label: {
                Image(systemName: "timer")
                    .font(.title3)
                    .foregroundColor(tokens.palette.primary)
            }
        }
        .padding(.horizontal, tokens.spacing.md)
        .padding(.vertical, tokens.spacing.sm)
    }

    // MARK: - Progress Bar

    private var progressBar: some View {
        GeometryReader { geometry in
            ZStack(alignment: .leading) {
                Rectangle()
                    .fill(tokens.palette.divider)
                    .frame(height: 4)

                Rectangle()
                    .fill(tokens.palette.primary)
                    .frame(
                        width: geometry.size.width * viewModel.progress,
                        height: 4
                    )
                    .animation(.easeInOut, value: viewModel.progress)
            }
        }
        .frame(height: 4)
    }

    // MARK: - Step Content

    private var stepContent: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: tokens.spacing.lg) {
                // Step number badge
                Text("Step \(viewModel.currentStepIndex + 1)")
                    .font(tokens.typography.labelLarge)
                    .foregroundColor(.white)
                    .padding(.horizontal, tokens.spacing.md)
                    .padding(.vertical, tokens.spacing.sm)
                    .background(tokens.palette.primary)
                    .clipShape(Capsule())

                // Step text
                Text(viewModel.currentStep?.text ?? "")
                    .font(tokens.typography.bodyLarge)
                    .foregroundColor(tokens.palette.text)
                    .lineSpacing(8)

                // Duration if available
                if let duration = viewModel.currentStep?.formattedDuration {
                    HStack {
                        Image(systemName: "clock")
                        Text(duration)

                        Spacer()

                        Button("Start Timer") {
                            if let seconds = viewModel.currentStep?.durationSeconds {
                                viewModel.addTimer(duration: TimeInterval(seconds))
                            }
                        }
                        .font(tokens.typography.labelMedium)
                        .foregroundColor(tokens.palette.primary)
                    }
                    .font(tokens.typography.bodyMedium)
                    .foregroundColor(tokens.palette.textSecondary)
                    .padding(tokens.spacing.md)
                    .background(tokens.palette.secondary)
                    .cornerRadius(tokens.shape.cornerRadiusSmall)
                }

                // Completion check
                if viewModel.isCurrentStepCompleted {
                    HStack {
                        Image(systemName: "checkmark.circle.fill")
                            .foregroundColor(tokens.palette.success)
                        Text("Step completed")
                            .foregroundColor(tokens.palette.success)
                    }
                    .font(tokens.typography.labelMedium)
                }
            }
            .padding(tokens.spacing.lg)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    // MARK: - Timer Display

    private var timerDisplay: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: tokens.spacing.sm) {
                ForEach(viewModel.workstate.timers) { timer in
                    TimerCard(timer: timer, tokens: tokens) {
                        viewModel.removeTimer(id: timer.id)
                    }
                }
            }
            .padding(.horizontal, tokens.spacing.md)
        }
        .frame(height: 60)
        .background(tokens.palette.surface)
    }

    // MARK: - Navigation Controls

    private var navigationControls: some View {
        HStack(spacing: tokens.spacing.lg) {
            // Previous
            Button {
                viewModel.previousStep()
            } label: {
                HStack {
                    Image(systemName: "chevron.left")
                    Text("Previous")
                }
                .font(tokens.typography.labelLarge)
                .foregroundColor(viewModel.canGoPrevious ? tokens.palette.text : tokens.palette.textSecondary)
            }
            .disabled(!viewModel.canGoPrevious)

            Spacer()

            // Complete/Next
            Button {
                if viewModel.isLastStep {
                    viewModel.endSession()
                    dismiss()
                } else {
                    viewModel.nextStep()
                }
            } label: {
                HStack {
                    Text(viewModel.isLastStep ? "Finish" : "Next")
                    Image(systemName: viewModel.isLastStep ? "checkmark" : "chevron.right")
                }
                .font(tokens.typography.labelLarge)
                .foregroundColor(.white)
                .padding(.horizontal, tokens.spacing.lg)
                .padding(.vertical, tokens.spacing.md)
                .background(tokens.palette.primary)
                .clipShape(Capsule())
            }
        }
        .padding(tokens.spacing.md)
        .background(tokens.palette.surface)
    }

    // MARK: - Assistant Button

    private var assistantButton: some View {
        Button {
            showingAssistant = true
        } label: {
            HStack {
                Image(systemName: "waveform")
                Text("Ask Assistant")
            }
            .font(tokens.typography.labelMedium)
            .foregroundColor(tokens.palette.primary)
            .padding(.vertical, tokens.spacing.sm)
        }
        .padding(.bottom, tokens.spacing.sm)
    }
}

// MARK: - Timer Card

struct TimerCard: View {
    let timer: CookingTimer
    let tokens: TemplateTokens
    let onRemove: () -> Void

    var body: some View {
        HStack {
            VStack(alignment: .leading) {
                Text(timer.label)
                    .font(tokens.typography.caption)
                    .foregroundColor(tokens.palette.textSecondary)
                Text(timer.formattedRemaining)
                    .font(tokens.typography.titleMedium)
                    .foregroundColor(timer.isCompleted ? tokens.palette.success : tokens.palette.text)
            }

            Button {
                onRemove()
            } label: {
                Image(systemName: "xmark.circle.fill")
                    .foregroundColor(tokens.palette.textSecondary)
            }
        }
        .padding(tokens.spacing.sm)
        .background(timer.isCompleted ? tokens.palette.success.opacity(0.1) : tokens.palette.secondary)
        .cornerRadius(tokens.shape.cornerRadiusSmall)
    }
}

// MARK: - Timer Sheet

struct TimerSheet: View {
    @ObservedObject var viewModel: CookingModeViewModel
    let tokens: TemplateTokens
    @Environment(\.dismiss) private var dismiss

    @State private var minutes: Int = 5

    var body: some View {
        NavigationStack {
            VStack(spacing: tokens.spacing.lg) {
                Text("Set Timer")
                    .font(tokens.typography.titleLarge)

                Picker("Minutes", selection: $minutes) {
                    ForEach([1, 2, 3, 5, 10, 15, 20, 30, 45, 60], id: \.self) { m in
                        Text("\(m) min").tag(m)
                    }
                }
                .pickerStyle(.wheel)

                Button {
                    viewModel.addTimer(duration: TimeInterval(minutes * 60))
                    dismiss()
                } label: {
                    Text("Start Timer")
                        .font(tokens.typography.labelLarge)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(tokens.palette.primary)
                        .cornerRadius(tokens.shape.cornerRadiusMedium)
                }

                if !viewModel.workstate.timers.isEmpty {
                    Divider()

                    Text("Active Timers")
                        .font(tokens.typography.titleMedium)

                    ForEach(viewModel.workstate.timers) { timer in
                        HStack {
                            Text(timer.label)
                            Spacer()
                            Text(timer.formattedRemaining)
                                .font(tokens.typography.titleMedium)
                        }
                        .padding()
                        .background(tokens.palette.surface)
                        .cornerRadius(tokens.shape.cornerRadiusSmall)
                    }
                }

                Spacer()
            }
            .padding(tokens.spacing.lg)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Done") { dismiss() }
                }
            }
        }
        .presentationDetents([.medium])
    }
}

// MARK: - Assistant Sheet

struct AssistantSheet: View {
    @ObservedObject var viewModel: CookingModeViewModel
    let recipe: Recipe
    let tokens: TemplateTokens
    @Environment(\.dismiss) private var dismiss

    @State private var query = ""
    @State private var response = ""
    @State private var isListening = false

    var body: some View {
        NavigationStack {
            VStack(spacing: tokens.spacing.md) {
                // Response area
                if !response.isEmpty {
                    ScrollView {
                        Text(response)
                            .font(tokens.typography.bodyLarge)
                            .foregroundColor(tokens.palette.text)
                            .padding()
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(tokens.palette.secondary)
                            .cornerRadius(tokens.shape.cornerRadiusMedium)
                    }
                }

                Spacer()

                // Quick actions
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack {
                        QuickActionButton(title: "Next step", tokens: tokens) {
                            handleQuery("next step")
                        }
                        QuickActionButton(title: "Repeat", tokens: tokens) {
                            handleQuery("repeat")
                        }
                        QuickActionButton(title: "What's next?", tokens: tokens) {
                            handleQuery("what's next")
                        }
                        QuickActionButton(title: "How long left?", tokens: tokens) {
                            handleQuery("how long left")
                        }
                    }
                }

                // Input
                HStack {
                    TextField("Ask anything...", text: $query)
                        .textFieldStyle(.roundedBorder)

                    Button {
                        // Toggle voice input
                        isListening.toggle()
                    } label: {
                        Image(systemName: isListening ? "mic.fill" : "mic")
                            .foregroundColor(isListening ? .red : tokens.palette.primary)
                    }

                    Button {
                        handleQuery(query)
                        query = ""
                    } label: {
                        Image(systemName: "arrow.up.circle.fill")
                            .font(.title2)
                            .foregroundColor(tokens.palette.primary)
                    }
                    .disabled(query.isEmpty)
                }
            }
            .padding(tokens.spacing.md)
            .navigationTitle("Cooking Assistant")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Done") { dismiss() }
                }
            }
        }
        .presentationDetents([.medium, .large])
    }

    private func handleQuery(_ text: String) {
        let assistant = AssistantEngine.shared
        response = assistant.process(
            query: text,
            workstate: viewModel.workstate,
            recipe: recipe
        )

        // Handle navigation intents
        let intent = assistant.classifyIntent(text)
        switch intent {
        case .nextStep:
            viewModel.nextStep()
        case .previousStep:
            viewModel.previousStep()
        case .setTimer(let duration):
            viewModel.addTimer(duration: duration)
        default:
            break
        }
    }
}

struct QuickActionButton: View {
    let title: String
    let tokens: TemplateTokens
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(tokens.typography.labelMedium)
                .foregroundColor(tokens.palette.primary)
                .padding(.horizontal, tokens.spacing.md)
                .padding(.vertical, tokens.spacing.sm)
                .background(tokens.palette.primary.opacity(0.1))
                .clipShape(Capsule())
        }
    }
}
