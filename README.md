# react-native-adpopcorn-reward

AdPopcorn Reward (Offerwall) SDK React Native plugin. Implemented as a TurboModule + Fabric component on the New Architecture.

> 2.0 is a hard breaking change from 1.x — see [`CHANGELOG.md`](./CHANGELOG.md) before upgrading.

## Requirements

- React Native **0.76 or newer** (this repo targets 0.85).
- Host app must run with **the New Architecture enabled**. The module uses `TurboModuleRegistry.getEnforcing` and will crash at startup otherwise.
  - iOS: set `:fabric_enabled => true` or `ENV['RCT_NEW_ARCH_ENABLED'] = '1'` in `ios/Podfile` (RN 0.76+ defaults to enabled).
  - Android: `newArchEnabled=true` in `android/gradle.properties` (also enabled by default on the RN 0.76+ template).
- iOS deployment target ≥ 12.0, Xcode 15+.
- Android `minSdk` 24, `compileSdk` 36.
- You'll need an `appKey` / `hashKey` from the [AdPopcorn dashboard](https://www.adpopcorn.com/).

## Installation

```sh
npm install react-native-adpopcorn-reward
# or
yarn add react-native-adpopcorn-reward
```

### iOS

```sh
cd ios && pod install
```

The podspec pulls in `AdPopcornOfferwall 5.2.3` from CocoaPods trunk.

Configure the SDK at runtime — typically in your app entry point:

```ts
AdPopcornReward.setAppKey("YOUR_APP_KEY", "YOUR_HASH_KEY");
AdPopcornReward.setLogEnable(__DEV__);
```

### Android

Add the AdPopcorn meta-data to `android/app/src/main/AndroidManifest.xml`. Unlike iOS, the Android SDK reads these at startup — `setAppKey` / `setLogEnable` are intentional no-ops:

```xml
<application ...>
  <meta-data android:name="com.igaworks.adpopcorn.cores.common.APAppKey"  android:value="YOUR_APP_KEY"  />
  <meta-data android:name="com.igaworks.adpopcorn.cores.common.APHashKey" android:value="YOUR_HASH_KEY" />
</application>
```

The IGAWorks Maven repository is added by the library's own `android/build.gradle`, so no host-level repository configuration is needed.

## Usage

### Offerwall

```tsx
import AdPopcornReward, { AdPopcornRewardEvents } from "react-native-adpopcorn-reward";

useEffect(() => {
  AdPopcornReward.setUserId("user-123");

  const closed = AdPopcornReward.addListener(AdPopcornRewardEvents.OnClosedOfferWallPage, () =>
    console.log("offerwall closed")
  );
  const completed = AdPopcornReward.addListener(AdPopcornRewardEvents.OnCompletedCampaign, () =>
    console.log("campaign completed")
  );
  return () => {
    closed.remove();
    completed.remove();
  };
}, []);

const open = () => AdPopcornReward.openOfferwall();
const openBridge = () => AdPopcornReward.openBridge("BRIDGE_PLACEMENT_ID");
```

### Reward info queries (Promise)

```ts
const info = await AdPopcornReward.getOfferwallTotalRewardInfo();
// { queryResult, totalCount, totalReward }

const bridge = await AdPopcornReward.getBridgeTotalRewardInfo("BRIDGE_PLACEMENT_ID");
// { queryResult, totalCount, totalReward, bridgePlacementId }
```

A second call while the first is still pending rejects with code `E_INFLIGHT`. Catch it explicitly if you fire these queries in response to user input:

```ts
try {
  const info = await AdPopcornReward.getOfferwallTotalRewardInfo();
} catch (error) {
  // `error.code === "E_INFLIGHT"` means another query was already pending
}
```

### Native ad

```tsx
import {
  AdPopcornRewardNativeAd,
  type AdPopcornRewardNativeAdRef,
} from "react-native-adpopcorn-reward";

const ref = useRef<AdPopcornRewardNativeAdRef>(null);

<AdPopcornRewardNativeAd
  ref={ref}
  placementId="YOUR_PLACEMENT_ID"
  nativeWidth={320} // dp
  nativeHeight={250} // dp
  onLoadSuccess={() => {}}
  onLoadFailed={(e) => console.log(e.nativeEvent.errorCode)}
  onClicked={() => {}}
  onCompleted={() => {}}
/>;

// imperative
ref.current?.loadAd();
ref.current?.stopAd();
```

`AdPopcornRewardNativeAd` is a Fabric component — there is no `Platform.OS` branch needed for the imperative commands, unlike 1.x.

### Style

```ts
AdPopcornReward.setStyle("My offerwall title", "#1A8CFF");
```

Color codes must be 7-character `#RRGGBB` strings. Anything else is silently skipped on both platforms.

## API summary

```ts
import AdPopcornReward, {
  AdPopcornRewardEvents,
  AdPopcornRewardNativeAd,
  type AdPopcornRewardNativeAdRef,
  type OfferwallTotalRewardInfo,
  type BridgeTotalRewardInfo,
} from "react-native-adpopcorn-reward";

AdPopcornReward.setAppKey(appKey, hashKey); // iOS only; Android no-op (manifest meta-data)
AdPopcornReward.setLogEnable(enable); // iOS only; Android no-op
AdPopcornReward.setUserId(userId);
AdPopcornReward.setStyle(title, "#RRGGBB");
AdPopcornReward.openOfferwall();
AdPopcornReward.openBridge(bridgePlacementId);
AdPopcornReward.openCSPage();
AdPopcornReward.getOfferwallTotalRewardInfo(); // Promise<OfferwallTotalRewardInfo>
AdPopcornReward.getBridgeTotalRewardInfo(id); // Promise<BridgeTotalRewardInfo>
AdPopcornReward.addListener(name, callback); // EventSubscription
```

Only two event names are supported:

```ts
AdPopcornRewardEvents.OnClosedOfferWallPage; // "OnClosedOfferWallPage"
AdPopcornRewardEvents.OnCompletedCampaign; // "OnCompletedCampaign"
```

## Trying the example app

A working example app lives in `example/`. After cloning:

```sh
yarn
# fill in APP_KEY / HASH_KEY / NATIVE_AD_PLACEMENT_ID at the top of example/src/App.tsx
yarn example start   # Metro, in one terminal
yarn example ios     # in another, or
yarn example android
```

On Android, add the same appKey/hashKey to `example/android/app/src/main/AndroidManifest.xml` as `<meta-data>` entries (see [Installation › Android](#android) above) — `setAppKey()` is intentionally a no-op there.

## Troubleshooting

- **`TurboModuleRegistry.getEnforcing(...): 'AdpopcornReward' could not be found`** — the New Architecture is off. Re-check the [Requirements](#requirements) section.
- **Offerwall opens but returns immediately on Android** — `<meta-data>` for `APAppKey` / `APHashKey` is missing from your `AndroidManifest.xml`. The library logs `W AdpopcornRewardModule: setAppKey() is a no-op on Android. Configure appKey/hashKey via AndroidManifest meta-data ...` when you call `setAppKey()` — that warning is your reminder.
- **`E_INFLIGHT` from `getOfferwallTotalRewardInfo()` / `getBridgeTotalRewardInfo()`** — a previous query for the same scope is still pending. The library is single-flight per scope; queue your own calls, or await each one before issuing the next.
- **`[runtime not ready]: ... 'PlatformConstants' could not be found`** in logcat / iOS console — only seen when the app starts without Metro reachable (e.g. a debug build with no `yarn example start` running). Not caused by this library. Start Metro and reload.

## Validating an integration

See [`docs/validation-checklist.md`](./docs/validation-checklist.md) for a per-platform matrix of what to exercise once real AdPopcorn dashboard credentials are in place. The migration is considered complete only when both platforms pass that checklist against a real placement.

## Contributing

- [Development workflow](CONTRIBUTING.md#development-workflow)
- [Sending a pull request](CONTRIBUTING.md#sending-a-pull-request)
- [Code of conduct](CODE_OF_CONDUCT.md)

## License

MIT
