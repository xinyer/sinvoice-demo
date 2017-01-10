package com.example.sinvoicedemo.voice;


import android.content.Context;
import android.text.TextUtils;

import com.libra.sinvoice.Common;
import com.libra.sinvoice.LogHelper;
import com.libra.sinvoice.SinVoicePlayer;

import java.io.UnsupportedEncodingException;

public class VoicePlayerHelper implements SinVoicePlayer.Listener {

    private final static int[] TOKENS = {32, 32, 32, 32, 32, 32};
    private final static int TOKEN_LEN = TOKENS.length;

    private Context context;
    private SinVoicePlayer mSinVoicePlayer;

    private boolean play;
    private String content = "abcdefg1234567890ABCDEFG";
    private String[] contents;
    private int playIndex;

    public VoicePlayerHelper(Context context) {
        this.context = context;
    }

    public void init() {
        mSinVoicePlayer = new SinVoicePlayer();
        mSinVoicePlayer.init(context);
        mSinVoicePlayer.setListener(this);

        contents = VoiceMsg.splitVoiceMsg(content);
    }

    private void sendVoice(String msg) {
        if (TextUtils.isEmpty(msg)) return;
        System.out.println("sendVoice:" + msg);
        try {
            byte[] strs = msg.getBytes("UTF8");
            int len = strs.length;
            int[] tokens = new int[len];
            int maxEncoderIndex = mSinVoicePlayer.getMaxEncoderIndex();
            for (int i = 0; i < len; i++) {
                if (maxEncoderIndex < 255) {
                    tokens[i] = Common.DEFAULT_CODE_BOOK.indexOf(msg.charAt(i));
                } else {
                    tokens[i] = strs[i];
                }
            }
            mSinVoicePlayer.play(tokens, len, false, 3000);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void startPlay() {
        play = true;
        playIndex = 0;
        sendVoice(contents[0]);
    }

    public void stopPlay() {
        play = false;
        mSinVoicePlayer.stop();
    }

    public void destroy() {
        play = false;
        mSinVoicePlayer.uninit();
    }


    @Override
    public void onSinVoicePlayStart() {

    }

    @Override
    public void onSinVoicePlayEnd() {
        mSinVoicePlayer.uninit();
        playIndex = (playIndex + 1) % contents.length;
        if (play) sendVoice(contents[playIndex]);
    }

    @Override
    public void onSinToken(int[] tokens) {

    }
}
