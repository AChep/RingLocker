/*
 * Copyright (C) 2013 AChep@xda <artemchep@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.achep.ringlocker.locker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.achep.ringlocker.utils.DisplayUtils;

/**
 * Created by achep on 13.08.13.
 */
public class LockerView extends View {


    private RenderManager mRenderManager;
    private Paint mErasePaint;

    private final float[] mCenter = new float[2];
    private float mRadius;
    private float mTargetRadius;

    private OnUnlockListener mOnUnlockListener;

    public interface OnUnlockListener {

        void unlock();

        void progress(float value);

    }

    public LockerView(Context context) {
        super(context);
        init(context);
    }

    public LockerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mRenderManager = new RenderManager(new RenderManager.OnRenderRequestListener() {
            @Override
            public void OnRenderRequest() {
                invalidate();
            }
        }, DisplayUtils.getRefreshRate(context));

        mErasePaint = new Paint();
        mErasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
        mErasePaint.setAntiAlias(true);
        mErasePaint.setColor(Color.BLACK);
        mErasePaint.setAlpha(0);
    }

    public void setOnUnlockListener(OnUnlockListener listener) {
        mOnUnlockListener = listener;
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        mTargetRadius = (float) (Math.sqrt(w * w + h * h) / 3);
    }

    @Override
    public void onDraw(Canvas canvas) {
        float ratio = mRadius / mTargetRadius;
        if (ratio > 1f) ratio = 1f;

        canvas.drawARGB((int) (215 * (1f - ratio)), 0, 0, 0);
        canvas.drawCircle(mCenter[0], mCenter[1], mRadius, mErasePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float x = event.getX(), y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mCenter[0] = x;
                mCenter[1] = y;
            case MotionEvent.ACTION_MOVE:
                calculateRadius(x, y);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                hide();
                break;
            default:
                return false;
        }

        mRenderManager.requestRender();
        return true;
    }

    private void calculateRadius(float x, float y) {
        final float x0 = mCenter[0] - x;
        final float y0 = mCenter[1] - y;
        setRadius((float)Math.sqrt(x0 * x0 + y0 * y0));
    }

    private void setRadius(float radius) {
        mRadius = radius;
        if (mOnUnlockListener != null) {
            if (mRadius > mTargetRadius) {
                mOnUnlockListener.unlock();
            } else {
                mOnUnlockListener.progress(mRadius / mTargetRadius);
            }
        }
    }

    // TODO: Hide animation
    private void hide() {
        setRadius(0);
    }

}
