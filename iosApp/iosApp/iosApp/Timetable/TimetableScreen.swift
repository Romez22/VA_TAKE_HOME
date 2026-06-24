import SwiftUI
import Shared

struct TimetableScreen: View {
    @StateObject private var vm: TimetableViewModel

    init(component: AppComponent) {
        _vm = StateObject(wrappedValue: TimetableViewModel(component: component))
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            header
            DayTabs(selected: vm.selectedTab, onSelect: vm.selectTab)

            if let notice = vm.dateNotice {
                dateNotice(notice)
            }

            content
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .toast($vm.snackbar)
        .task { vm.load() }
        .onDisappear { vm.cancel() }
    }

    private var header: some View {
        Text("Classes This Week")
            .font(.vaDisplay)
            .foregroundStyle(VaColor.onSurface)
            .padding(.horizontal, VaSpacing.md)
            .padding(.top, VaSpacing.lg)
            .padding(.bottom, VaSpacing.sm)
    }

    private func dateNotice(_ text: String) -> some View {
        Text(text)
            .font(.vaBody)
            .foregroundStyle(VaColor.onSurfaceVariant)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, VaSpacing.md)
            .padding(.vertical, VaSpacing.sm)
            .background(VaColor.surfaceVariant)
    }

    @ViewBuilder
    private var content: some View {
        if vm.isLoading {
            LoadingView()
        } else if vm.error != nil {
            ErrorView(retry: { vm.load() })
        } else {
            timetableList
        }
    }

    @ViewBuilder
    private var timetableList: some View {
        let days = vm.week?.daysFor(vm.selectedTab) ?? []
        ScrollView {
            LazyVStack(alignment: .leading, spacing: 0, pinnedViews: [.sectionHeaders]) {
                if days.isEmpty {
                    EmptyStateView(heading: "No classes this day")
                } else {
                    ForEach(days, id: \.date) { day in
                        Section {
                            if day.classes.isEmpty {
                                EmptyStateView(heading: "No classes this day")
                            } else {
                                ForEach(day.classes, id: \.classId) { instance in
                                    ClassRow(
                                        instance: instance,
                                        inFlight: vm.rowActionInFlight.contains(instance.classId),
                                        onBook: { vm.book(clubId: instance.clubId, classId: instance.classId) }
                                    )
                                }
                            }
                        } header: {
                            dayHeader(day.date)
                        }
                    }
                }
            }
            .padding(.bottom, VaSpacing.lg)
        }
    }

    private func dayHeader(_ date: String) -> some View {
        Text(DateTimeDisplay.shared.dateLabel(isoWithOffset: date))
            .font(.vaLabel)
            .foregroundStyle(VaColor.onSurface)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, VaSpacing.md)
            .padding(.vertical, VaSpacing.sm)
            .background(VaColor.white)
    }
}
