package com.licheedev.serialworker.core;

/**
 * 通用的回调
 *
 * @param <T>
 */
public interface Callback<T> {

    void onSuccess(T t);

    void onFailure(Throwable tr);
}
