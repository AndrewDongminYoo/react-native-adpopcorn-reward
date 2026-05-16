package com.adpopcorn.adpopcornreward.reactnative;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.bridge.ReadableArray;

import java.util.Map;

/**
 * RN 브릿지 역할만 담당하는 ViewManager.
 * 실제 뷰 로직은 {@link RNAdPopcornRewardNativeAdView}에 위임한다.
 */
public class RNAdPopcornRewardNativeAdManager extends SimpleViewManager<RNAdPopcornRewardNativeAdView> {

    public static final String REACT_CLASS = "RNAdPopcornRewardNativeAd";
    private static final int COMMAND_LOAD_AD = 1;
    private static final int COMMAND_STOP_AD = 2;

    @NonNull
    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @NonNull
    @Override
    protected RNAdPopcornRewardNativeAdView createViewInstance(@NonNull ThemedReactContext context) {
        return new RNAdPopcornRewardNativeAdView(context);
    }

    @ReactProp(name = "placementId")
    public void setPlacementId(RNAdPopcornRewardNativeAdView view, @Nullable String placementId) {
        view.setPlacementId(placementId);
    }

    @ReactProp(name = "nativeWidth", defaultInt = 0)
    public void setNativeWidth(RNAdPopcornRewardNativeAdView view, int width) {
        view.setNativeWidth(width);
    }

    @ReactProp(name = "nativeHeight", defaultInt = 0)
    public void setNativeHeight(RNAdPopcornRewardNativeAdView view, int height) {
        view.setNativeHeight(height);
    }

    @Nullable
    @Override
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        MapBuilder.Builder<String, Object> builder = MapBuilder.builder();
        builder.put("onLoadSuccess", MapBuilder.of("registrationName", "onLoadSuccess"));
        builder.put("onLoadFailed", MapBuilder.of("registrationName", "onLoadFailed"));
        builder.put("onClicked", MapBuilder.of("registrationName", "onClicked"));
        builder.put("onCompleted", MapBuilder.of("registrationName", "onCompleted"));
        return builder.build();
    }

    @Nullable
    @Override
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of(
                "loadAd", COMMAND_LOAD_AD,
                "stopAd", COMMAND_STOP_AD
        );
    }

    @Override
    public void receiveCommand(@NonNull RNAdPopcornRewardNativeAdView view, int commandId, @Nullable ReadableArray args) {
        switch (commandId) {
            case COMMAND_LOAD_AD:
                view.loadAd();
                break;
            case COMMAND_STOP_AD:
                view.stopAd();
                break;
        }
    }

    @Override
    public void onDropViewInstance(@NonNull RNAdPopcornRewardNativeAdView view) {
        view.stopAd();
        super.onDropViewInstance(view);
    }
}
