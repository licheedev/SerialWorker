package com.licheedev.serialworker.worker;

import android.support.annotation.Nullable;
import com.licheedev.serialworker.core.Callback;
import com.licheedev.serialworker.core.RecvData;
import com.licheedev.serialworker.core.SendData;

/**
 * 用于半双工的rs485设备，请求应答必须串行执行，一个请求应答（或异常）未结束，下一请求不允许执行；
 * 适用于不会主动上报数据，只响应上位机命令的串口设备。
 *
 * @param <S> 发送的数据类型
 * @param <R> 接收的数据类型
 */
public abstract class Rs485SerialWorker<S extends SendData, R extends RecvData>
    extends Rs232SerialWorker<S, R> {

    public static final String NO_SUPPORT_RS485 = "Don't call this method on RS485";

    @Override
    public void syncSendOnly(S sendData) throws Exception {
        throw new RuntimeException(NO_SUPPORT_RS485);
        //super.syncSendOnly(sendData);
    }

    @Override
    public void syncSendOnlyNoThrow(S sendData) {
        throw new RuntimeException(NO_SUPPORT_RS485);
        //super.syncSendOnlyNoThrow(sendData);
    }

    @Override
    public void sendOnly(S sendData, @Nullable Callback<Void> callback) {
        throw new RuntimeException(NO_SUPPORT_RS485);
        //super.sendOnly(sendData, callback);
    }
}
