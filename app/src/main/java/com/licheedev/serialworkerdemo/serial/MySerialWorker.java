package com.licheedev.serialworkerdemo.serial;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.licheedev.serialworker.worker.Rs232ReactiveSerialWorker;
import com.licheedev.serialworker.core.DataReceiver;
import com.licheedev.serialworkerdemo.serial.command.RecvCommand;
import com.licheedev.serialworkerdemo.serial.command.SendCommand;
import com.licheedev.serialworkerdemo.serial.command.recv.RecvTimeout;

/**
 * 柜子的串口操作
 */
public class MySerialWorker extends Rs232ReactiveSerialWorker<SendCommand, RecvCommand> {

    private final MyDataReceiver mReceiver;
    private final Handler mRecvHandler;

    public MySerialWorker(@Nullable Handler recvHandler) {
        mReceiver = new MyDataReceiver();
        mRecvHandler = recvHandler;
    }

    @Override
    protected boolean isMyResponse(SendCommand sendData, RecvCommand recvData) {
        // 如果收到的命令跟发送的命令是同类型，且接收时间大于发送时间
        return sendData.getCmd() == recvData.getCmd()
            && recvData.getRecvTime() > sendData.getSendTime();
    }

    @Override
    public DataReceiver<RecvCommand> getReceiver() {
        return mReceiver;
    }

    @Override
    public void onReceiveValidData(@NonNull final RecvCommand receive) {
        // 把数据暴露出去
        if (mReceiveCallback != null) {
            if (mRecvHandler != null) {
                mRecvHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mReceiveCallback.onReceive(receive);
                    }
                });
            } else {
                mReceiveCallback.onReceive(receive);
            }
        }
    }

    @Override
    public RecvCommand returnItemWhenTimeout() {
        return RecvTimeout.INST;
    }

    private ReceiveCallback mReceiveCallback;

    public interface ReceiveCallback {

        void onReceive(RecvCommand recvCommand);
    }

    public void setReceiveCallback(ReceiveCallback receiveCallback) {
        mReceiveCallback = receiveCallback;
    }
}