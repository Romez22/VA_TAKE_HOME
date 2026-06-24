import SwiftUI
import Shared

struct RootView: View {
    let component: AppComponent

    private enum Route {
        case login
        case shell
    }

    @State private var route: Route = .login

    @State private var selectedClassId: String?

    var body: some View {
        Group {
            switch route {
            case .login:
                loginPlaceholder
            case .shell:
                shell
            }
        }
        .task {
            _ = try? await component.authRepository.bootstrap()
            for await state in component.authRepository.authState {
                switch onEnum(of: state) {
                case .authenticated:
                    route = .shell
                case .loggedOut:
                    route = .login
                }
            }
        }
    }


    private var shell: some View {
        NavigationStack {
            TabView {
                homeTab
                    .tabItem { Label("Home", systemImage: "house") }

                classesTab
                    .tabItem { Label("Classes", systemImage: "calendar") }
            }
            .navigationDestination(item: $selectedClassId) { classId in
                ClassDetailScreen(component: component, classId: classId)
            }
        }
    }


    private var loginPlaceholder: some View {
        LoginScreen(component: component) { route = .shell }
    }

    private var homeTab: some View {
        HomeScreen(component: component, onClassClick: { classId in selectedClassId = classId })
    }

    private var classesTab: some View {
        TimetableScreen(component: component)
    }
}
