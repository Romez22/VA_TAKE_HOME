import Foundation
import Combine
import Shared

struct Confirmation {
    let bookingId: String?
    let userBookingStatus: UserBookingStatus
    let waitlistPosition: Int?
}

@MainActor
final class ClassDetailViewModel: ObservableObject {
    @Published var isLoading: Bool = false
    @Published var error: DomainError?
    @Published var classInstance: ClassInstance?
    @Published var clubId: String?
    @Published var confirmation: Confirmation?
    @Published var actionInFlight: Bool = false
    @Published var actionError: DomainError?
    @Published var actionErrorMessage: String?

    private let getProfileUseCase: GetProfileUseCase
    private let getTimetableUseCase: GetTimetableUseCase
    private let bookClassUseCase: BookClassUseCase
    private let cancelBookingUseCase: CancelBookingUseCase

    private var loadTask: Task<Void, Never>?
    private var actionTask: Task<Void, Never>?

    init(component: AppComponent) {
        self.getProfileUseCase = component.getProfileUseCase
        self.getTimetableUseCase = component.getTimetableUseCase
        self.bookClassUseCase = component.bookClassUseCase
        self.cancelBookingUseCase = component.cancelBookingUseCase
    }


    func load(classId: String) {
        guard !isLoading else { return }
        isLoading = true
        error = nil
        loadTask = Task {
            let resolvedClubId: String
            switch onEnum(of: try? await getProfileUseCase.invoke()) {
            case .ok(let ok)?:
                resolvedClubId = (ok.value as? UserProfile)?.homeClub.id ?? ""
            default:
                resolvedClubId = ""
            }

            guard !Task.isCancelled else { return }

            switch onEnum(of: try? await getTimetableUseCase.invoke(clubId: resolvedClubId, date: Self.today())) {
            case .ok(let ok)?:
                let week = ok.value as? WeeklyTimetable
                let instance = week?.days
                    .flatMap { $0.classes }
                    .first { $0.classId == classId }
                if let instance {
                    classInstance = instance
                    clubId = resolvedClubId
                    confirmation = Confirmation(
                        bookingId: nil,
                        userBookingStatus: instance.userBookingStatus,
                        waitlistPosition: nil
                    )
                    isLoading = false
                } else {
                    error = DomainErrorNotFound(code: "ClassNotFound")
                    clubId = resolvedClubId
                    isLoading = false
                }
            case .err(let err)?:
                error = err.error
                isLoading = false
            case .none:
                isLoading = false
            }
        }
    }


    func book() {
        guard let instance = classInstance, let clubId, !actionInFlight else { return }
        actionInFlight = true
        actionError = nil
        actionTask = Task {
            switch onEnum(of: try? await bookClassUseCase.invoke(clubId: clubId, classId: instance.classId)) {
            case .ok(let ok)?:
                if let outcome = ok.value as? BookingOutcome {
                    confirmation = Self.toConfirmation(outcome)
                }
                actionInFlight = false
            case .err(let err)?:
                actionError = err.error
                actionErrorMessage = "We couldn't complete that. Tap to try again."
                actionInFlight = false
            case .none:
                actionInFlight = false
            }
        }
    }


    func cancel() {
        guard let instance = classInstance, let clubId, !actionInFlight else { return }
        actionInFlight = true
        actionError = nil
        actionTask = Task {
            switch onEnum(of: try? await cancelBookingUseCase.invoke(clubId: clubId, classId: instance.classId)) {
            case .ok?:
                confirmation = Confirmation(
                    bookingId: nil,
                    userBookingStatus: .none,
                    waitlistPosition: nil
                )
                actionInFlight = false
            case .err(let err)?:
                actionError = err.error
                actionErrorMessage = "We couldn't complete that. Tap to try again."
                actionInFlight = false
            case .none:
                actionInFlight = false
            }
        }
    }

    func actionErrorShown() {
        actionError = nil
        actionErrorMessage = nil
    }


    func isWithinForfeitWindow() -> Bool {
        guard let startsAt = classInstance?.startsAt else { return false }
        return ForfeitPolicy.shared.isWithinForfeitWindow(startsAtIso: startsAt, now: Self.nowInstant())
    }


    func cancelTasks() {
        loadTask?.cancel()
        actionTask?.cancel()
    }


    private static func today() -> String {
        let now = Calendar.current.dateComponents([.year, .month, .day], from: Date())
        let year = now.year ?? 1970
        let month = now.month ?? 1
        let day = now.day ?? 1
        return String(format: "%04d-%02d-%02d", year, month, day)
    }

    private static func nowInstant() -> KotlinInstant {
        let millis = Int64((Date().timeIntervalSince1970 * 1000).rounded())
        return KotlinInstant.companion.fromEpochMilliseconds(epochMilliseconds: millis)
    }


    private static func toConfirmation(_ outcome: BookingOutcome) -> Confirmation {
        switch onEnum(of: outcome) {
        case .confirmed(let confirmed):
            return Confirmation(
                bookingId: confirmed.booking.bookingId,
                userBookingStatus: .booked,
                waitlistPosition: nil
            )
        case .waitlisted(let waitlisted):
            return Confirmation(
                bookingId: waitlisted.booking.bookingId,
                userBookingStatus: .waitlisted,
                waitlistPosition: waitlisted.position?.intValue
            )
        case .alreadyBooked(let already):
            return Confirmation(
                bookingId: nil,
                userBookingStatus: already.wasWaitlist ? .waitlisted : .booked,
                waitlistPosition: nil
            )
        }
    }
}
