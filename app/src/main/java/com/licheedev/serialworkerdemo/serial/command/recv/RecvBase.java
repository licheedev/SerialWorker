package com.licheedev.serialworkerdemo.serial.command.recv;

import android.os.SystemClock;
import com.licheedev.serialworker.util.Util;
import com.licheedev.serialworkerdemo.serial.command.RecvCommand;
import java.net.InetSocketAddress;

/**
 * 柜子接收到的命令封装
 */

public class RecvBase implements RecvCommand {

    private final byte[] mAllPack;
    protected final byte[] data;
    protected final int cmd;
    private final long mRecvTime;
    private InetSocketAddress mAddress;

    public RecvBase(byte[] allPack, int cmd, byte[] data) {
        mAllPack = allPack;
        this.cmd = cmd;
        this.data = data;
        mRecvTime = SystemClock.uptimeMillis();
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
    public byte[] getAllPack() {
        return mAllPack;
    }

    @Override
    public String toString() {
        return "数据=" + Util.bytes2HexStr(mAllPack);
    }
}
