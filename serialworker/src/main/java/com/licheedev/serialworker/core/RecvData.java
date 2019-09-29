package com.licheedev.serialworker.core;

import android.os.SystemClock;

/**
 * 表示接收到的数据
 */
public interface RecvData {

    /**
     * 接收数据的时间
     *
     * @return
     */
    long getRecvTime();

    /**
     * 设置接收数据的时间，建议使用{@link SystemClock#elapsedRealtime()}
     */
    void updateRecvTime();
}
