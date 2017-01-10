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

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.text.TextUtils;

public class SinVoiceRecognition implements Record.Listener, Record.Callback, VoiceRecognition.Listener, VoiceRecognition.Callback {
    private final static String TAG = "SinVoiceRecognition";

    private final static int STATE_START = 1;
    private final static int STATE_STOP = 2;

    private BufferQueue mBufferQueue;
    private Record mRecord;
    private VoiceRecognition mRecognition;

    private Thread mRecordThread;
    private Thread mRecognitionThread;
    private int mState;
    private Listener mListener;

    private String mCodeBook;

    public static interface Listener {
        void onSinVoiceRecognitionStart();

        void onSinVoiceRecognition(char ch);

        void onSinVoiceRecognitionEnd(int result);
    }

    public SinVoiceRecognition() {
        this(Common.DEFAULT_CODE_BOOK);
    }

    public SinVoiceRecognition(String codeBook) {
        this(codeBook, Common.DEFAULT_SAMPLE_RATE, Common.DEFAULT_BUFFER_COUNT);
    }

    public SinVoiceRecognition(String codeBook, int sampleRate, int bufferCount) {
        mState = STATE_STOP;

        int bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        LogHelper.d(TAG, "AudioRecordMinBufferSize:" + bufferSize + "  sampleRate:" + sampleRate);

        mBufferQueue = new BufferQueue(bufferCount, bufferSize);

        mRecord = new Record(this, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        mRecord.setListener(this);
        mRecognition = new VoiceRecognition(this, sampleRate);
        mRecognition.setListener(this);

        setCodeBook(codeBook);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void setCodeBook(String codeBook) {
        if (!TextUtils.isEmpty(codeBook) ) {
            mCodeBook = codeBook;
        }
    }

    public void init(Context context) {
        mRecognition.init(context);
    }

    public void uninit() {
        mRecognition.uninit();
    }

    public void start(final int tokenCount, final boolean isReadFromFile) {
        if (STATE_STOP == mState) {
            mState = STATE_START;

            mBufferQueue.set();

            mRecognitionThread = new Thread() {
                @Override
                public void run() {
                    mRecognition.start(tokenCount);
                }
            };
            if (null != mRecognitionThread) {
                mRecognitionThread.start();
            }

            mRecordThread = new Thread() {
                @Override
                public void run() {
                    mRecord.start(isReadFromFile);
                }
            };
            if (null != mRecordThread) {
                mRecordThread.start();
            }
        }
    }

    public void stop() {
        if (STATE_START == mState) {
            mState = STATE_STOP;

            LogHelper.d(TAG, "stop start");

            mRecord.stop();
            mRecognition.stop();
            mBufferQueue.reset();

            if (null != mRecordThread) {
                try {
                    LogHelper.d(TAG, "wait record thread exit");
                    mRecordThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    mRecordThread = null;
                }
            }

            if (null != mRecognitionThread) {
                try {
                    LogHelper.d(TAG, "wait recognition thread exit");
                    mRecognitionThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    mRecognitionThread = null;
                }
            }

            LogHelper.d(TAG, "stop end");
        }
    }

    @Override
    public void onStartRecord() {
        LogHelper.d(TAG, "start record");
    }

    @Override
    public void onStopRecord() {
        LogHelper.d(TAG, "stop record");
    }

    @Override
    public BufferData getRecordBuffer() {
        BufferData buffer = mBufferQueue.getEmpty();
        if (null == buffer) {
            LogHelper.e(TAG, "get null empty buffer");
        }
        return buffer;
    }

    @Override
    public void freeRecordBuffer(BufferData buffer) {
        if (null != buffer) {
            if (!mBufferQueue.putFull(buffer)) {
                LogHelper.e(TAG, "put full buffer failed");
            }
        }
    }

    @Override
    public BufferData getRecognitionBuffer() {
        BufferData buffer = mBufferQueue.getFull();
        if (null == buffer) {
            LogHelper.e(TAG, "get null full buffer");
        }
        return buffer;
    }

    @Override
    public void freeRecognitionBuffer(BufferData buffer) {
        if (null != buffer) {
            if (!mBufferQueue.putEmpty(buffer)) {
                LogHelper.e(TAG, "put empty buffer failed");
            }
        }
    }

    @Override
    public void onStartRecognition() {
        LogHelper.d(TAG, "start recognition");
    }

    @Override
    public void onRecognition(int index) {
        LogHelper.d(TAG, "recognition:" + index);
        if (null != mListener) {
            if (index >= 0) {
                if ( mRecognition.getMaxEncoderIndex() < 255 ) {
                    mListener.onSinVoiceRecognition(mCodeBook.charAt(index));
                } else {
                    mListener.onSinVoiceRecognition((char)index);
                }
            } else {
                LogHelper.d(TAG, "recognition: gIsError" + index);
                if ( VoiceDecoder.VOICE_DECODER_START == index ) {
                    mListener.onSinVoiceRecognitionStart();
                } else if ( VoiceDecoder.VOICE_DECODER_END == index ) {
                    mListener.onSinVoiceRecognitionEnd(-1);
                } else if ( index <= -3 ) {
                    mListener.onSinVoiceRecognitionEnd(-3 - index);
                } else {
                    LogHelper.d(TAG, "onRecognition error index" + index);
                }
            }
        }
    }

    @Override
    public void onStopRecognition() {
        LogHelper.d(TAG, "stop recognition");
    }

    public int getMaxEncoderIndex() {
        return mRecognition.getMaxEncoderIndex();
    }

}
