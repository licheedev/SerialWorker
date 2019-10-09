package com.licheedev.serialworker.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * 响应式的时候，要实现的接口
 *
 * @param <S>
 * @param <R>
 */
public interface Reactivie<S extends SendData, R extends RecvData> {

    /**
     * 获取超时
     *
     * @return
     */
    long getTimeout();

    /**
     * 设置超时
     *
     * @param timeout
     */
    void setTimeout(long timeout);

    /**
     * 收到有效的数据
     *
     * @param receive
     */
    void onReceiveValidData(@NonNull R receive);

    /**
     * 在超时的时候，返回的默认项，一般弄一个单例的{@link RecvData}就好了
     *
     * @return
     */
    R returnItemWhenTimeout();

    /**
     * 收发数据，同步，会阻塞当前线程；
     * 可能会抛出异常
     *
     * @param sendData
     * @return 响应的数据
     */
    @NonNull
    R send(final S sendData) throws Throwable;

    /**
     * 收发数据，同步，会阻塞当前线程；
     * 已try-catch
     *
     * @param sendData
     * @return 响应的数据;返回null表示超时了
     */
    @Nullable
    R sendNoThrow(final S sendData);

    /**
     * 收发数据，需要处理异常；
     * 没切线程，需自己进行线程调度
     *
     * @param sendData
     * @return
     */
    Observable<R> rxSend(final S sendData);

    /**
     * 收发数据，需要处理异常；
     * 已切IO线程（{@link Schedulers#io()}）
     *
     * @param sendData
     * @return
     */
    Observable<R> rxSendOnIo(final S sendData);

    /**
     * 收发数据，内部已try-catch，出现异常的时候
     *
     * @param sendData
     * @return
     */
    Observable<R> rxSendNoThrow(final S sendData);

    /**
     * @param sendData
     * @return
     */
    Observable<R> rxSendNoThrowOnIo(final S sendData);
}
