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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.widget.TextView;

import com.achep.ringlocker.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LockerActivity extends Activity {

    private TextView mTimeView;
    private TextView mDateView;

    private Calendar mCalendar;
    private ContentObserver mFormatChangeObserver;
    private boolean mAttached;

    /* called by system on minute ticks */
    private final Handler mHandler = new Handler();
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                mCalendar = Calendar.getInstance();
            }
            // Post a runnable to avoid blocking the broadcast.
            mHandler.post(new Runnable() {
                public void run() {
                    updateClock();
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locker);

        mTimeView = (TextView) findViewById(R.id.time);
        mDateView = (TextView) findViewById(R.id.date);

        final LockerSurfaceView lv = (LockerSurfaceView) findViewById(R.id.locker);
        lv.setOnUnlockListener(new LockerSurfaceView.OnUnlockListener() {
            @Override
            public void unlock() {
                finish();
            }

            @Override
            public void progress(float value) {
                float alpha = 1 - value * 2;
                if (alpha < 0) alpha = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mTimeView.setAlpha(alpha);
                    mDateView.setAlpha(alpha);
                } else {
                    final int color = Color.argb((int) (255 * alpha), 255, 255, 255);
                    mTimeView.setTextColor(color);
                    mDateView.setTextColor(color);
                }
            }
        });

        mCalendar = Calendar.getInstance();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mAttached) {
            return;
        }
        mAttached = true;

		/* monitor time ticks, time changed, timezone */
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        registerReceiver(mIntentReceiver, filter);

		/* monitor 12/24-hour display preference */
        mFormatChangeObserver = new FormatChangeObserver();
        getContentResolver().registerContentObserver(
                Settings.System.CONTENT_URI, true, mFormatChangeObserver);

        updateClock();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (!mAttached)
            return;
        mAttached = false;

        unregisterReceiver(mIntentReceiver);
        getContentResolver().unregisterContentObserver(
                mFormatChangeObserver);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return true;
    }

    private class FormatChangeObserver extends ContentObserver {
        public FormatChangeObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            updateClock();
        }
    }

    private void updateClock() {
        mCalendar.setTimeInMillis(System.currentTimeMillis());

        boolean hour24Mode = get24HourMode();
        int mins = mCalendar.get(Calendar.MINUTE);
        int hour = mCalendar.get(Calendar.HOUR_OF_DAY);

        mTimeView.setText((hour > 12 && !hour24Mode ? hour - 12 : hour) + ":" + (mins > 9 ? mins : "0" + mins));
        mDateView.setText(new SimpleDateFormat("EEE, d MMMM").format(
                mCalendar.getTime()).toUpperCase());
    }

    private boolean get24HourMode() {
        return DateFormat.is24HourFormat(this);
    }

}
