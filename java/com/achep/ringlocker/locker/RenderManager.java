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

import com.achep.Looper;

public final class RenderManager extends Looper {

    private final OnRenderRequestListener mListener;
    private boolean mDrawRequired;

    public interface OnRenderRequestListener {

        public void OnRenderRequest();

    }

    public RenderManager(OnRenderRequestListener onRenderRequestListener,
                         float frameRate) {
        super(frameRate);

        mListener = onRenderRequestListener;
    }

    @Override
    protected boolean onLoop(final long uptime) {
        if (mDrawRequired) {
            mDrawRequired = false;
            render();
            return CONTINUE_LOOPING;
        }
        return STOP_LOOPING;
    }

    private void render() {
        mListener.OnRenderRequest();
    }

    public void requestRender() {
        mDrawRequired = true;
        startDelayed(2);
    }

}