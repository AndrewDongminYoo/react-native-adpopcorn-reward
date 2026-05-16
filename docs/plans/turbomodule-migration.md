# TurboModule Migration Plan

Status: **draft / pre-implementation**
Target: restore the full legacy AdPopcorn Offerwall API on the New Architecture
(TurboModule + Fabric) without churning the published JS surface.

## Decisions (locked before Phase 1)

| #   | Decision                                                                                                                                                                                                                                                                                                                                                                    | Rationale                                                                                                                                    |
| --- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------- |
| D1  | **Native module name** = `AdpopcornReward` (scaffold default). **Codegen spec** = `AdpopcornRewardSpec`. **Fabric component** = `AdpopcornRewardNativeAd`.                                                                                                                                                                                                                  | Scaffold already committed to these. The rename is invisible to package consumers because we keep the JS wrapper identical (D2).             |
| D2  | **JS public API is method-level back-compat** with legacy `src/index.ts` — same default export `AdPopcornReward` with the same method names/arities, same named export `AdPopcornRewardNativeAd`, same named export `AdPopcornRewardEvents`. **Event constants are _not_ byte-compat — see D3** which removes `OnOfferwallTotalRewardInfo` / `OnBridgeTotalRewardInfo`.     | Avoids forcing consumers (the existing 영끌 app) to touch their method imports; the dropped event names are a known, scoped breaking change. |
| D3  | **Reward-info queries return `Promise`**, not events. `getOfferwallTotalRewardInfo()` → `Promise<{queryResult, totalCount, totalReward}>`. `getBridgeTotalRewardInfo(id)` → `Promise<{queryResult, totalCount, totalReward, bridgePlacementId}>`. Existing legacy event names `OnOfferwallTotalRewardInfo` / `OnBridgeTotalRewardInfo` are **dropped** from the public API. | Single source of truth, idiomatic for codegen, simpler types. Migration cost is one `.then()` per call site, acceptable.                     |
| D4  | **Only `OnClosedOfferWallPage` and `OnCompletedCampaign` remain as events.** These are pure delegate callbacks with no caller, so a Promise model does not fit.                                                                                                                                                                                                             | Matches the legacy delegate semantics.                                                                                                       |
| D5  | **Event transport** uses the codegen `EventEmitter<T>` pattern available in RN 0.76+ (we are on 0.85). Event names are exposed verbatim to JS via `NativeEventEmitter(NativeAdpopcornReward)` for back-compat with legacy listener registration.                                                                                                                            | Strongly typed on the native side, untyped event names on JS side preserved.                                                                 |
| D6  | **No Paper / legacy bridge fallback** — Fabric and TurboModule only. `getEnforcing` in the spec enforces this at runtime; the example app must run with `newArchEnabled=true`.                                                                                                                                                                                              | The repo is already opted in (`TurboModuleRegistry.getEnforcing`).                                                                           |
| D7  | **iOS appKey/hashKey is runtime; Android is `AndroidManifest.xml` meta-data.** TurboModule spec keeps the symmetric `setAppKey(appKey, hashKey)` shape, but the Android impl is a documented no-op with a `Log.w` reminder. README + the spec's JSDoc must call this out.                                                                                                   | Identical to legacy behavior; do not paper over an SDK reality with a synthetic implementation.                                              |
| D8  | **Native AdPopcorn SDK versions are unchanged for this migration**: iOS `AdPopcornOfferwall 5.2.3`, Android `com.igaworks.offerwall:AdPopcornOfferwall:9.2.6`. Version bumps are a separate task.                                                                                                                                                                           | Keeps surface of change limited; SDK upgrade has its own integration risk.                                                                   |

## Phase 0 — Codegen scope + spec scaffolding (~ 0.5 day)

- [ ] `package.json` → `codegenConfig.type`: change `"modules"` → `"all"`. Required because we will codegen both the TurboModule and the Fabric component.
- [ ] Add ios `s.platforms` / android `compileSdk` review in `AdpopcornReward.podspec` and `android/build.gradle` against the legacy podspec — no expected change but verify.
- [ ] Build a smoke run of the example app _before_ writing any spec code, to confirm the current placeholder `multiply` codegen path is healthy. If `pod install` or gradle fails on the bare scaffold, fix that first — debugging codegen with new code on top is much harder.

## Phase 1 — TurboModule: `AdpopcornReward` (~ 1 day)

### 1.1 Spec — `src/NativeAdpopcornReward.ts`

Replace the `multiply` placeholder. Final shape:

```ts
import { TurboModuleRegistry, type TurboModule } from "react-native";
import type { EventEmitter } from "react-native/Libraries/Types/CodegenTypes";

export interface OfferwallTotalRewardInfo {
  queryResult: boolean;
  totalCount: number;
  totalReward: string;
}

export interface BridgeTotalRewardInfo extends OfferwallTotalRewardInfo {
  bridgePlacementId: string;
}

export interface Spec extends TurboModule {
  setAppKey(appKey: string, hashKey: string): void; // iOS: runtime; Android: no-op (see D7)
  setLogEnable(enable: boolean): void; // iOS only; Android: no-op
  setUserId(userId: string): void;
  setStyle(offerwallTitle: string, mainOfferwallColorHex: string): void;
  openOfferwall(): void;
  openBridge(bridgePlacementId: string): void;
  openCSPage(): void;
  getOfferwallTotalRewardInfo(): Promise<OfferwallTotalRewardInfo>;
  getBridgeTotalRewardInfo(bridgePlacementId: string): Promise<BridgeTotalRewardInfo>;

  readonly onClosedOfferWallPage: EventEmitter<void>;
  readonly onCompletedCampaign: EventEmitter<void>;
}

export default TurboModuleRegistry.getEnforcing<Spec>("AdpopcornReward");
```

- [ ] Confirm `EventEmitter<T>` is wired in RN 0.85 codegen (it is, but verify generated headers actually expose `emitOnClosedOfferWallPage()` etc. on the iOS spec and `emitOnClosedOfferWallPage()` on the Android spec — if not, fall back to D5's `DeviceEventManagerModule` route).
- [ ] Add the legacy event name constants (`OnClosedOfferWallPage`, `OnCompletedCampaign`) to `src/index.tsx` as exported strings (`AdPopcornRewardEvents`). They map onto the codegen-emitted names; the wrapper translates if codegen names differ.

### 1.2 iOS — `ios/AdpopcornReward.{h,mm}`

Entry point class: `AdpopcornReward : NSObject <NativeAdpopcornRewardSpec>`.

- [ ] Port main-queue dispatch pattern from legacy (every `[AdPopcornOfferwall …]` call is `dispatch_async(dispatch_get_main_queue(), …)` — see legacy `RNAdPopcornRewardModule.m`).
- [ ] Port `APActiveWindow()` helper and `colorFromHexString:` helper as static functions in the `.mm`.
- [ ] Implement `AdPopcornOfferwallDelegate` on the new class; bridge `didCloseOfferWall` / `onCompletedCampaign` to codegen `emitOnClosedOfferWallPage` / `emitOnCompletedCampaign` (or fallback per Phase 1.1 verification).
- [ ] `getOfferwallTotalRewardInfo` / `getBridgeTotalRewardInfo` must store the `RCTPromiseResolveBlock` / `RCTPromiseRejectBlock` (or codegen equivalent) and resolve from `offerwallTotalRewardInfo:totalCount:totalReward:` / `bridgeTotalRewardInfo:...`. Watch out for multiple in-flight calls — store as a queue keyed by `bridgePlacementId` for the bridge variant, or document that overlapping calls are unsupported and reject the second.
- [ ] `+ moduleName` returns `@"AdpopcornReward"`.

### 1.3 Android — `android/.../AdpopcornRewardModule.kt`

Already extends `NativeAdpopcornRewardSpec`. Port:

- [ ] All `@ReactMethod` bodies from legacy `RNAdPopcornRewardModule.java`, converted to Kotlin overrides of the generated abstract methods.
- [ ] `IAdPOPcornEventListener` implementation: emit through the codegen emitter; if codegen emitters are absent for Android (Kotlin), fall back to `reactApplicationContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java).emit(...)` with the legacy event names.
- [ ] `setAppKey` / `setLogEnable` are no-ops: `Log.w(TAG, "setAppKey() is iOS-only; configure via AndroidManifest meta-data")`.
- [ ] Promise plumbing: convert legacy `IAPRewardInfoCallbackListener` callbacks to resolve the codegen-generated Promise. Same overlap concern as iOS — pick one policy and document it.
- [ ] `AdpopcornRewardPackage.kt` is already correct; no change needed.

## Phase 2 — Fabric Native Component: `AdpopcornRewardNativeAd` (~ 1 day)

### 2.1 Spec — `src/AdpopcornRewardNativeAdNativeComponent.ts`

```ts
import codegenNativeCommands from "react-native/Libraries/Utilities/codegenNativeCommands";
import codegenNativeComponent from "react-native/Libraries/Utilities/codegenNativeComponent";
import type { HostComponent, ViewProps } from "react-native";
import type { DirectEventHandler, Int32 } from "react-native/Libraries/Types/CodegenTypes";

interface LoadFailedEvent {
  errorCode: Int32;
}

export interface NativeProps extends ViewProps {
  placementId: string;
  nativeWidth: Int32; // dp
  nativeHeight: Int32; // dp
  onLoadSuccess?: DirectEventHandler<null>;
  onLoadFailed?: DirectEventHandler<LoadFailedEvent>;
  onClicked?: DirectEventHandler<null>;
  onCompleted?: DirectEventHandler<null>;
}

interface NativeCommands {
  loadAd: (viewRef: React.ElementRef<HostComponent<NativeProps>>) => void;
  stopAd: (viewRef: React.ElementRef<HostComponent<NativeProps>>) => void;
}

export const Commands: NativeCommands = codegenNativeCommands<NativeCommands>({
  supportedCommands: ["loadAd", "stopAd"],
});

export default codegenNativeComponent<NativeProps>("AdpopcornRewardNativeAd");
```

### 2.2 iOS Fabric — `ios/AdpopcornRewardNativeAdComponentView.{h,mm}`

- [ ] Inherit `RCTViewComponentView`. Implement `+ (ComponentDescriptorProvider)componentDescriptorProvider` returning the codegen-provided descriptor (`AdpopcornRewardNativeAdComponentDescriptor`).
- [ ] Override `-updateProps:oldProps:` and diff `placementId` / `nativeWidth` / `nativeHeight`. Reuse the legacy `setupIfReady` semantics: only (re)build the underlying `AdPopcornRewardNativeAd` when all three are set, and tear down/rebuild on change.
- [ ] Implement `-handleCommand:args:` to dispatch `loadAd` / `stopAd`.
- [ ] Implement `AdPopcornRewardNativeAdDelegate`. Codegen events: call `static_cast<const AdpopcornRewardNativeAdEventEmitter &>(*_eventEmitter).onLoadSuccess({})` etc. — note these are C++ event emitters under Fabric, not the ObjC block style of legacy.
- [ ] `dealloc` must call the underlying SDK teardown (legacy did this).

### 2.3 Android Fabric — `android/.../AdpopcornRewardNativeAdViewManager.kt`

- [ ] Extend `SimpleViewManager<AdpopcornRewardNativeAdView>` AND implement the codegen-generated `AdpopcornRewardNativeAdManagerInterface<AdpopcornRewardNativeAdView>`. Delegate via the generated `AdpopcornRewardNativeAdManagerDelegate`.
- [ ] `getName()` returns `"AdpopcornRewardNativeAd"`.
- [ ] Port the legacy view class to Kotlin (`AdpopcornRewardNativeAdView`) — same `ReactViewGroup` base, same `Choreographer` loadAd pattern, same `dpToPx` helper.
- [ ] Events: emit via the generated `OnLoadSuccessEvent` / etc. classes (codegen produces them under `com.facebook.react.viewmanagers.AdpopcornRewardNativeAdManagerDelegate$Event*`). If codegen path is too brittle on first attempt, fall back to `UIManagerHelper.getEventDispatcherForReactTag(...).dispatchEvent(...)` with custom `Event` subclasses.
- [ ] `receiveCommand` is handled by the codegen delegate — just override `loadAd` / `stopAd` on the manager.
- [ ] Register the new ViewManager in `AdpopcornRewardPackage.kt` (`createViewManagers` — currently `BaseReactPackage` returns no view managers, will need to add).

## Phase 3 — JS API restoration (~ 0.5 day)

- [ ] Delete `src/multiply.tsx`, `src/multiply.native.tsx`.
- [ ] Replace `src/index.tsx` with the legacy-equivalent wrapper:
  - default export `AdPopcornReward` — re-export of TurboModule methods, but with method-name preservation (`setAppKey`, `openOfferwall`, etc.) and the rewards-info functions returning Promises (D3).
  - `addListener(eventName, callback)` — implemented on top of `new NativeEventEmitter(NativeAdpopcornReward)`. Accepts the legacy event names as strings. Maps to whichever underlying transport phase 1 chose (codegen emitter vs `DeviceEventEmitter`).
  - named exports: `AdPopcornRewardNativeAd` (component), `AdPopcornRewardNativeAdProps`, `AdPopcornRewardNativeAdRef`, `AdPopcornRewardEvents`.
  - `AdPopcornRewardEvents` keeps `OnClosedOfferWallPage` and `OnCompletedCampaign`. `OnOfferwallTotalRewardInfo` / `OnBridgeTotalRewardInfo` are removed per D3 — call out as breaking change in changelog.
- [ ] `AdPopcornRewardNativeAd` component wraps the codegen `HostComponent<NativeProps>` and exposes the legacy ref-based `loadAd()` / `stopAd()` via `useImperativeHandle` calling `Commands.loadAd(ref)` / `Commands.stopAd(ref)` (no `Platform.OS` branching needed — Fabric handles both).
- [ ] Update `example/src/App.tsx` to demonstrate the actual API: a button to open the offerwall, a button to query reward info (Promise), and the `<AdPopcornRewardNativeAd>` rendered with valid placement id from env.

## Phase 4 — Verification

Order matters — start cheap, end expensive.

- [ ] `yarn typecheck` — must pass cleanly with the new spec types.
- [ ] `yarn lint` — flat config will catch quote/semicolon drift quickly.
- [ ] `yarn test` — keep the existing `it.todo` for now; add one shape-sanity test that imports the wrapper and asserts the public API surface (`typeof AdPopcornReward.openOfferwall === "function"` etc.) so accidental renames during refactor are caught. **This is not a behavioral test** — that lives in the example app.
- [ ] `yarn example ios` — verify Metro logs show `"fabric":true,"concurrentRoot":true`. Open the offerwall. Check `OnClosedOfferWallPage` listener fires. Render `<AdPopcornRewardNativeAd>` with a real placement id, confirm `onLoadSuccess` fires.
- [ ] `yarn example android` — same checks. Confirm `setAppKey` no-op behavior (logs the warning), confirm AndroidManifest path works.
- [ ] `yarn prepare` — bob must produce valid ESM + types. Sanity-check `lib/typescript/src/index.d.ts` has the legacy-equivalent public surface.

## Phase 5 — Wrap

- [ ] Update `README.md` usage section with the real API (replace the `multiply` example). Include the iOS/Android appKey asymmetry from D7.
- [ ] Bump `package.json` version. Suggest **`1.0.0`** stays as-is if not yet published, or **`2.0.0`** if `1.0.0` was published with the legacy bridge implementation — Bridge → TurboModule is a hard breaking change for consumers (`newArchEnabled=true` becomes mandatory).
- [ ] Add a `CHANGELOG` (or release notes) entry listing: New Architecture required; `OnOfferwallTotalRewardInfo` / `OnBridgeTotalRewardInfo` events replaced by Promise return values; `addListener` only accepts the two remaining event names.

## Risks / unknowns to confirm during Phase 1

1. **RN 0.85 codegen `EventEmitter<T>` shape on Android Kotlin** — RN 0.76+ supports this on iOS reliably; Android Kotlin emitter support has shipped but is still less worn-in. If the generated base class lacks `emitOnClosedOfferWallPage`, fall back to `DeviceEventEmitter` and document it in the migration plan, do not let it block Phase 2.
2. **Pod transitive dependency** — `AdPopcornOfferwall 5.2.3` is a CocoaPods-only artifact. Confirm it resolves from the default CocoaPods spec repo on a clean machine; if it requires the IGAWorks private spec source, the podspec needs `s.dependency` plus a `Podfile`-level source declaration documented in README.
3. **AndroidManifest meta-data keys** — legacy Java module assumed `appKey` / `hashKey` are configured in the host app's manifest. Document the exact `<meta-data>` lines required (this was implicit in the legacy package and likely caused integration friction; surface it now).
4. **Overlapping reward-info calls** — Promise resolution requires either queuing or rejecting. Pick during Phase 1.2/1.3 implementation, document inline.

## Sequencing

Phases 1, 2, 3 can be **partially overlapped** if two people are working, but for a single-developer execution run them strictly in order. Phase 0's example-app smoke build is non-negotiable — it isolates "scaffold is broken" from "my new code is broken".
