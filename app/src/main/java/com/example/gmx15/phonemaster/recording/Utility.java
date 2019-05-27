package com.example.gmx15.phonemaster.recording;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.speech.tts.TextToSpeech;
import android.view.accessibility.AccessibilityNodeInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;


public class Utility {
    public static TextToSpeech tts;
    static boolean ttsPrepared;
    static AccessibilityService service;

    public static void init(AccessibilityService s){
        Utility.service = s;
        if(tts == null){
            tts = new TextToSpeech(s, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int i) {
                    ttsPrepared = (i == TextToSpeech.SUCCESS);
                    if(!ttsPrepared){
                        tts = null;
                    } else {
                        tts.setLanguage(Locale.CHINESE);
                    }
                }
            });
        }
    }

    public static String printTree(AccessibilityNodeInfo root){
        StringBuffer res = new StringBuffer();
        printNodeStructure(root, 0, res);
        return res.toString();
    }

    public static void printNodeStructure(AccessibilityNodeInfo root, int depth, StringBuffer res){
        if(root == null){
            return;
        }
        root.refresh();
        Rect border = new Rect();
        root.getBoundsInScreen(border);
        for(int i = 0; i < depth; i ++){
            res.append("\t");
        }

        res.append(root.hashCode()).append(" ")
                .append(root.getClassName()).append(" ")
                .append(root.getViewIdResourceName()).append(" ")
                .append(border.toString()).append(" ")
                .append(root.getText()).append(" ")
                .append(root.getContentDescription()).append(" ")
                .append("isClickable: ").append(root.isClickable()).append(" ")
                .append("isScrollable: ").append(root.isScrollable()).append(" ")
                .append("isVisible: ").append(root.isVisibleToUser()).append(" ")
                .append("isEnabled: ").append(root.isEnabled()).append(" ")
                .append("labelBy: ").append((root.getLabeledBy() == null)? -1: root.getLabeledBy().hashCode()).append("\n");

        //res.append(root.toString()).append("\n");
        for(int i = 0; i < root.getChildCount(); ++ i){
            printNodeStructure(root.getChild(i), depth + 1, res);
        }
    }

    public static boolean isNodeVisible(AccessibilityNodeInfo node){
        if(node == null){
            return false;
        }
        Rect r = new Rect();
        node.getBoundsInScreen(r);
        return r.width() > 0 && r.height() > 0;
    }

    public static boolean isNodeVisible(AccessibilityNodeInfoRecord node){
        if(node == null){
            return false;
        }
        Rect r = new Rect();
        node.getBoundsInScreen(r);
        return r.width() > 0 && r.height() > 0;
    }

    public static AccessibilityNodeInfoRecord getNodeByNodeId(AccessibilityNodeInfoRecord startNode, String relativeNodeId){
        if(startNode == null){
            return null;
        }
        int indexEnd = relativeNodeId.indexOf(';');
        if(indexEnd < 0){
            // 不存在分号，说明已经结束了
            if(startNode.getClassName().toString().equals(relativeNodeId)){
                return startNode;
            } else {
                return null;
            }
        }

        String focusPart = relativeNodeId.substring(0, indexEnd);
        int indexDivision = focusPart.indexOf('|');
        int childIndex = Integer.valueOf(focusPart.substring(indexDivision + 1));
        String crtPartClass = focusPart.substring(0, indexDivision);
        String remainPart = relativeNodeId.substring(indexEnd + 1);

        // 这里对 id 的处理方式，应该和 crawler 中的处理方式相一致的
        if(startNode.isScrollable() || (startNode.getClassName() != null && startNode.getClassName().toString().contains("ListView"))){
            int actualIndex = 0;
            while (actualIndex < startNode.getChildCount()) {
                if (isNodeVisible(startNode.getChild(actualIndex))) {
                    childIndex -= 1;
                    if (childIndex < 0) {
                        break;
                    }
                }
                actualIndex += 1;
            }
            childIndex = actualIndex;
        }

        if(startNode.getClassName().toString().equals(crtPartClass) && childIndex >= 0 && childIndex < startNode.getChildCount()){
            return getNodeByNodeId(startNode.getChild(childIndex), remainPart);
        } else {
            return null;
        }
    }


    public static JSONObject generateLayoutJson(AccessibilityNodeInfo crtRoot, int indexInParent){
        // 生成描述这个节点及其子节点的 json 字符串
        JSONObject obj = new JSONObject();
        try {
            obj.put("@index", indexInParent);
            obj.put("@text", crtRoot.getText());
            obj.put("@resource-id", crtRoot.getViewIdResourceName());
            obj.put("@class", crtRoot.getClassName());
            obj.put("@package", crtRoot.getPackageName());
            obj.put("@content-desc", crtRoot.getContentDescription());
            obj.put("@checkable", crtRoot.isCheckable());
            obj.put("@checked", crtRoot.isChecked());
            obj.put("@clickable", crtRoot.isClickable());
            obj.put("@enabled", crtRoot.isEnabled());
            obj.put("@focusable", crtRoot.isFocusable());
            obj.put("@focused", crtRoot.isFocused());
            obj.put("@scrollable", crtRoot.isScrollable());
            obj.put("@long-clickable", crtRoot.isLongClickable());
            obj.put("@password", crtRoot.isPassword());
            obj.put("@selected", crtRoot.isSelected());
            obj.put("@editable", crtRoot.isEditable());
            obj.put("@accessibilityFocused", crtRoot.isAccessibilityFocused());
            obj.put("@dismissable", crtRoot.isDismissable());

            Rect r = new Rect();
            crtRoot.getBoundsInScreen(r);
            String bounds = "[" + r.left + ',' + r.top + "][" + r.right + ',' + r.bottom + "]";
            obj.put("@bounds", bounds);

            if(crtRoot.getChildCount() == 0){
                JSONArray emptyArray = new JSONArray();
                obj.put("node", emptyArray);
            } else {
                for(int i = 0; i < crtRoot.getChildCount(); ++ i){
                    if(crtRoot.getChild(i) == null){
                        continue;
                    }
                    obj.accumulate("node", generateLayoutJson(crtRoot.getChild(i), i));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
