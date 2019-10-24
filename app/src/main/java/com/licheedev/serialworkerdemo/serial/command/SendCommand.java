package com.licheedev.serialworkerdemo.serial.command;

import com.licheedev.serialworker.data.SendData;

/**
 * 发送的数据
 */

public interface SendCommand extends SendData {

    int getCmd();
}
