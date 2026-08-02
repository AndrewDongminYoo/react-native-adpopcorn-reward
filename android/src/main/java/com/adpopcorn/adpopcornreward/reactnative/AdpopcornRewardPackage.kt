package com.adpopcorn.adpopcornreward.reactnative

import com.facebook.react.BaseReactPackage
import com.facebook.react.ViewManagerOnDemandReactPackage
import com.facebook.react.bridge.ModuleSpec
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfo
import com.facebook.react.module.model.ReactModuleInfoProvider
import com.facebook.react.uimanager.ViewManager
import javax.inject.Provider

class AdpopcornRewardPackage :
  BaseReactPackage(),
  ViewManagerOnDemandReactPackage {
  override fun getModule(
    name: String,
    reactContext: ReactApplicationContext,
  ): NativeModule? =
    if (name == AdpopcornRewardModule.NAME) {
      AdpopcornRewardModule(reactContext)
    } else {
      null
    }

  override fun getReactModuleInfoProvider() =
    ReactModuleInfoProvider {
      mapOf(
        AdpopcornRewardModule.NAME to
          ReactModuleInfo(
            name = AdpopcornRewardModule.NAME,
            className = AdpopcornRewardModule.NAME,
            canOverrideExistingModule = false,
            needsEagerInit = false,
            isCxxModule = false,
            isTurboModule = true,
          ),
      )
    }

  override fun getViewManagers(reactContext: ReactApplicationContext): List<ModuleSpec> =
    listOf(
      ModuleSpec.viewManagerSpec(Provider { AdpopcornRewardNativeAdViewManager() }),
    )

  override fun getViewManagerNames(reactContext: ReactApplicationContext): List<String> =
    listOf(AdpopcornRewardNativeAdViewManager.REACT_CLASS)

  override fun createViewManager(
    reactContext: ReactApplicationContext,
    viewManagerName: String,
  ): ViewManager<*, *>? =
    when (viewManagerName) {
      AdpopcornRewardNativeAdViewManager.REACT_CLASS -> AdpopcornRewardNativeAdViewManager()
      else -> null
    }
}
