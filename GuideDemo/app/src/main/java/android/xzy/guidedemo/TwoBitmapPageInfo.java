package android.xzy.guidedemo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

/**
 * Created by zhengyangxu on 14-10-8.
 */
public class TwoBitmapPageInfo extends PageInfo {

    private Bitmap mSecondBitmap;

    private Matrix mFirstMatrix;

    private Matrix mSecondMatrix;

    private float mFirstBeginX;

    private float mFirstBeginY;

    private float mSecondBeginX;

    private float mSecondBeginY;

    private static final float FIRST_MIN_TRANS_X = 0;

    private static float FIRST_MAX_TRANS_X;

    private static final float FIRST_MAX_SCALE = 1f;

    private static final float FIRST_MIN_SCALE = .57f;

    private static final float SECOND_MAX_SCALE = 1f;

    private static final float SECOND_MIN_SCALE = .75f;

    private static final int SECOND_MAX_ALPHA = 255;

    private static final int SECOND_MIN_ALPHA = 122;

    private Paint mSecondBitmapPaint;

    public TwoBitmapPageInfo() {
        mFirstMatrix = new Matrix();
        mSecondMatrix = new Matrix();
        mSecondBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public void setSecondBitmap(Bitmap bitmap) {
        mSecondBitmap = bitmap;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, mFirstMatrix, null);
        canvas.drawBitmap(mSecondBitmap, mSecondMatrix, mSecondBitmapPaint);
    }

    /**
     * delta : 0f to 2.0f ; 0-1渐进 1-2渐出
     * 根据delta做动画
     */
    @Override
    void update(float delta) {
        Log.i("xzy", "--->delta is : " + delta);
        super.update(delta);
        if (delta > 1) {
            updateBitmaps(mLastDelta - delta);
        } else {
            updateBitmaps(delta - mLastDelta);
        }
        mLastDelta = delta;
    }

    private void updateBitmaps(float value) {
        mFirstMatrix.postTranslate(mFirstBeginX * 2 * value,
                mFirstBeginY * 1.1f * value);

        float[] firstCenter = {mBitmap.getWidth() / 2, mBitmap.getHeight() / 2};
        mFirstMatrix.mapPoints(firstCenter);

        //矫正
        float[] firstValues = new float[9];
        mFirstMatrix.getValues(firstValues);
        float firstScale = firstValues[Matrix.MSCALE_X]
                - (FIRST_MAX_SCALE - FIRST_MIN_SCALE) * value;
        firstScale = firstValues[Matrix.MSCALE_X] / firstScale;
        if (firstValues[Matrix.MSCALE_X] < FIRST_MIN_SCALE && firstScale <= 1) {
            firstScale = FIRST_MIN_SCALE / firstValues[Matrix.MSCALE_X];
        }
        if (firstValues[Matrix.MSCALE_X] > FIRST_MAX_SCALE && firstScale >= 1) {
            firstScale = FIRST_MAX_SCALE / firstValues[Matrix.MSCALE_X];
        }

        mFirstMatrix.postTranslate(-firstCenter[0], -firstCenter[1]);
        mFirstMatrix.postScale(firstScale, firstScale);
        mFirstMatrix.postTranslate(firstCenter[0], firstCenter[1]);

        //second bitmap
        float[] secondCenter = {mSecondBitmap.getWidth() / 2, mSecondBitmap.getHeight() / 2};
        mSecondMatrix.mapPoints(secondCenter);
        float[] secondValues = new float[9];
        mSecondMatrix.getValues(secondValues);
        float secondScale = secondValues[Matrix.MSCALE_X]
                - (SECOND_MAX_SCALE - SECOND_MIN_SCALE) * value;
        secondScale = secondValues[Matrix.MSCALE_X] / secondScale;

        if (secondValues[Matrix.MSCALE_X] <= SECOND_MIN_SCALE && secondScale <= 1) {
            secondScale = SECOND_MIN_SCALE / secondValues[Matrix.MSCALE_X];
        }
        if (secondValues[Matrix.MSCALE_X] >= SECOND_MAX_SCALE && secondScale >= 1) {
            secondScale = SECOND_MAX_SCALE / secondValues[Matrix.MSCALE_X];
        }

        mSecondMatrix.postTranslate(-secondCenter[0], -secondCenter[1]);
        mSecondMatrix.postScale(secondScale, secondScale);
        mSecondMatrix.postTranslate(secondCenter[0], secondCenter[1]);

        int alpha = mSecondBitmapPaint.getAlpha() + (int) (
                (SECOND_MAX_ALPHA - SECOND_MIN_ALPHA) * value);

        if (alpha > SECOND_MAX_ALPHA) {
            alpha = SECOND_MAX_ALPHA;
        } else if (alpha < SECOND_MIN_ALPHA) {
            alpha = SECOND_MIN_ALPHA;
        }

        mSecondBitmapPaint.setAlpha(alpha);
    }

    @Override
    public void onSizeChaged(int w, int h, int oldw, int oldh) {
        super.onSizeChaged(w, h, oldw, oldh);
        mFirstBeginX = (w - mBitmap.getWidth()) * .5f;
        mFirstBeginY = h * .1f;
        mFirstMatrix.postTranslate(mFirstBeginX, mFirstBeginY);

        mSecondBeginX = (w - mSecondBitmap.getWidth()) * .5f;
        mSecondBeginY = h * .23f;
        mSecondMatrix.postTranslate(mSecondBeginX, mSecondBeginY);
    }
}
