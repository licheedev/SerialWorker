package com.licheedev.serialworkerdemo.serial;

import com.licheedev.serialworker.core.DataReceiver;
import com.licheedev.serialworker.core.ValidData;
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
public class DoorDataReceiver implements DataReceiver<RecvCommand> {

    private final ByteBuffer mByteBuffer;

    public DoorDataReceiver() {
        mByteBuffer = ByteBuffer.allocate(2048);
        mByteBuffer.clear();
    }

    @Override
    public void onReceive(ValidData validData, byte[] bytes, int offset, int length) {
        try {
            //LogPlus.i("Receiver", "接收数据=" + Util.bytes2HexStr(bytes, offset, length));

            mByteBuffer.put(bytes, 0, length);
            mByteBuffer.flip();

            byte[] head = Protocol.FRAME_HEAD;
            byte[] twoBytes = new byte[2];

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

                // 数据长度,包含命令码+数据N
                mByteBuffer.get(twoBytes);
                final int dataLen = (int) ByteUtil.bytes2long(twoBytes, 0, 2);
                final int dataN = dataLen - 1; // 减去命令码那1字节
                // 如果数据域长度过大，表示数据可能出现异常
                if (dataN > Protocol.MAX_N) {
                    //回到“第二位”，继续找到下一个3BB3
                    mByteBuffer.position(frameStart + 2);
                    continue;
                }
                // 总数据长度
                final int total = Protocol.MIN_PACK_LEN + dataN;
                // 如果可读数据小于总数据长度，表示不够,还有数据没接收
                if (readable < total) {
                    // 重置一下要处理的位置,并跳出循环
                    mByteBuffer.reset();
                    break;
                }

                // 找到校验位
                mByteBuffer.position(mByteBuffer.position() + dataLen);
                byte xor = mByteBuffer.get();
                // 回到头
                mByteBuffer.reset();
                // 拿到整个包
                byte[] allPack = new byte[total];
                mByteBuffer.get(allPack);
                byte calXor = ByteUtil.getXOR(allPack, 0, allPack.length - 1);
                //LogPlus.e("xor=" + xor + ",calXor=" + calXor);
                // 校验通过
                if (xor == calXor) {
                    // 收到有效数据
                    validData.add(allPack);
                } else {
                    // 不一致则回到“第二位”，继续找到下一个3BB3
                    mByteBuffer.position(frameStart + 2);
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
        } finally {
            // 最后清掉之前处理过的不合适的数据
            mByteBuffer.compact();
        }
    }

    @Override
    public RecvCommand adaptReceive(byte[] allPack) {

        RecvCommand recvCommand = null;
        try {

            int cmd = 0xff & allPack[Protocol.COMMAND_POS];
            // 数据长度
            final int dataLen = (int) ByteUtil.bytes2long(allPack, Protocol.COMMAND_LEN_POS, 2);
            // 数据域
            final byte[] data = new byte[dataLen - 1];
            System.arraycopy(allPack, Protocol.DATA_N_POS, data, 0, data.length);

            //分发数据
            switch (cmd) {
                case Protocol.CMD_A4_OPEN_DOOR:
                    recvCommand = new RecvA4OpenDoor(allPack, cmd, data);
                    break;
                case Protocol.CMD_28_READ_TEMP:
                case Protocol.CMD_A8_SET_TEMP:
                    recvCommand = new RecvSetReadTemp(allPack, cmd, data);
                    break;
                case Protocol.CMD_5D_STATUS_UPDATE:
                    recvCommand = new Recv5DStatus(allPack, cmd, data);
                    break;
                default:
                    // ignore
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
