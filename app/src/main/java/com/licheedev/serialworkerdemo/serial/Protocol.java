package com.licheedev.serialworkerdemo.serial;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 协议
 */
public interface Protocol {

    int SERIAL_BAUD_RATE = 115200;

    /**
     * 发送数据后，接收数据超时时间，毫秒
     */
    long RECEIVE_TIME_OUT = 3000;

    /**
     * 两次接收数据之间，判断已不再会有数据的超时时间，毫秒
     */
    long RECEIVE_INTERVAL_TIME_OUT = 20;

    //////////////////////帧结构
    /**
     * 帧头
     */
    byte[] FRAME_HEAD = { 0x3B, (byte) 0xB3 };

    /**
     * 协议版本
     */
    byte VER = 0x10;

    /**
     * 最小数据包的长度(除开数据的N个字节）
     * 帧头  数据帧类型  数据长度  命令码  协议版本     数据    校验和
     * 2       1         1          1      1          N        1
     */
    int MIN_PACK_LEN = 2 + 1 + 1 + 1 + 1 + 1;

    /**
     * 	数据帧类型：安卓板发送固定为0x00, 控制板发送固定为0x01
     */
    byte DATA_TYPE = 0x00;

    ///////////////////////////// 下面是具体的协议
    /**
     * 1．安卓板开锁发送命令A4
     */
    int CMD_A4_OPEN_DOOR = 0xA4;
    /**
     * 2．安卓板控制灯发送命令A6
     */
    int CMD_A6_CTRLL_LIGHT = 0xA6;
    /**
     * 3．控制信号输出A7
     */
    int CMD_A7_CTRL_SIGNAL = 0xA7;
    /**
     * 4．读取控制板温度参数28
     */
    int CMD_28_READ_TEMP = 0x28;
    /**
     * 5．设置控制板温度参数A8
     */
    int CMD_A8_SET_TEMP = 0xA8;
    /**
     * 6．控制板主动上传状态（每1秒发送一次，门状态改变立即触发）5C
     */
    @Deprecated
    int CMD_5C_STATUS_UPDATE = 0x5C;
    /**
     * 7．控制板主动上传状态（每1秒发送一次，门状态改变立即触发）5D
     */
    int CMD_5D_STATUS_UPDATE = 0x5D;

    /**
     * @hide
     */
    @IntDef({
        CMD_A4_OPEN_DOOR, CMD_A6_CTRLL_LIGHT, CMD_A7_CTRL_SIGNAL, CMD_28_READ_TEMP, CMD_A8_SET_TEMP,
        CMD_5C_STATUS_UPDATE, CMD_5D_STATUS_UPDATE
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Cmd {
    }
}
