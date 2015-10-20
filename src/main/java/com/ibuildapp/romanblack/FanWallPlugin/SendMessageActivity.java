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
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import android.widget.TextView.OnEditorActionListener;
import com.appbuilder.sdk.android.AppBuilderModuleMain;
import com.appbuilder.sdk.android.Widget;
import com.appbuilder.sdk.android.authorization.Authorization;
import com.appbuilder.sdk.android.authorization.entities.User;
import com.ibuildapp.romanblack.FanWallPlugin.data.FanWallMessage;
import com.ibuildapp.romanblack.FanWallPlugin.data.JSONParser;
import com.ibuildapp.romanblack.FanWallPlugin.data.Prefs;
import com.ibuildapp.romanblack.FanWallPlugin.data.Statics;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

import java.io.File;
import java.nio.charset.Charset;

/**
 * This activity provides send comment functionality.
 */
public class SendMessageActivity extends AppBuilderModuleMain implements OnClickListener,
        OnEditorActionListener, TextWatcher {

    private final int TAKE_A_PICTURE_ACTIVITY = 10000;
    private final int PICK_IMAGE_ACTIVITY = 10001;
    private final int CLOSE_ACTIVITY_OK = 0;
    private final int CLOSE_ACTIVITY_BAD = 1;
    private boolean uploading = false;
    private boolean needMenu = false;
    private String imagePath = "";
    private Widget widget = null;
    private FanWallMessage message = null;
    private FanWallMessage recievedMessage = null;
    private LinearLayout imageLayout = null;
    private ImageView imageImageView = null;
    private Button removeImageButton = null;
    private EditText messageEditText = null;
    private TextView cancelButton = null;
    private TextView clearButton = null;
    private TextView postButton = null;
    private ImageView photoButton = null;
    private TextView symbolCounter = null;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case CLOSE_ACTIVITY_OK: {
                    closeActivityOK();
                }
                break;
                case CLOSE_ACTIVITY_BAD: {
                    closeActivityBad();
                }
                break;
            }
        }
    };
    private ProgressDialog progressDialog;

    @Override
    public void create() {
        setContentView(R.layout.romanblack_fanwall_send_message);
        hideTopBar();
        swipeBlock();

        Intent currentIntent = getIntent();
        widget = (Widget) currentIntent.getSerializableExtra("Widget");

        message = (FanWallMessage) currentIntent.getSerializableExtra("message");


        imageLayout = (LinearLayout) findViewById(R.id.romanblack_fanwall_send_message_image_layout);
        imageLayout.setVisibility(View.GONE);

        View root = findViewById(R.id.romanblack_fanwall_send_message_main);
        root.setBackgroundColor(Statics.color1);

        imageImageView = (ImageView) findViewById(R.id.romanblack_fanwall_send_message_image);

        removeImageButton = (Button) findViewById(R.id.romanblack_fanwall_sendmessage_removeimage_button);
        removeImageButton.setOnClickListener(this);

        messageEditText = (EditText) findViewById(R.id.romanblack_fanwall_sendmessage_edittext);
        messageEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        messageEditText.addTextChangedListener(this);

        cancelButton = (TextView) findViewById(R.id.romanblack_fanwall_sendmessage_cancelbtn);
        cancelButton.setOnClickListener(this);
        cancelButton.setTextColor(bottomBarDesign.leftButtonDesign.textColor);

        clearButton = (TextView) findViewById(R.id.romanblack_fanwall_sendmessage_clear_btn);
        clearButton.setOnClickListener(this);
        clearButton.setTextColor(bottomBarDesign.leftButtonDesign.textColor);

        photoButton = (ImageView) findViewById(R.id.romanblack_fanwall_sendmessage_image_photo);
        photoButton.setOnClickListener(this);

        postButton = (TextView) findViewById(R.id.romanblack_fanwall_sendmessage_post_btn);
        postButton.setOnClickListener(this);

        postButton.setTextColor(bottomBarDesign.leftButtonDesign.textColor);

        symbolCounter = (TextView) findViewById(R.id.romanblack_fanwall_sendmessage_symbols_counter);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TAKE_A_PICTURE_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                imagePath = data.getStringExtra("imagePath");

                if (TextUtils.isEmpty(imagePath))
                    return;

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

                setImage();
            }
        }
    }

    /**
     * This menu contains "take from camera", "choose from gallery" and cancel buttons.
     *
     * @param menu
     * @return true
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        if (needMenu) {
            menu.add(R.string.romanblack_fanwall_camera_upper).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem arg0) {
                    Intent it = new Intent(SendMessageActivity.this, CameraActivity.class);
                    it.putExtra("Widget", widget);
                    startActivityForResult(it, TAKE_A_PICTURE_ACTIVITY);
                    needMenu = false;

                    return true;
                }
            });
            menu.add(R.string.romanblack_fanwall_gallery_upper).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem arg0) {
                    Intent i = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, PICK_IMAGE_ACTIVITY);
                    needMenu = false;

                    return true;
                }
            });
            menu.add(R.string.common_cancel_upper).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem arg0) {
                    needMenu = false;

                    return true;
                }
            });
        }

        return true;
    }

    /**
     * Sets up image after it was taken from camera or chosen from gallery.
     */
    private void setImage() {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, opts);

        //Find the correct scale value. It should be the power of 2.
        int width = opts.outWidth, height = opts.outHeight;
        int scale = 1;
        while (true) {
            if (width / 2 <= 100 || height / 2 <= 100) {
                break;
            }
            width /= 2;
            height /= 2;
            scale *= 2;
        }
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inSampleSize = scale;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, opt);
        imageImageView.setImageBitmap(bitmap);

        imageLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Closes the activity with "OK" result.
     */
    private void closeActivityOK() {
        Intent it = new Intent();
        it.putExtra("message", recievedMessage);
        setResult(RESULT_OK, it);

        finish();
    }

    /**
     * Closes the activity with "Cancel" result.
     */
    private void closeActivityBad() {
        setResult(RESULT_CANCELED);

        finish();
    }

    public void onClick(View arg0) {
        if (!uploading) {
            if (arg0.getId() == R.id.romanblack_fanwall_sendmessage_removeimage_button) {
                imageLayout.setVisibility(View.GONE);
                imagePath = "";
            } else if (arg0.getId() == R.id.romanblack_fanwall_sendmessage_cancelbtn) {
                finish();
            } else if (arg0.getId() == R.id.romanblack_fanwall_sendmessage_clear_btn) {
                messageEditText.setText("");
            } else if (arg0.getId() == R.id.romanblack_fanwall_sendmessage_image_photo) {
                needMenu = true;

                openOptionsMenu();
            } else if (arg0.getId() == R.id.romanblack_fanwall_sendmessage_post_btn) {
                showProgressDialog();

                uploading = true;
                if (messageEditText.getText().length() > 150) {
                    Toast.makeText(this, R.string.romanblack_fanwall_alert_big_text,
                            Toast.LENGTH_LONG).show();
                    uploading = false;

                    hideProgressDialog();

                    return;
                }

                if ((messageEditText.getText().length() == 0)
                        && (imageLayout.getVisibility() == View.GONE)) {
                    Toast.makeText(this, R.string.romanblack_fanwall_alert_empty_message,
                            Toast.LENGTH_LONG).show();
                    uploading = false;

                    hideProgressDialog();

                    return;
                }

                new Thread(new Runnable() {
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
                                Toast.makeText(SendMessageActivity.this, "You can't comment this message!", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }

                        recievedMessage = Statics.postMessage(messageEditText.getText().toString(), imagePath, (int) parentId, (int) replyId, Prefs.with(getApplicationContext()).getBoolean(Prefs.KEY_GPS, false));
                        if (recievedMessage != null)
                            handler.sendEmptyMessage(CLOSE_ACTIVITY_OK);
                        else
                            handler.sendEmptyMessage(CLOSE_ACTIVITY_BAD);
                    }
                }).start();
            }
        }
    }

    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        return false;
    }

    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    }

    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    }

    public void afterTextChanged(Editable arg0) {
        if (arg0 != null) {
            if (arg0.length() > 150) {
                arg0.replace(arg0.length() - 1, arg0.length(), "");
                Toast.makeText(this, R.string.romanblack_fanwall_alert_big_text, Toast.LENGTH_SHORT).show();
                symbolCounter.setText("150/150");
            } else {
                symbolCounter.setText(arg0.length() + "/150");
            }
        } else {
            symbolCounter.setText("0/150");
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
}
