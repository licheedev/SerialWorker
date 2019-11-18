package com.licheedev.serialworker.worker;

import android.support.annotation.Nullable;
import com.licheedev.serialworker.core.Callback;
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
public interface SendReceiveX<S extends SendData, R extends RecvData> {

    /**
     * 发送并接收数据，会阻塞调用的线程，需要处理异常。
     * 发送数据，在单一线程池中执行；接收数据，会在其他线程运行。
     *
     * @param sendData 发送的数据
     * @return
     * @throws Exception
     */
    R sendX(S sendData) throws Exception;

    /**
     * 发送并接收数据，会阻塞调用的线程，不会抛出异常。
     * 发送数据，在单一线程池中执行；接收数据，会在其他线程运行。
     *
     * @param sendData
     * @return
     */
    R sendXNoThrow(S sendData);

    /**
     * 异步发送数据。
     * 发送数据，在单一线程池中执行；接收数据，会在其他线程运行。
     *
     * @param sendData 发送的数据
     * @param callback 回调
     */
    void sendX(S sendData, @Nullable Callback<R> callback);

    /**
     * 异步发送数据并进行类型转换。
     * 发送数据，在单一线程池中执行；接收数据，会在其他线程运行。
     *
     * @param sendData 发送的数据
     * @param cast 接收数据的准确类型
     * @param callback 回调
     * @param <T> 接收数据的准确类型
     */
    <T extends R> void sendX(S sendData, Class<T> cast, @Nullable Callback<T> callback);

    /**
     * 收发数据，需要处理异常。
     * 发送数据，在单一线程池中执行；接收数据，会在其他线程运行。
     * 没切线程，需自己进行线程调度。
     *
     * @param sendData
     * @return
     */
    Observable<R> rxSendX(S sendData);

    /**
     * 收发数据，并对接收的数据进行类型转换，需要处理异常。
     * 发送数据，在单一线程池中执行；接收数据，会在其他线程运行。
     * 没切线程，需自己进行线程调度。
     *
     * @param sendData 发送的数据
     * @param cast 对接收的数据进行类型转换
     * @return
     */
    <T extends R> Observable<T> rxSendX(S sendData, Class<T> cast);

    /**
     * 收发数据，需要处理异常。
     * 发送数据，在单一线程池中执行；接收数据，会在其他线程运行。
     * 已切IO线程（{@link Schedulers#io()}）。
     *
     * @param sendData 发送的数据
     * @return
     */
    Observable<R> rxSendXOnIo(S sendData);

    /**
     * 收发数据，并对接收的数据进行类型转换，需要处理异常。
     * 发送数据，在单一线程池中执行；接收数据，会在其他线程运行。
     * 已切IO线程（{@link Schedulers#io()}）。
     *
     * @param sendData 发送的数据
     * @param cast 对接收的数据进行类型转换
     * @return
     */
    <T extends R> Observable<T> rxSendXOnIo(S sendData, Class<T> cast);
}
