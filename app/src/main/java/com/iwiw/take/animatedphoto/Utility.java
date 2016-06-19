package com.iwiw.take.animatedphoto;


import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utility {
    public static final String TAG = "@] MyApp";
    public  static final String BR = System.getProperty("line.separator");
    private static final boolean IS_DETAIL = true;

    public static void logDebug(String msg){
        if(Utility.IS_DETAIL){
            StackTraceElement callStack = Thread.currentThread().getStackTrace()[3];
            Log.d(TAG + callStack.getFileName() + "#" + callStack.getMethodName() + ":" + callStack.getLineNumber(), msg);
        } else {
            Log.d(TAG, msg);
        }
    }


    public static void logInfo(String msg){
        if(Utility.IS_DETAIL){
            StackTraceElement callStack = Thread.currentThread().getStackTrace()[3];
            Log.i(TAG
                    + callStack.getFileName() + "#" + callStack.getMethodName() + ":" + callStack.getLineNumber(), msg);
        } else {
            Log.i(TAG, msg);
        }
    }

    public static void logWarning(String msg){
        if(Utility.IS_DETAIL){
            StackTraceElement callStack = Thread.currentThread().getStackTrace()[3];
            Log.w(TAG
                    + callStack.getFileName() + "#" + callStack.getMethodName() + ":" + callStack.getLineNumber(), msg);
        } else {
            Log.w(TAG, msg);
        }
    }

    public static void logError(String msg){
        if(Utility.IS_DETAIL){
            StackTraceElement callStack = Thread.currentThread().getStackTrace()[3];
            Log.e(TAG
                    + callStack.getFileName() + "#" + callStack.getMethodName() + ":" + callStack.getLineNumber(), msg);
        } else {
            Log.e(TAG, msg);
        }
    }

    public static void showToast(Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void showToastDetail(Context context, String msg){

        StackTraceElement callStack = Thread.currentThread().getStackTrace()[3];
        Toast.makeText(context, callStack.getFileName() + "#" + callStack.getMethodName() + ":"+ callStack.getLineNumber()
                + BR + msg, Toast.LENGTH_LONG).show();
    }

    public static void registerContent(Activity activity, String path) {
        String[] paths = {path};
        String[] mimeTypes = {"image/jpeg"};
        MediaScannerConnection.scanFile(activity,
                paths,
                mimeTypes,
                null);
    }
    public static boolean getPermissionStorage(Activity activity) {
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(
                    activity,
                    permissions,
                    0);
            return false;
        } else {
            return true;
        }
    }

    public static String getSaveDirectory() {
        return Environment.getExternalStorageDirectory().toString() + "/AnimatedPhoto";
    }

    public static Uri getSaveFile() {
        final Date date = new Date(System.currentTimeMillis());
        final SimpleDateFormat dataFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
        final String filename = dataFormat.format(date) + ".jpg";
        Uri uri = Uri.fromFile(new File(Utility.getSaveDirectory(), filename));
        return uri;
    }

    public static void saveBitmap(Activity activity, Bitmap bitmap, String path) {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            registerContent(activity, path);
        } catch (Exception e) {
            logError("Save error");
            showToast(activity, "Failed to save file");
        }
    }
}
