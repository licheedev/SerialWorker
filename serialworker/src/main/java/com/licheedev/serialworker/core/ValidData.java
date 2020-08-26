package com.licheedev.serialworker.core;

import androidx.annotation.NonNull;
import java.util.ArrayList;

/**
 * 有效数据容器
 */
public class ValidData {

    private final ArrayList<byte[]> mBuffer;

    public ValidData() {
        mBuffer = new ArrayList<>();
    }

    public void add(@NonNull byte[] validData) {
        mBuffer.add(validData);
    }

    public void clear() {
        mBuffer.clear();
    }

    @NonNull
    public ArrayList<byte[]> getAll() {
        return mBuffer;
    }

    public int size() {
        return mBuffer.size();
    }

    public boolean isEmpty() {
        return mBuffer.isEmpty();
    }
    
    @NonNull
    public ArrayList<byte[]> cloneData() {
        return new ArrayList<>(mBuffer);
    }
}
