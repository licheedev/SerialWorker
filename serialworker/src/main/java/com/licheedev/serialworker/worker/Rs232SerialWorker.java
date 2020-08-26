package com.licheedev.serialworker.worker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.licheedev.serialworker.core.Callback;
import com.licheedev.serialworker.core.DataReceiver;
import com.licheedev.serialworker.core.OpenSerialException;
import com.licheedev.serialworker.core.RecvData;
import com.licheedev.serialworker.core.SendData;
import com.licheedev.serialworker.core.ValidData;
import com.licheedev.serialworker.core.WaitRoom;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;

/**
 * 用于全双工的rs232设备。此类的实现是类rs485的，即请求与应答在同一个单一线程中执行。
 * 如果需要请求与应答在不同线程中执行，可以使用{@link Rs232SerialWorkerX}，并使用其中的带“X”方法。
 *
 * @param <S>
 * @param <R>
 * @see Rs232SerialWorkerX
 */
public abstract class Rs232SerialWorker<S extends SendData, R extends RecvData>
    extends BaseSerialWorker implements SendReceive<S, R> {

    public static final String SERIAL_PORT_RECEIVES_DATA_TIMEOUT =
        "SerialPort receives data timeout!";
    protected final List<WaitRoom<R>> mWaitRooms;
    private long mTimeout = 2000L;

    public Rs232SerialWorker() {
        mWaitRooms = new CopyOnWriteArrayList<>();
    }

    @Override
    public void handleValidData(@NonNull ValidData validData, DataReceiver receiver) {
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
    public void onReceiveData(@NonNull byte[] receiveBuffer, int offset, int length) {
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
     * 在当前线程发送数据，并等待接收数据
     *
     * @param sendData 发送的数据
     * @param timeout 接收数据超时，0表示不会等待接收数据
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
    protected Callable<R> rawSendCallable(final S sendData, final long timeout) {
        return new Callable<R>() {
            @Override
            public R call() throws Exception {
                return rawSend(sendData, timeout);
            }
        };
    }

    @NonNull
    protected Callable<R> rawSendNoNullCallable(final S sendData, final long timeout) {
        return new Callable<R>() {
            @Override
            public R call() throws Exception {
                return rawSendNoNull(sendData, timeout);
            }
        };
    }

    @Override
    public R syncSend(@NonNull S sendData) throws Exception {
        return callOnSerialThread(rawSendNoNullCallable(sendData, getTimeout()));
    }

    @Override
    public R syncSendNoThrow(@NonNull S sendData) {
        try {
            return syncSend(sendData);
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }

    @Override
    public void syncSendOnly(@NonNull S sendData) throws Exception {
        callOnSerialThread(rawSendCallable(sendData, 0));
    }

    @Override
    public void syncSendOnlyNoThrow(@NonNull S sendData) {
        try {
            syncSendOnly(sendData);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    @Override
    public void send(@NonNull final S sendData, @Nullable final Callback<R> callback) {
        asyncCallOnSerialThread(rawSendNoNullCallable(sendData, getTimeout()), callback);
    }

    @Override
    public <T extends R> void send(@NonNull final S sendData, @NonNull Class<T> cast,
        @Nullable final Callback<T> callback) {

        asyncCallOnSerialThread(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return (T) rawSendNoNull(sendData, getTimeout());
            }
        }, callback);
    }

    @Override
    public void sendOnly(@NonNull final S sendData, @Nullable final Callback<Void> callback) {

        asyncCallOnSerialThread(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                rawSend(sendData, 0);
                return null;
            }
        }, callback);
    }


}
