package com.licheedev.serialtool.comn;

import android.serialport.SerialPort;
import android.support.annotation.Nullable;
import com.licheedev.myutils.LogPlus;
import com.licheedev.serialtool.comn.message.LogManager;
import com.licheedev.serialtool.comn.message.RecvMessage;
import com.licheedev.serialtool.comn.message.SendMessage;
import com.licheedev.serialtool.util.ByteUtil;
import com.licheedev.serialworker.core.Callback;
import com.licheedev.serialworker.core.DataReceiver;
import com.licheedev.serialworker.core.ValidData;
import com.licheedev.serialworker.worker.BaseSerialWorker;
import io.reactivex.Scheduler;

/**
 * Created by Administrator on 2017/3/28 0028.
 */
public class SerialPortManager {

    private static final String TAG = "SerialPortManager";

    private Scheduler mSendScheduler;
    private final MySerialWorker mSerialWorker;

    private static class InstanceHolder {

        public static SerialPortManager sManager = new SerialPortManager();
    }

    public static SerialPortManager instance() {
        return InstanceHolder.sManager;
    }

    private SerialPortManager() {
        mSerialWorker = new MySerialWorker();
    }

    private class MySerialWorker extends BaseSerialWorker {

        @Override
        public void onReceiveData(byte[] receiveBuffer, int offset, int length) {
            String hexStr = ByteUtil.bytes2HexStr(receiveBuffer, 0, length);
            LogManager.instance().post(new RecvMessage(hexStr));
        }

        @Nullable
        @Override
        public DataReceiver newReceiver() {
            // ignore
            return null;
        }

        @Override
        public void handleValidData(ValidData validData, DataReceiver receiver) {
            // ignore
        }
    }

    /**
     * 打开串口
     *
     * @param device
     * @return
     */
    public SerialPort open(Device device) {
        return open(device.getPath(), device.getBaudrate());
    }

    /**
     * 打开串口
     *
     * @param devicePath
     * @param baudrateString
     * @return
     */
    public SerialPort open(String devicePath, String baudrateString) {

        return mSerialWorker.openSerial(devicePath, Integer.parseInt(baudrateString));
    }

    /**
     * 关闭串口
     */
    public void close() {

        mSerialWorker.closeSerial();
    }

    /**
     * 发送命令包
     */
    public void sendCommand(final String command) {

        // TODO: 2018/3/22  
        LogPlus.i("发送命令：" + command);

        byte[] bytes = ByteUtil.hexStr2bytes(command);

        LogManager.instance().post(new SendMessage(command));
        mSerialWorker.sendBytes(bytes, new Callback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                LogPlus.i("send success!"+Thread.currentThread());
            }

            @Override
            public void onFailure(Throwable tr) {
                LogPlus.w("send failure", tr);
            }
        });
    }
}
