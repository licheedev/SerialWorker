package com.licheedev.serialworkerdemo.serial.command;

import com.licheedev.serialworker.core.SendData;

/**
 * 发送的数据
 */

public interface SendCommand extends SendData {

    int getCmd();
}
