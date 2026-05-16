package com.adpopcorn.adpopcornreward.reactnative

import android.view.Choreographer
import android.view.ViewGroup
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.UIManagerHelper
import com.facebook.react.uimanager.events.Event
import com.facebook.react.views.view.ReactViewGroup
import com.igaworks.adpopcorn.nativead.AdPopcornRewardNativeAd
import com.igaworks.adpopcorn.nativead.AdPopcornRewardNativeEventListener

class AdpopcornRewardNativeAdView(
    context: ThemedReactContext,
) : ReactViewGroup(context),
    AdPopcornRewardNativeEventListener {
    private var placementId: String? = null
    private var nativeWidthPx: Int = -1
    private var nativeHeightPx: Int = -1
    private var nativeAd: AdPopcornRewardNativeAd? = null

    fun setPlacementId(value: String?) {
        placementId = value
        maybeLoad()
    }

    fun setNativeWidth(dp: Int) {
        nativeWidthPx = dpToPx(dp)
        maybeLoad()
    }

    fun setNativeHeight(dp: Int) {
        nativeHeightPx = dpToPx(dp)
        maybeLoad()
    }

    fun loadAd() {
        if (nativeAd == null) {
            initAndLoad()
        } else {
            nativeAd?.loadAd()
        }
    }

    fun stopAd() {
        if (nativeAd != null) {
            removeAllViews()
            nativeAd = null
        }
    }

    override fun requestLayout() {
        super.requestLayout()
        post(measureAndLayout)
    }

    private val measureAndLayout =
        Runnable {
            nativeAd?.let { ad ->
                ad.measure(
                    MeasureSpec.makeMeasureSpec(nativeWidthPx, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(nativeHeightPx, MeasureSpec.EXACTLY),
                )
                ad.layout(ad.left, ad.top, nativeWidthPx, nativeHeightPx)
            }
        }

    private fun maybeLoad() {
        if (placementId.isNullOrEmpty() || nativeWidthPx <= 0 || nativeHeightPx <= 0) return
        initAndLoad()
    }

    private fun initAndLoad() {
        stopAd()
        val ad =
            AdPopcornRewardNativeAd(context).apply {
                setPlacementId(this@AdpopcornRewardNativeAdView.placementId)
                setEventListener(this@AdpopcornRewardNativeAdView)
                layoutParams = ViewGroup.LayoutParams(nativeWidthPx, nativeHeightPx)
            }
        nativeAd = ad
        addView(ad)
        Choreographer.getInstance().postFrameCallback { ad.loadAd() }
    }

    // AdPopcornRewardNativeEventListener -----------------------------------

    override fun onNativeAdLoadSuccess() {
        nativeAd?.let { ad ->
            ad.measure(
                MeasureSpec.makeMeasureSpec(nativeWidthPx, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(nativeHeightPx, MeasureSpec.EXACTLY),
            )
            ad.layout(ad.left, ad.top, nativeWidthPx, nativeHeightPx)
        }
        dispatch("onLoadSuccess", null)
    }

    override fun onNativeAdLoadFailed(errorCode: Int) {
        val map = Arguments.createMap().apply { putInt("errorCode", errorCode) }
        dispatch("onLoadFailed", map)
    }

    override fun onClicked() {
        dispatch("onClicked", null)
    }

    override fun onCompleted() {
        dispatch("onCompleted", null)
    }

    // -----------------------------------------------------------------------

    private fun dispatch(
        name: String,
        payload: WritableMap?,
    ) {
        val reactContext = context as ReactContext
        val surfaceId = UIManagerHelper.getSurfaceId(this)
        UIManagerHelper
            .getEventDispatcherForReactTag(reactContext, id)
            ?.dispatchEvent(AdpopcornRewardNativeAdEvent(surfaceId, id, name, payload))
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
}

private class AdpopcornRewardNativeAdEvent(
    surfaceId: Int,
    viewTag: Int,
    private val name: String,
    private val payload: WritableMap?,
) : Event<AdpopcornRewardNativeAdEvent>(surfaceId, viewTag) {
    override fun getEventName(): String = name

    override fun getEventData(): WritableMap = payload ?: Arguments.createMap()
}
