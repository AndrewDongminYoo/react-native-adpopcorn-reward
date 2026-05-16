//
//  RNAdPopcornRewardModule.m
//  AdPopcornRewardPlugin
//
//  Created by Mick on 2023/06/07.
//
#import <Foundation/Foundation.h>
#import "RNAdPopcornRewardModule.h"

static UIWindow * _Nullable APActiveWindow(void) {
    if (@available(iOS 15.0, *)) {
        for (UIScene *scene in [UIApplication sharedApplication].connectedScenes) {
            if (scene.activationState == UISceneActivationStateForegroundActive && [scene isKindOfClass:[UIWindowScene class]]) {
                return ((UIWindowScene *)scene).keyWindow;
            }
        }
    }
    if (@available(iOS 13.0, *)) {
        for (UIScene *scene in [UIApplication sharedApplication].connectedScenes) {
            if (scene.activationState == UISceneActivationStateForegroundActive && [scene isKindOfClass:[UIWindowScene class]]) {
                for (UIWindow *window in ((UIWindowScene *)scene).windows) {
                    if (window.isKeyWindow) return window;
                }
            }
        }
    }
    return [UIApplication sharedApplication].keyWindow;
}

@implementation RNAdPopcornRewardModule

RCT_EXPORT_MODULE(RNAdPopcornRewardModule)

+ (BOOL)requiresMainQueueSetup
{
  return YES;
}

- (NSArray<NSString*>*)supportedEvents
{
  return @[@"OnClosedOfferWallPage", @"OnCompletedCampaign", @"OnOfferwallTotalRewardInfo", @"OnBridgeTotalRewardInfo"];
}

RCT_EXPORT_METHOD(setAppKey:(NSString *)appKey hashKey:(NSString *)hashKey)
{
  dispatch_async(dispatch_get_main_queue(), ^{
    [AdPopcornOfferwall setAppKey:appKey andHashKey:hashKey];
  });
}

RCT_EXPORT_METHOD(setLogEnable:(BOOL)enable)
{
  dispatch_async(dispatch_get_main_queue(), ^{
    if(enable)
    {
      [AdPopcornOfferwall setLogLevel:AdPopcornOfferwallLogTrace];
    }
    else
    {
      [AdPopcornOfferwall setLogLevel:AdPopcornOfferwallLogOff];
    }
  });
}

RCT_EXPORT_METHOD(openOfferwall)
{
  dispatch_async(dispatch_get_main_queue(), ^{
    [AdPopcornOfferwall shared].delegate = self;
    [AdPopcornOfferwall openOfferWallWithViewController:[APActiveWindow() rootViewController] delegate:self userDataDictionaryForFilter:nil];
  });
}

RCT_EXPORT_METHOD(openBridge:(NSString *)bridgePlacementId)
{
  dispatch_async(dispatch_get_main_queue(), ^{
    [AdPopcornOfferwall shared].delegate = self;
    [AdPopcornOfferwall openBridgeWithViewController:[APActiveWindow() rootViewController] bridgePlacementId:bridgePlacementId delegate:self];
  });
}

RCT_EXPORT_METHOD(openCSPage)
{
  dispatch_async(dispatch_get_main_queue(), ^{
    [AdPopcornOfferwall openCSViewController:[APActiveWindow() rootViewController]];
  });
}

RCT_EXPORT_METHOD(setUserId:(NSString *)userId)
{
  [AdPopcornOfferwall setUserId:userId];
}

RCT_EXPORT_METHOD(setStyle:(NSString *)offerwallTitle mainOfferwallColor:(NSString *)colorCode)
{
  if(offerwallTitle != nil)
  {
    [AdPopcornStyle sharedInstance].offerwallTitle = offerwallTitle;
  }
  
  // #RRGGBB
  if(colorCode != nil && colorCode.length == 7)
  {
    [AdPopcornStyle sharedInstance].mainOfferwallColor = [self colorFromHexString:colorCode];
  }
}

RCT_EXPORT_METHOD(getOfferwallTotalRewardInfo)
{
  dispatch_async(dispatch_get_main_queue(), ^{
    [AdPopcornOfferwall shared].delegate = self;
    [AdPopcornOfferwall getEarnableTotalRewardInfo:self];
  });
}

RCT_EXPORT_METHOD(getBridgeTotalRewardInfo:(NSString *)bridgePlacementId)
{
  dispatch_async(dispatch_get_main_queue(), ^{
    [AdPopcornOfferwall shared].delegate = self;
    [AdPopcornOfferwall getBridgeTotalRewardInfo:bridgePlacementId delegate:self];
  });
}

- (UIColor *)colorFromHexString:(NSString *)hexString {
    unsigned rgbValue = 0;
    NSScanner *scanner = [NSScanner scannerWithString:hexString];
    [scanner setScanLocation:1]; // bypass '#' character
    [scanner scanHexInt:&rgbValue];
    return [UIColor colorWithRed:((rgbValue & 0xFF0000) >> 16)/255.0 green:((rgbValue & 0xFF00) >> 8)/255.0 blue:(rgbValue & 0xFF)/255.0 alpha:1.0];
}

#pragma mark AdPopcornOfferwallDelegate
- (void)didCloseOfferWall
{
  NSLog(@"RNAdPopcornRewardModule didCloseOfferWall");
  [self sendEventWithName:@"OnClosedOfferWallPage" body:nil];
}

- (void)onCompletedCampaign
{
  NSLog(@"RNAdPopcornRewardModule onCompletedCampaign");
  [self sendEventWithName:@"OnCompletedCampaign" body:nil];
}

- (void)offerwallTotalRewardInfo:(BOOL)queryResult totalCount:(NSInteger)count totalReward:(NSString *)reward
{
  NSLog(@"RNAdPopcornRewardModule offerwallTotalRewardInfo : %d / %ld / %@", queryResult, (long)count, reward);
  [self sendEventWithName:@"OnOfferwallTotalRewardInfo" body:@{
    @"queryResult": @(queryResult),
    @"totalCount": @(count),
    @"totalReward": reward ?: @""
  }];
}

- (void)bridgeTotalRewardInfo:(BOOL)queryResult totalCount:(NSInteger)count totalReward:(NSString *)reward bridgePlacementId:(NSString *)bridgePlacementId
{
  NSLog(@"RNAdPopcornRewardModule bridgeTotalRewardInfo : %d / %ld / %@ / %@", queryResult, (long)count, reward, bridgePlacementId);
  [self sendEventWithName:@"OnBridgeTotalRewardInfo" body:@{
    @"queryResult": @(queryResult),
    @"totalCount": @(count),
    @"totalReward": reward ?: @"",
    @"bridgePlacementId": bridgePlacementId ?: @""
  }];
}

@end
