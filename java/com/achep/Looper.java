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

package com.achep;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import static android.os.SystemClock.uptimeMillis;

/**
 * Simple makeLoop class for making timers / animations / stopwatchs and more.
 */
public abstract class Looper {

    public static final boolean STOP_LOOPING = true;
    public static final boolean CONTINUE_LOOPING = false;

    private static final byte MSG_MAKE_A_NEW_LOOP = 0;

    private boolean mLooping;
    private long mCurLoopingTime;
    private long mIterationNumber;
    private long mStartTime;

    private final H mHandler;
    private final int mFrameDuration;

    @SuppressLint("HandlerLeak")
    private class H extends Handler {
        public void handleMessage(Message m) {
            if (m.what == MSG_MAKE_A_NEW_LOOP) {
                makeLoop();
            }
        }
    }

    public Looper(final float refreshRate) {
        mFrameDuration = (int) (1000 / refreshRate);
        mHandler = new H();
    }

    // //////////////////////////////////////////
    // /////////// -- CONTROLS -- ///////////////
    // //////////////////////////////////////////

    /**
     * Start looping if it is not already running. <i>The same as {@code startDelayed(0)}.</i>
     */
    public void start() {
        startDelayed(0);
    }

    /**
     * @see #start()
     */
    public void startDelayed(final int ms) {
        if (!mLooping) {
            mLooping = true;
            mStartTime = getTime();
            mCurLoopingTime = mStartTime + ms;
            mIterationNumber = 0;

            mHandler.sendMessageAtTime(
                    mHandler.obtainMessage(MSG_MAKE_A_NEW_LOOP),
                    mCurLoopingTime);
        }
    }

    public void stop() {
        if (mLooping) {
            mLooping = false;
            mHandler.removeMessages(MSG_MAKE_A_NEW_LOOP);
        }
    }

    public void resume() {
        mLooping = true;
        mHandler.sendMessageAtTime(
                mHandler.obtainMessage(MSG_MAKE_A_NEW_LOOP),
                mCurLoopingTime);
    }

    // //////////////////////////////////////////
    // ////////////// -- CORE -- ////////////////
    // //////////////////////////////////////////

    private void makeLoop() {
        if (mLooping) {
            if (onLoop(getTime())) {
                stop();
            } else {
                mIterationNumber++;
                mCurLoopingTime += mFrameDuration;
                if (mCurLoopingTime < getTime()) {
                    mCurLoopingTime = getTime() + mFrameDuration;
                }
                mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_MAKE_A_NEW_LOOP),
                        mCurLoopingTime);
            }
        }
    }

    protected abstract boolean onLoop(final long uptime);

    // //////////////////////////////////////////
    // ///////// -- GETTING STUFF -- ////////////
    // //////////////////////////////////////////

    public boolean isRunning() {
        return mLooping;
    }

    public long getIterationNumber() {
        return mIterationNumber;
    }

    public int getFrameDuration() {
        return mFrameDuration;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public long getTime() {
        return uptimeMillis();
    }

}