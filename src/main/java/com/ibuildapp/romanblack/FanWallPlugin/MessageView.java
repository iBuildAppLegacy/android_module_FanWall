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
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.appbuilder.sdk.android.AppBuilderModuleMain;
import com.appbuilder.sdk.android.Utils;
import com.appbuilder.sdk.android.Widget;
import com.appbuilder.sdk.android.authorization.Authorization;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.ibuildapp.romanblack.FanWallPlugin.adapter.DetailMessageAdapter;
import com.ibuildapp.romanblack.FanWallPlugin.data.DAO.MessagesDAO;
import com.ibuildapp.romanblack.FanWallPlugin.data.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This activity represents wall details page.
 */
public class MessageView extends AppBuilderModuleMain implements OnClickListener, TextWatcher {

    // constants
    public enum ACTIONS {

        ACTION_NO, SEND_MESSAGE, SEND_MESSAGE_FROM_WALL
    };

    private final int IMAGE_ADDED_SIZE = 100;
    private final int LIST_PROGRESS_COMPLITE = 20001;
    private final int SHOW_MESSAGES = 20002;

    public final int MESSAGE_VIEW_ACTIVITY = 10000;
    private final int IMAGE_VIEW_ACTIVITY = 10001;
    public final int SEND_MESSAGE_ACTIVITY = 10002;
    public final int AUTHORIZATION_ACTIVITY = 10003;
    private final int TAKE_A_PICTURE_ACTIVITY = 10007;
    private final int PICK_IMAGE_ACTIVITY = 10008;
    private final int CLEAR_MSG_TEXT = 10005;
    private final int GPS_SETTINGS_ACTIVITY = 10010;


    private final int INITIALIZATION_FAILED = 0;
    private final int CHECK_MESSAGES = 1;
    private final int REFRESH_TOP = 2;
    private final int REFRESH_BOTTOM = 3;

    private final int SHOW_PROGRESS_DIALOG = 22;
    private final int HIDE_PROGRESS_DIALOG = 33;
    private final int NEED_INTERNET_CONNECTION = 6;
    private final String DATE_PATTERN = "HH:mm MMM d, yyyy";
    public ACTIONS action = ACTIONS.ACTION_NO;

    // backend
    private int postTotalCommentsCount;
    public Intent actionIntent = null;
    private int position = 0;
    private Widget widget = null;
    private FanWallMessage message = null;
    private ArrayList<FanWallMessage> messages = new ArrayList<FanWallMessage>();

    private Resources res;
    private DetailMessageAdapter adapter;
    private CopyOnWriteArrayList<DetailActivityAdapterData> content = new CopyOnWriteArrayList<DetailActivityAdapterData>();

    private boolean refreshingBottom = false;
    private boolean refreshingTop = false;

    private Animation showProgress;
    private Animation hideProgress;
    private boolean needMenu;
    private ProgressDialog progressDialog;
    private String imagePath;
    private float density;
    private  LocationManager locationManager;

    // UI
    private PullToRefreshListView messageList;
    private EditText editMsg;
    private LinearLayout imageHolder;
    private ImageView userImage;
    private ImageView closeBtn;
    private LinearLayout openBottom;
    private LinearLayout chooserHolder;
    private LinearLayout galleryChooser;
    private LinearLayout photoChooser;
    private LinearLayout postMsg;
    private LinearLayout bottomBarHolder;
    private CheckBox enableGpsCheckbox;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case INITIALIZATION_FAILED: {
                    Toast.makeText(MessageView.this, R.string.alert_cannot_init,
                            Toast.LENGTH_LONG).show();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            setResult(RESULT_CANCELED);
                            finish();
                        }
                    }, 3000);
                }
                break;


                case REFRESH_TOP: {
                    refreshTop();
                }
                break;

                case REFRESH_BOTTOM: {
                    refreshBottom();
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

                case NEED_INTERNET_CONNECTION: {
                    Toast.makeText(MessageView.this, R.string.alert_no_internet, Toast.LENGTH_LONG).show();
                }
                break;

                case LIST_PROGRESS_COMPLITE: {
                    messageList.onRefreshComplete();
                }
                break;

                case SHOW_MESSAGES: {
                    showMessages();
                }
                break;

                case CLEAR_MSG_TEXT: {
                    editMsg.setText("");
                    imagePath = "";
                    imageHolder.setVisibility(View.GONE);
                    hideKeyboard();
                }
                break;
            }
        }
    };

    @Override
    public void destroy() {
        adapter.clearBitmaps();
    }

    private void showMessages() {
        // if data collection has data -> sort
        boolean emptyData = false;
        for (DetailActivityAdapterData s : content) {
            if (s.noComments)
                emptyData = true;
        }

        if (!emptyData && content.size() > 1) {
            List<DetailActivityAdapterData> tempList = new ArrayList<DetailActivityAdapterData>();
            tempList.addAll(content);

            for (DetailActivityAdapterData s : content) {
                if (s.message == null)
                    Log.e(TAG, "KOSYAAAAAAAAAAAAK = null!!!");
                else if (s.message.getDate() == null)
                    Log.e(TAG, "KOSYAAAAAAAAAAAAK = data!!!");

            }

            Collections.sort(tempList, Collections.reverseOrder(new Comparator<DetailActivityAdapterData>() {
                @Override
                public int compare(DetailActivityAdapterData detailActivityAdapterData, DetailActivityAdapterData detailActivityAdapterData2) {
                    long value1 = detailActivityAdapterData.message.getDate().getTime();
                    long value2 = detailActivityAdapterData2.message.getDate().getTime();

                    return value1 == value2 ? 0 : (value1 > value2 ? 1 : -1);
                }
            }));
            content.clear();
            content.addAll(tempList);
        }


        message.setTotalComments(postTotalCommentsCount);
        content.add(0, new DetailActivityAdapterData(null, message, false));

        adapter.notifyDataSetChanged();
        handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
        messageList.onRefreshComplete();

        // save to cache
        cacheMessages();
    }

    /**
     * First loading
     */
    private void loadStart() {
        new Thread(new Runnable() {
            public void run() {
                if (Utils.networkAvailable(MessageView.this)) {

                    ArrayList<FanWallMessage> newMessages = new ArrayList<FanWallMessage>();
                    if (message.getParentId() == 0) // 1st level - COMMENT
                        newMessages = JSONParser.makeRequest(message.getId(), 0, 0, 20);
                    else // 2nd level - REPLY
                        newMessages = JSONParser.makeRequest(message.getParentId(), message.getId(), 0, 20);

                    content.clear();
                    if (newMessages == null || newMessages.isEmpty()) {
                        content.add(new DetailActivityAdapterData(null, null, true));
                    } else {
                        for (FanWallMessage s : newMessages)
                            content.add(new DetailActivityAdapterData(s, null, false));
                    }

                    postTotalCommentsCount = JSONParser.getPostCommentsCount(message.getId());
                } else {
                    ArrayList<FanWallMessage> newMessages = new ArrayList<FanWallMessage>();
                    if (message.getParentId() == 0) // 1st level - COMMENT
                        newMessages = new MessagesDAO(Statics.cachePath).getMessages((int) message.getId(), 0);
                    else // 2nd level - REPLY
                        newMessages = new MessagesDAO(Statics.cachePath).getMessages((int) message.getParentId(), (int) message.getId());

                    if (newMessages == null || newMessages.isEmpty()) {
                        content.add(new DetailActivityAdapterData(null, null, true));
                        postTotalCommentsCount = 0;
                    } else {
                        for (FanWallMessage s : newMessages)
                            content.add(new DetailActivityAdapterData(s, null, false));
                        postTotalCommentsCount = content.size();
                    }
                }

                handler.sendEmptyMessage(SHOW_MESSAGES);
            }
        }).start();
    }

    private void initializeUI() {
        setContentView(R.layout.romanblack_fanwall_message_main_ms);

        // topbar initialization
        setTopBarTitle(getResources().getString(R.string.romanblack_fanwall_comments_title));
        setTopBarLeftButtonText(getResources().getString(R.string.wall), true, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeActivity();
            }
        });
        swipeBlock();

        FrameLayout root = (FrameLayout) findViewById(R.id.fanwall_messagdetails_root);
        root.setBackgroundColor(Statics.color1);

        bottomBarHolder = (LinearLayout) findViewById(R.id.romanblack_fanwall_main_bottom_bar);
        if (Statics.canEdit.compareToIgnoreCase("all") == 0) {
            bottomBarHolder.setVisibility(View.VISIBLE);
        } else {
            bottomBarHolder.setVisibility(View.GONE);
        }


        imageHolder = (LinearLayout) findViewById(R.id.fanwall_image_holder);
        userImage = (ImageView) findViewById(R.id.fanwall_user_image);
        closeBtn = (ImageView) findViewById(R.id.fanwall_close_image);
        closeBtn.setOnClickListener(this);

        openBottom = (LinearLayout) findViewById(R.id.romanblack_fanwall_open_bottom);
        openBottom.setOnClickListener( this );
        chooserHolder = (LinearLayout) findViewById(R.id.fanwall_chooser_holder);

        galleryChooser = (LinearLayout) findViewById(R.id.romanblack_fanwall_gallery);
        galleryChooser.setOnClickListener(this);
        photoChooser = (LinearLayout) findViewById(R.id.romanblack_fanwall_make_photo);
        photoChooser.setOnClickListener(this);


        postMsg = (LinearLayout) findViewById(R.id.romanblack_fanwall_send_post);
        postMsg.setOnClickListener(this);
        editMsg = (EditText) findViewById(R.id.romanblack_fanwall_edit_msg);
        editMsg.addTextChangedListener(this);

        enableGpsCheckbox = (CheckBox) findViewById(R.id.romanblack_fanwall_enable_gps_checkbox);
        enableGpsCheckbox.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if ( b )
                {
                    if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(MessageView.this);
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
                    } else
                    {
                        Prefs.with(MessageView.this).save(Prefs.KEY_GPS, true );
                    }
                }
                else
                    Prefs.with(MessageView.this).save(Prefs.KEY_GPS, false );

            }
        });
        enableGpsCheckbox.setChecked(Prefs.with(MessageView.this).getBoolean(Prefs.KEY_GPS, false));

        messageList = (PullToRefreshListView) findViewById(R.id.fanwall_messagdetails_pulltorefresh);

        // prepare listview
        adapter = new DetailMessageAdapter(MessageView.this, messageList, content, message, widget);


        ILoadingLayout loadingLayout = messageList.getLoadingLayoutProxy();
        if (Statics.isSchemaDark) {
            loadingLayout.setHeaderColor(Color.WHITE);
        } else {
            loadingLayout.setHeaderColor(Color.BLACK);
        }
        messageList.setDivider(null);
        messageList.setBackgroundColor(Color.TRANSPARENT);
        messageList.setAdapter(adapter);
        messageList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                if (!refreshingBottom && !refreshingTop)
                    refreshTop();
            }
        });
        messageList.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {
            @Override
            public void onLastItemVisible() {
                if (!refreshingBottom && !refreshingTop)
                    refreshBottom();
            }
        });

        // loadContent
        showProgressDialog();
        loadStart();
    }

    private void initializeBackend() {
        res = getResources();
        density = res.getDisplayMetrics().density;

        showProgress = AnimationUtils.loadAnimation(MessageView.this, R.anim.show_progress_anim);
        hideProgress = AnimationUtils.loadAnimation(MessageView.this, R.anim.hide_progress_anim);

        Intent currentIntent = getIntent();
        message = (FanWallMessage) currentIntent.getSerializableExtra("message");
        if (message == null) {
            handler.sendEmptyMessage(INITIALIZATION_FAILED);
            finish();
            return;
        }

        widget = (Widget) currentIntent.getSerializableExtra("Widget");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Statics.currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000,0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Statics.currentLocation = location;
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {}

            @Override
            public void onProviderEnabled(String s) {
                Prefs.with(MessageView.this).save(Prefs.KEY_GPS, true);
                enableGpsCheckbox.setChecked(true);
            }

            @Override
            public void onProviderDisabled(String s) {
                Prefs.with(MessageView.this).save(Prefs.KEY_GPS, false);
                enableGpsCheckbox.setChecked(false);
            }
        });
    }

    @Override
    public void create() {
        initializeBackend();
        initializeUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GPS_SETTINGS_ACTIVITY ) {
            if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) )
            {
                Prefs.with(MessageView.this).save(Prefs.KEY_GPS, false);
                enableGpsCheckbox.setChecked(false);
            }
            else
            {
                enableGpsCheckbox.setChecked(true);
                Prefs.with(MessageView.this).save(Prefs.KEY_GPS, true);
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
                            long parentId = 0;
                            long replyId = 0;
                            if (message != null) {
                                if (message.getParentId() == 0) {
                                    parentId = message.getId();
                                    replyId = 0;
                                } else if ((message.getParentId() != 0)
                                        && (message.getReplyId() == 0)) {
                                    parentId = message.getParentId();
                                    replyId = message.getId();
                                } else {
                                    Toast.makeText(MessageView.this, "You can't comment this message!", Toast.LENGTH_LONG).show();
                                    return;
                                }
                            }

                            FanWallMessage msg = Statics.postMessage(editMsg.getText().toString(), imagePath, (int) parentId, (int) replyId, Prefs.with(getApplicationContext()).getBoolean(Prefs.KEY_GPS, false));
                            if (msg != null) {
                                imagePath = "";
                                refreshTop();
                                handler.sendEmptyMessage(CLEAR_MSG_TEXT);
                            } else
                                handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
                        }
                    }).start();
                }
            }
        } else if (requestCode == SEND_MESSAGE_ACTIVITY) {

            refreshTop();
        } else if (requestCode == MESSAGE_VIEW_ACTIVITY) {
            refreshTop();
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
     * Refreshes new wall messages.
     */
    private void refreshTop() {
        refreshingTop = true;
        new Thread(new Runnable() {
            public void run() {

                Log.e(TAG, "refreshTop");

                if (!Utils.networkAvailable(MessageView.this)) {
                    handler.sendEmptyMessage(NEED_INTERNET_CONNECTION);
                    handler.sendEmptyMessage(LIST_PROGRESS_COMPLITE);
                    handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
                    refreshingTop = false;
                    return;
                }

                // compute amount of old messages
                int msgCount = 0;
                for (DetailActivityAdapterData s : content) {
                    if (s.message != null) {
                        msgCount++;
                    }
                }

                if ((message.getReplyId() == 0) && (message.getParentId() == 0)) {

                    // comments empty
                    if (msgCount == 0) {
                        ArrayList<FanWallMessage> newMessages = JSONParser.makeRequest((int) message.getId(), 0, 0, 20);
                        content.clear();

                        if (newMessages == null || newMessages.isEmpty()) {
                            content.add(new DetailActivityAdapterData(null, null, true));
                        } else {
                            for (FanWallMessage s : newMessages)
                                content.add(new DetailActivityAdapterData(s, null, false));
                        }
                    } else {
                        ArrayList<FanWallMessage> newMessages = JSONParser.makeRequest((int) message.getId(), 0, content.get(1).message.getId(), 20);
                        FanWallMessage startMsg = content.get(1).message;
                        ArrayList<FanWallMessage> updateMyMessages = JSONParser.makeRequest((int) message.getId(), 0, content.get(1).message.getId(), -msgCount);

                        content.clear();
                        if (newMessages != null) {
                            for (FanWallMessage s : newMessages)
                                content.add(new DetailActivityAdapterData(s, null, false));
                        }
                        content.add(new DetailActivityAdapterData(startMsg, null, false));

                        if (updateMyMessages != null) {
                            for (FanWallMessage s : updateMyMessages)
                                content.add(new DetailActivityAdapterData(s, null, false));
                        }
                    }
                } else if ((message.getReplyId() == 0) && (message.getParentId() != 0)) {

                    // comments empty
                    if (msgCount == 0) {
                        ArrayList<FanWallMessage> newMessages = JSONParser.makeRequest((int) message.getParentId(), message.getId(), 0, 20);
                        content.clear();

                        if (newMessages == null || newMessages.isEmpty()) {
                            content.add(new DetailActivityAdapterData(null, null, true));
                        } else {
                            for (FanWallMessage s : newMessages)
                                content.add(new DetailActivityAdapterData(s, null, false));
                        }
                    } else {
                        ArrayList<FanWallMessage> newMessages = JSONParser.makeRequest((int) message.getParentId(), message.getId(), content.get(1).message.getId(), 20);
                        FanWallMessage startMsg = content.get(1).message;
                        ArrayList<FanWallMessage> updateMyMessages = JSONParser.makeRequest((int) message.getId(), 0, content.get(1).message.getId(), -msgCount);

                        content.clear();
                        if (newMessages != null) {
                            for (FanWallMessage s : newMessages)
                                content.add(new DetailActivityAdapterData(s, null, false));
                        }
                        content.add(new DetailActivityAdapterData(startMsg, null, false));

                        if (updateMyMessages != null) {
                            for (FanWallMessage s : updateMyMessages)
                                content.add(new DetailActivityAdapterData(s, null, false));
                        }
                    }
                }

                postTotalCommentsCount = JSONParser.getPostCommentsCount(message.getId());

                refreshingTop = false;
                Log.e(TAG, "");
                handler.sendEmptyMessage(SHOW_MESSAGES);
                Log.e(TAG, "");
            }
        }).start();
    }

    /**
     * Refreshes old comments.
     */
    private void refreshBottom() {
        refreshingBottom = true;

        handler.sendEmptyMessage(SHOW_PROGRESS_DIALOG);

        new Thread(new Runnable() {
            public void run() {
                Log.e(TAG, "refreshBottom");

                refreshingBottom = true;
                ArrayList<FanWallMessage> historyMessages = null;
                if ((message.getReplyId() == 0)
                        && (message.getParentId() == 0)) {

                    if (content.get(content.size() == 1 ? 0 : 1).noComments) // no comments
                    {
                        historyMessages = JSONParser.makeRequest((int) message.getId(), 0, 0, -20);
                    } else // get history
                    {
                        historyMessages = JSONParser.makeRequest((int) message.getId(), 0, content.get(content.size() - 1).message.getId(), -20);
                    }
                } else if ((message.getReplyId() == 0)
                        && (message.getParentId() != 0)) {

                    if (content.get(1).noComments) // no comments
                    {
                        historyMessages = JSONParser.makeRequest((int) message.getParentId(), message.getId(), 0, -20);
                    } else // get history
                    {
                        historyMessages = JSONParser.makeRequest((int) message.getParentId(), message.getId(), content.get(content.size() - 1).message.getId(), -20);
                    }
                }

                if (historyMessages == null || historyMessages.isEmpty()) {
                    refreshingBottom = false;
                    handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
                    return;
                }

                try {
                    DetailActivityAdapterData temp = content.get(0);
                    if (temp.header != null)
                        content.remove(0);
                } catch (Exception e) {
                }

                for (FanWallMessage s : historyMessages) {
                    content.add(new DetailActivityAdapterData(s, null, false));
                }

                postTotalCommentsCount = JSONParser.getPostCommentsCount(message.getId());

                refreshingBottom = false;
                handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
                handler.sendEmptyMessage(SHOW_MESSAGES);
            }
        }).start();
    }

    /**
     * Caches downloaded comments to device external storage.
     */
    private void cacheMessages() {
        List<FanWallMessage> cacheList = new ArrayList<FanWallMessage>();
        for (DetailActivityAdapterData s : content) {
            if (s.message != null)
                cacheList.add(s.message);
        }
        new MessagesDAO(Statics.cachePath).setMessages(cacheList, (int) message.getId(), 0);
        if ((message.getReplyId() == 0)
                && (message.getParentId() == 0)) {
            new MessagesDAO(Statics.cachePath).setMessages(cacheList, (int) message.getId(), 0);
        } else if ((message.getReplyId() == 0)
                && (message.getParentId() != 0)) {
            new MessagesDAO(Statics.cachePath).setMessages(cacheList, (int) message.getParentId(), (int) message.getId());
        }
    }

    public void onClick(View arg0) {
        int id = arg0.getId();
        if (id == R.id.romanblack_fanwall_message_voice
                || id == R.id.romanblack_fanwall_message_nocomments_button) {
            if (Utils.networkAvailable(MessageView.this)) {
                if (!Authorization.isAuthorized()) {
                    actionIntent = new Intent(this, SendMessageActivity.class);
                    actionIntent.putExtra("messages", messages);
                    actionIntent.putExtra("message", message);
                    actionIntent.putExtra("Widget", widget);

                    action = ACTIONS.SEND_MESSAGE;

                    Intent it = new Intent(this, AuthorizationActivity.class);
                    it.putExtra("Widget", widget);
                    startActivityForResult(it, AUTHORIZATION_ACTIVITY);
                } else {
                    Intent it = new Intent(this, SendMessageActivity.class);
                    it.putExtra("messages", messages);
                    it.putExtra("message", message);
                    it.putExtra("Widget", widget);
                    startActivityForResult(it, SEND_MESSAGE_ACTIVITY);
                }
            } else {
                handler.sendEmptyMessage(NEED_INTERNET_CONNECTION);
            }
        } else if (id == R.id.romanblack_fanwall_message_msgimage) {
            Intent it = new Intent(this, ImageViewActivity.class);
            it.putExtra("messages", messages);
            it.putExtra("position", position);
            it.putExtra("Widget", widget);
            startActivityForResult(it, IMAGE_VIEW_ACTIVITY);
        } else if (id == R.id.romanblack_fanwall_send_post) {
            if (TextUtils.isEmpty(editMsg.getText()) && TextUtils.isEmpty(imagePath))
                return;

            if (Utils.networkAvailable(MessageView.this)) {
                if (!Authorization.isAuthorized()) {
                    action = ACTIONS.SEND_MESSAGE_FROM_WALL;
                    Intent it = new Intent(MessageView.this, AuthorizationActivity.class);
                    it.putExtra("Widget", widget);
                    startActivityForResult(it, AUTHORIZATION_ACTIVITY);
                } else {
                    showProgressDialog();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            long parentId = 0;
                            long replyId = 0;
                            if (message != null) {
                                if (message.getParentId() == 0) {
                                    parentId = message.getId();
                                    replyId = 0;
                                } else if ((message.getParentId() != 0)
                                        && (message.getReplyId() == 0)) {
                                    parentId = message.getParentId();
                                    replyId = message.getId();
                                } else {
                                    Toast.makeText(MessageView.this, "You can't comment this message!", Toast.LENGTH_LONG).show();
                                    return;
                                }
                            }

                            FanWallMessage msg = Statics.postMessage(editMsg.getText().toString(), imagePath, (int) parentId, (int) replyId,Prefs.with(getApplicationContext()).getBoolean(Prefs.KEY_GPS, false));
                            if (msg != null) {
                                handler.sendEmptyMessage(CLEAR_MSG_TEXT);
                                refreshTop();
                            }
                        }
                    }).start();
                }
            } else {
                handler.sendEmptyMessage(NEED_INTERNET_CONNECTION);
            }
        } else if (id == R.id.fanwall_close_image) {
            imageHolder.setVisibility(View.GONE);
            imagePath = "";
        } else if (id == R.id.romanblack_fanwall_gallery) {
            Intent i = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, PICK_IMAGE_ACTIVITY);
        } else if (id == R.id.romanblack_fanwall_make_photo) {
            Intent it = new Intent(MessageView.this, CameraActivity.class);
            it.putExtra("Widget", widget);
            startActivityForResult(it, TAKE_A_PICTURE_ACTIVITY);
        }  else if (id == R.id.romanblack_fanwall_open_bottom) {
            if ( chooserHolder.getVisibility() == View.GONE )
                chooserHolder.setVisibility(View.VISIBLE);
            else if (chooserHolder.getVisibility() == View.VISIBLE)
                chooserHolder.setVisibility(View.GONE);
        }
    }

    private void closeActivity() {
        Intent res = new Intent();
        res.putExtra("message", message);
        setResult(RESULT_OK, res);
        finish();
    }

    @Override
    public void onBackPressed() {
        closeActivity();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

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

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editMsg.getWindowToken(), 0);
    }

}
