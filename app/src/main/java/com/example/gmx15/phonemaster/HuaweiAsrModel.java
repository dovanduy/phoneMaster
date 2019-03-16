package com.example.gmx15.phonemaster;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.huawei.hiai.asr.AsrRecognizer;

public class HuaweiAsrModel {
    private static final String TAG = "HuaweiAsrModel";

    private Context mContext;
    private AsrRecognizer mAsrRecognizer;

    public HuaweiAsrModel(Context context, AsrRecognizer asrRecognizer) {
        mContext = context;
        mAsrRecognizer = asrRecognizer;
    }

    public void startListening(Intent intent) {
        Log.d(TAG, "startListening() ");
        if (mAsrRecognizer != null) {
            mAsrRecognizer.startListening(intent);
        }
    }

    public void stopListening() {
        Log.d(TAG, "stopListening() ");
        if (mAsrRecognizer != null) {
            mAsrRecognizer.stopListening();
        }
    }

    public void cancelListening() {
        Log.d(TAG, "cancelListening() ");
        if (mAsrRecognizer != null) {
            mAsrRecognizer.cancel();
        }
    }

    public void destroyEngine() {
        Log.d(TAG, "destroyEngine() ");
        if (mAsrRecognizer != null) {
            mAsrRecognizer.destroy();
        }
    }

    public static boolean isSupportAsr(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo("com.huawei.hiai", 0);
            Log.d(TAG, "Engine versionName: " + packageInfo.versionName + " ,versionCode: " + packageInfo.versionCode);
            if (packageInfo.versionCode <= 801000300) {
                return false;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }


}
