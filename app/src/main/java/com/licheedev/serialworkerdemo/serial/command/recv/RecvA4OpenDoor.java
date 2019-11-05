package com.licheedev.serialworkerdemo.serial.command.recv;

import com.licheedev.serialworkerdemo.serial.DoorSerialWorker;

/**
 * 控制板开锁应答命令
 *
 * @see DoorSerialWorker
 */
public class RecvA4OpenDoor extends RecvBase {

    private final int mLockNum;
    private final int mResult;

    public RecvA4OpenDoor(byte[] allPack, int cmd, byte[] data) {
        super(allPack, cmd, data);
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
