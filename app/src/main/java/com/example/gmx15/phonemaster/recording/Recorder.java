package com.example.gmx15.phonemaster.recording;

import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.speech.tts.TextToSpeech;

import com.example.gmx15.phonemaster.MainActivity;
import com.example.gmx15.phonemaster.utilities.MyThread;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.net.Socket;


public class Recorder {

    private static final String TAG = "RecorderService";
    private AccessibilityNodeInfo previousLayout = null;
    private Socket sck;
    private String filePath = "/sdcard/records/";
    private String shortCutName = "temp";
    public static int stepId = 0;

    public Recorder(Socket sck) {
        this.sck = sck;
    }

    public void dispatchAccessibilityEvent(AccessibilityEvent event, AccessibilityNodeInfo root) {

        AccessibilityNodeInfo node = null;
        boolean isStep = false;
        String path = "";
        JSONObject layout = null;
        String stepParams = "";

        int eventType = event.getEventType();

        switch (eventType) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                node = event.getSource();
                isStep = true;
                Log.i("RecordEvent", event.getText().toString());
                Log.i("RecordEvent", event.getClassName().toString());
                Log.i("RecordEvent", event.toString());
                stepParams = "CLICK " + event.getText().toString();
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
            savePath(path + "\r\n" + stepParams, layout.toString());
            stepId += 1;

//            File file = new File("/data/data/com.example.gmx15.phonemaster/records/path1.txt");
//            if (!file.exists()) {
//                Log.i("readfile", "file not exists");
//            }
//            else {
//                RandomAccessFile raf = null;
//                try {
//                    raf = new RandomAccessFile(file, "r");
//                    raf.seek(0);
//                    Log.i("read file", raf.readLine());
//                    raf.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }

            Thread t = new MyThread(res, sck);
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
                        return root.getClassName() + "|" + i + ";" + tempPath;
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

        if (root.getText() == event.getText().get(0) && root.getClassName() == event.getClassName())
            return event.getClassName() + ";";
        if(root.getChildCount() == 0) {
            return null;
        } else {
            for(int i = 0; i < root.getChildCount(); ++ i) {
                if(root.getChild(i) != null) {
                    String tempPath = fuzzyFindPath(root.getChild(i), event);
                    if (tempPath != null) {
                        return root.getClassName() + "|" + i + ";" + tempPath;
                    }
                }
            }
        }
        return null;
    }

    private void savePath(String path, String layout) {
        String stepFilePath = filePath + shortCutName + "/" + stepId + "/";
        writeTxtToFile(path, stepFilePath, "important_ids.txt");
        writeTxtToFile(layout, stepFilePath, "layout.json");
    }

    // 将字符串写入到文本文件中
    private void writeTxtToFile(String strcontent, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);

        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.seek(0);
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }

    //生成文件
    private File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    //生成文件夹
    private static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            Log.i("filepath: ", filePath);
            file = new File(filePath);
            if (!file.exists()) {
                boolean res = file.mkdir();
                if (!res) {
                    Log.i("filepath:", "fail to create");
                }
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }
    }

}
