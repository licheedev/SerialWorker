package com.licheedev.serialworker;

import com.licheedev.myutils.LogPlus;
import com.licheedev.serialworker.core.DataReceiver;
import com.licheedev.serialworker.core.SendData;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * 用于全双工的rs232设备,发送命令和接收数据完全异步；
 * 适用于会主动上报心跳、状态的串口设备。
 *
 * @param <S> 发送的数据类型
 * @param <DR> 数据接收器
 */
public abstract class Rs232SerialWorker<S extends SendData, DR extends DataReceiver>
    extends BaseSerialWorker<DR> {

    private static final String TAG = "Rs232SerialWorker";

    public Rs232SerialWorker() {
    }

    /**
     * 在当前线程发送数据
     *
     * @param sendData
     */
    private void sendOnCurrentThread(final S sendData) throws IOException {

        byte[] bytes = sendData.toBytes();
        // 更新发送时间
        sendData.updateSendTime();
        sendOnCurrentThread(bytes, 0, bytes.length);
    }

    /**
     * 发送数据，同步，会阻塞当前线程；
     * 可能会抛出异常
     *
     * @param sendData
     */
    public void send(final S sendData) throws Exception {
        Callable callable = new Callable() {
            @Override
            public Object call() throws Exception {
                sendOnCurrentThread(sendData);
                return null; // 不用管返回值
            }
        };
        // 指定在串口线程池发送
        mSerialExecutor.submit(callable).get();
    }

    /**
     * 发送数据，同步，会阻塞当前线程；
     * 已try-catch
     *
     * @param sendData
     */
    public void sendNoThrow(final S sendData) {
        try {
            send(sendData);
        } catch (Exception e) {
            LogPlus.w(TAG, e);
        }
    }

    /**
     * 异步发送数据，不会阻塞当前线程
     *
     * @param sendData
     */
    public void asyncSend(final S sendData) {
        try {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        sendOnCurrentThread(sendData);
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                }
            };
            mSerialExecutor.execute(runnable);
        } catch (Exception e) {
            LogPlus.w(TAG, e);
        }
    }

    /**
     * 发送数据,需要处理异常;
     * 没切线程，需自己进行线程调度
     *
     * @param sendData
     * @return
     */
    public Observable<Boolean> rxSend(final S sendData) {

        return Observable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                send(sendData);
                return Boolean.TRUE;
            }
        });
    }

    /**
     * 发送数据,需要处理异常；
     * 已切IO线程（{@link Schedulers#io()}）
     *
     * @param sendData
     * @return
     */
    public Observable<Boolean> rxSendOnIo(final S sendData) {

        return rxSend(sendData).subscribeOn(Schedulers.io());
    }

    /**
     * 发送数据，内部已try-catch;
     * 没切线程，需自己进行线程调度
     *
     * @param sendData
     * @return
     */
    public Observable<Boolean> rxSendNoThrow(final S sendData) {

        return Observable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                sendNoThrow(sendData);
                return Boolean.TRUE;
            }
        });
    }

    /**
     * 发送数据，内部已try-catch;
     * 已切IO线程（{@link Schedulers#io()}）
     *
     * @param sendData
     * @return
     */
    public Observable<Boolean> rxSendOnIoNoThrow(final S sendData) {
        return rxSendNoThrow(sendData).subscribeOn(Schedulers.io());
    }
}
