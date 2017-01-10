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

public class VoiceDecoder {
    private Callback mCallback;
    private int mNativeDecoder;

    public final static int TYPE_1 = 1;
    public final static int TYPE_2 = 2;

    public final static int VOICE_DECODER_START = -1;
    public final static int VOICE_DECODER_END = -2;

    public static interface Callback {
        void onVoiceDecoderResult(int index);
    }

    public native void initVR(Context context, String companyId, String appId);

    public native void startVR(int sampleRate, int tokenLen);

    public native void putData(byte[] data, int bytesLen);

    public native void stopVR();

    public native void uninitVR();

    public native int getMaxEncoderIndex();

    public VoiceDecoder(Callback callback) {
        mCallback = callback;
        mNativeDecoder = 0;
    }

    private void onRecognized(int index) {
        LogHelper.d("VoiceRecognition", "onRecognized:" + index);
        if (null != mCallback) {
            mCallback.onVoiceDecoderResult(index);
        }
    }

}
