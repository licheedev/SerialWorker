package com.licheedev.serialworkerdemo.serial.command.send;

import com.licheedev.serialworkerdemo.serial.Protocol;

/**
 * 5．设置控制板温度参数A8
 */
public class SendA8SetTemp extends SendBase {

    private final byte[] mData;

    /**
     * 5．设置控制板温度参数A8
     *
     * @param ctrlType 温度控制类型	1	0x00：无效，不控制，0x01：制冷，0x02：加热
     * @param upLimit 温度上限	1	有符号数，范围-50到+50，超过此值无效
     * @param lowLimit 温度下限	1	有符号数，范围-50到+50，超过此值无效
     */
    public SendA8SetTemp(int ctrlType, int upLimit, int lowLimit) {
        super(Protocol.CMD_A8_SET_TEMP);
        mData = new byte[3];
        mData[0] = (byte) ctrlType;
        mData[1] = (byte) upLimit;
        mData[2] = (byte) lowLimit;
    }

    @Override
    protected byte[] getDataN() {
        return mData;
    }
}
