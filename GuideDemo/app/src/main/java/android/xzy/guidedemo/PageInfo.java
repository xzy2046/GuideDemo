package android.xzy.guidedemo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by zhengyangxu on 14-10-8.
 *
 * TODO 是否需要index，直接按存放顺序加载
 *
 * 每一Page 拥有整个Touch相等的宽和高，自己控制自己的绘制
 */
abstract class PageInfo {

    final static int DEFAULT_INDEX = -1;

    /* 大图，所有Page都使用 */
    Bitmap mBitmap;

    int index = DEFAULT_INDEX;

    int mWidth;

    int mHeight;

    float mLastDelta = 0;

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    abstract void draw(Canvas canvas);

    /**
     * delta : 0f to 2.0f ; 0-1渐进 1-2渐出
     * 根据delta做动画
     * @param delta
     */
    void update(float delta) {
        if (delta >= 1.999) {
            delta = 2;   //确保最后一帧效果
        }
        if (delta < 0.001) {
            delta = 0;
        }
    };

    public void onSizeChaged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        mHeight = h;
    }

    public void setLastDelta(float lastDelta) {
        mLastDelta = lastDelta;
    }
}
