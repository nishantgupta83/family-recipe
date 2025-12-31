import Foundation
import AVFoundation
import Speech

// MARK: - Voice Service

/// Handles text-to-speech and speech-to-text for cooking assistant
@MainActor
final class VoiceService: NSObject, ObservableObject {
    static let shared = VoiceService()

    // MARK: - Published State

    @Published var isListening = false
    @Published var isSpeaking = false
    @Published var recognizedText = ""
    @Published var errorMessage: String?

    // MARK: - TTS

    private let synthesizer = AVSpeechSynthesizer()

    // MARK: - STT

    private let speechRecognizer = SFSpeechRecognizer(locale: Locale(identifier: "en-US"))
    private var recognitionRequest: SFSpeechAudioBufferRecognitionRequest?
    private var recognitionTask: SFSpeechRecognitionTask?
    private let audioEngine = AVAudioEngine()

    // MARK: - Settings

    var speechRate: Float = AVSpeechUtteranceDefaultSpeechRate
    var voicePitch: Float = 1.0
    var voiceVolume: Float = 1.0

    private override init() {
        super.init()
        synthesizer.delegate = self
    }

    // MARK: - Text-to-Speech

    func speak(_ text: String) {
        guard !text.isEmpty else { return }

        // Stop any current speech
        if synthesizer.isSpeaking {
            synthesizer.stopSpeaking(at: .immediate)
        }

        let utterance = AVSpeechUtterance(string: text)
        utterance.rate = speechRate
        utterance.pitchMultiplier = voicePitch
        utterance.volume = voiceVolume

        // Use a natural voice if available
        if let voice = AVSpeechSynthesisVoice(language: "en-US") {
            utterance.voice = voice
        }

        isSpeaking = true
        synthesizer.speak(utterance)
    }

    func stopSpeaking() {
        synthesizer.stopSpeaking(at: .immediate)
        isSpeaking = false
    }

    // MARK: - Speech-to-Text

    func requestSpeechAuthorization() async -> Bool {
        await withCheckedContinuation { continuation in
            SFSpeechRecognizer.requestAuthorization { status in
                continuation.resume(returning: status == .authorized)
            }
        }
    }

    func startListening(onResult: @escaping (String) -> Void) {
        guard let speechRecognizer = speechRecognizer, speechRecognizer.isAvailable else {
            errorMessage = "Speech recognition not available"
            return
        }

        // Cancel any existing task
        stopListening()

        do {
            try configureAudioSession()

            recognitionRequest = SFSpeechAudioBufferRecognitionRequest()
            guard let recognitionRequest = recognitionRequest else { return }

            recognitionRequest.shouldReportPartialResults = true

            let inputNode = audioEngine.inputNode
            let recordingFormat = inputNode.outputFormat(forBus: 0)

            inputNode.installTap(onBus: 0, bufferSize: 1024, format: recordingFormat) { buffer, _ in
                self.recognitionRequest?.append(buffer)
            }

            audioEngine.prepare()
            try audioEngine.start()

            isListening = true
            recognizedText = ""

            recognitionTask = speechRecognizer.recognitionTask(with: recognitionRequest) { [weak self] result, error in
                guard let self = self else { return }

                if let result = result {
                    let text = result.bestTranscription.formattedString
                    Task { @MainActor in
                        self.recognizedText = text

                        if result.isFinal {
                            onResult(text)
                            self.stopListening()
                        }
                    }
                }

                if let error = error {
                    Task { @MainActor in
                        self.errorMessage = error.localizedDescription
                        self.stopListening()
                    }
                }
            }
        } catch {
            errorMessage = error.localizedDescription
            stopListening()
        }
    }

    func stopListening() {
        audioEngine.stop()
        audioEngine.inputNode.removeTap(onBus: 0)

        recognitionRequest?.endAudio()
        recognitionRequest = nil

        recognitionTask?.cancel()
        recognitionTask = nil

        isListening = false
    }

    private func configureAudioSession() throws {
        let audioSession = AVAudioSession.sharedInstance()
        try audioSession.setCategory(.record, mode: .measurement, options: .duckOthers)
        try audioSession.setActive(true, options: .notifyOthersOnDeactivation)
    }

    // MARK: - Convenience

    func speakStep(_ step: Instruction) {
        speak("Step \(step.stepNumber): \(step.text)")
    }

    func speakResponse(_ response: String) {
        speak(response)
    }
}

// MARK: - AVSpeechSynthesizerDelegate

extension VoiceService: AVSpeechSynthesizerDelegate {
    nonisolated func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didFinish utterance: AVSpeechUtterance) {
        Task { @MainActor in
            isSpeaking = false
        }
    }

    nonisolated func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didCancel utterance: AVSpeechUtterance) {
        Task { @MainActor in
            isSpeaking = false
        }
    }
}
