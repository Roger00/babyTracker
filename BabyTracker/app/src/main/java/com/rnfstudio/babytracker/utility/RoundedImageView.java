package com.rnfstudio.babytracker.utility;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.rnfstudio.babytracker.R;

/**
 * source: http://stackoverflow.com/questions/16208365/create-a-circular-image-view-in-android
 */
public class RoundedImageView extends ImageView {
    private boolean mIsCircle = false;

    public RoundedImageView(Context context) {
        super(context);
        init(context, null);
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(final Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.RoundedImageView,
                    0, 0);
            try {
                mIsCircle = a.getBoolean(R.styleable.RoundedImageView_isCircle, false);
            } finally {
                a.recycle();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();

        if (drawable == null) {
            return;
        }

        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        Bitmap b =  ((BitmapDrawable)drawable).getBitmap() ;
        if (b == null) {
            return;
        }

        Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);

        int w = getWidth();

        Bitmap roundBitmap =  getCroppedBitmap(bitmap, w);
        canvas.drawBitmap(roundBitmap, 0, 0, null);
    }

    public Bitmap getCroppedBitmap(Bitmap bmp, int radius) {
        Bitmap sbmp;
        if(bmp.getWidth() != radius || bmp.getHeight() != radius)
            sbmp = Bitmap.createScaledBitmap(bmp, radius, radius, false);
        else
            sbmp = bmp;
        Bitmap output = Bitmap.createBitmap(sbmp.getWidth(),
                sbmp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        int bw = 10; // border width
        final Rect innerRect = new Rect(bw, bw, sbmp.getWidth()-bw, sbmp.getHeight()-bw);
        final Rect outerRect = new Rect(0, 0, sbmp.getWidth(), sbmp.getHeight());

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);

        if (mIsCircle) {
            paint.setColor(Color.parseColor("#BAB399"));
            canvas.drawCircle(sbmp.getWidth() / 2 + 0.7f, sbmp.getHeight() / 2 + 0.7f,
                    sbmp.getWidth() / 2 + 0.1f, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(sbmp, innerRect, innerRect, paint);
        } else {
            // draw image
            paint.setColor(Color.parseColor("#BAB399"));
            canvas.drawPath(RoundedRect(innerRect, 50.0f - bw, 50.0f - bw), paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(sbmp, innerRect, innerRect, paint);

            // draw border
            paint.setColor(Color.WHITE);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
            canvas.drawPath(RoundedRect(outerRect, 50.0f, 50.0f), paint);
        }

        return output;
    }

    static public Path RoundedRect(Rect rect, float rx, float ry) {
        return RoundedRect(rect.left, rect.top, rect.right, rect.bottom, rx, ry);
    }

    /**
     * Tartarize's answer from:
     *
     * http://stackoverflow.com/questions/5896234/
     * how-to-use-android-canvas-to-draw-a-roundrect-with-only-topleft-and-topright-cor
     *
     * This helps us draw rounded rectangles under API level 21
     *
     * For 21 and above, we can use Canvas.drawRoundRect()
     */
    static public Path RoundedRect(float left, float top, float right, float bottom,
                                   float rx, float ry) {
        Path path = new Path();
        if (rx < 0) rx = 0;
        if (ry < 0) ry = 0;
        float width = right - left;
        float height = bottom - top;
        if (rx > width/2) rx = width/2;
        if (ry > height/2) ry = height/2;
            float widthMinusCorners = (width - (2 * rx));
        float heightMinusCorners = (height - (2 * ry));

        path.moveTo(right, top + ry);
        path.rQuadTo(0, -ry, -rx, -ry);//top-right corner
        path.rLineTo(-widthMinusCorners, 0);
        path.rQuadTo(-rx, 0, -rx, ry); //top-left corner
        path.rLineTo(0, heightMinusCorners);
        path.rQuadTo(0, ry, rx, ry);//bottom-left corner
        path.rLineTo(widthMinusCorners, 0);
        path.rQuadTo(rx, 0, rx, -ry); //bottom-right corner

        path.rLineTo(0, -heightMinusCorners);

        path.close();//Given close, last lineto can be removed.

        return path;
    }
}