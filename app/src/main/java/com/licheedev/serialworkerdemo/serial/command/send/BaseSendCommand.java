package com.licheedev.serialworkerdemo.serial.command.send;

import android.os.SystemClock;
import com.licheedev.serialworkerdemo.serial.Protocol;
import com.licheedev.serialworkerdemo.serial.command.SendCommand;
import java.nio.ByteBuffer;

/**
 * 柜子发送的数据
 */

abstract class BaseSendCommand implements SendCommand {

    protected byte[] mBytes;
    protected int cmd;
    private long mSendTime;

    public BaseSendCommand(@Protocol.Cmd int cmd) {
        this.cmd = cmd;
    }

    @Protocol.Cmd
    public int getCmd() {
        return cmd;
    }

    /**
     * 获取数据，即协议中的数据N
     *
     * @return
     */
    protected abstract byte[] getDataN();

    /**
     * 帧头	数据帧类型	数据长度	命令码	协议版本	数据	校验和
     * 2字节	1字节	1字节	1字节	1字节	N字节	1字节
     * 0x3B、0xB3						异或
     */
    public synchronized byte[] toBytes() {

        byte[] bytes = mBytes;

        if (bytes == null) {

            byte[] dataN = getDataN();

            int packLen = Protocol.MIN_PACK_LEN + dataN.length;
            bytes = new byte[packLen];

            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            byteBuffer.clear();
            // 填充START FLAG1	START FLAG2
            byteBuffer.put(Protocol.FRAME_HEAD);
            //填充数据帧类型
            byteBuffer.put(Protocol.DATA_TYPE);
            //数据长度：1字节，包括命令码、协议版本和数据域的长度
            byteBuffer.put((byte) (dataN.length + 2));
            //cmd
            byteBuffer.put((byte) cmd);
            // 协议版本
            byteBuffer.put(Protocol.VER);
            // 填充数据N
            byteBuffer.put(dataN);
            // 计算校验和 
            byte sum = 0;
            // 校验和为除开校验位外的所有数据做异或
            for (int i = 0; i < bytes.length - 1; i++) {
                sum = (byte) (sum ^ bytes[i]);
            }
            byteBuffer.put(sum);
            mBytes = bytes;
        }
        return bytes;
    }

    @Override
    public long getSendTime() {
        return mSendTime;
    }

    @Override
    public void updateSendTime() {
        mSendTime = SystemClock.elapsedRealtime();
    }
}
