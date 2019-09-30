package com.licheedev.serialworker;

import android.os.SystemClock;
import com.licheedev.serialworker.core.RecvData;

/**
 * 接收的数据，封装了接收的时间
 */
public abstract class BaseRecvData implements RecvData {

    private long mRecvTime;

    public BaseRecvData() {
        mRecvTime = SystemClock.elapsedRealtime();
    }

    @Override
    public long getRecvTime() {
        return mRecvTime;
    }

    //@Override
    //public void updateRecvTime() {
    //    mRecvTime = SystemClock.elapsedRealtime();
    //}
}
