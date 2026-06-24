import Foundation
import Combine
import Shared

@MainActor
final class HomeViewModel: ObservableObject {
    @Published var isLoading: Bool = false
    @Published var error: DomainError?
    @Published var manifest: HomeManifest?

    private let getProfileUseCase: GetProfileUseCase
    private let getHomeManifestUseCase: GetHomeManifestUseCase

    private var loadTask: Task<Void, Never>?

    init(component: AppComponent) {
        self.getProfileUseCase = component.getProfileUseCase
        self.getHomeManifestUseCase = component.getHomeManifestUseCase
    }

    func load() {
        guard !isLoading else { return }
        isLoading = true
        error = nil
        loadTask = Task {
            let clubId: String
            switch onEnum(of: try? await getProfileUseCase.invoke()) {
            case .ok(let ok)?:
                clubId = (ok.value as? UserProfile)?.homeClub.id ?? ""
            default:
                clubId = ""
            }

            guard !Task.isCancelled else { return }

            switch onEnum(of: try? await getHomeManifestUseCase.invoke(clubId: clubId)) {
            case .ok(let ok)?:
                manifest = ok.value as? HomeManifest
                isLoading = false
            case .err(let err)?:
                error = err.error
                isLoading = false
            case .none:
                isLoading = false
            }
        }
    }

    func cancel() {
        loadTask?.cancel()
    }
}
