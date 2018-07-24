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

import android.content.Context;
import android.graphics.*;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;
import com.appbuilder.sdk.android.Utils;
import com.appbuilder.sdk.android.authorization.Authorization;
import com.appbuilder.sdk.android.authorization.FacebookAuthorizationActivity;
import com.appbuilder.sdk.android.authorization.entities.User;
import com.ibuildapp.romanblack.FanWallPlugin.R;
import com.ibuildapp.romanblack.FanWallPlugin.callback.OnAuthListener;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;

import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * This class contains global module variables.
 */
public class Statics {

    public enum STATES {

        NO_MESSAGES, HAS_MESSAGES, AUTHORIZATION_NO,
        AUTHORIZATION_YES,
        AUTHORIZATION_FACEBOOK, AUTHORIZATION_TWITTER, AUTHORIZATION_EMAIL
    }

    public final static String TAG = "com.ibuildapp.romanblack.FanWallPlugin.data.Statics";


    public static boolean hasAd;
    public static float near = 180;
    public static int moduleId = 0;
    public static String canEdit = "all";
    public static String appName = "";
    /* Color Scheme */
    public static int color1 = Color.parseColor("#4d4948");// background
    public static int color2 = Color.parseColor("#fff58d");// category header
    public static int color3 = Color.parseColor("#fff7a2");// text header
    public static int color4 = Color.parseColor("#ffffff");// text
    public static int color5 = Color.argb(80, 35, 35, 35);// date
    public static boolean isSchemaDark = false;

    /* Color Scheme ends */
    public static final String BASE_URL = com.appbuilder.sdk.android.Statics.BASE_DOMEN + "/mdscr/fanwall";
    public static String APP_ID = "0";
    public static String MODULE_ID = "0";
    public static Location currentLocation = null;
    public static ArrayList<OnAuthListener> onAuthListeners = new ArrayList<OnAuthListener>();
    public static String cachePath;
    public static String FACEBOOK_APP_TOKEN = "";
    public static String urlComment;

    /**
     * This happen when user authorized.
     * This method call all preset callbacks.
     */
    static public void onAuth() {
        if (onAuthListeners == null) {
            return;
        }

        if (onAuthListeners.isEmpty()) {
            return;
        }

        for (int i = 0; i < onAuthListeners.size(); i++) {
            onAuthListeners.get(i).onAuth();
        }
    }

    /**
     * Sets the downloaded avatar.
     *
     * @param filePath file path to avatar image
     */
    public static Bitmap publishAvatar(String filePath) {
        Bitmap bitmap = null;

        if (!TextUtils.isEmpty(filePath)) {
            try {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = 1;
                bitmap = BitmapFactory.decodeFile(filePath, opts);
                int size = 0;
                if (bitmap.getHeight() > bitmap.getWidth()) {
                    size = bitmap.getWidth();
                } else {
                    size = bitmap.getHeight();
                }
                Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(output);

                final int color = 0xff424242;
                final Paint paint = new Paint();
                final Rect rect = new Rect(0, 0, size, size);
                final RectF rectF = new RectF(rect);
                final float roundPx = 12;

                paint.setAntiAlias(true);
                canvas.drawARGB(0, 0, 0, 0);
                paint.setColor(color);
                canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                canvas.drawBitmap(bitmap, rect, rect, paint);

                bitmap.recycle();

                return output;
            } catch (Exception e) {

            }
        }
        return null;
    }

    /**
     * Sets the downloaded avatar.
     *
     * @param rawBitmap bitmap to round corners
     */
    public static Bitmap publishAvatar(Bitmap rawBitmap, int roundK) {
        if (rawBitmap == null)
            return null;

        try {
            int size = 0;
            if (rawBitmap.getHeight() > rawBitmap.getWidth()) {
                size = rawBitmap.getWidth();
            } else {
                size = rawBitmap.getHeight();
            }
            Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, size, size);
            final RectF rectF = new RectF(rect);
            final float roundPx = roundK;

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(rawBitmap, rect, rect, paint);

            rawBitmap.recycle();

            return output;
        } catch (Exception e) {

        }

        return null;
    }

    /**
     * Sets the downloaded attached image.
     *
     * @param fileName picture file path
     */
    public static Bitmap publishPicture(String fileName) {
        Bitmap bitmap = null;
        try {

            if (!TextUtils.isEmpty(fileName)) {
                try {
                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(fileName, opts);

                    //Find the correct scale value. It should be the power of 2.
                    int width = opts.outWidth, height = opts.outHeight;
                    int scale = 1;
                    while (true) {
                        if (width / 2 <= 150 || height / 2 <= 150) {
                            break;
                        }
                        width /= 2;
                        height /= 2;
                        scale *= 2;
                    }
                    BitmapFactory.Options opt = new BitmapFactory.Options();
                    opt.inSampleSize = scale;

                    bitmap = BitmapFactory.decodeFile(fileName, opt);
                    int size = 0;
                    if (bitmap.getHeight() > bitmap.getWidth()) {
                        size = bitmap.getWidth();
                    } else {
                        size = bitmap.getHeight();
                    }
                    Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
                    Canvas canvas = new Canvas(output);

                    final int color = 0xff424242;
                    final Paint paint = new Paint();
                    final Rect rect = new Rect(0, 0, size, size);
                    final RectF rectF = new RectF(rect);
                    final float roundPx = 0;

                    paint.setAntiAlias(true);
                    canvas.drawARGB(0, 0, 0, 0);
                    paint.setColor(color);
                    canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                    canvas.drawBitmap(bitmap, rect, rect, paint);

                    bitmap.recycle();

                    return output;
                } catch (Exception e) {
                    Log.w("", "");
                }
            }
        } catch (Exception ex) {

        }

        return null;
    }

    /**
     * convert background color to font color
     */
    public static int BackColorToFontColor(int backColor) {
        int r = (backColor >> 16) & 0xFF;
        int g = (backColor >> 8) & 0xFF;
        int b = (backColor >> 0) & 0xFF;

        double Y = (0.299 * r + 0.587 * g + 0.114 * b);
        if (Y > 127) {
            return Color.BLACK;
        } else {
            return Color.WHITE;
        }
    }

    /**
     * Opens Bitmap from file
     *
     * @param fileName - file path
     * @return
     */
    public static Bitmap proccessBitmap(String fileName, Bitmap.Config config, int widthLimit) {

        Bitmap bitmap = null;
        File tempFile = null;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        try {
            // decode image with appropriate options
            tempFile = new File(fileName);
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(tempFile), null, opts);
        } catch (Exception e) {
        }

        //Find the correct scale value. It should be the power of 2.
        int width = opts.outWidth, height = opts.outHeight;
        ;
        int scale = 1;
        while (true) {
            if (width / 2 <= widthLimit || height / 2 <= widthLimit) {
                break;
            }
            width /= 2;
            height /= 2;
            scale *= 2;
        }

        opts = new BitmapFactory.Options();
        opts.inSampleSize = scale;
        opts.inPreferredConfig = config;

        try {
            System.gc();
            bitmap = BitmapFactory.decodeStream(new FileInputStream(tempFile), null, opts);
            if (bitmap != null) {
                return bitmap;
            }
        } catch (Exception ex) {
        } catch (OutOfMemoryError e) {
        }

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            System.gc();
            bitmap = BitmapFactory.decodeStream(new FileInputStream(tempFile), null, opts);
            if (bitmap != null) {
                return bitmap;
            }
        } catch (Exception ex) {
        } catch (OutOfMemoryError ex) {
        }

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            System.gc();
            bitmap = BitmapFactory.decodeStream(new FileInputStream(tempFile), null, opts);
        } catch (Exception ex) {
        } catch (OutOfMemoryError ex) {
        }

        return bitmap;
    }

    /**
     * Opens Bitmap from stream
     *
     * @param stream - input stream
     * @return
     */
    public static Bitmap proccessBitmap(InputStream stream, Bitmap.Config config) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = config;
        Bitmap bitmap = null;
        try {
            // decode image with appropriate options
            try {
                System.gc();
                bitmap = BitmapFactory.decodeStream(stream, null, opts);
            } catch (Exception ex) {
                Log.d("", "");
            } catch (OutOfMemoryError e) {
                Log.d("", "");
                System.gc();
                try {
                    bitmap = BitmapFactory.decodeStream(stream, null, opts);
                } catch (Exception ex) {
                    Log.d("", "");
                } catch (OutOfMemoryError ex) {
                    Log.e("decodeImageFile", "OutOfMemoryError");
                }
            }
        } catch (Exception e) {
            Log.d("", "");
            return null;
        }

        return bitmap;
    }

    /**
     * download file url and save it
     *
     * @param url
     */
    public static String downloadFile(String url) {
        int BYTE_ARRAY_SIZE = 1024;
        int CONNECTION_TIMEOUT = 30000;
        int READ_TIMEOUT = 30000;

        // downloading cover image and saving it into file
        try {
            URL imageUrl = new URL(URLDecoder.decode(url));
            URLConnection conn = imageUrl.openConnection();
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());

            File resFile = new File(cachePath + File.separator + Utils.md5(url));
            if (!resFile.exists()) {
                resFile.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(resFile);
            int current = 0;
            byte[] buf = new byte[BYTE_ARRAY_SIZE];
            Arrays.fill(buf, (byte) 0);
            while ((current = bis.read(buf, 0, BYTE_ARRAY_SIZE)) != -1) {
                fos.write(buf, 0, current);
                Arrays.fill(buf, (byte) 0);
            }

            bis.close();
            fos.flush();
            fos.close();
            Log.d("", "");
            return resFile.getAbsolutePath();
        } catch (SocketTimeoutException e) {
            return null;
        } catch (IllegalArgumentException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getTwitterFormattedDate(Date date, Context ctx) {
        // if date not found
        if (date == null) {
            date = new Date();
        }

        try {
            String ttt = ctx.getResources().getString(R.string.romanblack_twitter_date_minutes);
            ttt = ctx.getResources().getString(R.string.romanblack_twitter_date_minutes);

            long delta = (System.currentTimeMillis() - date.getTime()) / 1000;
            String strPubdate;
            if (delta <= 0) {
                strPubdate = 0 + ctx.getString(R.string.romanblack_twitter_date_seconds);
                return strPubdate;
            }

            if (delta < 60) {
                strPubdate = delta + ctx.getString(R.string.romanblack_twitter_date_seconds);
            } else if (delta < 120) {
                strPubdate = 1 + ctx.getString(R.string.romanblack_twitter_date_minutes);
            } else if (delta < (45 * 60)) {
                strPubdate = (int) (delta / 60) + ctx.getString(R.string.romanblack_twitter_date_minutes);
            } else if (delta < (2 * 60 * 60)) {
                strPubdate = 1 + ctx.getString(R.string.romanblack_twitter_date_hour_ago);
            } else if (delta < (24 * 60 * 60)) {
                strPubdate = (int) (delta / 3600) + ctx.getString(R.string.romanblack_twitter_date_hour_ago);
            } else {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ctx.getString(R.string.twitter_date_format_item));
                strPubdate = simpleDateFormat.format(date);
            }
            return strPubdate;
        } catch (Exception ex) {
            return date.toString();
        }
    }

    public static FanWallMessage postMessage(String msg, String imagePath, int parentId, int replyId, boolean withGps) {
        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
                HttpVersion.HTTP_1_1);
        HttpClient httpClient = new DefaultHttpClient(params);

        try {

            StringBuilder sb = new StringBuilder();
            sb.append(Statics.BASE_URL);
            sb.append("/");
            urlComment = sb.toString();
   /*         HttpPost httpPost;
            if (urlComment.contains("http")) {
                httpPost = new HttpPost(urlComment);
            }
            else {
                urlComment="http://"+urlComment;
                httpPost = new HttpPost(urlComment);
            }*/
            HttpPost httpPost = new HttpPost(Statics.BASE_URL + "/");


            MultipartEntity multipartEntity = new MultipartEntity();
            multipartEntity.addPart("app_id", new StringBody(com.appbuilder.sdk.android.Statics.appId, Charset.forName("UTF-8")));
            multipartEntity.addPart("token", new StringBody(com.appbuilder.sdk.android.Statics.appToken, Charset.forName("UTF-8")));
            multipartEntity.addPart("module_id", new StringBody(Statics.MODULE_ID, Charset.forName("UTF-8")));

            // подстановка коэффициентов
            multipartEntity.addPart("parent_id", new StringBody(Integer.toString(parentId), Charset.forName("UTF-8")));
            multipartEntity.addPart("reply_id", new StringBody(Integer.toString(replyId), Charset.forName("UTF-8")));

            if (Authorization.getAuthorizedUser().getAccountType() == User.ACCOUNT_TYPES.FACEBOOK) {
                multipartEntity.addPart("account_type", new StringBody("facebook", Charset.forName("UTF-8")));
            } else if (Authorization.getAuthorizedUser().getAccountType() == User.ACCOUNT_TYPES.TWITTER) {
                multipartEntity.addPart("account_type", new StringBody("twitter", Charset.forName("UTF-8")));
            } else {
                multipartEntity.addPart("account_type", new StringBody("ibuildapp", Charset.forName("UTF-8")));
            }
            multipartEntity.addPart("account_id", new StringBody(Authorization.getAuthorizedUser().getAccountId(), Charset.forName("UTF-8")));

            if (Authorization.getAuthorizedUser().getAccountType() == User.ACCOUNT_TYPES.TWITTER)
                multipartEntity.addPart("user_name", new StringBody(Authorization.getAuthorizedUser().getUserName(), Charset.forName("UTF-8")));
            else
                multipartEntity.addPart("user_name", new StringBody(Authorization.getAuthorizedUser().getUserName(), Charset.forName("UTF-8")));

            multipartEntity.addPart("user_avatar", new StringBody(Authorization.getAuthorizedUser().getAvatarUrl(), Charset.forName("UTF-8")));

            if ( withGps )
            {
                if (Statics.currentLocation != null) {
                    multipartEntity.addPart("latitude", new StringBody(Statics.currentLocation.getLatitude() + "", Charset.forName("UTF-8")));
                    multipartEntity.addPart("longitude", new StringBody(Statics.currentLocation.getLongitude() + "", Charset.forName("UTF-8")));
                }
            }

            multipartEntity.addPart("text", new StringBody(msg, Charset.forName("UTF-8")));

            if (!TextUtils.isEmpty(imagePath)) {
                multipartEntity.addPart("images", new FileBody(new File(imagePath)));
            }

            httpPost.setEntity(multipartEntity);

            String resp = httpClient.execute(httpPost, new BasicResponseHandler());

            return JSONParser.parseMessagesString(resp).get(0);

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * resize image with proportions
     *
     * @param fileName   - file path
     * @param widthLimit - max width limit
     */
    public static Bitmap resizeBitmapToMaxSize(String fileName, int widthLimit) {
        try {
            File tempFile = new File(fileName);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(tempFile), null, opts);

            //Find the correct scale value. It should be the power of 2.
            int width = opts.outWidth;
            int scale = 1;
            while (true) {
                if (width / 2 <= widthLimit) {
                    break;
                }
                width /= 2;
                scale *= 2;
            }

            opts = new BitmapFactory.Options();
            opts.inSampleSize = scale;

            // decode image with appropriate options
            Bitmap bitmap = null;
            try {
                System.gc();
                bitmap = BitmapFactory.decodeStream(new FileInputStream(tempFile), null, opts);
            } catch (Exception ex) {
                Log.d("", "");
            } catch (OutOfMemoryError e) {
                Log.d("", "");
                System.gc();
                try {
                    bitmap = BitmapFactory.decodeStream(new FileInputStream(tempFile), null, opts);
                } catch (Exception ex) {
                    Log.d("", "");
                } catch (OutOfMemoryError ex) {
                    Log.e("decodeImageFile", "OutOfMemoryError");
                }
            }
            return bitmap;

        } catch (FileNotFoundException e) {
            Log.e("", "");
            return null;
        }
    }

    public static ArrayList<FanWallMessage> getFbLikesForUrls( ArrayList<FanWallMessage> messagesList )
    {
        String token = FacebookAuthorizationActivity.getFbToken(com.appbuilder.sdk.android.Statics.FACEBOOK_APP_ID, com.appbuilder.sdk.android.Statics.FACEBOOK_APP_SECRET);
        if ( TextUtils.isEmpty(token) )
            return messagesList;

        if ( messagesList == null || messagesList.isEmpty() )
            return messagesList;

        // collect urls list
        List<String> urlList = new ArrayList<String>();
        for (int i = 0; i < messagesList.size(); i++) {
            if ( !TextUtils.isEmpty(messagesList.get(i).getImageUrl()) )
                urlList.add(messagesList.get(i).getImageUrl());
        }

        Map<String, String> tempResultMap = FacebookAuthorizationActivity.getLikesForUrls(urlList, token);

        if ( tempResultMap.size() != 0 )
        {
            Set<String> keySet = tempResultMap.keySet();
            for ( String key : keySet )
            {
                for ( FanWallMessage msg : messagesList )
                {
                    if ( !TextUtils.isEmpty(msg.getImageUrl()) && msg.getImageUrl().compareTo(key) == 0)
                    {
                        String likeCountStr = tempResultMap.get(key);
                        msg.setLikeCount( Long.parseLong(likeCountStr) );
                        break;
                    }
                }
            }
        }

        return messagesList;
    }


    public static IncrementSharingStatus incrementSharing( String postId )
    {
        String loginUrl = com.appbuilder.sdk.android.Statics.BASE_DOMEN + "/modules/fanwall/sharing_increment";
        Log.e(TAG, "incrementSharing url = " + loginUrl);


        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 15000);
        HttpConnectionParams.setSoTimeout(params, 15000);
        HttpClient httpClient = new DefaultHttpClient(params);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        try {
            HttpPost httpPost = new HttpPost(loginUrl);

            // order details
            nameValuePairs.add(new BasicNameValuePair("app_id", com.appbuilder.sdk.android.Statics.appId));
            nameValuePairs.add(new BasicNameValuePair("module_id", Statics.MODULE_ID));
            nameValuePairs.add(new BasicNameValuePair("post_id", postId));
            nameValuePairs.add(new BasicNameValuePair("token", com.appbuilder.sdk.android.Statics.appToken));

            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            String resp = httpClient.execute(httpPost, new BasicResponseHandler());

            if ( TextUtils.isEmpty(resp) ) {
                return null;
            }

            Log.e(TAG, "incrementSharing response = " + resp);

            JSONObject mainObject = new JSONObject(resp);

            IncrementSharingStatus result = new IncrementSharingStatus();
            result.status_code = mainObject.getInt("status_code");
            result.error = mainObject.getString("error");

            return result;

        } catch (Exception e) {
            return null;
        }
    }

    public static List<FanWallMessage> getLikedByMe( List<FanWallMessage> messages )
    {
        if ( Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_FACEBOOK) == null )
            return messages;

        List<String> likedList = null;
        try {
            likedList = FacebookAuthorizationActivity.getUserOgLikes();
            if( likedList != null && !likedList.isEmpty() )
                for ( String likedLink : likedList )
                {
                    for ( FanWallMessage msg : messages )
                    {
                        if ( likedLink.compareToIgnoreCase(msg.getImageUrl()) == 0 )
                        {
                            msg.setLikedByMe(true);
                            break;
                        }
                    }
                }

            return messages;
        } catch (FacebookAuthorizationActivity.FacebookNotAuthorizedException e) {
            return null;
        }

    }
}
