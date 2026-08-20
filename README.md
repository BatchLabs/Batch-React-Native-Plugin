<img src="https://static.batch.com/documentation/Readmes/logo_batch_full_178@2x.png" srcset="https://static.batch.com/documentation/Readmes/logo_batch_full_178.png 1x" alt="Batch Logo" />

# Batch React-Native Plugin

The Batch React-Native Plugin allows you to build meaningful communication experience in your Android/iOS app through highly personalized push notifications & In-App messages.

Our [📕 setup documentation](https://doc.batch.com/react-native/prerequisites) details the steps to take for an easy and successful integration.

# Prerequisites

As this plugin is built over Batch's Native SDKs, their respective Android and iOS requirements apply.

# Documentation
- [Setup guide](https://doc.batch.com/react-native/prerequisites): start your implementation here!
- [Help center](https://help.batch.com/en/): answers to most questions you may have during the integration
- [API reference](https://batchlabs.github.io/Batch-React-Native-Plugin/): this documents each of the classes and methods in the Batch React-Native Plugin

# React Native architecture
Batch ships a TurboModule implementation for the React Native New Architecture.
Starting with v12.0.0, the legacy bridge has been removed and the plugin now requires the New Architecture to be enabled.

You may also find this guide useful to review after integration to make sure you're ready to go live: [How can I test the integration on iOS?](https://help.batch.com/en/articles/2669866-how-can-i-test-the-integration-on-ios) / [How can I test the integration on Android?](https://help.batch.com/en/articles/2672749-how-can-i-test-the-integration-on-android)

# Swift Package Manager (iOS, experimental)

The plugin supports React Native's experimental [Swift Package Manager](https://www.swift.org/documentation/package-manager/) integration, for iOS apps that use `npx react-native spm` instead of CocoaPods.

The plugin ships a self-managed `ios/Package.swift`, so no manual scaffolding is required. Install the plugin, then run your app's usual SPM setup:

```sh
npm install @batch.com/react-native-plugin
npx react-native spm   # regenerates the SPM autolinking graph
```

`ios/Package.swift` wires the native [Batch iOS SDK](https://github.com/BatchLabs/Batch-iOS-SDK) as a Swift package dependency.

# Releases
Release instructions are detailed in [RELEASING.md](https://github.com/BatchLabs/Batch-React-Native-Plugin/blob/master/RELEASING.md).
 
# Building
Build instructions are detailed in [BUILDING.md](https://github.com/BatchLabs/Batch-React-Native-Plugin/blob/master/BUILDING.md).

# Contributing
Please refer to our [contributing guidelines](https://github.com/BatchLabs/Batch-React-Native-Plugin/blob/master/CONTRIBUTING.md).

# Credits
Batch-React-Native-Plugin is based on [bamlab's](https://github.com/bamlab/) [React Native plugin](https://github.com/bamlab/react-native-batch-push).
