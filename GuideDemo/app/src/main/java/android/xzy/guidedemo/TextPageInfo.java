package android.xzy.guidedemo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Log;

/**
 * Created by zhengyangxu on 14-10-8.
 */
public class TextPageInfo extends PageInfo {

    private String mTitle;

    private String mSummary;

    private Matrix mMatrix;

    private PointF mTitleLocation;

    private PointF mSummaryLocation;

    private Paint mTitlePaint;

    private Paint mSummaryPaint;

    private Paint mBitmapPaint;

    private PointF mTitleBeginLocation;

    private PointF mSummaryBeginLocation;

    private PointF mBitmapBeginLocation;

    private PointF mTitleEndLocation;

    private PointF mSummaryEndLocation;

    private static final float BITMAP_MAX_SCALE = 1f;

    private static final float BITMAP_MIN_SCALE = .5f;

    private static final int BITMAP_MAX_ALPHA = 255;

    private static final int BITMAP_MIN_ALPHA = 20;


    public TextPageInfo() {
        init();
    }

    private void init() {
        mMatrix = new Matrix();

    }

    private void initPaint() {
        if (mTitlePaint == null) {
            mTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }
        mTitlePaint.setTextSize(mHeight * .045f);
        mTitlePaint.setColor(Color.WHITE);
        mTitlePaint.setTextAlign(Paint.Align.CENTER);

        if (mSummaryPaint == null) {
            mSummaryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }
        mSummaryPaint.setTextSize(mHeight * .030f);
        mSummaryPaint.setColor(Color.WHITE);
        mSummaryPaint.setTextAlign(Paint.Align.CENTER);

        if (mBitmapPaint == null) {
            mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }
        mBitmapPaint.setAlpha(0);
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setSummary(String summary) {
        mSummary = summary;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawText(mTitle, mTitleLocation.x, mTitleLocation.y, mTitlePaint);
        canvas.drawText(mSummary, mSummaryLocation.x, mSummaryLocation.y, mSummaryPaint);

        canvas.drawBitmap(mBitmap, mMatrix, mBitmapPaint);
    }

    /**
     * delta : 0f to 2.0f ; 0-1渐进 1-2渐出
     * 根据delta做动画
     */
    @Override
    void update(float delta) {
        super.update(delta);

        Log.i("xzy", "Two Text delta is : " + delta + " mLastDelta is : " + mLastDelta);

        //TODO 放入TouchLayer中做
        if (Math.abs(delta - mLastDelta) > .5f) {
            mLastDelta = delta;
            return;
        }

        if (delta > 1) {
            updateBitmaps(delta - mLastDelta);
            updateTexts(delta - mLastDelta);
        } else {
            updateBitmaps(mLastDelta - delta);
            updateTexts(mLastDelta - delta);
        }
        mLastDelta = delta;
    }

    private void updateBitmaps(float value) {
        float[] center = {mBitmap.getWidth() / 2, mBitmap.getHeight() / 2};
        mMatrix.mapPoints(center);
        float[] values = new float[9];
        mMatrix.getValues(values);
        float scale = values[Matrix.MSCALE_X]
                - (BITMAP_MAX_SCALE - BITMAP_MIN_SCALE) * value;
        scale = values[Matrix.MSCALE_X] / scale;

        Log.i("xzy", "scale x is : " + values[Matrix.MSCALE_X] + "  scale is : " + scale);
        if (values[Matrix.MSCALE_X] <= BITMAP_MIN_SCALE && scale <= 1) {
            scale = BITMAP_MIN_SCALE / values[Matrix.MSCALE_X];
        }
        if (values[Matrix.MSCALE_X] >= BITMAP_MAX_SCALE && scale >= 1) {
            scale = BITMAP_MAX_SCALE / values[Matrix.MSCALE_X];
        }
        Log.i("xzy", "scale is : " + scale);
        mMatrix.postTranslate(-center[0], -center[1]);
        mMatrix.postScale(scale, scale);
        mMatrix.postTranslate(center[0], center[1]);

        int alpha = mBitmapPaint.getAlpha() + (int) (
                (BITMAP_MAX_ALPHA - BITMAP_MIN_ALPHA) * value);

        if (alpha > BITMAP_MAX_ALPHA) {
            alpha = BITMAP_MAX_ALPHA;
        } else if (alpha < BITMAP_MIN_ALPHA) {
            alpha = BITMAP_MIN_ALPHA;
        }

        mBitmapPaint.setAlpha(alpha);
    }

    private void updateTexts(float value) {
        mTitleLocation.x += (int) ((mTitleEndLocation.x - mTitleBeginLocation.x) * value);
        mTitleLocation.y += (int) ((mTitleEndLocation.y - mTitleBeginLocation.y) * value);
        if (mTitleLocation.x > mTitleBeginLocation.x) {
            mTitleLocation.x = mTitleBeginLocation.x;
        }
        if (mTitleLocation.x < mTitleEndLocation.x) {
            mTitleLocation.x = mTitleEndLocation.x;
        }
        if (mTitleLocation.y > mTitleBeginLocation.y) {
            mTitleLocation.y = mTitleBeginLocation.y;
        }
        if (mTitleLocation.y < mTitleEndLocation.y) {
            mTitleLocation.y = mTitleEndLocation.y;
        }

        mSummaryLocation.x += (int) ((mSummaryEndLocation.x - mSummaryBeginLocation.x) * value);
        mSummaryLocation.y += (int) ((mSummaryEndLocation.y - mSummaryBeginLocation.y) * value);
        if (mSummaryLocation.x > mSummaryBeginLocation.x) {
            mSummaryLocation.x = mSummaryBeginLocation.x;
        }
        if (mSummaryLocation.x < mSummaryEndLocation.x) {
            mSummaryLocation.x = mSummaryEndLocation.x;
        }
        if (mSummaryLocation.y > mSummaryBeginLocation.y) {
            mSummaryLocation.y = mSummaryBeginLocation.y;
        }
        if (mSummaryLocation.y < mSummaryEndLocation.y) {
            mSummaryLocation.y = mSummaryEndLocation.y;
        }
    }



    @Override
    public void onSizeChaged(int w, int h, int oldw, int oldh) {
        super.onSizeChaged(w, h, oldw, oldh);

        mTitleBeginLocation = new PointF(mWidth * 1.5f, mHeight * .15f);
        mSummaryBeginLocation = new PointF(mWidth * 3f, mHeight * .21f);

        mTitleLocation = new PointF();
        mSummaryLocation = new PointF();

        mTitleLocation.set(mTitleBeginLocation);
        mSummaryLocation.set(mSummaryBeginLocation);

        mTitleEndLocation = new PointF(mWidth * .5f, mHeight * .15f);
        mSummaryEndLocation = new PointF(mWidth * .5f, mHeight * .21f);

//        mTitleBeginLocation = new PointF(mWidth * .5f, mHeight * .15f);
//        mSummaryBeginLocation = new PointF(mWidth * .5f, mHeight * .21f);

        mBitmapBeginLocation = new PointF((mWidth - mBitmap.getWidth()) * .5f, h * .33f);

        mMatrix.postTranslate(mBitmapBeginLocation.x, mBitmapBeginLocation.y);
        initPaint();
    }

    @Override
    public void setLastDelta(float lastDelta) {
        if (lastDelta == 0f) {
            if (mBitmapPaint == null) {
                mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            }
            mBitmapPaint.setAlpha(0);
        }
        super.setLastDelta(lastDelta);
    }
}
