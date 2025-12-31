import SwiftUI
import VisionKit

// MARK: - Scan Recipe View

/// Entry point for scanning recipes using VisionKit Document Scanner
struct ScanRecipeView: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(\.templateTokens) private var tokens
    @EnvironmentObject private var appState: AppState

    @StateObject private var viewModel = ScanRecipeViewModel()

    var body: some View {
        NavigationStack {
            ZStack {
                tokens.palette.background
                    .ignoresSafeArea()

                content
            }
            .navigationTitle("Scan Recipe")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") {
                        dismiss()
                    }
                }
            }
            .sheet(isPresented: $viewModel.showScanner) {
                DocumentScannerView(
                    scannedImages: $viewModel.scannedImages,
                    isPresented: $viewModel.showScanner
                )
            }
            .navigationDestination(isPresented: $viewModel.showReview) {
                ReviewOCRView(
                    viewModel: viewModel,
                    familyId: appState.currentFamilyId ?? UUID(),
                    createdById: appState.currentMemberId ?? UUID()
                )
            }
            .alert("Scanning Error", isPresented: $viewModel.showError) {
                Button("OK", role: .cancel) {}
            } message: {
                Text(viewModel.errorMessage)
            }
        }
    }

    @ViewBuilder
    private var content: some View {
        VStack(spacing: tokens.spacing.xl) {
            Spacer()

            // Icon
            Image(systemName: "doc.viewfinder")
                .font(.system(size: 80))
                .foregroundStyle(tokens.palette.primary)

            // Title and description
            VStack(spacing: tokens.spacing.sm) {
                Text("Scan Your Recipe")
                    .font(tokens.typography.titleLarge)
                    .foregroundStyle(tokens.palette.text)

                Text("Take a photo of a handwritten or printed recipe to digitize it")
                    .font(tokens.typography.bodyMedium)
                    .foregroundStyle(tokens.palette.textSecondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, tokens.spacing.xl)
            }

            Spacer()

            // Processing indicator
            if viewModel.isProcessing {
                VStack(spacing: tokens.spacing.md) {
                    ProgressView(value: viewModel.progress)
                        .progressViewStyle(.linear)
                        .padding(.horizontal, tokens.spacing.xl)

                    Text("Processing scan...")
                        .font(tokens.typography.caption)
                        .foregroundStyle(tokens.palette.textSecondary)
                }
            }

            // Action buttons
            VStack(spacing: tokens.spacing.md) {
                Button {
                    viewModel.startScanning()
                } label: {
                    HStack(spacing: tokens.spacing.sm) {
                        Image(systemName: "camera.fill")
                        Text("Open Camera")
                    }
                    .font(tokens.typography.labelLarge)
                    .foregroundStyle(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, tokens.spacing.md)
                    .background(tokens.palette.primary)
                    .clipShape(tokens.shape.mediumRoundedRect)
                }
                .disabled(viewModel.isProcessing)

                // Tips
                tipsSection
            }
            .padding(.horizontal, tokens.spacing.lg)
            .padding(.bottom, tokens.spacing.xl)
        }
    }

    private var tipsSection: some View {
        VStack(alignment: .leading, spacing: tokens.spacing.sm) {
            Text("Tips for best results:")
                .font(tokens.typography.labelMedium)
                .foregroundStyle(tokens.palette.textSecondary)

            VStack(alignment: .leading, spacing: tokens.spacing.xs) {
                tipRow(icon: "light.max", text: "Ensure good lighting")
                tipRow(icon: "rectangle.portrait", text: "Keep the recipe flat")
                tipRow(icon: "hand.raised.slash", text: "Hold steady while scanning")
                tipRow(icon: "doc.on.doc", text: "Scan multiple pages if needed")
            }
        }
        .padding(tokens.spacing.md)
        .background(tokens.palette.secondary.opacity(0.5))
        .clipShape(tokens.shape.mediumRoundedRect)
    }

    private func tipRow(icon: String, text: String) -> some View {
        HStack(spacing: tokens.spacing.sm) {
            Image(systemName: icon)
                .font(.caption)
                .foregroundStyle(tokens.palette.primary)
                .frame(width: 20)

            Text(text)
                .font(tokens.typography.caption)
                .foregroundStyle(tokens.palette.text)
        }
    }
}

// MARK: - View Model

@MainActor
final class ScanRecipeViewModel: ObservableObject {
    @Published var showScanner = false
    @Published var showReview = false
    @Published var showError = false
    @Published var errorMessage = ""
    @Published var isProcessing = false
    @Published var progress: Double = 0

    @Published var scannedImages: [UIImage] = [] {
        didSet {
            if !scannedImages.isEmpty {
                processScannedImages()
            }
        }
    }

    // OCR Results
    @Published var ocrResult: OCRService.OCRResult?
    @Published var parsedRecipe: ParsedRecipe?
    @Published var rawOCRText: String = ""

    private let ocrService = OCRService()

    func startScanning() {
        // Check if document scanning is supported
        guard VNDocumentCameraViewController.isSupported else {
            errorMessage = "Document scanning is not supported on this device."
            showError = true
            return
        }

        scannedImages = []
        ocrResult = nil
        parsedRecipe = nil
        rawOCRText = ""
        showScanner = true
    }

    private func processScannedImages() {
        guard !scannedImages.isEmpty else { return }

        isProcessing = true
        progress = 0

        Task {
            do {
                // Process OCR
                let result = try await ocrService.processImages(scannedImages)
                self.ocrResult = result
                self.rawOCRText = result.text
                self.progress = 0.8

                // Parse into recipe structure
                let parsed = ocrService.parseRecipeText(result.text)
                self.parsedRecipe = parsed
                self.progress = 1.0

                // Show review screen
                self.isProcessing = false
                self.showReview = true

            } catch {
                self.isProcessing = false
                self.errorMessage = error.localizedDescription
                self.showError = true
            }
        }
    }
}

// MARK: - Document Scanner View (UIKit Wrapper)

struct DocumentScannerView: UIViewControllerRepresentable {
    @Binding var scannedImages: [UIImage]
    @Binding var isPresented: Bool

    func makeUIViewController(context: Context) -> VNDocumentCameraViewController {
        let scanner = VNDocumentCameraViewController()
        scanner.delegate = context.coordinator
        return scanner
    }

    func updateUIViewController(_ uiViewController: VNDocumentCameraViewController, context: Context) {}

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    class Coordinator: NSObject, VNDocumentCameraViewControllerDelegate {
        let parent: DocumentScannerView

        init(_ parent: DocumentScannerView) {
            self.parent = parent
        }

        func documentCameraViewController(_ controller: VNDocumentCameraViewController, didFinishWith scan: VNDocumentCameraScan) {
            var images: [UIImage] = []
            for pageIndex in 0..<scan.pageCount {
                images.append(scan.imageOfPage(at: pageIndex))
            }
            parent.scannedImages = images
            parent.isPresented = false
        }

        func documentCameraViewControllerDidCancel(_ controller: VNDocumentCameraViewController) {
            parent.isPresented = false
        }

        func documentCameraViewController(_ controller: VNDocumentCameraViewController, didFailWithError error: Error) {
            parent.isPresented = false
        }
    }
}

// MARK: - Preview

#Preview {
    ScanRecipeView()
        .environmentObject(AppState())
}
