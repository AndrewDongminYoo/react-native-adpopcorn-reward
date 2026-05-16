/**
 * AdPopcornRewardNativeAd React Native 래퍼 컴포넌트.
 *
 * @example
 * ```tsx
 * <AdPopcornRewardNativeAd
 *   placementId="YOUR_PLACEMENT_ID"
 *   nativeWidth={320}
 *   nativeHeight={250}
 *   onLoadSuccess={() => console.log('loaded')}
 *   onLoadFailed={(e) => console.log('failed', e.nativeEvent.errorCode)}
 *   onClicked={() => console.log('clicked')}
 *   onCompleted={() => console.log('completed')}
 * />
 * ```
 */

import React, {forwardRef, useImperativeHandle, useRef} from 'react';
import {
  requireNativeComponent,
  UIManager,
  findNodeHandle,
  NativeSyntheticEvent,
  NativeModules,
  ViewStyle,
  Platform,
} from 'react-native';

/** onLoadFailed 이벤트 데이터 */
interface LoadFailedEvent {
  errorCode: number;
}

/** AdPopcornRewardNativeAd 컴포넌트 Props */
export interface AdPopcornRewardNativeAdProps {
  /** 광고 지면 ID (필수) */
  placementId: string;
  /** 네이티브 지면 너비 dp (필수) */
  nativeWidth: number;
  /** 네이티브 지면 높이 dp (필수) */
  nativeHeight: number;
  /** 광고 로딩 성공 콜백 */
  onLoadSuccess?: () => void;
  /** 광고 로딩 실패 콜백 */
  onLoadFailed?: (event: NativeSyntheticEvent<LoadFailedEvent>) => void;
  /** 광고 클릭 콜백 */
  onClicked?: () => void;
  /** 광고 완료 콜백 */
  onCompleted?: () => void;
  /** 추가 스타일 */
  style?: ViewStyle;
}

/** ref를 통한 수동 제어 메서드 */
export interface AdPopcornRewardNativeAdRef {
  /** 광고 수동 로드 */
  loadAd: () => void;
  /** 광고 수동 중지 */
  stopAd: () => void;
}

const NATIVE_COMPONENT_NAME = 'RNAdPopcornRewardNativeAd';
const NativeAdView = requireNativeComponent<any>(NATIVE_COMPONENT_NAME);

const AdPopcornRewardNativeAd = forwardRef<
  AdPopcornRewardNativeAdRef,
  AdPopcornRewardNativeAdProps
>((props, ref) => {
  const {placementId, nativeWidth, nativeHeight, onLoadSuccess, onLoadFailed, onClicked, onCompleted, style} = props;
  const nativeRef = useRef(null);

  const dispatchCommand = (command: string) => {
    const handle = findNodeHandle(nativeRef.current);
    if (!handle) return;

    if (Platform.OS === 'ios') {
      // iOS: RCT_EXPORT_METHOD 방식
      NativeModules.RNAdPopcornRewardNativeAd?.[command](handle);
    } else {
      // Android: dispatchViewManagerCommand 방식
      const commands = UIManager.getViewManagerConfig(NATIVE_COMPONENT_NAME)?.Commands;
      const cmdId = commands?.[command] ?? (command === 'loadAd' ? 1 : 2);
      UIManager.dispatchViewManagerCommand(handle, cmdId, []);
    }
  };

  useImperativeHandle(ref, () => ({
    loadAd: () => dispatchCommand('loadAd'),
    stopAd: () => dispatchCommand('stopAd'),
  }));

  return (
    <NativeAdView
      ref={nativeRef}
      placementId={placementId}
      nativeWidth={nativeWidth}
      nativeHeight={nativeHeight}
      onLoadSuccess={onLoadSuccess}
      onLoadFailed={onLoadFailed}
      onClicked={onClicked}
      onCompleted={onCompleted}
      style={[{width: nativeWidth, height: nativeHeight}, style]}
    />
  );
});

AdPopcornRewardNativeAd.displayName = 'AdPopcornRewardNativeAd';

export default AdPopcornRewardNativeAd;
