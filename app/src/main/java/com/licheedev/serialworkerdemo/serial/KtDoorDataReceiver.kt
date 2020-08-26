package com.licheedev.serialworkerdemo.serial

import com.licheedev.serialworker.core.DataReceiver
import com.licheedev.serialworker.core.ValidData
import com.licheedev.serialworkerdemo.serial.command.RecvCommand
import com.licheedev.serialworkerdemo.serial.command.recv.Recv5DStatus
import com.licheedev.serialworkerdemo.serial.command.recv.RecvA4OpenDoor
import com.licheedev.serialworkerdemo.serial.command.recv.RecvSetReadTemp
import java.nio.ByteBuffer

/**
 * 数据接收器实现类
 * author: John
 * create time: 2018/7/5 14:15
 * description:
 */
class KtDoorDataReceiver : DataReceiver<RecvCommand> {

    private val mByteBuffer: ByteBuffer

    init {
        mByteBuffer = ByteBuffer.allocate(2048)
        mByteBuffer.clear()
    }

    override fun onReceive(validData: ValidData, bytes: ByteArray, offset: Int, length: Int) {
        try {
            //LogPlus.i("Receiver", "接收数据=" + Util.bytes2HexStr(bytes, offset, length));
            mByteBuffer.put(bytes, 0, length)
            mByteBuffer.flip()
            val head = Protocol.FRAME_HEAD
            val twoBytes = ByteArray(2)
            var b: Byte
            var readable: Int
            out@ while (mByteBuffer.remaining().also { readable = it } >= Protocol.MIN_PACK_LEN) {
                mByteBuffer.mark() // 标记一下开始的位置
                val frameStart = mByteBuffer.position()
                for (aHead in head) {
                    b = mByteBuffer.get()
                    if (b != aHead) { // 不满足包头，就跳到第二位，重新开始
                        mByteBuffer.position(frameStart + 1)
                        continue@out
                    }
                }

                // 数据长度,包含命令码+数据N
                mByteBuffer[twoBytes]
                val dataLen = ByteUtil.bytes2long(twoBytes, 0, 2)
                    .toInt()
                val dataN = dataLen - 1 // 减去命令码那1字节
                // 如果数据域长度过大，表示数据可能出现异常
                if (dataN > Protocol.MAX_N) {
                    //回到“第二位”，继续找到下一个3BB3
                    mByteBuffer.position(frameStart + 2)
                    continue
                }
                // 总数据长度
                val total = Protocol.MIN_PACK_LEN + dataN
                // 如果可读数据小于总数据长度，表示不够,还有数据没接收
                if (readable < total) {
                    // 重置一下要处理的位置,并跳出循环
                    mByteBuffer.reset()
                    break
                }

                // 找到校验位
                mByteBuffer.position(mByteBuffer.position() + dataLen)
                val xor = mByteBuffer.get()
                // 回到头
                mByteBuffer.reset()
                // 拿到整个包
                val allPack = ByteArray(total)
                mByteBuffer[allPack]
                val calXor = ByteUtil.getXOR(allPack, 0, allPack.size - 1)
                //LogPlus.e("xor=" + xor + ",calXor=" + calXor);
                // 校验通过
                if (xor == calXor) {
                    // 收到有效数据
                    validData.add(allPack)
                } else {
                    // 不一致则回到“第二位”，继续找到下一个3BB3
                    mByteBuffer.position(frameStart + 2)
                }
            }
        } catch (e: Exception) {
            //e.printStackTrace();
        } finally {
            // 最后清掉之前处理过的不合适的数据
            mByteBuffer.compact()
        }
    }

    override fun adaptReceive(allPack: ByteArray): RecvCommand? {
        var recvCommand: RecvCommand? = null
        try {
            val cmd = 0xff and allPack[Protocol.COMMAND_POS]
                .toInt()
            // 数据长度
            val dataLen = ByteUtil.bytes2long(allPack, Protocol.COMMAND_LEN_POS, 2)
                .toInt()
            // 数据域
            val data = ByteArray(dataLen - 1)
            System.arraycopy(allPack, Protocol.DATA_N_POS, data, 0, data.size)
            when (cmd) {
                Protocol.CMD_A4_OPEN_DOOR -> recvCommand = RecvA4OpenDoor(allPack, cmd, data)
                Protocol.CMD_28_READ_TEMP, Protocol.CMD_A8_SET_TEMP -> recvCommand =
                    RecvSetReadTemp(allPack, cmd, data)
                Protocol.CMD_5D_STATUS_UPDATE -> recvCommand = Recv5DStatus(allPack, cmd, data)
                else -> {
                }
            }
        } catch (e: Exception) {
            // 保险起见，try-catch一下
            e.printStackTrace()
        }
        return recvCommand
    }

    override fun resetCache() {
        mByteBuffer.clear()
    }


}