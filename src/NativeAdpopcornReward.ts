import {
  TurboModuleRegistry,
  type CodegenTypes,
  type TurboModule,
} from "react-native";

export interface OfferwallTotalRewardInfo {
  queryResult: boolean;
  totalCount: number;
  totalReward: string;
}

// Flat — do NOT use `extends OfferwallTotalRewardInfo`. RN codegen has been
// inconsistent at flattening interface inheritance into generated specs.
export interface BridgeTotalRewardInfo {
  queryResult: boolean;
  totalCount: number;
  totalReward: string;
  bridgePlacementId: string;
}

export interface Spec extends TurboModule {
  // iOS: configures the SDK at runtime.
  // Android: no-op — appKey/hashKey must be set via AndroidManifest meta-data.
  setAppKey(appKey: string, hashKey: string): void;

  // iOS: toggles AdPopcornOfferwallLogTrace.
  // Android: no-op — Android SDK reads its log flag from the manifest.
  setLogEnable(enable: boolean): void;

  setUserId(userId: string): void;
  setStyle(offerwallTitle: string, mainOfferwallColorHex: string): void;

  openOfferwall(): void;
  openBridge(bridgePlacementId: string): void;
  openCSPage(): void;

  getOfferwallTotalRewardInfo(): Promise<OfferwallTotalRewardInfo>;
  getBridgeTotalRewardInfo(
    bridgePlacementId: string,
  ): Promise<BridgeTotalRewardInfo>;

  readonly onClosedOfferWallPage: CodegenTypes.EventEmitter<void>;
  readonly onCompletedCampaign: CodegenTypes.EventEmitter<void>;
}

export default TurboModuleRegistry.getEnforcing<Spec>("AdpopcornReward");
