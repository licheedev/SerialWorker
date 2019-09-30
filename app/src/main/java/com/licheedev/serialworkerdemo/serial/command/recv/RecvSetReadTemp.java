package com.licheedev.serialworkerdemo.serial.command.recv;

import com.licheedev.serialworkerdemo.serial.MySerialWorker;

/**
 * 5．设置控制板温度参数A8
 *
 * @see MySerialWorker
 */
public class RecvSetReadTemp extends BaseRecvCommand {

    /**
     * 温度控制类型	1	0x00：无效，不控制，0x01：制冷，0x02：加热
     */
    private final int mCtrlType;
    /**
     * 温度上限	1	有符号数，范围-50到+50，超过此值无效
     */
    private final int mUpLimit;
    /**
     * 温度下限	1	有符号数，范围-50到+50，超过此值无效
     */
    private final int mDowmLimit;

    public RecvSetReadTemp(byte[] allPack, byte[] data) {
        super(allPack, data);

        mCtrlType = data[0];
        mUpLimit = data[1];
        mDowmLimit = data[2];
    }

    /**
     * 温度控制类型	1	0x00：无效，不控制，0x01：制冷，0x02：加热
     *
     * @return
     */
    public int getCtrlType() {
        return mCtrlType;
    }

    public int getUpLimit() {
        return mUpLimit;
    }

    public int getDowmLimit() {
        return mDowmLimit;
    }

    @Override
    public String toString() {
        return "RecvSetReadTemp{"
            + "mCtrlType="
            + mCtrlType
            + ", mUpLimit="
            + mUpLimit
            + ", mDowmLimit="
            + mDowmLimit
            + '}';
    }
}
