package com.licheedev.serialworker.worker;

import android.os.Handler;
import android.os.Looper;
import android.serialport.SerialPort;
import android.support.annotation.Nullable;
import com.licheedev.myutils.LogPlus;
import com.licheedev.serialworker.core.DataReceiver;
import com.licheedev.serialworker.core.InitSerialException;
import com.licheedev.serialworker.core.SerialWorker;
import com.licheedev.serialworker.core.ValidData;
import com.licheedev.serialworker.util.MyClock;
import com.licheedev.serialworker.util.Util;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

/**
 * 基本的串口操作,默认会在打开串口的时候开线程进行读取，没有额外处理收发是否同步（不区分232还是485）
 */
public abstract class BaseSerialWorker implements SerialWorker {

    public static final String TAG = "SerialWorker";

    protected InputStream mInputStream;
    protected OutputStream mOutputStream;
    protected SerialPort mSerialPort;

    private DefaultSerialReadThread mReadThread; // 读线程

    protected final ExecutorService mSerialExecutor;

    private boolean mLogSend = false; // 打印发送的数据
    private boolean mLogRecv = false; // 打印接收的数
    protected final Handler mUiHandler;

    public BaseSerialWorker() {
        // 用来操作串口发送数据的单一线程池
        mSerialExecutor = Executors.newSingleThreadExecutor();
        mUiHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 通用的同步方法
     *
     * @param callable
     * @param <T>
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    protected <T> T callOnSerialThread(Callable<T> callable)
        throws InterruptedException, ExecutionException, InitSerialException, IOException,
        TimeoutException {

        try {
            return mSerialExecutor.submit(callable).get();
        } catch (ExecutionException e) {
            //e.printStackTrace();
            Throwable cause = e.getCause();

            if (cause instanceof InitSerialException) {
                throw ((InitSerialException) cause);
            } else if (cause instanceof IOException) {
                throw ((IOException) cause);
            } else if (cause instanceof TimeoutException) {
                throw ((TimeoutException) cause);
            } else if (cause instanceof RuntimeException) {
                throw ((RuntimeException) cause);
            } else {
                throw e;
            }
        }
    }

    /**
     * 打开串口，并抛出异常
     *
     * @param devicePath
     * @param baudrate
     * @return
     * @throws InitSerialException
     */
    private synchronized SerialPort doOpenSerial(String devicePath, int baudrate)
        throws InitSerialException {

        if (mSerialPort != null) {
            closeSerial();
        }

        try {
            mSerialPort = new SerialPort(devicePath, baudrate, 0);
            mInputStream = new BufferedInputStream(mSerialPort.getInputStream());
            mOutputStream = new BufferedOutputStream(mSerialPort.getOutputStream());

            onSerialOpened(mInputStream, mOutputStream);

            return mSerialPort;
        } catch (Exception e) {
            // 清理数据
            closeSerial();
            // 跑出异常
            throw new InitSerialException(e);
        }
    }

    /**
     * 当串口打开成功时运行，可以用来打开读线程什么的。
     * 默认开启了一个读线程{@link DefaultSerialReadThread}不停读取数据，然后使用{@link DataReceiver}对数据进行处理;
     * 如果需要完全控制读写，则可以重写这个方法。
     */
    protected void onSerialOpened(InputStream inputStream, OutputStream outputStream) {
        // 打开读线程
        mReadThread = new DefaultSerialReadThread();
        mReadThread.start();
    }

    /**
     * 默认读线程
     */
    private class DefaultSerialReadThread extends Thread {

        private final byte[] mRecvBuffer;
        private boolean mRunning = true;

        DefaultSerialReadThread() {
            // 接收收据缓存
            mRecvBuffer = new byte[2048];
        }

        @Override
        public void run() {

            DataReceiver receiver = newReceiver();
            if (receiver != null) {
                // 读之前先reset一下
                receiver.resetCache();
            }

            // 用来容纳有效数据的
            ValidData validData = new ValidData();

            LogPlus.i(TAG, "Start Read Thread");

            int len;

            while (mRunning) {
                try {
                    // 清空有效数据缓存
                    validData.clear();
                    if (mInputStream.available() > 0) {
                        len = mInputStream.read(mRecvBuffer);
                        if (len > 0) {
                            // 打印日志
                            if (isLogRecv()) {
                                LogPlus.i(TAG, "Recv=" + Util.bytes2HexStr(mRecvBuffer, 0, len));
                            }

                            onReceiveData(mRecvBuffer, 0, len);

                            if (receiver != null) {
                                // 处理接收到的数据
                                receiver.onReceive(validData, mRecvBuffer, 0, len);
                                if (validData.size() > 0) {
                                    // 处理有效的数据
                                    handleValidData(validData, receiver);
                                    validData.clear();
                                }
                            }
                        }
                    } else {
                        // 暂停一点时间，免得一直循环造成CPU占用率过高
                        MyClock.sleep(10);
                    }

                    notifyRunningReceive(mRunning);
                } catch (Exception e) {
                    LogPlus.w(TAG, "Read data failed", e);
                }
                //Thread.yield();

            }

            LogPlus.i(TAG, "Read Thread Finished");
        }

        /**
         * 关闭读线程
         */
        void close() {
            mRunning = false;
            this.interrupt();
        }
    }

    /**
     * 同步打开串口，会阻塞线程
     *
     * @param devicePath 串口设备地址
     * @param baudrate 串口波特率
     * @return null表示打开串口失败
     */
    @Override
    public SerialPort openSerial(final String devicePath, final int baudrate) {
        try {
            return callOnSerialThread(new Callable<SerialPort>() {
                @Override
                public SerialPort call() throws Exception {
                    return doOpenSerial(devicePath, baudrate);
                }
            });
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 异步打开串口
     *
     * @param devicePath 串口设备地址
     * @param baudrate 串口波特率
     * @param callback
     */
    @Override
    public void openSerial(final String devicePath, final int baudrate,
        final OpenCallback callback) {

        try {
            mSerialExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        final SerialPort serialPort = doOpenSerial(devicePath, baudrate);
                        if (callback != null) {
                            mUiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onSuccess(serialPort);
                                }
                            });
                        }
                    } catch (final Exception e) {
                        if (callback != null) {
                            mUiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onFailure(e);
                                }
                            });
                        }
                    }
                }
            });
        } catch (final Exception e) {
            if (callback != null) {
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailure(e);
                    }
                });
            }
        }
    }

    /**
     * 关闭串口
     */
    @Override
    public synchronized void closeSerial() {

        // 关闭读线程
        if (mReadThread != null) {
            try {
                mReadThread.close();
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }

        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (IOException e) {
                //e.printStackTrace();
            } finally {
                mOutputStream = null;
            }
        }

        if (mInputStream != null) {
            try {
                mInputStream.close();
            } catch (IOException e) {
                //e.printStackTrace();
            } finally {
                mInputStream = null;
            }
        }

        if (mSerialPort != null) {
            try {
                mSerialPort.close();
            } catch (Exception e) {
                //e.printStackTrace();
            } finally {
                mSerialPort = null;
            }
        }
    }

    /**
     * 获取打开的串口
     *
     * @return
     */
    @Override
    public synchronized @Nullable
    SerialPort getSerialPort() {
        return mSerialPort;
    }

    @Override
    public synchronized boolean isSerialOpened() {
        return mSerialPort != null;
    }

    /**
     * 释放资源
     */
    @Override
    public synchronized void release() {
        closeSerial();
        // 关闭线程池
        mSerialExecutor.shutdown();

        // TODO: 如果子类有其他东西要释放，就在这里处理
    }

    //@Nullable
    //@Override
    //public DataReceiver newReceiver() {
    //    // TODO 返回数据接收器，可以返回null 
    //    //return null;
    //}

    //@Override
    //public void handleValidData(byte[] allPack, Object... other) {
    //    TODO 处理收到的有效数据
    //}

    @Override
    public void enableLog(boolean logSend, boolean logRecv) {
        mLogSend = logSend;
        mLogRecv = logRecv;
    }

    @Override
    public boolean isLogSend() {
        return mLogSend;
    }

    @Override
    public boolean isLogRecv() {
        return mLogRecv;
    }

    /**
     * 在当前线程发送数据
     *
     * @param bytes
     * @param offset
     * @param len
     */
    protected void rawSend(byte[] bytes, int offset, int len) throws IOException {

        if (len < 1) {
            return;
        }

        if (isLogSend()) {
            LogPlus.i(TAG, "Send=" + Util.bytes2HexStr(bytes, 0, len));
        }

        mOutputStream.write(bytes, offset, len);
        mOutputStream.flush();
    }

    @Override
    public void syncSend(final byte[] bytes, final int offset, final int length) {

        try {
            callOnSerialThread(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    rawSend(bytes, offset, length);
                    return null;
                }
            });
        } catch (Exception e) {
            //e.printStackTrace();
            // 不用管
        }
    }

    @Override
    public void asyncSend(final byte[] bytes, final int offset, final int length) {

        try {
            mSerialExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        rawSend(bytes, offset, length);
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            //e.printStackTrace();
            // 不用管
        }
    }
}
