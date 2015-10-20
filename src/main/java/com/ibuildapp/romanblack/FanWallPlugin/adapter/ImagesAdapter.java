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

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.appbuilder.sdk.android.Utils;
import com.ibuildapp.romanblack.FanWallPlugin.R;
import com.ibuildapp.romanblack.FanWallPlugin.data.FanWallMessage;
import com.ibuildapp.romanblack.FanWallPlugin.data.Statics;

import java.io.File;
import java.util.ArrayList;

/**
 * This class represents photos grid adapter.
 */
public class ImagesAdapter extends BaseImageAdapterAbstractUI {

    // const
    final private int UPDATE_VIEW = 0;
    final private int IMAGE_MARGIN = 5;

    // backend
    private Context context;
    private LayoutInflater inflater;
    private ArrayList<FanWallMessage> items = new ArrayList<FanWallMessage>();
    private int imageWidth;
    private int imageHeight;
    private String cachePath = "";
    private int screenWidth;
    private Bitmap placeHolderAvatar;

    /**
     * Constructs new ImageAdapter instance with given parameters.
     *
     * @param context activity that using this adapter
     * @param items   list of messages that contains attached images
     */
    public ImagesAdapter(Context context, AbsListView uiView, ArrayList<FanWallMessage> items) {
        super(context, uiView);
        this.context = context;
        this.items = items;
        inflater = LayoutInflater.from(context);

        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();

        imageWidth = (int) ((screenWidth / 2));
        imageHeight = imageWidth;

        placeHolderAvatar = BitmapFactory.decodeResource(context.getResources(), R.drawable.romanblack_fanwall_picture_placeholder);
    }

    @Override
    public int getCount() {

        return items.size();
    }

    @Override
    public FanWallMessage getItem(int index) {
        return items.get(index);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    synchronized public View getView(int position, View convertView, ViewGroup parent) {
        try {

            View row = convertView;
            if (convertView == null)
                row = inflater.inflate(R.layout.fanwall_gallery_item_layout, null);

            ImageView imageView = (ImageView) row.findViewById(R.id.imageView);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(imageWidth, imageHeight);
            imageView.setLayoutParams(params);
            imageView.setTag(items.get(position).hashCode());
            setPhotoImage(imageView, position, items.get(position).hashCode());

            return row;

        } catch (Exception ex) {
            return null;
        }
    }

    private void setPhotoImage(ImageView imageHolder, int i, int uid) {
        Bitmap btm = imageMap.get(uid);
        if (btm == null || btm.getHeight() == 1) {
            imageHolder.setImageBitmap(placeHolderAvatar);
            FanWallMessage tempMsg = items.get(i);
            String avaCache = Statics.cachePath + File.separator + Utils.md5(tempMsg.getUserAvatarUrl());
            tempMsg.setUserAvatarCache(avaCache);
            addTask(imageHolder, uid, tempMsg.getText(), null, avaCache, tempMsg.getImageUrl(), -1, -1, -1);
        } else {
            imageHolder.setImageBitmap(btm);
        }
    }
}
