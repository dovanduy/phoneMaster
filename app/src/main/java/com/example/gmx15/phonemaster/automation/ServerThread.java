package com.example.gmx15.phonemaster.automation;

import android.util.Pair;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ServerThread extends Thread {

    PrintStream writer;
    boolean threadRunning;
    public static Map<String, List<UIAuto.TargetFromFile>> intentToTarget;

    public static void init() throws IOException {
        intentToTarget = new HashMap<>();

        InputStreamReader inputStreamReader = new InputStreamReader(
                UIALServer.self.getAssets().open("intentToTarget.txt"));
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String s = null;
        while ((s = reader.readLine()) != null){
            List<UIAuto.TargetFromFile> targetFromFiles = new ArrayList<>();
            String[] split_s = s.split(":");
            Utility.assertTrue(split_s.length == 2);
            String intentName = split_s[0];
            String actionStr = split_s[1];
            String[] actions = actionStr.split(";");
            for(String a: actions){
                String[] split_a = a.split("#");
                int pageIndex = Integer.valueOf(split_a[0]);
                int stateIndex = Integer.valueOf(split_a[1]);
                String targetNodeClickStr = split_a.length == 3? split_a[2] : null;
                targetFromFiles.add(new UIAuto.TargetFromFile(pageIndex, stateIndex, targetNodeClickStr));
            }
            intentToTarget.put(intentName, targetFromFiles);
        }
    }

    @Override
    public void run() {
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Utility.LuisRes res = Utility.getLuisRes("告诉李家惠明天晚上一起吃饭");
        System.out.println(res == null? "error": res.intent);

        threadRunning = true;

        while (threadRunning){
            handleJumpAccordingToStoredFiles("./shortcutPath");
        }
    }

    void handleJumpAccordingToStoredFiles(String rootPath){
        try {
            Pair<List<UIAuto.Action>, List<String>> listFromFile = UIAuto.generateActionFromDir(rootPath);  // "/sdcard/pbd_blind/ele/"
            /*UIAuto.openTargetApp(AccessibilityNodeInfoRecord.root.getPackageName().toString(),
                    UIALServer.self.packageToActivity.get(AccessibilityNodeInfoRecord.root.getPackageName().toString()));*/

            if(listFromFile != null) {
                // 跳转到对应到页面
                boolean jumpToApp = UIAuto.jumpToApp(listFromFile.first.get(0).actionNode.packageName);
                if(!jumpToApp){
                    writer.println("failed");
                    return;
                }

                // 跳转到对应到页面
                boolean jumpToStart = UIAuto.jumpToTargetNodeFromCurrent(listFromFile.first.get(0).actionNode);
                if(!jumpToStart){
                    writer.println("failed");
                    return;
                }

                Pair<List<UIAuto.Action>, Boolean> res = UIAuto.execActions(listFromFile.first, listFromFile.second, null, 5000, null, true);
                if(res.second) {
                    writer.println("success");
                } else {
                    writer.println("failed");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

