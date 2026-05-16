//
//  RNAdPopcornRewardModule.h
//  AdPopcornRewardPlugin
//
//  Created by Mick on 2023/06/07.
//
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <UIKit/UIKit.h>
#import <AdPopcornOfferwall/AdPopcornOfferwall.h>
#import <AdPopcornOfferwall/AdPopcornStyle.h>
#import <AdPopcornOfferwall/RewardInfo.h>

@interface RNAdPopcornRewardModule : RCTEventEmitter<RCTBridgeModule, AdPopcornOfferwallDelegate>
{
  
}

@end
