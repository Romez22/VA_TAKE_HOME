import SwiftUI
import Shared

struct HeroBlock: View {
    let block: ManifestBlockHero

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(block.title)
                .font(.vaHeading)
                .foregroundStyle(VaColor.white)

            if let subtitle = block.subtitle {
                Text(subtitle)
                    .font(.vaBody)
                    .foregroundStyle(VaColor.white)
                    .padding(.top, VaSpacing.xs)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(VaSpacing.md)
        .background(VaColor.vaRed)
        .clipShape(RoundedRectangle(cornerRadius: VaSpacing.cardCornerRadius))
        .padding(.horizontal, VaSpacing.md)
    }
}
