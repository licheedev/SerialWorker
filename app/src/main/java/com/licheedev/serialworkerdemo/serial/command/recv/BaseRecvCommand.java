package com.licheedev.serialworkerdemo.serial.command.recv;

import android.os.SystemClock;
import com.licheedev.serialworkerdemo.serial.ByteUtil;
import com.licheedev.serialworkerdemo.serial.command.RecvCommand;

/**
 * 柜子接收到的命令封装
 */

public class BaseRecvCommand implements RecvCommand {

    private final byte[] mAllPack;
    protected int cmd;
    protected byte[] data;
    private final long mRecvTime;

    public BaseRecvCommand(byte[] allPack, byte[] data) {
        mAllPack = allPack;
        this.cmd = 0xff & allPack[4];
        this.data = data;

        mRecvTime = SystemClock.elapsedRealtime();
    }

    public int getCmd() {
        return cmd;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public long getRecvTime() {
        return mRecvTime;
    }

    @Override
    public String toString() {
        return "数据=" + ByteUtil.bytes2HexStr(mAllPack);
    }
}
