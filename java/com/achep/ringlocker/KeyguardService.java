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

package com.achep.ringlocker;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;

import com.achep.ringlocker.locker.LockerActivity;

/**
 * Created by achep on 13.08.13.
 */
public class KeyguardService extends Service {

    private KeyguardManager.KeyguardLock mKeyguardLock;
    private Intent mIntent;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            TelephonyManager ts = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (ts.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                startActivity(mIntent);
            }
        }

    };

    @Override
    public void onCreate() {
        mIntent = new Intent(this, LockerActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mKeyguardLock = ((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE)).newKeyguardLock(getString(R.string.app_name));
        mKeyguardLock.disableKeyguard();

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        intentFilter.setPriority(Integer.MAX_VALUE);
        registerReceiver(mReceiver, intentFilter);

        final Notification notification = new Notification();
        notification.setLatestEventInfo(this, getString(R.string.app_name), "is running!",
                PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0));
        notification.flags |= Notification.FLAG_NO_CLEAR;

        startForeground(1337, notification);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        mKeyguardLock.reenableKeyguard();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
