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

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.*;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.*;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.*;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.appbuilder.sdk.android.AppBuilderModuleMain;
import com.appbuilder.sdk.android.DialogSharing;
import com.appbuilder.sdk.android.StartUpActivity;
import com.appbuilder.sdk.android.Utils;
import com.appbuilder.sdk.android.Widget;
import com.appbuilder.sdk.android.authorization.Authorization;
import com.appbuilder.sdk.android.authorization.FacebookAuthorizationActivity;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.ibuildapp.romanblack.FanWallPlugin.adapter.MainLayoutMessagesAdapter;
import com.ibuildapp.romanblack.FanWallPlugin.callback.OnAuthListener;
import com.ibuildapp.romanblack.FanWallPlugin.data.*;
import com.ibuildapp.romanblack.FanWallPlugin.data.DAO.MessagesDAO;
import com.ibuildapp.romanblack.FanWallPlugin.view.SoftKeyboard;


import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

/**
 * Main module class. Module entry point. Represents fan wall widget.
 */
@StartUpActivity(moduleName = "FanWall")
public class FanWallPlugin extends AppBuilderModuleMain implements OnCancelListener,
        View.OnClickListener, OnAuthListener, TextWatcher {

    private final int SHOW_MAP_ACTIVITY = 20000;
    private final int SHOW_PHOTOLIST_ACTIVITY = 20001;
    public final int SHOW_IMAGES_ACTIVITY = 20002;
    private final int IMAGE_ADDED_SIZE = 100;


    // constants
    private final int FACEBOOK_AUTHORIZATION_ACTIVITY = 10031;
    private final int TWITTER_AUTHORIZATION_ACTIVITY = 10032;
    private final int TWITTER_PUBLISH_ACTIVITY = 10033;
    private final int FACEBOOK_PUBLISH_ACTIVITY = 10034;

    private final String logname = "FanWall";
    private final String TAG = "com.ibuildapp.FanWallPlugin";
    private final int AUTHORIZATION_ACTIVITY = 10000;
    private final int MESSAGE_VIEW_ACTIVITY = 10001;
    private final int IMAGE_VIEW_ACTIVITY = 10002;
    private final int SEND_MESSAGE_ACTIVITY = 10003;
    private final int TAKE_A_PICTURE_ACTIVITY = 10007;
    private final int PICK_IMAGE_ACTIVITY = 10008;
    private final int CLEAR_MSG_TEXT = 10009;
    public final int FACEBOOK_LIKE_AUTH = 10021;
    public final int GPS_SETTINGS_ACTIVITY = 10022;

    private final int SIGN_UP_ACTIVITY = 10005;
    private final int NEED_INTERNET_CONNECTION = 0;
    private final int SEND_ERROR = 1;


    private final int SHOW_PROGRESS_DIALOG = 22;
    private final int HIDE_PROGRESS_DIALOG = 33;

    private final int SHOW_NO_MESSAGES = 4;
    private final int SHOW_MESSAGES = 5;
    private final int INITIALIZATION_FAILED = 7;
    private final int NO_GPS_SERVICE = 19;
    private final int LIST_PROGRESS_COMPLITE = 10011;
    private final int HANDLE_TAP_BAR = 10012;
    private static final int CAMERA_REQUEST = 1008;
    private static final int STORAGE_REQUEST = 1009;


    //backend
    public long postIdToShare;
    public String urlToLike;


    private float density;
    private Animation showProgress;
    private Animation hideProgress;

    private enum ACTIONS {

        ACTION_NO, SEND_MESSAGE, SEND_MESSAGE_FROM_WALL
    }

    private ACTIONS action = ACTIONS.ACTION_NO;
    private Intent actionIntent = null;
    private boolean refreshingBottom = false;

    private Intent currentIntent;
    private Widget widget = null;
    private ArrayList<FanWallMessage> messages = new ArrayList<FanWallMessage>();
    private Resources res;
    private MainLayoutMessagesAdapter adapter;
    private String imagePath;
    private LocationManager locationManager;


    // UI
    private LinearLayout tabHolder;
    private ProgressDialog progressDialog;
    private LinearLayout mainlLayout = null;
    private LinearLayout bottomBarHodler;

    private LinearLayout tabMapLayout = null;
    private LinearLayout tabPhotosLayout = null;
    private LinearLayout noMessagesLayout = null;
    private FrameLayout messageListLayoutRoot = null;
    private PullToRefreshListView messageList;
    private LinearLayout openBottom;
    private LinearLayout postMsg;
    private EditText editMsg;
    private LinearLayout imageHolder;
    private ImageView userImage;
    private ImageView closeBtn;

    private LinearLayout chooserHolder;
    private CheckBox enableGpsCheckbox;
    private LinearLayout galleryChooser;
    private LinearLayout photoChooser;

    private String complainOk = "";
    private String complainFalse = "";
    private String complainNoInternet = "";

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case NEED_INTERNET_CONNECTION: {
                    Toast.makeText(FanWallPlugin.this, R.string.alert_no_internet, Toast.LENGTH_LONG).show();
                }
                break;

                case CLEAR_MSG_TEXT: {
                    editMsg.setText("");
                    imagePath = "";
                    imageHolder.setVisibility(View.GONE);
                    hideProgressDialog();
                    hideKeyboard();
                }
                break;

                case HANDLE_TAP_BAR:
                {
                    if (msg.arg1 == 1)
                        tabHolder.setVisibility(View.VISIBLE);
                    else if ( msg.arg1 == 0 )
                        tabHolder.setVisibility(View.GONE);
                } break;

                case SEND_ERROR: {
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

                case LIST_PROGRESS_COMPLITE: {
                    messageList.onRefreshComplete();
                }
                break;

                case SHOW_NO_MESSAGES: {
                    showNoMessages();
                }
                break;

                case SHOW_MESSAGES: {
                    showMessages();
                }
                break;

                case INITIALIZATION_FAILED: {
                    Toast.makeText(FanWallPlugin.this, R.string.alert_cannot_init, Toast.LENGTH_LONG).show();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            finish();
                        }
                    }, 3000);
                }
                break;

                case NO_GPS_SERVICE: {
                    Toast.makeText(FanWallPlugin.this, R.string.fanwall_alert_no_gps, Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    };

    private void initializeBackend() {
        // parse input xml data
        res = getResources();
        density = res.getDisplayMetrics().density;

        showProgress = AnimationUtils.loadAnimation(FanWallPlugin.this, R.anim.show_progress_anim);
        hideProgress = AnimationUtils.loadAnimation(FanWallPlugin.this, R.anim.hide_progress_anim);

  /*      currentIntent = getIntent();
        widget = (Widget) currentIntent.getSerializableExtra("Widget");*/
        Intent currentIntent = getIntent();
        Bundle store = currentIntent.getExtras();
        widget = (Widget) store.getSerializable("Widget");
        String tempCachePath = widget.getCachePath();
        if (tempCachePath.equals("")) {
            tempCachePath = getExternalCacheDir().getAbsolutePath()+"/cache/";
        }

        EntityParser parser = new EntityParser();
        try {
            if (TextUtils.isEmpty(widget.getPluginXmlData())) {
                 if (widget.getPathToXmlFile().length() == 0) {
                    handler.sendEmptyMessageDelayed(INITIALIZATION_FAILED, 3000);
                    return;
                }
            }

            if (!TextUtils.isEmpty(widget.getPluginXmlData())) {
                parser.parse(widget.getPluginXmlData());
            } else {
                String xmlData = readXmlFromFile(widget.getPathToXmlFile());
                parser.parse(xmlData);
            }
        } catch (Exception e) {
            handler.sendEmptyMessageDelayed(INITIALIZATION_FAILED, 3000);
            return;
        }

        Statics.hasAd = widget.isHaveAdvertisement();
        Statics.appName = widget.getAppName();
        Statics.near = parser.getNear();
        Statics.MODULE_ID = parser.getModuleId();
        Statics.canEdit = parser.getCanEdit();
        Statics.APP_ID = parser.getAppId();

        Statics.color1 = parser.getColor1();
        Statics.color2 = parser.getColor2();
        Statics.color3 = parser.getColor3();
        Statics.color4 = parser.getColor4();
        Statics.color5 = parser.getColor5();
        if (Statics.BackColorToFontColor(Statics.color1) == Color.WHITE)
            Statics.isSchemaDark = true;
        else
            Statics.isSchemaDark = false;

        // init cache path
        if (!TextUtils.isEmpty(tempCachePath))
            Statics.cachePath = tempCachePath + "/fanwall-" + widget.getOrder();

        Statics.onAuthListeners.add(this);
    }

    @Override
    public void onBackPressed() {
        //editMsg.clearFocus();

        super.onBackPressed();
    }

    private void initializeUI() {
        setContentView(R.layout.romanblack_fanwall_main);

        mainlLayout = (LinearLayout) findViewById(R.id.romanblack_fanwall_main);
        mainlLayout.setBackgroundColor(Statics.color1);

        // less then android L
        if (android.os.Build.VERSION.SDK_INT <= 20) {
            InputMethodManager im = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);
            SoftKeyboard softKeyboard;
            softKeyboard = new SoftKeyboard(mainlLayout, im);
            softKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged()
            {

                @Override
                public void onSoftKeyboardHide()
                {
                    Message msg = handler.obtainMessage(HANDLE_TAP_BAR, 1,0);
                    handler.sendMessage(msg);
                }

                @Override
                public void onSoftKeyboardShow()
                {
                    Message msg = handler.obtainMessage(HANDLE_TAP_BAR, 0,0);
                    handler.sendMessage(msg);
                }
            });
        }

        bottomBarHodler = (LinearLayout) findViewById(R.id.romanblack_fanwall_main_bottom_bar);

        TextView noMsgText = (TextView) findViewById(R.id.romanblack_fanwall_nomessages_text);

        // top bar
        setTopBarLeftButtonText(getResources().getString(R.string.common_home_upper), true, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // set title
        if (TextUtils.isEmpty(widget.getTitle()))
            setTopBarTitle(getString(R.string.fanwall_talks));
        else
            setTopBarTitle(widget.getTitle());

        imageHolder = (LinearLayout) findViewById(R.id.fanwall_image_holder);
        userImage = (ImageView) findViewById(R.id.fanwall_user_image);
        closeBtn = (ImageView) findViewById(R.id.fanwall_close_image);
        closeBtn.setOnClickListener(this);

        chooserHolder = (LinearLayout) findViewById(R.id.fanwall_chooser_holder);
        openBottom = (LinearLayout) findViewById(R.id.romanblack_fanwall_open_bottom);
        openBottom.setOnClickListener(this);

        enableGpsCheckbox = (CheckBox) findViewById(R.id.romanblack_fanwall_enable_gps_checkbox);
        enableGpsCheckbox.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if ( b )
                {
                     if (locationManager!=null) {
                    if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(FanWallPlugin.this);
                        builder.setMessage(getString(R.string.enable_gps_msg))
                                .setCancelable(false)
                                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                        startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), GPS_SETTINGS_ACTIVITY);
                                    }
                                })
                                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                        enableGpsCheckbox.setChecked(false);
                                        dialog.dismiss();
                                    }
                                });
                        final AlertDialog alert = builder.create();
                        alert.show();
                    } } else
                    {
                        Prefs.with(FanWallPlugin.this).save(Prefs.KEY_GPS, true );
                    }
                }
                else
                    Prefs.with(FanWallPlugin.this).save(Prefs.KEY_GPS, false );

            }
        });
        enableGpsCheckbox.setChecked(Prefs.with(FanWallPlugin.this).getBoolean(Prefs.KEY_GPS, false));


        galleryChooser = (LinearLayout) findViewById(R.id.romanblack_fanwall_gallery);
        galleryChooser.setOnClickListener(this);
        photoChooser = (LinearLayout) findViewById(R.id.romanblack_fanwall_make_photo);
        photoChooser.setOnClickListener(this);

        postMsg = (LinearLayout) findViewById(R.id.romanblack_fanwall_send_post);
        postMsg.setOnClickListener(this);
        editMsg = (EditText) findViewById(R.id.romanblack_fanwall_edit_msg);
        editMsg.addTextChangedListener(FanWallPlugin.this);

        tabHolder = (LinearLayout) findViewById(R.id.romanblack_fanwall_tab_holder);
        tabHolder.setBackgroundColor(res.getColor(R.color.black_50_trans));
        LinearLayout separator = (LinearLayout) findViewById(R.id.romanblack_fanwall_tab_holder_separator);
        separator.setBackgroundColor(res.getColor(R.color.white_30_trans));
        LinearLayout separatorUp = (LinearLayout) findViewById(R.id.romanblack_fanwall_tab_holder_up);
        LinearLayout separatorDown = (LinearLayout) findViewById(R.id.romanblack_fanwall_tab_holder_down);

        tabMapLayout = (LinearLayout) findViewById(R.id.romanblack_fanwall_tab_map_layout);
        tabMapLayout.setOnClickListener(this);


        tabPhotosLayout = (LinearLayout) findViewById(R.id.romanblack_fanwall_tab_photos_layout);
        tabPhotosLayout.setOnClickListener(this);


        if (Statics.isSchemaDark) {
            separatorUp.setBackgroundColor(res.getColor(R.color.white_20_trans));
            separatorDown.setBackgroundColor(res.getColor(R.color.white_20_trans));
            int temp = Color.WHITE & 0x00ffffff;
            int result = temp | 0x80000000;
            noMsgText.setTextColor(result);
        } else {
            separatorUp.setBackgroundColor(res.getColor(R.color.black_20_trans));
            separatorDown.setBackgroundColor(res.getColor(R.color.black_20_trans));
            int temp = Color.BLACK & 0x00ffffff;
            int result = temp | 0x80000000;
            noMsgText.setTextColor(result);
        }

        noMessagesLayout = (LinearLayout) findViewById(R.id.romanblack_fanwall_main_nomessages_layout);

        messageListLayoutRoot = (FrameLayout) findViewById(R.id.romanblack_fanwall_messagelist_list_layout);
        messageList = (PullToRefreshListView) findViewById(R.id.romanblack_fanwall_messagelist_pulltorefresh);
        messageList.setDivider(null);
        messageList.setBackgroundColor(Color.TRANSPARENT);
        messageList.setDrawingCacheBackgroundColor(Color.TRANSPARENT);

        ILoadingLayout loadingLayout = messageList.getLoadingLayoutProxy();
        if (Statics.isSchemaDark) {
            loadingLayout.setHeaderColor(Color.WHITE);
        } else {
            loadingLayout.setHeaderColor(Color.BLACK);
        }

        //messageList.set
        adapter = new MainLayoutMessagesAdapter(FanWallPlugin.this, messageList, messages, widget);
        adapter.setInnerInterface(new MainLayoutMessagesAdapter.onEndReached() {
            @Override
            public void endReached() {
                if (!refreshingBottom)
                    refreshBottom();
            }
        });

        messageList.setAdapter(adapter);
        messageList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                refreshTop();
            }
        });

        if (Statics.canEdit.compareToIgnoreCase("all") == 0) {
            bottomBarHodler.setVisibility(View.VISIBLE);
        } else {
            bottomBarHodler.setVisibility(View.GONE);
        }

        // start downloading messages
        // exactly in create() method!!!
        handler.sendEmptyMessage(SHOW_PROGRESS_DIALOG);
        refreshMessages();
    }

    @Override
    public void create() {

        // preparing backend data
        initializeBackend();

        // init UI
        initializeUI();
    }

    /**
     * This method using when module data is too big to put in Intent
     *
     * @param fileName - xml module data file name
     * @return xml module data
     */
    protected String readXmlFromFile(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        BufferedReader in = null;

        try {
            in = new BufferedReader(new FileReader(new File(fileName)));
            while ((line = in.readLine()) != null) {
                stringBuilder.append(line);
            }

        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }

        return stringBuilder.toString();
    }

    @Override
    public void destroy() {
        Statics.onAuthListeners.remove(this);

        adapter.clearBitmaps();

        if(progressDialog != null)
            progressDialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if ( requestCode == SHOW_PHOTOLIST_ACTIVITY || requestCode == SHOW_IMAGES_ACTIVITY)
        {
            messages.clear();
            refreshMessages();
        } else if (requestCode == GPS_SETTINGS_ACTIVITY ) {
            if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) )
            {
                Prefs.with(FanWallPlugin.this).save(Prefs.KEY_GPS, false);
                enableGpsCheckbox.setChecked(false);
            }
            else
            {
                enableGpsCheckbox.setChecked(true);
                Prefs.with(FanWallPlugin.this).save(Prefs.KEY_GPS, true);
            }
        } else if (requestCode == FACEBOOK_AUTHORIZATION_ACTIVITY ) {
            if (resultCode == RESULT_OK)
                shareFacebook();
            else if (resultCode == RESULT_CANCELED)
                Toast.makeText(FanWallPlugin.this, getResources().getString(R.string.alert_facebook_auth_error), Toast.LENGTH_SHORT).show();
        } else if (requestCode == TWITTER_AUTHORIZATION_ACTIVITY ) {
            if (resultCode == RESULT_OK)
                shareTwitter();
            else if (resultCode == RESULT_CANCELED)
                Toast.makeText(FanWallPlugin.this, getResources().getString(R.string.alert_twitter_auth_error), Toast.LENGTH_SHORT).show();
        } else
        if (requestCode == TWITTER_PUBLISH_ACTIVITY ) {
            if (resultCode == RESULT_OK) {
                // increment sharing count
                new Thread( new Runnable() {
                    @Override
                    public void run() {
                        if ( postIdToShare != -1 )
                        {
                            IncrementSharingStatus status = Statics.incrementSharing(Long.toString(postIdToShare));
                            Log.e(TAG, "Status = " + status.toString());

                            if ( status.status_code == 0 )
                            {
                                FanWallMessage resMsg = null;
                                for ( FanWallMessage msg : messages )
                                {
                                    if ( msg.getId() == postIdToShare )
                                    {
                                        msg.setSharingCount(msg.getSharingCount() + 1);
                                        resMsg = msg;
                                        break;
                                    }
                                }

                                if ( resMsg != null )
                                    handler.sendEmptyMessage(SHOW_MESSAGES);
                            }
                        }
                    }
                }).start();

                Toast.makeText(FanWallPlugin.this, getResources().getString(R.string.directoryplugin_twitter_posted_success), Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(FanWallPlugin.this, getResources().getString(R.string.directoryplugin_twitter_posted_error), Toast.LENGTH_LONG).show();
            }
        } else
        if (requestCode == FACEBOOK_PUBLISH_ACTIVITY ) {
            if (resultCode == RESULT_OK) {
                // increment sharing count
                new Thread( new Runnable() {
                    @Override
                    public void run() {
                        if ( postIdToShare != -1 )
                        {
                            IncrementSharingStatus status = Statics.incrementSharing(Long.toString(postIdToShare));
                            Log.e(TAG, "Status = " + status.toString());

                            if ( status.status_code == 0 )
                            {
                                FanWallMessage resMsg = null;
                                for ( FanWallMessage msg : messages )
                                {
                                    if ( msg.getId() == postIdToShare )
                                    {
                                        msg.setSharingCount(msg.getSharingCount() + 1);
                                        resMsg = msg;
                                        break;
                                    }
                                }

                                if ( resMsg != null )
                                    handler.sendEmptyMessage(SHOW_MESSAGES);
                            }
                        }
                    }
                }).start();

                Toast.makeText(FanWallPlugin.this, getResources().getString(R.string.directoryplugin_facebook_posted_success), Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(FanWallPlugin.this, getResources().getString(R.string.directoryplugin_facebook_posted_error), Toast.LENGTH_LONG).show();
            }
        } else

        if (requestCode == FACEBOOK_LIKE_AUTH ) {
            if ( resultCode == RESULT_OK )
            {
                if ( !TextUtils.isEmpty(urlToLike) )
                {
                    new Thread( new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if ( FacebookAuthorizationActivity.like(urlToLike) )
                                    refreshTop();
                            } catch (FacebookAuthorizationActivity.FacebookNotAuthorizedException e) {
                                e.printStackTrace();
                            } catch (FacebookAuthorizationActivity.FacebookAlreadyLiked facebookAlreadyLiked) {
                                refreshTop();
                            }
                        }
                    }).start();
                }
            }
        } else if (requestCode == AUTHORIZATION_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                if (action == ACTIONS.SEND_MESSAGE) {
                    startActivityForResult(actionIntent, SEND_MESSAGE_ACTIVITY);
                } else if (action == ACTIONS.SEND_MESSAGE_FROM_WALL) {
                    showProgressDialog();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            FanWallMessage msg = Statics.postMessage(editMsg.getText().toString(), imagePath, 0, 0, Prefs.with(getApplicationContext()).getBoolean(Prefs.KEY_GPS, false));
                            if (msg != null) {
                                imagePath = "";
                                handler.sendEmptyMessage(CLEAR_MSG_TEXT);

                                if (messages.size() == 0)
                                    refreshMessages();
                                else
                                    refreshTop();
                            } else
                                handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
                        }
                    }).start();
                }
            }
        } else if (requestCode == SIGN_UP_ACTIVITY) {
            if (resultCode == RESULT_OK) {

                if (action == ACTIONS.SEND_MESSAGE) {
                    startActivityForResult(actionIntent, SEND_MESSAGE_ACTIVITY);
                }
            }
        } else if (requestCode == MESSAGE_VIEW_ACTIVITY) {
            FanWallMessage msg = (FanWallMessage) data.getSerializableExtra("message");
            if (msg != null) {
                for (FanWallMessage s : messages) {
                    if (s.getId() == msg.getId()) {
                        s.setTotalComments(msg.getTotalComments());
                        break;
                    }
                }
            }

            if ( Prefs.with(FanWallPlugin.this).getBoolean(Prefs.KEY_GPS, false) )
                enableGpsCheckbox.setChecked(true);
            else
                enableGpsCheckbox.setChecked(false);

            refreshMessages();
            handler.sendEmptyMessage(SHOW_MESSAGES);
        } else if (requestCode == IMAGE_VIEW_ACTIVITY) {
        } else if (requestCode == SEND_MESSAGE_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                final FanWallMessage tmpMessage = (FanWallMessage) data.getSerializableExtra("message");

                new Thread(new Runnable() {
                    public void run() {
                        boolean stop = false;

                        ArrayList<FanWallMessage> tmpTmpMessages = new ArrayList<FanWallMessage>();
                        while (!stop) {

                            ArrayList<FanWallMessage> tmpMessages = new ArrayList<FanWallMessage>();

                            if (messages.isEmpty()) {
                                tmpMessages =
                                        JSONParser.parseMessagesUrl(Statics.BASE_URL + "/"
                                                + com.appbuilder.sdk.android.Statics.appId + "/"
                                                + Statics.MODULE_ID + "/" + "0" + "/"
                                                + "0" + "/" + "0" + "/" + "0" + "/"
                                                + com.appbuilder.sdk.android.Statics.appId + "/"
                                                + com.appbuilder.sdk.android.Statics.appToken);
                            } else {
                                tmpMessages =
                                        JSONParser.parseMessagesUrl(Statics.BASE_URL + "/"
                                                + com.appbuilder.sdk.android.Statics.appId + "/"
                                                + Statics.MODULE_ID + "/" + "0" + "/"
                                                + "0" + "/" + messages.get(0).getId() + "/" + "0" + "/"
                                                + com.appbuilder.sdk.android.Statics.appId + "/"
                                                + com.appbuilder.sdk.android.Statics.appToken);
                            }

                            for (int i = 0; i < tmpMessages.size(); i++) {
                                FanWallMessage msg = tmpMessages.get(tmpMessages.size() - i - 1);
                                tmpTmpMessages.add(msg);
                                if (msg.getId() == tmpMessage.getId()) {
                                    stop = true;
                                    break;
                                }
                            }
                        }

                        Collections.reverse(tmpTmpMessages);
                        tmpTmpMessages.addAll(messages);
                        messages.clear();
                        messages.addAll(tmpTmpMessages);

                        if (messages.isEmpty()) {
                            handler.sendEmptyMessage(SHOW_NO_MESSAGES);
                        } else {
                            handler.sendEmptyMessage(SHOW_MESSAGES);
                        }
                    }
                }).start();

            }
        } else if (requestCode == TAKE_A_PICTURE_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                imagePath = data.getStringExtra("imagePath");

                if (TextUtils.isEmpty(imagePath))
                    return;

                chooserHolder.setVisibility(View.GONE);

                setImage();
            }
        } else if (requestCode == PICK_IMAGE_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                cursor.close();

                imagePath = filePath;

                if (TextUtils.isEmpty(imagePath))
                    return;

                if (imagePath.startsWith("http")) {
                    Toast.makeText(this, R.string.romanblack_fanwall_alert_cant_select_image, Toast.LENGTH_LONG).show();
                    return;
                }
                chooserHolder.setVisibility(View.GONE);

                setImage();
            }
        }
    }

    /**
     * Downloads wall messages.
     *
     * @param msgsUrl
     */
    private void loadMessages(String msgsUrl) {

        messages = JSONParser.parseMessagesUrl(msgsUrl);
    }

    /**
     * Refreshes new wall messages.
     */
    private void refreshTop() {
        new Thread(new Runnable() {
            public void run() {
                if (!Utils.networkAvailable(FanWallPlugin.this)) {
                    handler.sendEmptyMessage(NEED_INTERNET_CONNECTION);
                    handler.sendEmptyMessage(LIST_PROGRESS_COMPLITE);
                    return;
                }

                if (messages.isEmpty()) {
                    handler.sendEmptyMessage(LIST_PROGRESS_COMPLITE);
                    return;
                }

                ArrayList<FanWallMessage> newMessages = JSONParser.makeRequest(0, 0, (int) messages.get(0).getId(), 0);
                FanWallMessage startMsg = messages.get(0);
                ArrayList<FanWallMessage> updateMyMessages = JSONParser.makeRequest(0, 0, (int) messages.get(0).getId(), -messages.size());

                messages.clear();
                if (newMessages != null)
                    messages.addAll(newMessages);

                messages.add(startMsg);
                if (updateMyMessages != null)
                    messages.addAll(updateMyMessages);

                Statics.getFbLikesForUrls(messages);
                Statics.getLikedByMe(messages);
                handler.sendEmptyMessage(SHOW_MESSAGES);
            }
        }).start();
    }

    /**
     * Refreshes old wall messages.
     */
    private void refreshBottom() {
        refreshingBottom = true;

        handler.sendEmptyMessage(SHOW_PROGRESS_DIALOG);

        new Thread(new Runnable() {
            public void run() {

                if (!Utils.networkAvailable(FanWallPlugin.this)) {
                    handler.sendEmptyMessage(NEED_INTERNET_CONNECTION);
                    handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
                    refreshingBottom = false;
                    return;
                }

                if (messages.isEmpty()) {
                    handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
                    refreshingBottom = false;
                    return;
                }

                ArrayList<FanWallMessage> historyMessages = JSONParser.makeRequest(0, 0, (messages.get(messages.size() - 1).getId()), -20);
                if (historyMessages == null || historyMessages.size() == 0) {
                    handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
                    refreshingBottom = false;
                    return;
                }

                messages.addAll(historyMessages);
                refreshingBottom = false;
                Statics.getFbLikesForUrls(messages);
                Statics.getLikedByMe(messages);

                handler.sendEmptyMessage(SHOW_MESSAGES);
            }
        }).start();
    }

    /**
     * Refreshes wall messages.
     */
    private void refreshMessages() {
        handler.sendEmptyMessage(SHOW_PROGRESS_DIALOG);

        new Thread(
                new Runnable() {
                    public void run() {
                        if (Utils.networkAvailable(FanWallPlugin.this)) {

                            if (messages.isEmpty()) {
                                ArrayList<FanWallMessage> wallMessages = null;
                                wallMessages = JSONParser.makeRequest(0, 0, 0, 20);
                                if (wallMessages != null)
                                    messages.addAll(wallMessages);
                            } else {
                                ArrayList<FanWallMessage> newMessages = JSONParser.makeRequest(0, 0, (int) messages.get(0).getId(), 0);
                                FanWallMessage startMsg = messages.get(0);
                                ArrayList<FanWallMessage> updateMyMessages = JSONParser.makeRequest(0, 0, (int) messages.get(0).getId(), -messages.size());

                                messages.clear();
                                if (newMessages != null)
                                    messages.addAll(newMessages);

                                messages.add(startMsg);
                                if (updateMyMessages != null)
                                    messages.addAll(updateMyMessages);
                            }
                        } else {
                            ArrayList<FanWallMessage> tempMessages = new MessagesDAO(Statics.cachePath).getMessages(0, 0);
                            if(messages != null && tempMessages != null) {
                                messages.clear();
                                messages.addAll(tempMessages);
                            }
                        }

                        if (messages == null || messages.isEmpty()) {
                            handler.sendEmptyMessage(SHOW_NO_MESSAGES);
                            return;
                        } else {
                            Statics.getFbLikesForUrls(messages);
                            Statics.getLikedByMe(messages);
                            handler.sendEmptyMessage(SHOW_MESSAGES);
                        }
                    }
                }).start();
    }

    /**
     * Shows wall placeholder if the wall has no messages.
     */
    private void showNoMessages() {
        noMessagesLayout.setVisibility(View.VISIBLE);
        messageListLayoutRoot.setVisibility(View.GONE);
        handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
    }

    /**
     * Shows wall messages list if the wall has messages.
     */
    private void showMessages() {
        if (messages.size() == 0) {
            noMessagesLayout.setVisibility(View.VISIBLE);
            messageListLayoutRoot.setVisibility(View.GONE);
        } else {
            Collections.sort(messages, new Comparator<FanWallMessage>() {
                @Override
                public int compare(FanWallMessage detailActivityAdapterData, FanWallMessage detailActivityAdapterData2) {
                    return (int) (detailActivityAdapterData2.getId() - detailActivityAdapterData.getId());
                }
            });

            noMessagesLayout.setVisibility(View.GONE);
            messageListLayoutRoot.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }

        handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
        messageList.onRefreshComplete();

        new MessagesDAO(Statics.cachePath).setMessages(messages, 0, 0);
    }

    /*Listeners methods*/
    public void onCancel(DialogInterface arg0) {
        finish();
    }

    public void onClick(View arg0) {
        int id = arg0.getId();

        if (id == R.id.romanblack_fanwall_main_voice ) {// AddMessage pictogram in home screen

            if (Utils.networkAvailable(FanWallPlugin.this)) {
                if (!Authorization.isAuthorized()) {
                    actionIntent = new Intent(this, SendMessageActivity.class);
                    actionIntent.putExtra("Widget", widget);

                    action = ACTIONS.SEND_MESSAGE;

                    Intent it = new Intent(this, AuthorizationActivity.class);
                    it.putExtra("Widget", widget);
                    startActivityForResult(it, AUTHORIZATION_ACTIVITY);
                } else {
                    Intent it = new Intent(this, SendMessageActivity.class);
                    it.putExtra("Widget", widget);
                    startActivityForResult(it, SEND_MESSAGE_ACTIVITY);
                }
            } else {
                handler.sendEmptyMessage(NEED_INTERNET_CONNECTION);
            }
        } else if (id == R.id.romanblack_fanwall_tab_map_layout) {

            Intent bridge = new Intent(FanWallPlugin.this, FanWallMapActivity.class);
            bridge.putExtra("messages", messages);
            startActivityForResult(bridge, SHOW_MAP_ACTIVITY);

        } else if (id == R.id.romanblack_fanwall_tab_photos_layout) {

            Intent bridge = new Intent(FanWallPlugin.this, FanWallPhotoListActivity.class);
            bridge.putExtra("widget", widget);
            startActivityForResult(bridge, SHOW_PHOTOLIST_ACTIVITY);
        } else if (id == R.id.romanblack_fanwall_send_post) {
            if (TextUtils.isEmpty(editMsg.getText()) && TextUtils.isEmpty(imagePath))
                return;

            if (Utils.networkAvailable(FanWallPlugin.this)) {
                if (!Authorization.isAuthorized()) {
                    action = ACTIONS.SEND_MESSAGE_FROM_WALL;
                    Intent it = new Intent(FanWallPlugin.this, AuthorizationActivity.class);
                    it.putExtra("Widget", widget);
                    startActivityForResult(it, AUTHORIZATION_ACTIVITY);
                } else {
                    showProgressDialog();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            FanWallMessage msg = Statics.postMessage(editMsg.getText().toString(), imagePath, 0, 0,Prefs.with(getApplicationContext()).getBoolean(Prefs.KEY_GPS, false));
                            if (msg != null) {
                                handler.sendEmptyMessage(CLEAR_MSG_TEXT);

                                if (messages.size() == 0)
                                    refreshMessages();
                                else
                                    refreshTop();
                           }
                            handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
                        }
                    }).start();
                }
            } else {
                handler.sendEmptyMessage(NEED_INTERNET_CONNECTION);
            }
        } else if (id == R.id.fanwall_close_image) {
            imageHolder.setVisibility(View.GONE);
            imagePath = "";
        } else if (id == R.id.romanblack_fanwall_open_bottom) {
            if ( chooserHolder.getVisibility() == View.GONE )
                chooserHolder.setVisibility(View.VISIBLE);
            else if (chooserHolder.getVisibility() == View.VISIBLE)
                chooserHolder.setVisibility(View.GONE);
        } else if (id == R.id.romanblack_fanwall_gallery) {
            requestReadPermissionAndProcess();
        } else if (id == R.id.romanblack_fanwall_make_photo) {
            /*if (android.os.Build.VERSION.SDK_INT >= 23) {
                int res = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
                if (res != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,R.string.fanwall_no_camera_permision, Toast.LENGTH_LONG).show();
                    return;
                }
            }*/
            int permissionCheck = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA);

            if (permissionCheck!= PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_REQUEST);
            } else {
                Intent it = new Intent(FanWallPlugin.this, CameraActivity.class);
                it.putExtra("Widget", widget);
                startActivityForResult(it, TAKE_A_PICTURE_ACTIVITY);
            }

        }
    }

    private void requestReadPermissionAndProcess() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);

        if (permissionCheck!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_REQUEST);
        } else {
            Intent i = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            startActivityForResult(i, PICK_IMAGE_ACTIVITY);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent it = new Intent(FanWallPlugin.this, CameraActivity.class);
                    it.putExtra("Widget", widget);
                    startActivityForResult(it, TAKE_A_PICTURE_ACTIVITY);
                } else {
                    finish();
                }
                return;
            }
            case STORAGE_REQUEST:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent i = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    startActivityForResult(i, PICK_IMAGE_ACTIVITY);
                }
                return;
        }
    }

    public void onAuth() {

    }

    /**
     * Sets up image after it was taken from camera or chosen from gallery.
     */
    private void setImage() {
        Bitmap input = Statics.resizeBitmapToMaxSize(imagePath, (int) (IMAGE_ADDED_SIZE * density));
        Bitmap avaBtm = ThumbnailUtils.extractThumbnail(input,
                (int) (IMAGE_ADDED_SIZE * density),
                (int) (IMAGE_ADDED_SIZE * density),
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

        imageHolder.setVisibility(View.VISIBLE);
        userImage.setImageBitmap(avaBtm);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        Log.e(TAG, "");
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        Log.e(TAG, "");
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (editable.length() > 150) {
            editable.replace(editable.length() - 1, editable.length(), "");
            Toast.makeText(this, R.string.romanblack_fanwall_alert_big_text, Toast.LENGTH_SHORT).show();
        }
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

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editMsg.getWindowToken(), 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

     //   return false;
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    public void showSharingDialog() {
        showDialogSharing(new DialogSharing.Configuration.Builder()
                        .setFacebookSharingClickListener(new DialogSharing.Item.OnClickListener() {
                            @Override
                            public void onClick() {
                                // checking Internet connection
                                if (!Utils.networkAvailable(FanWallPlugin.this))
                                    Toast.makeText(FanWallPlugin.this, getResources().getString(R.string.alert_no_internet), Toast.LENGTH_SHORT).show();
                                else {
                                    if (Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_FACEBOOK) != null) {
                                        shareFacebook();
                                    } else {
                                        Authorization.authorize(FanWallPlugin.this, FACEBOOK_AUTHORIZATION_ACTIVITY, Authorization.AUTHORIZATION_TYPE_FACEBOOK);
                                    }
                                }
                            }
                        })
                        .setTwitterSharingClickListener(new DialogSharing.Item.OnClickListener() {
                            @Override
                            public void onClick() {
                                // checking Internet connection
                                if (!Utils.networkAvailable(FanWallPlugin.this))
                                    Toast.makeText(FanWallPlugin.this, getResources().getString(R.string.alert_no_internet), Toast.LENGTH_SHORT).show();
                                else {
                                    if (Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_TWITTER) != null) {
                                        shareTwitter();
                                    } else {
                                        Authorization.authorize(FanWallPlugin.this, TWITTER_AUTHORIZATION_ACTIVITY, Authorization.AUTHORIZATION_TYPE_TWITTER);
                                    }
                                }
                            }
                        })
/*                        .setFlaggingClickListener(new DialogSharing.Item.OnClickListener() {
                            @Override
                            public void onClick() {
                                // checking Internet connection
                                if (!Utils.networkAvailable(FanWallPlugin.this))
                                    Toast.makeText(FanWallPlugin.this, getResources().getString(R.string.alert_no_internet), Toast.LENGTH_SHORT).show();
                                else {
                                    final String imagePath = makeScreenshot(getExternalCacheDir().getAbsolutePath());

                                    Locale current = getResources().getConfiguration().locale;
                                    if ( current.getLanguage().compareToIgnoreCase("en") == 0 )
                                    {
                                        complainOk = "Your complain has been send and will be consider in short";
                                        complainNoInternet = "No internet connection available";
                                        complainFalse = "Unable connect to server";
                                    } else if ( current.getLanguage().compareToIgnoreCase("ru") == 0 )
                                    {
                                        complainOk = "Ваш запрос успешно отправлен и будет в ближайшее время рассмотрен";
                                        complainNoInternet = "Отсутствует соединение с интернетом";
                                        complainFalse = "Неозможно подключиться к серверу";
                                    }

                                    if ( !TextUtils.isEmpty(imagePath) )
                                    {
                                        if ( Utils.networkAvailable(FanWallPlugin.this) )
                                        {
                                            showProgressDialog();
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    final boolean status;
                                                    try {
                                                        status = Utils.sendClaim(imagePath);
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                if ( status )
                                                                    Toast.makeText(FanWallPlugin.this, complainOk, Toast.LENGTH_SHORT).show();
                                                                else
                                                                    Toast.makeText(FanWallPlugin.this, complainFalse, Toast.LENGTH_SHORT).show();
                                                                hideProgressDialog();
                                                            }
                                                        });
                                                    } catch (Exception e) {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Toast.makeText(FanWallPlugin.this, complainFalse, Toast.LENGTH_SHORT).show();
                                                                hideProgressDialog();
                                                            }
                                                        });
                                                    }
                                                }
                                            }).start();
                                        } else
                                            Toast.makeText(FanWallPlugin.this, complainNoInternet, Toast.LENGTH_SHORT).show();
                                    }else
                                        Toast.makeText(FanWallPlugin.this, complainFalse, Toast.LENGTH_SHORT).show();

                                }
                            }
                        })*/
                        .build()
        );
    }

    public String makeScreenshot(String cachePath)
    {
        mainlLayout.setDrawingCacheEnabled(true);
        mainlLayout.buildDrawingCache(true);
        Bitmap b = Bitmap.createBitmap(mainlLayout.getDrawingCache());
        mainlLayout.setDrawingCacheEnabled(false);

        try {
            File fl = new File( cachePath + File.separator + "test.png");
            try {
                if ( !fl.exists() )
                    fl.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            b.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream( fl ));
            return fl.getAbsolutePath();
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    /**
     * Starts SharingActivity to share on Twitter.
     */
    private void shareTwitter() {
        Intent it = new Intent(FanWallPlugin.this, SharingActivity.class);

        // pass the picture path and start the activity
        it.putExtra("type", "twitter");
        it.putExtra("image_url", urlToLike);
        startActivityForResult(it, TWITTER_PUBLISH_ACTIVITY);
    }

    /**
     * Starts SharingActivity to share on Facebook.
     */
    private void shareFacebook() {
        Intent it = new Intent(FanWallPlugin.this, SharingActivity.class);

        // pass the picture path and start the activity
        it.putExtra("type", "facebook");
        it.putExtra("image_url", urlToLike);
        startActivityForResult(it, FACEBOOK_PUBLISH_ACTIVITY);
    }
}
