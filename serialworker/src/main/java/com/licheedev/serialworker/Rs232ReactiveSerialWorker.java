package com.licheedev.serialworker;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.licheedev.serialworker.core.DataReceiver;
import com.licheedev.serialworker.core.Reactivie;
import com.licheedev.serialworker.core.RecvData;
import com.licheedev.serialworker.core.SendData;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * 用于全双工的rs232设备，但是与{@link Rs232SerialWorker}不同的时。
 * 此类实现了，上位机发送命令后，会同步返回对应的相应数据，同时允许设备主动上报心跳、状态等数据
 *
 * @param <S> 发送的数据类型
 * @param <R> 接收的数据类型
 */
public abstract class Rs232ReactiveSerialWorker<S extends SendData, R extends RecvData>
    extends BaseSerialWorker<DataReceiver<R>> implements Reactivie<S, R> {

    private static final String TAG = "Rs232ReactiveSerialWorker";

    // 保存一个接收器备用
    private final DataReceiver<R> mDefaultReceiver;

    private long mTimeout;

    private WaitRoom mWaitRoom;

    public Rs232ReactiveSerialWorker() {
        // 获取一个备用
        mDefaultReceiver = getReceiver();
    }

    /**
     * 获取超时
     *
     * @return
     */
    @Override
    public long getTimeout() {
        return mTimeout;
    }

    /**
     * 设置超时
     *
     * @param timeout
     */
    @Override
    public void setTimeout(long timeout) {
        mTimeout = timeout;
    }

    /**
     * 等待响应数据
     */
    private class WaitRoom {

        private final S mSendData;
        private R mResponse;

        WaitRoom(S sendData) {
            mSendData = sendData;
        }

        synchronized void setResponse(R response) {
            // 检查一下响应
            if (mResponse != null) {
                return;
            }

            if (isMyResponse(mSendData, response)) {
                mResponse = response;
                // 收到后，就不要等了
                notify();
            }
        }

        synchronized R getResponse(long timeout) {
            // 先检查
            if (mResponse != null) {
                return mResponse;
            }
            // 等一段时间
            waitNoThrow(timeout);
            return mResponse;
        }

        private void waitNoThrow(long timeout) {
            try {
                wait(timeout);
            } catch (InterruptedException e) {
                // 无视
            }
        }
    }

    /**
     * 判断收到的数据是否未所发送的命令的响应
     *
     * @param sendData 发送的数据
     * @param recvData 接收的数据
     * @return
     */
    protected abstract boolean isMyResponse(S sendData, R recvData);

    /**
     * 在当前线程发送数据
     *
     * @param sendData
     */
    private R sendOnCurrentThread(final S sendData) throws IOException, TimeoutException {

        mWaitRoom = new WaitRoom(sendData);

        byte[] bytes = sendData.toBytes();
        // 更新发送时间
        sendData.updateSendTime();
        // 发送
        sendOnCurrentThread(bytes, 0, bytes.length);
        // 等待数据返回
        R response = mWaitRoom.getResponse(getTimeout());
        if (response == null) {
            throw new TimeoutException("RS232 timeout，send=" + sendData);
        }
        return response;
    }

    @Override
    @CallSuper
    public void onReceiveValidData(byte[] allPack, Object... other) {
        // 封装数据
        R receive = mDefaultReceiver.adaptReceive(allPack, other);

        if (receive != null) {
            mWaitRoom.setResponse(receive);
            // 再把数据暴露给子类
            onReceiveValidData(receive);
        }
    }

    /**
     * 收发数据，同步，会阻塞当前线程；
     * 可能会抛出异常
     *
     * @param sendData
     * @return 响应的数据
     */
    @Override
    public @NonNull
    R send(final S sendData) throws ExecutionException, InterruptedException {

        Callable<R> callable = new Callable<R>() {
            @Override
            public R call() throws Exception {
                return sendOnCurrentThread(sendData);
            }
        };
        // 指定在串口线程池发送
        return mSerialExecutor.submit(callable).get();
    }

    /**
     * 收发数据，同步，会阻塞当前线程；
     * 已try-catch
     *
     * @param sendData
     * @return 响应的数据;返回null表示超时了
     */
    @Override
    public @Nullable
    R sendNoThrow(final S sendData) {
        try {
            return send(sendData);
        } catch (Throwable e) {
            //LogPlus.w(TAG, e);
            return null;
        }
    }

    /**
     * Rx发送数据源
     *
     * @param sendData
     * @return
     */
    @NonNull
    private ObservableOnSubscribe<R> getRxSendSource(final S sendData) {
        return new ObservableOnSubscribe<R>() {
            @Override
            public void subscribe(ObservableEmitter<R> emitter) throws Exception {

                boolean terminated = false;
                try {
                    R send = send(sendData);
                    if (!emitter.isDisposed()) {
                        terminated = true;
                        emitter.onNext(send);
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
        };
    }

    /**
     * 收发数据，需要处理异常；
     * 没切线程，需自己进行线程调度
     *
     * @param sendData
     * @return
     */
    @Override
    public Observable<R> rxSend(final S sendData) {

        return Observable.create(getRxSendSource(sendData));
    }

    /**
     * 收发数据，需要处理异常；
     * 已切IO线程（{@link Schedulers#io()}）
     *
     * @param sendData
     * @return
     */
    @Override
    public Observable<R> rxSendOnIo(final S sendData) {
        return rxSend(sendData).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<R> rxSendNoThrow(final S sendData) {

        return Observable.fromCallable(new Callable<R>() {
            @Override
            public R call() throws Exception {
                R r = sendNoThrow(sendData);
                if (r == null) {
                    return returnItemWhenTimeout();
                }
                return r;
            }
        });
    }

    /**
     * @param sendData
     * @return
     */
    @Override
    public Observable<R> rxSendNoThrowOnIo(final S sendData) {
        return rxSendNoThrow(sendData).subscribeOn(Schedulers.io());
    }

    /**
     * 【慎用】异步发送数据，不会阻塞当前线程
     *
     * @param sendData
     */
    public void asyncSend(final S sendData) {
        try {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] bytes = sendData.toBytes();
                        // 更新发送时间
                        sendData.updateSendTime();
                        // 发送
                        sendOnCurrentThread(bytes, 0, bytes.length);
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                }
            };
            mSerialExecutor.execute(runnable);
        } catch (Exception e) {
            //LogPlus.w(TAG, e);
        }
    }
}
