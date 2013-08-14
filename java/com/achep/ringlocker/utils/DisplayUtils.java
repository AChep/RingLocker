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
