package com.example.sinvoicedemo.voice;


import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.libra.sinvoice.SinVoiceRecognition;

public class VoiceRecognizeHelper implements SinVoiceRecognition.Listener {

    private final static int MSG_SET_RECG_TEXT = 1;
    private final static int MSG_RECG_START = 2;
    private final static int MSG_RECG_END = 3;
    private final static int MSG_PLAY_TEXT = 4;

    private final static int[] TOKENS = {32, 32, 32, 32, 32, 32};
    private final static int TOKEN_LEN = TOKENS.length;

    private Context context;
    private Handler mHanlder;
    private SinVoiceRecognition mRecognition;

    private VoiceMsg voiceMsg = new VoiceMsg();
    private StringBuilder sb = new StringBuilder();

    public VoiceRecognizeHelper(Context context) {
        this.context = context;
    }

    public void init() {
        mRecognition = new SinVoiceRecognition();
        mRecognition.init(context);
        mRecognition.setListener(this);
        mHanlder = new RegHandler();

        voiceMsg.setListener(new VoiceMsgListener() {
            @Override
            public void onReceiveOver(String msg) {
                System.out.println("onReceiverOver:" + msg);
            }
        });
    }

    public void startRecognize() {
        mRecognition.start(TOKEN_LEN, false);
    }

    public void stopRecognize() {
        mRecognition.stop();
    }

    public void destroy() {
        mRecognition.uninit();
    }

    @Override
    public void onSinVoiceRecognitionStart() {
        mHanlder.sendEmptyMessage(MSG_RECG_START);
    }

    @Override
    public void onSinVoiceRecognition(char ch) {
        mHanlder.sendMessage(mHanlder.obtainMessage(MSG_SET_RECG_TEXT, ch, 0));
    }

    @Override
    public void onSinVoiceRecognitionEnd(int result) {
        mHanlder.sendMessage(mHanlder.obtainMessage(MSG_RECG_END, result, 0));
    }

    private class RegHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SET_RECG_TEXT:
                    char ch = (char) msg.arg1;
                    sb.append(ch);
                    break;

                case MSG_RECG_START:
                    break;

                case MSG_RECG_END:
                    System.out.println("receive:" + sb.toString());
                    voiceMsg.receiveMsg(sb.toString());
                    sb.delete(0, sb.length());
                    break;

                case MSG_PLAY_TEXT:
                    break;
            }
            super.handleMessage(msg);
        }
    }
}
