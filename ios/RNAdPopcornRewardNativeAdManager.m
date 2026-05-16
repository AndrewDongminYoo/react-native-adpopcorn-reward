//
//  RNAdPopcornRewardNativeAdManager.m
//  AdPopcornRewardPlugin
//
//  RN 브릿지 역할만 담당하는 ViewManager.
//  실제 뷰 로직은 RNAdPopcornRewardNativeAdView에 위임한다.
//

#import <React/RCTViewManager.h>
#import <React/RCTUIManager.h>
#import "RNAdPopcornRewardNativeAdView.h"

@interface RNAdPopcornRewardNativeAdManager : RCTViewManager
@end

@implementation RNAdPopcornRewardNativeAdManager

RCT_EXPORT_MODULE(RNAdPopcornRewardNativeAd)

- (UIView *)view {
    return [[RNAdPopcornRewardNativeAdView alloc] init];
}

RCT_EXPORT_VIEW_PROPERTY(placementId, NSString)
RCT_EXPORT_VIEW_PROPERTY(nativeWidth, NSInteger)
RCT_EXPORT_VIEW_PROPERTY(nativeHeight, NSInteger)
RCT_EXPORT_VIEW_PROPERTY(onLoadSuccess, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onLoadFailed, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onClicked, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onCompleted, RCTDirectEventBlock)

RCT_EXPORT_METHOD(loadAd:(nonnull NSNumber *)reactTag)
{
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
        RNAdPopcornRewardNativeAdView *view = (RNAdPopcornRewardNativeAdView *)viewRegistry[reactTag];
        if ([view isKindOfClass:[RNAdPopcornRewardNativeAdView class]]) {
            [view loadAd];
        }
    }];
}

RCT_EXPORT_METHOD(stopAd:(nonnull NSNumber *)reactTag)
{
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
        RNAdPopcornRewardNativeAdView *view = (RNAdPopcornRewardNativeAdView *)viewRegistry[reactTag];
        if ([view isKindOfClass:[RNAdPopcornRewardNativeAdView class]]) {
            [view stopAd];
        }
    }];
}

@end
