import SwiftUI
import EventKit
import Shared

struct ReminderSheet: View {
    let classInstance: ClassInstance
    let onMessage: (_ message: String, _ offerSettings: Bool) -> Void
    let onScheduled: () -> Void

    @State private var eventStore = EKEventStore()
    @State private var pendingEvent: EKEvent?

    private var timeLabel: String {
        DateTimeDisplay.shared.timeLabel(isoWithOffset: classInstance.startsAt)
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text("Set a reminder")
                .font(.vaHeading)
                .foregroundStyle(VaColor.onSurface)
                .padding(.bottom, VaSpacing.sm)

            Text(timeLabel)
                .font(.vaBody)
                .foregroundStyle(VaColor.onSurfaceVariant)
                .padding(.bottom, VaSpacing.sm)

            SheetOption(label: "Notification reminder", action: onNotificationReminder)
            SheetOption(label: "Add to calendar", action: onAddToCalendar)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.horizontal, VaSpacing.md)
        .padding(.vertical, VaSpacing.md)
        .presentationDetents([.height(220)])
        .sheet(item: $pendingEvent) { event in
            CalendarEditView(store: eventStore, event: event) {
                pendingEvent = nil
                onScheduled()
            }
        }
    }


    private func onNotificationReminder() {
        Task {
            let scheduled = await NotificationScheduler.schedule(
                classId: classInstance.classId,
                title: classInstance.title,
                startsAtIso: classInstance.startsAt,
                timeLabel: timeLabel
            )
            if scheduled {
                onScheduled()
            } else {
                onMessage("Reminder needs notification permission", true)
            }
        }
    }

    private func onAddToCalendar() {
        Task {
            let granted = await CalendarInsert.requestAccess(store: eventStore)
            guard granted else {
                onMessage("Calendar access was denied", true)
                return
            }
            guard let event = CalendarInsert.makeEvent(
                store: eventStore,
                title: classInstance.title,
                startsAtIso: classInstance.startsAt,
                endsAtIso: classInstance.endsAt,
                location: classInstance.clubId
            ) else {
                onMessage("Calendar access was denied", true)
                return
            }
            pendingEvent = event
        }
    }
}

private struct SheetOption: View {
    let label: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(label)
                .font(.vaBody)
                .foregroundStyle(VaColor.onSurface)
                .frame(maxWidth: .infinity, minHeight: VaSpacing.minTouchTarget, alignment: .leading)
        }
    }
}

extension EKEvent: @retroactive Identifiable {
    public var id: ObjectIdentifier { ObjectIdentifier(self) }
}
