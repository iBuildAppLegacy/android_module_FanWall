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
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.appbuilder.sdk.android.AppBuilderModuleMain;
import com.appbuilder.sdk.android.Utils;
import com.ibuildapp.romanblack.FanWallPlugin.data.FanWallMessage;
import com.ibuildapp.romanblack.FanWallPlugin.data.MapWebPageCreator;
import com.ibuildapp.romanblack.FanWallPlugin.data.Statics;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class FanWallMapActivity extends AppBuilderModuleMain {

    // UI
    private WebView mapView;
    private ProgressDialog progressDialog = null;
    private LinearLayout root;

    // backend
    private ArrayList<FanWallMessage> messages = new ArrayList<FanWallMessage>();

    private void initializeBackend() {
        Intent parentIntent = getIntent();
        messages = (ArrayList<FanWallMessage>) parentIntent.getSerializableExtra("messages");
    }

    private void initializeUI() {
        setContentView(R.layout.fanwall_map_layout);

        setTopBarTitle(getString(R.string.fanwall_map));
        setTopBarLeftButtonText(getString(R.string.wall), true, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        root = (LinearLayout) findViewById(R.id.fanwall_map_root);
        root.setBackgroundColor(Statics.color1);

        mapView = (WebView) findViewById(R.id.fanwall_map_webview);
        mapView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        mapView.getSettings().setJavaScriptEnabled(true);
//        mapView.getSettings().setPluginsEnabled(true);
        mapView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                hideProgressDialog();
                super.onPageFinished(view, url);
            }
        });
    }

    @Override
    public void create() {
        initializeBackend();
        initializeUI();
    }

    @Override
    public void resume() {
        showProgressDialog();
        showMap();
    }

    private void showMap() {
        String htmlSource = "";
        try {
            InputStream is = getResources().openRawResource(R.raw.romanblack_fanwall_mappage_new);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int flag = is.read();
            while (flag != -1) {
                baos.write(flag);
                flag = is.read();
            }
            htmlSource = baos.toString();
        } catch (IOException iOEx) {
            Log.e("", "");
        }

        htmlSource = MapWebPageCreator.createMapPage(htmlSource, messages);
        mapView.loadDataWithBaseURL("", htmlSource, "text/html", "utf-8", "");

        if (!Utils.networkAvailable(FanWallMapActivity.this))
            Toast.makeText(this, R.string.alert_no_internet, Toast.LENGTH_LONG).show();
    }

    private void showProgressDialog() {
        try {
            if (progressDialog.isShowing()) {
                return;
            }
        } catch (NullPointerException nPEx) {
        }

        progressDialog = ProgressDialog.show(this, null, getString(R.string.common_loading_upper));
        progressDialog.setCancelable(true);
    }

    private void hideProgressDialog() {
        try {
            progressDialog.dismiss();
        } catch (NullPointerException nPEx) {
        }
    }
}
