package com.licheedev.serialworker.worker;

import androidx.annotation.NonNull;
import com.licheedev.serialworker.core.RecvData;
import com.licheedev.serialworker.core.SendData;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * 额外的收发数据操作，发送数据和接收数据会在不同的线程中执行。
 * 发送数据，在单一线程池中执行；
 * 接收数据，会在其他线程运行。
 *
 * @param <S>
 * @param <R>
 */
interface RxSendReceiveX<S extends SendData, R extends RecvData> {

    /**
     * 收发数据，需要处理异常。
     * 发送数据，在单一线程池中执行；接收数据，会在其他线程运行。
     * 没切线程，需自己进行线程调度。
     *
     * @param sendData
     * @return
     */
    @NonNull
    Observable<R> rxSendX(@NonNull S sendData);

    /**
     * 收发数据，并对接收的数据进行类型转换，需要处理异常。
     * 发送数据，在单一线程池中执行；接收数据，会在其他线程运行。
     * 没切线程，需自己进行线程调度。
     *
     * @param sendData 发送的数据
     * @param cast 对接收的数据进行类型转换
     * @return
     */
    @NonNull
    <T extends R> Observable<T> rxSendX(@NonNull S sendData, @NonNull Class<T> cast);

    /**
     * 收发数据，需要处理异常。
     * 发送数据，在单一线程池中执行；接收数据，会在其他线程运行。
     * 已切IO线程（{@link Schedulers#io()}）。
     *
     * @param sendData 发送的数据
     * @return
     */
    @NonNull
    Observable<R> rxSendXOnIo(@NonNull S sendData);

    /**
     * 收发数据，并对接收的数据进行类型转换，需要处理异常。
     * 发送数据，在单一线程池中执行；接收数据，会在其他线程运行。
     * 已切IO线程（{@link Schedulers#io()}）。
     *
     * @param sendData 发送的数据
     * @param cast 对接收的数据进行类型转换
     * @return
     */
    @NonNull
    <T extends R> Observable<T> rxSendXOnIo(@NonNull S sendData, @NonNull Class<T> cast);
}
