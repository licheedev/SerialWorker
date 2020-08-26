package com.licheedev.serialworker.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 通用的回调
 *
 * @param <T>
 */
public interface Callback<T> {

    void onSuccess(@Nullable T t);

    void onFailure(@NonNull Throwable tr);
}
