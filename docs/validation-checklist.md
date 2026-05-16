# Runtime Validation Checklist

The migration's automated gates (typecheck, lint, jest, codegen, `yarn example build:ios`, `yarn example build:android`, install + launch smoke) only prove the package builds, registers, and loads without crashing. **Functional SDK behaviour requires a live device with real AdPopcorn credentials.**

Run through this checklist once you have an `appKey` / `hashKey` from the AdPopcorn dashboard.

## Preconditions

1. Replace the placeholders at the top of `example/src/App.tsx`:

   ```ts
   const APP_KEY = "YOUR_APP_KEY";
   const HASH_KEY = "YOUR_HASH_KEY";
   const NATIVE_AD_PLACEMENT_ID = "YOUR_NATIVE_AD_PLACEMENT";
   const USER_ID = "demo-user";
   ```

2. On Android, add the AdPopcorn meta-data to `example/android/app/src/main/AndroidManifest.xml` (or to your host app's manifest). `setAppKey` is intentionally a no-op on Android — the native SDK reads these at startup:

   ```xml
   <application ...>
     <meta-data android:name="com.igaworks.adpopcorn.cores.common.APAppKey"   android:value="YOUR_APP_KEY"   />
     <meta-data android:name="com.igaworks.adpopcorn.cores.common.APHashKey"  android:value="YOUR_HASH_KEY"  />
   </application>
   ```

3. Start Metro in one terminal:
   ```sh
   yarn example start
   ```
   Reload the app on the device once Metro is up.

## iOS validation

Run on a booted simulator or device:

```sh
yarn example ios
```

Watch the Metro log for `"fabric":true,"concurrentRoot":true`. Then exercise each surface:

| Action                         | Expected                                | Validates                                                                  |
| ------------------------------ | --------------------------------------- | -------------------------------------------------------------------------- |
| App opens, no red box          | "status: idle" visible                  | TurboModule registration, JS bundle loads                                  |
| Tap **Open offerwall**         | Offerwall web view appears, dismissable | `openOfferwall` → SDK delegate                                             |
| Dismiss offerwall              | "status: offerwall closed"              | `EventEmitter<void>.onClosedOfferWallPage`                                 |
| Complete a campaign            | "status: campaign completed"            | `EventEmitter<void>.onCompletedCampaign`                                   |
| Tap **Query reward info**      | "status: reward N / X"                  | Promise resolution via delegate callback                                   |
| Native ad area renders content | "status: native ad loaded"              | Fabric ComponentView, `AdPopcornRewardNativeAdEventEmitter::onLoadSuccess` |
| Tap ad surface                 | "status: native ad clicked"             | `onClicked` direct event                                                   |
| Tap **loadAd** / **stopAd**    | ad reloads / disappears                 | `Commands.loadAd` / `Commands.stopAd`                                      |

Crashes or red boxes at any step are regressions — capture the stack and file an issue.

## Android validation

```sh
yarn example android
```

Same matrix as iOS. Plus one Android-only check:

| Action                               | Expected                                                                                                                                                                                |
| ------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| App start without manifest meta-data | `setAppKey()` logs `W AdpopcornRewardModule: setAppKey() is a no-op on Android. Configure appKey/hashKey via AndroidManifest meta-data ...` and offerwall returns an error from the SDK |
| App start with meta-data set         | offerwall opens normally                                                                                                                                                                |

## Known expected behaviour

- **`[runtime not ready]: Invariant Violation: TurboModuleRegistry.getEnforcing(...): 'PlatformConstants' could not be found`** in logcat / iOS console _when Metro is not running_: this is the expected dev-build failure path when no JS bundle is reachable. It is not a missing module. Start Metro and reload — the error disappears.
- **`onWindowFocusChange while context is not ready`** soft exception: same cause, harmless.
- **iOS `RCTRootView is deprecated`** warning during build: noise from React core headers, not from this library.
- **iOS `NSLocationWhenInUseUsageDescription must be a non-empty string`** warning: example app's `Info.plist` placeholder — set it to a non-empty string if you exercise location-dependent flows.
- **Android NDK `[CXX5304] This version only understands SDK XML versions up to 3 but an SDK XML file of version 4`**: cmdline-tools / Android Studio NDK metadata mismatch on the build host. Cosmetic. Align `sdkmanager --update` if it bothers you.

## Single-flight reward-query policy

`getOfferwallTotalRewardInfo()` and `getBridgeTotalRewardInfo(id)` reject with `E_INFLIGHT` if a second call arrives while the first is still pending. Test by tapping **Query reward info** twice in rapid succession — the second call should hit the catch branch and `Alert.alert` an `E_INFLIGHT` message.

## What to report after validation

When all eight rows of the matrix pass on both platforms with real credentials, the migration is complete. Open an issue if any row fails — include:

- the platform
- the matrix row that failed
- the relevant logcat / iOS console excerpt
- the AdPopcorn SDK version (`5.2.3` iOS / `9.2.6` Android per the current podspec / build.gradle)
