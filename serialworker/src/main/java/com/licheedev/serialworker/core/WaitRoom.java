package com.licheedev.serialworker.core;

/**
 * 发送数据后，用来等待相应数据的
 *
 * @param <T>
 */
public interface WaitRoom<T> {


    Object getResponse(long timeout);
    
    void putResponse(T t);

    void notifyRunningReceive(boolean running);
}
