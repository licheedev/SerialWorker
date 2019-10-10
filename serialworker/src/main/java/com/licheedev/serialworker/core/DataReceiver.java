package com.licheedev.serialworker.core;

/**
 * 数据接收器，用来处理接收到的数据，根据协议，进行分包、并包等操作
 */
public interface DataReceiver<T> {

    /**
     * 当接收到数据时被调用，在这里根据协议进行分包、并包等操作；
     * 如果判定收到了有效数据，则需要调用{@link SerialWorker#onReceiveValidData(DataReceiver, byte[], Object...)}
     * 方法，尝试把数据传递出去
     *
     * @param serialWorker 接受到有效数据时，
     * 用来调用{@link SerialWorker#onReceiveValidData(DataReceiver, byte[], Object...)}
     * @param data 接收到的数据所在的缓存
     * @param offset 收到数据在缓存中的开始位置
     * @param length 收到数据的长度
     * @return 表示至少收到一个[有效]的数据包，根据协议而定
     */
    boolean onReceive(SerialWorker serialWorker, byte[] data, int offset, int length);

    /**
     * 把{@link #onReceive(SerialWorker, byte[], int, int)}收到的数据封装成特定数据
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
