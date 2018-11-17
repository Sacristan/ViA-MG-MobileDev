package com.teamtwo.md1;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AudioFragment extends Fragment {
    private static final String LOG_TAG = "AudioFragment";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String mFileName = null;

    private AudioRecordTest.RecordButton mRecordButton = null;
    private MediaRecorder mRecorder = null;

    private MediaPlayer mPlayer = null;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };

    Button recordButton;
    TextView currentRecordAudioNameTextView;

    private boolean isRecording = false;

    String currentAudioFileName;
    List<String> audioFilesList = new ArrayList<>();


    public void toggleRecord(){
        isRecording =!isRecording;

        String recordButtonText;


        if(isRecording) {
            startRecording();
            recordButtonText = "Stop Recording";
            currentRecordAudioNameTextView.setText("Recording " +currentAudioFileName + " ...");
        }
        else
        {
            stopRecording();
            recordButtonText = "Start Recording";
            currentRecordAudioNameTextView.setText("");
        }

        recordButton.setText(recordButtonText);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audio, container, false);

        ActivityCompat.requestPermissions(this.getActivity(), permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        recordButton = view.findViewById(R.id.recordAudioButton);
        currentRecordAudioNameTextView = view.findViewById(R.id.currentRecordAudioName);
        currentRecordAudioNameTextView.setText("");
        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                Log.d(LOG_TAG, "Audio Permission Granted!");
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
    }


    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        //TODO: FIX
//        if (start) {
//            startPlaying();
//        } else {
//            stopPlaying();
//        }
    }

    private void startPlaying(String filePath) {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(filePath);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private String getAudioPath(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "Rec_"+timeStamp;
        currentAudioFileName = fileName;
        return getContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/" + fileName;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(getAudioPath());
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

//    class RecordButton extends Button {
//        boolean mStartRecording = true;
//
//        OnClickListener clicker = new OnClickListener() {
//            public void onClick(View v) {
//                onRecord(mStartRecording);
//                if (mStartRecording) {
//                    setText("Stop recording");
//                } else {
//                    setText("Start recording");
//                }
//                mStartRecording = !mStartRecording;
//            }
//        };
//
//        public RecordButton(Context ctx) {
//            super(ctx);
//            setText("Start recording");
//            setOnClickListener(clicker);
//        }
//    }
//
//    class PlayButton extends Button {
//        boolean mStartPlaying = true;
//
//        OnClickListener clicker = new OnClickListener() {
//            public void onClick(View v) {
//                onPlay(mStartPlaying);
//                if (mStartPlaying) {
//                    setText("Stop playing");
//                } else {
//                    setText("Start playing");
//                }
//                mStartPlaying = !mStartPlaying;
//            }
//        };
//
//        public PlayButton(Context ctx) {
//            super(ctx);
//            setText("Start playing");
//            setOnClickListener(clicker);
//        }
//    }

}
