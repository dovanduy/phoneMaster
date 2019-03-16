package com.example.gmx15.phonemaster;

import android.accessibilityservice.AccessibilityService;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Environment;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.view.accessibility.AccessibilityRecord;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.speech.tts.TextToSpeech;


import static android.support.v4.app.ActivityCompat.startActivityForResult;
import static com.example.gmx15.phonemaster.Utility.printTree;

public class MyAccessibilityService extends AccessibilityService {
    public MyAccessibilityService() {

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

        switch (accessibilityEvent.getEventType()) {
            // View Types
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                // represents the event of clicking on a View like Button, CompoundButton, etc.
                Log.i("Click", accessibilityEvent.getText().toString());
                // 可能可以判断点击的是否在一个list里
                Log.i("Click_Class", accessibilityEvent.getClassName().toString());
                // 可以用于获取在哪个应用里
                Log.i("Click_Package", accessibilityEvent.getPackageName().toString());
                // 可以作为补充信息
                if (accessibilityEvent.getContentDescription() != null) {
                    Log.i("MyDescription", accessibilityEvent.getContentDescription().toString());
                }
                MainActivity.getmTextToSpeech().speak(accessibilityEvent.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                break;
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                // represents the event of long clicking on a View like Button, CompoundButton, etc
                Log.i("Long_Click", accessibilityEvent.getText().toString());
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                // represents the event of changing the text of an EditText.
                Log.i("Text", accessibilityEvent.getText().toString());
                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                // 一次滚动会产生很多条log
                Log.i("Scroll", accessibilityEvent.getPackageName().toString());
            // Notification
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                Log.i("Notification", accessibilityEvent.getText().toString());
            // TRANSITION TYPES
//            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
//                Log.i("Window_Change", accessibilityEvent.getContentChangeTypes());
//            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
//                // represents the event of opening a PopupWindow, Menu, Dialog, etc.
//                Log.i("Window_State_Change", accessibilityEvent.getClassName().toString());
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                Log.i("Window_Content_Change", accessibilityEvent.getClassName().toString());
                Log.i("Window_Content_Change_Text", accessibilityEvent.getText().toString());
            default:
                Log.i("NewEvent", accessibilityEvent.toString());
        }

//        Log.i("Tree", printTree(getRootInActiveWindow()));

        AccessibilityNodeInfo root = getRootInActiveWindow();
        int try_time = 10;
        while (root == null && try_time > 0) {
            root = getRootInActiveWindow();
            try_time -= 1;
        }
//        if (root == null) {
//            boolean handHandled = false;
//            if (!handHandled) {
//                Log.i("Res", "RES-DUMP_LAYOUT#Failure#");
//            } else {
//            }
//            return;
//        }

//        StringBuilder xmlBuilder = new StringBuilder();
//        xmlBuilder.append("RES-DUMP_LAYOUT#Success#");
//        Utility.generateLayoutXMLWithoutUselessNodes(root, 0, xmlBuilder);
//        xmlBuilder.append("\n");
//        String res = xmlBuilder.toString();
//        Log.i("XML", res);

//        Log.i("path", getFilesDir().getAbsolutePath());
//        Thread t = new MyThread(res);
//        t.start();
    }

    @Override
    public void onInterrupt() {

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
