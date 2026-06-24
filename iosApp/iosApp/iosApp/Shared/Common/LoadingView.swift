import SwiftUI

struct LoadingView: View {
    var body: some View {
        ProgressView()
            .progressViewStyle(.circular)
            .tint(VaColor.vaRed)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
