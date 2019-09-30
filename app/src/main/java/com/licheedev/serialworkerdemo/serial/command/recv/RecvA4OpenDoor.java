package com.licheedev.serialworkerdemo.serial.command.recv;

import com.licheedev.serialworkerdemo.serial.MySerialWorker;

/**
 * 控制板开锁应答命令
 *
 * @see MySerialWorker
 */
public class RecvA4OpenDoor extends BaseRecvCommand {

    private final int mLockNum;
    private final int mResult;

    public RecvA4OpenDoor(byte[] allPack, byte[] data) {
        super(allPack, data);

        mLockNum = 0xff & data[0];
        mResult = 0xff & data[1];
    }

    public int getLockNum() {
        return mLockNum;
    }

    public int getResult() {
        return mResult;
    }

    public boolean isSuccess() {
        return mResult == 0;
    }
}
