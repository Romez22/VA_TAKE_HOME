import SwiftUI
import Shared

struct HomeScreen: View {
    @StateObject private var vm: HomeViewModel
    private let onClassClick: (String) -> Void

    init(component: AppComponent, onClassClick: @escaping (String) -> Void) {
        _vm = StateObject(wrappedValue: HomeViewModel(component: component))
        self.onClassClick = onClassClick
    }

    var body: some View {
        content
            .task { vm.load() }
            .onDisappear { vm.cancel() }
    }

    @ViewBuilder
    private var content: some View {
        if vm.isLoading {
            LoadingView()
        } else if vm.error != nil {
            ErrorView(retry: { vm.load() })
        } else if vm.manifest == nil || vm.manifest?.blocks.isEmpty == true {
            EmptyStateView(heading: "Nothing to show yet")
        } else if let blocks = vm.manifest?.blocks {
            ScrollView {
                LazyVStack(alignment: .leading, spacing: VaSpacing.lg) {
                    ForEach(blocks.indices, id: \.self) { index in
                        ManifestBlockView(blocks[index], onClassClick: onClassClick)
                    }
                }
                .padding(.vertical, VaSpacing.md)
            }
        }
    }
}
