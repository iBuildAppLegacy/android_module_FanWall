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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.*;
import android.provider.MediaStore;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import com.appbuilder.sdk.android.AppBuilderModuleMain;
import com.appbuilder.sdk.android.DialogSharing;
import com.appbuilder.sdk.android.Utils;
import com.appbuilder.sdk.android.Widget;
import com.appbuilder.sdk.android.authorization.Authorization;
import com.ibuildapp.romanblack.FanWallPlugin.data.FanWallMessage;
import com.ibuildapp.romanblack.FanWallPlugin.data.Statics;
import com.ibuildapp.romanblack.FanWallPlugin.view.ImagesBottomPanel;
import com.ibuildapp.romanblack.FanWallPlugin.view.ImagesWebView;

import java.io.*;
import java.util.ArrayList;

/**
 * This activity represents gallery details page.
 */
public class ImageViewActivity extends AppBuilderModuleMain implements OnTouchListener,
        DialogInterface.OnCancelListener {

    private Widget widget = null;
    private ArrayList<FanWallMessage> items = new ArrayList<FanWallMessage>();
    private int position = 0;
    private int oldPosition = 0;
    private boolean tapsZoomIn = true;
    private long sharingPostId = -1;
    private int imageHeight = 0;
    private int imageWidth = 0;
    private ProgressDialog progressDialog = null;
    private DisplayMetrics metrix = new DisplayMetrics();
    private GestureDetector gd = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (tapsZoomIn) {
                zoomImage();
                tapsZoomIn = false;
            } else {
                zoomImageOut();
                tapsZoomIn = true;
            }

            return true;
        }
    });
    final private int SLIDE_TO_RIGHT_START = 0;
    final private int SLIDE_TO_LEFT_START = 2;
    final private int SLIDE_COMPLETE = 3;
    final private int SHOW_DESCRIPTION = 4;
    final private int HIDE_DESCRIPTION = 7;
    final private int HIDE_INFO = 5;
    final private int SHOW_INFO = 6;
    final private int CHECK_CONTROLS_STATE = 8;
    final private int SAVE_IMAGE = 9;
    final private int SHOW_SAVE_IMAGE_DIALOG = 10;
    final private int SHOW_IMAGE = 11;
    final private int SHOW_PROGRESS_DIALOG = 12;
    final private int HIDE_PROGRESS_DIALOG = 13;
    final private int TEST_SCROLL = 1000;

    final private int TWITTER_PUBLISH_ACTIVITY = 1002;
    final private int FACEBOOK_PUBLISH_ACTIVITY = 1003;
    final private int TWITTER_AUTHORIZATION_ACTIVITY = 2002;
    final private int FACEBOOK_AUTHORIZATION_ACTIVITY = 2003;
    private int topBarHeight = 0;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case SLIDE_TO_LEFT_START: {
                    if (position < items.size() - 1) {
                        oldPosition = position;
                        position++;
                        storePosition();
                        slideImage(-500);
                    }
                }
                break;
                case SLIDE_TO_RIGHT_START: {
                    if (position > 0) {
                        oldPosition = position;
                        position--;
                        storePosition();
                        slideImage(500);
                    }
                }
                break;
                case SLIDE_COMPLETE: {
                    ImageViewActivity.this.onCreate(ImageViewActivity.this.state);
                }
                break;
                case SHOW_DESCRIPTION: {
                    showDescription();
                }
                break;
                case HIDE_DESCRIPTION: {
                    hideDescription();
                }
                break;
                case HIDE_INFO: {
                    hideInfo();
                }
                break;
                case SHOW_INFO: {
                    showInfo();
                    ImageViewActivity.this.visibleTopBar();
                }
                break;
                case CHECK_CONTROLS_STATE: {
                    checkControlsState();
                }
                break;
                case SAVE_IMAGE: {
                    saveImage();
                }
                break;
                case SHOW_SAVE_IMAGE_DIALOG: {
                    showSaveImageDialog();
                }
                break;
                case SHOW_IMAGE: {
                    showImage();
                }
                break;
                case TEST_SCROLL: {
                    scrollView.smoothScrollTo(displayHeigh, 0);
                }
                break;
                case SHOW_PROGRESS_DIALOG: {
                    showProgressDialog();
                }
                break;
                case HIDE_PROGRESS_DIALOG: {
                    hideProgressDialog();
                }
                break;
            }
        }
    };
    private ArrayList<ImagesWebView> webViews = new ArrayList<ImagesWebView>();
    private TextView imageTitle = null;
    private RelativeLayout layoutDescription = null;
    private TextView imageDescription = null;
    private ImagesBottomPanel bottomPanel = null;
    private ImageView infoButton = null;
    private ImageView downloadButton = null;
    private HorizontalScrollView scrollView = null;
    private LinearLayout linearLayout = null;
    private float startPosX = 0;
    private float startPosY = 0;
    private int displayWidth = 0;
    private int displayHeigh = 0;
    private boolean canSaveToSD = false;
    private int activeTime = 4;
    private boolean wasSecondFinger = false;
    private boolean lastTimeWasActionUp = false;

    FrameLayout mainLayout;

    @Override
    public void hideTopBar() {
        super.hideTopBar();
        linearLayout.setPadding(0,0,0,0);
    }

    @Override
    public void visibleTopBar() {
        super.visibleTopBar();

        linearLayout.setPadding(0, -topBarHeight, 0, 0);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (topBarHeight == 0){
            topBarHeight= getTopBar().getMeasuredHeight();
            visibleTopBar();
        }
    }

    @Override
    public void create() {
        try {
            setContentView(R.layout.romanblack_fanwall_images_details);
            setTopBarLeftButtonText(getResources().getString(R.string.common_back_upper), true, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
            setTopBarTitle("");
            displayWidth = getWindowManager().getDefaultDisplay().getWidth();
            displayHeigh = getWindowManager().getDefaultDisplay().getHeight();

            getWindowManager().getDefaultDisplay().getMetrics(metrix);

            if (hasAdView()) {
                if (getAdvType().equalsIgnoreCase("html")) {
                    displayHeigh = displayHeigh - 50;
                } else {
                    displayHeigh = displayHeigh - (int) (50 * metrix.density);
                }
            }

            Intent currentIntent = getIntent();
            Bundle store = currentIntent.getExtras();
            items = (ArrayList) store.getSerializable("messages");
            position = store.getInt("position");

            try {
                if (items.get(position).getImageCachePath().length() == 0) {
                    String cacheFilePath = Statics.cachePath + File.separator + Utils.md5(items.get(position).getImageUrl());
                    if (!new File(cacheFilePath).exists())
                        finish();
                    else {
                        items.get(position).setImageCachePath(cacheFilePath);
                    }
                }
            } catch (Exception e) {
                finish();
            }

            Integer session = (Integer) getSession();
            if (session != null) {
                position = session.intValue();
            }

            canSaveToSD = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());

            mainLayout = (FrameLayout) findViewById(R.id.gallery_main);
            try {
                widget = (Widget) store.getSerializable("Widget");
                mainLayout.setBackgroundColor(store.getInt("backgroundColor"));
            } catch (Exception e) {
            }

            linearLayout = (LinearLayout) findViewById(R.id.romanblack_fanwall_images_gallery_linearlayout);
            visibleTopBar();

            Display display = getWindowManager().getDefaultDisplay();
            imageHeight = display.getHeight();
            imageWidth = display.getWidth();

            bottomPanel = (ImagesBottomPanel) findViewById(R.id.romanblack_fanwall_images_bottom_panel);

            infoButton = (ImageView) findViewById(R.id.romanblack_fanwall_gallery_image_info);
            infoButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    handler.sendEmptyMessage(SHOW_DESCRIPTION);
                }
            });


            downloadButton = (ImageView) findViewById(R.id.romanblack_fanwall_gallery_btn_save_image);
            downloadButton.setVisibility(View.VISIBLE);
            downloadButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {

                    showDialogSharing(new DialogSharing.Configuration.Builder()
                                    .setFacebookSharingClickListener(new DialogSharing.Item.OnClickListener() {
                                        @Override
                                        public void onClick() {
                                            // checking Internet connection
                                            if (!Utils.networkAvailable(ImageViewActivity.this))
                                                Toast.makeText(ImageViewActivity.this, getResources().getString(R.string.alert_no_internet), Toast.LENGTH_SHORT).show();
                                            else {
                                                if (Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_FACEBOOK) != null) {
                                                    shareFacebook();
                                                } else {
                                                    Authorization.authorize(ImageViewActivity.this, FACEBOOK_AUTHORIZATION_ACTIVITY, Authorization.AUTHORIZATION_TYPE_FACEBOOK);
                                                }
                                            }
                                        }
                                    })
                                    .setTwitterSharingClickListener(new DialogSharing.Item.OnClickListener() {
                                        @Override
                                        public void onClick() {
                                            // checking Internet connection
                                            if (!Utils.networkAvailable(ImageViewActivity.this))
                                                Toast.makeText(ImageViewActivity.this, getResources().getString(R.string.alert_no_internet), Toast.LENGTH_SHORT).show();
                                            else {
                                                if (Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_TWITTER) != null) {
                                                    shareTwitter();
                                                } else {
                                                    Authorization.authorize(ImageViewActivity.this, TWITTER_AUTHORIZATION_ACTIVITY, Authorization.AUTHORIZATION_TYPE_TWITTER);
                                                }
                                            }
                                        }
                                    })
                                    .setSavePictureSharingClickListener(new DialogSharing.Item.OnClickListener() {
                                        @Override
                                        public void onClick() {
                                            saveImage();
                                        }
                                    })
                                    .build()
                    );


                }
            });


            imageTitle = (TextView) findViewById(R.id.romanblack_fanwall_gallery_image_title);

            layoutDescription = (RelativeLayout) findViewById(R.id.romanblack_fanwall_image_description_panel);
            layoutDescription.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    handler.sendEmptyMessage(SHOW_DESCRIPTION);
                }
            });
            imageDescription = (TextView) findViewById(R.id.romanblack_fanwall_images_description);

            handler.sendEmptyMessage(SHOW_PROGRESS_DIALOG);

            for (int i = 0; i < items.size(); i++) {
                ImagesWebView webView = new ImagesWebView(this);

                webView.setBackgroundColor(Color.TRANSPARENT);

                webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
                webView.setListener(this);
                webView.setOnTouchListener(this);

                webView.getSettings().setSupportZoom(true);
                webView.getSettings().setBuiltInZoomControls(true);

                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                        super.onReceivedError(view, errorCode, description, failingUrl);
                    }

                    @Override
                    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                        super.onReceivedSslError(view, handler, error);
                    }
                });
                webViews.add(webView);

                linearLayout.addView(webView, new LinearLayout.LayoutParams(displayWidth, displayHeigh));

            }

            showPosition(-1);

            handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);

            scrollView = (HorizontalScrollView) findViewById(R.id.romanblack_fanwall_images_scrollview);
            scrollView.setOnTouchListener(this);
            scrollView.setBackgroundColor(Color.TRANSPARENT);

            showImage();

        } catch (Throwable ex) { // Error Logging
            Log.e("Error", "exception", ex);
        }
    }

    /**
     * Shows the image in given position.
     *
     * @param oldPosition the image position
     */
    private void showPosition(int oldPosition) {
        try {
            if ((oldPosition != (position + 1)) || (oldPosition != (position - 1))) {
                String s = prepageImageHTML(position);
                webViews.get(position).loadDataWithBaseURL("", s,
                        "text/html", "utf-8", "");
            }

            if (position != 0) {
                String s = prepageImageHTML(position - 1);
                webViews.get(position-1).loadDataWithBaseURL("",
                        s, "text/html", "utf-8", "");

            }


        }catch (Throwable e){
            Log.e("Error", "exception", e);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        onCreate(ImageViewActivity.this.state);
        super.onConfigurationChanged(newConfig);

        displayWidth = getWindowManager().getDefaultDisplay().getWidth();
        displayHeigh = getWindowManager().getDefaultDisplay().getHeight();

        linearLayout.removeAllViews();
        webViews = new ArrayList<ImagesWebView>();

        for (int i = 0; i < items.size(); i++) {
            ImagesWebView webView = new ImagesWebView(this);

            webView.setBackgroundColor(Color.TRANSPARENT);

            webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
            webView.setListener(this);
            webView.setOnTouchListener(this);

            webView.getSettings().setSupportZoom(true);
            webView.getSettings().setBuiltInZoomControls(true);
//            webView.refreshPlugins(true);

            webViews.add(webView);

            linearLayout.addView(webView, new LinearLayout.LayoutParams(
                    displayWidth, displayHeigh));
        }

        showPosition(-1);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        try {
            if (v == scrollView) {
                webViews.get(position).onTouchEvent(event);
                return true;

            } else {

                ImagesWebView webView = (ImagesWebView) v;

                gd.onTouchEvent(event);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        lastTimeWasActionUp = false;
                        startPosX = event.getX();
                        startPosY = event.getY();
                    }
                    case MotionEvent.ACTION_MOVE: {
                        lastTimeWasActionUp = false;
                        return false;
                    }
                    case MotionEvent.ACTION_UP: {
                        if (wasSecondFinger) {
                            wasSecondFinger = false;
                            lastTimeWasActionUp = true;
                            return false;
                        } else if (!lastTimeWasActionUp) {
                            if ((event.getX() - startPosX) > (displayWidth * 3 / 8)) {
                                if (webView.isOverScrollLeft()) {
                                    handler.sendEmptyMessage(SLIDE_TO_RIGHT_START);
                                    return true;
                                }
                            } else if (startPosX - event.getX() > displayWidth * 3 / 8) {
                                if (webView.isOverScrollRight()) {
                                    handler.sendEmptyMessage(SLIDE_TO_LEFT_START);
                                    return true;
                                }
                            } else if ((Math.abs(startPosX - event.getX()) < 10)
                                    && (Math.abs(startPosY - event.getY()) < 10)) {
                                hideDescription();
                                if (event.getY() >= (metrix.heightPixels - 65 * metrix.density)) {
                                    return false;
                                }
                                if (bottomPanel.getVisibility() == View.GONE
                                        || bottomPanel.getVisibility() == View.INVISIBLE) {
                                    showInfo();
                                    ImageViewActivity.this.visibleTopBar();
                                    return true;
                                }
                            } else {
                                return false;
                            }
                        }
                    }
                    break;
                    case MotionEvent.ACTION_POINTER_1_DOWN: { // finger 1
                        lastTimeWasActionUp = false;
                        wasSecondFinger = true;
                        return false;
                    }
                    case MotionEvent.ACTION_POINTER_1_UP: {
                        lastTimeWasActionUp = false;
                        wasSecondFinger = true;
                        return false;
                    }
                    case MotionEvent.ACTION_POINTER_2_DOWN: { // finger 2
                        lastTimeWasActionUp = false;
                        wasSecondFinger = true;
                        return false;
                    }
                    case MotionEvent.ACTION_POINTER_2_UP: {
                        lastTimeWasActionUp = false;
                        wasSecondFinger = true;
                        return false;
                    }
                }
                return true;

            }

        } catch (Throwable ex) { // Error Logging
            Log.e("Error", "exception", ex);
            return false;
        }
    }

    /* PRIVATE METHODS */

    /**
     * Checks if need to hide control panel.
     */
    private void checkControlsState() {
        if (activeTime > 0) {
            activeTime--;
            handler.sendEmptyMessageDelayed(CHECK_CONTROLS_STATE, 1000);
        } else {
            handler.sendEmptyMessageDelayed(HIDE_INFO, 1000);
        }
    }

    /**
     * Hides the control panel.
     */
    private void hideInfo() {
        bottomPanel.setVisibility(View.INVISIBLE);
        ImageViewActivity.this.hideTopBar();
    }

    /**
     * Shows the control panel.
     */
    private void showInfo() {
        try { //ErrorLogging

            if (items.get(position).getAuthor().length() == 0 && items.get(position).getText().length() == 0) {
                bottomPanel.setVisibility(View.INVISIBLE);
            } else {
                if (items.get(position).getText().length() == 0) {
                    infoButton.setVisibility(View.INVISIBLE);
                } else {
                    infoButton.setVisibility(View.VISIBLE);
                }
                bottomPanel.setVisibility(View.VISIBLE);
            }
            activeTime = 4;
            checkControlsState();

        } catch (Throwable ex) { // Error Logging
            Log.e("Error", "exception", ex);
        }
    }

    /**
     * Stores current position.
     */
    private void storePosition() {
        setSession(new Integer(position));
    }

    /**
     * Prepares and shows the current image.
     */
    private void showImage() {
        try {

            hideInfo();

            setTitle((position + 1) + " " + getString(R.string.romanblack_fanwall_from) + " " + items.size());
            imageTitle.setText(getString(R.string.romanblack_fanwall_posted_by) + " " + items.get(position).getAuthor());
            imageDescription.setText(items.get(position).getText());

            showInfo();
            ImageViewActivity.this.visibleTopBar();
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    scrollView.scrollTo(displayWidth * position, 0);
                }
            }, 100);

        } catch (Throwable ex) { // Error Logging
            Log.e("Error", "exception", ex);
        }
    }

    /**
     * Prepares the image HTML code.
     *
     * @param position the image position
     * @return the HTML code
     */
    private String prepageImageHTML(int position) {
        try {
            if (items.get(position).getImageCachePath().length() == 0) {
                String cacheFilePath = Statics.cachePath + File.separator + Utils.md5(items.get(position).getImageUrl());
                if (!new File(cacheFilePath).exists())
                    finish();
                else {
                    items.get(position).setImageCachePath(cacheFilePath);
                }
            }
            Bitmap srcImg = null;
            try {
              srcImg  = BitmapFactory.decodeFile(items.get(position).getImageCachePath());
                if (srcImg == null){
                    Log.e("decoding", "exception");
                    return "";
                }
            }
            catch(Throwable e ){
                Log.e("decoding", "exception", e);
            }

            float h1 = (displayHeigh / metrix.density);
            h1 = h1 - 56;
            if (hasAdView()) {
                h1 = h1 - 50;
            }

            float w1 = (displayWidth / metrix.density);
            w1 = w1 - 6;
            float w2 = srcImg.getWidth();
            float perWigth = w1 / w2;

            boolean byWidth = false;
            if (((displayHeigh / metrix.density) - 56) >= (srcImg.getHeight() * perWigth)) {
                byWidth = true;
            }

            StringBuilder html = new StringBuilder();
            html.append("<html>" + "<head></head>" + "<body bottommargin=\"0\" "
                    + "leftmargin=\"0\" " + "rightmargin=\"0\" ");
            if (byWidth) {
                html.append("topmargin=\"");
                html.append(((displayHeigh / metrix.density) - 56) / 2
                        - (srcImg.getHeight() * perWigth) / 2);
                html.append("\" ");
            } else {
                html.append("topmargin=\"0\" ");
            }

            html.append("\">");

            html.append("<table align=\"center\"><tr><td>");
            html.append("<img src=\"file://");
            html.append(items.get(position).getImageCachePath());
            html.append("\" ");

            if (byWidth) {
                html.append("width=\"");
                html.append((displayWidth / metrix.density) - 6);
                html.append("\" ");
                html.append("/>");
            } else {
                html.append("height=\"");
                html.append((displayHeigh / metrix.density) - 56);
                html.append("\" ");
                html.append("/>");
            }
            html.append("</td></tr></table>");
            html.append("</body>" + "</html>");

            return html.toString();

        } catch (Throwable e) {
            Log.e("Error", "exception", e);
           return e.getMessage();
        }
    }

    /**
     * Slides the image.
     *
     * @param pos
     */
    private void slideImage(int pos) {
        try {//ErrorLogging
            hideInfo();
            setTitle((position + 1) + " " + getString(R.string.romanblack_fanwall_from) + " " + items.size());
            imageTitle.setText(getString(R.string.romanblack_fanwall_posted_by) + " " + items.get(position).getAuthor());
            imageDescription.setText(items.get(position).getText());

            new Handler().postDelayed(new Runnable() {
                public void run() {
                    scrollView.smoothScrollTo(displayWidth * position, 0);
                }
            }, 10);

            SystemClock.sleep(10);

            showPosition(oldPosition);

            showInfo();
            ImageViewActivity.this.visibleTopBar();
        } catch (Throwable ex) { // Error Logging
            Log.e("Error", "exception", ex);
        }
    }

    /**
     * Shows the image description.
     */
    private void showDescription() {
        if (layoutDescription.getVisibility() == View.VISIBLE) {
            layoutDescription.setVisibility(View.INVISIBLE);
        } else {
            layoutDescription.setVisibility(View.VISIBLE);

            handler.sendEmptyMessageDelayed(HIDE_DESCRIPTION, 10000);
        }
    }

    /**
     * Hides the image description.
     */
    private void hideDescription() {
        layoutDescription.setVisibility(View.INVISIBLE);
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
        progressDialog.setOnCancelListener(this);
    }

    private void hideProgressDialog() {
        try {
            progressDialog.dismiss();
        } catch (NullPointerException nPEx) {
        }
    }

    /**
     * Prepares and shows save image dialog.
     */
    private void showSaveImageDialog() {
        try {//ErrorLogging

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage(R.string.romanblack_fanwall_dialog_save_image);
            dialog.setCancelable(false);
            dialog.setPositiveButton(getString(R.string.common_yes_upper), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    handler.sendEmptyMessage(SAVE_IMAGE);
                }
            }).setNegativeButton(getString(R.string.common_no_upper), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            AlertDialog alert = dialog.create();
            alert.show();
            TextView messageText = (TextView) alert.findViewById(android.R.id.message);
            messageText.setGravity(Gravity.CENTER);

        } catch (Throwable ex) { // Error Logging
            Log.e("Error", "exception", ex);
        }
    }

    /**
     * Zooms the current image in.
     */
    private void zoomImage() {
        webViews.get(position).zoomIn();
        webViews.get(position).zoomIn();
        webViews.get(position).zoomIn();
    }

    /**
     * Zooms the current image out.
     */
    private void zoomImageOut() {
        webViews.get(position).zoomOut();
        webViews.get(position).zoomOut();
        webViews.get(position).zoomOut();
    }

    public void onCancel(DialogInterface arg0) {
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    /**
     * Starts SharingActivity to share on Twitter.
     */
    private void shareTwitter() {
        Intent it = new Intent(ImageViewActivity.this, SharingActivity.class);

        // pass the picture path and start the activity
        it.putExtra("type", "twitter");
        it.putExtra("image_url", items.get(position).getImageUrl());
        sharingPostId = items.get(position).getId();
        startActivityForResult(it, TWITTER_PUBLISH_ACTIVITY);
    }

    /**
     * Starts SharingActivity to share on Facebook.
     */
    private void shareFacebook() {
        Intent it = new Intent(ImageViewActivity.this, SharingActivity.class);

        // pass the picture path and start the activity
        it.putExtra("type", "facebook");
        it.putExtra("image_url", items.get(position).getImageUrl());
        sharingPostId = items.get(position).getId();
        startActivityForResult(it, FACEBOOK_PUBLISH_ACTIVITY);
    }

    /**
     * Saves the current image to SD card.
     */
    private void saveImage() {
        try {//ErrorLogging

            if (!canSaveToSD)
                return;

            if (TextUtils.isEmpty(items.get(position).getImageCachePath())) {
                return;
            }

            try {
                Bitmap btm = BitmapFactory.decodeFile(items.get(position).getImageCachePath());
                MediaStore.Images.Media.insertImage(getContentResolver(), btm,
                        Utils.md5(items.get(position).getText()), "");

                Toast.makeText(this, getString(R.string.fanwall_image_successfully_loaded), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.w("ImageDetails -->", "Error copying image " + e);
            } finally {
            }

        } catch (Exception ex) { // Error Logging
            Log.e(TAG, "");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case FACEBOOK_AUTHORIZATION_ACTIVITY: {
                if (resultCode == RESULT_OK)
                    shareFacebook();
                else if (resultCode == RESULT_CANCELED)
                    Toast.makeText(ImageViewActivity.this, getResources().getString(R.string.alert_facebook_auth_error), Toast.LENGTH_SHORT).show();
            }
            break;
            case TWITTER_AUTHORIZATION_ACTIVITY: {
                if (resultCode == RESULT_OK)
                    shareTwitter();
                else if (resultCode == RESULT_CANCELED)
                    Toast.makeText(ImageViewActivity.this, getResources().getString(R.string.alert_twitter_auth_error), Toast.LENGTH_SHORT).show();
            }
            break;

            case TWITTER_PUBLISH_ACTIVITY: {
                if (resultCode == RESULT_OK) {
                    Toast.makeText(ImageViewActivity.this, getResources().getString(R.string.directoryplugin_twitter_posted_success), Toast.LENGTH_LONG).show();
                    new Thread( new Runnable() {
                        @Override
                        public void run() {
                            Statics.incrementSharing(Long.toString(sharingPostId));
                        }
                    }).start();
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(ImageViewActivity.this, getResources().getString(R.string.directoryplugin_twitter_posted_error), Toast.LENGTH_LONG).show();
                }
            }
            break;

            case FACEBOOK_PUBLISH_ACTIVITY: {
                if (resultCode == RESULT_OK) {
                    Toast.makeText(ImageViewActivity.this, getResources().getString(R.string.directoryplugin_facebook_posted_success), Toast.LENGTH_LONG).show();
                    new Thread( new Runnable() {
                        @Override
                        public void run() {
                            Statics.incrementSharing(Long.toString(sharingPostId));
                        }
                    }).start();
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(ImageViewActivity.this, getResources().getString(R.string.directoryplugin_facebook_posted_error), Toast.LENGTH_LONG).show();
                }
            }
            break;
        }
    }
}
