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
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;


/**
 * View that shows touch events and their history. This view demonstrates the
 * use of {@link #onTouchEvent(android.view.MotionEvent)} and {@link android.view.MotionEvent}s to keep
 * track of touch pointers across events.
 */
public class TouchDisplayView extends View {

    // Is there an active touch?
    private boolean mHasTouch = false;

    static class Point {
        public float x, y;
        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    static class Stroke {
        public ArrayList<Point> points;
        public Stroke() {
            points = new ArrayList<>();
        }
        public void add(Point p) { points.add(p); }
        public void clear() {
            points.clear();
        }

    }

    private ArrayList<Stroke> strokes = new ArrayList<>();
    private Stroke cur_stroke = new Stroke();

    public TouchDisplayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initialisePaint();
    }

    // BEGIN_INCLUDE(onTouchEvent)
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        final int action = event.getAction();

        /*
         * Switch on the action. The action is extracted from the event by
         * applying the MotionEvent.ACTION_MASK. Alternatively a call to
         * event.getActionMasked() would yield in the action as well.
         */
        switch (action & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN: {
                // first pressed gesture has started

                int id = event.getPointerId(0);
                if (id == 0) {
                    cur_stroke.clear();
                    cur_stroke.add(new Point(event.getX(0), event.getY(0)));
                }

                mHasTouch = true;

                break;
            }

            case MotionEvent.ACTION_POINTER_DOWN: {
                /*
                 * A non-primary pointer has gone down, after an event for the
                 * primary pointer (ACTION_DOWN) has already been received.
                 */

                /*
                 * The MotionEvent object contains multiple pointers. Need to
                 * extract the index at which the data for this particular event
                 * is stored.
                 */
                int index = event.getActionIndex();
                int id = event.getPointerId(index);

                //event.getX(index), event.getY(index));

                break;
            }

            case MotionEvent.ACTION_UP: {
                /*
                 * Final pointer has gone up and has ended the last pressed
                 * gesture.
                 */

                if (cur_stroke.points.size() > 2) {
                    strokes.add(cur_stroke);
                    cur_stroke = new Stroke();
                }

                mHasTouch = false;

                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                /*
                 * A non-primary pointer has gone up and other pointers are
                 * still active.
                 */

                /*
                 * The MotionEvent object contains multiple pointers. Need to
                 * extract the index at which the data for this particular event
                 * is stored.
                 */
                int index = event.getActionIndex();
                int id = event.getPointerId(index);

                break;
            }

            case MotionEvent.ACTION_MOVE: {
                /*
                 * A change event happened during a pressed gesture. (Between
                 * ACTION_DOWN and ACTION_UP or ACTION_POINTER_DOWN and
                 * ACTION_POINTER_UP)
                 */

                /*
                 * Loop through all active pointers contained within this event.
                 * Data for each pointer is stored in a MotionEvent at an index
                 * (starting from 0 up to the number of active pointers). This
                 * loop goes through each of these active pointers, extracts its
                 * data (position and pressure) and updates its stored data. A
                 * pointer is identified by its pointer number which stays
                 * constant across touch events as long as it remains active.
                 * This identifier is used to keep track of a pointer across
                 * events.
                 */
                for (int index = 0; index < event.getPointerCount(); index++) {
                    // get pointer id for data stored at this index
                    int id = event.getPointerId(index);

                    if (id == 0)
                        cur_stroke.add(new Point(event.getX(index), event.getY(index)));

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
        canvas.drawRect(mBorderWidth, mBorderWidth, getWidth() - mBorderWidth, getHeight()
                - mBorderWidth, mBorderPaint);

        for (Stroke s : strokes)
            drawStroke(canvas, s, strokePaint);
        drawStroke(canvas, cur_stroke, curStrokePaint);

        canvas.drawText("" + strokes.size() + " strokes", 10f, 30f, mTextPaint);
    }

    /*
     * Below are only helper methods and variables required for drawing.
     */


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

    /*protected void drawCircle(Canvas canvas, int id, TouchHistory data) {
        // select the color based on the id
        int color = COLORS[id % COLORS.length];
        mCirclePaint.setColor(color);

        float radius = mCircleRadius;

        canvas.drawCircle(data.x, (data.y) - (radius / 2f), radius,
                mCirclePaint);

        // draw its label next to the main circle
        canvas.drawText(data.label, data.x + radius, data.y
                - radius, mTextPaint);
    }*/

    protected void drawStroke(Canvas canvas, Stroke s, Paint paint) {

        Path path = new Path();
        if (s.points.size() > 0)
            path.moveTo(s.points.get(0).x, s.points.get(0).y);
        for (Point p : s.points)
            path.lineTo(p.x, p.y);

        canvas.drawPath(path, paint);
    }

    public void clear() {
        strokes.clear();
        this.invalidate();
    }

}
