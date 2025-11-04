#import <Batch/Batch.h>

@interface RNBatchEvent : NSObject

@property (readonly) NSString* _Nonnull name;

@property (readonly) NSDictionary* _Nonnull body;

- (nonnull instancetype)initWithName:(nonnull NSString *)name andBody:(nullable NSDictionary *)body;

@end

@interface RNBatchEventDispatcher : NSObject <BatchEventDispatcherDelegate>

- (void)setSendBlock:(void (^_Nullable)(RNBatchEvent* _Nonnull event))callback;

- (void)setModuleIsReady:(BOOL)ready;

+ (nullable NSString *)mapBatchEventDispatcherTypeToRNEvent:(BatchEventDispatcherType)type;

@end
