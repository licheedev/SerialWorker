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
     * 告知接收数据线程正在运行。此方法会不停的被调用。
     *
     * @param running
     */
    void notifyRunningReceive(boolean running);

    /**
     * 收到数据(在串口的读线程中运行，尽量不要执行耗时操作)
     *
     * @param receiveBuffer 接收数据缓存
     * @param offset 接收收据在缓存中的偏移
     * @param length 接收到的数据长度
     */
    void onReceiveData(byte[] receiveBuffer, int offset, int length);

    /**
     * 新建数据接收器
     *
     * @return 尽量new出来，不要复用成员变量;如果不需要处理收到的数据，可以返回null
     */
    @Nullable
    DataReceiver newReceiver();

    /**
     * 收到有效数据(在串口的读线程中运行，尽量不要执行耗时操作)；
     * 只有{@link #newReceiver()}返回非null对象，才会进此方法
     *
     * @param validData 收到的有效数据
     * @param receiver 数据接收器，参考{@link #newReceiver()}
     */
    void handleValidData(ValidData validData, DataReceiver receiver);

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
    void syncSend(byte[] bytes, int offset, int length);

    /**
     * 在串口线程发送数据,异步，立即返回
     *
     * @param bytes
     * @param offset
     * @param length
     */
    void asyncSend(byte[] bytes, int offset, int length);
}
