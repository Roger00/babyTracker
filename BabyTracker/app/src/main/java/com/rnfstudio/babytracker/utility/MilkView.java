package com.rnfstudio.babytracker.utility;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.View;

import com.rnfstudio.babytracker.R;

/**
 * Created by Roger on 2016/3/22.
 */
public class MilkView extends View {
    private final Paint mPaint;
    private final float rectSize;
    private final float amplitude;
    private final Path circlePath;
    private final ValueAnimator waveAnim;

    private float ampFactor;
    private float phase;
    private float offsetY;
    private float startVolume;
    private float endVolume;
    private Path wavePath;

    private Bitmap bottleBitmap;

    public MilkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(2);
        mPaint.setColor(Color.RED);

        rectSize = context.getResources().getDimension(R.dimen.milk_body_rect);
        amplitude = context.getResources().getDimension(R.dimen.milk_amplitude);

        // adapted from: https://gist.github.com/rogerpujol/99b3e8229b7a958d0930
        waveAnim = ValueAnimator.ofFloat(1.0f, 0.0f);
        waveAnim.setDuration(1000);
        waveAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();

                // animate amplitude, phase and y-offset
                ampFactor = value;
                phase += (float) (.08f * value * Math.PI);
                updateOffsetY(value);

                // draw again
                MilkView.this.invalidate();
            }
        });

        float radius = rectSize / 2;
        circlePath = new Path();
        circlePath.addCircle(radius, radius, radius, Path.Direction.CW);

        wavePath = new Path();

//        bottleBitmap = BitmapFactory.decodeResource(
//                context.getResources(), R.drawable.real_milk_bottle_cut_alpha);
    }

    private void updateOffsetY(float value) {
        float y_ratio = (endVolume - startVolume) * (1.0f - value) + startVolume;
        offsetY = (1.0f - y_ratio) * rectSize;
    }

    /**
     * For clipping, see <a href="http://ipjmc.iteye.com/blog/1299476">Clipping canvas</a>
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.clipPath(circlePath, Region.Op.REPLACE);
        drawWave(canvas);
    }

    private void drawWave(Canvas canvas) {
        wavePath.reset();
        wavePath.moveTo(0, rectSize);

        int NUM_WAVE_POINTS = 20;
        for (int i = 0; i <= NUM_WAVE_POINTS; i++) {
            float totalAngle = (float) (Math.PI);
            float angleFraction = totalAngle / NUM_WAVE_POINTS;
            float angle = i * angleFraction;
            float angle2 = angle - (angleFraction / 2);
            float x = angle * rectSize / totalAngle;
            float x2 = angle2 * rectSize / totalAngle;
            float y = (float) Math.cos(angle + phase) * amplitude * ampFactor + offsetY;
            float y2 = (float) Math.cos(angle2 + phase) * amplitude * ampFactor + offsetY;

            if (i == 0) {
                wavePath.lineTo(x, y);
            } else {
                wavePath.quadTo(x2, y2, x, y);
            }
        }
        wavePath.lineTo(rectSize, rectSize);
        wavePath.lineTo(0, rectSize);

        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(wavePath, mPaint);
    }

    public void startWaveAnim(boolean reverse) {
        if (waveAnim.isRunning()) {
            waveAnim.cancel();
        }

        if (reverse) {
            waveAnim.reverse();
        } else {
            waveAnim.start();
        }
    }

    public void setAmountStartEnd(float start, float end) {
        startVolume = start;
        endVolume = end;
        updateOffsetY(0.0f);
    }
}
