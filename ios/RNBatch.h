#if __has_include(<React/RCTBridgeModule.h>)
  #import <React/RCTBridgeModule.h>
#else
  #import <React/RCTBridgeModule.h>
#endif

#if __has_include(<React/RCTEventEmitter.h>)
  #import <React/RCTEventEmitter.h>
#else
  #import <React/RCTEventEmitter.h>
#endif

#import <Batch/Batch.h>

#define PluginVersion "ReactNative/9.0.2"

@interface RNBatch : RCTEventEmitter <RCTBridgeModule>

+ (void)start;

@property (nonatomic, strong) NSMutableDictionary<NSString *, BatchInboxFetcher *> *batchInboxFetcherMap;

@end
