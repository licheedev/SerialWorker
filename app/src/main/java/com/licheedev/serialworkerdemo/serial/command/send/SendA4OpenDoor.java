package com.licheedev.serialworkerdemo.serial.command.send;

import com.licheedev.serialworkerdemo.serial.Protocol;

/**
 * 1．安卓板开锁发送命令A4
 */
public class SendA4OpenDoor extends SendBase {

    private final byte[] mData;
    private final int mLockNum;
    private final long mOpenTime;

    /**
     * 1．安卓板开锁发送命令A4
     *
     * @param lockNum 锁编号,从1开始
     * @param openTime 开锁后自动锁门的延时，毫秒
     */
    public SendA4OpenDoor(int lockNum, long openTime) {
        super(Protocol.CMD_A4_OPEN_DOOR);

        mLockNum = lockNum;
        mOpenTime = openTime;

        mData = new byte[2];
        mData[0] = (byte) (lockNum - 1);
        int amount100ms = (int) (openTime / 100);
        mData[1] = (byte) (amount100ms > 0xff ? 0xff : amount100ms);
    }

    @Override
    protected byte[] getDataN() {
        return mData;
    }

    public int getLockNum() {
        return mLockNum;
    }

    public long getOpenTime() {
        return mOpenTime;
    }
}
