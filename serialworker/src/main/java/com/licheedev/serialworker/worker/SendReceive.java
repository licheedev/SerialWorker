package com.licheedev.serialworker.worker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.licheedev.serialworker.core.Callback;
import com.licheedev.serialworker.core.DataReceiver;
import com.licheedev.serialworker.core.RecvData;
import com.licheedev.serialworker.core.SendData;

/**
 * 收发数据操作
 *
 * @param <S>
 * @param <R>
 */
interface SendReceive<S extends SendData, R extends RecvData> {

    /**
     * 判断收到的数据是否未所发送的命令的响应
     *
     * @param sendData 发送的数据
     * @param recvData 接收的数据
     * @return
     */
    boolean isMyResponse(@NonNull S sendData, @NonNull R recvData);

    /**
     * 新建数据接收器
     *
     * @return 尽量new出来，不要复用成员变量
     */
    @Nullable
    DataReceiver<R> newReceiver();

    /**
     * 收到有效的数据
     *
     * @param recvData
     */
    void onReceiveData(@NonNull R recvData);

    /**
     * 获取超时
     *
     * @return
     */
    long getTimeout();

    /**
     * 设置超时
     *
     * @param millis 毫秒
     */
    void setTimeout(long millis);

    /**
     * 同步发送并接收数据，会阻塞调用的线程，需要处理异常；
     * 收发数据在同一线程中执行。
     *
     * @param sendData 发送的数据
     * @return
     * @throws Exception
     */
    R syncSend(@NonNull S sendData) throws Exception;

    /**
     * 同步发送并接收数据，会阻塞调用的线程，不会抛出异常；
     * 收发数据在同一线程中执行。
     *
     * @param sendData
     * @return
     */
    R syncSendNoThrow(@NonNull S sendData);

    /**
     * 仅发送数据，不需要等待接收；会阻塞调用的线程；需要处理异常
     *
     * @param sendData
     */
    void syncSendOnly(@NonNull S sendData) throws Exception;

    /**
     * 仅发送数据，不需要等待接收；会阻塞调用的线程，不会抛出异常
     *
     * @param sendData
     */
    void syncSendOnlyNoThrow(@NonNull S sendData);

    /**
     * 异步发送并接收数据；
     * 收发数据在同一线程中执行。
     *
     * @param sendData 发送的数据
     * @param callback 回调
     */
    void send(@NonNull S sendData, @Nullable Callback<R> callback);

    /**
     * 异步发送数据和接收数据，并进行类型转换；
     * 收发数据在同一线程中执行。
     *
     * @param sendData 发送的数据
     * @param cast 接收数据的准确类型
     * @param callback 回调
     * @param <T> 接收数据的准确类型
     */
    <T extends R> void send(@NonNull S sendData, @NonNull Class<T> cast,
        @Nullable Callback<T> callback);

    /**
     * 仅发送数据，异步，不会阻塞调用的线程
     *
     * @param sendData
     * @param callback
     */
    void sendOnly(@NonNull S sendData, @Nullable Callback<Void> callback);
}
