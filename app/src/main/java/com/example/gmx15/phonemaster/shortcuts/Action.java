package com.example.gmx15.phonemaster.shortcuts;

import android.view.accessibility.AccessibilityEvent;


public class Action {
    public static int CLICK = 1, LONG_CLICK = 2, TEXT = 3;

    private Integer type;
    private String[] args;
    private UILayout page;
    private StringBuilder path;

    public Action() {

    }
}
