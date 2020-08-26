package com.licheedev.serialworker.worker;

import androidx.annotation.Nullable;
import com.licheedev.serialworker.core.Callback;
import com.licheedev.serialworker.core.OpenSerialException;
import com.licheedev.serialworker.core.RecvData;
import com.licheedev.serialworker.core.SendData;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

/**
 * {@link Rs232SerialWorker}的增强版。
 * {@link Rs232SerialWorker}中，请求和应答，是在同一线程中处理的。必须等待前一条命令请求和应答完成，才能处理下一条命令。
 * {@link Rs232SerialWorkerX}中的带“X”的方法，发送数据会在单一线程中处理（发送是串行的），接收数据则在不同的线程中处理。
 *
 * @param <S>
 * @param <R>
 */
public abstract class Rs232SerialWorkerX<S extends SendData, R extends RecvData>
    extends Rs232SerialWorker<S, R> implements SendReceiveX<S, R> {

    private final ExecutorService mReceiveExecutor;

    public Rs232SerialWorkerX() {
        mReceiveExecutor = Executors.newCachedThreadPool();
    }

    /**
     * 在当前线程发送数据
     *
     * @param sendData
     */
    protected R rawSendX(final S sendData)
        throws IOException, OpenSerialException, InterruptedException, ExecutionException,
        TimeoutException {

        SingleWaitRoom<S, R> waitRoom = new SingleWaitRoom<>(this, sendData);
        mWaitRooms.add(waitRoom);
        R response;
        try {
            callOnSerialThread(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    byte[] bytes = sendData.toBytes();
                    // 更新发送时间
                    sendData.updateSendTime();
                    // 发送数据
                    rawSend(bytes, 0, bytes.length);
                    return null;
                }
            });
            response = waitRoom.getResponse(getTimeout());
        } finally {
            mWaitRooms.remove(waitRoom);
        }

        if (response == null) {
            throw new TimeoutException(SERIAL_PORT_RECEIVES_DATA_TIMEOUT);
        }

        return response;
    }

    protected Callable<R> rawSendXCallable(final S sendData) {
        return new Callable<R>() {
            @Override
            public R call() throws Exception {
                return rawSendX(sendData);
            }
        };
    }

    protected <T> T callOnReceiveThread(Callable<T> callable)
        throws InterruptedException, ExecutionException, OpenSerialException, IOException,
        TimeoutException {

        return callOnExecutor(mReceiveExecutor, callable);
    }

    protected void asyncCallOnReceiveThread(final Callable<?> callable, final Callback callback) {
        asyncCallOnExecutor(mReceiveExecutor, callable, callback);
    }

    @Override
    public R sendX(S sendData) throws Exception {
        return callOnReceiveThread(rawSendXCallable(sendData));
    }

    @Override
    public R sendXNoThrow(S sendData) {
        try {
            return sendX(sendData);
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }

    @Override
    public void sendX(S sendData, @Nullable Callback<R> callback) {
        asyncCallOnReceiveThread(rawSendXCallable(sendData), callback);
    }

    @Override
    public <T extends R> void sendX(final S sendData, Class<T> cast,
        @Nullable Callback<T> callback) {
        asyncCallOnReceiveThread(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return (T) rawSendX(sendData);
            }
        }, callback);
    }


}
