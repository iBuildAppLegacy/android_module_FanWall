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
import android.view.MotionEvent;
import android.webkit.WebView;

/**
 * ImageView that can detect that overscrolled left or right.
 */
public class ImagesWebView extends WebView {

    private OnTouchListener listener;
    private boolean overScrollLeft = false;
    private boolean overScrollRight = false;

    public ImagesWebView(Context ctx) {
        super(ctx);
    }

    public ImagesWebView(Context ctx, AttributeSet as) {
        super(ctx, as);
    }

    public ImagesWebView(Context ctx, AttributeSet as, int i) {
        super(ctx, as, i);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int scrollBarWidth = getVerticalScrollbarWidth();

        // width of the view depending of you set in the layout
        int viewWidth = computeHorizontalScrollExtent();

        // width of the webpage depending of the zoom
        int innerWidth = computeHorizontalScrollRange();

        // position of the left side of the horizontal scrollbar
        int scrollBarLeftPos = computeHorizontalScrollOffset();

        // position of the right side of the horizontal scrollbar, the width of scroll is the width of view minus the width of vertical scrollbar
        int scrollBarRightPos = scrollBarLeftPos + viewWidth - scrollBarWidth;

        // if left pos of scroll bar is 0 left over scrolling is true
        if (scrollBarLeftPos == 0) {
            overScrollLeft = true;
        } else {
            overScrollLeft = false;
        }

        // if right pos of scroll bar is superior to webpage width: right over scrolling is true
        if (scrollBarRightPos >= innerWidth - 50) {
            overScrollRight = true;
        } else {
            overScrollRight = false;
        }
        try {
            boolean res = super.onTouchEvent(ev);

            this.setOnTouchListener(listener);
            this.listener.onTouch(this, ev);

            return res;
        }
        catch(Throwable e){
           Log.e("onTouch","exception",e);
            return false;
        }
    }

    /**
     * Sets the OnTouchListener instance.
     *
     * @param listener the listener to set
     */
    public void setListener(OnTouchListener listener) {
        this.listener = listener;
    }

    /**
     * Checks if this WebView is overscrolled from left.
     *
     * @return true if is overscrolled
     */
    public boolean isOverScrollLeft() {
        return overScrollLeft;
    }

    /**
     * Checks if this WebView is overscrolled from right.
     *
     * @return true if is overscrolled
     */
    public boolean isOverScrollRight() {
        return overScrollRight;
    }
}
