package com.licheedev.serialworkerdemo.serial.command.recv;

import com.licheedev.serialworkerdemo.serial.MySerialWorker;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * 控制板开锁应答命令
 *
 * @see MySerialWorker
 */
public class Recv5DStatus extends BaseRecvCommand {

    /**
     * 门已关闭
     */
    public static final int STATUS_CLOSED = 0;
    /**
     * 门已开启
     */
    public static final int STATUS_OPENED = 1;
    /**
     * 门异常
     */
    public static final int STATUS_ABNORMAL = -1;

    private final int[] mLocks; // 锁列表,0x00: 门锁关  0x01: 门锁开
    private final int[] mTongues; // 锁舌列表
    private final int mLockAmount;
    private final byte[] mTemps;
    private String mTemperature;

    public Recv5DStatus(byte[] allPack, byte[] data) {
        super(allPack, data);
        // 温度，占5个字节
        mTemps = Arrays.copyOfRange(data, 0, 5);
        mTemperature = getAscii(mTemps);

        mLockAmount = 0xff & data[5];

        mLocks = new int[mLockAmount];
        mTongues = new int[mLockAmount];

        int lockIndex;
        int tongueIndex;
        for (int i = 0; i < mLockAmount; i++) {
            lockIndex = 6 + i * 2;
            tongueIndex = lockIndex + mLockAmount;
            mLocks[i] = data[lockIndex];
            mTongues[i] = data[tongueIndex];
        }
    }
    
    public int getLockAmount() {
        return mLockAmount;
    }

    /**
     * 获取锁状态
     *
     * @param lockNum 锁编号，从1开始
     * @return
     */
    public int getLockStatus(int lockNum) {
        return mLocks[lockNum - 1];
    }

    /**
     * 获取锁舌状态
     *
     * @param lockNum 锁编号，从1开始
     * @return
     */
    public int getTongueStatus(int lockNum) {
        return mTongues[lockNum - 1];
    }

    /**
     * 锁是否已经打开(是否可以拉开门)
     *
     * @param lockNum 锁编号，从1开始
     * @return
     */
    public boolean isLockOpened(int lockNum) {
        //0x00: 门锁关  0x01: 门锁开
        //0x00: 锁舌缩回 0x01: 锁舌伸出

        // 只要锁舌缩回，就算门开了（可以随意拉开门）
        return mTongues[lockNum - 1] == 0x00;
    }

    /**
     * 门是否已经打开（至少有一个锁是开的，就算门开了）
     *
     * @return
     */
    public boolean isDoorOpened() {
        //0x00: 门锁关  0x01: 门锁开
        //0x00: 锁舌缩回 0x01: 锁舌伸出

        // 只要锁舌缩回，就算门开了（可以随意拉开门）

        for (int i = 1; i <= mLockAmount; i++) {
            if (isLockOpened(i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 门锁是否已经锁上
     *
     * @param lockNum 锁编号，从1开始
     * @return
     */
    public boolean isLockClosed(int lockNum) {
        //0x00: 门锁关  0x01: 门锁开
        //0x00: 锁舌缩回 0x01: 锁舌伸出

        // 门锁关闭，且锁舌伸出，才算门被锁上
        int i = lockNum - 1;
        return mLocks[i] == 0x00 && mTongues[i] == 0x01;
    }

    /**
     * 所有门锁已经锁上，才算门关闭了
     *
     * @return
     */
    public boolean isDoorClosed() {
        for (int i = 1; i <= mLockAmount; i++) {
            if (!isLockClosed(i)) {
                return false;
            }
        }
        return true;
    }

    public String getTemperature() {
        return mTemperature;
    }

    @Override
    public String toString() {
        return "Recv5DStatus{"
            + "mLocks="
            + Arrays.toString(mLocks)
            + ", mTongues="
            + Arrays.toString(mTongues)
            + ", mLockAmount="
            + mLockAmount
            + ", mTemperature="
            + mTemperature
            + '}';
    }

    private String getAscii(byte[] tempBytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte temp : tempBytes) {
            if (temp != 0) {
                stringBuilder.append((char) (0xff & temp));
            } else {
                break;
            }
        }
        return stringBuilder.toString();
    }

    private String getAscii2(byte[] tempBytes) {

        int len = 0;
        for (byte temp : tempBytes) {
            if (temp != 0) {
                len++;
            } else {
                break;
            }
        }
        return new String(tempBytes, 0, len, Charset.forName("ascii"));
    }
}
