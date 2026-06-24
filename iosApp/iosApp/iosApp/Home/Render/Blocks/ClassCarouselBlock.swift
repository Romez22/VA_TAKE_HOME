import SwiftUI
import Shared

struct ClassCarouselBlock: View {
    let block: ManifestBlockClassCarousel
    let onClassClick: (String) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(block.title)
                .font(.vaLabel)
                .foregroundStyle(VaColor.onSurface)
                .padding(.horizontal, VaSpacing.md)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(alignment: .top, spacing: VaSpacing.md) {
                    ForEach(block.items, id: \.id) { item in
                        ClassCard(item: item) { onClassClick(item.id) }
                    }
                }
                .padding(.horizontal, VaSpacing.md)
            }
            .padding(.top, VaSpacing.sm)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

private struct ClassCard: View {
    let item: ManifestBlockClassCarousel.Item
    let onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            VStack(alignment: .leading, spacing: 0) {
                Image(item.image.assetName)
                    .resizable()
                    .scaledToFill()
                    .frame(width: 200, height: 96)
                    .clipped()

                VStack(alignment: .leading, spacing: 0) {
                    Text(item.title)
                        .font(.vaLabel)
                        .foregroundStyle(VaColor.onSurface)

                    if let subtitle = item.subtitle { MutedLine(subtitle) }

                    Text("\(DateTimeDisplay.shared.timeLabel(isoWithOffset: item.startsAt)) · \(DateTimeDisplay.shared.dateLabel(isoWithOffset: item.startsAt))")
                        .font(.vaBody)
                        .foregroundStyle(VaColor.onSurfaceVariant)
                        .padding(.top, VaSpacing.xs)
                }
                .padding(VaSpacing.md)
            }
            .frame(width: 200, alignment: .leading)
            .background(VaColor.surfaceVariant)
            .clipShape(RoundedRectangle(cornerRadius: VaSpacing.cardCornerRadius))
        }
        .buttonStyle(.plain)
    }
}
