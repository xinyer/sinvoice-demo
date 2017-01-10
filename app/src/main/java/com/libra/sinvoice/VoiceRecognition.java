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

public class VoiceRecognition implements VoiceDecoder.Callback {
    private final static String TAG = "Recognition";

    private final static int STATE_START = 1;
    private final static int STATE_STOP = 2;

    private int mState;
    private Listener mListener;
    private Callback mCallback;

    private int mSampleRate;
    private FileOutputStream mFileOut;
    private VoiceDecoder mVoiceDecoder;
    private Context mContext;
    private long mConsumedSize = 0;

    public static interface Listener {
        void onStartRecognition();

        void onRecognition(int index);

        void onStopRecognition();
    }

    public static interface Callback {
        BufferData getRecognitionBuffer();

        void freeRecognitionBuffer(BufferData buffer);
    }

    public VoiceRecognition(Callback callback, int SampleRate) {
        mState = STATE_STOP;

        mCallback = callback;
        mSampleRate = SampleRate;

        mVoiceDecoder = new VoiceDecoder(this);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void init(Context context) {
        mContext = context;
    }

    public void uninit() {
    }

    public void start(int tokenCount) {
        if (STATE_STOP == mState) {
            mConsumedSize = 0;

            mVoiceDecoder.initVR(mContext, "com.sinvoice.demo", "SinVoice");
            LogHelper.d(
                    TAG,
                    "Voice Recogintiono start threadid:"
                            + Thread.currentThread());
            if (null != mCallback) {
                mState = STATE_START;

                if (null != mListener) {
                    mListener.onStartRecognition();
                }
                try {
                    String sdcardPath = Environment.getExternalStorageDirectory().getPath();
                    if ( !TextUtils.isEmpty(sdcardPath) ) {
                        mFileOut = new FileOutputStream(String.format("%s/record.pcm", sdcardPath));
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                LogHelper.d(TAG, "Voice Recogintion startVR");
                mVoiceDecoder.startVR(mSampleRate, tokenCount);
                LogHelper.d(TAG, "Voice Recogintion start VR End");
                while (STATE_START == mState) {
                    BufferData data = mCallback.getRecognitionBuffer();
                    if (null != data) {
                        if (null != data.mData) {
                            LogHelper.d(TAG, "putData data:" + data + " filledSize:" + data.getFilledSize());
                            mConsumedSize += data.getFilledSize();
                            LogHelper.d(TAG, "VoiceRecognition putData data mConsumedSize:" + mConsumedSize);
                            mVoiceDecoder.putData(data.mData, data.getFilledSize());

                            mCallback.freeRecognitionBuffer(data);
                            if (null != mFileOut) {
                                try {
                                    mFileOut.write(data.mData);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            LogHelper.d(TAG, "end input buffer, so stop");
                            break;
                        }
                    } else {
                        LogHelper.e(TAG, "get null recognition buffer");
                        break;
                    }
                }

                mVoiceDecoder.stopVR();
                if (null != mFileOut) {
                    try {
                        mFileOut.close();
                        mFileOut = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                mState = STATE_STOP;
                if (null != mListener) {
                    mListener.onStopRecognition();
                }
            }

            mVoiceDecoder.uninitVR();
        }
    }

    public int getMaxEncoderIndex() {
        return mVoiceDecoder.getMaxEncoderIndex();
    }

    public void stop() {
        if (STATE_START == mState) {
            mState = STATE_STOP;
        }
    }

    @Override
    public void onVoiceDecoderResult(int index) {
        LogHelper.d("VoiceRecognition", "onRecognized:" + index);
        if (null != mListener) {
            LogHelper.d("jichengtoken", "receive  token:" + index);

            mListener.onRecognition(index);
        }
    }
}
