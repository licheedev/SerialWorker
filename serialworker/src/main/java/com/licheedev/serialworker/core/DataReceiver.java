package com.licheedev.serialworker.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 数据接收器，用来处理接收到的数据，根据协议，进行分包、并包等操作
 */
public interface DataReceiver<T> extends Cloneable {

    /**
     * 当接收到数据时被调用，在这里根据协议进行分包、并包等操作；
     *
     * @param validData 用来缓存收到的有效数据的容器
     * @param bytes 接收到的数据所在的缓存
     * @param offset 收到数据在缓存中的开始位置
     * @param length 收到数据的长度
     * @return 表示至少收到一个[有效]的数据包，根据协议而定
     */
    void onReceive(@NonNull ValidData validData, @NonNull byte[] bytes, int offset, int length);

    /**
     * 把收到的有效数据转换成特定的数据类型
     *
     * @param allPack 完整的数据包
     * @return
     */
    @Nullable
    T adaptReceive(@NonNull byte[] allPack);

    /**
     * 清除缓存
     */
    void resetCache();
}
