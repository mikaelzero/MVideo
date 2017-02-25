package com.miaoyongjun.mdragvideo;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.miaoyongjun.mdragvideo.media.IjkVideoView;


/**
 * Created by miaoyongjun.
 * Date:  2017/2/17
 */

public class DragVideoView extends IjkVideoView {
    private final int ALPHA_255 = 255;
    private final long DURATION_DEFAULT = 300;
    private int mAlpha = ALPHA_255;
    private boolean isDragging = false;
    private Paint mPaint;

    private float mDownX;
    private float mDownY;
    //触摸点距离按下点的距离
    private float mTranslateY;
    private float mTranslateX;
    private int screenHeight;
    private int screenWidth;

    //子view距离画布边缘距离
    private float childMarginParentX;
    private float childMarginParentY;

    private float mScale = 1;
    private float mMinScale = 0.5f;

    private int MAX_TRANSLATE_Y = 0;

    private long animationDuration = DURATION_DEFAULT;
    private OnTapListener mTapListener;
    private OnDragOutListener mDragOutListener;
    boolean isReset = false;
    private View childView;


    public DragVideoView(Context context) {
        this(context, null);
    }

    public DragVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        MAX_TRANSLATE_Y = screenHeight / 8;
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        //保证会回调onDraw方法
        setWillNotDraw(false);
        childView = getChildAt(0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //你也可以在dispatchDraw中操作,不过需要在super调用之前
        mPaint.setAlpha(mAlpha);
        canvas.drawRect(0, 0, screenWidth, screenHeight, mPaint);
        if (isReset) {
            isReset = false;
            canvas.translate(-childMarginParentX, -childMarginParentY);
        } else {
            childView.setTranslationX(mTranslateX);
            childView.setTranslationY(mTranslateY);
            childView.setPivotX(mDownX);
            childView.setPivotY(mDownY);
            childView.setScaleX(mScale);
            childView.setScaleY(mScale);
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    onActionDown(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    onActionMove(event);
                    break;
                case MotionEvent.ACTION_UP:
                    onActionUp();
                    break;
            }
        }
        return true;

    }


    private void onActionDown(MotionEvent event) {
        mDownX = event.getX();
        mDownY = event.getY();
    }

    private void onActionMove(MotionEvent event) {
        float moveX = event.getX();
        float moveY = event.getY();

        childMarginParentX = moveX - mDownX * mScale;
        childMarginParentY = moveY - mDownY * mScale;

        mTranslateX = moveX - mDownX;
        mTranslateY = moveY - mDownY;

        if (mTranslateY > 0) {
            isDragging = true;
        }
        //如果处于原位置则不允许向上拖动
        if (mAlpha == ALPHA_255 && mTranslateY < 0 && !isDragging) {
            return;
        }
        //根据触摸点的Y坐标和屏幕的比例来更改透明度
        float alphaChangePercent = mTranslateY / screenHeight;

        if (mScale >= mMinScale && mScale <= 1f) {
            mScale = 1 - alphaChangePercent;
            mAlpha = (int) (ALPHA_255 * (1 - alphaChangePercent));
            if (mAlpha > ALPHA_255) {
                mAlpha = ALPHA_255;
            } else if (mAlpha < 0) {
                mAlpha = 0;
            }
        }
        if (mScale < mMinScale) {
            mScale = mMinScale;
        } else if (mScale > 1f) {
            mScale = 1;
        }
        invalidate();
    }

    private void onActionUp() {
        if (mAlpha == ALPHA_255 && mTranslateY < 0 && !isDragging) {
            isDragging = false;
            return;
        }
        isDragging = false;
        if (mTranslateY > MAX_TRANSLATE_Y) {
            if (mDragOutListener != null) {
                resetViewLocation();
                mDragOutListener.onExit(this, mScale);
            } else {
                throw new RuntimeException("OnDragOutListener can't be null ! ");
            }
        } else if (mTranslateX == 0 && mTranslateY == 0) {
            if (mTapListener != null) {
                resetAlpha();
                mTapListener.onTap(this);
            }
        } else {
            performAnimation();
        }
    }

    private void resetViewLocation() {
        mAlpha = 0;
        isReset = true;
        invalidate();
        //设置整个DragVideoView的x,y
        setX(childMarginParentX);
        setY(childMarginParentY);
    }

    public void resetAlpha() {
        mAlpha = 0;
        invalidate();
    }

    private void performAnimation() {
        getScaleAnimation().start();
        getTranslateXAnimation().start();
        getTranslateYAnimation().start();
        getAlphaAnimation().start();
    }


    private ValueAnimator getAlphaAnimation() {
        final ValueAnimator animator = ValueAnimator.ofInt(mAlpha, ALPHA_255);
        animator.setDuration(animationDuration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mAlpha = (int) valueAnimator.getAnimatedValue();
            }
        });

        return animator;
    }

    private ValueAnimator getTranslateYAnimation() {
        final ValueAnimator animator = ValueAnimator.ofFloat(mTranslateY, 0);
        animator.setDuration(animationDuration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mTranslateY = (float) valueAnimator.getAnimatedValue();
            }
        });

        return animator;
    }

    private ValueAnimator getTranslateXAnimation() {
        final ValueAnimator animator = ValueAnimator.ofFloat(mTranslateX, 0);
        animator.setDuration(animationDuration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mTranslateX = (float) valueAnimator.getAnimatedValue();
            }
        });

        return animator;
    }

    private ValueAnimator getScaleAnimation() {
        final ValueAnimator animator = ValueAnimator.ofFloat(mScale, 1);
        animator.setDuration(animationDuration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mScale = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                animator.removeAllListeners();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        return animator;
    }


    public float getMinScale() {
        return mMinScale;
    }

    public void setMinScale(float minScale) {
        mMinScale = minScale;
    }

    public void setOnTapListener(OnTapListener listener) {
        mTapListener = listener;
    }

    public void setOnExitListener(OnDragOutListener listener) {
        mDragOutListener = listener;
    }

    public interface OnTapListener {
        void onTap(FrameLayout frameLayout);
    }

    public interface OnDragOutListener {
        void onExit(DragVideoView dragVideoView, float scale);
    }


    public long getAnimationDuration() {
        return animationDuration;
    }

    public void setAnimationDuration(long animationDuration) {
        this.animationDuration = animationDuration;
    }

    public void setBgAlpha(float alpha) {
        mAlpha = (int) alpha;
        invalidate();
    }

    public void setChildViewVisible(int viewVisible) {
        childView.setVisibility(viewVisible);
    }
}
