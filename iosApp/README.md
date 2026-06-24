# iOS App — Shared.xcframework Integration

The iOS app is a native SwiftUI project that consumes the Kotlin Multiplatform `:shared`
module as a **static XCFramework** named `Shared` (no CocoaPods, no SPM-from-KMP).

> The Xcode project itself (`iosApp.xcodeproj`) is created/linked **manually in Xcode** —
> Claude cannot drive the Xcode UI. The Swift source (`iosApp/iosApp/iOSApp.swift`),
> `Info.plist`, and these instructions are provided; you wire them into an Xcode app target
> as described below. This is the manual-only step noted in `01-VALIDATION.md`.

## 1. Build the XCFramework

From the repository root:

```bash
./gradlew :shared:assembleSharedXCFramework            # debug + release slices
# or, for the dev loop, the per-config variant:
./gradlew :shared:assembleSharedDebugXCFramework
```

The artifact is produced under `shared/build/XCFrameworks/<config>/Shared.xcframework`.

## 2. Run Script build phase (recommended — auto-embed + sign)

In the Xcode app target, add a **New Run Script Phase** and move it **BEFORE**
"Compile Sources":

```bash
cd "$SRCROOT/.."
./gradlew :shared:embedAndSignAppleFrameworkForXcode
```

This task reads Xcode's environment (`CONFIGURATION`, `SDK_NAME`, `ARCHS`), builds the
correct slice, and copies + code-signs `Shared.framework` into the app bundle.

### Framework Search Paths

Add the framework output directory to the target's **Framework Search Paths**, e.g.:

```
$(SRCROOT)/../shared/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)
```

Then `import Shared` in Swift (see `iosApp/iOSApp.swift`, which calls
`KoinKt.doInitKoin()` at launch).

## 3. App Transport Security (dev)

`Info.plist` includes a **dev-only** ATS exception permitting cleartext HTTP to
`localhost` (the local mock API on `:8080`). Production must use HTTPS — do not add
`NSAllowsArbitraryLoads`.

## 4. Intel-Mac reviewers — enabling the `iosX64` simulator target

The build targets `iosArm64` (device) + `iosSimulatorArm64` (Apple-Silicon simulator)
by default. On an **Intel Mac**, add the Intel simulator slice in `shared/build.gradle.kts`
by uncommenting the one line in the targets list:

```kotlin
listOf(
    iosArm64(),
    iosSimulatorArm64(),
    iosX64(),          // ← uncomment for Intel-Mac simulators
).forEach { target ->
    target.binaries.framework {
        baseName = "Shared"
        isStatic = true
        xcf.add(this)
    }
}
```

Re-run `./gradlew :shared:assembleSharedXCFramework` afterwards. [D-RES-02]
