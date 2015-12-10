package com.rnfstudio.babytracker.utility;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Modified from example:
 * http://stackoverflow.com/questions/29381474/how-to-draw-a-circle-with-animation-in-android
 */
public class CircleView extends View {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------
    private static final int START_ANGLE_POINT = 0;

    private final Paint paint;
    private final RectF rect;

    private float angle;
    private List<Pair<Float, Float>> mDataPairs;

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);

        final int strokeWidth = 30;

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        //Circle color
        paint.setColor(Color.RED);

        //size 200x200 example
        rect = new RectF(strokeWidth, strokeWidth, 300 + strokeWidth, 300 + strokeWidth);

        //Initial Angle (optional, it can be zero)
        angle = START_ANGLE_POINT;

        initData();
    }

    private void initData() {
        mDataPairs = new ArrayList<>();
        mDataPairs.add(new Pair<>(new Float(0), new Float(20)));
        mDataPairs.add(new Pair<>(new Float(40), new Float(60)));
        mDataPairs.add(new Pair<>(new Float(80), new Float(100)));
        mDataPairs.add(new Pair<>(new Float(180), new Float(190)));
        mDataPairs.add(new Pair<>(new Float(240), new Float(270)));
        mDataPairs.add(new Pair<>(new Float(299), new Float(350)));
    }

    public void setData(List<Pair<Float, Float>> data) {
        mDataPairs = data;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // draw underlying circle
        paint.setColor(Color.GRAY);
        canvas.drawArc(rect, START_ANGLE_POINT, 360, false, paint);

        // draw data
        paint.setColor(Color.RED);
        for (Pair<Float, Float> p : mDataPairs) {
            if (p.first > angle) break;

            float startAngle = p.first;
            float endAngle = p.second > angle ? angle : p.second;
            float sweepAngle = endAngle - startAngle;

            canvas.drawArc(rect, startAngle, sweepAngle, false, paint);
        }
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }
}
