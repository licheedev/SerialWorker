package com.licheedev.serialworker.core;

/**
 * 初始化串口异常
 */
public class OpenSerialException extends Exception {

    private static final long serialVersionUID = -1;

    public OpenSerialException() {
    }

    public OpenSerialException(String message) {
        super(message);
    }

    public OpenSerialException(String message, Throwable cause) {
        super(message, cause);
    }

    public OpenSerialException(Throwable cause) {
        super(cause);
    }
}
