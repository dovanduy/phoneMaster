package com.example.gmx15.phonemaster.accessibility_service;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.example.gmx15.phonemaster.MainActivity;
import com.example.gmx15.phonemaster.recording.Utility;
import com.example.gmx15.phonemaster.utilities.MyThread;

public class MyAccessibilityService extends AccessibilityService {

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

    private ActivityInfo getActivityInfo(ComponentName componentName) {
        try {
            return getPackageManager().getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
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