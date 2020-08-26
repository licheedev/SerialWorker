package com.licheedev.serialworker.worker;

import androidx.annotation.NonNull;
import com.licheedev.serialworker.core.RecvData;
import com.licheedev.serialworker.core.SendData;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.Callable;

/**
 * 用于全双工的rs232设备。此类的实现是类rs485的，即请求与应答在同一个单一线程中执行。
 * 如果需要请求与应答在不同线程中执行，可以使用{@link RxRs232SerialWorkerX}，并使用其中的带“X”方法。
 *
 * @param <S>
 * @param <R>
 * @see RxRs232SerialWorkerX
 */
public abstract class RxRs232SerialWorker<S extends SendData, R extends RecvData>
    extends Rs232SerialWorker<S, R> implements RxSendReceive<S, R> {

    @NonNull
    @Override
    public Observable<R> rxSend(@NonNull final S sendData) {

        return RxTool.getRxObservable(new Callable<R>() {
            @Override
            public R call() throws Exception {
                return callOnSerialThread(rawSendNoNullCallable(sendData, getTimeout()));
            }
        });
    }

    @NonNull
    @Override
    public <T extends R> Observable<T> rxSend(@NonNull S sendData, @NonNull Class<T> cast) {
        return rxSend(sendData).cast(cast);
    }

    @NonNull
    @Override
    public Observable<R> rxSendOnIo(@NonNull S sendData) {
        return rxSend(sendData).subscribeOn(Schedulers.io());
    }

    @NonNull
    @Override
    public <T extends R> Observable<T> rxSendOnIo(@NonNull S sendData, @NonNull Class<T> cast) {
        return rxSendOnIo(sendData).cast(cast);
    }
}
