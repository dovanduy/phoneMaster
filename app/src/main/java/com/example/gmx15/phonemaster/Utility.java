package com.example.gmx15.phonemaster;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.graphics.Rect;
import android.speech.tts.TextToSpeech;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.gmx15.phonemaster.AccessibilityNodeInfoRecord;

import java.util.Locale;

/**
 * Created by kinnplh on 2018/5/14.
 */

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

    public static void shutdownTts(){
        if(tts != null){
            tts.shutdown();
            tts = null;
        }
    }

    public static void speak(String text){
        if(!ttsPrepared){
            Log.e("error", "speak: No tts available");
        } else {
            Log.i("info", "speak: " + text);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "text to speak: " + text);
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

    public static void generateLayoutXML(AccessibilityNodeInfo crtRoot, int indexInParent, StringBuilder builder){
        // 生成描述这个节点及其子节点的 xml 字符串
        builder.append("<node ");
        appendField("index", indexInParent, builder);
        appendField("text", crtRoot.getText(), builder);
        appendField("resource-id", crtRoot.getViewIdResourceName(), builder);
        appendField("class", crtRoot.getClassName(), builder);
        appendField("package", crtRoot.getPackageName(), builder);
        appendField("content-desc", crtRoot.getContentDescription(), builder);
        appendField("checkable", crtRoot.isCheckable(), builder);
        appendField("checked", crtRoot.isChecked(), builder);
        appendField("clickable", crtRoot.isClickable(), builder);
        appendField("enabled", crtRoot.isEnabled(), builder);
        appendField("focusable", crtRoot.isFocusable(), builder);
        appendField("focused", crtRoot.isFocused(), builder);
        appendField("scrollable", crtRoot.isScrollable(), builder);
        appendField("long-clickable", crtRoot.isLongClickable(), builder);
        appendField("password", crtRoot.isPassword(), builder);
        appendField("selected", crtRoot.isSelected(), builder);
        appendField("editable", crtRoot.isEditable(), builder);
        appendField("accessibilityFocused", crtRoot.isAccessibilityFocused(), builder);
        appendField("dismissable", crtRoot.isDismissable(), builder);

        Rect r = new Rect();
        crtRoot.getBoundsInScreen(r);
        builder.append("bounds=\"").append('[').append(r.left).append(',').append(r.top).append("][").append(r.right).append(',').append(r.bottom).append(']').append('"');
        if(crtRoot.getChildCount() == 0){
            builder.append("/>");
        } else {
            builder.append(">");
            for(int i = 0; i < crtRoot.getChildCount(); ++ i){
                if(crtRoot.getChild(i) == null){
                    continue;
                }
                generateLayoutXML(crtRoot.getChild(i), i, builder);
            }
            builder.append("</node>");
        }
    }

    public static void generateLayoutXMLWithoutUselessNodes(AccessibilityNodeInfo crtRoot, int indexInParent, StringBuilder builder){
        assert indexInParent == 0;
        long start1 = System.currentTimeMillis();
        AccessibilityNodeInfoRecord root = new AccessibilityNodeInfoRecord(crtRoot, null, 0);
        long start2 = System.currentTimeMillis();
        Log.i("time spend", "build record " + String.valueOf(start2 - start1));
        root.ignoreUselessChild(false);
        long start3 = System.currentTimeMillis();
        Log.i("time spend", "ignore useless " + String.valueOf(start3 - start2));
        generateLayoutXMLWithRecord(root, 0, builder);
        long start4 = System.currentTimeMillis();
        Log.i("time spend", "get xml " + String.valueOf(start4 - start3));
    }

    public static void generateLayoutXMLWithRecord(AccessibilityNodeInfoRecord crtRoot, int indexInParent, StringBuilder builder){
        // 生成描述这个节点及其子节点的 xml 字符串
        builder.append("<node ");
        appendField("index", indexInParent, builder);
        appendField("text", crtRoot.getText(), builder);
        appendField("resource-id", crtRoot.getViewIdResourceName(), builder);
        appendField("class", crtRoot.getClassName(), builder);
        appendField("package", crtRoot.getPackageName(), builder);
        appendField("content-desc", crtRoot.getContentDescription(), builder);
        appendField("checkable", crtRoot.isCheckable(), builder);
        appendField("checked", crtRoot.isChecked(), builder);
        appendField("clickable", crtRoot.isClickable(), builder);
        appendField("enabled", crtRoot.isEnabled(), builder);
        appendField("focusable", crtRoot.isFocusable(), builder);
        appendField("focused", crtRoot.isFocused(), builder);
        appendField("scrollable", crtRoot.isScrollable(), builder);
        appendField("long-clickable", crtRoot.isLongClickable(), builder);
        appendField("password", crtRoot.isPassword(), builder);
        appendField("selected", crtRoot.isSelected(), builder);
        appendField("editable", crtRoot.isEditable(), builder);
        appendField("accessibilityFocused", crtRoot.isAccessibilityFocused(), builder);
        appendField("dismissable", crtRoot.isDismissable(), builder);

        Rect r = new Rect();
        crtRoot.getBoundsInScreen(r);
        builder.append("bounds=\"").append('[').append(r.left).append(',').append(r.top).append("][").append(r.right).append(',').append(r.bottom).append(']').append('"');
        if(crtRoot.getChildCount() == 0){
            builder.append("/>");
        } else {
            builder.append(">");
            for(int i = 0; i < crtRoot.getChildCount(); ++ i){
                if(crtRoot.getChild(i) == null){
                    continue;
                }
                generateLayoutXMLWithRecord(crtRoot.getChild(i), i, builder);
            }
            builder.append("</node>");
        }
    }

    static void appendField(String name, String value, StringBuilder builder){
        builder.append(name).append("=\"").append(value == null? "": intoXMLFormat(value)).append("\" ");
    }

    static void appendField(String name, int value, StringBuilder builder){
        builder.append(name).append("=\"").append(value).append("\" ");
    }

    static void appendField(String name, CharSequence value, StringBuilder builder){
        builder.append(name).append("=\"").append(value == null? "": intoXMLFormat(value)).append("\" ");
    }

    static void appendField(String name, boolean value, StringBuilder builder){
        builder.append(name).append("=\"").append(value? "true": "false").append("\" ");
    }

    static String intoXMLFormat(Object s){
        return s == null? "": s.toString().replace("\n", " ")
                .replace("&", "&amp;")
                .replace("\'", "&apos;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("#", " ");
    }
}
