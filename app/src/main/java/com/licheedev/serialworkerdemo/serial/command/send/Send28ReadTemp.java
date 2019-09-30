package com.licheedev.serialworkerdemo.serial.command.send;

import com.licheedev.serialworkerdemo.serial.Protocol;

/**
 * 4．读取控制板温度参数28
 */
public class Send28ReadTemp extends BaseSendCommand {

    private final byte[] mData;

    /**
     * 4．读取控制板温度参数28
     */
    public Send28ReadTemp() {
        super(Protocol.CMD_28_READ_TEMP);
        mData = new byte[0];
    }

    @Override
    protected byte[] getDataN() {
        return mData;
    }
}
