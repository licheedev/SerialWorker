package com.licheedev.serialworker;

import android.os.SystemClock;
import android.serialport.SerialPort;
import com.licheedev.myutils.LogPlus;
import com.licheedev.serialworker.core.DataReceiver;
import com.licheedev.serialworker.core.SerialWorker;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 基本的串口操作,默认会在打开串口的时候开线程进行读取，没有额外处理收发是否同步（不区分232还是485）
 */
public abstract class BaseSerialWorker<DR extends DataReceiver> implements SerialWorker {

    public static final String TAG = "SerialWorker";

    protected InputStream mInputStream;
    protected OutputStream mOutputStream;
    protected SerialPort mSerialPort;

    private InnerSerialReadThread mReadThread; // 读线程

    protected final ExecutorService mSerialExecutor;

    private boolean mLogSend = false; // 打印发送的数据
    private boolean mLogRecv = false; // 打印接收的数据

    public BaseSerialWorker() {
        // 用来操作串口发送数据的单一线程池
        mSerialExecutor = Executors.newSingleThreadExecutor();
    }

    /**
     * 打开串口，并抛出异常
     *
     * @param devicePath
     * @param baudrate
     * @return
     * @throws IOException
     */
    private synchronized SerialPort doOpenSerial(String devicePath, int baudrate)
        throws IOException {

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
            throw e;
        }
    }

    /**
     * 当串口打开成功时运行，可以用来打开读线程什么的。
     * 默认开启了一个读线程{@link InnerSerialReadThread}不停读取数据，然后使用{@link DataReceiver}对数据进行处理;
     * 如果需要完全控制读写，则可以重写这个方法。
     */
    protected void onSerialOpened(InputStream inputStream, OutputStream outputStream) {
        // 打开读线程
        mReadThread = new InnerSerialReadThread();
        mReadThread.start();
    }

    /**
     * 默认读线程
     */
    private class InnerSerialReadThread extends Thread {

        private final byte[] mRecvBuffer;

        public InnerSerialReadThread() {
            // 接收收据缓存
            mRecvBuffer = new byte[1024];
        }

        @Override
        public void run() {

            DR receiver = getReceiver();
            // 读之前先reset一下
            receiver.resetCache();

            LogPlus.i(TAG, "开始读线程");

            int len;

            while (!Thread.currentThread().isInterrupted()) {

                try {

                    if (mInputStream.available() > 0) {
                        len = mInputStream.read(mRecvBuffer);
                        if (len > 0) {

                            // 打印日志
                            if (isLogRecv()) {
                                LogPlus.i(TAG, "收到数据=" + Util.bytes2HexStr(mRecvBuffer, 0, len));
                            }

                            // 处理接收到的数据
                            receiver.onReceive(mRecvBuffer, 0, len);
                        }
                    } else {
                        // 暂停一点时间，免得一直循环造成CPU占用率过高
                        SystemClock.sleep(1);
                    }
                } catch (Exception e) {
                    LogPlus.w(TAG, "读取数据失败", e);
                }
                //Thread.yield();
            }

            // 完事后也reset一下
            receiver.resetCache();

            LogPlus.i(TAG, "结束读进程");
        }
    }

    @Override
    public SerialPort openSerial(final String devicePath, final int baudrate) {
        try {
            return doOpenSerial(devicePath, baudrate);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void openSerial(final String devicePath, final int baudrate,
        final OpenCallback callback) {

        Single.fromCallable(new Callable<SerialPort>() {
            @Override
            public SerialPort call() throws Exception {
                return doOpenSerial(devicePath, baudrate);
            }
        })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new SingleObserver<SerialPort>() {
                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onSuccess(SerialPort serialPort) {
                    callback.onSuccess(serialPort);
                }

                @Override
                public void onError(Throwable e) {
                    callback.onFailure(e);
                }
            });
    }

    /**
     * 关闭串口
     */
    @Override
    public synchronized void closeSerial() {

        // 关闭读线程
        if (mReadThread != null) {
            try {
                mReadThread.interrupt();
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
    public synchronized SerialPort getSerialPort() {
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

    /**
     * 获取数据接收器
     *
     * @return
     */
    protected abstract DR getReceiver();

    //@Override
    //public void onReceiveValidData(byte[] allPack, Object... other) {
    //    TODO 处理收到的有效数据
    //}

    @Override
    public void setLogSend(boolean logSend, boolean logRecv) {
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
    protected void sendOnCurrentThread(byte[] bytes, int offset, int len) throws IOException {

        if (len < 1) {
            return;
        }

        if (isLogSend()) {
            LogPlus.i(TAG, "发送数据=" + Util.bytes2HexStr(bytes, 0, len));
        }

        mOutputStream.write(bytes, offset, len);
        mOutputStream.flush();
    }

    /**
     * 在当前线程发送数据，已try catch
     *
     * @param bytes
     * @param offset
     * @param len
     * @throws IOException
     */
    protected void trySyncSendOnCurrentThread(byte[] bytes, int offset, int len) {
        try {
            sendOnCurrentThread(bytes, offset, len);
        } catch (Exception e) {
            // 不用管
        }
    }

    /**
     * 发送数据,同步，会阻塞调用的线程
     *
     * @param bytes
     * @param offset
     * @param length
     */
    public void sendData(final byte[] bytes, final int offset, final int length) {
        try {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    trySyncSendOnCurrentThread(bytes, offset, length);
                }
            };
            mSerialExecutor.submit(runnable).get();
        } catch (Exception e) {
            // 不用管
        }
    }

    /**
     * 异步发送数据
     *
     * @param bytes
     * @param offset
     * @param length
     */
    public void asyncSendData(final byte[] bytes, final int offset, final int length) {
        try {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    trySyncSendOnCurrentThread(bytes, offset, length);
                }
            };
            mSerialExecutor.execute(runnable);
        } catch (Exception e) {
            // 不用管
        }
    }
}
