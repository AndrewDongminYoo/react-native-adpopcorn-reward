#import "AdpopcornReward.h"

#import <AdPopcornOfferwall/AdPopcornStyle.h>
#import <AdPopcornOfferwall/RewardInfo.h>
#import <UIKit/UIKit.h>

// In-flight policy: each reward-info query is single-flight. A concurrent call
// while one is pending rejects with E_INFLIGHT — keeps Promise semantics
// unambiguous, matches the legacy callback model (last writer wins, but we make
// it explicit) and avoids leaking resolve blocks.
static NSString *const APRewardErrorInflight = @"E_INFLIGHT";
static NSString *const APRewardErrorInflightMessage =
    @"another reward info query for this scope is already in flight";

static UIWindow *_Nullable APActiveWindow(void) {
  if (@available(iOS 15.0, *)) {
    for (UIScene *scene in [UIApplication sharedApplication].connectedScenes) {
      if (scene.activationState == UISceneActivationStateForegroundActive &&
          [scene isKindOfClass:[UIWindowScene class]]) {
        return ((UIWindowScene *)scene).keyWindow;
      }
    }
  }
  if (@available(iOS 13.0, *)) {
    for (UIScene *scene in [UIApplication sharedApplication].connectedScenes) {
      if (scene.activationState == UISceneActivationStateForegroundActive &&
          [scene isKindOfClass:[UIWindowScene class]]) {
        for (UIWindow *window in ((UIWindowScene *)scene).windows) {
          if (window.isKeyWindow) {
            return window;
          }
        }
      }
    }
  }
  return [UIApplication sharedApplication].keyWindow;
}

static UIColor *_Nullable APColorFromHex(NSString *hexString) {
  if (hexString.length != 7) {
    return nil;
  }
  unsigned rgbValue = 0;
  NSScanner *scanner = [NSScanner scannerWithString:hexString];
  [scanner setScanLocation:1]; // skip '#'
  if (![scanner scanHexInt:&rgbValue]) {
    return nil;
  }
  return [UIColor colorWithRed:((rgbValue & 0xFF0000) >> 16) / 255.0
                         green:((rgbValue & 0xFF00) >> 8) / 255.0
                          blue:(rgbValue & 0xFF) / 255.0
                         alpha:1.0];
}

@interface AdpopcornReward ()
@property(nonatomic, copy, nullable) RCTPromiseResolveBlock offerwallResolve;
@property(nonatomic, copy, nullable) RCTPromiseRejectBlock offerwallReject;
@property(nonatomic, strong) NSMutableDictionary<NSString *, RCTPromiseResolveBlock> *bridgeResolvers;
@property(nonatomic, strong) NSMutableDictionary<NSString *, RCTPromiseRejectBlock> *bridgeRejecters;
@end

@implementation AdpopcornReward

- (instancetype)init {
  if ((self = [super init])) {
    _bridgeResolvers = [NSMutableDictionary dictionary];
    _bridgeRejecters = [NSMutableDictionary dictionary];
  }
  return self;
}

+ (NSString *)moduleName {
  return @"AdpopcornReward";
}

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params {
  return std::make_shared<facebook::react::NativeAdpopcornRewardSpecJSI>(params);
}

#pragma mark - Spec methods

- (void)setAppKey:(NSString *)appKey hashKey:(NSString *)hashKey {
  dispatch_async(dispatch_get_main_queue(), ^{
    [AdPopcornOfferwall setAppKey:appKey andHashKey:hashKey];
  });
}

- (void)setLogEnable:(BOOL)enable {
  dispatch_async(dispatch_get_main_queue(), ^{
    [AdPopcornOfferwall setLogLevel:enable ? AdPopcornOfferwallLogTrace : AdPopcornOfferwallLogOff];
  });
}

- (void)setUserId:(NSString *)userId {
  [AdPopcornOfferwall setUserId:userId];
}

- (void)setStyle:(NSString *)offerwallTitle mainOfferwallColorHex:(NSString *)mainOfferwallColorHex {
  if (offerwallTitle.length > 0) {
    [AdPopcornStyle sharedInstance].offerwallTitle = offerwallTitle;
  }
  UIColor *color = APColorFromHex(mainOfferwallColorHex);
  if (color != nil) {
    [AdPopcornStyle sharedInstance].mainOfferwallColor = color;
  }
}

- (void)openOfferwall {
  dispatch_async(dispatch_get_main_queue(), ^{
    [AdPopcornOfferwall shared].delegate = self;
    [AdPopcornOfferwall openOfferWallWithViewController:APActiveWindow().rootViewController
                                               delegate:self
                            userDataDictionaryForFilter:nil];
  });
}

- (void)openBridge:(NSString *)bridgePlacementId {
  dispatch_async(dispatch_get_main_queue(), ^{
    [AdPopcornOfferwall shared].delegate = self;
    [AdPopcornOfferwall openBridgeWithViewController:APActiveWindow().rootViewController
                                  bridgePlacementId:bridgePlacementId
                                           delegate:self];
  });
}

- (void)openCSPage {
  dispatch_async(dispatch_get_main_queue(), ^{
    [AdPopcornOfferwall openCSViewController:APActiveWindow().rootViewController];
  });
}

- (void)getOfferwallTotalRewardInfo:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
  if (self.offerwallResolve != nil) {
    reject(APRewardErrorInflight, APRewardErrorInflightMessage, nil);
    return;
  }
  self.offerwallResolve = resolve;
  self.offerwallReject = reject;
  dispatch_async(dispatch_get_main_queue(), ^{
    [AdPopcornOfferwall shared].delegate = self;
    [AdPopcornOfferwall getEarnableTotalRewardInfo:self];
  });
}

- (void)getBridgeTotalRewardInfo:(NSString *)bridgePlacementId
                         resolve:(RCTPromiseResolveBlock)resolve
                          reject:(RCTPromiseRejectBlock)reject {
  if (self.bridgeResolvers[bridgePlacementId] != nil) {
    reject(APRewardErrorInflight, APRewardErrorInflightMessage, nil);
    return;
  }
  self.bridgeResolvers[bridgePlacementId] = resolve;
  self.bridgeRejecters[bridgePlacementId] = reject;
  dispatch_async(dispatch_get_main_queue(), ^{
    [AdPopcornOfferwall shared].delegate = self;
    [AdPopcornOfferwall getBridgeTotalRewardInfo:bridgePlacementId delegate:self];
  });
}

#pragma mark - AdPopcornOfferwallDelegate

- (void)didCloseOfferWall {
  [self emitOnClosedOfferWallPage];
}

- (void)onCompletedCampaign {
  [self emitOnCompletedCampaign];
}

- (void)offerwallTotalRewardInfo:(BOOL)queryResult
                      totalCount:(NSInteger)count
                     totalReward:(NSString *)reward {
  RCTPromiseResolveBlock resolve = self.offerwallResolve;
  self.offerwallResolve = nil;
  self.offerwallReject = nil;
  if (resolve != nil) {
    resolve(@{
      @"queryResult" : @(queryResult),
      @"totalCount" : @(count),
      @"totalReward" : reward ?: @"",
    });
  }
}

- (void)bridgeTotalRewardInfo:(BOOL)queryResult
                   totalCount:(NSInteger)count
                  totalReward:(NSString *)reward
            bridgePlacementId:(NSString *)bridgePlacementId {
  RCTPromiseResolveBlock resolve = self.bridgeResolvers[bridgePlacementId];
  [self.bridgeResolvers removeObjectForKey:bridgePlacementId];
  [self.bridgeRejecters removeObjectForKey:bridgePlacementId];
  if (resolve != nil) {
    resolve(@{
      @"queryResult" : @(queryResult),
      @"totalCount" : @(count),
      @"totalReward" : reward ?: @"",
      @"bridgePlacementId" : bridgePlacementId ?: @"",
    });
  }
}

@end
