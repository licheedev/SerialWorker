package com.licheedev.serialworker.worker;

import androidx.annotation.NonNull;
import com.licheedev.serialworker.core.RecvData;
import com.licheedev.serialworker.core.SendData;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.Callable;

/**
 * {@link RxRs232SerialWorker}的增强版。
 * {@link RxRs232SerialWorker}中，请求和应答，是在同一线程中处理的。必须等待前一条命令请求和应答完成，才能处理下一条命令。
 * {@link RxRs232SerialWorkerX}中的带“X”的方法，发送数据会在单一线程中处理（发送是串行的），接收数据则在不同的线程中处理。
 *
 * @param <S>
 * @param <R>
 */
public abstract class RxRs232SerialWorkerX<S extends SendData, R extends RecvData>
    extends Rs232SerialWorkerX<S, R> implements RxSendReceiveX<S, R> {

    @NonNull
    @Override
    public Observable<R> rxSendX(@NonNull final S sendData) {
        return RxTool.getRxObservable(new Callable<R>() {
            @Override
            public R call() throws Exception {
                return callOnReceiveThread(rawSendXCallable(sendData));
            }
        });
    }

    @NonNull
    @Override
    public <T extends R> Observable<T> rxSendX(@NonNull S sendData, @NonNull Class<T> cast) {
        return rxSendX(sendData).cast(cast);
    }

    @NonNull
    @Override
    public Observable<R> rxSendXOnIo(@NonNull S sendData) {
        return RxTool.getRxObservable(rawSendXCallable(sendData)).subscribeOn(Schedulers.io());
    }

    @NonNull
    @Override
    public <T extends R> Observable<T> rxSendXOnIo(@NonNull S sendData, @NonNull Class<T> cast) {
        return rxSendXOnIo(sendData).cast(cast);
    }
}
