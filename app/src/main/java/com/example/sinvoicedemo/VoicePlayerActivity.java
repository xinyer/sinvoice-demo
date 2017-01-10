package com.example.sinvoicedemo;

import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.libra.sinvoice.Common;
import com.libra.sinvoice.LogHelper;
import com.libra.sinvoice.SinVoicePlayer;

public class VoicePlayerActivity extends Activity implements SinVoicePlayer.Listener {
    private final static String TAG = "MainActivity";

    private final static int[] TOKENS = {32, 32, 32, 32, 32, 32};
    private final static int TOKEN_LEN = TOKENS.length;

    private SinVoicePlayer mSinVoicePlayer;
    private EditText mPlayTextView;

    private boolean play;
    private String[] content = new String[] {"abcdefghi", "jklmnopq", "rstuvwxyz", "1234567890"};
    private int playIndex;

    static {
        System.loadLibrary("sinvoice");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mSinVoicePlayer = new SinVoicePlayer();
        mSinVoicePlayer.init(this);
        mSinVoicePlayer.setListener(this);

        mPlayTextView = (EditText) findViewById(R.id.playtext);
        mPlayTextView.setMovementMethod(ScrollingMovementMethod.getInstance());

        Button playStart = (Button) findViewById(R.id.start_play);
        playStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                play = true;
                playIndex = 0;
                sendVoice(content[0]);
            }
        });

        Button playStop = (Button) findViewById(R.id.stop_play);
        playStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mSinVoicePlayer.stop();
            }
        });

        findViewById(R.id.btn_to_recognize).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(VoicePlayerActivity.this, VoiceRecognizeActivity.class));
            }
        });
    }

    private void sendVoice(String msg) {
        if (TextUtils.isEmpty(msg)) return;
        System.out.println("sendVoice:" + msg);
        try {
            byte[] strs = msg.getBytes("UTF8");
            int len = strs.length;
            int[] tokens = new int[len];
            int maxEncoderIndex = mSinVoicePlayer.getMaxEncoderIndex();
            LogHelper.d(TAG, "maxEncoderIndex:" + maxEncoderIndex);
//            String encoderText = mPlayTextView.getText().toString();
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

    private static final int SPLIT_LEN = 10;

    private String[] splitVoiceMsg(String msg) {
        if (msg.length() <= SPLIT_LEN) return new String[]{msg};

        int len = msg.length() / SPLIT_LEN + 1;
        String[] result = new String[len];
        for (int i = 0; i < len; i++) {
            if (i == len - 1) result[i] = msg.substring(i * SPLIT_LEN);
            else result[i] = msg.substring(i * SPLIT_LEN, (i + 1) * SPLIT_LEN);
        }
        return result;
    }

    @Override
    public void onPause() {
        super.onPause();
        play = false;
        mSinVoicePlayer.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        play = false;
        mSinVoicePlayer.uninit();
    }

    @Override
    public void onSinVoicePlayStart() {

    }

    @Override
    public void onSinVoicePlayEnd() {
        mSinVoicePlayer.uninit();
        playIndex = (playIndex + 1) % content.length;
        if (play) sendVoice(content[playIndex]);
    }

    @Override
    public void onSinToken(int[] tokens) {

    }

}
