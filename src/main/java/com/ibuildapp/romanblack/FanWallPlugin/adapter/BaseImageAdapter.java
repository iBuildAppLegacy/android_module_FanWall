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
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.ibuildapp.romanblack.FanWallPlugin.data.Statics;
import com.ibuildapp.romanblack.FanWallPlugin.view.RoundedImageView;

import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Base image adapter class provides easy access to download images for adapter asynchronous
 */
public class BaseImageAdapter extends BaseAdapter implements BaseImageAdapterInterface {

    // constants
    private final int UPDATE_IMAGES = 10003;
    private final int THREAD_POOL_SIZE = 5;
    private final int QUEUE_SIZE = 200;
    private final String TAG = "com.ibuildapp.masterapp.adapter.BaseImageAdapter";


    private Comparator<TaskItem> comparator = new Comparator<TaskItem>() {
        @Override
        public int compare(TaskItem dataObject, TaskItem dataObject2) {
            return (int) (dataObject2.timeStamp.getTime() - dataObject.timeStamp.getTime());
        }
    };
    protected Context context;
    protected ConcurrentHashMap<Integer, Bitmap> imageMap;
    protected ConcurrentHashMap<Integer, ImageView> imageViewMap;
    private PriorityBlockingQueue taskQueue;
    private QueueManager queueTask;
    private PullToRefreshListView uiView;
    private onLoadedListener onLoadedListener;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_IMAGES: {

                    if (uiView != null) {
                        try {
                            View v = uiView.findViewWithTag(msg.arg1);
                            if (v != null) {
                                ((ImageView) v).setImageBitmap(imageMap.get(msg.arg1));
                            } else
                                notifyDataSetChanged();
                        } catch (Exception e) {
                            notifyDataSetChanged();
                        }
                    } else
                        notifyDataSetChanged();
                }
                break;
            }
        }
    };

    public void clearBitmaps() {
        for (Integer key : imageMap.keySet()) {
            imageMap.get(key).recycle();
        }
        System.gc();
    }

    public void setOnLoadedListener(BaseImageAdapter.onLoadedListener onLoadedListener) {
        this.onLoadedListener = onLoadedListener;
    }

    public BaseImageAdapter(Context context, PullToRefreshListView uiView) {
        this.uiView = uiView;
        this.context = context;
        this.imageMap = new ConcurrentHashMap<Integer, Bitmap>();
        this.imageViewMap = new ConcurrentHashMap<Integer, ImageView>();
        this.taskQueue = new PriorityBlockingQueue(QUEUE_SIZE, comparator);
        queueTask = new QueueManager();
        queueTask.start();
    }

    @Override
    public void addTask(ImageView imageHolder, int uid, String DUBUG_PRODUCT_NAME, String resPath, String cachePath, String url, int width, int height, int rounded) {
        // refresh imageview for this uid!
        imageViewMap.put(uid, imageHolder);

        if (!imageMap.containsKey(uid)) {
            taskQueue.add(new TaskItem(imageHolder, uid, DUBUG_PRODUCT_NAME, resPath, cachePath, url, width, height, new Date(), rounded));
            Bitmap.Config conf = Bitmap.Config.valueOf("RGB_565");
            imageMap.put(uid, Bitmap.createBitmap(1, 1, conf));
        }
    }

    @Override
    public void stopAllTasks() {
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return null;
    }

    /**
     * TASK object
     */
    private class TaskItem {

        private ImageView imageView;
        private int uid;
        private String uri;
        private String resPath;
        private String cachePath;
        private String name;
        private int width;
        private int height;
        private Date timeStamp;
        private int rounded;

        public TaskItem(ImageView imageView, int uid, String name, String resPath, String cachePath, String uri, int width, int height, Date timeStamp, int rounded) {
            this.rounded = rounded;
            this.uid = uid;
            this.name = name;
            this.imageView = imageView;
            this.resPath = resPath;
            this.cachePath = cachePath;
            this.uri = uri;
            this.timeStamp = timeStamp;

            this.width = width;
            this.height = height;
        }
    }

    /**
     * This manager handle queue limits and creates new thread to download images
     */
    private class QueueManager extends Thread implements OnImageDoneListener {

        private ConcurrentHashMap<Integer, Thread> threadList;

        private QueueManager() {
            this.threadList = new ConcurrentHashMap<Integer, Thread>();
        }

        @Override
        public void run() {
            super.run();

            while (true) {
                if ( threadList.size() < THREAD_POOL_SIZE ) {

                    TaskItem taskItem = null;
                    while ( (taskItem = (TaskItem) taskQueue.poll()) != null )
                    {
                        if (!threadList.contains(taskItem.uid)) {
                            GetBitmapTask task =
                                    new GetBitmapTask(context,
                                            taskItem.uid,
                                            taskItem.name,
                                            taskItem.imageView,
                                            taskItem.resPath,
                                            taskItem.cachePath,
                                            taskItem.uri,
                                            taskItem.width,
                                            taskItem.height,
                                            taskItem.rounded);
                            task.setListener(this);
                            threadList.put(taskItem.uid, task);
                            task.start();
                        }

                        if ( threadList.size() >= THREAD_POOL_SIZE )
                            break;
                    }
                }

                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onImageLoaded(int uid, ImageView imageHolder, String name, Bitmap image, String downloadedImagePath) {
            threadList.remove(uid);

            // put image to storage and refresh adapter
            if (image != null) {
                imageMap.put(uid, image);
                Message msg = handler.obtainMessage(UPDATE_IMAGES, uid, -1);
                handler.sendMessage(msg);

                if (!TextUtils.isEmpty(downloadedImagePath)) {
                    if (onLoadedListener != null) {
                        onLoadedListener.onImageLoaded(uid, downloadedImagePath);
                    }
                }
            }
        }
    }

    public interface onLoadedListener {

        /**
         * Callback after image downloading
         *
         * @param uid                 - image uid
         * @param downloadedImagePath - downloaded image path
         */
        void onImageLoaded(int uid, String downloadedImagePath);
    }
}