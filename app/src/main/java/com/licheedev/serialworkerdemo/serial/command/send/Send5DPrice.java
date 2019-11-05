package com.licheedev.serialworkerdemo.serial.command.send;

import com.licheedev.serialworkerdemo.serial.ByteUtil;
import com.licheedev.serialworkerdemo.serial.Protocol;
import java.util.List;

/**
 * 设置数码管价格
 */
public class Send5DPrice extends SendBase {

    private final byte[] mData;

    /**
     * 设置数码管价格
     *
     * @param prices 价格列表，单位：分
     */
    public Send5DPrice(List<Integer> prices) {
        super(Protocol.CMD_5D_STATUS_UPDATE);
        mData = new byte[prices.size() * 2 + 1];
        mData[0] = (byte) prices.size();
        for (int i = 0; i < prices.size(); i++) {
            int price = prices.get(i);
            int offset = i * 2 + 1;
            ByteUtil.long2bytes(price, mData, offset, 2);
        }
    }

    @Override
    protected byte[] getDataN() {
        return mData;
    }
}
