package com.miaoyongjun.mdragvideo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.util.Log;
import android.view.View;

import com.miaoyongjun.mdragvideo.media.IjkVideoView;


/**
 * Author: miaoyongjun
 * Date : 17/2/22
 */

public class MVideo {

    private String TAG = "MVideo";
    static boolean DEBUG = false;
    private static Bundle bundle;
    static final String VIDEO_PATH = "videoPath";
    static final String EXIT_DURATION = "exitDuration";
    static final String ENTER_DURATION = "enterDuration";
    static final String ROTATE_DIRECTION = "duration";
    static final String PROGRESS_COLOR = "progressColor";
    static final String LOOPER = "looper";
    static final String PREVIEW_IMAGE = "previewImage";
    static final String PREVIEW_PATH = "previewPath";

    public static MVideo getInstance() {
        init();
        return MVideoHolder.holder;
    }

    private static class MVideoHolder {
        private static MVideo holder = new MVideo();
    }

    private static void init() {
        bundle = new Bundle();
    }

    public MVideo setVideoPath(String videoPath) {
        bundle.putString(VIDEO_PATH, videoPath);
        return this;
    }

    public MVideo setExitDuration(long duration) {
        bundle.putLong(EXIT_DURATION, duration);
        return this;
    }

    public MVideo setEnterDuration(long duration) {
        bundle.putLong(ENTER_DURATION, duration);
        return this;
    }

    /**
     * LEFT(90),
     * TOP(180),
     * RIGHT(270),
     * BOTTOM(0),
     * DEFAULT(1);
     */
    public MVideo setRotateDirection(IjkVideoView.RotateDirection rotateDirection) {
        bundle.putInt(ROTATE_DIRECTION, rotateDirection.getRotateDirectionValue());
        return this;
    }

    public MVideo setDebugMode(boolean isDebug) {
        DEBUG = isDebug;
        return this;
    }

    public void printLog(String log) {
        if (DEBUG) {
            Log.d(TAG, log);
        }
    }

    /**
     * such as 0xFF00bcd4
     */
    public MVideo setProgressColor(@ColorInt int progressColor) {
        bundle.putInt(PROGRESS_COLOR, progressColor);
        return this;
    }

    public MVideo setLooper(boolean isLooper) {
        bundle.putBoolean(LOOPER, isLooper);
        return this;
    }

    public MVideo setPreviewImage(@DrawableRes int resid) {
        bundle.putInt(PREVIEW_IMAGE, resid);
        return this;
    }

    public MVideo setPreviewImage(String path) {
        bundle.putString(PREVIEW_PATH, path);
        return this;
    }

    public void start(Activity activity, View srcView, String videoPath) {
        bundle.putString(VIDEO_PATH, videoPath);
        this.start(activity, srcView);
    }

    public void start(Activity activity, View srcView) {
        Intent intent = new Intent(activity, DragVideoActivity.class);
        intent.putExtras(bundle);
        int location[] = new int[2];
        srcView.getLocationOnScreen(location);
        intent.putExtra(DragVideoActivity.LEFT, location[0]);
        intent.putExtra(DragVideoActivity.TOP, location[1]);
        intent.putExtra(DragVideoActivity.HEIGHT, srcView.getHeight());
        intent.putExtra(DragVideoActivity.WIDTH, srcView.getWidth());
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }


}
