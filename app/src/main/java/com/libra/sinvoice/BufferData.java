package com.libra.sinvoice;

public class BufferData {
    public byte mData[];

    private int mFilledSize;
    private int mMaxBufferSize;

    public BufferData(int maxBufferSize) {
        mMaxBufferSize = maxBufferSize;
        reset();

        if (maxBufferSize > 0) {
            mMaxBufferSize = maxBufferSize;
            mData = new byte[mMaxBufferSize];
        } else {
            mData = null;
        }
    }

    final public void reset() {
        mFilledSize = 0;
    }

    final public int getMaxBufferSize() {
        return mMaxBufferSize;
    }

    final public void setFilledSize(int size) {
        mFilledSize = size;
    }

    final public int getFilledSize() {
        return mFilledSize;
    }
}
