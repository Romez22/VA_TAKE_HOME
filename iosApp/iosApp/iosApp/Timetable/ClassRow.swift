import SwiftUI
import Shared

struct ClassRow: View {
    let instance: ClassInstance
    let inFlight: Bool
    let onBook: () -> Void

    private var isCancelled: Bool { instance.status == .cancelled }

    private var rowAlpha: Double { isCancelled ? Self.disabledAlpha : 1 }

    var body: some View {
        HStack(alignment: .center, spacing: VaSpacing.md) {
            VStack(alignment: .leading, spacing: 0) {
                Text(DateTimeDisplay.shared.timeLabel(isoWithOffset: instance.startsAt))
                    .font(.vaBody)
                    .foregroundStyle(VaColor.onSurface)
                Text(DateTimeDisplay.shared.timeLabel(isoWithOffset: instance.endsAt))
                    .font(.vaBody)
                    .foregroundStyle(VaColor.onSurfaceVariant)
            }
            .frame(width: 72, alignment: .leading)

            VStack(alignment: .leading, spacing: 0) {
                Text(instance.title)
                    .font(.vaLabel)
                    .foregroundStyle(VaColor.onSurface)
                Text(instance.trainer)
                    .font(.vaBody)
                    .foregroundStyle(VaColor.onSurfaceVariant)
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            trailing
        }
        .padding(.horizontal, VaSpacing.md)
        .padding(.vertical, VaSpacing.sm)
        .frame(maxWidth: .infinity, alignment: .leading)
        .opacity(rowAlpha)
    }

    @ViewBuilder
    private var trailing: some View {
        if instance.userBookingStatus == .booked || instance.userBookingStatus == .waitlisted {
            BookedBadge(status: instance.userBookingStatus)
        } else if isCancelled || !instance.isActionable() {
            Text(instance.status.label)
                .font(.vaLabel)
                .foregroundStyle(VaColor.onSurfaceVariant)
        } else {
            VStack(alignment: .trailing, spacing: VaSpacing.xs) {
                if inFlight {
                    ProgressView()
                        .progressViewStyle(.circular)
                        .tint(VaColor.vaRed)
                        .frame(width: 24, height: 24)
                } else {
                    Button(instance.status == .full ? "Join Waitlist" : "Book", action: onBook)
                        .buttonStyle(.bordered)
                        .font(.vaLabel)
                }
                Text("\(instance.available) spots")
                    .font(.vaBody)
                    .foregroundStyle(VaColor.onSurfaceVariant)
            }
        }
    }

    private static let disabledAlpha: Double = 0.45
}

private extension ClassStatus {
    var label: String {
        switch self {
        case .cancelled: return "Cancelled"
        case .full: return "Full"
        case .available: return "Available"
        case .unknown: return "Unavailable"
        @unknown default: return "Unavailable"
        }
    }
}
