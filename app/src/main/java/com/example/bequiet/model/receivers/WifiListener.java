package com.example.bequiet.model.receivers;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import androidx.room.Room;

import com.example.bequiet.model.AppDatabase;
import com.example.bequiet.model.RuleTimer;
import com.example.bequiet.model.VolumeManager;
import com.example.bequiet.model.dataclasses.WlanRule;

import java.util.List;

public class WifiListener extends BrodcastReceiver {
    private static final String TAG = "WifiBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
            boolean connected = intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false);
            if (connected) {
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                //Array needs to be final to access it inside runnable.
                final WifiInfo[] wifiInfo = {wifiManager.getConnectionInfo()};
                // Connecting takes a while so we wait until the INfo is there.
                new Thread(() -> {
                    while (wifiInfo[0].getSupplicantState() != SupplicantState.COMPLETED) {
                        wifiInfo[0] = wifiManager.getConnectionInfo();
                    }
                    checkRules(context, wifiInfo[0].getSSID().replace("\"", ""));
                    Log.d(TAG, wifiInfo[0].toString());
                }).start();
            }
        }
    }

    private void checkRules(Context context, String ssid) {
        Thread thread = new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(context,
                    AppDatabase.class, "rules").build();

            List<WlanRule> wlanRules = db.ruleDAO().loadAllWlanRules();
            Log.d(TAG, "Rules: " + wlanRules.size());
            for (WlanRule wlanRule : wlanRules) {
                if (wlanRule.getWlanName().equals(ssid)) {
                    if (wlanRule.isActive()) {
                        switch (wlanRule.getReactionType()) {
                            case SILENT:
                                VolumeManager.getInstance().muteDevice(context);
                                Log.d(TAG, "Muted device.");
                                break;
                            case VIBRATE:
                                VolumeManager.getInstance().turnVibrationOn(context);
                                Log.d(TAG, "Device vibrating.");
                                break;
                            case NOISE:
                                VolumeManager.getInstance().turnNoiseOn(context);
                                Log.d(TAG, "Device making noise.");
                        }
                        RuleTimer.getInstance().startTimer(wlanRule.getDurationEnd(), () -> {
                            VolumeManager.getInstance().turnNoiseOn(context);
                            Log.d(TAG, "Reset Volume in Handler.");
                            RuleTimer.getInstance().startTimer(wlanRule.getDurationStart(), () -> {
                                checkRules(context, ssid);
                            });

                        });

                    } else {
                        VolumeManager.getInstance().turnNoiseOn(context);
                        Log.d(TAG, "Reset Volume in Handler.");
                        RuleTimer.getInstance().startTimer(wlanRule.getDurationStart(), () -> {
                            switch (wlanRule.getReactionType()) {
                                case SILENT:
                                    VolumeManager.getInstance().muteDevice(context);
                                    Log.d(TAG, "Muted device. In Handler");
                                    break;
                                case VIBRATE:
                                    VolumeManager.getInstance().turnVibrationOn(context);
                                    Log.d(TAG, "Device vibrating. In Handler");
                                    break;
                                case NOISE:
                                    VolumeManager.getInstance().turnNoiseOn(context);
                                    Log.d(TAG, "Device making noise. In Handler");
                            }
                            RuleTimer.getInstance().startTimer(wlanRule.getDurationEnd(), () -> {
                                checkRules(context, ssid);
                            });
                        });
                    }
                }
            }
            db.close();
        });
        thread.start();
    }

}