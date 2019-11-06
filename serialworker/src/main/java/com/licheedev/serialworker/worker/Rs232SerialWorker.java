package com.licheedev.serialworker.worker;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.licheedev.serialworker.core.Callback;
import com.licheedev.serialworker.core.DataReceiver;
import com.licheedev.serialworker.core.OpenSerialException;
import com.licheedev.serialworker.core.RecvData;
import com.licheedev.serialworker.core.SendData;
import com.licheedev.serialworker.core.ValidData;
import com.licheedev.serialworker.core.WaitRoom;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;

public abstract class Rs232SerialWorker<S extends SendData, R extends RecvData>
    extends BaseSerialWorker implements SendReceive<S, R> {

    public static final String SERIAL_PORT_RECEIVES_DATA_TIMEOUT =
        "SerialPort receives data timeout!";
    private final List<WaitRoom<R>> mWaitRooms;
    private long mTimeout = 2000L;

    public Rs232SerialWorker() {
        mWaitRooms = new CopyOnWriteArrayList<>();
    }

    @Override
    public void handleValidData(ValidData validData, DataReceiver receiver) {
        ArrayList<byte[]> all = validData.getAll();
        for (byte[] bytes : all) {
            R r = (R) receiver.adaptReceive(bytes);
            if (r != null) {
                Iterator<WaitRoom<R>> iterator = mWaitRooms.iterator();
                while (iterator.hasNext()) {
                    try {
                        iterator.next().putResponse(r);
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                }
                onReceiveData(r);
            }
        }
    }

    @Override
    public void notifyRunningReceive(boolean running) {
        Iterator<WaitRoom<R>> iterator = mWaitRooms.iterator();
        while (iterator.hasNext()) {
            try {
                iterator.next().notifyRunningReceive(running);
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }

    @Override
    public void onReceiveData(byte[] receiveBuffer, int offset, int length) {
        // ignore
    }

    @Override
    public void setTimeout(long millis) {
        mTimeout = millis;
    }

    @Override
    public long getTimeout() {
        return mTimeout;
    }

    /**
     * Rx发送数据源
     *
     * @return
     */
    private <T> Observable<T> getRxObservable(final Callable<T> callable) {

        return Observable.create(new ObservableOnSubscribe<T>() {
            @Override
            public void subscribe(ObservableEmitter<T> emitter) throws Exception {
                boolean terminated = false;
                try {
                    T t = callOnSerialThread(callable);
                    if (!emitter.isDisposed()) {
                        terminated = true;
                        emitter.onNext(t);
                        emitter.onComplete();
                    }
                } catch (Throwable t) {
                    if (terminated) {
                        RxJavaPlugins.onError(t);
                    } else if (!emitter.isDisposed()) {
                        try {
                            emitter.onError(t);
                        } catch (Throwable inner) {
                            RxJavaPlugins.onError(new CompositeException(t, inner));
                        }
                    }
                }
            }
        });
    }

    /**
     * 在当前线程发送数据
     *
     * @param sendData
     */
    protected R rawSend(final S sendData, long timeout) throws IOException, OpenSerialException {

        SingleWaitRoom<S, R> waitRoom = null;
        R response = null;
        try {
            if (timeout > 0) {
                waitRoom = new SingleWaitRoom<>(this, sendData);
                mWaitRooms.add(waitRoom);
            }
            byte[] bytes = sendData.toBytes();
            // 更新发送时间
            sendData.updateSendTime();
            // 发送数据
            rawSend(bytes, 0, bytes.length);
            if (waitRoom != null) {
                response = waitRoom.getResponse(timeout);
            }
        } finally {
            if (waitRoom != null) {
                mWaitRooms.remove(waitRoom);
            }
        }
        return response;
    }

    /**
     * 发送数据，没收到数据会抛出超时异常
     *
     * @param sendData
     * @param timeout
     * @return
     * @throws TimeoutException
     * @throws IOException
     */
    protected R rawSendNoNull(final S sendData, long timeout)
        throws TimeoutException, IOException, OpenSerialException {
        R r = rawSend(sendData, timeout);
        if (r == null) {
            throw new TimeoutException(SERIAL_PORT_RECEIVES_DATA_TIMEOUT);
        }
        return r;
    }

    @NonNull
    private Callable<R> rawSendCallable(final S sendData, final long timeout) {
        return new Callable<R>() {
            @Override
            public R call() throws Exception {
                return rawSend(sendData, timeout);
            }
        };
    }

    @NonNull
    private Callable<R> rawSendNoNullCallable(final S sendData, final long timeout) {
        return new Callable<R>() {
            @Override
            public R call() throws Exception {
                return rawSendNoNull(sendData, timeout);
            }
        };
    }

    @Override
    public RecvData syncSend(S sendData) throws Exception {
        return callOnSerialThread(rawSendNoNullCallable(sendData, getTimeout()));
    }

    @Override
    public RecvData syncSendNoThrow(S sendData) {
        try {
            return syncSend(sendData);
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }

    @Override
    public void syncSendOnly(S sendData) throws Exception {
        callOnSerialThread(rawSendCallable(sendData, 0));
    }

    @Override
    public void syncSendOnlyNoThrow(S sendData) {
        try {
            syncSendOnly(sendData);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }



    @Override
    public void send(final S sendData, @Nullable final Callback<R> callback) {
        asyncCallOnSerialThread(new Callable<R>() {
            @Override
            public R call() throws Exception {
                return rawSendNoNull(sendData, getTimeout());
            }
        }, callback);
    }

    @Override
    public <T extends R> void send(final S sendData, Class<T> cast,
        @Nullable final Callback<T> callback) {

        asyncCallOnSerialThread(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return (T) rawSendNoNull(sendData, getTimeout());
            }
        }, callback);
    }

    @Override
    public void sendOnly(final S sendData, @Nullable final Callback<Void> callback) {

        asyncCallOnSerialThread(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                rawSend(sendData, 0);
                return null;
            }
        }, callback);
    }

    @Override
    public Observable<R> rxSend(S sendData) {
        return getRxObservable(rawSendNoNullCallable(sendData, getTimeout()));
    }

    @Override
    public <T extends R> Observable<T> rxSend(S sendData, Class<T> cast) {
        return rxSend(sendData).cast(cast);
    }

    @Override
    public Observable<R> rxSendOnIo(S sendData) {
        return rxSend(sendData).subscribeOn(Schedulers.io());
    }

    @Override
    public <T extends R> Observable<T> rxSendOnIo(S sendData, Class<T> cast) {
        return rxSendOnIo(sendData).cast(cast);
    }
}
