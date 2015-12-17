package com.rnfstudio.babytracker.utility;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.rnfstudio.babytracker.R;

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
    class CircleAngleAnimation extends Animation {
        private CircleView circle;

        private float oldAngle;
        private float newAngle;

        public CircleAngleAnimation(CircleView circle, int newAngle) {
            this.oldAngle = circle.getAngle();
            this.newAngle = newAngle;
            this.circle = circle;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation transformation) {
            float angle = oldAngle + ((newAngle - oldAngle) * interpolatedTime);

            circle.setAngle(angle);
            circle.requestLayout();
        }
    }

    interface OnCircleTouchListener {
        void onCircleTouch(int index);
    }

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    public static final String TAG = "CircleView";

    private static final int START_ANGLE_POINT = 270;
    private static float size = 0;
    private static float strokeWidth = 0;
    private static float innerRadius = 0;
    private static float outerRadius = 0;

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------
    private final Paint paint;
    private final RectF rect;
    private float angle;
    private Animation mAnimation;

    private List<Pair<Float, Float>> mDataPairs = new ArrayList<>();
    private List<OnCircleTouchListener> mListeners = new ArrayList<>();

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------
    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);

        size = context.getResources().getDimension(R.dimen.circle_view_inner_size);
        strokeWidth = context.getResources().getDimension(R.dimen.circle_view_stroke_width);
        innerRadius = getResources().getDimension(R.dimen.circle_view_touch_inner_radius);
        outerRadius = getResources().getDimension(R.dimen.circle_view_touch_outer_radius);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(Color.RED);
        rect = new RectF(strokeWidth, strokeWidth, size + strokeWidth, size + strokeWidth);
        angle = START_ANGLE_POINT;

        initialize();
    }

    private void initialize() {
        this.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // decide gesture
                float x = event.getX();
                float y = event.getY();
                float centerX = rect.centerX();
                float centerY = rect.centerY();
                float angle = DirectionUtils.getAngle(x, y, centerX, centerY);
                float distance = DirectionUtils.getDistance(x, y, centerX, centerY);
                boolean touched = distance >= innerRadius && distance <= outerRadius;

                if (touched) {
                    for (OnCircleTouchListener listener : mListeners) {
                        listener.onCircleTouch((int) distance);
                    }
                }
                return true;
            }
        });

        // default data
        mDataPairs = new ArrayList<>();
        mDataPairs.add(new Pair<>(new Float(0), new Float(20)));
        mDataPairs.add(new Pair<>(new Float(40), new Float(60)));
        mDataPairs.add(new Pair<>(new Float(80), new Float(100)));
        mDataPairs.add(new Pair<>(new Float(180), new Float(190)));
        mDataPairs.add(new Pair<>(new Float(240), new Float(270)));
        mDataPairs.add(new Pair<>(new Float(299), new Float(350)));

        // animation
        mAnimation = new CircleAngleAnimation(this, 360);
        mAnimation.setDuration(2500);
    }

    public void setData(List<Pair<Float, Float>> data) {
        mDataPairs = data;
        startAnimation(mAnimation);
    }

    public void dumpData() {
        if (mDataPairs == null) {
            Log.d(TAG, "dumpData: data is null");
            return;
        }

        for (Pair<Float, Float> p : mDataPairs) {
            Log.d(TAG, "dumpData: start: " + p.first + ", end: " + p.second);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // draw underlying circle
        paint.setColor(Color.GRAY);
        canvas.drawArc(rect, START_ANGLE_POINT, 360, false, paint);

        if (mDataPairs == null) {
            return;
        }

        // draw data
        paint.setColor(Color.RED);
        for (Pair<Float, Float> p : mDataPairs) {
            if (p.first > angle) break;

            float startAngle = p.first;
            float endAngle = p.second > angle ? angle : p.second;
            float sweepAngle = endAngle - startAngle;

            canvas.drawArc(rect, START_ANGLE_POINT + startAngle, sweepAngle, false, paint);
        }
    }

    private float getAngle() {
        return angle;
    }

    private void setAngle(float angle) {
        this.angle = angle;
    }

    public void addListener(OnCircleTouchListener listener) {
        mListeners.add(listener);
    }

    public void removeListener(OnCircleTouchListener listener) {
        if (mListeners.contains(listener)) {
            mListeners.remove(listener);
        }
    }

    private void showHighlight(boolean animated) {
    }

    private void sendHapticFeedback() {

    }

    
}
