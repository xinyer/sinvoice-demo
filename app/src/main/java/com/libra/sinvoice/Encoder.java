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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

public class Encoder implements VoiceEncoder.Callback {
    private final static String TAG = "Encoder";

    private final static int STATE_START = 1;
    private final static int STATE_STOPED = 2;

    public static interface Callback {
        void freeEncodeBuffer(BufferData buffer);

        BufferData getEncodeBuffer();
    }

    public static interface Listener {
        void onStartEncode();

        void onBeginEncode();

        void onEndEncode();

        void onFinishEncode();

        void onSinToken(int[] tokens);
    }

    private Listener mListener;
    private FileOutputStream mFileOut;
    private int mState;
    private VoiceEncoder mVoiceEncoder;
    private BufferData mBuf;
    private Callback mCallback;
    private Context mContext;

    public Encoder(Callback callback) {
        mState = STATE_STOPED;
        mCallback = callback;
        mVoiceEncoder = new VoiceEncoder(this);
    }

    public void setListener(Listener listener) {
        mListener = listener;
        LogHelper.d(TAG, "setListener onSinToken " + mListener);
    }

    public void init(Context context) {
        mContext = context;
    }

    public void uninit() {
    }

    public final boolean isStoped() {
        return (STATE_STOPED == mState);
    }

    public int getMaxEncoderIndex() {
        return mVoiceEncoder.getMaxEncoderIndex();
    }

    public void start(int sampleRate, int bufferSize, int[] tokens, int tokenLen, int muteInterval, boolean repeat) {
        LogHelper.d(TAG, "start");

        if ( STATE_STOPED == mState ) {
            mState = STATE_START;

            mVoiceEncoder.initSV(mContext, "com.sinvoice.demo", "SinVoice");

            LogHelper.d("gujicheng", "encode start SV");
            boolean needRepeat = false;
            mVoiceEncoder.startSV(sampleRate, bufferSize);

            try {
                 String sdcardPath = Environment.getExternalStorageDirectory().getPath();
                 if ( !TextUtils.isEmpty(sdcardPath) ) {
                     mFileOut = new FileOutputStream( String.format("%s/player.pcm", sdcardPath));
                 }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if ( null != mListener ) {
                mListener.onBeginEncode();
            }

            do {
                LogHelper.d(TAG, "encode start");
                encode(tokens, tokenLen, muteInterval, needRepeat);
                needRepeat = repeat;
                LogHelper.d(TAG, "encode end");
            } while (repeat && (STATE_START == mState));
            stop();
            mVoiceEncoder.stopSV();

            if ( null != mListener ) {
                mListener.onFinishEncode();
            }

            if ( null != mFileOut ) {
                try {
                    mFileOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            mVoiceEncoder.uninitSV();
        }

        LogHelper.d(TAG, "stop");
    }

    private void encode(int[] tokens, int tokenLen, int muteInterval, boolean isRepeat) {
        if (STATE_START == mState) {
            if (null != mListener) {
                mListener.onStartEncode();
            }

            for ( int i = 0; i < tokenLen; ++i ) {
                LogHelper.d("jichengtoken", "send i" + i + "  token:" + tokens[i]);
            }
            mVoiceEncoder.genRate(tokens, tokenLen);

            // for mute
            if ( muteInterval > 0 ) {
                int interval = 0;
                while ( STATE_START == mState && interval < muteInterval ) {
                    interval += 20;
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (STATE_START == mState) {
//                genRateMute(muteInterval);
            } else {
                LogHelper.d(TAG, "encode force stop");
            }

            if (null != mListener) {
                mListener.onEndEncode();
            }
        }
    }

    public void stop() {
        LogHelper.d(TAG, "stop start");
        if (STATE_START == mState) {
            mState = STATE_STOPED;
            LogHelper.d("gujicheng", "encode stop SV");
//            mVoiceEncoder.stopSV();

            // for last pcmplay
            mCallback.freeEncodeBuffer(null);
        }
        LogHelper.d(TAG, "stop end");
    }

    @Override
    public void freeVoiceEncoderBuffer(int filledBytesSize) {
        if (null != mCallback) {
            LogHelper.d(TAG, "onFreeBuffer start");
            if ( null != mFileOut ) {
                try {
                    mFileOut.write(mBuf.mData, 0, filledBytesSize);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mBuf.setFilledSize(filledBytesSize);
            mCallback.freeEncodeBuffer(mBuf);
            LogHelper.d(TAG, "onFreeBuffer end ");
        }
    }

    @Override
    public byte[] getVoiceEncoderBuffer() {
        LogHelper.d(TAG, "onGetBuffer");
        if (null != mCallback) {
            LogHelper.d(TAG, "onGetBuffer start");
            BufferData data = mCallback.getEncodeBuffer();
            if ( null != data ) {
                mBuf = data;
                LogHelper.d(TAG, "onGetBuffer end :" + data.mData + "   len:" + data.mData.length);
                return data.mData;
            }
        }

        return null;
    }

    @Override
    public void onVoiceEncoderToken(int[] tokens) {
        LogHelper.d(TAG, "onSinToken");
        if (null != tokens && null != mListener) {
            LogHelper.d(TAG, "onSinToken " + tokens.length);

            mListener.onSinToken(tokens);
        }
    }
}
