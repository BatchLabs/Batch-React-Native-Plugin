8.2.0
____

**Plugin**

* Updated Batch 1.21. 
* Batch requires iOS 12.0 or higher. 
* Batch now compiles with and targets SDK 34 (Android 14).
* Added support for react-native 0.73+
* Added support for Expo 50. Since, as of writing, Expo 50 is still in beta, this might change after the stable release.
* Fixed an issue on iOS where `refreshToken` was not running on main thread.

**User**

* Removed automatic collection of the advertising id. You need to collect it from your side and pass it to Batch via the added `BatchUser.editor().setAttributionIdentifier(id)` method.
* Added `setEmail` method to `BatchUserEditor`. This requires to have a user identifier registered or to call the `setIdentifier` method on the editor instance beforehand.
* Added `setEmailMarketingSubscriptionState` method to `BatchUserEditor`.

**Inbox**

* Added `hasLandingMessage` property to `IInboxNotification`.
* Added `displayNotificationLandingMessage` method to `BatchInboxFetcher`.

8.1.2
----

**Plugin**

* Fixed an issue on Android where `open` push message events queued on cold start were sent before we could register a listener.

8.1.1
----

**Plugin**

* Fixed an issue where listening for `open` push message events wasn't working on cold start.

8.1.0
----

**Plugin**

* Plugin now compiles with and targets SDK 33 (Android 13).

**Push**

* Added a new API: `BatchPush.requestNotificationAuthorization()`. This allows you to request for the [new notification permission introduced](https://developer.android.com/about/versions/13/changes/notification-permission) in Android 13. See the documentation for more info.


8.0.2
----

**Plugin**

* Fixed autolinking on react-native 0.69+ and Expo 46

8.0.0 - 8.0.1
----

**Plugin**

* Updated Batch 1.19. Batch requires Xcode 13.3.1 and iOS 10.0 or higher.
* Updated how Batch is imported to support React-Native v0.68 wich now uses Objective-C++.
* Added support for Expo 45

**User**

* Added getters for `identifier`, `language`, `region`, `attributes` and `tagCollections` in `BatchUser`.
* Added a fix where you couldn't use `setLanguage` or `setRegion` with a nil value on iOS.

7.0.3
----

* Add support for expo 44 
* Add ios expo plugin

7.0.2
----

* Add check for active catalyst instance on react context

7.0.1
----

* Add support for Expo plugins for Android (no changes for regular usage)

7.0.0
----

**Batch SDK version**

* Migrated to Batch SDK v1.18
    * For iOS: 
        1. **[BREAKING]** Make sure to update your Podfile Batch SDK version
        2. **[BREAKING]** Batch requires Xcode 13 or higher

**Events**

* Added URL type in event data with `putURL`.
* Added Date type in event data with `putDate`.

**Attributes**

* Added URL type in custom attributes with `setURLAttribute`.

**Messaging**

* Added ` BatchMessaging.addListener` to listen for messaging notifications events. See [documentation](/react-native/messaging#listening-events).

**Push**

* Added ` BatchPush.addListener` to listen for push notifications events. See [documentation](/react-native/messaging#listening-events) to see platform differences.

* iOS: Deprecated `BatchPush.registerForRemoteNotifications` by splitting it into two methods:
  * `BatchPush.refreshToken`, which should be called on every app start after opt-in if you're opted out by default.
  * `BatchPush.requestNotificationAuthorization`, which should be called whenever you want to ask the user the permission to display notifications.
* iOS: Added `requestProvisionalNotificationAuthorization` to request a provisional authorization on iOS 11 and higher.

**Bug Fixes**

* Fix `isUnread` property of a Batch inbox notification : it was previously always undefined, now it is correctly set.
* Fix crash in `trackTransaction` when it was called without data as second parameter.

6.0.0
----

**Batch SDK version**

* Migrated to Batch SDK v1.17
    * For iOS: 
        1. **[BREAKING]** Make sure to update your Podfile Batch SDK version
        2. **[BREAKING]** Batch requires Xcode 12 and iOS 10.0 or higher
        3. **[BREAKING]** Make sure to follow again the [Extra steps on iOS](https://github.com/bamlab/react-native-batch-push#5-extra-steps-on-ios), a new section has been added about the `BatchUNUserNotificationCenterDelegate` (if you haven't implemented a delegate yet)
    * For Android: 
        1. **[BREAKING]** Make sure to have up-to-date android build tools (see [Android blog](https://android-developers.googleblog.com/2020/07/preparing-your-build-for-package-visibility-in-android-11.html))
* [Android] You can now update the Batch SDK manually with the rootProject ext property `batchSdkVersion`. On iOS this was already possible (you set the version in your Podfile)

**Mobile landings /  In-App messages**

* Added `BatchMessaging.setFontOverride()` so that you can change the font of messaging views (eg. In-app campaigns landings)
* Added `disableDoNotDisturbAndShowPendingMessage` for quicker setup and to minimize any race condition between disabling do not disturb and showing the pending message
* Do not disturb (for mobile landings/in app messages) can now be enabled natively on Android and iOS. 99% of the time on a regular React Native app, you should enable it. See [README](https://github.com/bamlab/react-native-batch-push#4-configure-do-not-disturb-mode-for-mobile-landingsin-app-messages)) to see why and how to handle the new behavior (call `disableDoNotDisturbAndShowPendingMessage` when ready, or disable do not disturb manually).
  * **[BREAKING]** `Batch.start` is now useless so it has been removed. You must remove any call to `Batch.start` in your javascript code.
  * **[BREAKING]** [iOS] the optional argument to `[RNBatch start]` native method is now useless so it has been removed. You must replace any `[RNBatch start:false]` to `[RNBatch start]`

**DX improvements**

* Chaining Batch User editor is now optional
* Added `Batch.showDebugView()` so that you can easily debug your Batch installation

**Inbox**

* **[BREAKING]** [Inbox] Rewrote inbox API to expose every native SDK Inbox methods such as `markNotificationAsRead`. You can now implement an infinite loading list of notifications with this API. (see [migration examples](https://github.com/bamlab/react-native-batch-push/releases/v6.0.0-rc.2))

**Bug Fixes**

* [Android] Fix resume count
  * This removes an error in the logs when receiving a push while in background and potentially fixes issues related to the app going from background to foreground
  * **[BREAKING]** In order to migrate, make sure to follow the new [`README`](https://github.com/bamlab/react-native-batch-push#b-configure-auto-linking) section on configuring auto-linking
* Fixed a GDPR scenario case where the Batch iOS/Android SDK might not be started
* Added `BatchPush.getInitialURL` to workaround issues on iOS to get the deeplink associated to the notification that opened the app initially. `Linking.getInitialURL` can be replaced by `BatchPush.getInitialURL` on every platforms.
* [iOS] [Inbox] Fixed a crash when a notification's source was unknown
* Exposed promises in critical methods such as `optIn` in order to prevent potential race conditions
* [iOS] `showPendingMessage` will now work correctly
* [Android] Fixed potential crash in `showPendingMessage` when the activity is not found

**Documentation**

* Added a guide in the doc for GDPR compliance
* [iOS] Added a guide in the doc for showing notifications on foreground
