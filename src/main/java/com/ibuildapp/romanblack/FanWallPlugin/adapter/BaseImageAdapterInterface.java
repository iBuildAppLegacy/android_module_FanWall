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


import android.widget.ImageView;

public interface BaseImageAdapterInterface {

    /**
     * @param imageHolder - image holder
     * @param resPath     - resource assets name
     * @param cachePath   - image cache path
     * @param url         - image url
     * @param width       - image preview width
     * @param height      - image preview height
     * @param rounded     - round koeff
     */
    void addTask(ImageView imageHolder, int uid, String DUBUG_PRODUCT_NAME, String resPath, String cachePath, String url, int width, int height, int rounded);

    /**
     * not in use now
     */
    void stopAllTasks();
}