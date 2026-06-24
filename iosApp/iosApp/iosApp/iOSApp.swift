import SwiftUI
import Shared

@main
struct iOSApp: App {
    private let component: AppComponent

    init() {
        KoinKt.doInitKoin()
        component = AppComponent()
    }

    var body: some Scene {
        WindowGroup {
            RootView(component: component)
        }
    }
}
