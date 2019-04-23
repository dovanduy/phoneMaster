package com.example.gmx15.phonemaster.recording;

import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.speech.tts.TextToSpeech;

import com.example.gmx15.phonemaster.MainActivity;
import com.example.gmx15.phonemaster.utilities.MyThread;

import org.json.JSONObject;

import java.io.StringWriter;


public class Recorder {

    private static final String TAG = "RecorderService";
    private AccessibilityNodeInfo previousLayout = null;

    public Recorder() {

    }

    public void dispatchAccessibilityEvent(AccessibilityEvent event, AccessibilityNodeInfo root) {

        AccessibilityNodeInfo node = null;
        boolean isStep = false;
        String path = "";
        JSONObject layout = null;

        int eventType = event.getEventType();

        switch (eventType) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                node = event.getSource();
                isStep = true;
                Log.i("RecordEvent", event.getText().toString());
                Log.i("RecordEvent", event.getClassName().toString());
                Log.i("RecordEvent", event.toString());
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                Log.i("Focus", event.getText().toString());
//                    previousLayout = getRootInActiveWindow();
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                Log.i("My_Text", event.getText().toString());
//            default:
//                Log.i("NewEvent", accessibilityEvent.toString());
        }

        if (isStep) {
            if (node == null) {
                Log.i(TAG, "node is null");
                if (event.getClassName() != null && event.getText() != null) {
                    if (fuzzyFindPath(previousLayout, event) != null) {
                        path = fuzzyFindPath(previousLayout, event);
                        layout = Utility.generateLayoutJson(previousLayout, 0);
                    }
                }
            } else {
                if (findPath(previousLayout, node) != null) {
                    path = findPath(previousLayout, node);
                    layout = Utility.generateLayoutJson(previousLayout, 0);
                } else {
                    Rect r = new Rect();
                    node.getBoundsInScreen(r);
                    Log.i("Click", event.getText().toString());
                    Log.i("Target", r.toString());
                }
            }
        }

        if (!path.equals("") && layout != null ) {
            Log.i("RecordPath", path);
            String res = layout.toString();
            Thread t = new MyThread(res);
            t.start();
        }

        previousLayout = root;
    }

    private String findPath(AccessibilityNodeInfo root, AccessibilityNodeInfo target) {
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

    private String fuzzyFindPath(AccessibilityNodeInfo root, AccessibilityEvent event) {
        if(root == null) {
            return null;
        }

//        if (root.getText() != null) {
//            Log.i("ALlText", root.getText().toString());
//        }

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
}
