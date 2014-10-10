package android.xzy.guidedemo;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.util.ArrayList;
import java.util.List;

import xzy.android.agraphics.animation.AAnimator;
import xzy.android.agraphics.animation.ValuesHolder;
import xzy.android.agraphics.interpolater.AnticipateInterpolator;
import xzy.android.agraphics.interpolater.EaseType;
import xzy.android.agraphics.interpolater.QuadInterpolator;
import xzy.android.agraphics.interpolater.QuintInterpolator;
import xzy.android.agraphics.interpolater.SineInterpolator;


/**
 * Guide画面Parent容器
 */
public class TouchLayer extends View {

    private static final int FIRST_PAGE = 0;

    private static final int INVALID_PAGE = -1;

    private static final int DEFAULT_INDEX = FIRST_PAGE;

    private int mCurrentIndex = DEFAULT_INDEX;

    private int mNextIndex = mCurrentIndex + 1;

    private List<Bitmap> mBitmaps = new ArrayList<Bitmap>();

    private List<PageInfo> mPageInfos = new ArrayList<PageInfo>();

    private float mDelta = 0f;

    private float mDownX = -1f;

    private float mDownY = -1f;

    private float mLastX = -1f;

    private float mLastY = -1f;

    private int mWidth;

    private int mHeight;

    private int mHalfWidth;

    //delta Animatior
    private AAnimator mDeltaAnimator;

    private ValuesHolder mDeltaValueHolder;

    //Touch state
    private static final int STATE_DOWN = 0x101;

    private static final int STATE_MOVE = STATE_DOWN + 1;

    private static final int STATE_NONE = STATE_MOVE + 1;

    private int mTouchState = STATE_NONE;

    private static final int PAGE_SNAP_ANIMATION_DURATION = 250;

    private static final boolean DEBUG = false;

    public TouchLayer(Context context) {
        super(context);
        init(null, 0);
    }

    public TouchLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public TouchLayer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        initPageInfos();
        initAnimation();
    }

    /**
     * 初始化所有的Page
     */
    private void initPageInfos() {
        Resources resources = getResources();
        TwoBitmapPageInfo info = new TwoBitmapPageInfo();
        info.setBitmap(BitmapFactory.decodeResource(resources, R.drawable.welcome_logo));
        info.setSecondBitmap(BitmapFactory.decodeResource(resources, R.drawable.welcome_image));
        mPageInfos.add(info);

        TextPageInfo textInfo = new TextPageInfo();
        textInfo.setTitle(resources.getString(R.string.search_title));
        textInfo.setSummary(resources.getString(R.string.search_summary));
        textInfo.setBitmap(BitmapFactory.decodeResource(resources, R.drawable.welcome_search_bg));
        mPageInfos.add(textInfo);

        updateCurrentPage(0);
    }

    private void initAnimation() {
        mDeltaValueHolder = new ValuesHolder();
        mDeltaAnimator = new AAnimator();
        mDeltaAnimator.setAnimationListener(new AAnimator.AnimationListener() {
            @Override
            public void onAnimationStart(AAnimator aAnimator) {

            }

            @Override
            public void onAnimationEnd(AAnimator aAnimator) {
                int tmpCurrent = mCurrentIndex;
                updateCurrentPage(mNextIndex);
                mNextIndex = tmpCurrent;
            }

            @Override
            public void onAnimationRepeat(AAnimator aAnimator) {

            }

            @Override
            public void onAnimationCancel(AAnimator aAnimator) {

            }
        });
        mDeltaAnimator.setAnimationUpdateListener(new AAnimator.AnimationUpdateListener() {
            @Override
            public void onAnimationUpdate(AAnimator aAnimator) {
                mDelta = mDeltaValueHolder.getValue();
                if (mDelta < 0) {
                    mDelta = 0;
                }
                if (mDelta > 1) {
                    mDelta = 1;
                }
                updatePageDelta();
            }
        });
        mDeltaAnimator.setInterpolator(new QuadInterpolator(EaseType.Type.IN));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (DEBUG) {
                    Log.i("xzy", "ActionDown");
                }
                mDownX = event.getX();
                mDownY = event.getY();
                mLastX = mDownX;
                mLastY = mDownY;
                mTouchState = STATE_DOWN;
                break;
            case MotionEvent.ACTION_MOVE:
                if (DEBUG) {
                    Log.i("xzy", "Action move");
                }
                ViewConfiguration vc = ViewConfiguration.get(getContext());
                float moveX = mLastX - event.getX();
                if (Math.abs(moveX) > vc.getScaledTouchSlop() / 3) {
                    mLastX = event.getX();
                    mLastY = event.getY();
                    calDelta(moveX);
                    updateNextIndex();
                    updatePageDelta();
                    Log.i("xzy", "translate");
                }
                mTouchState = STATE_MOVE;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (DEBUG) {
                    Log.i("xzy", "Action up or cancel");
                }
                snapToPage(mNextIndex);
                resetValues();
                mTouchState = STATE_NONE;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (DEBUG) {
                    Log.i("xzy", "Action pointer down");
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (DEBUG) {
                    Log.i("xzy", "Action pointer up");
                }
                break;
            default:
                ;
        }
        return true;
    }

    /**
     * 手指滑动一屏mDelta才会等于1
     *
     * @param moveX 手指x方向的移动值
     */
    private float calDelta(float moveX) {
        mDelta += moveX / mWidth;
//        Log.i("xzy", "calDelta is : " + mDelta);
        if (mDelta < 0) {
            mDelta = 0;
        }
        if (mDelta > 1) {
            mDelta = 1;
        }

        return mDelta;
    }

    private void updateNextIndex() {
        if (mLastX - mDownX >= 0) {
            mNextIndex = mCurrentIndex - 1;
        } else {
            mNextIndex = mCurrentIndex + 1;
        }

        if (mNextIndex > getPageCount() - 1 || mNextIndex < 0) {
            mNextIndex = INVALID_PAGE;
//            mNextIndex = mCurrentIndex;
        }

//        mNextIndex = Math.min(getPageCount() -1, Math.max(0, mNextIndex));


    }

    private void updatePageDelta() {
        //mCurrentIndex 一定是淡出的
        mPageInfos.get(mCurrentIndex).update(1 + mDelta);
        //mNextIndex 一定是淡入的
        if (mNextIndex != INVALID_PAGE) {
            mPageInfos.get(mNextIndex).update(1 - mDelta);
        }
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.argb(255, 63, 145,148 ));
        for (PageInfo info : mPageInfos) {
            info.draw(canvas);
        }
//        mPageInfos.get(mCurrentIndex).draw(canvas);
//
//        if (mNextIndex != INVALID_PAGE) {
//            mPageInfos.get(mNextIndex).draw(canvas);
//        }
    }

    /**
     * Guide画面显示的图片列表，每一屏对应一张图
     */
    public List<Bitmap> getBitmaps() {
        return mBitmaps;
    }

    public void setBitmaps(final List<Bitmap> bitmaps) {
        if (mBitmaps != null) {
            mBitmaps.clear();
        }
        mBitmaps = bitmaps;
    }

    public void addPageInfo(final PageInfo pageInfo) {
        mPageInfos.add(pageInfo);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        for (PageInfo info : mPageInfos) {
            info.onSizeChaged(w, h, oldw, oldh);
        }
        mWidth = w;
        mHeight = h;

        mHalfWidth = mWidth / 2;
    }

    private void updateCurrentPage(int currentIndex) {
        Log.i("xzy", "currentIndex is : " + currentIndex + " next page is : " + mNextIndex);

        currentIndex = Math.max(0, Math.min(getPageCount() - 1, currentIndex));

        mCurrentIndex = currentIndex;
        mPageInfos.get(mCurrentIndex).setLastDelta(1);
        if (getPageCount() - 1 > mCurrentIndex) {
            mPageInfos.get(mCurrentIndex + 1).setLastDelta(0);
        }
        if (mCurrentIndex > 0) {
            mPageInfos.get(mCurrentIndex - 1).setLastDelta(0);
        }
    }

    private void resetValues() {
        mDownX = -1;
        mDownY = -1;
        mLastX = -1;
        mLastY = -1;
    }

    private void snapToPage(int index) {
        snapToPage(index, PAGE_SNAP_ANIMATION_DURATION);
    }

    private void snapToPage(int index, int duration) {
        if (index == INVALID_PAGE) return;
        index = Math.max(0, Math.min(getPageCount() - 1, index));


        if (index > mCurrentIndex) {
            snapToPage(index, 1f, duration);
        } else if (index < mCurrentIndex) {
            snapToPage(index, 0f, duration);
        }
    }

    private void snapToPage(int index, float delta, int duration) {
        mNextIndex = index;

        mDeltaValueHolder.setValue(mDelta);
        mDeltaAnimator.setupAnimate(mDeltaValueHolder, mDelta, delta, duration);
        mDeltaAnimator.start();
    }

    private int getPageCount() {
        return mPageInfos.size();
    }
}
