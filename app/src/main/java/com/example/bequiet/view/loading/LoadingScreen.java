package com.example.bequiet.view.loading;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.bequiet.R;
import com.example.bequiet.model.receivers.WifiListener;
import com.example.bequiet.view.home.HomePageActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoadingScreen extends AppCompatActivity {
    private final int PERMISSION_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);
    }


    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        WifiListener wifiListener = new WifiListener();
        getApplicationContext().registerReceiver(wifiListener, filter);
        Log.i("Perms", "Hello App.: ");
        checkDoNotDisturbPermission();
        checkBackgroundLocationPermission();
    }


    private void checkAndRequestPermissions() {
        String[] permissionsList = new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_NOTIFICATION_POLICY,
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                android.Manifest.permission.ACCESS_WIFI_STATE,
                android.Manifest.permission.RECEIVE_BOOT_COMPLETED
        };

        List<String> permissionsNeeded = new ArrayList<>();
        for (String permission : permissionsList) { //check which permissions are missing
            if (ContextCompat.checkSelfPermission(LoadingScreen.this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            } else {
                Log.i("Perms", "Got: " + permission);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            Log.i("Perms", "Requesting: " + Arrays.toString(permissionsNeeded.toArray()));
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[permissionsNeeded.size()]),
                    PERMISSION_REQUEST_CODE);
        } else {
            goToHomeActivity();
        }
    }

    private void checkDoNotDisturbPermission() {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        ActivityResultLauncher<Intent> notificationPolicyLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (!notificationManager.isNotificationPolicyAccessGranted()) {
                        Toast.makeText(this, "We need the do not disturb permission to work properly", Toast.LENGTH_LONG).show();
                        checkDoNotDisturbPermission();
                    } else {
                        checkAndRequestPermissions();
                    }
                });

        if (!notificationManager.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            notificationPolicyLauncher.launch(intent);
        } else {
            checkAndRequestPermissions();
        }
    }

    private void checkBackgroundLocationPermission() {
        if (!(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(this, "Go under settings and give us the location all the time", Toast.LENGTH_LONG).show();
            ActivityResultLauncher<Intent> locationLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (!(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                            Toast.makeText(this, "We need the background location to work properly", Toast.LENGTH_LONG).show();
                            checkBackgroundLocationPermission();
                        } else {
                            checkAndRequestPermissions();
                        }
                    });

            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            locationLauncher.launch(intent);
        } else {
            checkAndRequestPermissions();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        String[] permissionsList = new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_NOTIFICATION_POLICY,
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                android.Manifest.permission.ACCESS_WIFI_STATE,
                android.Manifest.permission.RECEIVE_BOOT_COMPLETED
        };

        List<String> permissionsNeeded = new ArrayList<>();
        for (String permission : permissionsList) { //check which permissions are missing
            if (ContextCompat.checkSelfPermission(LoadingScreen.this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            } else {
                Log.i("Perms", "Got: " + permission);
            }
        }
        if (!permissionsNeeded.isEmpty()) {
            Toast.makeText(this, "We need all the permissions to work properly", Toast.LENGTH_LONG).show();
        } else {
            goToHomeActivity();

        }
    }

    private void goToHomeActivity() {
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            Intent intent = new Intent(LoadingScreen.this, HomePageActivity.class);
            startActivity(intent);
            finish();
        }, 3000);
    }
}