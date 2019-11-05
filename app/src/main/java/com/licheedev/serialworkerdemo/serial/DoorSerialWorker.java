package com.licheedev.serialworkerdemo.serial;

import android.os.Handler;
import android.support.annotation.Nullable;
import com.licheedev.serialworker.core.DataReceiver;
import com.licheedev.serialworker.worker.Rs232SerialWorker;
import com.licheedev.serialworkerdemo.serial.command.RecvCommand;
import com.licheedev.serialworkerdemo.serial.command.SendCommand;

/**
 * 柜子的串口操作
 */
public class DoorSerialWorker extends Rs232SerialWorker<SendCommand, RecvCommand> {

    private final Handler mRecvHandler;

    public DoorSerialWorker(@Nullable Handler recvHandler) {
        mRecvHandler = recvHandler;
    }

    @Override
    public boolean isMyResponse(SendCommand sendData, RecvCommand recvData) {
        // 如果收到的命令跟发送的命令是同类型
        return sendData.getCmd() == recvData.getCmd();
    }

    @Override
    public DataReceiver<RecvCommand> newReceiver() {
        return new DoorDataReceiver();
    }

    @Override
    public void onReceiveData(final RecvCommand recvData) {
        // 把数据暴露出去
        if (mReceiveCallback != null) {
            if (mRecvHandler != null) {
                mRecvHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mReceiveCallback.onReceive(recvData);
                    }
                });
            } else {
                mReceiveCallback.onReceive(recvData);
            }
        }
    }

    private ReceiveCallback mReceiveCallback;

    public interface ReceiveCallback {

        void onReceive(RecvCommand recvCommand);
    }

    public void setReceiveCallback(ReceiveCallback receiveCallback) {
        mReceiveCallback = receiveCallback;
    }
}