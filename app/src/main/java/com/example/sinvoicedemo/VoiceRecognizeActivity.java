package com.example.sinvoicedemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.libra.sinvoice.LogHelper;
import com.libra.sinvoice.SinVoiceRecognition;

public class VoiceRecognizeActivity extends Activity implements SinVoiceRecognition.Listener {
    private final static String TAG = "VoiceRecognizeActivity";

    private final static int MSG_SET_RECG_TEXT = 1;
    private final static int MSG_RECG_START = 2;
    private final static int MSG_RECG_END = 3;
    private final static int MSG_PLAY_TEXT = 4;

    private final static int[] TOKENS = {32, 32, 32, 32, 32, 32};
    private final static int TOKEN_LEN = TOKENS.length;

    private Handler mHanlder;
    private SinVoiceRecognition mRecognition;
    private TextView mRecognisedTextView;

    static {
        System.loadLibrary("sinvoice");
        LogHelper.d(TAG, "sinvoice jnicall loadlibrary");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize);

        mRecognition = new SinVoiceRecognition();
        mRecognition.init(this);
        mRecognition.setListener(this);

        mRecognisedTextView = (TextView) findViewById(R.id.regtext);
        mHanlder = new RegHandler();

        Button recognitionStart = (Button) findViewById(R.id.start_reg);
        recognitionStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mRecognition.start(TOKEN_LEN, false);
            }
        });

        Button recognitionStop = (Button) findViewById(R.id.stop_reg);
        recognitionStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mRecognition.stop();
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        mRecognition.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mRecognition.uninit();
    }

    private StringBuilder sb = new StringBuilder();

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
                    mRecognisedTextView.setText(sb.toString());
                    break;

                case MSG_PLAY_TEXT:
                    break;
            }
            super.handleMessage(msg);
        }
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

}
