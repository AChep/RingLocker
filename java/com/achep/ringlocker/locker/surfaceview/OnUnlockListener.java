package com.achep.ringlocker.locker.surfaceview;

/**
* Created by Artem on 07.09.13.
*/
public interface OnUnlockListener {

    void unlock();

    void progress(float value);

}
