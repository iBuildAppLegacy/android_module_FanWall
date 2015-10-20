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
package com.ibuildapp.romanblack.FanWallPlugin.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ScrollView;

/**
 * This class represents ScrollView that ca handle overscroll events.
 */
public class MyScrollView extends ScrollView {

    private int currentVerticalScrollPosition = 0;
    private int counter = 0;
    private OnOverScrollListener mOnOverScrollListener = null;

    public MyScrollView(Context context) {
        super(context);
    }

    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        if ((t == 0) || (t == (this.getChildAt(0).getHeight() - this.getHeight()))) {
            counter++;
        } else {
            counter = 0;
        }

        currentVerticalScrollPosition = t;

        if ((t == 0) && (counter > 1)) {
            if (mOnOverScrollListener != null) {
                mOnOverScrollListener.onOverScrollTop(t);
            }
        }

        if ((t == (this.getChildAt(0).getHeight() - this.getHeight()))
                && (counter > 1)) {
            if (mOnOverScrollListener != null) {
                mOnOverScrollListener.onOverScrollBottom(t);
            }
        }

        Log.d("", "");
    }

    /**
     * Returns current vertical scroll position.
     *
     * @return current vertical scroll position
     */
    public int getCurrentVerticalScrollPosition() {
        return currentVerticalScrollPosition;
    }

    /**
     * Sets OnOverScrollListener to catch overscrolled events.
     *
     * @param mOnOverScrollListener OnOverScroolListener to set
     */
    public void setOnOverScrollListener(OnOverScrollListener mOnOverScrollListener) {
        this.mOnOverScrollListener = mOnOverScrollListener;
    }

    /**
     * This interface must be implemented to catch ScroolView overscrolled events.
     */
    public interface OnOverScrollListener {

        /**
         * This method is invoked when ScrollView is overscrolled on top.
         *
         * @param scrollPosition the scroll position of ScrollView
         */
        public void onOverScrollTop(int scrollPosition);

        /**
         * This method is invoked when ScrollView is overscrolled on bottom.
         *
         * @param scrollPosition the scroll position of ScrollView
         */
        public void onOverScrollBottom(int scrollPosition);
    }
}
