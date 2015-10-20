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
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.appbuilder.sdk.android.AppBuilderModuleMain;
import com.appbuilder.sdk.android.authorization.Authorization;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This activity provides authorization via email functionality.
 */
public class EMailAuthorizationActivity extends AppBuilderModuleMain implements OnClickListener,
        View.OnFocusChangeListener {

    private final int CLOSE_ACTIVITY_OK = 0;
    private final int CLOSE_ACTIVITY_BAD = 1;
    private final int NEED_INTERNET_CONNECTION = 2;
    private final int FAILED_TO_LOGIN = 3;
    private final int SHOW_PROGRESS_DIALOG = 4;
    private final int HIDE_PROGRESS_DIALOG = 5;
    private final int EMAIL_NOT_MATCHES = 6;
    private String DEFAULT_EMAIL_TEXT = "";
    private String DEFAULT_PASSWORD_TEXT = "";
    private ProgressDialog progressDialog = null;
    private EditText loginEditText = null;
    private EditText passwordEditText = null;
    private Button loginButton = null;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case CLOSE_ACTIVITY_OK: {
                    closeActivityOk();
                }
                break;
                case CLOSE_ACTIVITY_BAD: {
                }
                break;
                case NEED_INTERNET_CONNECTION: {
                    Toast.makeText(EMailAuthorizationActivity.this, getString(R.string.alert_no_internet), Toast.LENGTH_LONG).show();
                }
                break;
                case FAILED_TO_LOGIN: {
                    Toast.makeText(EMailAuthorizationActivity.this, getString(R.string.romanblack_fanwall_alert_failed_to_login), Toast.LENGTH_LONG).show();
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
                case EMAIL_NOT_MATCHES: {
                    Toast.makeText(EMailAuthorizationActivity.this, getString(R.string.alert_invalid_email), Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    };

    @Override
    public void create() {
        setContentView(R.layout.romanblack_fanwall_email_auth);

        DEFAULT_EMAIL_TEXT = getString(R.string.common_email_upper);
        DEFAULT_PASSWORD_TEXT = getString(R.string.common_password_upper);

        loginEditText = (EditText) findViewById(R.id.romanblack_fanwall_emailauth_fname);
        loginEditText.setTextColor(Color.GRAY);

        passwordEditText = (EditText) findViewById(R.id.romanblack_fanwall_emailauth_lname);
        passwordEditText.setTextColor(Color.GRAY);

        loginButton = (Button) findViewById(R.id.romanblack_fanwall_emailauth_btn_sugnup);
        loginButton.setOnClickListener(this);

        // set topbar title
        setTopBarTitle(getResources().getString(R.string.common_log_in_upper));
        swipeBlock();
        setTopBarLeftButtonText(getResources().getString(R.string.common_cancel_upper), true, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_FIRST_USER);
                hideKeyboard();
                finish();
            }
        });
    }

    /**
     * Athorizes user via email.
     */
    private void login() {
        try {
            if (Authorization.authorizeEmail(loginEditText.getText().toString(),
                    passwordEditText.getText().toString())) {
                handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);

                handler.sendEmptyMessage(CLOSE_ACTIVITY_OK);
            } else {
                handler.sendEmptyMessage(FAILED_TO_LOGIN);
                handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
            }
        } catch (Exception e) {
            Log.d("", "");
        }
    }

    /**
     * Closes activity with "OK" result.
     */
    private void closeActivityOk() {
        Intent resIntent = new Intent();
        setResult(RESULT_OK, resIntent);

        finish();
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(this, null, getString(R.string.common_loading_upper));
        } else {
            if (!progressDialog.isShowing()) {
                progressDialog = ProgressDialog.show(this, null, getString(R.string.common_loading_upper));
            }
        }
    }

    private void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public void onClick(View arg0) {
        if (arg0.getId() == R.id.romanblack_fanwall_emailauth_btn_sugnup) {
            hideKeyboard();
            String regExpn =
                    "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                            + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                            + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                            + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                            + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                            + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

            Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(loginEditText.getText().toString());

            if (!matcher.matches()) {
                handler.sendEmptyMessage(EMAIL_NOT_MATCHES);
                return;
            }

            handler.sendEmptyMessage(SHOW_PROGRESS_DIALOG);
            hideKeyboard();

            new Thread(new Runnable() {
                public void run() {
                    login();
                }
            }).start();
        } else if (arg0.getId() == R.id.romanblack_fanwall_emailauth_fname) {
            if (loginEditText.getText().toString().equals(DEFAULT_EMAIL_TEXT)) {
                loginEditText.setText("");
                loginEditText.setTextColor(Color.BLACK);
            }
        } else if (arg0.getId() == R.id.romanblack_fanwall_emailauth_lname) {
            if (passwordEditText.getText().toString().equals(DEFAULT_PASSWORD_TEXT)) {
                passwordEditText.setText("");
                passwordEditText.setTextColor(Color.BLACK);
            }
        }
    }

    public void onFocusChange(View arg0, boolean arg1) {
        if (arg0.getId() == R.id.romanblack_fanwall_emailauth_fname) {
            if (arg1) {
                if (((TextView) arg0).getText().toString().equals(DEFAULT_EMAIL_TEXT)) {
                    ((TextView) arg0).setText("");
                    ((TextView) arg0).setTextColor(Color.BLACK);
                }
            } else {
                if (((TextView) arg0).getText().toString().equals("")) {
                    ((TextView) arg0).setText(DEFAULT_EMAIL_TEXT);
                    ((TextView) arg0).setTextColor(Color.GRAY);
                }
            }
        } else if (arg0.getId() == R.id.romanblack_fanwall_emailauth_lname) {
            if (arg1) {
                if (((TextView) arg0).getText().toString().equals(DEFAULT_PASSWORD_TEXT)) {
                    ((TextView) arg0).setText("");
                    ((TextView) arg0).setTextColor(Color.BLACK);
                }
            } else {
                if (((TextView) arg0).getText().toString().equals("")) {
                    ((TextView) arg0).setText(DEFAULT_PASSWORD_TEXT);
                    ((TextView) arg0).setTextColor(Color.GRAY);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_FIRST_USER);
        finish();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);
    }
}
