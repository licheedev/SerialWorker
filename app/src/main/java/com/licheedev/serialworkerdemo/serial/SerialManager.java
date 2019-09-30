package com.licheedev.serialworkerdemo.serial;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.serialport.SerialPort;
import com.licheedev.serialworkerdemo.serial.command.RecvCommand;
import com.licheedev.serialworkerdemo.serial.command.SendCommand;
import com.licheedev.serialworkerdemo.serial.command.recv.Recv5DStatus;
import com.licheedev.serialworkerdemo.serial.command.recv.RecvTimeout;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * 串口管理器
 */
public class SerialManager {

    private static final String TAG = "SerialManager";

    private static volatile SerialManager sManager = null;
    private final HandlerThread mDispatchThread;
    private final Handler mDispatchThreadHandler;

    private final MySerialWorker mMySerialWorker;
    private final Subject<Recv5DStatus> mStateSubject;
    private final Observable<Recv5DStatus> mRxState;

    /**
     * [单例]获取串口管理器
     *
     * @return
     */
    public static SerialManager get() {

        SerialManager manager = sManager;
        if (manager == null) {
            synchronized (SerialManager.class) {
                manager = sManager;
                if (manager == null) {
                    manager = new SerialManager();
                    sManager = manager;
                }
            }
        }
        return manager;
    }

    private SerialManager() {

        // 用来rx接受新的状态消息的
        mStateSubject = PublishSubject.<Recv5DStatus>create().toSerialized();
        // 参考 https://github.com/JakeWharton/RxReplayingShare
        mRxState = mStateSubject.hide();

        mDispatchThread = new HandlerThread("serial-dispatch-thread");
        mDispatchThread.start();
        mDispatchThreadHandler = new Handler(mDispatchThread.getLooper());

        mMySerialWorker = new MySerialWorker(mDispatchThreadHandler);
        // 设置超时
        mMySerialWorker.setTimeout(Protocol.RECEIVE_TIME_OUT);
        // 开启打印日志
        mMySerialWorker.enableLog(true, true);

        mMySerialWorker.setReceiveCallback(new MySerialWorker.ReceiveCallback() {

            @Override
            public void onReceive(RecvCommand recvCommand) {

                switch (recvCommand.getCmd()) {
                    case Protocol.CMD_5D_STATUS_UPDATE:
                        Recv5DStatus recv = (Recv5DStatus) recvCommand;
                        //LogPlus.i("状态更新="+recv);
                        // 发送到rx上面
                        mStateSubject.onNext(recv);
                        // 发送事件
                        //EventBus.getDefault().post(recv);
                        break;
                }
            }
        });
    }

    /**
     * 通过rx来获取状态
     *
     * @return
     */
    public Observable<Recv5DStatus> getRxState() {
        return mRxState;
    }

    /**
     * 打开串口
     *
     * @return
     */
    public SerialPort openSerial(String device) {
        return mMySerialWorker.openSerial(device, Protocol.SERIAL_BAUD_RATE);
    }

    /**
     * 关闭串口
     */
    public void closeSerial() {
        mMySerialWorker.closeSerial();
    }

    /**
     * 释放资源
     */
    public synchronized void release() {
        sManager = null;

        mMySerialWorker.release();
        // 结束
        mStateSubject.onComplete();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mDispatchThread.quitSafely();
        } else {
            mDispatchThread.quit();
        }
    }

    public boolean isDeviceConnected() {
        return mMySerialWorker.getSerialPort() != null;
    }

    /**
     * 异步发送命令
     *
     * @param command
     */
    public void ayncSendCommand(SendCommand command) {
        mMySerialWorker.asyncSend(command);
    }

    /**
     * 同步发送命令，会抛异常
     *
     * @param command
     * @return
     * @throws Exception
     */
    public RecvCommand sendCommand(final SendCommand command) throws Exception {
        return mMySerialWorker.send(command);
    }

    /**
     * 同步发送命令，超时返回{@link RecvTimeout#INST}
     *
     * @param command
     * @return
     */
    public RecvCommand sendCommandNoThrow(final SendCommand command) {
        return mMySerialWorker.sendNoThrow(command);
    }

    /**
     * rx方式发送命令，超时抛出异常。已经切换好线程
     *
     * @param command
     * @return
     */
    public Observable<RecvCommand> rxSendCommand(final SendCommand command) {
        return mMySerialWorker.rxSendOnIo(command);
    }

    /**
     * rx方式发送命令，超时发射{@link RecvTimeout#INST}。已经切换好线程
     *
     * @param command
     * @return
     */
    public Observable<RecvCommand> rxSendCommandNoThrow(final SendCommand command) {
        return mMySerialWorker.rxSendNoThrowOnIo(command);
    }
}
