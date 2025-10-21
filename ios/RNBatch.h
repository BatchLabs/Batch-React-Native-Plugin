#import <React/RCTEventEmitter.h>
#import <Batch/Batch.h>

#define PluginVersion "ReactNative/11.1.0"

#ifdef RCT_NEW_ARCH_ENABLED
#import <RNBatchSpec/RNBatchSpec.h>
@interface RNBatch: RCTEventEmitter <NativeRNBatchModuleSpec>
#else
#import <React/RCTBridgeModule.h>
@interface RNBatch: RCTEventEmitter <RCTBridgeModule>
#endif

+ (void)start;

@property (nonatomic, strong) NSMutableDictionary<NSString *, BatchInboxFetcher *> *batchInboxFetcherMap;

@end
