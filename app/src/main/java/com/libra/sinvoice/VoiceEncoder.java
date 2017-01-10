/*
 * Copyright (C) 2014 gujicheng
 * 未经作者许可，禁止将该程序用于商业用途
 * 
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

public class VoiceEncoder {
    private final static String TAG = "VoiceEncoder";
    private Callback mCallback;
    private int mNativeEncoder;

    public final static int TYPE_1 = 1;
    public final static int TYPE_2 = 2;

    public static interface Callback {
        void freeVoiceEncoderBuffer(int filledBytesSize);

        byte[] getVoiceEncoderBuffer();

        void onVoiceEncoderToken(int[] tokens);
    }

    public native void initSV(Context context, String companyId, String appId);

    public native void startSV(int sampleRate, int bufferBytesLen);

    public native void genRate(int[] tokens, int tokenLen);

    public native void stopSV();

    public native void uninitSV();

    public native int getMaxEncoderIndex();

    public VoiceEncoder(Callback callback) {
        mCallback = callback;
    }

    private byte[] onGetBuffer() {
        LogHelper.d(TAG, "onGetBuffer");
        if (null != mCallback) {
            return mCallback.getVoiceEncoderBuffer();
        }

        return null;
    }

    private void onFreeBuffer(int filledBytesSize) {
        if (null != mCallback) {
            mCallback.freeVoiceEncoderBuffer(filledBytesSize);
        }
    }

    private void onSinToken(int[] tokens) {
        LogHelper.d(TAG, "onSinToken");
        if (null != tokens && null != mCallback) {
            LogHelper.d(TAG, "onSinToken " + tokens.length);

            mCallback.onVoiceEncoderToken(tokens);
        }
    }

}
