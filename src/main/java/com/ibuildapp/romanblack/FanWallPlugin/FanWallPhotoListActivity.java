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
package com.ibuildapp.romanblack.FanWallPlugin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import com.appbuilder.sdk.android.AppBuilderModuleMain;
import com.appbuilder.sdk.android.Utils;
import com.appbuilder.sdk.android.Widget;
import com.ibuildapp.romanblack.FanWallPlugin.adapter.ImagesAdapter;
import com.ibuildapp.romanblack.FanWallPlugin.data.FanWallMessage;
import com.ibuildapp.romanblack.FanWallPlugin.data.JSONParser;
import com.ibuildapp.romanblack.FanWallPlugin.data.Statics;

import java.io.*;
import java.util.ArrayList;


public class FanWallPhotoListActivity extends AppBuilderModuleMain implements AdapterView.OnItemClickListener {

    // constants
    private final int PREPARE_GALLERY = 10001;
    private final int GALLERY_MARGIN = 8;

    // UI
    private GridView grid;
    private ProgressDialog progressDialog = null;
    private Widget widget;
    private LinearLayout root;

    // backend
    private ImagesAdapter adapter;
    private float density;
    private ArrayList<FanWallMessage> galleryMessages;
    private Thread dataThread;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case PREPARE_GALLERY: {
                    prepareGallery();
                }
                break;
            }
        }
    };

    @Override
    public void destroy() {
        if (adapter!= null)
         adapter.clearBitmaps();
    }

    private void initializeBackend() {
        Intent bridge = getIntent();
        widget = (Widget) bridge.getSerializableExtra("widget");
    }

    private void initializeUI() {
        setContentView(R.layout.fanwall_photolist_layout);

        setTopBarTitle(getString(R.string.fanwall_photos));
        setTopBarLeftButtonText(getString(R.string.wall), true, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        density = getResources().getDisplayMetrics().density;
        root = (LinearLayout) findViewById(R.id.fanwall_photogrid_root);
        root.setBackgroundColor(Statics.color1);

        grid = (GridView) findViewById(R.id.fanwall_photogrid);
        grid.setNumColumns(2);
        grid.setNumColumns(GridView.AUTO_FIT);
        grid.setVerticalSpacing((int) (GALLERY_MARGIN * density));
        grid.setHorizontalSpacing((int) (density * GALLERY_MARGIN));
        grid.setOnItemClickListener(this);
        grid.setVerticalScrollBarEnabled(false);

        showProgressDialog();
        dataThread = new Thread(new Runnable() {
            public void run() {

                boolean loaded = false;

                if ( Utils.networkAvailable(FanWallPhotoListActivity.this) )
                {
                    galleryMessages = JSONParser.parseGalleryUrl(Statics.BASE_URL
                            + "/" + Statics.APP_ID + "/"
                            + Statics.MODULE_ID + "/gallery" + "/"
                            + com.appbuilder.sdk.android.Statics.appId + "/"
                            + com.appbuilder.sdk.android.Statics.appToken);

                    loaded = true;

                    File cacheFile = new File(Statics.cachePath);
                    if (!cacheFile.exists()) {
                        cacheFile.mkdirs();
                    }

                    File cache = new File(Statics.cachePath + "/" + "ca-0-0-gal");
                    if (cache.exists()) {
                        cache.delete();
                        try {
                            cache.createNewFile();
                        } catch (IOException iOEx) {
                            Log.d(TAG, "can't create cache file");
                        }
                    }

                    try {
                        FileOutputStream fos = new FileOutputStream(cache);
                        ObjectOutputStream oos = new ObjectOutputStream(fos);
                        oos.writeObject(galleryMessages);
                        oos.close();
                        fos.close();
                    } catch (IOException iOEx) {
                        Log.d(TAG, "can't write image to file");
                    }

                }

                if (!loaded) {
                    try {
                        FileInputStream fis = new FileInputStream(Statics.cachePath + "/" + "ca-0-0-gal");
                        ObjectInputStream ois = new ObjectInputStream(fis);
                        galleryMessages = (ArrayList<FanWallMessage>) ois.readObject();
                        ois.close();
                        fis.close();
                    } catch (FileNotFoundException fNFEx) {
                        Log.d(TAG, "can't found file");
                    } catch (IOException iOEx) {
                        Log.d(TAG, "can't read fanwall messages from file");
                    } catch (ClassNotFoundException cNFEx) {
                        Log.d(TAG, "can't cast cache file to fanwall message");
                    }
                }

                if (galleryMessages == null) {
                    galleryMessages = new ArrayList<FanWallMessage>();
                }

                handler.sendEmptyMessage(PREPARE_GALLERY);
            }
        });
        dataThread.start();
    }

    @Override
    public void create() {
        initializeBackend();
        initializeUI();
    }

    /**
     * Prepares gallery images.
     */
    private void prepareGallery() {
        adapter = new ImagesAdapter(this, grid, galleryMessages);
        grid.setAdapter(adapter);

        hideProgressDialog();
    }

    private void showProgressDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (progressDialog.isShowing()) {
                        return;
                    }
                } catch (NullPointerException nPEx) {
                }

                //progressDialog = ProgressDialog.show(FanWallPhotoListActivity.this, null, getString(R.string.common_loading_upper));
                //progressDialog.setCancelable(true);
            }
        });

    }

    private void hideProgressDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    progressDialog.dismiss();
                } catch (NullPointerException nPEx) {
                }
            }
        });

    }

    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        Intent it = new Intent(FanWallPhotoListActivity.this, ImageViewActivity.class);
        it.putExtra("messages", galleryMessages);
        it.putExtra("position", arg2);
        it.putExtra("Widget", widget);
        startActivity(it);

    }


}
