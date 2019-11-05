package com.licheedev.serialworker.core;

/**
 * 初始化串口异常
 */
public class InitSerialException extends Exception {

    private static final long serialVersionUID = -1;

    public InitSerialException() {
    }

    public InitSerialException(String message) {
        super(message);
    }

    public InitSerialException(String message, Throwable cause) {
        super(message, cause);
    }

    public InitSerialException(Throwable cause) {
        super(cause);
    }
}
