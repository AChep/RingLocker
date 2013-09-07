package com.achep.ringlocker.locker.surfaceview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.achep.Looper;
import com.achep.ringlocker.R;
import com.achep.ringlocker.utils.DisplayUtils;

/**
 * Created by Artem on 07.09.13.
 */
public class LockerSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final int MAX_TOUCH_TIME = 2000; // ms
    private static final int MESSAGE_CANCEL_TOUCH = 0;

    private RenderThread mRenderThread;
    private OnUnlockListener mListener;

    private boolean mTouched;
    private final float[] mCenter = new float[2];
    ;
    private float mTargetRadius =200;
    private float mRadius;

    private Animator mAnimator;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message m) {
            if (m.what == MESSAGE_CANCEL_TOUCH) {
                hide(true);
            }
        }
    };

    public LockerSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public LockerSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
       // setZOrderOnTop(true);

        final SurfaceHolder holder = getHolder();
        assert holder != null;
        holder.addCallback(this);
        holder.setFormat(PixelFormat.TRANSPARENT);

        final float refreshRate = DisplayUtils.getRefreshRate(context);
        mAnimator = new Animator(refreshRate, getResources().getInteger(R.integer.config_ringAnimTime));
    }

    public void setOnUnlockListener(OnUnlockListener listener) {
        mListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float x = event.getX(), y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouched = true;
                mCenter[0] = x;
                mCenter[1] = y;

                mHandler.removeMessages(MESSAGE_CANCEL_TOUCH);
                mHandler.sendEmptyMessageDelayed(MESSAGE_CANCEL_TOUCH, MAX_TOUCH_TIME);
                mAnimator.stop();
            case MotionEvent.ACTION_MOVE:
                if (mTouched) {
                    calculateRadius(x, y);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                hide(true);
                break;
            default:
                return false;
        }
        return true;
    }

    private void calculateRadius(float x, float y) {
        setRadius((float) Math.hypot(mCenter[0] - x, mCenter[1] - y));
    }

    private void setRadius(float radius) {
        mRadius = radius;
        if (mListener != null) {
            if (mRadius > mTargetRadius) {
                mListener.unlock();
            } else {
                mListener.progress(mRadius / mTargetRadius);
            }
        }
    }

    private void hide(boolean animate) {
        mTouched = false;
        if (animate) {
            mAnimator.start(mRadius, 0, mTargetRadius);
        } else {
            setRadius(0);
        }
    }

    private class Animator extends Looper {

        private float startRadius;
        private float endRadius;
        private long timeEnd;

        private final int duration;

        public Animator(float frameRate, int duration) {
            super(frameRate);
            this.duration = duration;
        }

        public void start(float startRadius, float endRadius, float maxRadius) {
            this.startRadius = startRadius;
            this.endRadius = endRadius;
            this.timeEnd = getTime() + duration;

            super.start();
        }

        @Override
        protected boolean onLoop(final long time) {
            final long deltaTime = timeEnd - time;
            final float radius = deltaTime > 0
                    ? (float) (startRadius + (endRadius - startRadius)
                    * Math.pow((double) (duration - deltaTime) / duration, 1 / 1.4))
                    : endRadius;

            LockerSurfaceView.this.setRadius(radius);

            if ((int) radius == (int) endRadius)
                return Looper.STOP;
            return Looper.CONTINUE;
        }
    }

    // //////////////////////////////////////////
    // ///////////// -- PUBLIC -- ///////////////
    // //////////////////////////////////////////

    public final float getRadius() {
        return mRadius;
    }

    public final float getTargetRadius() {
        return mTargetRadius;
    }

    public final float[] getCenterPoint() {
        return mCenter;
    }

    // //////////////////////////////////////////
    // ///////// -- SURFACE VIEW -- /////////////
    // //////////////////////////////////////////

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mTargetRadius = (float) (Math.sqrt(width * width + height * height) / 3);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mRenderThread = new RenderThread(holder, this);
        mRenderThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        mRenderThread.running = false;
        while (retry) {
            try {
                mRenderThread.join();
                retry = false;
            } catch (InterruptedException e) {
                // try again even if failed
            }
        }
    }

    private static class RenderThread extends Thread {

        private final LockerSurfaceView mLockerSurfaceView;
        private final SurfaceHolder mSurfaceHolder;
        private final Paint mErasePaint;

        public boolean running = true;

        public RenderThread(SurfaceHolder surfaceHolder, LockerSurfaceView lockerSurfaceView) {
            mLockerSurfaceView = lockerSurfaceView;
            mSurfaceHolder = surfaceHolder;

            mErasePaint = new Paint();
            mErasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
            mErasePaint.setAntiAlias(true);
            mErasePaint.setColor(Color.GREEN);
            mErasePaint.setAlpha(0);
        }

        @Override
        public void run() {
            Canvas canvas;
            while (running) {
                canvas = null;
                try {
                    canvas = mSurfaceHolder.lockCanvas(null);
                    synchronized (mSurfaceHolder) {
                        if (running) draw(canvas);
                    }
                } finally {
                    if (canvas!= null) {
                        mSurfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }

                /*try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            }
        }

        private void draw(Canvas canvas) {
            final float targetRadius = mLockerSurfaceView.getTargetRadius();
            if (targetRadius != 0) {
                final float radius = mLockerSurfaceView.getRadius();
                final float[] center = mLockerSurfaceView.getCenterPoint();

                float ratio = radius / targetRadius;
                if (ratio > 1f) ratio = 1f;

                canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                canvas.drawARGB((int) (210 * (1f - ratio)), 0, 0, 0);
                canvas.drawCircle(center[0], center[1], radius, mErasePaint);
            }
        }
    }
}
