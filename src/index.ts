// Phase 1: temporary re-export so codegen / typecheck pass.
// Phase 3 replaces this with the legacy-shaped wrapper (AdPopcornReward
// default export + AdPopcornRewardNativeAd + AdPopcornRewardEvents).
export { default } from "./NativeAdpopcornReward";
export type {
  BridgeTotalRewardInfo,
  OfferwallTotalRewardInfo,
} from "./NativeAdpopcornReward";
