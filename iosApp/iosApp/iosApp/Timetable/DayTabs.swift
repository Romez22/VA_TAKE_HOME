import SwiftUI
import Shared

struct DayTabs: View {
    let selected: DayTab
    let onSelect: (DayTab) -> Void

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: VaSpacing.sm) {
                ForEach(DayTab.allCases, id: \.self) { tab in
                    DayTabPill(
                        label: tab.label,
                        isActive: tab == selected,
                        onTap: { onSelect(tab) }
                    )
                }
            }
            .padding(.horizontal, VaSpacing.md)
            .padding(.vertical, VaSpacing.sm)
        }
    }
}

private struct DayTabPill: View {
    let label: String
    let isActive: Bool
    let onTap: () -> Void

    private var background: Color { isActive ? VaColor.vaRed : VaColor.surfaceVariant }
    private var content: Color { isActive ? VaColor.white : VaColor.onSurfaceVariant }

    var body: some View {
        Button(action: onTap) {
            Text(label)
                .font(.vaLabel)
                .foregroundStyle(content)
                .padding(.horizontal, VaSpacing.md)
                .padding(.vertical, VaSpacing.sm)
                .frame(minHeight: VaSpacing.minTouchTarget)
                .background(background)
                .clipShape(Capsule())
        }
        .buttonStyle(.plain)
    }
}
