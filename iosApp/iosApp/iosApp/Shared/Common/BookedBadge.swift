import SwiftUI
import Shared

struct BookedBadge: View {
    let status: UserBookingStatus

    private var label: String? {
        switch status {
        case .booked: return "Booked"
        case .waitlisted: return "Waitlisted"
        case .none, .unknown: return nil
        @unknown default: return nil
        }
    }

    var body: some View {
        if let label {
            Text(label)
                .font(.vaLabel)
                .foregroundStyle(VaColor.onSuccessContainer)
                .padding(.horizontal, VaSpacing.sm)
                .padding(.vertical, VaSpacing.xs)
                .background(VaColor.successContainer)
                .clipShape(Capsule())
        }
    }
}
