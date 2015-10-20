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
import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.appbuilder.sdk.android.AppBuilderModuleMain;
import com.appbuilder.sdk.android.Utils;
import com.appbuilder.sdk.android.authorization.entities.User;
import com.ibuildapp.romanblack.FanWallPlugin.data.FanWallMessage;
import com.ibuildapp.romanblack.FanWallPlugin.data.JSONParser;
import com.ibuildapp.romanblack.FanWallPlugin.data.Statics;
import com.seppius.i18n.plurals.PluralResources;

import java.net.IDN;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * This activity represents profile details page.
 */
public class ProfileViewActivity extends AppBuilderModuleMain {

    private final int INITIALIZATION_FAILED = 0;
    private final int NEED_INTERNET_CONNECTION = 1;
    private final int SHOW_PROGRESS_DIALOG = 2;
    private final int HIDE_PROGRESS_DIALOG = 3;
    private final int SET_AVATAR = 4;
    private final int SET_RECIEVED_DATA = 5;
    private final String DATE_PATTERN = "HH:mm MMM d, yyyy";
    private final String COMMAND = "getprofileinfo";
    private String totalPosts = "";
    private String totalComments = "";
    private String lastMessage = "";
    private FanWallMessage message = null;
    private ArrayList<FanWallMessage> messages = null;
    private Bitmap avatarBitmap = null;
    private ImageView avatarImageView = null;
    private TextView nameTextView = null;
    private TextView dateTextView = null;
    private ImageView accountTypeIcon = null;
    private TextView accountTypeTextView = null;
    private TextView postsCountTextView = null;
    private TextView commentsCountTextView = null;
    private TextView lastMsgText;
    private ProgressDialog progressDialog = null;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case INITIALIZATION_FAILED: {
                    Toast.makeText(ProfileViewActivity.this, R.string.romanblack_fanwall_alert_cant_view_profile, Toast.LENGTH_LONG).show();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                        }
                    }, 3000);
                }
                break;
                case NEED_INTERNET_CONNECTION: {
                    Toast.makeText(ProfileViewActivity.this, R.string.alert_no_internet, Toast.LENGTH_LONG).show();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                        }
                    }, 3000);
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
                case SET_AVATAR: {
                    setAvatar();
                }
                break;
                case SET_RECIEVED_DATA: {
                    setRecievedData();
                }
                break;
            }
        }
    };

    @Override
    public void create() {
        setContentView(R.layout.romanblack_fanwall_profile);

        // topbar initialization
        setTopBarTitle(getResources().getString(R.string.profile));
        setTopBarLeftButtonText(getResources().getString(R.string.common_back_upper), true, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        swipeBlock();

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.romanblack_fanwall_profile_back_layout);
        linearLayout.setBackgroundColor(Statics.color1);

        avatarImageView = (ImageView) findViewById(R.id.romanblack_fanwall_profile_avatar);

        nameTextView = (TextView) findViewById(R.id.romanblack_fanwall_profile_name);
        nameTextView.setTextColor(Statics.color3);

        lastMsgText = (TextView) findViewById(R.id.romanblack_fanwall_profile_last_msg);
        lastMsgText.setTextColor(Statics.color3);

        dateTextView = (TextView) findViewById(R.id.romanblack_fanwall_profile_date);
        dateTextView.setTextColor(Statics.color3);

        accountTypeIcon = (ImageView) findViewById(R.id.romanblack_fanwall_profile_account_icon);

        accountTypeTextView = (TextView) findViewById(R.id.romanblack_fanwall_profile_account_type);
        accountTypeTextView.setTextColor(Statics.color5);

        postsCountTextView = (TextView) findViewById(R.id.romanblack_fanwall_profile_posts_count);
        postsCountTextView.setTextColor(Statics.color4);

        commentsCountTextView = (TextView) findViewById(R.id.romanblack_fanwall_profile_comments_count);
        commentsCountTextView.setTextColor(Statics.color4);

        Intent currentIntent = getIntent();
        messages = (ArrayList<FanWallMessage>) currentIntent.getSerializableExtra("messages");

        message = messages.get(currentIntent.getIntExtra("position", 0));

        if (message == null) {
            handler.sendEmptyMessage(INITIALIZATION_FAILED);
            finish();
        }

        if (!Utils.networkAvailable(ProfileViewActivity.this)) {
            handler.sendEmptyMessage(NEED_INTERNET_CONNECTION);
            return;
        }

        nameTextView.setText(message.getAuthor());
        if (message.getAccountType() == User.ACCOUNT_TYPES.FACEBOOK) {
            accountTypeIcon.setImageResource(R.drawable.romanblack_fanwall_facebook_icon);
            accountTypeTextView.setText("Facebook");
            OnClickListener onClickListener = new OnClickListener() {
                public void onClick(View arg0) {
                    String _url = "http://www.facebook.com/profile.php?id=" + message.getAccountId();
                    Intent webIntent = new Intent(Intent.ACTION_VIEW);
                    webIntent.setData(Uri.parse(_url));
                    startActivity(webIntent);
                }
            };
            accountTypeIcon.setOnClickListener(onClickListener);
            accountTypeTextView.setOnClickListener(onClickListener);
        } else if (message.getAccountType() == User.ACCOUNT_TYPES.TWITTER) {
            accountTypeIcon.setImageResource(R.drawable.romanblack_fanwall_twitter_icon);
            accountTypeTextView.setText("Twitter");
            OnClickListener onClickListener = new OnClickListener() {
                public void onClick(View arg0) {
                    String _url = "https://twitter.com/intent/user?user_id=" + message.getAccountId();
                    Intent webIntent = new Intent(Intent.ACTION_VIEW);
                    webIntent.setData(Uri.parse(_url));
                    startActivity(webIntent);
                }
            };
            accountTypeIcon.setOnClickListener(onClickListener);
            accountTypeTextView.setOnClickListener(onClickListener);
        } else {
            accountTypeIcon.setImageResource(R.drawable.romanblack_fanwall_iba_icon);
            if (com.appbuilder.sdk.android.Statics.BASE_DOMEN.equalsIgnoreCase("ibuildapp.com")) {
                accountTypeTextView.setText("iBuildApp");

                if (!com.appbuilder.sdk.android.Statics.showLink) {
                    accountTypeTextView.setVisibility(View.GONE);
                    accountTypeIcon.setVisibility(View.GONE);
                }
            } else {
                accountTypeTextView.setText(detectIDN(com.appbuilder.sdk.android.Statics.BASE_DOMEN));
                accountTypeIcon.setVisibility(View.GONE);
            }
            OnClickListener onClickListener = new OnClickListener() {
                public void onClick(View arg0) {
                    String _url = "http://" + com.appbuilder.sdk.android.Statics.BASE_DOMEN + "/members/" + message.getAccountId() + "/";
                    Intent webIntent = new Intent(Intent.ACTION_VIEW);
                    webIntent.setData(Uri.parse(_url));
                    startActivity(webIntent);
                }
            };
            accountTypeIcon.setOnClickListener(onClickListener);
            accountTypeTextView.setOnClickListener(onClickListener);
        }



        new Thread(new Runnable() {
            public void run() {
                Bitmap bitmap = null;

                if (!TextUtils.isEmpty(message.getUserAvatarCache())) {
                    try {
                        bitmap = BitmapFactory.decodeFile(message.getUserAvatarCache());
                        avatarBitmap = Statics.publishAvatar(bitmap, 15);
                    } catch (Exception e) {
                        Log.w("", "");
                    }
                } else {
                    String avaCache = Statics.downloadFile(message.getUserAvatarUrl());
                    if (!TextUtils.isEmpty(avaCache)) {
                        message.setUserAvatarCache(avaCache);
                        bitmap = BitmapFactory.decodeFile(message.getUserAvatarCache());
                        avatarBitmap = Statics.publishAvatar(bitmap, 15);
                    }
                }

                handler.sendEmptyMessage(SET_AVATAR);
            }
        }).start();

        handler.sendEmptyMessage(SHOW_PROGRESS_DIALOG);

        new Thread(new Runnable() {
            public void run() {
                String appendix = "email";

                if (message.getAccountType() == User.ACCOUNT_TYPES.FACEBOOK) {
                    appendix = "facebook";
                } else if (message.getAccountType() == User.ACCOUNT_TYPES.TWITTER) {
                    appendix = "twitter";
                } else {
                    appendix = "ibuildapp";
                }

                String[] res = JSONParser.parseProfileData(Statics.BASE_URL + "/"
                        + Statics.APP_ID + "/" + Statics.MODULE_ID + "/" + COMMAND
                        + "/" + appendix + "/" + message.getAccountId() + "/"
                        + com.appbuilder.sdk.android.Statics.appId + "/"
                        + com.appbuilder.sdk.android.Statics.appToken);

                try {
                    totalPosts = res[0];
                    totalComments = res[1];
                    lastMessage = res[2];

                    handler.sendEmptyMessage(SET_RECIEVED_DATA);
                } catch (NullPointerException nPEx) {
                }

                handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
            }
        }).start();
    }

    /**
     * detects idn in domain
     * @param domain domain name to detect
     * @return result in utf-8, if domain contains "xn--" sign - this is idn url and it will be converted, just return input string otherwise
     */
    private String detectIDN(String domain) {
        return domain.contains("xn--") ? IDN.toUnicode(domain) : domain;
    }

    /**
     * Sets up user avatar when it was downloaded.
     */
    private void setAvatar() {
        if (avatarBitmap != null) {
            avatarImageView.setImageBitmap(avatarBitmap);
        }
    }

    /**
     * Sets up resieved user data after it was recieved.
     */
    private void setRecievedData() {
        try {
            int posts = new Integer(totalPosts).intValue();
            String result = new PluralResources(getResources()).getQuantityString(R.plurals.numberOfMessages, posts, posts);

            postsCountTextView.setText(result);
        } catch (NumberFormatException nFEx) {
        } catch (NullPointerException nPEx) {
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        try {
            int comments = new Integer(totalComments).intValue();
            String result = new PluralResources(getResources()).getQuantityString(R.plurals.numberOfComments, comments, comments);
            commentsCountTextView.setText(result);
        } catch (NumberFormatException nFEx) {
        } catch (NullPointerException nPEx) {
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        try {
            Date dt = new Date(new Long(lastMessage).longValue());
            SimpleDateFormat sdf;
            if (Locale.getDefault().toString().equals("en_US")) {
                sdf = new SimpleDateFormat(DATE_PATTERN);
            } else {
                sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");
            }

            String last = sdf.format(dt);
            dateTextView.setText(last);
        } catch (Exception ex) {
        }
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(this, null, getString(R.string.common_loading_upper));
        }

        if (!progressDialog.isShowing()) {
            progressDialog = ProgressDialog.show(this, null, getString(R.string.common_loading_upper));
        }
    }

    private void hideProgressDialog() {
        try {
            progressDialog.dismiss();
        } catch (NullPointerException nPEx) {
        }
    }
}
