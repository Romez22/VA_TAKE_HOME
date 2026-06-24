import SwiftUI
import Shared

struct ClassDetailScreen: View {
    let classId: String

    @StateObject private var vm: ClassDetailViewModel

    init(component: AppComponent, classId: String) {
        self.classId = classId
        _vm = StateObject(wrappedValue: ClassDetailViewModel(component: component))
    }

    var body: some View {
        content
            .navigationTitle("Class Details")
            .navigationBarTitleDisplayMode(.inline)
            .task { vm.load(classId: classId) }
            .onDisappear { vm.cancelTasks() }
    }

    @ViewBuilder
    private var content: some View {
        if vm.isLoading {
            LoadingView()
        } else if vm.error != nil {
            ErrorView(retry: { vm.load(classId: classId) })
        } else if let instance = vm.classInstance {
            ClassDetailBody(
                instance: instance,
                confirmation: vm.confirmation,
                actionInFlight: vm.actionInFlight,
                withinForfeitWindow: vm.isWithinForfeitWindow(),
                actionErrorMessage: vm.actionErrorMessage,
                onBook: vm.book,
                onCancel: vm.cancel,
                onActionErrorShown: vm.actionErrorShown
            )
        } else {
            EmptyStateView(heading: "Nothing to show yet")
        }
    }
}

private struct ClassDetailBody: View {
    let instance: ClassInstance
    let confirmation: Confirmation?
    let actionInFlight: Bool
    let withinForfeitWindow: Bool
    let actionErrorMessage: String?
    let onBook: () -> Void
    let onCancel: () -> Void
    let onActionErrorShown: () -> Void

    @State private var showReminderSheet = false
    @State private var showCancelAlert = false
    @State private var toastMessage: String?

    private var isBooked: Bool {
        confirmation?.userBookingStatus == .booked || confirmation?.userBookingStatus == .waitlisted
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                TypeChip(type: instance.type)

                Text(instance.title)
                    .font(.vaDisplay)
                    .foregroundStyle(VaColor.onSurface)
                    .padding(.top, VaSpacing.sm)

                Text("with \(instance.trainer)")
                    .font(.vaBody)
                    .foregroundStyle(VaColor.onSurfaceVariant)
                    .padding(.top, VaSpacing.xs)

                InfoCard(instance: instance)
                    .padding(.top, VaSpacing.md)

                if let confirmation {
                    ConfirmationView(confirmation: confirmation)
                        .padding(.top, VaSpacing.md)
                }

                if withinForfeitWindow && isBooked {
                    ForfeitStrip()
                        .padding(.top, VaSpacing.md)
                }

                PrimaryAction(status: instance.status, actionInFlight: actionInFlight, onBook: onBook)
                    .padding(.top, VaSpacing.lg)

                if isBooked {
                    Button(action: { showReminderSheet = true }) {
                        Text("Set Reminder")
                            .font(.vaLabel)
                            .foregroundStyle(VaColor.onSurface)
                            .frame(maxWidth: .infinity, minHeight: VaSpacing.minTouchTarget)
                            .background(VaColor.surfaceVariant)
                            .clipShape(RoundedRectangle(cornerRadius: VaSpacing.cardCornerRadius))
                    }
                    .padding(.top, VaSpacing.sm)

                    Button(role: .destructive, action: { showCancelAlert = true }) {
                        Text("Cancel Booking")
                            .font(.vaLabel)
                            .foregroundStyle(VaColor.errorRed)
                            .frame(maxWidth: .infinity, minHeight: VaSpacing.minTouchTarget)
                    }
                    .padding(.top, VaSpacing.sm)
                }
            }
            .padding(.horizontal, VaSpacing.md)
            .padding(.vertical, VaSpacing.md)
        }
        .toast($toastMessage)
        .onChange(of: actionErrorMessage) { _, newValue in
            guard let newValue else { return }
            toastMessage = newValue
            onActionErrorShown()
        }
        .alert("Cancel this booking?", isPresented: $showCancelAlert) {
            Button("Cancel Booking", role: .destructive, action: onCancel)
            Button("Keep Booking", role: .cancel) {}
        } message: {
            Text(
                withinForfeitWindow
                    ? "This class starts within 12 hours, so cancelling now incurs a forfeit."
                    : "You can rebook later if spots are available."
            )
        }
        .sheet(isPresented: $showReminderSheet) {
            ReminderSheet(classInstance: instance) { message, _ in
                toastMessage = message
                showReminderSheet = false
            } onScheduled: {
                showReminderSheet = false
            }
        }
    }
}

private struct TypeChip: View {
    let type: ClassType

    private var label: String {
        switch type {
        case .groupWorkout: return "GROUP WORKOUT"
        case .yoga: return "YOGA"
        case .spin: return "SPIN"
        case .pilates: return "PILATES"
        case .hiit: return "HIIT"
        case .swimming: return "SWIMMING"
        case .unknown: return "CLASS"
        @unknown default: return "CLASS"
        }
    }

    var body: some View {
        Text(label)
            .font(.vaLabel)
            .foregroundStyle(VaColor.white)
            .padding(.horizontal, VaSpacing.sm)
            .padding(.vertical, VaSpacing.xs)
            .background(VaColor.vaRed)
            .clipShape(Capsule())
    }
}

private struct InfoCard: View {
    let instance: ClassInstance

    private var timeValue: String {
        let display = DateTimeDisplay.shared
        let offset = display.offsetLabel(isoWithOffset: instance.startsAt)
        var value = "\(display.timeLabel(isoWithOffset: instance.startsAt)) – \(display.timeLabel(isoWithOffset: instance.endsAt))"
        if !offset.isEmpty { value += " (\(offset))" }
        return value
    }

    var body: some View {
        VStack(spacing: 0) {
            InfoRow(label: "Date", value: DateTimeDisplay.shared.dateLabel(isoWithOffset: instance.startsAt))
            InfoRow(label: "Time", value: timeValue)
            InfoRow(label: "Availability", value: "\(instance.available) of \(instance.spots) spots")
        }
        .frame(maxWidth: .infinity)
        .padding(VaSpacing.md)
        .background(VaColor.surfaceVariant)
        .clipShape(RoundedRectangle(cornerRadius: VaSpacing.cardCornerRadius))
    }
}

private struct InfoRow: View {
    let label: String
    let value: String

    var body: some View {
        HStack {
            Text(label)
                .font(.vaBody)
                .foregroundStyle(VaColor.onSurfaceVariant)
            Spacer()
            Text(value)
                .font(.vaLabel)
                .foregroundStyle(VaColor.onSurface)
                .multilineTextAlignment(.trailing)
        }
        .padding(.vertical, VaSpacing.xs)
    }
}

private struct ForfeitStrip: View {
    var body: some View {
        Text("Cancelling within 12 hours of the class incurs a forfeit.")
            .font(.vaBody)
            .foregroundStyle(VaColor.onWarningContainer)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(VaSpacing.md)
            .background(VaColor.warningContainer)
            .clipShape(RoundedRectangle(cornerRadius: VaSpacing.cardCornerRadius))
            .overlay(
                RoundedRectangle(cornerRadius: VaSpacing.cardCornerRadius)
                    .stroke(VaColor.warningBorder, lineWidth: 2)
            )
    }
}

private struct PrimaryAction: View {
    let status: ClassStatus
    let actionInFlight: Bool
    let onBook: () -> Void

    private var label: String { status == .full ? "Join Waitlist" : "Book Class" }

    var body: some View {
        Button(action: onBook) {
            ZStack {
                if actionInFlight {
                    ProgressView()
                        .progressViewStyle(.circular)
                        .tint(VaColor.white)
                } else {
                    Text(label).font(.vaLabel)
                }
            }
            .foregroundStyle(VaColor.white)
            .frame(maxWidth: .infinity, minHeight: VaSpacing.minTouchTarget)
            .background(status == .cancelled ? VaColor.vaRed.opacity(0.4) : VaColor.vaRed)
            .clipShape(RoundedRectangle(cornerRadius: VaSpacing.cardCornerRadius))
        }
        .disabled(actionInFlight || status == .cancelled)
    }
}
