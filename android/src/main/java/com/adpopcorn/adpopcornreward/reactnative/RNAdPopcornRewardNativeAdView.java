package com.adpopcorn.adpopcornreward.reactnative;

import android.view.Choreographer;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.views.view.ReactViewGroup;

import com.igaworks.adpopcorn.nativead.AdPopcornRewardNativeAd;
import com.igaworks.adpopcorn.nativead.AdPopcornRewardNativeEventListener;

/**
 * AdPopcornRewardNativeAd를 감싸는 컨테이너 뷰.
 * ReactViewGroup을 상속하여 RN 레이아웃 시스템과 통합한다.
 */
public class RNAdPopcornRewardNativeAdView extends ReactViewGroup implements AdPopcornRewardNativeEventListener {

    private String placementId;
    private int nativeWidthPx = -1;
    private int nativeHeightPx = -1;
    private AdPopcornRewardNativeAd nativeAd;

    public RNAdPopcornRewardNativeAdView(ThemedReactContext context) {
        super(context);
    }

    public void setPlacementId(String placementId) {
        this.placementId = placementId;
        internalLoadAd();
    }

    public void setNativeWidth(int widthDp) {
        this.nativeWidthPx = dpToPx(widthDp);
        internalLoadAd();
    }

    public void setNativeHeight(int heightDp) {
        this.nativeHeightPx = dpToPx(heightDp);
        internalLoadAd();
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
        post(measureAndLayout);
    }

    private final Runnable measureAndLayout = () -> {
        if (nativeAd != null) {
            nativeAd.measure(
                    MeasureSpec.makeMeasureSpec(nativeWidthPx, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(nativeHeightPx, MeasureSpec.EXACTLY));
            nativeAd.layout(nativeAd.getLeft(), nativeAd.getTop(), nativeWidthPx, nativeHeightPx);
        }
    };

    /**
     * 수동 loadAd — stop 후 재로드 시 뷰를 다시 생성한다.
     */
    public void loadAd() {
        if (nativeAd == null) {
            initAndLoadAd();
        } else {
            nativeAd.loadAd();
        }
    }

    public void stopAd() {
        if (nativeAd != null) {
            removeAllViews();
            nativeAd = null;
        }
    }

    /**
     * props가 모두 설정되면 자동으로 광고를 로드한다.
     */
    private void internalLoadAd() {
        if (placementId == null || placementId.isEmpty() || nativeWidthPx == -1 || nativeHeightPx == -1) return;
        initAndLoadAd();
    }

    private void initAndLoadAd() {
        stopAd();

        nativeAd = new AdPopcornRewardNativeAd(getContext());
        nativeAd.setPlacementId(placementId);
        nativeAd.setEventListener(this);
        nativeAd.setLayoutParams(new ViewGroup.LayoutParams(nativeWidthPx, nativeHeightPx));
        addView(nativeAd);

        Choreographer.getInstance().postFrameCallback(frameTimeNanos -> {
            if (nativeAd != null) {
                nativeAd.loadAd();
            }
        });
    }

    // --- AdPopcornRewardNativeEventListener ---

    @Override
    public void onNativeAdLoadSuccess() {
        if (nativeAd != null) {
            nativeAd.measure(
                    MeasureSpec.makeMeasureSpec(nativeWidthPx, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(nativeHeightPx, MeasureSpec.EXACTLY));
            nativeAd.layout(nativeAd.getLeft(), nativeAd.getTop(), nativeWidthPx, nativeHeightPx);
        }
        sendEvent("onLoadSuccess", null);
    }

    @Override
    public void onNativeAdLoadFailed(int errorCode) {
        WritableMap map = Arguments.createMap();
        map.putInt("errorCode", errorCode);
        sendEvent("onLoadFailed", map);
    }

    @Override
    public void onClicked() {
        sendEvent("onClicked", null);
    }

    @Override
    public void onCompleted() {
        sendEvent("onCompleted", null);
    }

    private void sendEvent(String eventName, @Nullable WritableMap params) {
        WritableMap event = params != null ? params : Arguments.createMap();
        ((ThemedReactContext) getContext())
                .getJSModule(RCTEventEmitter.class)
                .receiveEvent(getId(), eventName, event);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
