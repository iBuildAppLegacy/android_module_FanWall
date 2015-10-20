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
package com.ibuildapp.romanblack.FanWallPlugin.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.appbuilder.sdk.android.Utils;
import com.appbuilder.sdk.android.Widget;
import com.appbuilder.sdk.android.authorization.Authorization;
import com.appbuilder.sdk.android.authorization.FacebookAuthorizationActivity;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.ibuildapp.romanblack.FanWallPlugin.*;
import com.ibuildapp.romanblack.FanWallPlugin.data.FanWallMessage;
import com.ibuildapp.romanblack.FanWallPlugin.data.Statics;
import com.seppius.i18n.plurals.PluralResources;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;


public class MainLayoutMessagesAdapter extends BaseImageAdapter {

    // const
    private final String TAG = "com.ibuildapp.romanblack.FanWallPlugin.adapter.MainLayoutMessagesAdapter";
    private final String DATE_PATTERN = "HH:mm MMM d, yyyy";
    private final int MESSAGE_VIEW_ACTIVITY = 10001;

    // backend
    private Context context;
    private ArrayList<FanWallMessage> messages;
    private LayoutInflater inflater;
    private Resources res;
    private Widget widget;
    private Bitmap placeHolderAvatar;
    private Bitmap placeHolderMessageImage;
    private onEndReached innerInterface;
    private float density;

    public MainLayoutMessagesAdapter(Context context, PullToRefreshListView absListView, ArrayList<FanWallMessage> messages, Widget widget) {
        super(context, absListView);
        this.context = context;
        this.messages = messages;
        this.widget = widget;
        this.density = context.getResources().getDisplayMetrics().density;
        inflater = LayoutInflater.from(context);
        res = context.getResources();
        placeHolderAvatar = BitmapFactory.decodeResource(res, R.drawable.fanwall_avatar_placeholder);
        placeHolderMessageImage = BitmapFactory.decodeResource(res, R.drawable.romanblack_fanwall_picture_placeholder);
    }

    public void setInnerInterface(onEndReached innerInterface) {
        this.innerInterface = innerInterface;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return messages.get(i).hashCode();
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {

        if (i == (messages.size() - 1)) {
            if (innerInterface != null)
                innerInterface.endReached();
        }

        View itemView = view;
        ViewHolderMessage viewHolder = null;
        if (itemView == null) {
            itemView = inflater.inflate(R.layout.romanblack_fanwall_message_item_left, null);
            viewHolder = new ViewHolderMessage();
            viewHolder.rootRoot = (RelativeLayout) itemView.findViewById(R.id.romanblack_fanwall_messageitem_root_root);
            viewHolder.rootView = (LinearLayout) itemView.findViewById(R.id.romanblack_fanwall_comments_root);
            viewHolder.author = (TextView) itemView.findViewById(R.id.romanblack_fanwall_messageitem_name);
            viewHolder.date = (TextView) itemView.findViewById(R.id.romanblack_fanwall_messageitem_date);
            viewHolder.commentsCount = (TextView) itemView.findViewById(R.id.romanblack_fanwall_messageitem_commentscount);
            viewHolder.message = (TextView) itemView.findViewById(R.id.romanblack_fanwall_messageitem_text);
            viewHolder.avatar = (ImageView) itemView.findViewById(R.id.romanblack_fanwall_messageitem_avatar);
            viewHolder.imageHolder = (LinearLayout) itemView.findViewById(R.id.romanblack_fanwall_messageitem_msgimage_holder);
            viewHolder.image = (ImageView) itemView.findViewById(R.id.romanblack_fanwall_messageitem_msgimage);
            viewHolder.lastLayout = (LinearLayout) itemView.findViewById(R.id.romanblack_fanwall_last_layout);
            viewHolder.likeImage = (ImageView) itemView.findViewById(R.id.romanblack_fanwall_like_img);
            viewHolder.likeCount = (TextView) itemView.findViewById(R.id.romanblack_fanwall_messageitem_likecount);
            viewHolder.sharingCount = (TextView) itemView.findViewById(R.id.romanblack_fanwall_messageitem_sharingcount);
            viewHolder.sharingImage = (ImageView) itemView.findViewById(R.id.romanblack_fanwall_messageitem_sharing_img);
            viewHolder.likeHolder = (LinearLayout) itemView.findViewById(R.id.romanblack_fanwall_like_holder);
            viewHolder.sharingHolder = (LinearLayout) itemView.findViewById(R.id.romanblack_fanwall_sharing_holder);

            itemView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderMessage) itemView.getTag();
        }

        // for last item add bottom margins
        if (i == (messages.size() - 1))
            viewHolder.lastLayout.setVisibility(View.VISIBLE);
        else
            viewHolder.lastLayout.setVisibility(View.GONE);

        viewHolder.rootRoot.setBackgroundColor(Statics.color1);


        viewHolder.rootView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent it = new Intent(context, MessageView.class);
                it.putExtra("Widget", widget);
                it.putExtra("message", messages.get(i));
                ((Activity) context).startActivityForResult(it, MESSAGE_VIEW_ACTIVITY);
            }
        });

        viewHolder.author.setText(messages.get(i).getAuthor());
        viewHolder.author.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent it = new Intent(context, ProfileViewActivity.class);
                it.putExtra("messages", messages);
                it.putExtra("position", i);
                ((Activity) context).startActivity(it);
            }
        });

        viewHolder.date.setText(Statics.getTwitterFormattedDate(messages.get(i).getDate(), context));

        viewHolder.likeHolder.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Utils.networkAvailable((Activity)context))
                {
                    Toast.makeText(context, context.getString(R.string.alert_no_internet), Toast.LENGTH_SHORT).show();
                    return;
                }

                if ( Authorization.isAuthorized(Authorization.AUTHORIZATION_TYPE_FACEBOOK) )
                {
                    new Thread( new Runnable() {
                        @Override
                        public void run() {

                            try {
                                boolean res = FacebookAuthorizationActivity.like(messages.get(i).getImageUrl());
                                if ( res )
                                {
                                    ((FanWallPlugin)context).postIdToShare = messages.get(i).getId();
                                    messages.get(i).setLikedByMe( true );
                                    messages.get(i).setLikeCount(messages.get(i).getLikeCount()+1);
                                    ((FanWallPlugin) context).runOnUiThread( new Runnable() {
                                        @Override
                                        public void run() {
                                            notifyDataSetChanged();
                                        }
                                    });
                                }
                            } catch (FacebookAuthorizationActivity.FacebookNotAuthorizedException e) {
                                e.printStackTrace();
                            } catch (FacebookAuthorizationActivity.FacebookAlreadyLiked facebookAlreadyLiked) {
                                facebookAlreadyLiked.printStackTrace();
                            }
                        }
                    } ).start();
                } else
                {
                    ((FanWallPlugin)context).urlToLike = messages.get(i).getImageUrl();
                    ((FanWallPlugin)context).postIdToShare = messages.get(i).getId();
                    Authorization.authorize((Activity)context, ((FanWallPlugin)context).FACEBOOK_LIKE_AUTH, Authorization.AUTHORIZATION_TYPE_FACEBOOK);
                }
            }
        });

        viewHolder.sharingHolder.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FanWallPlugin)context).postIdToShare = messages.get(i).getId();
                ((FanWallPlugin)context).urlToLike = messages.get(i).getImageUrl();
                ((FanWallPlugin)context).showSharingDialog();
            }
        });

        if ( TextUtils.isEmpty(messages.get(i).getImageUrl()) )
        {
            viewHolder.likeImage.setVisibility(View.INVISIBLE);
            viewHolder.likeCount.setVisibility(View.INVISIBLE);
            viewHolder.sharingCount.setVisibility(View.INVISIBLE);
            viewHolder.sharingImage.setVisibility(View.INVISIBLE);
        } else
        {
            viewHolder.likeImage.setVisibility(View.VISIBLE);
            viewHolder.likeCount.setVisibility(View.VISIBLE);
            viewHolder.sharingCount.setVisibility(View.INVISIBLE);
            viewHolder.sharingImage.setVisibility(View.VISIBLE);

            if ( messages.get(i).isLikedByMe() )
                viewHolder.likeImage.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.fanwall_like_on));
            else
                viewHolder.likeImage.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.fanwall_like_off));

            viewHolder.likeCount.setText(Long.toString(messages.get(i).getLikeCount()));
            viewHolder.sharingCount.setText(Long.toString(messages.get(i).getSharingCount()));
        }
        viewHolder.commentsCount.setText(Integer.toString(messages.get(i).getTotalComments()));


        if (!TextUtils.isEmpty(messages.get(i).getText())) {
            viewHolder.message.setText(messages.get(i).getText().trim());
            viewHolder.message.setVisibility(View.VISIBLE);
        } else {
            viewHolder.message.setVisibility(View.GONE);
        }

        // set avatar bitmap
        viewHolder.avatar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent it = new Intent(context, ProfileViewActivity.class);
                it.putExtra("messages", messages);
                it.putExtra("position", i);
                it.putExtra("Widget", widget);
                ((Activity) context).startActivity(it);
            }
        });

        setAvatar(viewHolder.avatar, i, messages.get(i).getUserAvatarUrl().hashCode() + i);
        viewHolder.avatar.setTag(messages.get(i).getUserAvatarUrl().hashCode());

        // set message image
        if (TextUtils.isEmpty(messages.get(i).getImageUrl())) {
            viewHolder.imageHolder.setVisibility(View.GONE);
        } else {
            setMessageImage(viewHolder.image, i, messages.get(i).getImageUrl().hashCode());
            viewHolder.image.setTag(messages.get(i).getImageUrl().hashCode());
            viewHolder.imageHolder.setVisibility(View.VISIBLE);
        }
        viewHolder.image.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent it = new Intent(context, ImageViewActivity.class);
                ArrayList<FanWallMessage> msgs = new ArrayList<FanWallMessage>();
                msgs.add(messages.get(i));
                it.putExtra("messages", msgs);
                it.putExtra("position", 0);
                it.putExtra("Widget", widget);
                ((FanWallPlugin)context).startActivityForResult(it, ((FanWallPlugin)context).SHOW_IMAGES_ACTIVITY);
            }
        });

        return itemView;
    }

    private void setAvatar(ImageView imageHolder, int i, int uid) {
        Bitmap btm = imageMap.get(uid);
        if (btm == null || btm.getHeight() == 1) {
            imageHolder.setImageBitmap(placeHolderAvatar);
            FanWallMessage tempMsg = messages.get(i);
            String avaCache = Statics.cachePath + File.separator + Utils.md5(tempMsg.getUserAvatarUrl());
            tempMsg.setUserAvatarCache(avaCache);
            addTask(imageHolder, uid, tempMsg.getText(), null, avaCache, tempMsg.getUserAvatarUrl(), -1, -1, 15);
        } else {
            imageHolder.setImageBitmap(btm);
        }
    }

    private void setMessageImage(ImageView imageHolder, int i, int uid) {
        Bitmap btm = imageMap.get(uid);
        if (btm == null || btm.getHeight() == 1) {
            imageHolder.setImageBitmap(placeHolderMessageImage);
            FanWallMessage tempMsg = messages.get(i);
            String imageCache = Statics.cachePath + File.separator + Utils.md5(tempMsg.getImageUrl());
            addTask(imageHolder, uid, tempMsg.getText(), null, imageCache, tempMsg.getImageUrl(), -1, -1, -1);
        } else {
            imageHolder.setImageBitmap(btm);
        }
    }

    private static class ViewHolderMessage {
        public RelativeLayout rootRoot;
        public LinearLayout rootView;
        public TextView author;
        public TextView date;
        public TextView message;
        public ImageView avatar;
        public ImageView image;
        public LinearLayout imageHolder;
        public ImageView likeImage;
        public ImageView sharingImage;
        public TextView commentsCount;
        public TextView likeCount;
        public TextView sharingCount;
        public LinearLayout likeHolder;
        public LinearLayout sharingHolder;

        public LinearLayout lastLayout;


        public ViewHolderMessage() {
        }
    }

    public interface onEndReached {
        void endReached();
    }
}
