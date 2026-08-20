// swift-tools-version: 6.0
//
// Swift Package Manager support for @batch.com/react-native-plugin.


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
        .package(url: "https://github.com/BatchLabs/Batch-iOS-SDK.git", .upToNextMinor(from: "3.4.0")),
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
