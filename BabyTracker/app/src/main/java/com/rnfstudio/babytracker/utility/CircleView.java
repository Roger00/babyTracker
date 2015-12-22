package com.rnfstudio.babytracker.utility;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Vibrator;
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

    // the angle the drawing starts
    private static final int START_ANGLE_POINT = 270;
    private static float size = 0;
    private static float strokeWidth = 0;
    private static float strokeWidthNormal = 0;
    private static float strokeWidthTouched = 0;
    private static float innerRadius = 0;
    private static float outerRadius = 0;

    private static final int INDEX_NO_FOUND = -1;

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------
    private final Context mContext;
    private final Paint paint;
    private final RectF rect;
    private float angle;
    private Animation mAnimation;
    private ValueAnimator mStrokeWidthAnim;
    private boolean mCircleTouched = false;

    private List<Pair<Float, Float>> mDataPairs = new ArrayList<>();
    private List<OnCircleTouchListener> mListeners = new ArrayList<>();
    private int mHighlightIndex = INDEX_NO_FOUND;

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

        mContext = context;
        size = context.getResources().getDimension(R.dimen.circle_view_inner_size);
        strokeWidth = context.getResources().getDimension(R.dimen.circle_view_stroke_width);
        strokeWidthNormal = context.getResources().getDimension(R.dimen.circle_view_stroke_width);
        strokeWidthTouched = context.getResources().getDimension(R.dimen.circle_view_stroke_width_touched);
        innerRadius = getResources().getDimension(R.dimen.circle_view_touch_inner_radius);
        outerRadius = getResources().getDimension(R.dimen.circle_view_touch_outer_radius);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(Color.RED);
        rect = new RectF(strokeWidthTouched, strokeWidthTouched, size + strokeWidthTouched, size + strokeWidthTouched);
        angle = 0;

        initialize();
    }

    private void initialize() {
        this.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                // decide gesture
                float x = event.getX();
                float y = event.getY();
                float centerX = rect.centerX();
                float centerY = rect.centerY();
                float angle = DirectionUtils.getAngle(x, y, centerX, centerY);
                float dataAngle = (angle - START_ANGLE_POINT + 360) % 360;
                float distance = DirectionUtils.getDistance(x, y, centerX, centerY);
                boolean touched = distance >= innerRadius && distance <= outerRadius;

                if (action == MotionEvent.ACTION_UP ||
                        action == MotionEvent.ACTION_CANCEL) {

                    // cancel when previously touched
                    boolean prevState = getCircleTouchState();
                    if (prevState) {
                        startStrokeAnim(true);
                    }
                    setCircleTouchState(false);

                } else if (action == MotionEvent.ACTION_DOWN ||
                        action == MotionEvent.ACTION_MOVE) {

                    // update touch state
                    setCircleTouchState(touched);
                }

                if (touched) {
                    int dataIndex = searchDataIndex(dataAngle);

                    // highlight selected data
                    setHighlightedData(dataIndex);

                    // notify listeners the highlighted data
                    for (OnCircleTouchListener listener : mListeners) {
                        listener.onCircleTouch(dataIndex);
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
        mAnimation.setDuration(1200);

        // adapted from: https://gist.github.com/rogerpujol/99b3e8229b7a958d0930
        mStrokeWidthAnim = ValueAnimator.ofFloat(strokeWidthNormal, strokeWidthTouched);
        mStrokeWidthAnim.setDuration(200);
        mStrokeWidthAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                paint.setStrokeWidth(value.floatValue());
                //Do whatever you need to to with the value and...
                //Call invalidate if it's necessary to update the canvas
                CircleView.this.invalidate();
            }
        });
    }

    public void setData(List<Pair<Float, Float>> data) {
        mDataPairs = data;
        setAngle(0);
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
        int index = 0;
        for (Pair<Float, Float> p : mDataPairs) {
            if (p.first > angle) break;

            float startAngle = p.first;
            float endAngle = p.second > angle ? angle : p.second;
            float sweepAngle = endAngle - startAngle;

            paint.setColor(mHighlightIndex == index ? Color.MAGENTA : Color.RED);

            canvas.drawArc(rect, START_ANGLE_POINT + startAngle, sweepAngle, false, paint);
            index++;
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

    private void setHighlightedData(int index) {
        mHighlightIndex = index;
        requestLayout();
    }

    private void sendHapticFeedback() {
        Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(20);
    }

    public void setCircleTouchState(boolean touched) {
        boolean prevState = mCircleTouched;
        mCircleTouched = touched;

        boolean enter = !prevState && touched;
        boolean leave = prevState && !touched;

        if (enter) {
            sendHapticFeedback();
        }

        if (enter) {
            startStrokeAnim(false);
        } else if (leave) {
            startStrokeAnim(true);
        }
    }

    private boolean getCircleTouchState() {
        return mCircleTouched;
    }

    private void startStrokeAnim(boolean reverse) {
        if (mStrokeWidthAnim.isRunning()) {
            mStrokeWidthAnim.cancel();
        }

        if (reverse) {
            mStrokeWidthAnim.reverse();
        } else {
            mStrokeWidthAnim.start();
        }
    }

    private int searchDataIndex(float point) {
        if (mDataPairs == null || mDataPairs.size() == 0) {
            return INDEX_NO_FOUND;
        }

        for (int i = 0; i < mDataPairs.size(); i++) {
            Pair<Float, Float> p = mDataPairs.get(i);
            if (point >= p.first && point <= p.second) {
                return i;
            }
        }

        return INDEX_NO_FOUND;
    }
}
