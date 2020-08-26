package com.licheedev.serialworker.core;

import android.serialport.SerialPort;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 真正的串口操作
 */
public interface SerialWorker {

    /**
     * 设置串口设备。
     * [注意]修改配置后，需要重新打开串口
     *
     * @param devicePath 串口设备地址
     * @param baudrate 串口波特率
     */
    void setDevice(String devicePath, int baudrate);

    /**
     * 设置串口参数（数据位、校验位、停止位）。
     * 默认配置为：8数据位、无校验(0)、1停止位。
     * [注意]修改配置后，需要重新打开串口
     *
     * @param dataBits 数据位；默认8,可选值为5~8
     * @param parity 校验位；0:无校验位(NONE，默认)；1:奇校验位(ODD);2:偶校验位(EVEN)
     * @param stopBits 停止位；默认1；1:1位停止位；2:2位停止位
     * @return
     */
    void setParams(int dataBits, int parity, int stopBits);

    /**
     * 打开串口，同步，会阻塞线程
     *
     * @return 串口实例
     */
    @NonNull
    SerialPort openSerial() throws Exception;

    /**
     * 打开串口，异步，回调在UI线程
     *
     * @param callback
     */
    void openSerial(Callback<SerialPort> callback);

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
