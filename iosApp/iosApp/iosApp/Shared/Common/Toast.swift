import SwiftUI

private struct ToastModifier: ViewModifier {
    @Binding var message: String?
    var duration: TimeInterval = 3

    func body(content: Content) -> some View {
        content
            .overlay(alignment: .bottom) {
                if let message {
                    Text(message)
                        .font(.vaBody)
                        .foregroundStyle(VaColor.white)
                        .padding(.horizontal, VaSpacing.md)
                        .padding(.vertical, VaSpacing.sm)
                        .background(VaColor.onSurface)
                        .clipShape(RoundedCornerShape(VaSpacing.cardCornerRadius))
                        .padding(.bottom, VaSpacing.xl)
                        .transition(.move(edge: .bottom).combined(with: .opacity))
                        .task(id: message) {
                            try? await Task.sleep(nanoseconds: UInt64(duration * 1_000_000_000))
                            withAnimation { self.message = nil }
                        }
                }
            }
            .animation(.easeInOut, value: message)
    }
}

private struct RoundedCornerShape: Shape {
    let radius: CGFloat
    init(_ radius: CGFloat) { self.radius = radius }
    func path(in rect: CGRect) -> Path {
        Path(roundedRect: rect, cornerRadius: radius)
    }
}

extension View {
    func toast(_ message: Binding<String?>, duration: TimeInterval = 3) -> some View {
        modifier(ToastModifier(message: message, duration: duration))
    }
}
