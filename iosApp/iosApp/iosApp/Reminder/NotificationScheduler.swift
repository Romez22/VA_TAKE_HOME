import Foundation
import UserNotifications
import Shared

enum NotificationScheduler {
    private static let leadSeconds: TimeInterval = 2 * 60 * 60

    static func schedule(classId: String, title: String, startsAtIso: String, timeLabel: String) async -> Bool {
        let center = UNUserNotificationCenter.current()
        let granted = (try? await center.requestAuthorization(options: [.alert, .sound])) ?? false
        guard granted else { return false }

        guard let start = KotlinInstant.companion.parseOrNull(input: startsAtIso) else { return false }
        let triggerEpoch = TimeInterval(start.toEpochMilliseconds()) / 1000.0 - leadSeconds
        let interval = triggerEpoch - Date().timeIntervalSince1970
        guard interval > 0 else { return false }

        let content = UNMutableNotificationContent()
        content.title = title
        content.body = "Starts at \(timeLabel)"
        content.sound = .default

        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: interval, repeats: false)
        let request = UNNotificationRequest(identifier: classId, content: content, trigger: trigger)
        do {
            try await center.add(request)
            return true
        } catch {
            return false
        }
    }
}
