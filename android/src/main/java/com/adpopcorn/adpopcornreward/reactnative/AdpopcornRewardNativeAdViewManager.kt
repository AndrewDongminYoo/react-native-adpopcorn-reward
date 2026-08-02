package com.adpopcorn.adpopcornreward.reactnative

import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewManagerDelegate
import com.facebook.react.viewmanagers.AdpopcornRewardNativeAdManagerDelegate
import com.facebook.react.viewmanagers.AdpopcornRewardNativeAdManagerInterface

@ReactModule(name = AdpopcornRewardNativeAdViewManager.REACT_CLASS)
class AdpopcornRewardNativeAdViewManager :
  SimpleViewManager<AdpopcornRewardNativeAdView>(),
  AdpopcornRewardNativeAdManagerInterface<AdpopcornRewardNativeAdView> {
  private val delegate = AdpopcornRewardNativeAdManagerDelegate(this)

  override fun getName(): String = REACT_CLASS

  override fun getDelegate(): ViewManagerDelegate<AdpopcornRewardNativeAdView> = delegate

  override fun createViewInstance(reactContext: ThemedReactContext): AdpopcornRewardNativeAdView = AdpopcornRewardNativeAdView(reactContext)

  override fun onDropViewInstance(view: AdpopcornRewardNativeAdView) {
    view.stopAd()
    super.onDropViewInstance(view)
  }

  override fun setPlacementId(
    view: AdpopcornRewardNativeAdView,
    value: String?,
  ) {
    view.setPlacementId(value)
  }

  override fun setNativeWidth(
    view: AdpopcornRewardNativeAdView,
    value: Int,
  ) {
    view.setNativeWidth(value)
  }

  override fun setNativeHeight(
    view: AdpopcornRewardNativeAdView,
    value: Int,
  ) {
    view.setNativeHeight(value)
  }

  override fun loadAd(view: AdpopcornRewardNativeAdView) {
    view.loadAd()
  }

  override fun stopAd(view: AdpopcornRewardNativeAdView) {
    view.stopAd()
  }

  override fun getExportedCustomDirectEventTypeConstants(): Map<String, Any> =
    mapOf(
      "onLoadSuccess" to mapOf("registrationName" to "onLoadSuccess"),
      "onLoadFailed" to mapOf("registrationName" to "onLoadFailed"),
      "onClicked" to mapOf("registrationName" to "onClicked"),
      "onCompleted" to mapOf("registrationName" to "onCompleted"),
    )

  companion object {
    const val REACT_CLASS: String = "AdpopcornRewardNativeAd"
  }
}
