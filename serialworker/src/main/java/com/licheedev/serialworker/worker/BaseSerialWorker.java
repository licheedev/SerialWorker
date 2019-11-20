package com.licheedev.serialworker.worker;

import android.os.Handler;
import android.os.Looper;
import android.serialport.SerialPort;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.licheedev.myutils.LogPlus;
import com.licheedev.serialworker.core.Callback;
import com.licheedev.serialworker.core.DataReceiver;
import com.licheedev.serialworker.core.OpenSerialException;
import com.licheedev.serialworker.core.SerialWorker;
import com.licheedev.serialworker.core.ValidData;
import com.licheedev.serialworker.util.MyClock;
import com.licheedev.serialworker.util.Util;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
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
    public static final String ERROR_NO_SERIALPORT_OPENED = "SerialPort hasn't been opened!";
    public static final String ERROR_OPEN_SERIAL_FAILED = "Open SerialPort failed!";

    protected InputStream mInputStream;
    protected OutputStream mOutputStream;
    protected SerialPort mSerialPort;

    private DefaultSerialReadThread mReadThread; // 读线程

    protected final ExecutorService mSerialExecutor;

    private boolean mLogSend = false; // 打印发送的数据
    private boolean mLogRecv = false; // 打印接收的数
    protected final Handler mUiHandler;

    private String mDevicePath; // 串口地址
    private int mBaudrate; // 串口波特率
    private int mDataBits = 8; // 数据位
    private int mParity = 0; // 校验位
    private int mStopBits = 1; // 停止位

    public BaseSerialWorker() {
        // 用来操作串口发送数据的单一线程池
        mSerialExecutor = Executors.newSingleThreadExecutor();
        mUiHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 告知接收数据线程正在运行。此方法会不停的被调用。
     *
     * @param running
     */
    protected void notifyRunningReceive(boolean running) {
        // TODO 默认空实现
    }

    /**
     * 收到数据(在串口的读线程中运行，尽量不要执行耗时操作)
     *
     * @param receiveBuffer 接收数据缓存
     * @param offset 接收收据在缓存中的偏移
     * @param length 接收到的数据长度
     */
    protected abstract void onReceiveData(byte[] receiveBuffer, int offset, int length);

    /**
     * 新建数据接收器
     *
     * @return 尽量new出来，不要复用成员变量;如果不需要处理收到的数据，可以返回null
     */
    @Nullable
    protected abstract DataReceiver newReceiver();

    /**
     * 收到有效数据(在串口的读线程中运行，尽量不要执行耗时操作)；
     * 只有{@link #newReceiver()}返回非null对象，才会进此方法
     *
     * @param validData 收到的有效数据
     * @param receiver 数据接收器，参考{@link #newReceiver()}
     */
    protected abstract void handleValidData(ValidData validData, DataReceiver receiver);

    /**
     * 打开串口，并抛出异常
     *
     * @return
     * @throws OpenSerialException
     */
    private synchronized SerialPort doOpenSerial() throws OpenSerialException {

        if (mSerialPort != null) {
            closeSerial();
        }

        try {

            if (TextUtils.isEmpty(mDevicePath) || mBaudrate == 0) {

                throw new RuntimeException("SerialPort hasn't been configured! (device="
                    + mDevicePath
                    + ",baudrate="
                    + mBaudrate);
            }

            mSerialPort = SerialPort.newBuilder(mDevicePath, mBaudrate)
                .stopBits(mStopBits)
                .dataBits(mDataBits)
                .parity(mParity)
                .build();

            mInputStream = new BufferedInputStream(mSerialPort.getInputStream());
            mOutputStream = new BufferedOutputStream(mSerialPort.getOutputStream());

            onSerialOpened(mInputStream, mOutputStream);

            return mSerialPort;
        } catch (Exception e) {
            // 清理数据
            closeSerial();
            // 抛出异常
            throw new OpenSerialException(ERROR_OPEN_SERIAL_FAILED, e);
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

            LogPlus.i(TAG, "Start SerialPort Read Thread(Path=" + mDevicePath + ")");

            int len;

            while (mRunning) {
                try {
                    if (mInputStream.available() > 0) {
                        len = mInputStream.read(mRecvBuffer);
                        if (len > 0) {
                            // 打印日志
                            if (isLogRecv()) {
                                LogPlus.i(TAG, "Recv=" + Util.bytes2HexStr(mRecvBuffer, 0, len));
                            }

                            onReceiveData(mRecvBuffer, 0, len);

                            if (receiver != null) {
                                // 清空有效数据缓存
                                validData.clear();
                                // 处理接收到的数据
                                receiver.onReceive(validData, mRecvBuffer, 0, len);
                                if (validData.size() > 0) {
                                    // 处理有效的数据
                                    handleValidData(validData, receiver);
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

    @Override
    public void setDevice(String devicePath, int baudrate) {
        mDevicePath = devicePath;
        mBaudrate = baudrate;
    }

    @Override
    public void setParams(int dataBits, int parity, int stopBits) {
        mDataBits = dataBits;
        mParity = parity;
        mStopBits = stopBits;
    }

    /**
     * 同步打开串口，会阻塞线程
     *
     * @return
     */
    @Override
    public SerialPort openSerial() throws Exception {

        return callOnSerialThread(new Callable<SerialPort>() {
            @Override
            public SerialPort call() throws Exception {
                return doOpenSerial();
            }
        });
    }

    @Override
    public void openSerial(Callback<SerialPort> callback) {

        asyncCallOnSerialThread(new Callable<SerialPort>() {
            @Override
            public SerialPort call() throws Exception {
                return doOpenSerial();
            }
        }, callback);
    }

    @Override
    public Observable<SerialPort> rxOpenSerial() {
        return getRxObservable(new Callable<SerialPort>() {
            @Override
            public SerialPort call() throws Exception {
                return openSerial();
            }
        }).subscribeOn(Schedulers.io());
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
     * 通用的同步方法
     *
     * @param executor 线程池
     * @param callable
     * @param <T>
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    protected <T> T callOnExecutor(ExecutorService executor, Callable<T> callable)
        throws InterruptedException, ExecutionException, OpenSerialException, IOException,
        TimeoutException {

        try {
            return executor.submit(callable).get();
        } catch (ExecutionException e) {
            //e.printStackTrace();
            Throwable cause = e.getCause();

            if (cause instanceof OpenSerialException) {
                throw ((OpenSerialException) cause);
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

    protected void asyncCallOnExecutor(ExecutorService executor, final Callable<?> callable,
        final Callback callback) {
        try {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        final Object o = callable.call();
                        if (callback != null) {
                            mUiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onSuccess(o);
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
     * 通用的同步方法
     *
     * @param callable
     * @param <T>
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    protected <T> T callOnSerialThread(Callable<T> callable)
        throws InterruptedException, ExecutionException, OpenSerialException, IOException,
        TimeoutException {

        return callOnExecutor(mSerialExecutor, callable);
    }

    protected void asyncCallOnSerialThread(final Callable<?> callable, final Callback callback) {
        asyncCallOnExecutor(mSerialExecutor, callable, callback);
    }

    /**
     * Rx发送数据源
     *
     * @return
     */
    protected <T> Observable<T> getRxObservable(final Callable<T> callable) {

        return Observable.create(new ObservableOnSubscribe<T>() {
            @Override
            public void subscribe(ObservableEmitter<T> emitter) throws Exception {
                boolean terminated = false;
                try {
                    T t = callable.call();
                    if (!emitter.isDisposed()) {
                        terminated = true;
                        emitter.onNext(t);
                        emitter.onComplete();
                    }
                } catch (Throwable t) {
                    if (terminated) {
                        RxJavaPlugins.onError(t);
                    } else if (!emitter.isDisposed()) {
                        try {
                            emitter.onError(t);
                        } catch (Throwable inner) {
                            RxJavaPlugins.onError(new CompositeException(t, inner));
                        }
                    }
                }
            }
        });
    }

    /**
     * 在当前线程发送数据
     *
     * @param bytes
     * @param offset
     * @param len
     */
    protected void rawSend(byte[] bytes, int offset, int len)
        throws IOException, OpenSerialException {

        if (len < 1) {
            return;
        }

        if (isLogSend()) {
            LogPlus.i(TAG, "Send=" + Util.bytes2HexStr(bytes, 0, len));
        }

        if (mOutputStream == null) {
            throw new OpenSerialException(ERROR_NO_SERIALPORT_OPENED);
        }

        mOutputStream.write(bytes, offset, len);
        mOutputStream.flush();
    }

    /**
     * 在当前线程发送数据
     *
     * @param bytes
     */
    protected void rawSend(byte[] bytes) throws IOException, OpenSerialException {
        rawSend(bytes, 0, bytes.length);
    }

    @Override
    public void syncSendBytes(final byte[] bytes, final int offset, final int length)
        throws Exception {
        callOnSerialThread(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                rawSend(bytes, offset, length);
                return null;
            }
        });
    }

    @Override
    public void syncSendBytesNoThrow(byte[] bytes, int offset, int length) {
        try {
            syncSendBytes(bytes, offset, length);
        } catch (Exception e) {
            //e.printStackTrace();
            // 不用管
        }
    }

    @Override
    public void syncSendBytes(byte[] bytes) throws Exception {
        syncSendBytes(bytes, 0, bytes.length);
    }

    @Override
    public void syncSendBytesNoThrow(byte[] bytes) {
        syncSendBytesNoThrow(bytes, 0, bytes.length);
    }

    @Override
    public void sendBytes(final byte[] bytes, final int offset, final int length,
        @Nullable Callback<Void> callback) {
        asyncCallOnSerialThread(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                rawSend(bytes, offset, length);
                return null;
            }
        }, callback);
    }

    @Override
    public void sendBytes(byte[] bytes, @Nullable Callback<Void> callback) {
        sendBytes(bytes, 0, bytes.length, callback);
    }
}
