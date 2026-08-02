import * as React from "react";
import { forwardRef, useImperativeHandle, useRef } from "react";
import type { NativeSyntheticEvent, ViewStyle } from "react-native";

import AdpopcornRewardNativeAdNativeComponent, {
  Commands,
  type AdpopcornRewardNativeAdType,
} from "./AdpopcornRewardNativeAdNativeComponent";

export interface LoadFailedEvent {
  errorCode: number;
}

export interface AdPopcornRewardNativeAdProps {
  placementId: string;
  nativeWidth: number;
  nativeHeight: number;
  onLoadSuccess?: () => void;
  onLoadFailed?: (event: NativeSyntheticEvent<LoadFailedEvent>) => void;
  onClicked?: () => void;
  onCompleted?: () => void;
  style?: ViewStyle;
}

export interface AdPopcornRewardNativeAdRef {
  loadAd: () => void;
  stopAd: () => void;
}

export const AdPopcornRewardNativeAd = forwardRef<
  AdPopcornRewardNativeAdRef,
  AdPopcornRewardNativeAdProps
>(function AdPopcornRewardNativeAd(props, ref) {
  const {
    placementId,
    nativeWidth,
    nativeHeight,
    onLoadSuccess,
    onLoadFailed,
    onClicked,
    onCompleted,
    style,
  } = props;

  const nativeRef = useRef<React.ComponentRef<AdpopcornRewardNativeAdType>>(null);

  useImperativeHandle(ref, () => ({
    loadAd: () => {
      if (nativeRef.current) {
        Commands.loadAd(nativeRef.current);
      }
    },
    stopAd: () => {
      if (nativeRef.current) {
        Commands.stopAd(nativeRef.current);
      }
    },
  }));

  return (
    <AdpopcornRewardNativeAdNativeComponent
      ref={nativeRef}
      placementId={placementId}
      nativeWidth={nativeWidth}
      nativeHeight={nativeHeight}
      onLoadSuccess={onLoadSuccess ? () => onLoadSuccess() : undefined}
      onLoadFailed={onLoadFailed}
      onClicked={onClicked ? () => onClicked() : undefined}
      onCompleted={onCompleted ? () => onCompleted() : undefined}
      style={[{ width: nativeWidth, height: nativeHeight }, style]}
    />
  );
});
