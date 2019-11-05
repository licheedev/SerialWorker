package com.licheedev.serialworkerdemo.serial;

import android.os.Handler;
import android.support.annotation.Nullable;
import com.licheedev.serialworker.core.DataReceiver;
import com.licheedev.serialworker.core.ValidData;
import com.licheedev.serialworker.worker.BaseSerialWorker;
import com.licheedev.serialworkerdemo.serial.command.RecvCommand;

/**
 * 简单的刷卡器示例
 */
public class CardReaderWorker extends BaseSerialWorker {

    public static final int CARD_ID_LENGHT = 20;
    private final Handler mRecvHandler;
    private final StringBuilder mCardBuffer;

    public CardReaderWorker(@Nullable Handler recvHandler) {
        mRecvHandler = recvHandler;
        mCardBuffer = new StringBuilder();
    }

    @Override
    public void notifyRunningReceive(boolean running) {
        // ignore
    }

    @Override
    public void onReceiveData(byte[] receiveBuffer, int offset, int length) {

        String hexStr = ByteUtil.bytes2HexStr(receiveBuffer, offset, length);
        mCardBuffer.append(hexStr);
        if (mCardBuffer.length() < CARD_ID_LENGHT) {
            return;
        } else if (mCardBuffer.length() == CARD_ID_LENGHT) {
            final String cardId = mCardBuffer.toString();
            if (mCardCallback != null) {
                if (mRecvHandler != null) {
                    mRecvHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mCardCallback.onReadCard(cardId);
                        }
                    });
                } else {
                    mCardCallback.onReadCard(cardId);
                }
            }
        } else {
            mCardBuffer.delete(0, mCardBuffer.length());
        }
    }

    @Override
    public DataReceiver<RecvCommand> newReceiver() {
        return new DoorDataReceiver();
    }

    @Override
    public void handleValidData(ValidData validData, DataReceiver receiver) {
        // never call，ignore
    }

    private CardCallback mCardCallback;

    public interface CardCallback {

        void onReadCard(String cardId);
    }

    public void setCardCallback(CardCallback cardCallback) {
        mCardCallback = cardCallback;
    }
}