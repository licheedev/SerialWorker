package com.licheedev.serialworkerdemo.serial.command;

import com.licheedev.serialworker.data.RecvData;

/**
 * 接收到的数据（空白接口）
 */
public interface RecvCommand extends RecvData {

    int getCmd();
}
