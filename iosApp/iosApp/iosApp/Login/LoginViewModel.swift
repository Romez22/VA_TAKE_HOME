import Foundation
import Combine
import Shared

@MainActor
final class LoginViewModel: ObservableObject {
    @Published var email: String = ""
    @Published var password: String = ""
    @Published var isLoading: Bool = false
    @Published var error: DomainError?

    private let loginUseCase: LoginUseCase

    private var submitTask: Task<Void, Never>?

    init(component: AppComponent) {
        self.loginUseCase = component.loginUseCase
    }

    func submit(onLoggedIn: @escaping () -> Void) {
        guard !isLoading else { return }
        isLoading = true
        error = nil
        submitTask = Task { [email, password] in
            switch onEnum(of: try? await loginUseCase.invoke(username: email, password: password)) {
            case .ok?:
                isLoading = false
                onLoggedIn()
            case .err(let err)?:
                error = err.error
                isLoading = false
            case .none:
                isLoading = false
            }
        }
    }

    func cancel() {
        submitTask?.cancel()
    }
}
