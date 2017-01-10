package com.example.sinvoicedemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.example.sinvoicedemo.voice.VoiceRecognizeHelper;

public class VoiceRecognizeActivity extends Activity  {
    private final static String TAG = "VoiceRecognizeActivity";


    private TextView mRecognisedTextView;

    private VoiceRecognizeHelper voiceRecognizeHelper;

    static {
        System.loadLibrary("sinvoice");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize);

        voiceRecognizeHelper = new VoiceRecognizeHelper(this);
        voiceRecognizeHelper.init();



        mRecognisedTextView = (TextView) findViewById(R.id.regtext);


        Button recognitionStart = (Button) findViewById(R.id.start_reg);
        recognitionStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                voiceRecognizeHelper.startRecognize();
            }
        });

        Button recognitionStop = (Button) findViewById(R.id.stop_reg);
        recognitionStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                voiceRecognizeHelper.stopRecognize();
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        voiceRecognizeHelper.stopRecognize();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        voiceRecognizeHelper.destroy();
    }


}
