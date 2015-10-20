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

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.appbuilder.sdk.android.Utils;
import com.appbuilder.sdk.android.Widget;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.ibuildapp.romanblack.FanWallPlugin.ImageViewActivity;
import com.ibuildapp.romanblack.FanWallPlugin.MessageView;
import com.ibuildapp.romanblack.FanWallPlugin.R;
import com.ibuildapp.romanblack.FanWallPlugin.data.DetailActivityAdapterData;
import com.ibuildapp.romanblack.FanWallPlugin.data.FanWallMessage;
import com.ibuildapp.romanblack.FanWallPlugin.data.Statics;
import com.seppius.i18n.plurals.PluralResources;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DetailMessageAdapter extends BaseImageAdapter {

    // const
    private final String DATE_PATTERN = "HH:mm MMM d, yyyy";
    private final String TAG = "com.ibuildapp.romanblack.FanWallPlugin.adapter.DetailMessageAdapter";

    private final int TYPE_HEADER = 11;
    private final int TYPE_MSG = 12;
    private final int TYPE_NO_MSG = 13;
    private final int PADDING_IMAGE = 15;


    // backend
    private MessageView context;
    private List<DetailActivityAdapterData> messages;
    private LayoutInflater inflater;
    private Resources res;
    private FanWallMessage replyMsg;
    private Widget widget;
    private Bitmap placeHolderAvatar;
    private Bitmap placeHolderMessageImage;
    private onEndReached innerInterface;
    private float density;
    private int screenWidth;


    public DetailMessageAdapter(MessageView context, PullToRefreshListView absListView, List<DetailActivityAdapterData> messages, FanWallMessage msg, Widget widget) {
        super(context, absListView);
        this.context = context;
        this.messages = messages;
        this.widget = widget;
        this.density = context.getResources().getDisplayMetrics().density;
        Display display = context.getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();

        replyMsg = msg;
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
    public int getItemViewType(int position) {
        if (messages.get(position).message != null)
            return TYPE_MSG;
        else if (messages.get(position).header != null)
            return TYPE_HEADER;
        else
            return TYPE_NO_MSG;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {

        View row = view;
        int type = getItemViewType(i);

        switch (type) {
            case TYPE_HEADER: {
                if (row == null) {
                    row = inflater.inflate(R.layout.fanwall_detailpage_header, null);
                }

                LinearLayout msgRoot = (LinearLayout) row.findViewById(R.id.romanblack_fanwall_message_header_root);
                if (msgRoot == null)
                    row = inflater.inflate(R.layout.fanwall_detailpage_header, null);

                msgRoot = (LinearLayout) row.findViewById(R.id.romanblack_fanwall_message_header_root);
                msgRoot.setBackgroundColor(Statics.color1);

                TextView nameTextView = (TextView) row.findViewById(R.id.romanblack_fanwall_message_name);
                nameTextView.setText(messages.get(i).header.getAuthor());
                nameTextView.setTextColor(Statics.color3);

                TextView dateTextView = (TextView) row.findViewById(R.id.romanblack_fanwall_message_date);
                int temp = Statics.color3 & 0x00ffffff;
                int result = temp | 0x80000000;
                dateTextView.setTextColor(result | 0x80000000);
                try {
                    SimpleDateFormat sdf = null;
                    if (Locale.getDefault().toString().equals("en_US")) {
                        sdf = new SimpleDateFormat("MMMM dd yyyy hh:mm");
                    } else {
                        sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm");
                    }
                    dateTextView.setText(sdf.format(messages.get(i).header.getDate()));
                } catch (NullPointerException nPEx) {
                }

                TextView messageText = (TextView) row.findViewById(R.id.romanblack_fanwall_message_text);
                messageText.setText(messages.get(i).header.getText());
                messageText.setTextColor(Statics.color3);

                TextView messageCount = (TextView) row.findViewById(R.id.romanblack_fanwall_message_counter);
                String itemQty = "";
                try {
                    itemQty = new PluralResources(res).getQuantityString(R.plurals.numberOfComments,
                            messages.get(i).header.getTotalComments(),
                            messages.get(i).header.getTotalComments());
                } catch (NoSuchMethodException e) {
                    itemQty = "";
                }
                messageCount.setText(itemQty);
                messageCount.setTextColor(Statics.color3);

                // avatar set image
                ImageView messageAvatar = (ImageView) row.findViewById(R.id.romanblack_fanwall_message_avatar);
                if (messages.get(i).header.hasAvatar()) {
                    setAvatarHeader(messageAvatar, i, messages.get(i).header.getUserAvatarUrl().hashCode());
                    messageAvatar.setTag(messages.get(i).header.getUserAvatarUrl().hashCode());
                }

                ImageView messageImage = (ImageView) row.findViewById(R.id.romanblack_fanwall_message_msgimage);
                if (messages.get(i).header.hasImage()) {

                    messageImage.getLayoutParams().height = screenWidth - (2 * PADDING_IMAGE);
                    setMessageImageHeader(messageImage, i, messages.get(i).header.getImageUrl().hashCode());
                    messageImage.setTag(messages.get(i).header.getImageUrl().hashCode());
                    messageImage.setVisibility(View.VISIBLE);
                    messageImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent it = new Intent(context, ImageViewActivity.class);
                            ArrayList<FanWallMessage> msgs = new ArrayList<FanWallMessage>();
                            msgs.add(messages.get(i).header);
                            it.putExtra("messages", msgs);
                            it.putExtra("position", 0);
                            it.putExtra("Widget", widget);
                            context.startActivity(it);
                        }
                    });
                } else
                    messageImage.setVisibility(View.GONE);

            }
            break;

            case TYPE_MSG: {

                ViewHolderMessage viewHolder = null;
                if (row == null) {
                    row = inflater.inflate(R.layout.fanwall_detailpage_msg, null);
                    viewHolder = new ViewHolderMessage();
                    viewHolder.rootRoot = (LinearLayout) row.findViewById(R.id.romanblack_fanwall_commentitem_root);
                    viewHolder.author = (TextView) row.findViewById(R.id.romanblack_fanwall_commentitem_name);
                    viewHolder.data = (TextView) row.findViewById(R.id.romanblack_fanwall_commentitem_date);
                    viewHolder.separator = (LinearLayout) row.findViewById(R.id.romanblack_fanwall_commentitem_separator);
                    viewHolder.message = (TextView) row.findViewById(R.id.romanblack_fanwall_commentitem_text);
                    viewHolder.avatar = (ImageView) row.findViewById(R.id.romanblack_fanwall_commentitem_avatar);
                    viewHolder.imageHolder = (LinearLayout) row.findViewById(R.id.romanblack_fanwall_messageitem_msgimage_holder);
                    viewHolder.image = (ImageView) row.findViewById(R.id.romanblack_fanwall_messageitem_msgimage);
                    viewHolder.lastLayout = (LinearLayout) row.findViewById(R.id.romanblack_fanwall_commentitem_last);
                    row.setTag(viewHolder);
                } else {
                    LinearLayout root = (LinearLayout) row.findViewById(R.id.romanblack_fanwall_commentitem_root);
                    if (root == null) {
                        row = inflater.inflate(R.layout.fanwall_detailpage_msg, null);
                        viewHolder = new ViewHolderMessage();
                        viewHolder.rootRoot = (LinearLayout) row.findViewById(R.id.romanblack_fanwall_commentitem_root);
                        viewHolder.author = (TextView) row.findViewById(R.id.romanblack_fanwall_commentitem_name);
                        viewHolder.data = (TextView) row.findViewById(R.id.romanblack_fanwall_commentitem_date);
                        viewHolder.separator = (LinearLayout) row.findViewById(R.id.romanblack_fanwall_commentitem_separator);
                        viewHolder.message = (TextView) row.findViewById(R.id.romanblack_fanwall_commentitem_text);
                        viewHolder.avatar = (ImageView) row.findViewById(R.id.romanblack_fanwall_commentitem_avatar);
                        viewHolder.imageHolder = (LinearLayout) row.findViewById(R.id.romanblack_fanwall_messageitem_msgimage_holder);
                        viewHolder.image = (ImageView) row.findViewById(R.id.romanblack_fanwall_messageitem_msgimage);
                        viewHolder.lastLayout = (LinearLayout) row.findViewById(R.id.romanblack_fanwall_commentitem_last);
                        row.setTag(viewHolder);
                    } else {
                        viewHolder = (ViewHolderMessage) row.getTag();
                    }
                }
                if (Statics.BackColorToFontColor(Statics.color1) == Color.WHITE)
                    viewHolder.separator.setBackgroundColor(res.getColor(R.color.white_20_trans));
                else
                    viewHolder.separator.setBackgroundColor(res.getColor(R.color.black_20_trans));


                viewHolder.rootRoot.setBackgroundColor(Statics.color1);

                viewHolder.author.setText(messages.get(i).message.getAuthor());
                viewHolder.author.setTextColor(Statics.color3);

                viewHolder.data.setText(Statics.getTwitterFormattedDate(messages.get(i).message.getDate(), context));
                int temp = Statics.color3 & 0x00ffffff;
                int result = temp | 0x80000000;
                viewHolder.data.setTextColor(result);

                // add small holder to last item
                if (i == (messages.size() - 1))
                    viewHolder.lastLayout.setVisibility(View.VISIBLE);
                else
                    viewHolder.lastLayout.setVisibility(View.GONE);

                viewHolder.message.setText(messages.get(i).message.getText());
                viewHolder.message.setTextColor(Statics.color4);

                if (messages.get(i).message.hasAvatar()) {
                    setAvatarMessagee(viewHolder.avatar, i, messages.get(i).message.getUserAvatarUrl().hashCode()+i);
                    viewHolder.avatar.setTag(messages.get(i).message.getUserAvatarUrl().hashCode());
                }

                if (messages.get(i).message.hasImage()) {
                    setMessageImage(viewHolder.image, i, messages.get(i).message.getImageUrl().hashCode());
                    viewHolder.image.setTag(messages.get(i).message.getImageUrl().hashCode());
                    viewHolder.imageHolder.setVisibility(View.VISIBLE);
                    viewHolder.image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent it = new Intent(context, ImageViewActivity.class);
                            ArrayList<FanWallMessage> msgs = new ArrayList<FanWallMessage>();
                            msgs.add(messages.get(i).message);
                            it.putExtra("messages", msgs);
                            it.putExtra("position", 0);
                            it.putExtra("Widget", widget);
                            context.startActivity(it);
                        }
                    });
                } else
                    viewHolder.imageHolder.setVisibility(View.GONE);
            }
            break;

            case TYPE_NO_MSG: {
                if (row == null) {
                    row = inflater.inflate(R.layout.fanwall_detailpage_nomsg, null);
                }

                LinearLayout root = (LinearLayout) row.findViewById(R.id.romanblack_fanwall_nomsg_root);
                if (root == null)
                    row = inflater.inflate(R.layout.fanwall_detailpage_nomsg, null);

                root = (LinearLayout) row.findViewById(R.id.romanblack_fanwall_nomsg_root);
                root.setBackgroundColor(Statics.color1);

                TextView text = (TextView) row.findViewById(R.id.romanblack_fanwall_nomsg_text);
                int temp = Statics.color3 & 0x00ffffff;
                int result = temp | 0x7f000000;
                text.setTextColor(result);
            }
            break;
        }

        return row;
    }

    public interface onEndReached {
        void endReached();
    }

    private void setAvatarHeader(ImageView imageHolder, int i, int uid) {
        Bitmap btm = imageMap.get(uid);
        if (btm == null || btm.getHeight() == 1) {
            imageHolder.setImageBitmap(placeHolderAvatar);
            FanWallMessage tempMsg = messages.get(i).header;
            String avaCache = Statics.cachePath + File.separator + Utils.md5(tempMsg.getUserAvatarUrl());
            addTask(imageHolder, uid, tempMsg.getText(), null, tempMsg.getUserAvatarCache(), tempMsg.getUserAvatarUrl(), -1, -1, 15);
        } else {
            imageHolder.setImageBitmap(btm);
        }
    }

    private void setMessageImageHeader(ImageView imageHolder, int i, int uid) {
        Bitmap btm = imageMap.get(uid);
        if (btm == null || btm.getHeight() == 1) {
            imageHolder.setImageBitmap(placeHolderMessageImage);
            FanWallMessage tempMsg = messages.get(i).header;
            String imageCache = Statics.cachePath + File.separator + Utils.md5(tempMsg.getImageUrl());
            addTask(imageHolder, uid, tempMsg.getText(), null, tempMsg.getImageCachePath(), tempMsg.getImageUrl(), -1, -1, -1);
        } else {
            imageHolder.setImageBitmap(btm);
        }
    }


    private void setAvatarMessagee(ImageView imageHolder, int i, int uid) {
        Bitmap btm = imageMap.get(uid);
        if (btm == null || btm.getHeight() == 1) {
            imageHolder.setImageBitmap(placeHolderAvatar);
            FanWallMessage tempMsg = messages.get(i).message;
            String avaCache = Statics.cachePath + File.separator + Utils.md5(tempMsg.getUserAvatarUrl());
            addTask(imageHolder, uid, tempMsg.getText(), null, tempMsg.getUserAvatarCache(), tempMsg.getUserAvatarUrl(), -1, -1, 15);
        } else {
            imageHolder.setImageBitmap(btm);
        }
    }

    private void setMessageImage(ImageView imageHolder, int i, int uid) {
        Bitmap btm = imageMap.get(uid);
        if (btm == null || btm.getHeight() == 1) {
            imageHolder.setImageBitmap(placeHolderMessageImage);
            FanWallMessage tempMsg = messages.get(i).message;
            String imageCache = Statics.cachePath + File.separator + Utils.md5(tempMsg.getImageUrl());
            addTask(imageHolder, uid, tempMsg.getText(), null, tempMsg.getImageCachePath(), tempMsg.getImageUrl(), -1, -1, -1);
        } else {
            imageHolder.setImageBitmap(btm);
        }
    }

    private static class ViewHolderMessage {
        public LinearLayout rootRoot;
        public TextView author;
        public TextView data;
        public TextView message;
        public ImageView avatar;
        public ImageView image;
        public LinearLayout separator;
        public LinearLayout imageHolder;
        public LinearLayout lastLayout;


        public ViewHolderMessage() {
        }
    }
}
