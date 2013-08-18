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

    public static final boolean STOP = true;
    public static final boolean CONTINUE = false;

    private static final byte MSG_MAKE_A_NEW_LOOP = 0;

    private boolean isRunning;
    private long mCurLoopingTime;

    private final int mPeriod;
    private final H mHandler = new H();

    @SuppressLint("HandlerLeak")
    private class H extends Handler {
        public void handleMessage(Message m) {
            if (m.what == MSG_MAKE_A_NEW_LOOP) {
                makeLoop();
            }
        }
    }

    public Looper(final float refreshRate) {
        mPeriod = (int) (1000 / refreshRate);
    }

    // //////////////////////////////////////////
    // /////////// -- CONTROLS -- ///////////////
    // //////////////////////////////////////////

    public void start() {
        startDelayed(0);
    }

    public void startDelayed(final int ms) {
        if (!isRunning) {
            isRunning = true;
            mCurLoopingTime = getTime() + ms;
            sendEmptyMessageToHandler();
        }
    }

    public void stop() {
        if (isRunning) {
            isRunning = false;
            mHandler.removeMessages(MSG_MAKE_A_NEW_LOOP);
        }
    }

    // //////////////////////////////////////////
    // ////////////// -- CORE -- ////////////////
    // //////////////////////////////////////////

    private void makeLoop() {
        if (isRunning) {
            if (onLoop(getTime())) {
                stop();
            } else {
                mCurLoopingTime += mPeriod;
                sendEmptyMessageToHandler();
            }
        }
    }

    private void sendEmptyMessageToHandler() {
        mHandler.sendEmptyMessageAtTime(MSG_MAKE_A_NEW_LOOP, mCurLoopingTime);
    }

    protected abstract boolean onLoop(final long uptime);

    // //////////////////////////////////////////
    // ///////// -- GETTING STUFF -- ////////////
    // //////////////////////////////////////////

    public boolean isRunning() {
        return isRunning;
    }

    public long getTime() {
        return uptimeMillis();
    }

}