package com.licheedev.serialworker.worker;

import androidx.annotation.NonNull;
import com.licheedev.serialworker.core.RecvData;
import com.licheedev.serialworker.core.SendData;
import io.reactivex.rxjava3.core.Observable;

/**
 * 收发数据操作
 *
 * @param <S>
 * @param <R>
 */
interface RxSendReceive<S extends SendData, R extends RecvData> {

    /**
     * 收发数据，需要处理异常；
     * 收发数据在同一线程中执行；
     * 没切线程，需自己进行线程调度
     *
     * @param sendData
     * @return
     * @see RxSendReceive#syncSend(SendData)
     */
    @NonNull
    Observable<R> rxSend(@NonNull S sendData);

    /**
     * 收发数据，需要处理异常；
     * 收发数据在同一线程中执行；
     * 没切线程，需自己进行线程调度
     *
     * @param sendData 发送的数据
     * @param cast 对接收的数据进行类型转换
     * @return
     * @see RxSendReceive#syncSend(SendData)
     */
    @NonNull
    <T extends R> Observable<T> rxSend(@NonNull S sendData, @NonNull Class<T> cast);

    /**
     * 收发数据，需要处理异常；
     * 收发数据在同一线程中执行；
     * 已切IO线程（{@link Schedulers#io()}）
     *
     * @param sendData 发送的数据
     * @return
     */
    @NonNull
    Observable<R> rxSendOnIo(@NonNull S sendData);

    /**
     * 收发数据，并对接收的数据进行类型转换，需要处理异常；
     * 收发数据在同一线程中执行
     * 已切IO线程（{@link Schedulers#io()}）
     *
     * @param sendData 发送的数据
     * @param cast 对接收的数据进行类型转换
     * @return
     */
    @NonNull
    <T extends R> Observable<T> rxSendOnIo(@NonNull S sendData, @NonNull Class<T> cast);
}
