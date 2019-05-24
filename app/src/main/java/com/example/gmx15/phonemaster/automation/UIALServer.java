package com.example.gmx15.phonemaster.automation;


import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.IBinder;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import org.json.JSONException;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UIALServer extends AccessibilityService {
    public static UIALServer self;
    Map<String, MergedApp> packageToMergedApp;
    ServerThread thread;

    @Override
    public void onCreate() {
        super.onCreate();
        self = this;
        packageToMergedApp = new HashMap<>();

        try {
            AssetManager manager = getAssets();
            String[] mergedAppNames = manager.list("merged_apps");

            for(String fileName: mergedAppNames) {
                if(!fileName.endsWith(".json"))
                    continue;
                MergedApp mergedApp = new MergedApp(this, "merged_apps/" + fileName);
                packageToMergedApp.put(mergedApp.packageName, mergedApp);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        if(thread == null) {
            thread = new ServerThread();
            thread.start();
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        self = null;
    }
}

