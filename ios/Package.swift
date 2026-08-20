// swift-tools-version: 6.0
//
// Swift Package Manager support for @batch.com/react-native-plugin.
//
// This is a hand-authored, self-managed manifest: React Native's experimental
// SPM tooling (`npx react-native spm`) detects it and leaves it untouched
// (it only regenerates manifests that carry its own AUTO-SCAFFOLDED marker).
// We own it here because RN's autolinking scaffolder cannot resolve the
// plugin's external native dependency — the Batch iOS SDK (the podspec's
// `s.dependency 'Batch'`), which is not an npm package — nor propagate the
// New Architecture flag to the generated target.
//
// Path references to the React Native + codegen packages are resolved from the
// normalized symlink location the autolinker creates
// (`<app>/ios/build/generated/autolinking/libs/ReactNativePlugin`), which sits
// at a fixed depth regardless of this package's own (scoped) node_modules
// path — hence the plain relative paths below.
//
// Requires Xcode 16.3+ (Swift tools 6.1) because the Batch iOS SDK's own
// Swift package manifest targets swift-tools 6.1.

import PackageDescription

let package = Package(
    name: "ReactNativePlugin",
    platforms: [.iOS(.v15)],
    products: [
        // The product name must stay "ReactNativePlugin": RN's autolinker
        // derives it from the npm package name and references it that way from
        // the generated aggregate package. The *target* (i.e. the importable
        // Swift/Clang module) is named "RNBatchPush" so consumers use the same
        // `import RNBatchPush` under SPM as under CocoaPods.
        .library(name: "ReactNativePlugin", targets: ["RNBatchPush"]),
    ],
    dependencies: [
        .package(name: "ReactNative", path: "../../../../xcframeworks"),
        .package(name: "React-GeneratedCode", path: "../../../ios"),
        // Native Batch iOS SDK. Pinned to 3.3.x (major+minor fixed, patch
        // floats to the latest) to mirror the podspec's `~> 3.3.0`.
        .package(url: "https://github.com/BatchLabs/Batch-iOS-SDK.git", .upToNextMinor(from: "3.3.0")),
    ],
    targets: [
        .target(
            name: "RNBatchPush",
            dependencies: [
                .product(name: "ReactHeaders", package: "ReactNative"),
                .product(name: "ReactNativeHeaders", package: "ReactNative"),
                .product(name: "ReactNativeDependenciesHeaders", package: "ReactNative"),
                .product(name: "ReactAppHeaders", package: "React-GeneratedCode"),
                .product(name: "Batch", package: "Batch-iOS-SDK"),
            ],
            path: ".",
            sources: [
                "BatchBridgeNotificationCenterDelegate.h",
                "BatchBridgeNotificationCenterDelegate.m",
                "RNBatch.h",
                "RNBatch.mm",
                "RNBatchEventDispatcher.h",
                "RNBatchEventDispatcher.mm",
                "RNBatchOpenedNotificationObserver.h",
                "RNBatchOpenedNotificationObserver.m",
            ],
            publicHeadersPath: ".",
            cSettings: [
                .headerSearchPath("."),
                .unsafeFlags(["-include", "react-native-spm-prefix.h"]),
                .define("RCT_NEW_ARCH_ENABLED", to: "1"),
            ],
            cxxSettings: [
                .headerSearchPath("."),
                .unsafeFlags(["-include", "react-native-spm-prefix.h"]),
                .define("DEBUG", .when(configuration: .debug)),
                .define("NDEBUG", .when(configuration: .release)),
                .define("RCT_NEW_ARCH_ENABLED", to: "1"),
            ],
            linkerSettings: [
                .linkedFramework("UIKit"),
                .linkedFramework("Foundation"),
                .linkedFramework("CoreGraphics"),
            ]
        ),
    ],
    cxxLanguageStandard: .cxx20
)
