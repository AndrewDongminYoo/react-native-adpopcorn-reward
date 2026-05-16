package com.adpopcorn.adpopcornreward.reactnative;

import android.graphics.Color;
import android.util.Log;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.igaworks.adpopcorn.Adpopcorn;
import com.igaworks.adpopcorn.AdpopcornExtension;
import com.igaworks.adpopcorn.interfaces.IAdPOPcornEventListener;
import com.igaworks.adpopcorn.interfaces.IAPRewardInfoCallbackListener;
import com.igaworks.adpopcorn.interfaces.IAPBridgeRewardInfoCallbackListener;
import com.igaworks.adpopcorn.renewal.ApRewardStyle;

class RNAdPopcornRewardModule extends ReactContextBaseJavaModule implements IAdPOPcornEventListener {
   public static final String EVENT_CLOSED_OFFERWALL = "OnClosedOfferWallPage";
   public static final String EVENT_COMPLETED_CAMPAIGN = "OnCompletedCampaign";
   public static final String EVENT_OFFERWALL_REWARD_INFO = "OnOfferwallTotalRewardInfo";
   public static final String EVENT_BRIDGE_REWARD_INFO = "OnBridgeTotalRewardInfo";

   public RNAdPopcornRewardModule(ReactApplicationContext context) {
      super(context);
   }

   @Override
   public String getName(){
      return "RNAdPopcornRewardModule";
   }

   @ReactMethod
   public void setAppKey(String appKey, String hashKey) {
      Log.d("RNAdPopcornRewardModule", "setAppKey : android is not supported. use AndroidManifest.xml");
   }

   @ReactMethod
   public void setLogEnable(boolean enable) {
      Log.d("RNAdPopcornRewardModule", "setLogEnable : android is not supported. use AndroidManifest.xml");
   }

   @ReactMethod
   public void openOfferwall() {
      Log.d("RNAdPopcornRewardModule", "openOfferwall");
      Adpopcorn.setEventListener(getReactApplicationContext(), this);
      Adpopcorn.openOfferwall(getCurrentActivity());
   }

   @ReactMethod
   public void openBridge(String bridgePlacementId) {
      Log.d("RNAdPopcornRewardModule", "openBridge : " + bridgePlacementId);
      Adpopcorn.setEventListener(getReactApplicationContext(), this);
      Adpopcorn.openBridge(getCurrentActivity(), bridgePlacementId);
   }

   @ReactMethod
   public void openCSPage() {
      Log.d("RNAdPopcornRewardModule", "openCSPage");
      Adpopcorn.openCSPage(getCurrentActivity());
   }

   @ReactMethod
   public void setUserId(String userId) {
      Log.d("RNAdPopcornRewardModule", "setUserId : " + userId);
      Adpopcorn.setUserId(getReactApplicationContext(), userId);
   }

   @ReactMethod
   public void setStyle(String offerwallTitle, String colorCode) {
      Log.d("RNAdPopcornRewardModule", "setStyle : " + offerwallTitle + "/" + colorCode);
      if(offerwallTitle != null && offerwallTitle.length() > 0)
         ApRewardStyle.offerwallTitle = offerwallTitle;

      // #RRGGBB
      if(colorCode != null && colorCode.length() == 7)
         ApRewardStyle.mainOfferwallColor = Color.parseColor(colorCode);
   }

   @ReactMethod
   public void getOfferwallTotalRewardInfo() {
      Log.d("RNAdPopcornRewardModule", "getOfferwallTotalRewardInfo");
      AdpopcornExtension.getOfferwallTotalRewardInfo(getReactApplicationContext(), new IAPRewardInfoCallbackListener() {
         @Override
         public void OnEarnableTotalRewardInfo(boolean queryResult, int totalCount, String totalReward) {
            WritableMap map = Arguments.createMap();
            map.putBoolean("queryResult", queryResult);
            map.putInt("totalCount", totalCount);
            map.putString("totalReward", totalReward);
            sendEvent(EVENT_OFFERWALL_REWARD_INFO, map);
         }
      });
   }

   @ReactMethod
   public void getBridgeTotalRewardInfo(String bridgePlacementId) {
      Log.d("RNAdPopcornRewardModule", "getBridgeTotalRewardInfo : " + bridgePlacementId);
      AdpopcornExtension.getBridgeTotalRewardInfo(getReactApplicationContext(), bridgePlacementId, new IAPBridgeRewardInfoCallbackListener() {
         @Override
         public void bridgeTotalRewardInfo(boolean queryResult, int totalCount, String totalReward, String bridgePlacementId) {
            WritableMap map = Arguments.createMap();
            map.putBoolean("queryResult", queryResult);
            map.putInt("totalCount", totalCount);
            map.putString("totalReward", totalReward);
            map.putString("bridgePlacementId", bridgePlacementId);
            sendEvent(EVENT_BRIDGE_REWARD_INFO, map);
         }
      });
   }

   @Override
   public void OnClosedOfferWallPage() {
      Log.d("RNAdPopcornRewardModule", "OnClosedOfferWallPage");
      sendEvent(EVENT_CLOSED_OFFERWALL, null);
   }

   @Override
   public void OnAgreePrivacy() {

   }

   @Override
   public void OnDisagreePrivacy() {

   }

   @Override
   public void OnCompletedCampaign() {
      Log.d("RNAdPopcornRewardModule", "OnCompletedCampaign");
      sendEvent(EVENT_COMPLETED_CAMPAIGN, null);
   }

   private void sendEvent(String eventName, @Nullable WritableMap params) {
      (getReactApplicationContext())
              .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
              .emit(eventName, params);
   }
}
