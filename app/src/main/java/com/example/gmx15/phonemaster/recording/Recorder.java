package com.example.gmx15.phonemaster.recording;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.speech.tts.TextToSpeech;

import com.example.gmx15.phonemaster.MainActivity;


public class Recorder {

    private int previousEvent = -1;
    private boolean stepFlag = false;

    public Recorder() {

    }

    public void dispatchAccessibilityEvent(AccessibilityEvent event, AccessibilityNodeInfo root) {
        int eventType = event.getEventType();
        stepFlag = false;

        switch (previousEvent) {
            case -1:
                Log.i("My_Start", "Start!");
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                Log.i("My_Text", event.getText().toString());
                stepFlag = true;
//            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
//                Log.i("My_Scroll", Integer.toString(accessibilityEvent.getScrollDeltaX())
//                            + " " + Integer.toString(accessibilityEvent.getScrollDeltaY()));
//                stepFlag = true;
        }


        previousEvent = eventType;

        switch (eventType) {
            // View Types
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                // represents the event of clicking on a View like Button, CompoundButton, etc.
                Log.i("My_Click", event.getText().toString());
                if(event.getSource() != null) {
                    Log.i("My_Click_Source", event.getSource().toString());
                    if (event.getSource().getParent() != null) {
                        Log.i("My_Click_Parent", event.getSource().getParent().toString());
                        if (event.getSource().getParent().getCollectionItemInfo() != null) {
                            Log.i("My_Click_Index", event.getSource().getParent().getCollectionItemInfo().toString());
                        }
                    }
                }
                stepFlag = true;
                // 可能可以判断点击的是否在一个list里
//                Log.i("My_Click_Class", accessibilityEvent.getClassName().toString());
                // 可以用于获取在哪个应用里
//                Log.i("My_Click_Package", accessibilityEvent.getPackageName().toString());
                // 可以作为补充信息
//                if (accessibilityEvent.getContentDescription() != null) {
//                    Log.i("My_Description", accessibilityEvent.getContentDescription().toString());
//                }
                // 测试text to speech
               MainActivity.getTextToSpeech().stop();
               MainActivity.getTextToSpeech().speak(event.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);

               while (true) {
                   if (!MainActivity.getTextToSpeech().isSpeaking()) {
                       Log.i("My_Speaking", "End speaking");
                       break;
                   } else {
                       try {
                           Thread.sleep(1000);
                       } catch (InterruptedException e) {
                           e.printStackTrace();
                       }
                    }
               }
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                // represents the event of long clicking on a View like Button, CompoundButton, etc
                Log.i("My_Long_Click", event.getText().toString());
//            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
//                // represents the event of changing the text of an EditText.
//                Log.i("My_Text", accessibilityEvent.getText().toString());
//            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
//                // 一次滚动会产生很多条log
//                Log.i("My_Scroll", accessibilityEvent.getPackageName().toString());
//                Log.i("DeltaY", Integer.toString(accessibilityEvent.getScrollDeltaY()));
                // Notification
//            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
//                Log.i("Notification", accessibilityEvent.getText().toString());
//          // TRANSITION TYPES
//          case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
//              Log.i("Window_Change", accessibilityEvent.getClassName().toString());
//          case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
//              // represents the event of opening a PopupWindow, Menu, Dialog, etc.
//              Log.i("Window_State_Change", accessibilityEvent.getClassName().toString());
//            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
//                Log.i("My_Window_Content_Change", accessibilityEvent.getClassName().toString());
//                Log.i("My_Window_Content_Change_Text", accessibilityEvent.getText().toString());
            default:
//                Log.i("NewEvent", event.toString());
        }

//        if (stepFlag) {
//            AccessibilityNodeInfo rootNode = root;
////            Log.i("Tree", Utility.printTree(rootNode));
//
//            StringBuilder jsonBuilder = new StringBuilder();
//            Utility.generateLayoutJson(rootNode, 0, jsonBuilder);
//            String res = jsonBuilder.toString();
////            Log.i("Layout", res);
//            Thread t = new MyThread(res);
//            t.start();
//        }



//        int try_time = 10;
//        while (root == null && try_time > 0) {
//            root = getRootInActiveWindow();
//            try_time -= 1;
//        }
//        if (root == null) {
//            boolean handHandled = false;
//            if (!handHandled) {
//                Log.i("Res", "RES-DUMP_LAYOUT#Failure#");
//            } else {
//            }
//            return;
//        }

    }

}
