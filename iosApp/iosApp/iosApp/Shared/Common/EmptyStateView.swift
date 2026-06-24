import SwiftUI

struct EmptyStateView: View {
    let heading: String

    var body: some View {
        Text(heading)
            .font(.vaHeading)
            .foregroundStyle(VaColor.onSurface)
            .multilineTextAlignment(.center)
            .padding(VaSpacing.xl)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
