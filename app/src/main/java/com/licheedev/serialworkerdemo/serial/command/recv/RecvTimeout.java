package com.licheedev.serialworkerdemo.serial.command.recv;

import com.licheedev.serialworkerdemo.serial.command.RecvCommand;

public class RecvTimeout implements RecvCommand {

    public static final RecvTimeout INST = new RecvTimeout();

    @Override
    public String toString() {
        return "接收数据超时";
    }

    @Override
    public int getCmd() {
        return 0;
    }

    @Override
    public long getRecvTime() {
        return 0;
    }
}
