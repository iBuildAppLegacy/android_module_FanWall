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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.ibuildapp.romanblack.FanWallPlugin.R;
import com.ibuildapp.romanblack.FanWallPlugin.data.FanWallMessage;
import com.ibuildapp.romanblack.FanWallPlugin.data.Statics;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Adapter for messages list.
 */
public class MessageAdapter extends BaseAdapter {

    /**
     * Constructs new MessageAdapter instance
     *
     * @param ctx  - Activity that using this adapter
     * @param messages - messages list
     */
    public MessageAdapter(ArrayList<FanWallMessage> messages, Context ctx) {
        this.ctx = ctx;
        this.messages = messages;

        inflater = LayoutInflater.from(this.ctx);
    }

    private Context ctx = null;
    private LayoutInflater inflater = null;
    private ArrayList<FanWallMessage> messages = new ArrayList<FanWallMessage>();

    public int getCount() {
        return messages.size();
    }

    public Object getItem(int arg0) {
        if (messages.size() > arg0) {
            return messages.get(arg0);
        } else {
            return null;
        }
    }

    public long getItemId(int arg0) {
        if (messages.size() > arg0) {
            return messages.get(arg0).getId();
        } else {
            return 0;
        }
    }

    public View getView(int position, View arg1, ViewGroup arg2) {
        View res = null;
        res = inflater.inflate(R.layout.romanblack_fanwall_message_item, null);

        TextView nameTextView = (TextView) res.findViewById(R.id.romanblack_fanwall_messageitem_name);
        nameTextView.setText(messages.get(position).getAuthor());
        nameTextView.setTextColor(Statics.color3);

        TextView timeTextView = (TextView) res.findViewById(R.id.romanblack_fanwall_messageitem_date);
        try {
            SimpleDateFormat sdf;
            if (Locale.getDefault().toString().equals("en_US")) {
                sdf = new SimpleDateFormat("MMMM dd yyyy hh:mm");
            } else {
                sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm");
            }
            timeTextView.setText(sdf.format(messages.get(position).getDate()));
        } catch (NullPointerException nPEx) {
        }
        timeTextView.setTextColor(Statics.color5);

        TextView commentsCountTextView = (TextView) res.findViewById(R.id.romanblack_fanwall_messageitem_commentscount);
        commentsCountTextView.setText(messages.get(position).getTotalComments() + "");

        TextView textTextView = (TextView) res.findViewById(R.id.romanblack_fanwall_messageitem_text);
        textTextView.setText(messages.get(position).getText());
        textTextView.setTextColor(Statics.color4);

        res.setBackgroundColor(Statics.color1);

        return res;
    }
}
