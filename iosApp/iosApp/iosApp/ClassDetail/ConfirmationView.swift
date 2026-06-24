import SwiftUI
import Shared

struct ConfirmationView: View {
    let confirmation: Confirmation

    var body: some View {
        switch confirmation.userBookingStatus {
        case .booked:
            if let bookingId = confirmation.bookingId {
                Banner(heading: "Booking Confirmed!", message: "Booking ID: \(bookingId)")
            } else {
                Banner(heading: "You are booked for this class", message: nil)
            }
        case .waitlisted:
            Banner(
                heading: "You're on the waitlist",
                message: confirmation.waitlistPosition.map { "Position \($0)" }
            )
        case .none, .unknown:
            EmptyView()
        @unknown default:
            EmptyView()
        }
    }
}

private struct Banner: View {
    let heading: String
    let message: String?

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(heading)
                .font(.vaHeading)
                .foregroundStyle(VaColor.onSuccessContainer)
            if let message {
                Text(message)
                    .font(.vaBody)
                    .foregroundStyle(VaColor.onSuccessContainer)
                    .padding(.top, VaSpacing.xs)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(VaSpacing.md)
        .background(VaColor.successContainer)
        .clipShape(RoundedRectangle(cornerRadius: VaSpacing.cardCornerRadius))
    }
}
