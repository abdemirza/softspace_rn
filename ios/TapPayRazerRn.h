
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNTapPayRazerRnSpec.h"

@interface TapPayRazerRn : NSObject <NativeTapPayRazerRnSpec>
#else
#import <React/RCTBridgeModule.h>

@interface TapPayRazerRn : NSObject <RCTBridgeModule>
#endif

@end
