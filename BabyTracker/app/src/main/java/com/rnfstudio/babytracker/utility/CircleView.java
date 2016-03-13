package com.rnfstudio.babytracker.utility;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
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
import com.rnfstudio.babytracker.SettingsActivity;

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
        protected void applyTransformation(float interpolatedTime,
                                           Transformation transformation) {
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
    private static float strokeWidthNormal = 0;
    private static float strokeWidthTouched = 0;
    private static float innerRadius = 0;
    private static float outerRadius = 0;

    private static final int INDEX_NO_FOUND = -1;

    private static final boolean ENLARGE_SMALL_DATA = true;
    private static final float ENLARGE_RANGE = 6;

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
    private final Paint paintCircle;
    private final Paint paintText;
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

        float size = context.getResources().getDimension(R.dimen.circle_view_inner_size);
        float strokeWidth = context.getResources().getDimension(R.dimen.circle_view_stroke_width);
        float textSize = context.getResources().getDimension(R.dimen.circle_view_text_size);

        mContext = context;
        strokeWidthNormal = context.getResources().getDimension(R.dimen.circle_view_stroke_width);
        strokeWidthTouched = context.getResources().getDimension(R.dimen.circle_view_stroke_width_touched);
        innerRadius = getResources().getDimension(R.dimen.circle_view_touch_inner_radius);
        outerRadius = getResources().getDimension(R.dimen.circle_view_touch_outer_radius);

        paintCircle = new Paint();
        paintCircle.setAntiAlias(true);
        paintCircle.setStyle(Paint.Style.STROKE);
        paintCircle.setStrokeWidth(strokeWidth);
        paintCircle.setColor(Color.RED);

        paintText = new Paint();
        paintText.setAntiAlias(true);
        paintText.setStyle(Paint.Style.STROKE);
        paintText.setTextSize(textSize);
        paintText.setColor(Color.GRAY);

        rect = new RectF(strokeWidthTouched,
                strokeWidthTouched,
                size + strokeWidthTouched,
                size + strokeWidthTouched);

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
                boolean isCancel = false;

                if (action == MotionEvent.ACTION_UP ||
                        action == MotionEvent.ACTION_CANCEL) {

                    // cancel when previously touched
                    boolean prevState = getCircleTouchState();
                    if (prevState) {
                        startStrokeAnim(true);
                    }
                    setCircleTouchState(false);

                    isCancel = true;

                } else if (action == MotionEvent.ACTION_DOWN ||
                        action == MotionEvent.ACTION_MOVE) {

                    // update touch state
                    setCircleTouchState(touched);

                    isCancel = false;
                }

                int dataIndex = (touched && !isCancel) ?
                        searchDataIndex(dataAngle) : INDEX_NO_FOUND;

                // highlight selected data
                setHighlightedData(dataIndex);

                // notify listeners the highlighted data
                notifyListeners();

                return true;
            }
        });

        // default data
        mDataPairs = new ArrayList<>();
        mDataPairs.add(new Pair<>((float) 0, (float) 20));
        mDataPairs.add(new Pair<>((float) 40, (float) 60));
        mDataPairs.add(new Pair<>((float) 80, (float) 100));
        mDataPairs.add(new Pair<>((float) 180, (float) 190));
        mDataPairs.add(new Pair<>((float) 240, (float) 270));
        mDataPairs.add(new Pair<>((float) 299, (float) 350));

        // animation
        mAnimation = new CircleAngleAnimation(this, 360);
        mAnimation.setDuration(1200);

        // adapted from: https://gist.github.com/rogerpujol/99b3e8229b7a958d0930
        mStrokeWidthAnim = ValueAnimator.ofFloat(strokeWidthNormal, strokeWidthTouched);
        mStrokeWidthAnim.setDuration(200);
        mStrokeWidthAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                paintCircle.setStrokeWidth(value);
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

        // draw clock text
        drawClockText(canvas);

        // draw underlying circle
        paintCircle.setColor(Color.GRAY);
        canvas.drawArc(rect, START_ANGLE_POINT, 360, false, paintCircle);

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

            // make arc no shorter than 5 degrees
            // so users can tap on it easily
            if (ENLARGE_SMALL_DATA && sweepAngle < ENLARGE_RANGE) {
                startAngle -= ENLARGE_RANGE / 2;
                sweepAngle += ENLARGE_RANGE;
            }

            paintCircle.setColor(mHighlightIndex == index ? Color.MAGENTA : Color.RED);

            canvas.drawArc(rect, START_ANGLE_POINT + startAngle, sweepAngle, false, paintCircle);
            index++;
        }
    }

    private void drawClockText(Canvas canvas) {
        Resources res = mContext.getResources();
        float size = res.getDimension(R.dimen.circle_view_inner_size);
        float strokeWidthTouched = res.getDimension(R.dimen.circle_view_stroke_width_touched);
        float radius = res.getDimension(R.dimen.circle_view_text_rect_radius);
        float textSize = res.getDimension(R.dimen.circle_view_text_size) / 2;

        float centerXY = (size + strokeWidthTouched * 2) / (float) 2;

        for (int i = 1; i < 13; i++) {
            int degree = i * 30 - 90;
            double angle = (((double) degree % 360) / 180) * Math.PI;
            float x = (float) Math.cos(angle) * radius;
            float y = (float) Math.sin(angle) * radius;
            canvas.drawText(String.valueOf(i),
                    centerXY + x - textSize / 2,
                    centerXY + y + textSize / 2,
                    paintText);
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

    private void notifyListeners() {
        for (OnCircleTouchListener listener : mListeners) {
            listener.onCircleTouch(mHighlightIndex);
        }
    }

    private void setHighlightedData(int index) {
        mHighlightIndex = index;
        requestLayout();
    }

    private void sendHapticFeedback() {
        if (SettingsActivity.isVibrateEnabled(getContext())) {
            Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(20);
        }
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

            float start = p.first, end = p.second;

            // search within a wider range if duration is too short
            if (ENLARGE_SMALL_DATA && (end - start) < ENLARGE_RANGE) {
                start -= ENLARGE_RANGE / 2;
                end += ENLARGE_RANGE / 2;
            }

            if (point >= start && point <= end) {
                return i;
            }
        }

        return INDEX_NO_FOUND;
    }
}
