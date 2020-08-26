package com.licheedev.serialworkerdemo.serial;

import android.os.Handler;
import androidx.annotation.Nullable;
import com.licheedev.serialworker.core.DataReceiver;
import com.licheedev.serialworker.core.ValidData;
import com.licheedev.serialworker.worker.BaseSerialWorker;

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
    public void onReceiveData(byte[] receiveBuffer, int offset, int length) {

        String hexStr = ByteUtil.bytes2HexStr(receiveBuffer, offset, length);
        mCardBuffer.append(hexStr);
        if (mCardBuffer.length() < CARD_ID_LENGHT) {
            return;
        } else if (mCardBuffer.length() == CARD_ID_LENGHT) {
            final String cardId = mCardBuffer.toString();

            mCardBuffer.delete(0, mCardBuffer.length());

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

    @Nullable
    @Override
    protected DataReceiver newReceiver() {
        // 上面直接处理收到的原始数据了，不需要额外的接收器
        return null;
    }

    @Override
    public void handleValidData(ValidData validData, DataReceiver receiver) {
        // newReceiver() 返回null, 此方法不会被调用
    }

    private CardCallback mCardCallback;

    public interface CardCallback {

        void onReadCard(String cardId);
    }

    public void setCardCallback(CardCallback cardCallback) {
        mCardCallback = cardCallback;
    }
}