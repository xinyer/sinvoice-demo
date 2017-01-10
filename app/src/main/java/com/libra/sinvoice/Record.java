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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class Record {
    private final static String TAG = "Record";
    private final static int STATE_START = 1;
    private final static int STATE_STOP = 2;

/*    public final static int BITS_8 = 1;
    public final static int BITS_16 = 2;

    public final static int CHANNEL_1 = 1;
    public final static int CHANNEL_2 = 2;
*/
    private final static String FILE_PATH = "/sdcard/sinvoice_record/sinvoice.pcm";

    private int mState;

    private int mSampleRate = 441000;
    private int mBufferSize;

    private int mChannelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int mAudioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    private Listener mListener;
    private Callback mCallback;

    public static interface Listener {
        void onStartRecord();

        void onStopRecord();
    }

    public static interface Callback {
        BufferData getRecordBuffer();

        void freeRecordBuffer(BufferData buffer);
    }

    public Record(Callback callback, int sampleRate, int channel, int bits, int bufferSize) {
        mState = STATE_STOP;

        mCallback = callback;
        mSampleRate = sampleRate;
        mBufferSize = bufferSize;

        mChannelConfig = channel;
        mAudioEncoding = bits;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    private void recordFromDevice() {
        LogHelper.d(TAG, "recordFromDevice Start");

        if (mBufferSize > 0) {
            AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.MIC, mSampleRate, mChannelConfig, mAudioEncoding, mBufferSize * 5);
            if (null != record) {
                try {
                    mState = STATE_START;
                    LogHelper.d(TAG, "record start");
                    record.startRecording();
                    LogHelper.d(TAG, "record start 1");

                    if (null != mCallback) {
                        if (null != mListener) {
                            mListener.onStartRecord();
                        }

                        while (STATE_START == mState) {
                            BufferData data = mCallback.getRecordBuffer();
                            if (null != data) {
                                if (null != data.mData) {
                                    int bufferReadResult = record.read(data.mData, 0, mBufferSize);
                                    LogHelper.d(TAG, "read record:" + bufferReadResult);
                                    data.setFilledSize(bufferReadResult);

                                    mCallback.freeRecordBuffer(data);
                                } else {
                                    // end of input
                                    LogHelper.d(TAG, "get end input data, so stop");
                                    break;
                                }
                            } else {
                                LogHelper.e(TAG, "get null data");
                                break;
                            }
                        }

                        if (null != mListener) {
                            mListener.onStopRecord();
                        }
                    }

                    LogHelper.d(TAG, "stop record");
                    record.stop();
                    LogHelper.d(TAG, "release record");
                    record.release();

                    LogHelper.d(TAG, "record stop");
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    LogHelper.e(TAG, "start record error");
                }
                mState = STATE_STOP;
            }
        } else {
            LogHelper.e(TAG, "bufferSize is too small");
        }

        LogHelper.d(TAG, "recordFromDevice End");
    }

    private void recordFromFile() {
        LogHelper.d(TAG, "recordFromFile Start thread id:" + Thread.currentThread());

        mState = STATE_START;
        if (null != mCallback) {
            if (null != mListener) {
                mListener.onStartRecord();
            }

            File file = new File(FILE_PATH);
            FileInputStream fis;
            try {
                fis = new FileInputStream(file);
                while (STATE_START == mState) {
                    BufferData data = mCallback.getRecordBuffer();
                    if (null != data) {
                        if (null != data.mData) {
                            LogHelper.d(TAG, "recordFromFile read start");
                            int bufferReadResult = fis.read(data.mData);
                            if ( bufferReadResult >= 0 ) {
                                LogHelper.d(TAG, "recordFromFile read size:" + bufferReadResult + "  data len:" + data.mData.length);

                                data.setFilledSize(bufferReadResult);

                                mCallback.freeRecordBuffer(data);
                            } else {
                                LogHelper.d(TAG, "recordFromFile end of file");
                                // stop
//                                mState = STATE_STOP;
                                break;
                            }
                        } else {
                            // end of input
                            LogHelper.d(TAG, "get end input data, so stop");
                            break;
                        }
                    } else {
                        LogHelper.e(TAG, "get null data");
                        break;
                    }
                }

                fis.close();
            } catch (FileNotFoundException e1) {
//                Toast.makeText(null, "fefef", Toast.LENGTH_SHORT).show();
                e1.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (null != mListener) {
                mListener.onStopRecord();
            }
        }

        LogHelper.d(TAG, "recordFromFile End");
    }

    public void start(boolean isReadFromFile) {
        if (STATE_STOP == mState) {
            if (isReadFromFile) {
                recordFromFile();
            } else {
                recordFromDevice();
            }
        }
    }

    public int getState() {
        return mState;
    }

    public void stop() {
        if (STATE_START == mState) {
            mState = STATE_STOP;
        }
    }
}
