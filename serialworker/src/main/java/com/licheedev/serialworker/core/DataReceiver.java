package com.licheedev.serialworker.core;

/**
 * 数据接收器，用来处理接收到的数据，根据协议，进行分包、并包等操作
 */
public interface DataReceiver<T> {

    /**
     * 当接收到数据时被调用，在这里根据协议进行分包、并包等操作；
     * 如果判定收到了有效数据，则需要调用{@link #onReceiveValidData(byte[], Object...)}方法，尝试把数据传递出去
     *
     * @param data 接收到的数据所在的缓存
     * @param offset 收到数据在缓存中的开始位置
     * @param length 收到数据的长度
     * @return 表示至少收到一个[有效]的数据包，根据协议而定
     */
    boolean onReceive(byte[] data, int offset, int length);

    /**
     * 通常在这里调用{@link SerialWorker#onReceiveValidData(byte[], Object...)}，通知收到了有效数据
     *
     * @param allPack 完整的数据包
     * @param other 其他附加数据，如命令码CMD、除开帧头帧尾等附加位的真正数据DATAN
     */
    void onReceiveValidData(byte[] allPack, Object... other);

    /**
     * 把{@link #onReceiveValidData(byte[], Object...)}收到的数据封装成特定数据
     *
     * @param allPack 完整的数据包
     * @param other 其他附加数据，如命令码CMD、除开帧头帧尾等附加位的真正数据DATAN
     * @return
     */
    T adaptReceive(byte[] allPack, Object... other);

    /**
     * 清除缓存
     */
    void resetCache();
}
