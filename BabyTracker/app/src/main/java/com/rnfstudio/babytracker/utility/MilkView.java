package com.rnfstudio.babytracker.utility;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.rnfstudio.babytracker.R;

/**
 * Created by Roger on 2016/3/22.
 */
public class MilkView extends View {
    private final Paint mPaint;
    private final RectF rect;
    private final float rectSize;
    private final float amplitude;
    private float mAmplitudeRatio;
    private float phase;
    private float mY_offset;
    private float mAmountStart;
    private float mAmountEnd;
    private final ValueAnimator mMilkAnim;
    private Bitmap bottleBitmap;

    public MilkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2);
        mPaint.setColor(Color.RED);

        rectSize = context.getResources().getDimension(R.dimen.milk_body_rect);
        amplitude = context.getResources().getDimension(R.dimen.milk_amplitude);
        rect = new RectF(0, 0, rectSize, rectSize);

        // adapted from: https://gist.github.com/rogerpujol/99b3e8229b7a958d0930
        mMilkAnim = ValueAnimator.ofFloat(1.0f, 0.0f);
        mMilkAnim.setDuration(1000);
        mMilkAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                mAmplitudeRatio = value;
                phase += (float) (.08f * value * Math.PI);

                updateYOffset(value);

                //Do whatever you need to to with the value and...
                //Call invalidate if it's necessary to update the canvas
                MilkView.this.invalidate();
            }
        });

        bottleBitmap = BitmapFactory.decodeResource(
                context.getResources(), R.drawable.real_milk_bottle_cut_alpha);
    }

    private void updateYOffset(float value) {
        float y_ratio = (mAmountEnd - mAmountStart) * (1.0f - value) + mAmountStart;
        mY_offset = (1.0f - y_ratio) * rectSize;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // draw circle
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(rect, 0, 360, false, mPaint);

        Path p = new Path();
        p.moveTo(0, rectSize);

        float x_offset = 0;
        int nPoints = 20;

        for (int i = 0; i <= nPoints; i++) {
            float totalAngle = (float) (Math.PI);
            float angleFraction = totalAngle / nPoints;
            float angle = i * angleFraction;
            float angle2 = angle - (angleFraction / 2);
            float x = angle * rectSize / totalAngle + x_offset;
            float x2 = angle2 * rectSize / totalAngle + x_offset;
            float y = (float) Math.cos(angle + phase) * amplitude * mAmplitudeRatio + mY_offset;
            float y2 = (float) Math.cos(angle2 + phase) * amplitude * mAmplitudeRatio + mY_offset;

            if (i == 0) {
                p.lineTo(x, y);
            } else {
                p.quadTo(x2, y2, x, y);
            }
        }
        p.lineTo(rectSize, rectSize);
        p.lineTo(0, rectSize);

        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(p, mPaint);

//        canvas.drawBitmap(bottleBitmap, 0, 0, null);
    }

    public void startAnim(boolean reverse) {
        if (mMilkAnim.isRunning()) {
            mMilkAnim.cancel();
        }

        if (reverse) {
            mMilkAnim.reverse();
        } else {
            mMilkAnim.start();
        }
    }

    public void setAmountStartEnd(float start, float end) {
        mAmountStart = start;
        mAmountEnd = end;
        updateYOffset(0.0f);
    }
}
