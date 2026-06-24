import SwiftUI

struct ErrorView: View {
    var heading: String = "Something went wrong"
    var message: String = "We couldn't load this right now. Check your connection and try again."
    let retry: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            Text(heading)
                .font(.vaHeading)
                .foregroundStyle(VaColor.onSurface)
                .multilineTextAlignment(.center)

            Text(message)
                .font(.vaBody)
                .foregroundStyle(VaColor.onSurfaceVariant)
                .multilineTextAlignment(.center)
                .padding(.top, VaSpacing.sm)

            Button("Retry", action: retry)
                .font(.vaLabel)
                .padding(.top, VaSpacing.lg)
        }
        .padding(VaSpacing.xl)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
