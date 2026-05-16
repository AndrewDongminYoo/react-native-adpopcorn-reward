//
//  RNAdPopcornRewardNativeAdView.m
//  AdPopcornRewardPlugin
//

#import "RNAdPopcornRewardNativeAdView.h"
#import <React/RCTUtils.h>

@implementation RNAdPopcornRewardNativeAdView

- (void)dealloc {
    [self stopAd];
}

- (void)setPlacementId:(NSString *)placementId {
    _placementId = placementId;
    [self setupIfReady];
}

- (void)setNativeWidth:(NSInteger)nativeWidth {
    _nativeWidth = nativeWidth;
    [self setupIfReady];
}

- (void)setNativeHeight:(NSInteger)nativeHeight {
    _nativeHeight = nativeHeight;
    [self setupIfReady];
}

- (void)loadAd {
    if (!_nativeAd) {
        [self setupIfReady];
    } else {
        [_nativeAd loadAd];
    }
}

- (void)stopAd {
    if (_nativeAd) {
        [_nativeAd removeFromSuperview];
        _nativeAd.delegate = nil;
        _nativeAd = nil;
    }
}

- (void)setupIfReady {
    if (!_placementId || _placementId.length == 0 || _nativeWidth <= 0 || _nativeHeight <= 0) return;

    [self stopAd];

    CGRect frame = CGRectMake(0, 0, _nativeWidth, _nativeHeight);
    _nativeAd = [[AdPopcornRewardNativeAd alloc] initWithFrame:frame viewController:RCTPresentedViewController()];
    _nativeAd.placementId = _placementId;
    _nativeAd.delegate = self;
    [self addSubview:_nativeAd];

    dispatch_async(dispatch_get_main_queue(), ^{
        [self->_nativeAd loadAd];
    });
}

#pragma mark - AdPopcornRewardNativeAdDelegate

- (void)ApRewardNativeAdLoadSuccess {
    if (self.onLoadSuccess) {
        self.onLoadSuccess(@{});
    }
}

- (void)ApRewardNativeAdLoadFailed:(NSInteger)errorCode {
    if (self.onLoadFailed) {
        self.onLoadFailed(@{@"errorCode": @(errorCode)});
    }
}

- (void)ApRewardNativeAdClicked {
    if (self.onClicked) {
        self.onClicked(@{});
    }
}

- (void)ApRewardNativeAdCompleted {
    if (self.onCompleted) {
        self.onCompleted(@{});
    }
}

@end
