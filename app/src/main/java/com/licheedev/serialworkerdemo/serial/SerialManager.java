package com.licheedev.serialworkerdemo.serial;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.serialport.SerialPort;
import com.licheedev.myutils.LogPlus;
import com.licheedev.serialworker.core.Callback;
import com.licheedev.serialworker.core.SerialWorker;
import com.licheedev.serialworkerdemo.serial.command.RecvCommand;
import com.licheedev.serialworkerdemo.serial.command.SendCommand;
import com.licheedev.serialworkerdemo.serial.command.recv.Recv5DStatus;
import com.licheedev.serialworkerdemo.serial.command.recv.RecvA4OpenDoor;
import com.licheedev.serialworkerdemo.serial.command.recv.RecvSetReadTemp;
import com.licheedev.serialworkerdemo.serial.command.send.SendA4OpenDoor;
import com.licheedev.serialworkerdemo.serial.command.send.SendA8SetTemp;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import org.greenrobot.eventbus.EventBus;

/**
 * 串口管理器
 */
public class SerialManager {

    private static final String TAG = "SerialManager";

    private static String DOOR_SERIAL = "/dev/ttyS0";
    private static int DOOR_BAUDRATE = 9600;

    private static String CARD_SERIAL = "/dev/ttyS1";
    private static int CARD_BAUDRATE = 115200;

    private static volatile SerialManager sManager = null;
    private final HandlerThread mDispatchThread;
    private final Handler mDispatchThreadHandler;

    private final DoorSerialWorker mDoorSerialWorker;
    private final Subject<Recv5DStatus> mStateSubject;
    private final Observable<Recv5DStatus> mRxState;
    private final CardReaderWorker mCardSerialWorker;

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
        mRxState = mStateSubject.hide();

        mDispatchThread = new HandlerThread("serial-dispatch-thread");
        mDispatchThread.start();
        mDispatchThreadHandler = new Handler(mDispatchThread.getLooper());

        // 
        mDoorSerialWorker = new DoorSerialWorker(mDispatchThreadHandler);
        // 设置超时
        mDoorSerialWorker.setTimeout(Protocol.RECEIVE_TIME_OUT);
        // 开启打印日志
        mDoorSerialWorker.enableLog(true, true);
        // 设置回调
        mDoorSerialWorker.setReceiveCallback(new DoorSerialWorker.ReceiveCallback() {

            @Override
            public void onReceive(RecvCommand recvCommand) {

                switch (recvCommand.getCmd()) {
                    case Protocol.CMD_5D_STATUS_UPDATE:
                        Recv5DStatus recv = (Recv5DStatus) recvCommand;
                        //LogPlus.i("状态更新="+recv);
                        // 发送到rx上面
                        mStateSubject.onNext(recv);
                        // 发送事件
                        EventBus.getDefault().post(recv);
                        break;
                }
            }
        });

        // 刷卡器
        mCardSerialWorker = new CardReaderWorker(mDispatchThreadHandler);
        // 开启打印日志
        mCardSerialWorker.enableLog(true, true);
        mCardSerialWorker.setCardCallback(new CardReaderWorker.CardCallback() {
            @Override
            public void onReadCard(String cardId) {
                // 发送事件
                EventBus.getDefault().post(cardId);
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

   public void initDevice() {
        mDoorSerialWorker.openSerial(DOOR_SERIAL, DOOR_BAUDRATE, new SerialWorker.OpenCallback() {
            @Override
            public void onSuccess(SerialPort serialPort) {
                LogPlus.i("DoorSerialWorker open success");
            }

            @Override
            public void onFailure(Throwable tr) {
                LogPlus.w("DoorSerialWorker open failure", tr);
            }
        });
        mCardSerialWorker.openSerial(CARD_SERIAL, CARD_BAUDRATE, new SerialWorker.OpenCallback() {
            @Override
            public void onSuccess(SerialPort serialPort) {
                LogPlus.i("CardSerialWorker open success");
            }

            @Override
            public void onFailure(Throwable tr) {
                LogPlus.w("CardSerialWorker open failure", tr);
            }
        });
    }

    public void sendCommand(SendCommand command, Callback<RecvCommand> callback) {
        mDoorSerialWorker.send(command, callback);
    }

    public void openDoor(SendA4OpenDoor command, Callback<RecvA4OpenDoor> callback) {
        mDoorSerialWorker.send(command, RecvA4OpenDoor.class, callback);
    }

    public Observable<RecvSetReadTemp> rxsetTemp(SendA8SetTemp command) {
        return mDoorSerialWorker.rxSendOnIo(command, RecvSetReadTemp.class);
    }

    /**
     * 释放资源
     */
    public synchronized void release() {

        mDoorSerialWorker.release();
        mCardSerialWorker.release();
        // 结束
        mStateSubject.onComplete();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mDispatchThread.quitSafely();
        } else {
            mDispatchThread.quit();
        }
        sManager = null;
    }
}
