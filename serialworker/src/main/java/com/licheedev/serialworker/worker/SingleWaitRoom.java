package com.licheedev.serialworker.worker;

import com.licheedev.serialworker.core.WaitRoom;
import com.licheedev.serialworker.core.RecvData;
import com.licheedev.serialworker.core.SendData;

public class SingleWaitRoom<S extends SendData, R extends RecvData> implements WaitRoom<R> {

    protected final SendReceive<S, R> mWork;
    protected final S mSendData;
    protected R mResponse;

    public SingleWaitRoom(SendReceive<S, R> work, S sendData) {
        mWork = work;
        mSendData = sendData;
    }

    @Override
    public synchronized R getResponse(long timeout) {
        // 先检查
        if (mResponse != null) {
            return mResponse;
        }
        // 等一段时间
        waitNoThrow(timeout);
        return mResponse;
    }

    @Override
    public synchronized void putResponse(R r) {
        if (r == null) {
            return;
        }

        // 检查一下响应
        if (mWork.isMyResponse(mSendData, r)) {
            mResponse = r;
            // 收到后，就不要等了
            notifyAll();
        }
    }

    @Override
    public void notifyRunningReceive(boolean running) {
        // 空实现
    }

    protected void waitNoThrow(long timeout) {
        try {
            wait(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
