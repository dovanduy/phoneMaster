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
import android.view.accessibility.AccessibilityRecord;
import android.view.accessibility.AccessibilityWindowInfo;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.example.gmx15.phonemaster.utilities.MyThread;
import com.example.gmx15.phonemaster.MainActivity;
import com.example.gmx15.phonemaster.recording.Utility;

public class MyAccessibilityService extends AccessibilityService {

    private static final String TAG = "RecorderService";

    private AccessibilityNodeInfo previousLayout = null;

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        MainActivity.keyutil.dispatchKeyEvent(event);
        return super.onKeyEvent(event);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

        switch (accessibilityEvent.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                AccessibilityNodeInfo node = accessibilityEvent.getSource();

                Log.i("Click", accessibilityEvent.getText().toString());
                Log.i("Click", accessibilityEvent.getClassName().toString());
                Log.i("Click", accessibilityEvent.toString());


                if (node == null) {
                    Log.i(TAG, "node is null");
                    if (accessibilityEvent.getClassName() != null && accessibilityEvent.getText() != null) {
                        if (fuzzyFindPath(previousLayout, accessibilityEvent) != null) {
                            Log.i("Path", fuzzyFindPath(previousLayout, accessibilityEvent));
                        } else {
                            Log.i("Fail", "Fail to find path");
                        }
                    }
                    return;
                } else {
                    Log.i(TAG, node.toString());
                    if (findPath(getRootInActiveWindow(), node) != null) {
                        Log.i("Path", findPath(previousLayout, node));
                    } else {
                        Rect r = new Rect();
                        node.getBoundsInScreen(r);
                        Log.i("Click", accessibilityEvent.getText().toString());
                        Log.i("Target", r.toString());
                    }
                }


                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
    //            Log.i("Tree", Utility.printTree(rootNode));

                StringBuilder jsonBuilder = new StringBuilder();
                Utility.generateLayoutJson(previousLayout, 0, jsonBuilder);
                String res = jsonBuilder.toString();
    //            Log.i("Layout", res);
                Thread t = new MyThread(res);
                t.start();


//                AccessibilityNodeInfo root = getRootInActiveWindow();
//                if (root == null) {
//                    Log.i(TAG, "root is null");
//                    return;
//                }
//
//                // Strategy #1: locate node via its id
//                String id = node.getViewIdResourceName();
//                if (id == null) {
//                    Log.i(TAG, "id is null");
//                } else {
//
//                    List<AccessibilityNodeInfo> rootNodes = root.findAccessibilityNodeInfosByViewId(id);
//                    if (rootNodes.size() == 1) {
//                        Log.i(TAG, "success (via id)");
//                        return;
//                    } else {
//                        Log.i(TAG, "multiple nodes with that id");
//                    }
//                }
//
//                // Strategy #2: locate node via its text
//                CharSequence text = node.getText();
//                if (text == null) {
//                    Log.i(TAG, "text is null");
//                } else {
//                    List<AccessibilityNodeInfo> rootNodes = root.findAccessibilityNodeInfosByText(text.toString());
//                    if (rootNodes.size() == 1) {
//                        Log.i(TAG, "success (via text)");
//                        return;
//                    }
//                }
//
//                Log.i(TAG, "failed, node was not recoverable");
        }

        previousLayout = getRootInActiveWindow();


//        if (MainActivity.isStarted) {
//            MainActivity.recorder.dispatchAccessibilityEvent(accessibilityEvent, getRootInActiveWindow());
//        }
    }

    public String findPath(AccessibilityNodeInfo root, AccessibilityNodeInfo target) {
        if(root == null) {
            return null;
        }

        Rect r1 = new Rect();
        root.getBoundsInScreen(r1);
        Rect r2 = new Rect();
        target.getBoundsInScreen(r2);
//        Log.i("RecList", r1.toString());

        if (r1.left == r2.left && r1.right == r2.right && r1.bottom == r2.bottom && r1.top == r2.top
                && root.getClassName() == target.getClassName())
            return target.getClassName() + ";";
        if(root.getChildCount() == 0) {
            return null;
        } else {
            for(int i = 0; i < root.getChildCount(); ++ i) {
                if(root.getChild(i) != null) {
                    String tempPath = findPath(root.getChild(i), target);
                    if (tempPath != null) {
                        return root.getClassName() + "|" + Integer.toString(i) + ";" + tempPath;
                    }
                }
            }
        }
        return null;
    }

    public String fuzzyFindPath(AccessibilityNodeInfo root, AccessibilityEvent event) {
        if(root == null) {
            return null;
        }

        if (root.getText() == event.getText() && root.getClassName() == event.getClassName())
            return event.getClassName() + ";";
        if(root.getChildCount() == 0) {
            return null;
        } else {
            for(int i = 0; i < root.getChildCount(); ++ i) {
                if(root.getChild(i) != null) {
                    String tempPath = fuzzyFindPath(root.getChild(i), event);
                    if (tempPath != null) {
                        return root.getClassName() + "|" + Integer.toString(i) + ";" + tempPath;
                    }
                }
            }
        }
        return null;
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
