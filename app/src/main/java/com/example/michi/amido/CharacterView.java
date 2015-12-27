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
import android.widget.Toast;


import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.abs;
import static java.lang.Math.min;


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
    boolean animating = false;
    Timer animationTimer = null;
    float animationTime;

    public CharacterView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initialisePaint();
    }

    public void close() {
        if (animationTimer != null)
            animationTimer.cancel();
        animationTimer = null;
        animating = false;
    }

    public void setDemo(CharacterDatabase.Character c) {
        demo = c;
        editable = false;

        clear();

        for (CharacterDatabase.StrokeDigest s : demo.strokes_digest) {
            strokes.add(s.undigest());
        }

        postInvalidate();


        animationTime = 0;
        if (!animating) {
            animating = true;
            animationTimer = new Timer();
            TimerTask tt = new TimerTask() {
                @Override
                public void run() {
                    CharacterView.this.animationTime += 0.02f;
                    if (CharacterView.this.animationTime > CharacterView.this.strokes.size() + 2)
                        CharacterView.this.animationTime = 0;
                    CharacterView.this.postInvalidate();
                }
            };
            animationTimer.schedule(tt, 0, 20);
        }
    }

    public CharacterDatabase.Point event2point(MotionEvent event, int index) {
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


    private Paint textPaint = new Paint();
    private Paint strokePaint = new Paint();
    private Paint strokePaintBack = new Paint();
    private Paint curStrokePaint = new Paint();

    private static final float STROKE_DP = 10f;
    private static final int STROKE_COLOR = 0xFF707070;
    private static final int STROKE_COLOR_BACK = 0xFFa0a0a0;
    private static final int CUR_STROKE_COLOR = 0xFFFF7070;

    private static final int BACKGROUND_ACTIVE = Color.WHITE;

    private static final float BORDER_DP = 2f;
    private static final int BORDER_COLOR = 0xFFb0b0b0;
    private Paint borderPaint = new Paint();
    private float borderWidth;
    private float fontSize;

    /**
     * Sets up the required {@link android.graphics.Paint} objects for the screen density of this
     * device.
     */
    private void initialisePaint() {

        // Calculate radiuses in px from dp based on screen density
        float density = getResources().getDisplayMetrics().density;

        fontSize = 15f * density;
        textPaint.setTextSize(fontSize);
        textPaint.setColor(Color.BLACK);

        borderWidth = BORDER_DP * density;
        borderPaint.setStrokeWidth(borderWidth);
        borderPaint.setColor(BORDER_COLOR);
        borderPaint.setStyle(Paint.Style.STROKE);

        strokePaint.setStrokeWidth(STROKE_DP * density);
        strokePaint.setColor(STROKE_COLOR);
        strokePaint.setStyle(Paint.Style.STROKE);

        strokePaintBack.setStrokeWidth(STROKE_DP * density * 0.7f);
        strokePaintBack.setColor(STROKE_COLOR_BACK);
        strokePaintBack.setStyle(Paint.Style.STROKE);

        curStrokePaint.setStrokeWidth(STROKE_DP * density);
        curStrokePaint.setColor(CUR_STROKE_COLOR);
        curStrokePaint.setStyle(Paint.Style.STROKE);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(BACKGROUND_ACTIVE);
        canvas.drawRect(borderWidth / 2, borderWidth / 2, getWidth() - borderWidth / 2, getHeight() - borderWidth / 2, borderPaint);

        if (animating) {
            int cur = (int)animationTime;
            float t = min((animationTime - cur) * 1.5f, 1);
            for (int i=cur; i<strokes.size(); i++)
                drawStroke(canvas, strokes.get(i), strokePaintBack);
            for (int i=0; i<min(cur, strokes.size()); i++)
                drawStroke(canvas, strokes.get(i), strokePaint);
            if (cur < strokes.size())
                drawStrokePartial(canvas, strokes.get(cur), t, curStrokePaint);

        } else {
            for (CharacterDatabase.Stroke s : strokes)
                drawStroke(canvas, s, strokePaint);
            drawStroke(canvas, cur_stroke, curStrokePaint);
        }

        String s = String.format(getResources().getString(R.string.character_view_strokes_count), strokes.size());
        canvas.drawText(s, 10f, fontSize, textPaint);
    }

    protected void drawStroke(Canvas canvas, CharacterDatabase.Stroke s, Paint paint) {

        Path path = new Path();
        if (s.points.size() > 0)
            path.moveTo(s.points.get(0).x * getWidth(), s.points.get(0).y * getHeight());
        for (CharacterDatabase.Point p : s.points)
            path.lineTo(p.x * getWidth(), p.y * getHeight());

        canvas.drawPath(path, paint);
    }

    protected void drawStrokePartial(Canvas canvas, CharacterDatabase.Stroke s, float t, Paint paint) {

        Path path = new Path();
        if (s.points.size() > 0)
            path.moveTo(s.points.get(0).x * getWidth(), s.points.get(0).y * getHeight());
        float strokeLength = s.getLength();
        float pathLength = 0;
        CharacterDatabase.Point last = s.points.get(0);
        for (CharacterDatabase.Point p : s.points) {
            float dl = p.distance(last);
            if (pathLength > t * strokeLength) {
                CharacterDatabase.Point pp = last.interpolateTo(p, (t * strokeLength - pathLength) / dl);
                path.lineTo(pp.x * getWidth(), pp.y * getHeight());
                break;
            }
            pathLength += dl;
            path.lineTo(p.x * getWidth(), p.y * getHeight());
            last = p;
        }

        canvas.drawPath(path, paint);
    }

    public void clear() {
        strokes.clear();
        this.invalidate();
    }

    public CharacterDatabase.Character getDigest() {
        return CharacterDatabase.digest(strokes);
    }


    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        int size = min(width, height);
        if (this.getRootView().getHeight() > 0)
            size = min(size, this.getRootView().getHeight() / 2);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        int size = min(w, h);
        //size = min(size, this.getRootView().getHeight() / 2);
        super.onSizeChanged(size, size, oldw, oldh);
    }

}
