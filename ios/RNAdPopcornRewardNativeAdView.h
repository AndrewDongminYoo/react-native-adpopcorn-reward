//
//  RNAdPopcornRewardNativeAdView.h
//  AdPopcornRewardPlugin
//

#import <UIKit/UIKit.h>
#import <React/RCTComponent.h>
#import <AdPopcornOfferwall/AdPopcornRewardNativeAd.h>

/**
 * AdPopcornRewardNativeAd를 감싸는 컨테이너 뷰.
 * props 수신, 광고 생성/해제, 콜백 전달을 담당한다.
 */
@interface RNAdPopcornRewardNativeAdView : UIView <AdPopcornRewardNativeAdDelegate>

@property (nonatomic, strong) AdPopcornRewardNativeAd *nativeAd;
@property (nonatomic, copy) NSString *placementId;
@property (nonatomic, assign) NSInteger nativeWidth;
@property (nonatomic, assign) NSInteger nativeHeight;

@property (nonatomic, copy) RCTDirectEventBlock onLoadSuccess;
@property (nonatomic, copy) RCTDirectEventBlock onLoadFailed;
@property (nonatomic, copy) RCTDirectEventBlock onClicked;
@property (nonatomic, copy) RCTDirectEventBlock onCompleted;

- (void)loadAd;
- (void)stopAd;

@end
