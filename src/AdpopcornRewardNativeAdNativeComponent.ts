import * as React from "react";
import {
  codegenNativeCommands,
  codegenNativeComponent,
  type CodegenTypes,
  type HostComponent,
  type ViewProps,
} from "react-native";

interface LoadFailedEvent {
  errorCode: CodegenTypes.Int32;
}

export interface NativeProps extends ViewProps {
  placementId: string;
  nativeWidth: CodegenTypes.Int32; // dp
  nativeHeight: CodegenTypes.Int32; // dp
  onLoadSuccess?: CodegenTypes.DirectEventHandler<null>;
  onLoadFailed?: CodegenTypes.DirectEventHandler<LoadFailedEvent>;
  onClicked?: CodegenTypes.DirectEventHandler<null>;
  onCompleted?: CodegenTypes.DirectEventHandler<null>;
}

export type AdpopcornRewardNativeAdType = HostComponent<NativeProps>;

interface NativeCommands {
  loadAd: (viewRef: React.ComponentRef<AdpopcornRewardNativeAdType>) => void;
  stopAd: (viewRef: React.ComponentRef<AdpopcornRewardNativeAdType>) => void;
}

export const Commands: NativeCommands = codegenNativeCommands<NativeCommands>({
  supportedCommands: ["loadAd", "stopAd"],
});

export default codegenNativeComponent<NativeProps>(
  "AdpopcornRewardNativeAd",
) as AdpopcornRewardNativeAdType;
