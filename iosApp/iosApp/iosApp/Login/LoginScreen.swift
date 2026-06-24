import SwiftUI
import Shared

struct LoginScreen: View {
    @StateObject private var vm: LoginViewModel
    private let onLoggedIn: () -> Void
    @State private var passwordVisible: Bool = false

    init(component: AppComponent, onLoggedIn: @escaping () -> Void) {
        _vm = StateObject(wrappedValue: LoginViewModel(component: component))
        self.onLoggedIn = onLoggedIn
    }

    var body: some View {
        VStack(spacing: 0) {
            Text("Virgin Active")
                .font(.vaDisplay)
                .foregroundStyle(VaColor.vaRed)

            Text("Sign in to your account")
                .font(.vaBody)
                .foregroundStyle(VaColor.onSurfaceVariant)
                .padding(.top, VaSpacing.sm)

            TextField("Email", text: $vm.email)
                .font(.vaBody)
                .textFieldStyle(.roundedBorder)
                .textInputAutocapitalization(.never)
                .keyboardType(.emailAddress)
                .autocorrectionDisabled(true)
                .disabled(vm.isLoading)
                .padding(.top, VaSpacing.xl)

            HStack(spacing: VaSpacing.sm) {
                Group {
                    if passwordVisible {
                        TextField("Password", text: $vm.password)
                    } else {
                        SecureField("Password", text: $vm.password)
                    }
                }
                .font(.vaBody)
                .textFieldStyle(.roundedBorder)
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled(true)

                Button(passwordVisible ? "Hide" : "Show") {
                    passwordVisible.toggle()
                }
                .font(.vaLabel)
                .foregroundStyle(VaColor.vaRed)
            }
            .disabled(vm.isLoading)
            .padding(.top, VaSpacing.md)

            if let error = vm.error {
                Text(DomainErrorCopy.loginMessage(error))
                    .font(.vaBody)
                    .foregroundStyle(VaColor.errorRed)
                    .multilineTextAlignment(.center)
                    .frame(maxWidth: .infinity)
                    .padding(.top, VaSpacing.md)
            }

            Button {
                vm.submit(onLoggedIn: onLoggedIn)
            } label: {
                ZStack {
                    if vm.isLoading {
                        ProgressView()
                            .progressViewStyle(.circular)
                            .tint(VaColor.white)
                    } else {
                        Text("Sign In")
                            .font(.vaLabel)
                            .foregroundStyle(VaColor.white)
                    }
                }
                .frame(maxWidth: .infinity)
                .frame(height: VaSpacing.minTouchTarget)
                .background(VaColor.vaRed)
                .clipShape(RoundedCornerShape())
            }
            .disabled(vm.isLoading)
            .padding(.top, VaSpacing.xl)
        }
        .padding(.horizontal, VaSpacing.lg)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .onDisappear { vm.cancel() }
    }
}

private struct RoundedCornerShape: Shape {
    func path(in rect: CGRect) -> Path {
        Path(roundedRect: rect, cornerRadius: VaSpacing.cardCornerRadius)
    }
}
