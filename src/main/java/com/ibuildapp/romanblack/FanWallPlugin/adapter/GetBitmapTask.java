/****************************************************************************
*                                                                           *
*  Copyright (C) 2014-2015 iBuildApp, Inc. ( http://ibuildapp.com )         *
*                                                                           *
*  This file is part of iBuildApp.                                          *
*                                                                           *
*  This Source Code Form is subject to the terms of the iBuildApp License.  *
*  You can obtain one at http://ibuildapp.com/license/                      *
*                                                                           *
****************************************************************************/
package com.ibuildapp.romanblack.FanWallPlugin.adapter;


import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import com.ibuildapp.romanblack.FanWallPlugin.data.Statics;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Support thread for baseimageadapter
 * Each copy of such class checks image in assets, cache or download it
 * As a result it calls the callback function
 */
public class GetBitmapTask extends Thread {

    private final int WIDTH_LIMITER = 300;
    private final String TAG = "com.ibuildapp.romanblack.ShopingCartPlugin.tasks";
    private boolean isInterrupted = false;
    private int uid;
    private ImageView id;
    private String url;
    private String cachePath;
    private String resPath;
    private String name;
    private OnImageDoneListener listener;
    private AssetManager assetMgr;
    private int roundedCorners;

    /**
     * @param context
     * @param uid       - task uid
     * @param name      - taks name
     * @param id        - uid of image
     * @param resPath   - path of image in assets
     * @param cachePath - path of image in cache
     * @param url       - - path of image at http
     * @param width     - not use now
     * @param height    - not use now
     */
    public GetBitmapTask(Context context, int uid, String name, ImageView id, String resPath, String cachePath, String url, int width, int height, int roundedCorners) {

        this.uid = uid;
        this.name = name;
        this.id = id;
        this.resPath = resPath;
        this.cachePath = cachePath;
        this.url = url;
        this.roundedCorners = roundedCorners;

        assetMgr = context.getAssets();
    }

    @Override
    public void interrupt() {
        super.interrupt();
        isInterrupted = true;
    }

    @Override
    public void run() {
        super.run();

        Bitmap result = null;

        // 1. check bitmap in assets
        if (!TextUtils.isEmpty(resPath)) {
            InputStream stream = null;
            try {
                stream = assetMgr.open(resPath);
                result = Statics.proccessBitmap(stream, Bitmap.Config.RGB_565);
                if (roundedCorners != -1)
                    result = Statics.publishAvatar(result, roundedCorners);

                if (listener != null) {
                    Log.e(TAG, "RESOURSES");
                    listener.onImageLoaded(uid, id, name, result, null);
                    return;
                }
            } catch (IOException e) {
                stream = null;
            }
        }

        if (isInterrupted) {
            return;
        }
        // 2. check bitmap in cache
        if (!TextUtils.isEmpty(cachePath)) {
            File imageFile = new File(cachePath);
            if (imageFile.exists()) {
                result = Statics.proccessBitmap(cachePath, Bitmap.Config.RGB_565, WIDTH_LIMITER);
                if (roundedCorners != -1)
                    result = Statics.publishAvatar(result, roundedCorners);

                if (listener != null) {
                    Log.e(TAG, "CACHE");
                    listener.onImageLoaded(uid, id, name, result, cachePath);
                    return;
                }
            }
        }

        if (isInterrupted) {
            return;
        }

        // 3. download bitmap from www
        if (!TextUtils.isEmpty(url)) {

            String downloadedImg =
                    Statics.downloadFile(url);


            if (downloadedImg != null) {

                result = Statics.proccessBitmap(downloadedImg, Bitmap.Config.RGB_565, WIDTH_LIMITER);
                if (roundedCorners != -1)
                    result = Statics.publishAvatar(result, roundedCorners);

                if (result == null) {
                    Log.e(TAG, "btm = null");
                }

                if (listener != null) {
                    Log.e(TAG, "HTTP");
                    listener.onImageLoaded(uid, id, name, result, downloadedImg);
                    return;
                }
            }
        }

        if (listener != null) {
            Log.e(TAG, "NULL");
            listener.onImageLoaded(uid, id, name, null, null);
            return;
        }
    }

    public void setListener(OnImageDoneListener listener) {
        this.listener = listener;
    }
}