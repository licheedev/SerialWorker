package com.licheedev.serialworker.worker;

import android.serialport.SerialPort;
import androidx.annotation.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.concurrent.Callable;

/**
 * 基本的串口操作,默认会在打开串口的时候开线程进行读取，没有额外处理收发是否同步（不区分232还是485）
 */
public abstract class RxBaseSerialWorker extends BaseSerialWorker {

    @NonNull
    public Observable<SerialPort> rxOpenSerial() {
        return RxTool.getRxObservable(new Callable<SerialPort>() {
            @Override
            public SerialPort call() throws Exception {
                return openSerial();
            }
        }).subscribeOn(Schedulers.io());
    }
}
