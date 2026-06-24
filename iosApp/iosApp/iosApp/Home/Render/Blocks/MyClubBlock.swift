import SwiftUI
import Shared

struct MyClubBlock: View {
    let block: ManifestBlockMyClub

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(block.name)
                .font(.vaLabel)
                .foregroundStyle(VaColor.onSurface)

            if let addressLine = block.addressLine { MutedLine(addressLine) }
            if let openingHoursToday = block.openingHoursToday { MutedLine(openingHoursToday) }
            if let phoneNumber = block.phoneNumber { MutedLine(phoneNumber) }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(VaSpacing.md)
        .background(VaColor.surfaceVariant)
        .clipShape(RoundedRectangle(cornerRadius: VaSpacing.cardCornerRadius))
        .padding(.horizontal, VaSpacing.md)
    }
}
