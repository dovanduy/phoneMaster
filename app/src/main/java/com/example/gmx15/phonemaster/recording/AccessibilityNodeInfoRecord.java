package com.example.gmx15.phonemaster.recording;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import java.util.ArrayList;
import java.util.List;


public class AccessibilityNodeInfoRecord {
    private AccessibilityNodeInfo nodeInfo;
    private List<AccessibilityNodeInfoRecord> children;
    private List<AccessibilityNodeInfoRecord> uselessChildren;
    private AccessibilityNodeInfoRecord parent;
    private int initIndex;
    public boolean isImportant;
    public boolean recycled;


    AccessibilityNodeInfoRecord(AccessibilityNodeInfo nodeInfo, AccessibilityNodeInfoRecord parent, int initIndex) {
        this.nodeInfo = nodeInfo;
        this.children = new ArrayList<>();
        this.uselessChildren = new ArrayList<>();
        this.parent = parent;
        this.initIndex = initIndex;
        for(int i = 0; i < nodeInfo.getChildCount(); ++ i){
            AccessibilityNodeInfo crtNode = nodeInfo.getChild(i);
            if(crtNode == null){
                continue;
            }
            children.add(new AccessibilityNodeInfoRecord(crtNode, this, i));

        }
        recycled = false;
    }

    public boolean ignoreUselessChild(boolean isForceUseless){
        isImportant = false;
        //boolean isRefresh = getViewIdResourceName() != null && Objects.equals("uik_refresh_header", getViewIdResourceName().toString());
        boolean isRefresh = false;
        for(AccessibilityNodeInfoRecord child: children){
            if(child.ignoreUselessChild(isRefresh)){
                isImportant = true;
            }
        }

        if(!isImportant){
            isImportant = isClickable() || isCheckable() || isScrollable() || isEditable()
                    || isLongClickable() || (getText() != null && getText().length() > 0)
                    || (getContentDescription() != null && getContentDescription().length() > 0);
        }

        isImportant = isImportant && !isForceUseless && !isRefresh;
        // 把所有不重要的节点从 children 里转移到 uselessChild 里
        uselessChildren.addAll(children);
        for(AccessibilityNodeInfoRecord child: children){
            if(child.isImportant){
                uselessChildren.remove(child);
            }
        }

        children.removeAll(uselessChildren);

        return isImportant;
    }

    public void refreshRecord(){
        boolean lastImportance = isImportant;
        recycleChildren();

        nodeInfo.refresh();
        for(int i = 0; i < nodeInfo.getChildCount(); ++ i){
            AccessibilityNodeInfo crtNode = nodeInfo.getChild(i);
            if(crtNode == null){
                continue;
            }
            children.add(new AccessibilityNodeInfoRecord(crtNode, this, i));
        }

        ignoreUselessChild(false);
        if(parent != null && lastImportance != isImportant){
            parent.refreshImportanceIfChildImportanceChanged(this);
        }
    }

    public void refreshImportanceIfChildImportanceChanged(AccessibilityNodeInfoRecord child){
        boolean lastImportance = isImportant;
        if((child.isImportant && children.contains(child)) || ((!child.isImportant) && uselessChildren.contains(child))){
            return;
        }
        if(child.isImportant){
            uselessChildren.remove(child);
            int indexFirstLarge;
            for(indexFirstLarge = 0; indexFirstLarge < children.size(); ++ indexFirstLarge){
                if(children.get(indexFirstLarge).initIndex > child.initIndex){
                    break;
                }
            }
            children.add(indexFirstLarge, child);
        } else {
            children.remove(child);
            int indexFirstLarge;
            for(indexFirstLarge = 0; indexFirstLarge < uselessChildren.size(); ++ indexFirstLarge){
                if(uselessChildren.get(indexFirstLarge).initIndex > child.initIndex){
                    break;
                }
            }
            uselessChildren.add(indexFirstLarge, child);
        }
        isImportant = (!children.isEmpty()) || isClickable() || isCheckable() || isScrollable() || isEditable()
                || isLongClickable() || (getText() != null && getText().length() > 0)
                || (getContentDescription() != null && getContentDescription().length() > 0);
        if(parent != null && lastImportance != isImportant){
            parent.refreshImportanceIfChildImportanceChanged(this);
        }
    }


    public void recycleRecord(){
        recycleChildren();
        nodeInfo.recycle();
        recycled = true;
        parent = null;
    }

    public void recycleChildren(){
        for(AccessibilityNodeInfoRecord child: children){
            child.recycleRecord();
        }
        for(AccessibilityNodeInfoRecord child: uselessChildren){
            child.recycleRecord();
        }

        children.clear();
        uselessChildren.clear();
        isImportant = false;
    }

    public AccessibilityNodeInfoRecord getParent(){
        return parent;
    }

    public boolean isClickable(){
        return nodeInfo.isClickable();
    }

    public boolean isScrollable(){
        return nodeInfo.isScrollable();
    }

    public boolean isLongClickable(){
        return nodeInfo.isLongClickable();
    }

    public boolean isEditable(){
        return nodeInfo.isEditable();
    }

    public boolean isCheckable(){
        return nodeInfo.isCheckable();
    }

    public int getChildCount(){
        return children.size();
    }

    public AccessibilityNodeInfoRecord getChild(int index){
        return children.get(index);
    }

    public CharSequence getText(){
        return nodeInfo.getText();
    }

    public CharSequence getContentDescription(){
        return nodeInfo.getContentDescription();
    }

    public boolean performAction(int action){
        if(!recycled)
            return nodeInfo.performAction(action);
        else
            return false;
    }

    public boolean performAction(int action, Bundle info){
        if(!recycled)
            return nodeInfo.performAction(action, info);
        else
            return false;
    }

    public CharSequence getClassName(){
        return nodeInfo.getClassName();
    }

    public AccessibilityWindowInfo getWindow(){
        return nodeInfo.getWindow();
    }

    public void getBoundsInScreen(Rect r){
        nodeInfo.getBoundsInScreen(r);
    }

    public boolean isSelected(){
        return nodeInfo.isSelected();
    }

    public CharSequence getPackageName(){
        return nodeInfo.getPackageName();
    }

    public int getDrawingOrder(){
        return nodeInfo.getDrawingOrder();
    }

    public CharSequence getViewIdResourceName(){
        return nodeInfo.getViewIdResourceName();
    }

    public boolean isVisibleToUser(){
        return nodeInfo.isVisibleToUser();
    }

    public boolean isFocusable(){
        return nodeInfo.isFocusable();
    }

    public AccessibilityNodeInfo getNodeInfo(){
        return nodeInfo;
    }

    public boolean isChecked(){
        return nodeInfo.isChecked();
    }

    public boolean isEnabled(){
        return nodeInfo.isEnabled();
    }

    public boolean isFocused(){
        return nodeInfo.isFocused();
    }

    public boolean isPassword(){
        return nodeInfo.isPassword();
    }

    public boolean isAccessibilityFocused(){
        return nodeInfo.isAccessibilityFocused();
    }

    public boolean isDismissable(){
        return nodeInfo.isDismissable();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return this.nodeInfo.equals(((AccessibilityNodeInfoRecord) obj).nodeInfo);
    }
}
