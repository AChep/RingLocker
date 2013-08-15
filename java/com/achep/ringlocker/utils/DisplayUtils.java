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

package com.achep.ringlocker.utils;

import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by achep on 02.07.13.
 */
public class DisplayUtils {

    public static final float DEFAULT_FRAME_RATE = 60f;

    public static float getRefreshRate(Context context) {
        return getRefreshRate(
                ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                        .getDefaultDisplay());
    }

    public static float getRefreshRate(Display display) {
        final float refreshRate = display.getRefreshRate();
        return refreshRate < 10f ? DEFAULT_FRAME_RATE : refreshRate;
    }

}
