/*
 * Copyright (C) 2014 gujicheng
 * 未经作者许可，禁止将该程序用于商业用途
 * 
 * 该声波通信程序在前一个开源版本（SinVoice）的基础上，做了许多优化：
 * 优化如下：
 * 1. 识别效率更高，几乎达到100%，完全可以达到商业用途标准，比chirp，支付宝，茄子快传等软件的识别效率更高。
 * 2. 能支持更多复杂场景的识别，在有嘈杂大声的背景音乐，嘈杂的会议室，食堂，公交车，马路，施工场地，
 *     小汽车，KTV等一些复杂的环境下，依然能保持很高的识别率。
 * 3. 能支持更多token的识别，通过编码可以传送所有字符。
 * 4. 通过定制可以实现相同字符的连续传递,比如“234456”。
 * 5. 支持自动纠错功能，在有3个以内字符解码出错的情况下可以自动纠正。
 * 6. 程序运行效率非常高，可以用于智能手机，功能手机，嵌入式设备，PC，平板等嵌入式系统上。
 * 7. 声波的频率声音和音量可定制。
 * 
 * 此demo程序属于试用性质程序，仅具备部分功能，其限制如下：
 * 1. 仅支持部分字符识别。
 * 2. 识别若干次后，程序会自动停止识别。若想继续使用，请停止该程序，然后重新启动程序。
 * 3. 不支持连续字符传递。
 * 4. 不支持自动纠错功能。
 * 5. 禁止用于商业用途。
 * 
 * 若您对完整的声波通信程序感兴趣，请联系作者获取商业授权版本（仅收取苦逼的加班费）。
 *************************************************************************
 **                   作者信息                                                            **
 *************************************************************************
 ** Email: gujicheng197@126.com                                        **
 ** QQ   : 29600731                                                                 **
 ** Weibo: http://weibo.com/gujicheng197                          **
 *************************************************************************
 */
package com.libra.sinvoice;

public class Queue {
    private final static String TAG = "DataQueue";

    private final static int STATE_reset = 1;
    private final static int STATE_set = 2;

    private final int mResCount;
    private BufferData[] mBufferData;

    private int mState;

    private int mStartIndex;
    private int mEndIndex;
    private int mCount;

    public Queue(int resCount) {
        mResCount = resCount;

        mState = STATE_reset;

        if (mResCount > 0) {
            mBufferData = new BufferData[mResCount];
        }

        mCount = 0;
        mStartIndex = 0;
        mEndIndex = 0;
    }

    public synchronized void reset() {
        if (STATE_set == mState) {
            mState = STATE_reset;

            mStartIndex = 0;
            mEndIndex = 0;
            mCount = 0;

            this.notifyAll();
            LogHelper.d(TAG, "gujicheng reset ok");
        } else {
            LogHelper.d(TAG, "already reseted");
        }
    }

    public synchronized void set(BufferData[] data) {
        if (STATE_reset == mState) {
            mState = STATE_set;
            if (null != data) {
                mCount = data.length;

                if ( mCount > mResCount ) {
                    mCount = mResCount;
                }
                for (int i = 0; i < mCount; ++i) {
                    mBufferData[i] = data[i];
                }
            } else {
                mCount = 0;
            }

            LogHelper.d(TAG, "set ok");
        } else {
            LogHelper.d(TAG, "already seted");
        }
    }

    final public int getCount() {
        return mCount;
    }

    public synchronized BufferData getBuffer() {
        BufferData ret = null;

        if (STATE_set == mState) {
            if (mCount <= 0) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
                if (STATE_reset == mState) {
                    LogHelper.d(TAG, "getBuffer, after waiing, state is reset");
                    return null;
                }
            }

            if ( mCount > 0 ) {
                ret = mBufferData[mStartIndex++];
                if (mStartIndex >= mResCount) {
                    mStartIndex = 0;
                }

                --mCount;
                if ( mCount + 1 == mResCount ) {
                    this.notify();
                }
            } else {
                LogHelper.e(TAG, "getBuffer error mCount:" + mCount);
            }
        } else {
            LogHelper.d(TAG, "getBuffer, state is reset");
        }
        return ret;
    }

    public synchronized boolean putBuffer(BufferData data) {
        boolean ret = false;
        if (STATE_set == mState ) {
            if ( mCount == mResCount ) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return false;
                }
                if (STATE_reset == mState) {
                    LogHelper.d(TAG, "putBuffer, after waiing, state is reset");
                    return false;
                }
            }

            if (mCount < mResCount) {
                mBufferData[mEndIndex++] = data;
                if (mEndIndex >= mResCount) {
                    mEndIndex = 0;
                }

                ++mCount;
                if (0 == mCount - 1) {
                    this.notify();
                }

                ret = true;
            } else {
                LogHelper.e(TAG, "putBuffer error mCount:" + mCount);
            }
        } else {
            LogHelper.d(TAG, "putBuffer, state is reset or data is null");
        }
        return ret;
    }
}
