package com.licheedev.serialworker.core;

import android.serialport.SerialPort;

/**
 * 真正的串口操作
 */
public interface SerialWorker {

    /**
     * 打开串口，同步，可能会阻塞线程
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
     * 收到有效数据(在串口的接收线中运行)；
     * 一般可以使用{@link DataReceiver#adaptReceive(byte[], Object...)} 来封装收到的数据
     *
     * @param allPack 完整的数据包
     * @param other 其他附加数据，如命令码CMD、除开帧头帧尾等附加位的真正数据DATAN
     */
    void onReceiveValidData(final byte[] allPack, Object... other);

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
}
