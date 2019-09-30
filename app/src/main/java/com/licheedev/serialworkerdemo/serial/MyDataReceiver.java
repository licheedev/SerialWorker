package com.licheedev.serialworkerdemo.serial;

import com.licheedev.serialworker.core.DataReceiver;
import com.licheedev.serialworker.core.SerialWorker;
import com.licheedev.serialworkerdemo.serial.command.RecvCommand;
import com.licheedev.serialworkerdemo.serial.command.recv.Recv5DStatus;
import com.licheedev.serialworkerdemo.serial.command.recv.RecvA4OpenDoor;
import com.licheedev.serialworkerdemo.serial.command.recv.RecvSetReadTemp;
import java.nio.ByteBuffer;

/**
 * 数据接收器实现类
 * author: John
 * create time: 2018/7/5 14:15
 * description:
 */
public class MyDataReceiver implements DataReceiver<RecvCommand> {

    private final SerialWorker mSerialWorker;
    private final ByteBuffer mByteBuffer;

    public MyDataReceiver(SerialWorker serialWorker) {
        mByteBuffer = ByteBuffer.allocate(2048);
        mByteBuffer.clear();
        mSerialWorker = serialWorker;
    }

    @Override
    public boolean onReceive(final byte[] bytes, int offset, int length) {

        // 判断收到有效的数据包
        boolean receivedOkPack = false;

        //LogPlus.i("DataReceiver", "接收数据=" + ByteUtil.bytes2HexStr(bytes, offset, length));

        mByteBuffer.put(bytes, 0, length);
        mByteBuffer.flip();

        byte[] head = Protocol.FRAME_HEAD;

        byte b;
        int readable;
        out:
        while ((readable = mByteBuffer.remaining()) >= Protocol.MIN_PACK_LEN) {
            mByteBuffer.mark(); // 标记一下开始的位置
            int frameStart = mByteBuffer.position();

            for (byte aHead : head) {
                b = mByteBuffer.get();
                if (b != aHead) { // 不满足包头，就跳到第二位，重新开始
                    mByteBuffer.position(frameStart + 1);
                    continue out;
                }
            }

            // 跳过数据帧类型
            mByteBuffer.position(mByteBuffer.position() + 1);
            // 数据长度
            final int dataLen = 0xff & mByteBuffer.get(); // 数据长度
            final int dataN = dataLen - 2; // 数据N长度
            // 总数据长度
            int total = Protocol.MIN_PACK_LEN + dataN;
            // 如果可读数据小于总数据长度，表示不够,还有数据没接收
            if (readable < total) {
                // 重置一下要处理的位置,并跳出循环
                mByteBuffer.reset();
                break;
            }

            // 找到校验和
            mByteBuffer.position(mByteBuffer.position() + dataLen);
            byte check = mByteBuffer.get();

            // 回到头
            mByteBuffer.reset();
            // 拿到整个包
            byte[] allPack = new byte[total];
            mByteBuffer.get(allPack);

            // 计算校验和 
            byte toDiff = 0;
            // 校验和为除开校验位外的所有数据做异或
            for (int i = 0; i < allPack.length - 1; i++) {
                toDiff = (byte) (toDiff ^ allPack[i]);
            }

            //LogPlus.e("check=" + check + ",toDiff=" + toDiff);

            // 校验通过
            if (toDiff == check) {
                final byte[] data = new byte[dataN];
                System.arraycopy(allPack, 6, data, 0, data.length);
                final int command = 0xff & allPack[4];
                receivedOkPack = true; // 数据包有效
                // 收到有效数据
                onReceiveValidData(allPack, command, data);
            } else {
                // 不一致则回到“第二位”，继续找到下一个3AA3
                mByteBuffer.position(frameStart + 2);
            }
        }

        // 最后清掉之前处理过的不合适的数据
        mByteBuffer.compact();
        // 收到有效数据包
        return receivedOkPack;
    }

    @Override
    public void onReceiveValidData(byte[] allPack, Object... other) {
        mSerialWorker.onReceiveValidData(allPack, other);
    }

    @Override
    public RecvCommand adaptReceive(byte[] allPack, Object... other) {
        int cmd = (int) other[0];
        byte[] data = (byte[]) other[1];

        RecvCommand recvCommand = null;

        ////分发数据
        try {
            switch (cmd) {
                case Protocol.CMD_A4_OPEN_DOOR:
                    recvCommand = new RecvA4OpenDoor(allPack, data);
                    break;
                case Protocol.CMD_28_READ_TEMP:
                case Protocol.CMD_A8_SET_TEMP:
                    recvCommand = new RecvSetReadTemp(allPack, data);
                    break;
                case Protocol.CMD_5D_STATUS_UPDATE:
                    recvCommand = new Recv5DStatus(allPack, data);
                    break;
                default:
                    // 其他暂时无视
                    break;
            }
        } catch (Exception e) {
            // 保险起见，try-catch一下
            e.printStackTrace();
        }
        return recvCommand;
    }

    @Override
    public void resetCache() {
        mByteBuffer.clear();
    }
}
