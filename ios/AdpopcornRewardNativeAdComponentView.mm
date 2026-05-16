#import "AdpopcornRewardNativeAdComponentView.h"

#import <react/renderer/components/AdpopcornRewardSpec/ComponentDescriptors.h>
#import <react/renderer/components/AdpopcornRewardSpec/EventEmitters.h>
#import <react/renderer/components/AdpopcornRewardSpec/Props.h>
#import <react/renderer/components/AdpopcornRewardSpec/RCTComponentViewHelpers.h>

#import <AdPopcornOfferwall/AdPopcornRewardNativeAd.h>
#import <UIKit/UIKit.h>

using namespace facebook::react;

@interface AdpopcornRewardNativeAdComponentView () <RCTAdpopcornRewardNativeAdViewProtocol, AdPopcornRewardNativeAdDelegate>
@end

@implementation AdpopcornRewardNativeAdComponentView {
  AdPopcornRewardNativeAd *_nativeAd;
  NSString *_placementId;
  NSInteger _nativeWidth;
  NSInteger _nativeHeight;
}

+ (ComponentDescriptorProvider)componentDescriptorProvider {
  return concreteComponentDescriptorProvider<AdpopcornRewardNativeAdComponentDescriptor>();
}

- (instancetype)initWithFrame:(CGRect)frame {
  if ((self = [super initWithFrame:frame])) {
    static const auto defaultProps = std::make_shared<const AdpopcornRewardNativeAdProps>();
    _props = defaultProps;
  }
  return self;
}

- (void)dealloc {
  [self stopAdInternal];
}

#pragma mark - RCTComponentViewProtocol

- (void)updateProps:(const Props::Shared &)props oldProps:(const Props::Shared &)oldProps {
  const auto &newProps = *std::static_pointer_cast<const AdpopcornRewardNativeAdProps>(props);

  NSString *placementId = [NSString stringWithUTF8String:newProps.placementId.c_str()];
  NSInteger nativeWidth = newProps.nativeWidth;
  NSInteger nativeHeight = newProps.nativeHeight;

  BOOL changed = ![placementId isEqualToString:_placementId] ||
                 nativeWidth != _nativeWidth ||
                 nativeHeight != _nativeHeight;

  _placementId = placementId;
  _nativeWidth = nativeWidth;
  _nativeHeight = nativeHeight;

  if (changed) {
    [self setupIfReady];
  }

  [super updateProps:props oldProps:oldProps];
}

- (void)handleCommand:(const NSString *)commandName args:(const NSArray *)args {
  RCTAdpopcornRewardNativeAdHandleCommand(self, commandName, args);
}

- (void)prepareForRecycle {
  [super prepareForRecycle];
  [self stopAdInternal];
  _placementId = nil;
  _nativeWidth = 0;
  _nativeHeight = 0;
}

#pragma mark - RCTAdpopcornRewardNativeAdViewProtocol (Commands)

- (void)loadAd {
  if (_nativeAd == nil) {
    [self setupIfReady];
  } else {
    [_nativeAd loadAd];
  }
}

- (void)stopAd {
  [self stopAdInternal];
}

#pragma mark - Internal

- (void)stopAdInternal {
  if (_nativeAd != nil) {
    [_nativeAd removeFromSuperview];
    _nativeAd.delegate = nil;
    _nativeAd = nil;
  }
}

- (void)setupIfReady {
  if (_placementId.length == 0 || _nativeWidth <= 0 || _nativeHeight <= 0) {
    return;
  }
  [self stopAdInternal];

  CGRect frame = CGRectMake(0, 0, _nativeWidth, _nativeHeight);
  UIViewController *vc = [self presentingViewController];

  _nativeAd = [[AdPopcornRewardNativeAd alloc] initWithFrame:frame viewController:vc];
  _nativeAd.placementId = _placementId;
  _nativeAd.delegate = self;
  [self addSubview:_nativeAd];

  __weak __typeof(self) weakSelf = self;
  dispatch_async(dispatch_get_main_queue(), ^{
    [weakSelf->_nativeAd loadAd];
  });
}

- (UIViewController *)presentingViewController {
  UIViewController *vc = self.window.rootViewController;
  if (vc != nil) {
    return vc;
  }
  if (@available(iOS 13.0, *)) {
    for (UIScene *scene in [UIApplication sharedApplication].connectedScenes) {
      if (scene.activationState == UISceneActivationStateForegroundActive &&
          [scene isKindOfClass:[UIWindowScene class]]) {
        UIWindowScene *windowScene = (UIWindowScene *)scene;
        if (windowScene.keyWindow != nil) {
          return windowScene.keyWindow.rootViewController;
        }
      }
    }
  }
  return nil;
}

#pragma mark - AdPopcornRewardNativeAdDelegate

- (void)ApRewardNativeAdLoadSuccess {
  if (_eventEmitter == nullptr) return;
  std::static_pointer_cast<const AdpopcornRewardNativeAdEventEmitter>(_eventEmitter)
      ->onLoadSuccess({});
}

- (void)ApRewardNativeAdLoadFailed:(NSInteger)errorCode {
  if (_eventEmitter == nullptr) return;
  std::static_pointer_cast<const AdpopcornRewardNativeAdEventEmitter>(_eventEmitter)
      ->onLoadFailed({.errorCode = static_cast<int>(errorCode)});
}

- (void)ApRewardNativeAdClicked {
  if (_eventEmitter == nullptr) return;
  std::static_pointer_cast<const AdpopcornRewardNativeAdEventEmitter>(_eventEmitter)
      ->onClicked({});
}

- (void)ApRewardNativeAdCompleted {
  if (_eventEmitter == nullptr) return;
  std::static_pointer_cast<const AdpopcornRewardNativeAdEventEmitter>(_eventEmitter)
      ->onCompleted({});
}

@end

Class<RCTComponentViewProtocol> AdpopcornRewardNativeAdCls(void) {
  return AdpopcornRewardNativeAdComponentView.class;
}
