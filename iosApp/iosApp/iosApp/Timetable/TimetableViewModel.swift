import Foundation
import Combine
import Shared

enum DayTab: String, CaseIterable, Hashable {
    case all = "All"
    case mon = "Mon"
    case tue = "Tue"
    case wed = "Wed"
    case thu = "Thu"
    case fri = "Fri"
    case sat = "Sat"
    case sun = "Sun"

    var label: String { rawValue }
}

@MainActor
final class TimetableViewModel: ObservableObject {
    @Published var isLoading: Bool = false
    @Published var error: DomainError?
    @Published var week: WeeklyTimetable?
    @Published var selectedTab: DayTab = .all
    @Published var rowActionInFlight: Set<String> = []
    @Published var dateNotice: String?
    @Published var snackbar: String?

    private let getProfileUseCase: GetProfileUseCase
    private let getTimetableUseCase: GetTimetableUseCase
    private let bookClassUseCase: BookClassUseCase

    private var loadTask: Task<Void, Never>?
    private var bookTasks: [String: Task<Void, Never>] = [:]

    init(component: AppComponent) {
        self.getProfileUseCase = component.getProfileUseCase
        self.getTimetableUseCase = component.getTimetableUseCase
        self.bookClassUseCase = component.bookClassUseCase
    }

    private static let dateNoticeCopy =
        "That date wasn't available, so we're showing this week's classes."


    func load() {
        guard !isLoading else { return }
        isLoading = true
        error = nil
        dateNotice = nil
        loadTask = Task {
            let clubId: String
            switch onEnum(of: try? await getProfileUseCase.invoke()) {
            case .ok(let ok)?:
                clubId = (ok.value as? UserProfile)?.homeClub.id ?? ""
            default:
                clubId = ""
            }

            guard !Task.isCancelled else { return }

            let today = Self.today()
            switch onEnum(of: try? await getTimetableUseCase.invoke(clubId: clubId, date: today)) {
            case .ok(let ok)?:
                week = ok.value as? WeeklyTimetable
                isLoading = false
            case .err(let err)?:
                await handleLoadError(clubId: clubId, today: today, error: err.error)
            case .none:
                isLoading = false
            }
        }
    }

    private func handleLoadError(clubId: String, today: String, error loadError: DomainError) async {
        switch onEnum(of: loadError) {
        case .validation:
            guard !Task.isCancelled else { return }
            switch onEnum(of: try? await getTimetableUseCase.invoke(clubId: clubId, date: today)) {
            case .ok(let ok)?:
                week = ok.value as? WeeklyTimetable
                dateNotice = Self.dateNoticeCopy
                isLoading = false
            case .err(let err)?:
                error = err.error
                dateNotice = Self.dateNoticeCopy
                isLoading = false
            case .none:
                isLoading = false
            }
        default:
            error = loadError
            isLoading = false
        }
    }


    func selectTab(_ tab: DayTab) {
        selectedTab = tab
    }


    func book(clubId: String, classId: String) {
        guard !rowActionInFlight.contains(classId) else { return }
        rowActionInFlight.insert(classId)
        bookTasks[classId] = Task {
            defer {
                rowActionInFlight.remove(classId)
                bookTasks[classId] = nil
            }
            switch onEnum(of: try? await bookClassUseCase.invoke(clubId: clubId, classId: classId)) {
            case .ok(let ok)?:
                guard let outcome = ok.value as? BookingOutcome else { break }
                week = week?.flippingRow(classId: classId, to: Self.bookingStatus(for: outcome))
                snackbar = Self.successMessage(for: outcome)
            case .err?:
                snackbar = "We couldn't complete that. Tap the class to try again."
            case .none:
                break
            }
        }
    }


    func cancel() {
        loadTask?.cancel()
        for task in bookTasks.values { task.cancel() }
    }


    private static func today() -> String {
        let now = Calendar.current.dateComponents([.year, .month, .day], from: Date())
        let year = now.year ?? 1970
        let month = now.month ?? 1
        let day = now.day ?? 1
        let y = String(format: "%04d", year)
        let m = String(format: "%02d", month)
        let d = String(format: "%02d", day)
        return "\(y)-\(m)-\(d)"
    }


    private static func bookingStatus(for outcome: BookingOutcome) -> UserBookingStatus {
        switch onEnum(of: outcome) {
        case .confirmed:
            return .booked
        case .waitlisted:
            return .waitlisted
        case .alreadyBooked(let already):
            return already.wasWaitlist ? .waitlisted : .booked
        }
    }

    private static func successMessage(for outcome: BookingOutcome) -> String {
        switch onEnum(of: outcome) {
        case .confirmed:
            return "Booked! See you there."
        case .waitlisted(let waitlisted):
            if let position = waitlisted.position?.intValue {
                return "You're #\(position) on the waitlist."
            }
            return "You're on the waitlist."
        case .alreadyBooked(let already):
            return already.wasWaitlist ? "You're already on the waitlist." : "You're already booked."
        }
    }
}


extension WeeklyTimetable {
    func daysFor(_ tab: DayTab) -> [TimetableDay] {
        guard tab != .all else { return days }
        return days.filter { $0.date.weekdayTab() == tab }
    }

    func flippingRow(classId: String, to newStatus: UserBookingStatus) -> WeeklyTimetable {
        let newDays = days.map { day -> TimetableDay in
            guard day.classes.contains(where: { $0.classId == classId }) else { return day }
            let newClasses = day.classes.map { instance -> ClassInstance in
                guard instance.classId == classId else { return instance }
                return instance.doCopy(
                    classId: instance.classId,
                    clubId: instance.clubId,
                    title: instance.title,
                    trainer: instance.trainer,
                    type: instance.type,
                    startsAt: instance.startsAt,
                    endsAt: instance.endsAt,
                    spots: instance.spots,
                    available: instance.available,
                    waitlistCount: instance.waitlistCount,
                    status: instance.status,
                    userBookingStatus: newStatus
                )
            }
            return day.doCopy(date: day.date, classes: newClasses)
        }
        return doCopy(weekStart: weekStart, weekEnd: weekEnd, selectedDate: selectedDate, days: newDays)
    }
}

extension ClassInstance {
    func isActionable() -> Bool {
        userBookingStatus != .booked &&
            userBookingStatus != .waitlisted &&
            status != .cancelled
    }
}

extension String {
    fileprivate func weekdayTab() -> DayTab? {
        let datePart = self.split(separator: "T", maxSplits: 1).first.map(String.init) ?? self
        let parts = datePart.split(separator: "-", omittingEmptySubsequences: false).map(String.init)
        guard parts.count >= 3,
              let y = Int(parts[0]),
              let m = Int(parts[1]),
              let d = Int(parts[2]),
              m >= 1, m <= 12 else { return nil }
        let t = [0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4]
        let yy = m < 3 ? y - 1 : y
        let dow = (yy + yy / 4 - yy / 100 + yy / 400 + t[m - 1] + d) % 7
        switch dow {
        case 0: return .sun
        case 1: return .mon
        case 2: return .tue
        case 3: return .wed
        case 4: return .thu
        case 5: return .fri
        case 6: return .sat
        default: return nil
        }
    }
}
