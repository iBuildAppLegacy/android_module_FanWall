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
package com.ibuildapp.romanblack.FanWallPlugin.data.DAO;

import android.util.Log;
import com.ibuildapp.romanblack.FanWallPlugin.data.FanWallMessage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class MessagesDAO {

    private final String LOG_TAG = "com.ibuildapp.romanblack.FanWallPlugin.data.DAO.MessagesDAO";
    private final String FILE_NAME = "ca";
    private String cachePath;

    public MessagesDAO(String cachePath) {
        this.cachePath = cachePath;

        // prepare cache folder
        File cacheFolder = new File(cachePath);
        if (!cacheFolder.exists()) {
            cacheFolder.mkdirs();
        }
    }

    public ArrayList<FanWallMessage> getMessages(int parentId, int repliesId) {
        // Deserialization
        File userCache = new File(cachePath + File.separator + String.format("%s-%d-%d", FILE_NAME, parentId, repliesId));
        if (userCache.exists()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(userCache));
                ArrayList<FanWallMessage> filter = (ArrayList<FanWallMessage>) ois.readObject();
                ois.close();
                return filter;
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage() != null ? e.getMessage() : e.toString());
                return null;
            }
        } else {
            return null;
        }
    }

    public void setMessages(List<FanWallMessage> messages, int parentId, int repliesId) {
        // serialization
        File cache = new File(cachePath + File.separator + String.format("%s-%d-%d", FILE_NAME, parentId, repliesId));
        if (cache.exists()) {
            cache.delete();
        }

        List<FanWallMessage> cacheMessages = new ArrayList<FanWallMessage>();

        if ((messages.size()) <= 20 && (!messages.isEmpty())) {
            cacheMessages = messages;
        } else if (messages.size() > 20) {
            for (int i = 0; i < 20; i++) {
                cacheMessages.add(messages.get(i));
            }
        }

        try {
            cache.createNewFile();
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cache));
            oos.writeObject(cacheMessages);
            oos.close();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            cache.delete();
        }
    }
}
