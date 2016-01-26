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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.appbuilder.sdk.android.AppBuilderModule;
import com.appbuilder.sdk.android.Statics;
import com.appbuilder.sdk.android.Utils;
import com.appbuilder.sdk.android.authorization.Authorization;
import com.appbuilder.sdk.android.authorization.FacebookAuthorizationActivity;
import com.restfb.BinaryAttachment;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.FacebookType;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This activity provides share on Facebook or Twitter functionality.
 */
public class SharingActivity extends AppBuilderModule implements
        OnClickListener {

    private final int NEED_INTERNET_CONNECTION = 0;
    private final int INITIALIZATION_FAILED = 1;
    private final int SHOW_PROGRESS_DIALOG = 2;
    private final int HIDE_PROGRESS_DIALOG = 3;
    private final int HIDE_PROGRESS_DIALOG_SUCCESS = 4;
    private final int HIDE_PROGRESS_DIALOG_FAILURE = 5;
    private final int CLOSE_ACTIVITY = 7;
    private String text = "";
    private Twitter twitter = null;
    private LinearLayout homeImageView = null;
    private LinearLayout postImageView = null;
    private TextView captionTextView = null;
    private EditText mainEditText = null;
    private ProgressDialog progressDialog = null;
    private String sharingType;
    private String image_url;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case INITIALIZATION_FAILED: {
                    finish();
                }
                break;

                case NEED_INTERNET_CONNECTION: {
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

                case CLOSE_ACTIVITY: {
                    finish();
                }
                break;

                case HIDE_PROGRESS_DIALOG_SUCCESS: {
                    setResult(RESULT_OK);
                    hideProgressDialog();
                }
                break;

                case HIDE_PROGRESS_DIALOG_FAILURE: {
                    setResult(RESULT_CANCELED);
                    hideProgressDialog();
                }
                break;
            }
        }
    };

    @Override
    public void create() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fanwall_sharing);
        Intent currentIntent = getIntent();
        image_url = currentIntent.getStringExtra("image_url");
        sharingType = currentIntent.getStringExtra("type");

        // handler of "home" button
        homeImageView = (LinearLayout) findViewById(R.id.sergeyb_directoryplugin_sharing_home);
        homeImageView.setOnClickListener(this);

        // handler of "post" button
        postImageView = (LinearLayout) findViewById(R.id.sergeyb_directoryplugin_sharing_post);
        postImageView.setOnClickListener(this);

        mainEditText = (EditText) findViewById(R.id.sergeyb_directoryplugin_sharing_edittext);
        mainEditText.setText(text);

        // label
        captionTextView = (TextView) findViewById(R.id.sergeyb_directoryplugin_sharing_label);
        if (sharingType.equalsIgnoreCase("facebook")) {
            captionTextView.setText("Facebook");
        } else if (sharingType.equalsIgnoreCase("twitter")) {
            captionTextView.setText("Twitter");
        }
    }

    private void showProgressDialog() {
        if (progressDialog == null || !progressDialog.isShowing()) {
            progressDialog = ProgressDialog.show(this, null, getString(R.string.common_loading_upper));
            progressDialog.setCancelable(true);
        }
    }

    private void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        finish();
    }

    /**
     * Post button and home button handler.
     */
    public void onClick(View arg0) {
        final String edittext = mainEditText.getText().toString();

        if (arg0 == homeImageView) {
            finish();
        } else if (arg0 == postImageView) {
            if (!Utils.networkAvailable(SharingActivity.this)) {
                handler.sendEmptyMessage(NEED_INTERNET_CONNECTION);
                return;
            }

            if (sharingType.equalsIgnoreCase("facebook")) {
                handler.sendEmptyMessage(SHOW_PROGRESS_DIALOG);

                new Thread(new Runnable() {
                    public void run() {
                        try {
                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                            String photo_name = "IMG_IBUILDAPP_" + timeStamp + ".jpg";

                            String message_text = edittext;

                            // *************************************************************************************************
                            // preparing sharing message
                            String downloadThe = getString(R.string.directoryplugin_email_download_this);
                            String androidIphoneApp = getString(R.string.directoryplugin_email_android_iphone_app);
                            String postedVia = getString(R.string.directoryplugin_email_posted_via);
                            String foundThis = getString(R.string.directoryplugin_email_found_this);

                            // prepare content
                            String downloadAppUrl = String.format("http://%s/projects.php?action=info&projectid=%s",
                                    Statics.BASE_DOMEN,
                                    com.ibuildapp.romanblack.FanWallPlugin.data.Statics.APP_ID);

                            String adPart = ""; /*String.format(downloadThe + " %s " + androidIphoneApp + ": %s\n%s",
                                    com.ibuildapp.romanblack.FanWallPlugin.data.Statics.appName,
                                    downloadAppUrl,
                                    postedVia + " http://ibuildapp.com");*/

                            // content part
                            String contentPath = ""; /* String.format(foundThis + " %s: %s \n%s",
                                    com.ibuildapp.romanblack.FanWallPlugin.data.Statics.appName,
                                    image_url,
                                    com.ibuildapp.romanblack.FanWallPlugin.data.Statics.hasAd ? adPart : "");*/

                            message_text += "\n" + contentPath;

                            boolean res = FacebookAuthorizationActivity.sharing(Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_FACEBOOK).getAccessToken(), message_text, image_url);
                            if ( res )
                                handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG_SUCCESS);
                            else
                                handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG_FAILURE);
                        } catch (Exception e) {
                            Log.e("", "");
                            handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG_FAILURE);
                        }
                    }
                }).start();

            } else if (sharingType.equalsIgnoreCase("twitter")) {
                handler.sendEmptyMessage(SHOW_PROGRESS_DIALOG);

                new Thread(new Runnable() {
                    public void run() {
                        try {
                            twitter = reInitTwitter();
                            String message_text = edittext;
                            if (com.ibuildapp.romanblack.FanWallPlugin.data.Statics.hasAd == true) {
                                message_text += getString(R.string.directoryplugin_email_posted_via) + " http://ibuildapp.com.";
                            }

                            if (message_text.length() > 140) {
                                if (TextUtils.isEmpty(image_url))
                                    message_text = message_text.substring(0, 110);
                                else
                                    message_text = message_text.substring(0, 140 - image_url.length());
                            }

                            StatusUpdate su = new StatusUpdate(message_text);
                            if (!TextUtils.isEmpty(image_url)) {
                                InputStream input = new URL(image_url).openStream();
                                su.setMedia(image_url, input);
                            }
                            twitter.updateStatus(su);
                            handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG_SUCCESS);
                        } catch (Exception e) {
                            Log.d("", "");
                            handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG_FAILURE);
                        }
                    }
                }).start();
            }
        }
    }

    /**
     * Reinitializes twitter credentials.
     *
     * @return the twitter instance
     */
    private Twitter reInitTwitter() {
        com.appbuilder.sdk.android.authorization.entities.User twitterUser = Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_TWITTER);
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setDebugEnabled(true)
                .setOAuthAccessToken(twitterUser.getAccessToken())
                .setOAuthAccessTokenSecret(twitterUser.getAccessTokenSecret())
                .setOAuthConsumerSecret(Statics.TWITTER_CONSUMER_SECRET)
                .setOAuthConsumerKey(Statics.TWITTER_CONSUMER_KEY);
        Configuration configuration = builder.build();
        return new TwitterFactory(configuration).getInstance();
    }
}
