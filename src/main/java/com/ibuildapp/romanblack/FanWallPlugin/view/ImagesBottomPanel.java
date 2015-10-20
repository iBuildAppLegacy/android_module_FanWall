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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * This class represents LinearLayout with custom background.
 */
public class ImagesBottomPanel extends RelativeLayout {

    private Paint innerPaint, borderPaint;

    public ImagesBottomPanel(Context context) {
        super(context);
        init();
    }

    public ImagesBottomPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Creates paints to draw custom background.
     */
    private void init() {
        innerPaint = new Paint();
        innerPaint.setARGB(225, 75, 75, 75);
        innerPaint.setAntiAlias(true);

        borderPaint = new Paint();
        borderPaint.setARGB(255, 255, 255, 255);
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Style.STROKE);
        borderPaint.setStrokeWidth(2);
    }

    /**
     * Sets the custom background inner paint.
     *
     * @param innerPaint
     */
    public void setInnerPaint(Paint innerPaint) {
        this.innerPaint = innerPaint;
    }

    /**
     * Sets the custom background border paint.
     *
     * @param borderPaint
     */
    public void setBorderPaint(Paint borderPaint) {
        this.borderPaint = borderPaint;
    }

    /**
     * Draws LinearLayout with custom background.
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        RectF drawRect = new RectF();
        drawRect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());

        canvas.drawRoundRect(drawRect, 5, 5, innerPaint);
        canvas.drawRoundRect(drawRect, 5, 5, borderPaint);

        super.dispatchDraw(canvas);
    }
}
