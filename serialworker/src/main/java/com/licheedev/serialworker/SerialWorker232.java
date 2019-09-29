package com.licheedev.serialworker;

import com.licheedev.myutils.LogPlus;
import com.licheedev.serialworker.core.DataReceiver;
import com.licheedev.serialworker.core.SendData;
import io.reactivex.Observable;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * 用于全双工的rs232
 *
 * @param <S> 发送的数据类型
 * @param <DR> 数据接收器
 */
public abstract class SerialWorker232<S extends SendData, DR extends DataReceiver>
    extends BaseSerialWorker<DR> {

    private static final String TAG = "SerialWorker232";

    /**
     * 在当前线程发送数据
     *
     * @param sendCommand
     */
    private void sendOnCurrentThread(final S sendCommand) throws IOException {

        byte[] bytes = sendCommand.toBytes();
        // 更新发送时间
        sendCommand.updateSendTime();
        sendOnCurrentThread(bytes, 0, bytes.length);
    }

    /**
     * 发送数据，同步，会阻塞当前线程；
     * 可能会抛出异常
     *
     * @param sendCommand
     */
    public void sendOrThrow(final S sendCommand) throws Exception {
        Callable callable = new Callable() {
            @Override
            public Object call() throws Exception {
                sendOnCurrentThread(sendCommand);
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
     * @param sendCommand
     */
    public void send(final S sendCommand) {
        try {
            sendOrThrow(sendCommand);
        } catch (Exception e) {
            LogPlus.w(TAG, e);
        }
    }

    /**
     * 异步发送数据，不会阻塞当前线程
     *
     * @param sendCommand
     */
    public void asyncSend(final S sendCommand) {
        try {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        sendOnCurrentThread(sendCommand);
                    } catch (IOException e) {
                        //e.printStackTrace();
                    }
                }
            };
            mSerialExecutor.execute(runnable);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    /**
     * 发送数据，没切线程，默认发射true，需要处理异常
     *
     * @param sendCommand
     * @return
     */
    public Observable<Boolean> rxSendOrThrow(final S sendCommand) {

        return Observable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                sendOrThrow(sendCommand);
                return Boolean.TRUE;
            }
        });
    }

    /**
     * 发送数据，没切线程，默认发射true，内部已try-catch
     *
     * @param sendCommand
     * @return
     */
    public Observable<Boolean> rxSend(final S sendCommand) {

        return Observable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                send(sendCommand);
                return Boolean.TRUE;
            }
        });
    }
}
