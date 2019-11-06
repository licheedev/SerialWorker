package com.licheedev.serialworker.core;

import android.serialport.SerialPort;
import android.support.annotation.Nullable;

/**
 * 真正的串口操作
 */
public interface SerialWorker {

    /**
     * 打开串口，同步，会阻塞线程
     *
     * @param devicePath 串口设备地址
     * @param baudrate 串口波特率
     * @return 串口实例，null表示打开失败
     */
    SerialPort openSerial(String devicePath, int baudrate);

    /**
     * 异步打开串口回调
     */
    interface OpenCallback {

        /**
         * 打开成功
         *
         * @param serialPort
         */
        void onSuccess(SerialPort serialPort);

        /**
         * 打开失败
         *
         * @param tr
         */
        void onFailure(Throwable tr);
    }

    /**
     * 打开串口，异步，回调在UI线程
     *
     * @param devicePath 串口设备地址
     * @param baudrate 串口波特率
     * @param callback
     */
    void openSerial(String devicePath, int baudrate, OpenCallback callback);

    /**
     * 关闭串口
     */
    void closeSerial();

    /**
     * 获取打开的串口
     *
     * @return
     */
    @Nullable
    SerialPort getSerialPort();

    /**
     * 串口是否已经打开
     *
     * @return
     */
    boolean isSerialOpened();

    /**
     * 释放资源，释放资源后，串口不允许再次打开和使用
     */
    void release();

    /**
     * 设置显示发送数据日志，默认全禁用
     *
     * @param logSend 打印发送数据的日志
     * @param logRecv 打印接收数据的日志
     */
    void enableLog(boolean logSend, boolean logRecv);

    /**
     * 是否显示发送数据日志
     *
     * @return
     */
    boolean isLogSend();

    /**
     * 是否显示接收数据日志
     *
     * @return
     */
    boolean isLogRecv();

    /**
     * 在串口线程发送数据，并阻塞调用的线程
     *
     * @param bytes
     * @param offset
     * @param length
     */
    void syncSendBytes(byte[] bytes, int offset, int length) throws Exception;

    /**
     * 在串口线程发送数据，并阻塞调用的线程。不会抛出异常。
     *
     * @param bytes
     * @param offset
     * @param length
     */
    void syncSendBytesNoThrow(byte[] bytes, int offset, int length);

    /**
     * 在串口线程发送数据，并阻塞调用的线程
     *
     * @param bytes
     */
    void syncSendBytes(byte[] bytes) throws Exception;

    /**
     * 在串口线程发送数据，并阻塞调用的线程。不会抛出异常
     *
     * @param bytes
     */
    void syncSendBytesNoThrow(byte[] bytes);

    /**
     * 异步在串口线程发送数据
     *
     * @param bytes
     * @param offset
     * @param length
     */
    void sendBytes(byte[] bytes, int offset, int length, @Nullable Callback<Void> callback);

    /**
     * 异步在串口线程发送数据
     *
     * @param bytes
     * @param callback
     */
    void sendBytes(byte[] bytes, @Nullable Callback<Void> callback);
}
