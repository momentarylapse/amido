/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.michi.amido;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


import java.util.ArrayList;


/**
 * View that shows touch events and their history. This view demonstrates the
 * use of {@link #onTouchEvent(android.view.MotionEvent)} and {@link android.view.MotionEvent}s to keep
 * track of touch pointers across events.
 */
public class CharacterView extends View {


    private ArrayList<CharacterDatabase.Stroke> strokes = new ArrayList<>();
    private CharacterDatabase.Stroke cur_stroke = new CharacterDatabase.Stroke();
    private CharacterDatabase.Character demo = null;

    boolean editable = true;

    public CharacterView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initialisePaint();
    }

    public void setDemo(CharacterDatabase.Character c)
    {
        demo = c;
        editable = false;

        clear();

        for (CharacterDatabase.StrokeDigest s : demo.strokes_digest){
            strokes.add(s.undigest());
        }

        this.postInvalidate();
    }

    public CharacterDatabase.Point event2point(MotionEvent event, int index)
    {
        return new CharacterDatabase.Point(event.getX(index) / (float)getWidth(), event.getY(index) / (float)getHeight());
    }

    // BEGIN_INCLUDE(onTouchEvent)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!editable)
            return true;

        final int action = event.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                int id = event.getPointerId(0);
                if (id == 0) {
                    cur_stroke.clear();
                    cur_stroke.add(event2point(event, 0));
                }
                break;
            }
            /*case MotionEvent.ACTION_POINTER_DOWN: {
                int index = event.getActionIndex();
                int id = event.getPointerId(index);
                //event.getX(index), event.getY(index));
                break;
            }*/
            case MotionEvent.ACTION_UP: {
                if (cur_stroke.points.size() > 2) {
                    strokes.add(cur_stroke);
                    cur_stroke = new CharacterDatabase.Stroke();
                }
                break;
            }
            /*case MotionEvent.ACTION_POINTER_UP: {
                int index = event.getActionIndex();
                int id = event.getPointerId(index);
                break;
            }*/

            case MotionEvent.ACTION_MOVE: {
                for (int index = 0; index < event.getPointerCount(); index++) {
                    int id = event.getPointerId(index);
                    if (id == 0)
                        cur_stroke.add(event2point(event, index));
                }
                break;
            }
        }

        // trigger redraw on UI thread
        this.postInvalidate();

        return true;
    }

    // END_INCLUDE(onTouchEvent)

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(BACKGROUND_ACTIVE);
        canvas.drawRect(mBorderWidth/2, mBorderWidth/2, getWidth() - mBorderWidth/2, getHeight()
                - mBorderWidth/2, mBorderPaint);

        for (CharacterDatabase.Stroke s : strokes)
            drawStroke(canvas, s, strokePaint);
        drawStroke(canvas, cur_stroke, curStrokePaint);

        canvas.drawText("" + strokes.size() + " strokes", 10f, 30f, mTextPaint);
    }


    private Paint mTextPaint = new Paint();
    private Paint strokePaint = new Paint();
    private Paint curStrokePaint = new Paint();

    private static final float STROKE_DP = 10f;
    private static final int STROKE_COLOR = 0xFF707070;
    private static final int CUR_STROKE_COLOR = 0xFFFF7070;

    private static final int BACKGROUND_ACTIVE = Color.WHITE;

    // inactive border
    private static final float INACTIVE_BORDER_DP = 3f;
    private static final int INACTIVE_BORDER_COLOR = 0xFFb0b0b0;
    private Paint mBorderPaint = new Paint();
    private float mBorderWidth;

    /**
     * Sets up the required {@link android.graphics.Paint} objects for the screen density of this
     * device.
     */
    private void initialisePaint() {

        // Calculate radiuses in px from dp based on screen density
        float density = getResources().getDisplayMetrics().density;

        // Setup text paint for circle label
        mTextPaint.setTextSize(27f);
        mTextPaint.setColor(Color.BLACK);

        // Setup paint for inactive border
        mBorderWidth = INACTIVE_BORDER_DP * density;
        mBorderPaint.setStrokeWidth(mBorderWidth);
        mBorderPaint.setColor(INACTIVE_BORDER_COLOR);
        mBorderPaint.setStyle(Paint.Style.STROKE);

        strokePaint.setStrokeWidth(STROKE_DP * density);
        strokePaint.setColor(STROKE_COLOR);
        strokePaint.setStyle(Paint.Style.STROKE);

        curStrokePaint.setStrokeWidth(STROKE_DP * density);
        curStrokePaint.setColor(CUR_STROKE_COLOR);
        curStrokePaint.setStyle(Paint.Style.STROKE);

    }

    protected void drawStroke(Canvas canvas, CharacterDatabase.Stroke s, Paint paint) {

        Path path = new Path();
        if (s.points.size() > 0)
            path.moveTo(s.points.get(0).x * getWidth(), s.points.get(0).y * getHeight());
        for (CharacterDatabase.Point p : s.points)
            path.lineTo(p.x * getWidth(), p.y * getHeight());

        canvas.drawPath(path, paint);
    }

    public void clear() {
        strokes.clear();
        this.invalidate();
    }

    public CharacterDatabase.Character getDigest()
    {
        return CharacterDatabase.digest(strokes);
    }


    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
    {
        final int width = getDefaultSize(getSuggestedMinimumWidth(),widthMeasureSpec);
        setMeasuredDimension(width, width);
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh)
    {
        super.onSizeChanged(w, w, oldw, oldh);
    }

}
