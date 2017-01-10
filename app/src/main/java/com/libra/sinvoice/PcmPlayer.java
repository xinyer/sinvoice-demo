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

import android.media.AudioManager;
import android.media.AudioTrack;

public class PcmPlayer {
    private final static String TAG = "PcmPlayer";
    private final static int STATE_START = 1;
    private final static int STATE_STOP = 2;

    private int mState;
    private boolean mStarted;
    private Listener mListener;
    private Callback mCallback;

    private int mSampleRate;
    private int mChannel;
    private int mFormat;
    private int mBufferSize;

    public static interface Listener {
        void onPlayStart();

        void onPlayStop();
    }

    public static interface Callback {
        BufferData getPlayBuffer();

        void freePlayData(BufferData data);
    }

    public PcmPlayer(Callback callback, int sampleRate, int channel,
            int format, int bufferSize) {
        mSampleRate = sampleRate;
        mChannel = channel;
        mFormat = format;
        mBufferSize = bufferSize;

        mCallback = callback;
        mState = STATE_STOP;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void start() {
        LogHelper.d(TAG, "start");
        if (STATE_STOP == mState ) {
            mStarted = false;

            if (null != mCallback) {
                mState = STATE_START;
                LogHelper.d(TAG, "start");
                if (null != mListener) {
                    mListener.onPlayStart();
                }

                AudioTrack mAudio = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate, mChannel,
                        mFormat, 3 * mBufferSize, AudioTrack.MODE_STREAM);

                while (STATE_START == mState) {
                    LogHelper.d(TAG, "start getbuffer");

                    long start = System.currentTimeMillis();
                    BufferData data = mCallback.getPlayBuffer();
                    long d = System.currentTimeMillis() - start;
                    LogHelper.d(TAG, "PcmPlayerTime getBuffer:" + d);
                    if (null != data) {
                        if (null != data.mData) {

                            start = System.currentTimeMillis();
                            int len = mAudio.write(data.mData, 0,
                                    data.getFilledSize());
                            if (len != data.getFilledSize()) {
                                LogHelper.e(
                                        TAG,
                                        "PcmPlayerTime writedata, write is invalidate len:"
                                                + len + "   filledSize:"
                                                + data.getFilledSize());
                            }
                            d = System.currentTimeMillis() - start;
                            LogHelper.d(TAG, "PcmPlayerTime writedata:" + d);

                            if (!mStarted) {
                                mStarted = true;
                                mAudio.play();
                            }
                            start = System.currentTimeMillis();
                            mCallback.freePlayData(data);
                            d = System.currentTimeMillis() - start;
                            LogHelper.d(TAG, "PcmPlayerTime freeBuffer:" + d);
                        } else {
                            // it is the end of input, so need stop
                            LogHelper.d(TAG,
                                    "it is the end of input, so need stop");
                            break;
                        }
                    } else {
                        LogHelper.e(TAG, "get null data");
                        break;
                    }
                }

                LogHelper.e(TAG, "audio stop");
                if (null != mAudio) {
                    mAudio.flush();
                    mAudio.stop();
                    mAudio.release();
                    mAudio = null;
                }
                mState = STATE_STOP;
                if (null != mListener) {
                    mListener.onPlayStop();
                }
                LogHelper.d(TAG, "pcm end");
            }
        }
    }

    public void stop() {
        if (STATE_START == mState) {
            mState = STATE_STOP;
        }
    }
}
