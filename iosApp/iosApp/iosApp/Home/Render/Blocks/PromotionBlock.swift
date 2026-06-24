import SwiftUI
import Shared

struct PromotionBlock: View {
    let block: ManifestBlockPromotion

    var body: some View {
        HStack(alignment: .center, spacing: VaSpacing.md) {
            Image(block.image.assetName)
                .resizable()
                .scaledToFill()
                .frame(width: 56, height: 56)
                .clipShape(RoundedRectangle(cornerRadius: VaSpacing.sm))

            VStack(alignment: .leading, spacing: 0) {
                Text(block.title)
                    .font(.vaLabel)
                    .foregroundStyle(VaColor.onSurface)

                if let subtitle = block.subtitle { MutedLine(subtitle) }
            }

            Spacer(minLength: 0)
        }
        .padding(VaSpacing.md)
        .background(VaColor.surfaceVariant)
        .clipShape(RoundedRectangle(cornerRadius: VaSpacing.cardCornerRadius))
        .padding(.horizontal, VaSpacing.md)
    }
}
