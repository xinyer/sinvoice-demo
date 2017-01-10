package com.libra.sinvoice;

public class BufferQueue {
    private final static String TAG = "BufferQueue";
    private final static int STATE_set = 1;
    private final static int STATE_reset = 2;

    private int mState;
    private Queue mEmptyQueue;
    private Queue mFullQueue;
    private BufferData[] mBufferData;

    public BufferQueue(int bufferCount, int bufferSize) {
        if (bufferCount > 0 && bufferSize > 0) {
            mBufferData = new BufferData[bufferCount];
            for (int i = 0; i < bufferCount; ++i) {
                mBufferData[i] = new BufferData(bufferSize);
            }

            mEmptyQueue = new Queue(bufferCount);
            mFullQueue = new Queue(bufferCount);

            mState = STATE_reset;
        } else {
            LogHelper.e(TAG, "BufferQueue param error, bufferCount:" + bufferCount + "  bufferSize:" + bufferSize);
        }
    }

    public void set() {
        if ( STATE_reset == mState ) {
            if (null != mEmptyQueue && null != mFullQueue) {
                for ( int i = 0; i < mBufferData.length; ++i ) {
                    mBufferData[i].reset();
                }

                mEmptyQueue.set(mBufferData);
                mFullQueue.set(null);

                mState = STATE_set;
            } else {
                LogHelper.e(TAG, "set queue is null");
            }
        }
    }

    public void reset() {
        if ( STATE_set == mState ) {
            if (null != mEmptyQueue && null != mFullQueue) {
                mState = STATE_reset;

                LogHelper.d(TAG, "reset start");
                mEmptyQueue.reset();
                mFullQueue.reset();

                LogHelper.d(TAG, "reset end");
            } else {
                LogHelper.e(TAG, "reset queue is null");
            }
        }
    }

    public BufferData getEmpty() {
        if ( STATE_set == mState ) {
            if (null != mEmptyQueue) {
                return mEmptyQueue.getBuffer();
            } else {
                LogHelper.e(TAG, "getEmpty queue is null");
            }
        }
        return null;
    }

    public boolean putEmpty(BufferData data) {
        if ( STATE_set == mState ) {
            if (null != mEmptyQueue) {
                return mEmptyQueue.putBuffer(data);
            } else {
                LogHelper.e(TAG, "putEmpty queue is null");
            }
        }
        return false;
    }

    public BufferData getFull() {
        if ( STATE_set == mState ) {
            if (null != mFullQueue) {
                return mFullQueue.getBuffer();
            } else {
                LogHelper.e(TAG, "getFull queue is null");
            }
        }
        return null;
    }

    public boolean putFull(BufferData data) {
        if (null != mFullQueue) {
            return mFullQueue.putBuffer(data);
        } else {
            LogHelper.e(TAG, "putFull queue is null");
        }
        return false;
    }
}
