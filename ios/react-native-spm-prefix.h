// Prefix header for Swift Package Manager builds of @batch.com/react-native-plugin.
//
// RN's SPM tooling force-includes a file of this name (via `-include`) to mirror
// CocoaPods' default prefix header, so Objective-C sources that rely on an
// implicit Foundation/UIKit import compile under SPM (which has no prefix-header
// mechanism). The scaffolder generates this automatically for libraries it
// manages; because our Package.swift is self-managed, we ship it ourselves.
#ifdef __OBJC__
#import <Foundation/Foundation.h>
#if __has_include(<UIKit/UIKit.h>)
#import <UIKit/UIKit.h>
#endif
#endif
