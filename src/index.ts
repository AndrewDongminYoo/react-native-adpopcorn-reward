import type { EventSubscription } from "react-native";
import NativeAdpopcornReward from "./NativeAdpopcornReward";

export {
  AdPopcornRewardNativeAd,
  type AdPopcornRewardNativeAdProps,
  type AdPopcornRewardNativeAdRef,
  type LoadFailedEvent,
} from "./AdPopcornRewardNativeAd";

export type {
  BridgeTotalRewardInfo,
  OfferwallTotalRewardInfo,
} from "./NativeAdpopcornReward";

// Public event name constants. Note: legacy also exposed
// OnOfferwallTotalRewardInfo / OnBridgeTotalRewardInfo, but those are removed
// in this migration — the corresponding queries are now Promise-returning.
export const AdPopcornRewardEvents = {
  OnClosedOfferWallPage: "OnClosedOfferWallPage",
  OnCompletedCampaign: "OnCompletedCampaign",
} as const;

export type AdPopcornRewardEventName =
  (typeof AdPopcornRewardEvents)[keyof typeof AdPopcornRewardEvents];

const AdPopcornReward = {
  /**
   * iOS only — sets the SDK appKey/hashKey at runtime.
   * Android no-op: configure via AndroidManifest meta-data
   * (com.igaworks.adpopcorn.cores.common.APAppKey / APHashKey).
   */
  setAppKey(appKey: string, hashKey: string): void {
    NativeAdpopcornReward.setAppKey(appKey, hashKey);
  },

  /**
   * iOS only — toggles AdPopcornOfferwallLogTrace.
   * Android no-op: the native SDK reads its log flag from manifest meta-data.
   */
  setLogEnable(enable: boolean): void {
    NativeAdpopcornReward.setLogEnable(enable);
  },

  setUserId(userId: string): void {
    NativeAdpopcornReward.setUserId(userId);
  },

  setStyle(offerwallTitle: string, mainOfferwallColorHex: string): void {
    NativeAdpopcornReward.setStyle(offerwallTitle, mainOfferwallColorHex);
  },

  openOfferwall(): void {
    NativeAdpopcornReward.openOfferwall();
  },

  openBridge(bridgePlacementId: string): void {
    NativeAdpopcornReward.openBridge(bridgePlacementId);
  },

  openCSPage(): void {
    NativeAdpopcornReward.openCSPage();
  },

  /**
   * Resolves with the total earnable reward across the offerwall.
   * Rejects with `E_INFLIGHT` if another query is already pending.
   */
  getOfferwallTotalRewardInfo() {
    return NativeAdpopcornReward.getOfferwallTotalRewardInfo();
  },

  /**
   * Resolves with the total earnable reward for the given bridge placement.
   * Rejects with `E_INFLIGHT` if another query for the same placement is
   * already pending.
   */
  getBridgeTotalRewardInfo(bridgePlacementId: string) {
    return NativeAdpopcornReward.getBridgeTotalRewardInfo(bridgePlacementId);
  },

  /**
   * Registers a callback for one of the two supported event names. The
   * returned subscription must be cleaned up with `.remove()`.
   *
   * Legacy callers passed `OnOfferwallTotalRewardInfo` /
   * `OnBridgeTotalRewardInfo` here — those are no longer events, use the
   * Promise-returning queries above instead.
   */
  addListener(
    eventName: AdPopcornRewardEventName,
    callback: () => void,
  ): EventSubscription {
    switch (eventName) {
      case AdPopcornRewardEvents.OnClosedOfferWallPage:
        return NativeAdpopcornReward.onClosedOfferWallPage(callback);
      case AdPopcornRewardEvents.OnCompletedCampaign:
        return NativeAdpopcornReward.onCompletedCampaign(callback);
      default: {
        const exhaustive: never = eventName;
        throw new Error(`unknown AdPopcornReward event: ${String(exhaustive)}`);
      }
    }
  },
};

export default AdPopcornReward;
