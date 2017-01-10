package com.example.sinvoicedemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.example.sinvoicedemo.voice.VoicePlayerHelper;

public class VoicePlayerActivity extends Activity {
    private final static String TAG = "MainActivity";

    private EditText mPlayTextView;

    private VoicePlayerHelper voicePlayerHelper;

    static {
        System.loadLibrary("sinvoice");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        voicePlayerHelper = new VoicePlayerHelper(this);
        voicePlayerHelper.init();

        mPlayTextView = (EditText) findViewById(R.id.playtext);
        mPlayTextView.setMovementMethod(ScrollingMovementMethod.getInstance());


        final Button playStart = (Button) findViewById(R.id.start_play);
        playStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                voicePlayerHelper.startPlay();
            }
        });

        Button playStop = (Button) findViewById(R.id.stop_play);
        playStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                voicePlayerHelper.stopPlay();
            }
        });

        findViewById(R.id.btn_to_recognize).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(VoicePlayerActivity.this, VoiceRecognizeActivity.class));
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        voicePlayerHelper.stopPlay();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        voicePlayerHelper.destroy();
    }

}
