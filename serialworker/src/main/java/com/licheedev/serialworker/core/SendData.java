package com.licheedev.serialworker.core;

import android.os.SystemClock;

/**
 * 表示发送的数据
 */
public interface SendData {

    /**
     * 所发送的数据的字节数组
     *
     * @return
     */
    byte[] toBytes();

    /**
     * 发送数据的时间
     *
     * @return
     */
    long getSendTime();

    /**
     * 更新发送数据的时间，建议使用{@link SystemClock#elapsedRealtime()}
     */
    void updateSendTime();
}
