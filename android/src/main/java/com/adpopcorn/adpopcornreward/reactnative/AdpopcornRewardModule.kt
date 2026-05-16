package com.adpopcorn.adpopcornreward.reactnative

import android.graphics.Color
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap
import com.igaworks.adpopcorn.Adpopcorn
import com.igaworks.adpopcorn.AdpopcornExtension
import com.igaworks.adpopcorn.interfaces.IAPBridgeRewardInfoCallbackListener
import com.igaworks.adpopcorn.interfaces.IAPRewardInfoCallbackListener
import com.igaworks.adpopcorn.interfaces.IAdPOPcornEventListener
import com.igaworks.adpopcorn.renewal.ApRewardStyle

class AdpopcornRewardModule(
    reactContext: ReactApplicationContext,
) : NativeAdpopcornRewardSpec(reactContext),
    IAdPOPcornEventListener {
    // In-flight policy mirrors iOS: each reward-info query is single-flight.
    // A concurrent call rejects with E_INFLIGHT — keeps Promise semantics
    // unambiguous and avoids leaking promise references.
    private var offerwallPromise: Promise? = null
    private val bridgePromises: MutableMap<String, Promise> = mutableMapOf()

    override fun setAppKey(
        appKey: String,
        hashKey: String,
    ) {
        Log.w(
            TAG,
            "setAppKey() is a no-op on Android. Configure appKey/hashKey via " +
                "AndroidManifest meta-data (com.igaworks.adpopcorn.cores.common.APAppKey / APHashKey).",
        )
    }

    override fun setLogEnable(enable: Boolean) {
        Log.w(
            TAG,
            "setLogEnable() is a no-op on Android. The native SDK reads its log " +
                "flag from manifest meta-data.",
        )
    }

    override fun setUserId(userId: String) {
        Adpopcorn.setUserId(reactApplicationContext, userId)
    }

    override fun setStyle(
        offerwallTitle: String,
        mainOfferwallColorHex: String,
    ) {
        if (offerwallTitle.isNotEmpty()) {
            ApRewardStyle.offerwallTitle = offerwallTitle
        }
        if (mainOfferwallColorHex.length == 7) {
            runCatching { Color.parseColor(mainOfferwallColorHex) }
                .onSuccess { ApRewardStyle.mainOfferwallColor = it }
                .onFailure { Log.w(TAG, "setStyle(): invalid color $mainOfferwallColorHex", it) }
        }
    }

    override fun openOfferwall() {
        Adpopcorn.setEventListener(reactApplicationContext, this)
        Adpopcorn.openOfferwall(currentActivity)
    }

    override fun openBridge(bridgePlacementId: String) {
        Adpopcorn.setEventListener(reactApplicationContext, this)
        Adpopcorn.openBridge(currentActivity, bridgePlacementId)
    }

    override fun openCSPage() {
        Adpopcorn.openCSPage(currentActivity)
    }

    override fun getOfferwallTotalRewardInfo(promise: Promise) {
        if (offerwallPromise != null) {
            promise.reject(ERROR_INFLIGHT, ERROR_INFLIGHT_MESSAGE)
            return
        }
        offerwallPromise = promise
        AdpopcornExtension.getOfferwallTotalRewardInfo(
            reactApplicationContext,
            object : IAPRewardInfoCallbackListener {
                override fun OnEarnableTotalRewardInfo(
                    queryResult: Boolean,
                    totalCount: Int,
                    totalReward: String?,
                ) {
                    val pending = offerwallPromise
                    offerwallPromise = null
                    pending?.resolve(
                        Arguments.createMap().apply {
                            putBoolean("queryResult", queryResult)
                            putInt("totalCount", totalCount)
                            putString("totalReward", totalReward ?: "")
                        },
                    )
                }
            },
        )
    }

    override fun getBridgeTotalRewardInfo(
        bridgePlacementId: String,
        promise: Promise,
    ) {
        if (bridgePromises[bridgePlacementId] != null) {
            promise.reject(ERROR_INFLIGHT, ERROR_INFLIGHT_MESSAGE)
            return
        }
        bridgePromises[bridgePlacementId] = promise
        AdpopcornExtension.getBridgeTotalRewardInfo(
            reactApplicationContext,
            bridgePlacementId,
            object : IAPBridgeRewardInfoCallbackListener {
                override fun bridgeTotalRewardInfo(
                    queryResult: Boolean,
                    totalCount: Int,
                    totalReward: String?,
                    callbackBridgePlacementId: String?,
                ) {
                    val key = callbackBridgePlacementId ?: bridgePlacementId
                    val pending = bridgePromises.remove(key)
                    pending?.resolve(
                        Arguments.createMap().apply {
                            putBoolean("queryResult", queryResult)
                            putInt("totalCount", totalCount)
                            putString("totalReward", totalReward ?: "")
                            putString("bridgePlacementId", key)
                        },
                    )
                }
            },
        )
    }

    // IAdPOPcornEventListener ---------------------------------------------------

    override fun OnClosedOfferWallPage() {
        emitOnClosedOfferWallPage()
    }

    override fun OnCompletedCampaign() {
        emitOnCompletedCampaign()
    }

    override fun OnAgreePrivacy() {
        // Privacy callbacks are not surfaced to JS — legacy did the same.
    }

    override fun OnDisagreePrivacy() {
        // Privacy callbacks are not surfaced to JS — legacy did the same.
    }

    companion object {
        private const val TAG = "AdpopcornRewardModule"
        private const val ERROR_INFLIGHT = "E_INFLIGHT"
        private const val ERROR_INFLIGHT_MESSAGE =
            "another reward info query for this scope is already in flight"

        const val NAME: String = NativeAdpopcornRewardSpec.NAME
    }
}
