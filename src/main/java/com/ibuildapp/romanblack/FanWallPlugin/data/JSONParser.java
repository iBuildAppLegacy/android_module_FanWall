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
package com.ibuildapp.romanblack.FanWallPlugin.data;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class provides static methods for JSON parsing.
 */
public class JSONParser {

    /**
     * Parses JSON comments data.
     *
     * @param data JSON data to parse.
     * @return comments array
     */
    public static ArrayList<FanWallMessage> parseMessagesString(String data) {
        try {
            String resp = data;

            if (resp == null) {
                return null;
            }

            if (resp.length() == 0) {
                return null;
            }

            JSONObject mainObject = new JSONObject(resp);

            JSONArray messagesJSON = mainObject.getJSONArray("posts");

            ArrayList<FanWallMessage> parsedMessages = new ArrayList<FanWallMessage>();

            for (int i = 0; i < messagesJSON.length(); i++) {
                JSONObject messageJSON = messagesJSON.getJSONObject(i);

                FanWallMessage tmpMessage = new FanWallMessage();
                tmpMessage.setId(new Long(messageJSON.getString("post_id")).longValue());
                tmpMessage.setAuthor(messageJSON.getString("user_name"));
                tmpMessage.setDate(new Date(
                        new Long(messageJSON.getString("create")).longValue()));
                tmpMessage.setUserAvatarUrl(messageJSON.getString("user_avatar"));
                tmpMessage.setText(messageJSON.getString("text"));
                try {
                    tmpMessage.setPoint(
                            new Float(messageJSON.getString("latitude")).floatValue(),
                            new Float(messageJSON.getString("longitude")).floatValue());
                } catch (NumberFormatException nFEx) {
                    Log.e("", "");
                }

                try {
                    tmpMessage.setParentId(new Integer(messageJSON.getString("parent_id")).intValue());
                } catch (NumberFormatException nFEx) {
                    Log.e("", "");
                }

                try {
                    tmpMessage.setReplyId(new Integer(messageJSON.getString("reply_id")).intValue());
                } catch (NumberFormatException nFEx) {
                    Log.e("", "");
                }

                tmpMessage.setTotalComments(new Integer(messageJSON.getString("total_comments")).intValue());

                JSONArray imagesJSON = messageJSON.getJSONArray("images");
                if (imagesJSON.length() > 0) {
                    tmpMessage.setImageUrl(imagesJSON.getString(0));
                }

                tmpMessage.setAccountId(messageJSON.getString("account_id"));
                tmpMessage.setAccountType(messageJSON.getString("account_type"));

                parsedMessages.add(tmpMessage);
            }

            return parsedMessages;
        } catch (JSONException jSSONEx) {
            return null;
        }
    }

    public static int getPostCommentsCount( long postId )
    {
        int totalCount = -1;
        String url = String.format("http://%s/modules/fanwall/%s/%s/commentcnt/%d/",
                com.appbuilder.sdk.android.Statics.BASE_DOMEN,
                Statics.APP_ID,
                Statics.MODULE_ID,
                postId);
        String resp = loadURLData(url);

        if ( !TextUtils.isEmpty(resp) )
        {
            try {
                JSONObject mainObject = new JSONObject(resp);
                if ( TextUtils.isEmpty( mainObject.getString("error") ) )
                {
                    totalCount = mainObject.getInt("commentcnt");
                    return totalCount;
                } else
                    return totalCount;
            } catch (JSONException e) {
                return totalCount;
            }
        }

        return totalCount;
    }

    /**
     * Downloads and parses JSON comments data.
     *
     * @param url URL resource that contains JSON data
     * @return comments array
     */
    public static ArrayList<FanWallMessage> parseMessagesUrl(String url) {
        try {
            Log.e("URLTAG", url);

            String resp = loadURLData(url);

            if (resp == null) {
                return null;
            }

            if (resp.length() == 0) {
                return null;
            }

            JSONObject mainObject = new JSONObject(resp);

            JSONArray messagesJSON = mainObject.getJSONArray("posts");

            ArrayList<FanWallMessage> parsedMessages = new ArrayList<FanWallMessage>();

            for (int i = 0; i < messagesJSON.length(); i++) {
                JSONObject messageJSON = messagesJSON.getJSONObject(i);

                FanWallMessage tmpMessage = new FanWallMessage();
                tmpMessage.setId(new Long(messageJSON.getString("post_id")).longValue());
                tmpMessage.setAuthor(messageJSON.getString("user_name"));
                tmpMessage.setDate(new Date(
                        new Long(messageJSON.getString("create")).longValue()));
                tmpMessage.setUserAvatarUrl(messageJSON.getString("user_avatar"));
                tmpMessage.setText(messageJSON.getString("text"));
                try {
                    tmpMessage.setPoint(
                            new Float(messageJSON.getString("latitude")).floatValue(),
                            new Float(messageJSON.getString("longitude")).floatValue());
                } catch (NumberFormatException nFEx) {
                    Log.e("", "");
                }

                try {
                    tmpMessage.setParentId(new Integer(messageJSON.getString("parent_id")).intValue());
                } catch (NumberFormatException nFEx) {
                    Log.e("", "");
                }

                try {
                    tmpMessage.setReplyId(new Integer(messageJSON.getString("reply_id")).intValue());
                } catch (NumberFormatException nFEx) {
                    Log.e("", "");
                }

                tmpMessage.setTotalComments(new Integer(messageJSON.getString("total_comments")).intValue());

                JSONArray imagesJSON = messageJSON.getJSONArray("images");
                if (imagesJSON.length() > 0) {
                    tmpMessage.setImageUrl(imagesJSON.getString(0));
                }

                tmpMessage.setAccountId(messageJSON.getString("account_id"));
                tmpMessage.setAccountType(messageJSON.getString("account_type"));

                parsedMessages.add(tmpMessage);
            }

            return parsedMessages;
        } catch (JSONException jSSONEx) {

            return null;
        }
    }

    /**
     * Downloads and parses JSON comments that contains images data.
     *
     * @param url URL resource that contains JSON data
     * @return comments array
     */
    public static ArrayList<FanWallMessage> parseGalleryUrl(String url) {
        try {
            Log.e("URLTAG", url);

            String resp = loadURLData(url);

            if (resp == null) {
                return null;
            }

            if (resp.length() == 0) {
                return null;
            }

            JSONObject mainObject = new JSONObject(resp);

            JSONArray messagesJSON = mainObject.getJSONArray("gallery");

            ArrayList<FanWallMessage> parsedMessages = new ArrayList<FanWallMessage>();

            for (int i = 0; i < messagesJSON.length(); i++) {
                JSONObject messageJSON = messagesJSON.getJSONObject(i);

                FanWallMessage tmpMessage = new FanWallMessage();
                tmpMessage.setId(messageJSON.getLong("post_id"));
                tmpMessage.setAuthor(messageJSON.getString("user_name"));
                tmpMessage.setText(messageJSON.getString("text"));

                JSONArray imagesJSON = messageJSON.getJSONArray("images");
                if (imagesJSON.length() > 0) {
                    tmpMessage.setImageUrl(imagesJSON.getString(0));
                }

                parsedMessages.add(tmpMessage);
            }

            return parsedMessages;
        } catch (JSONException jSSONEx) {
            return null;
        }
    }

    /**
     * Downloads and parses JSON profile data.
     *
     * @param url URL resource that contains JSON data
     * @return the profile data strings
     */
    public static String[] parseProfileData(String url) {
        try {
            Log.e("LOGURL", url);

            String[] res = new String[3];

            String resp = loadURLData(url);

            if (resp == null) {
                return null;
            }

            if (resp.length() == 0) {
                return null;
            }

            JSONObject mainObject = new JSONObject(resp);

            JSONObject dataObject = mainObject.getJSONObject("data");

            try {
                res[0] = dataObject.getString("total_posts");
            } catch (JSONException jSONEx) {
                Log.e("", "");
            }
            try {
                res[1] = dataObject.getString("total_comments");
            } catch (JSONException jSONEx) {
                Log.e("", "");
            }
            try {
                res[2] = dataObject.getString("last_message");
            } catch (JSONException jSONEx) {
                Log.e("", "");
            }

            Log.e("", "");

            return res;
        } catch (JSONException jSONEx) {
            return null;
        }
    }

    /**
     * Download URL data to String.
     *
     * @param msgsUrl URL to download
     * @return data string
     */
    private static String loadURLData(String msgsUrl) {
        try {
            URL url = new URL(msgsUrl);
            URLConnection conn = url.openConnection();
            InputStreamReader streamReader = new InputStreamReader(conn.getInputStream());

            BufferedReader br = new BufferedReader(streamReader);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            br.close();
            String resp = sb.toString();

            return resp;
        } catch (IOException iOEx) {
            Log.d("", "");

            return "";
        }
    }

    /**
     * Downloads and parses JSON comments data.
     * @return comments array
     */
    public static ArrayList<FanWallMessage> makeRequest(long wallMsgId, long commentId, long wallCommentsMsgIdEarly, int limit) {
        try {
            String url = String.format("%s/%s/%s/%d/%d/%d/%d/%s/%s", Statics.BASE_URL,
                    com.appbuilder.sdk.android.Statics.appId,
                    Statics.MODULE_ID,
                    wallMsgId,
                    commentId,
                    wallCommentsMsgIdEarly,
                    limit,
                    com.appbuilder.sdk.android.Statics.appId,
                    com.appbuilder.sdk.android.Statics.appToken);

            Log.e("URLTAG", url);

            String resp = loadURLData(url);

            if (TextUtils.isEmpty(resp))
                return null;

            JSONObject mainObject = new JSONObject(resp);

            JSONArray messagesJSON = mainObject.getJSONArray("posts");

            ArrayList<FanWallMessage> parsedMessages = new ArrayList<FanWallMessage>();

            for (int i = 0; i < messagesJSON.length(); i++) {
                JSONObject messageJSON = messagesJSON.getJSONObject(i);

                FanWallMessage tmpMessage = new FanWallMessage();
                try{
                    tmpMessage.setSharingCount(new Long(messageJSON.getString("sharing_count")).longValue());
                } catch (JSONException e) { }
                tmpMessage.setId(new Long(messageJSON.getString("post_id")).longValue());
                tmpMessage.setAuthor(messageJSON.getString("user_name"));
                tmpMessage.setDate(new Date(
                        new Long(messageJSON.getString("create")).longValue()));
                tmpMessage.setUserAvatarUrl(messageJSON.getString("user_avatar"));
                tmpMessage.setText(messageJSON.getString("text"));
                try {
                    tmpMessage.setPoint(
                            new Float(messageJSON.getString("latitude")).floatValue(),
                            new Float(messageJSON.getString("longitude")).floatValue());
                } catch (NumberFormatException nFEx) {
                    Log.e("", "");
                }

                try {
                    tmpMessage.setParentId(new Integer(messageJSON.getString("parent_id")).intValue());
                } catch (NumberFormatException nFEx) {
                    Log.e("", "");
                }

                try {
                    tmpMessage.setReplyId(new Integer(messageJSON.getString("reply_id")).intValue());
                } catch (NumberFormatException nFEx) {
                    Log.e("", "");
                }

                tmpMessage.setTotalComments(new Integer(messageJSON.getString("total_comments")).intValue());

                JSONArray imagesJSON = messageJSON.getJSONArray("images");
                if (imagesJSON.length() > 0) {
                    tmpMessage.setImageUrl(imagesJSON.getString(0));
                }

                tmpMessage.setAccountId(messageJSON.getString("account_id"));
                tmpMessage.setAccountType(messageJSON.getString("account_type"));

                parsedMessages.add(tmpMessage);
            }

            return parsedMessages;
        } catch (JSONException jSSONEx) {

            return null;
        }
    }
}
