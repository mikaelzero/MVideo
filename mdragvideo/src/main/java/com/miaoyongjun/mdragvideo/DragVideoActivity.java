package com.miaoyongjun.mdragvideo;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.miaoyongjun.mdragvideo.media.IjkVideoView;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;


/**
 * Created by miaoyongjun.
 * Date:  2017/2/17
 */

public class DragVideoActivity extends AppCompatActivity {
    DragVideoView dragVideoView;
    ImageView previewImage;
    private final long EXIT_DEFAULT_DURATION = 300;
    private final long ENTER_DEFAULT_DURATION = 500;
    private int DEFAULT_DIRECTION = IjkVideoView.RotateDirection.DEFAULT.getRotateDirectionValue();
    long exitDuration = EXIT_DEFAULT_DURATION;
    long enterDuration = ENTER_DEFAULT_DURATION;
    int mOriginLeft;
    int mOriginTop;
    int mOriginHeight;
    int mOriginWidth;

    //横屏时宽度填充时的高度
    int targetImageHeight;
    float imageScaleX;
    float imageScaleY;

    public static final String LEFT = "left";
    public static final String TOP = "top";
    public static final String HEIGHT = "height";
    public static final String WIDTH = "width";
    private String videoPath;

    int rotateDirection = DEFAULT_DIRECTION;
    private int progressColor;
    private boolean isLooper = true;
    private String imagePath;
    private int resId;
    private int resDefaultValue = -100;
    VideoStatusCallBack videoStatusCallBack;
    int screenWidth;
    int screenHeight;
    private ProgressBar mProgressBar;


    private void getIntentData() {
        videoPath = getIntent().getStringExtra(MVideo.VIDEO_PATH);
        exitDuration = getIntent().getLongExtra(MVideo.EXIT_DURATION, EXIT_DEFAULT_DURATION);
        enterDuration = getIntent().getLongExtra(MVideo.EXIT_DURATION, ENTER_DEFAULT_DURATION);
        rotateDirection = getIntent().getIntExtra(MVideo.ROTATE_DIRECTION, DEFAULT_DIRECTION);
        progressColor = getIntent().getIntExtra(MVideo.PROGRESS_COLOR, DragVideoView.DEFAULT_PROGRESS_COLOR);
        isLooper = getIntent().getBooleanExtra(MVideo.LOOPER, true);
        resId = getIntent().getIntExtra(MVideo.PREVIEW_IMAGE, resDefaultValue);
        imagePath = getIntent().getStringExtra(MVideo.PREVIEW_PATH);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_drag_video);
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        previewImage = (ImageView) findViewById(R.id.previewImage);
        dragVideoView = (DragVideoView) findViewById(R.id.dragVideoView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        getIntentData();
        mProgressBar.getIndeterminateDrawable().setColorFilter(
                progressColor, PorterDuff.Mode.MULTIPLY);
        if (!TextUtils.isEmpty(imagePath)) {
            Glide.with(DragVideoActivity.this).load(imagePath).into(previewImage);
        }
        setViewPosition();
    }

    private void setViewPosition() {
        //动态获取控件的宽高,你也可以通过post来实现
        dragVideoView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        dragVideoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        getLocation();
                        dragVideoView.setRotateDirection(rotateDirection);
                        dragVideoView.setAnimationDuration(exitDuration);
                        dragVideoView.setLooper(isLooper);
                        if (!TextUtils.isEmpty(videoPath)) {
                            dragVideoView.setVideoPath(videoPath);
                            dragVideoView.start();
                        } else {
                            MVideo.getInstance().printLog("videoPath can't be null");
                        }
                    }
                });
        dragVideoView.setOnExitListener(new DragVideoView.OnDragOutListener() {
            @Override
            public void onExit(DragVideoView dragVideoView, float scale) {
                performExitAnimation(dragVideoView, scale);
            }
        });
        dragVideoView.setOnTapListener(new DragVideoView.OnTapListener() {
            @Override
            public void onTap(FrameLayout frameLayout) {
                finishVideo();
            }
        });
        dragVideoView.setVideoPlayCallback(new IjkVideoView.VideoPlayCallbackImpl() {
            @Override
            public void onPrepared() {
                mProgressBar.setVisibility(View.GONE);
                previewImage.setVisibility(View.GONE);
            }

            @Override
            public void onPlayStart() {
                if (videoStatusCallBack != null) videoStatusCallBack.onPlayStart();
            }

            @Override
            public void onPlayFinish() {
                mProgressBar.setVisibility(View.GONE);
                previewImage.setVisibility(View.GONE);
                if (videoStatusCallBack != null) videoStatusCallBack.onPlayFinish();
                finishVideo();
            }

            @Override
            public void onPlayError(IMediaPlayer mp, int framework_err, int impl_err) {
                mProgressBar.setVisibility(View.GONE);
                previewImage.setVisibility(View.GONE);
                if (videoStatusCallBack != null)
                    videoStatusCallBack.onPlayError(mp, framework_err, impl_err);
                finishVideo();
            }
        });
    }


    private void getLocation() {
        /**
         * 通过图片先缩放到图片对应的大小,再显示出视频,当视频完全加载完时,隐藏图片
         * 因此,如果想达到最好的效果,你应该将图片的比例和视频的比例相对应
         */
        mOriginLeft = getIntent().getIntExtra(LEFT, 0);
        mOriginTop = getIntent().getIntExtra(TOP, 0);
        mOriginHeight = getIntent().getIntExtra(HEIGHT, 0);
        mOriginWidth = getIntent().getIntExtra(WIDTH, 0);
        dragVideoView.setChildViewVisible(View.GONE);


        int[] locationImage = new int[2];
        previewImage.getLocationOnScreen(locationImage);
        int targetImageWidth = screenWidth;
        float targetSize = (float) mOriginHeight / (float) mOriginWidth;
        targetImageHeight = (int) (screenWidth * targetSize);
        imageScaleX = (float) mOriginWidth / targetImageWidth;
        imageScaleY = (float) mOriginHeight / targetImageHeight;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(targetImageWidth, targetImageHeight);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        previewImage.setLayoutParams(layoutParams);
        if (resId != resDefaultValue) previewImage.setImageResource(resId);
        previewImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        //设置原view的位置，并且以该view的左上角为缩放中心点
        previewImage.setX(mOriginLeft);
        previewImage.setY(mOriginTop);
        previewImage.setPivotX(0);
        previewImage.setPivotY(0);
        previewImage.setScaleX(imageScaleX);
        previewImage.setScaleY(imageScaleY);

        performEnterAnimation();
    }


    private void finishVideo() {
        dragVideoView.resetAlpha();
        handleCoordinate(dragVideoView, 1);
    }

    private void performExitAnimation(DragVideoView view, float scale) {
        handleCoordinate(view, scale);
    }

    private void handleCoordinate(final DragVideoView dragVideoView, float scale) {
        View textureView = dragVideoView.getTextureView();
        View currentView;
        float topMargin;
        /**
         * 1.如果是横屏，获取角度，如果是90和270 ，则按竖屏方式处理，否则按照横屏方式处理
         * 2.如果是竖屏，也要获取角度，如果是90和270，则按照横屏方式处理
         *   这里这样写就是为了把逻辑写清楚，不然看起来很绕
         */
        if (textureView.getMeasuredWidth() > textureView.getMeasuredHeight()) {
            //视频本身为横屏
            if (textureView.getRotation() == 90 || textureView.getRotation() == 270) {
                //显示为竖屏方式处理
                dragVideoView.setPivotX(0);
                dragVideoView.setPivotY(0);
                currentView = dragVideoView;
                topMargin = mOriginTop;
            } else {
                /**
                 * 显示为横屏方式处理
                 */
                //设置缩放中心点
                dragVideoView.setPivotX(textureView.getX());
                dragVideoView.setPivotY(textureView.getY() * scale);
                currentView = textureView;
                float childViewAlignParent = ((dragVideoView.getMeasuredHeight() * scale - textureView.getMeasuredHeight() * scale) / 2);
                topMargin = mOriginTop - childViewAlignParent;
            }
        } else {
            //视频本身为竖屏
            if (textureView.getRotation() == 90 || textureView.getRotation() == 270) {
                /**
                 * 显示为横屏方式处理
                 */
                //设置缩放中心点
                dragVideoView.setPivotX(textureView.getX());
                dragVideoView.setPivotY(textureView.getY() * scale);
                currentView = textureView;
                float childViewAlignParent = ((dragVideoView.getMeasuredHeight() * scale - textureView.getMeasuredHeight() * scale) / 2);
                topMargin = mOriginTop - childViewAlignParent;
            } else {
                //显示为竖屏方式处理
                dragVideoView.setPivotX(0);
                dragVideoView.setPivotY(0);
                currentView = dragVideoView;
                topMargin = mOriginTop;
            }
        }
        //原view和拖动结束后的view宽高比
        float mWidthScales = mOriginWidth / (currentView.getMeasuredWidth() * scale);
        float mHeightScales = mOriginHeight / (currentView.getMeasuredHeight() * scale);
        finishWithAnimation(dragVideoView.getX(), mOriginLeft, dragVideoView.getY(), topMargin,
                1, mWidthScales, 1, mHeightScales);

    }

    private void performEnterAnimation() {
        if (previewImage == null) {
            return;
        }

        float transY;
        if (mOriginWidth > mOriginHeight) {
            //如果图片是横向的
            transY = (screenHeight - targetImageHeight) / 2;
        } else {
            transY = 0;
        }

        ValueAnimator alphaAnimator = ValueAnimator.ofFloat(0, 255);
        alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                dragVideoView.setBgAlpha((Float) valueAnimator.getAnimatedValue());
            }
        });
        alphaAnimator.setDuration(enterDuration);
        alphaAnimator.start();

        ValueAnimator translateXAnimator = ValueAnimator.ofFloat(mOriginLeft, 0);
        translateXAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                previewImage.setX((Float) valueAnimator.getAnimatedValue());
            }
        });
        translateXAnimator.setDuration(enterDuration);
        translateXAnimator.start();

        ValueAnimator translateYAnimator = ValueAnimator.ofFloat(mOriginTop, transY);
        translateYAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                previewImage.setY((Float) valueAnimator.getAnimatedValue());
            }
        });
        translateYAnimator.setDuration(enterDuration);
        translateYAnimator.start();

        ValueAnimator scaleYAnimator = ValueAnimator.ofFloat(imageScaleY, 1);
        scaleYAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                previewImage.setScaleY((Float) valueAnimator.getAnimatedValue());
            }
        });
        scaleYAnimator.setDuration(enterDuration);
        scaleYAnimator.start();

        ValueAnimator scaleXAnimator = ValueAnimator.ofFloat(imageScaleX, 1);
        scaleXAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                previewImage.setScaleX((Float) valueAnimator.getAnimatedValue());
            }
        });
        scaleXAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mProgressBar.setVisibility(View.VISIBLE);
                dragVideoView.setChildViewVisible(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        scaleXAnimator.setDuration(enterDuration);
        scaleXAnimator.start();
    }

    private void finishWithAnimation(float transXBegin, float transXEnd, float transYBegin, float transYEnd,
                                     float scaleXBegin, float scaleXEnd, float scaleYBegin, float scaleYEnd) {
        if (dragVideoView == null) {
            return;
        }
        ValueAnimator translateXAnimator = ValueAnimator.ofFloat(transXBegin, transXEnd);
        translateXAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                dragVideoView.setX((Float) valueAnimator.getAnimatedValue());
            }
        });
        translateXAnimator.setDuration(exitDuration);
        translateXAnimator.start();

        ValueAnimator translateYAnimator = ValueAnimator.ofFloat(transYBegin, transYEnd);
        translateYAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                dragVideoView.setY((Float) valueAnimator.getAnimatedValue());
            }
        });
        translateYAnimator.setDuration(exitDuration);
        translateYAnimator.start();

        ValueAnimator scaleYAnimator = ValueAnimator.ofFloat(scaleYBegin, scaleYEnd);
        scaleYAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                dragVideoView.setScaleY((Float) valueAnimator.getAnimatedValue());
            }
        });
        scaleYAnimator.setDuration(exitDuration);
        scaleYAnimator.start();

        ValueAnimator scaleXAnimator = ValueAnimator.ofFloat(scaleXBegin, scaleXEnd);
        scaleXAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                dragVideoView.setScaleX((Float) valueAnimator.getAnimatedValue());
            }
        });

        scaleXAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (dragVideoView != null) dragVideoView.release(true);
                animator.removeAllListeners();
                finish();
                overridePendingTransition(0, 0);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        scaleXAnimator.setDuration(exitDuration);
        scaleXAnimator.start();
    }


    @Override
    protected void onPause() {
        super.onPause();
        finishVideo();
    }

    @Override
    public void onBackPressed() {
        finishVideo();
    }

    public DragVideoView getDragVideoView() {
        return dragVideoView;
    }

    public interface VideoStatusCallBack {

        void onPlayStart();

        void onPlayFinish();

        void onPlayError(IMediaPlayer mp, int framework_err, int impl_err);

    }

}
