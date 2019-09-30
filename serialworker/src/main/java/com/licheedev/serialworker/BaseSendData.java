package com.licheedev.serialworker;

import android.os.SystemClock;
import com.licheedev.serialworker.core.SendData;

/**
 * 发送的数据，封装了发送的时间
 */
public abstract class BaseSendData implements SendData {

    private long mSentTime;

    @Override
    public long getSendTime() {
        return mSentTime;
    }

    @Override
    public void updateSendTime() {
        mSentTime = SystemClock.elapsedRealtime();
    }
}
