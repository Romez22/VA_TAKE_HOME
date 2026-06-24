import SwiftUI
import Shared


struct MutedLine: View {
    let text: String

    init(_ text: String) { self.text = text }

    var body: some View {
        Text(text)
            .font(.vaBody)
            .foregroundStyle(VaColor.onSurfaceVariant)
            .padding(.top, VaSpacing.xs)
    }
}

struct ImageRowCard: View {
    let assetName: String
    let title: String
    let subtitle: String?

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Image(assetName)
                .resizable()
                .scaledToFill()
                .frame(width: 48, height: 48)
                .clipShape(RoundedRectangle(cornerRadius: VaSpacing.sm))

            Text(title)
                .font(.vaLabel)
                .foregroundStyle(VaColor.onSurface)
                .padding(.top, VaSpacing.sm)

            if let subtitle = subtitle { MutedLine(subtitle) }
        }
        .frame(width: 160, alignment: .leading)
        .padding(VaSpacing.md)
        .background(VaColor.surfaceVariant)
        .clipShape(RoundedRectangle(cornerRadius: VaSpacing.cardCornerRadius))
    }
}

struct SectionWithImageRow: View {
    let title: String
    let items: [ImageRowEntry]

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(title)
                .font(.vaLabel)
                .foregroundStyle(VaColor.onSurface)
                .padding(.horizontal, VaSpacing.md)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(alignment: .top, spacing: VaSpacing.md) {
                    ForEach(items) { item in
                        ImageRowCard(assetName: item.assetName, title: item.title, subtitle: item.subtitle)
                    }
                }
                .padding(.horizontal, VaSpacing.md)
            }
            .padding(.top, VaSpacing.sm)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

struct ImageRowEntry: Identifiable {
    let id: String
    let assetName: String
    let title: String
    let subtitle: String?
}
