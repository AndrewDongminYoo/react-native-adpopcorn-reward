/**
 * react-native-adpopcorn-reward
 *
 * AdPopcorn Reward (Offerwall) SDK React Native Plugin
 */

import {NativeModules, NativeEventEmitter} from 'react-native';

export {default as AdPopcornRewardNativeAd} from './ads/adpopcornreward/AdPopcornRewardNativeAd';
export type {
  AdPopcornRewardNativeAdProps,
  AdPopcornRewardNativeAdRef,
} from './ads/adpopcornreward/AdPopcornRewardNativeAd';

const {RNAdPopcornRewardModule} = NativeModules;
const eventEmitter = new NativeEventEmitter(RNAdPopcornRewardModule);

/**
 * AdPopcornReward 모듈 API
 */
const AdPopcornReward = {
  /**
   * 앱키/해시키 설정 (필수, 최초 1회)
   * @param appKey 앱키
   * @param hashKey 해시키
   */
  setAppKey: (appKey: string, hashKey: string) =>
    RNAdPopcornRewardModule.setAppKey(appKey, hashKey),

  /**
   * 유저 식별자 설정
   * @param userId 유저 ID
   */
  setUserId: (userId: string) =>
    RNAdPopcornRewardModule.setUserId(userId),

  /**
   * 로그 활성화
   * @param enable 로그 활성화 여부
   */
  setLogEnable: (enable: boolean) =>
    RNAdPopcornRewardModule.setLogEnable(enable),

  /**
   * 오퍼월 스타일 설정
   * @param title 타이틀
   * @param color 색상 (hex)
   */
  setStyle: (title: string, color: string) =>
    RNAdPopcornRewardModule.setStyle(title, color),

  /** 오퍼월 열기 */
  openOfferwall: () => RNAdPopcornRewardModule.openOfferwall(),

  /**
   * 브릿지 오퍼월 열기
   * @param placementId 브릿지 지면 ID
   */
  openBridge: (placementId: string) =>
    RNAdPopcornRewardModule.openBridge(placementId),

  /** CS 페이지 열기 */
  openCSPage: () => RNAdPopcornRewardModule.openCSPage(),

  /** 오퍼월 리워드 정보 조회 */
  getOfferwallTotalRewardInfo: () =>
    RNAdPopcornRewardModule.getOfferwallTotalRewardInfo(),

  /**
   * 브릿지 리워드 정보 조회
   * @param placementId 브릿지 지면 ID
   */
  getBridgeTotalRewardInfo: (placementId: string) =>
    RNAdPopcornRewardModule.getBridgeTotalRewardInfo(placementId),

  /**
   * 이벤트 리스너 등록
   * @param eventName 이벤트 이름
   * @param callback 콜백 함수
   * @returns 리스너 구독 (remove()로 해제)
   */
  addListener: (eventName: string, callback: (event: any) => void) =>
    eventEmitter.addListener(eventName, callback),
};

/** 이벤트 이름 상수 */
export const AdPopcornRewardEvents = {
  OnClosedOfferWallPage: 'OnClosedOfferWallPage',
  OnCompletedCampaign: 'OnCompletedCampaign',
  OnOfferwallTotalRewardInfo: 'OnOfferwallTotalRewardInfo',
  OnBridgeTotalRewardInfo: 'OnBridgeTotalRewardInfo',
} as const;

export default AdPopcornReward;
