package com.example.gmx15.phonemaster;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;

import java.io.IOException;

public class KeyUtil {
    private boolean isVolumeDown = false;
    private boolean isVolumeUp = false;
    private boolean isMenu = false;
    private int currentKeyCode = 0;

    private MediaPlayer startMediaPlayer;
    private MediaPlayer endMediaPlayer;

    private static Boolean isDoubleClick = false;
    private static Boolean isLongClick = false;

    CheckForLongPress mPendingCheckForLongPress = null;
    CheckForDoublePress mPendingCheckForDoublePress = null;
    Handler mHandler = new Handler();

    Context mContext = null;
    private String TAG = "";

    public KeyUtil(Context context, String tag, MediaPlayer sMediaPlayer, MediaPlayer eMediaPlayer) {
        mContext = context;
        TAG = tag;
        startMediaPlayer = sMediaPlayer;
        endMediaPlayer = eMediaPlayer;
    }

    public void dispatchKeyEvent(KeyEvent event) {
        int keycode = event.getKeyCode();

        // 有不同按键按下，取消长按、短按的判断
        if (currentKeyCode != keycode) {
            removeLongPressCallback();
            isDoubleClick = false;
        }

        // 处理长按、单击、双击按键
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            checkForLongClick(event);
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            checkForDoubleClick(event);
        }

        if (keycode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                isVolumeDown = true;
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                isVolumeDown = false;
            }
        } else if (keycode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                isVolumeUp = true;
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                isVolumeUp = false;
            }
        } else if (keycode == KeyEvent.KEYCODE_MENU) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                isMenu = true;
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                isMenu = true;
            }
        }

        // 判断组合按键
        if (isVolumeDown
                && isVolumeUp
                && (keycode == KeyEvent.KEYCODE_VOLUME_UP
                || keycode == KeyEvent.KEYCODE_VOLUME_DOWN)
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            //组合按键事件处理；
            Log.i(TAG, "--组合--");
            isVolumeDown = false;
            isVolumeUp = false;

            if (MainActivity.isStarted) {
                startMediaPlayer.start();
            } else {
                endMediaPlayer.start();
            }
            MainActivity.isStarted = !MainActivity.isStarted;
        }
    }

    private void removeLongPressCallback() {
        if (mPendingCheckForLongPress != null) {
            mHandler.removeCallbacks(mPendingCheckForLongPress);
        }
    }

    private void checkForLongClick(KeyEvent event) {
        int count = event.getRepeatCount();
        int keycode = event.getKeyCode();
        if (count == 0) {
            currentKeyCode = keycode;
        } else {
            return;
        }
        if (mPendingCheckForLongPress == null) {
            mPendingCheckForLongPress = new CheckForLongPress();
        }
        mPendingCheckForLongPress.setKeycode(event.getKeyCode());
        mHandler.postDelayed(mPendingCheckForLongPress, 1000);
    }

    class CheckForLongPress implements Runnable {

        int currentKeycode = 0;

        public void run() {
            isLongClick = true;
            longPress(currentKeycode);
        }

        public void setKeycode(int keycode) {
            currentKeycode = keycode;
        }
    }

    private void longPress(int keycode) {
        Log.i(TAG, "--longPress 长按事件--" + keycode);
    }

    private void singleClick(int keycode) {
        Log.i(TAG, "--singleClick 单击事件--" + keycode);
    }

    private void doublePress(int keycode) {
        Log.i(TAG, "---doublePress 双击事件--" + keycode);
    }

    private void checkForDoubleClick(KeyEvent event) {
        // 有长按时间发生，则不处理单击、双击事件
        removeLongPressCallback();
        if (isLongClick) {
            isLongClick = false;
            return;
        }

        if (!isDoubleClick) {
            isDoubleClick = true;
            if (mPendingCheckForDoublePress == null) {
                mPendingCheckForDoublePress = new CheckForDoublePress();
            }
            mPendingCheckForDoublePress.setKeycode(event.getKeyCode());
            mHandler.postDelayed(mPendingCheckForDoublePress, 500);
        } else {
            // 500ms内两次单击，触发双击
            isDoubleClick = false;
            doublePress(event.getKeyCode());
        }
    }

    class CheckForDoublePress implements Runnable {

        int currentKeycode = 0;

        public void run() {
            if (isDoubleClick) {
                singleClick(currentKeycode);
            }
            isDoubleClick = false;
        }

        public void setKeycode(int keycode) {
            currentKeycode = keycode;
        }
    }

    private void removeDoublePressCallback() {
        if (mPendingCheckForDoublePress != null) {
            mHandler.removeCallbacks(mPendingCheckForDoublePress);
        }
    }
}
