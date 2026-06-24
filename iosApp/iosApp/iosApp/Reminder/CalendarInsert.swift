import SwiftUI
import EventKit
import EventKitUI
import Shared

enum CalendarInsert {
    static func requestAccess(store: EKEventStore) async -> Bool {
        (try? await store.requestWriteOnlyAccessToEvents()) ?? false
    }

    static func makeEvent(
        store: EKEventStore,
        title: String,
        startsAtIso: String,
        endsAtIso: String,
        location: String
    ) -> EKEvent? {
        guard let start = KotlinInstant.companion.parseOrNull(input: startsAtIso) else { return nil }
        let end = KotlinInstant.companion.parseOrNull(input: endsAtIso)

        let event = EKEvent(eventStore: store)
        event.title = title
        event.location = location
        event.startDate = Date(timeIntervalSince1970: TimeInterval(start.toEpochMilliseconds()) / 1000.0)
        let endMillis = end?.toEpochMilliseconds() ?? start.toEpochMilliseconds()
        event.endDate = Date(timeIntervalSince1970: TimeInterval(endMillis) / 1000.0)
        return event
    }
}

struct CalendarEditView: UIViewControllerRepresentable {
    let store: EKEventStore
    let event: EKEvent
    let onComplete: () -> Void

    func makeCoordinator() -> Coordinator { Coordinator(onComplete: onComplete) }

    func makeUIViewController(context: Context) -> EKEventEditViewController {
        let controller = EKEventEditViewController()
        controller.eventStore = store
        controller.event = event
        controller.editViewDelegate = context.coordinator
        return controller
    }

    func updateUIViewController(_ controller: EKEventEditViewController, context: Context) {}

    final class Coordinator: NSObject, EKEventEditViewDelegate {
        let onComplete: () -> Void
        init(onComplete: @escaping () -> Void) { self.onComplete = onComplete }

        func eventEditViewController(
            _ controller: EKEventEditViewController,
            didCompleteWith action: EKEventEditViewAction
        ) {
            onComplete()
        }
    }
}
