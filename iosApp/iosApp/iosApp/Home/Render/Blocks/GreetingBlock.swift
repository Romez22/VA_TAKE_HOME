import SwiftUI
import Shared

struct GreetingBlock: View {
    let block: ManifestBlockGreeting

    var body: some View {
        Text(block.title)
            .font(.vaHeading)
            .foregroundStyle(VaColor.onSurface)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, VaSpacing.md)
    }
}
