import SwiftUI

enum VaColor {
    static let vaRed = Color(hex: 0xE4002B)
    static let white = Color(hex: 0xFFFFFF)
    static let surfaceVariant = Color(hex: 0xF2F3F5)
    static let onSurface = Color(hex: 0x1A1A1A)
    static let onSurfaceVariant = Color(hex: 0x6B7280)
    static let errorRed = Color(hex: 0xB3261E)

    static let successContainer = Color(hex: 0xD7F2DE)
    static let onSuccessContainer = Color(hex: 0x137333)

    static let warningContainer = Color(hex: 0xFFF4D6)
    static let onWarningContainer = Color(hex: 0x8A6100)
    static let warningBorder = Color(hex: 0xF2B705)
}

extension Color {
    init(hex: UInt32) {
        let red = Double((hex >> 16) & 0xFF) / 255.0
        let green = Double((hex >> 8) & 0xFF) / 255.0
        let blue = Double(hex & 0xFF) / 255.0
        self.init(.sRGB, red: red, green: green, blue: blue, opacity: 1.0)
    }
}
