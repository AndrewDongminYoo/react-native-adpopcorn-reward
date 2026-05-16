import { useEffect, useRef, useState } from "react";
import {
  Alert,
  Button,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";

import AdPopcornReward, {
  AdPopcornRewardEvents,
  AdPopcornRewardNativeAd,
  type AdPopcornRewardNativeAdRef,
} from "react-native-adpopcorn-reward";

// Replace with values from the AdPopcorn dashboard before running.
const APP_KEY = "YOUR_APP_KEY";
const HASH_KEY = "YOUR_HASH_KEY";
const NATIVE_AD_PLACEMENT_ID = "YOUR_NATIVE_AD_PLACEMENT";
const USER_ID = "demo-user";

export default function App() {
  const [status, setStatus] = useState("idle");
  const adRef = useRef<AdPopcornRewardNativeAdRef>(null);

  useEffect(() => {
    if (Platform.OS === "ios") {
      AdPopcornReward.setAppKey(APP_KEY, HASH_KEY);
      AdPopcornReward.setLogEnable(true);
    }
    AdPopcornReward.setUserId(USER_ID);

    const closeSub = AdPopcornReward.addListener(
      AdPopcornRewardEvents.OnClosedOfferWallPage,
      () => setStatus("offerwall closed"),
    );
    const completedSub = AdPopcornReward.addListener(
      AdPopcornRewardEvents.OnCompletedCampaign,
      () => setStatus("campaign completed"),
    );

    return () => {
      closeSub.remove();
      completedSub.remove();
    };
  }, []);

  const handleOpenOfferwall = () => {
    setStatus("opening offerwall");
    AdPopcornReward.openOfferwall();
  };

  const handleQueryReward = async () => {
    setStatus("querying reward info");
    try {
      const info = await AdPopcornReward.getOfferwallTotalRewardInfo();
      setStatus(`reward ${info.totalCount} / ${info.totalReward}`);
    } catch (error) {
      const message = error instanceof Error ? error.message : String(error);
      Alert.alert("getOfferwallTotalRewardInfo failed", message);
      setStatus("query failed");
    }
  };

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.title}>AdPopcornReward example</Text>
      <Text style={styles.status}>status: {status}</Text>

      <View style={styles.buttons}>
        <Button title="Open offerwall" onPress={handleOpenOfferwall} />
        <Button title="Query reward info" onPress={handleQueryReward} />
        <Button title="loadAd" onPress={() => adRef.current?.loadAd()} />
        <Button title="stopAd" onPress={() => adRef.current?.stopAd()} />
      </View>

      <Text style={styles.sectionLabel}>Native ad (320 × 250 dp)</Text>
      <AdPopcornRewardNativeAd
        ref={adRef}
        placementId={NATIVE_AD_PLACEMENT_ID}
        nativeWidth={320}
        nativeHeight={250}
        onLoadSuccess={() => setStatus("native ad loaded")}
        onLoadFailed={(event) =>
          setStatus(`native ad failed (${event.nativeEvent.errorCode})`)
        }
        onClicked={() => setStatus("native ad clicked")}
        onCompleted={() => setStatus("native ad completed")}
      />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    padding: 16,
    gap: 16,
  },
  title: {
    fontSize: 20,
    fontWeight: "600",
  },
  status: {
    fontSize: 14,
    color: "#333",
  },
  sectionLabel: {
    fontSize: 12,
    color: "#666",
    marginTop: 8,
  },
  buttons: {
    gap: 8,
  },
});
