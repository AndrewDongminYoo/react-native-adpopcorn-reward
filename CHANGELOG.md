# Changelog

## 2.0.0

Full rewrite on React Native's New Architecture (TurboModule + Fabric).

### Breaking changes

- **New Architecture is required.** The package no longer ships a legacy Bridge implementation. Host apps must run with `newArchEnabled=true`. `TurboModuleRegistry.getEnforcing` throws at startup otherwise.
- **Reward-info queries return `Promise` instead of firing events.**
  - `getOfferwallTotalRewardInfo()` now returns `Promise<OfferwallTotalRewardInfo>`. The legacy `OnOfferwallTotalRewardInfo` event is removed.
  - `getBridgeTotalRewardInfo(bridgePlacementId)` now returns `Promise<BridgeTotalRewardInfo>`. The legacy `OnBridgeTotalRewardInfo` event is removed.
  - A concurrent call to either query (with the same bridge placement id, in the bridge case) rejects with code `E_INFLIGHT`.
- **`addListener` only accepts two event names**: `OnClosedOfferWallPage` and `OnCompletedCampaign`. Passing any other string is a TypeScript error (`AdPopcornRewardEventName` union) and a runtime exception. The legacy `AdPopcornRewardEvents` constant has been pruned to match.
- **iOS minimum deployment target** is `12.0` (unchanged), but Fabric requires Xcode 15+ and iOS 13+ at runtime to use the offerwall.
- **Android `minSdkVersion`** is `24`, `compileSdk`/`targetSdk` `36`, AGP `8.7.2`, Kotlin `2.0.21`.

### Migration

| 1.x                                                             | 2.0                                                                                                          |
| --------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------ |
| `AdPopcornReward.addListener("OnOfferwallTotalRewardInfo", cb)` | `const info = await AdPopcornReward.getOfferwallTotalRewardInfo();`                                          |
| `AdPopcornReward.addListener("OnBridgeTotalRewardInfo", cb)`    | `const info = await AdPopcornReward.getBridgeTotalRewardInfo(placementId);`                                  |
| `AdPopcornReward.addListener("OnClosedOfferWallPage", cb)`      | unchanged                                                                                                    |
| `AdPopcornReward.addListener("OnCompletedCampaign", cb)`        | unchanged                                                                                                    |
| `<AdPopcornRewardNativeAd ref={r} />`, `r.current.loadAd()`     | unchanged surface; under the hood the `Platform.OS` branch is gone (Fabric handles both platforms uniformly) |

### Platform asymmetry

The iOS/Android `setAppKey`/`setLogEnable` asymmetry is preserved from 1.x:

- iOS configures `appKey` / `hashKey` at runtime via `setAppKey(appKey, hashKey)`.
- Android requires the same values via `AndroidManifest.xml` `<meta-data>` entries (`com.igaworks.adpopcorn.cores.common.APAppKey` and `APHashKey`). `setAppKey` / `setLogEnable` are documented no-ops on Android and log a warning to `Log.w`.

### Native SDK versions

Unchanged versus 1.x:

- iOS: `AdPopcornOfferwall` `5.2.3` (CocoaPods)
- Android: `com.igaworks.offerwall:AdPopcornOfferwall` `9.2.6`

## 1.0.0

Initial release on the legacy Bridge architecture.
