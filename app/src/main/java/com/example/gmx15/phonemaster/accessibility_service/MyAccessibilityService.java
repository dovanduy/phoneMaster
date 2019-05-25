package com.example.gmx15.phonemaster.accessibility_service;

import android.accessibilityservice.AccessibilityService;
import android.content.res.AssetManager;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.gmx15.phonemaster.MainActivity;
import com.example.gmx15.phonemaster.automation.MergedApp;
import com.example.gmx15.phonemaster.automation.ServerThread;

import org.json.JSONException;

public class MyAccessibilityService extends AccessibilityService {
    public static MyAccessibilityService self;
    public Map<String, MergedApp> packageToMergedApp;
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
                if (!fileName.endsWith(".json"))
                    continue;
                MergedApp mergedApp = new MergedApp(this, "merged_apps/" + fileName);
                packageToMergedApp.put(mergedApp.packageName, mergedApp);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

//        if(thread == null) {
//            thread = new ServerThread();
//            thread.start();
//        }
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        MainActivity.keyutil.dispatchKeyEvent(event);
        return super.onKeyEvent(event);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

        if (MainActivity.isStarted) {
            MainActivity.recorder.dispatchAccessibilityEvent(accessibilityEvent, getRootInActiveWindow());
        }
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        self = null;
    }

    @Override
    public AccessibilityNodeInfo getRootInActiveWindow()
    {
        List<AccessibilityWindowInfo> windows = super.getWindows();
        Collections.sort(windows, new Comparator<AccessibilityWindowInfo>() {
            @Override
            public int compare(AccessibilityWindowInfo accessibilityWindowInfo, AccessibilityWindowInfo t1) {
                if(accessibilityWindowInfo.getType() == AccessibilityWindowInfo.TYPE_APPLICATION && t1.getType() != AccessibilityWindowInfo.TYPE_APPLICATION){
                    return -1;
                } else if(accessibilityWindowInfo.getType() != AccessibilityWindowInfo.TYPE_APPLICATION && t1.getType() == AccessibilityWindowInfo.TYPE_APPLICATION){
                    return 1;
                } else if(accessibilityWindowInfo.getType() != AccessibilityWindowInfo.TYPE_APPLICATION && t1.getType() != AccessibilityWindowInfo.TYPE_APPLICATION){
                    return 0;
                }
                if(accessibilityWindowInfo.getParent() != null && t1.getParent() == null){
                    return -1;
                } else if(accessibilityWindowInfo.getParent() == null && t1.getParent() != null){
                    return 1;
                }

                if(accessibilityWindowInfo.isActive()){
                    return -1;
                } else if(t1.isActive()){
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        if(windows.isEmpty())
            return null;
        else
            return windows.get(0).getRoot();
    }
}