package com.teamtwo.md1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
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
import java.util.Date;

public class AudioFragment extends Fragment {
    private static final String LOG_TAG = "AudioFragment";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

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
    LinearLayout audioButtonContainer;

    private boolean isRecording = false;

    String currentAudioFileName;

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

            addNewRecordItem();
        }

        recordButton.setText(recordButtonText);
    }

    private void addNewRecordItem(){
        Button itemButton = new Button(this.getContext());
        itemButton.setText(currentAudioFileName);

        itemButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String p = getAudioFilePath(currentAudioFileName);
                startPlaying(p);
            }
        });

        audioButtonContainer.addView(itemButton, 0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audio, container, false);

        ActivityCompat.requestPermissions(this.getActivity(), permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        recordButton = view.findViewById(R.id.recordAudioButton);
        audioButtonContainer = view.findViewById(R.id.audioButtonContainer);
        currentRecordAudioNameTextView = view.findViewById(R.id.currentRecordAudioName);
        currentRecordAudioNameTextView.setText("");
        return view;
    }

    //Cleanup Audio files
    @Override
    public void onDestroy() {
        super.onDestroy();
        File storageDir = getContext().getExternalFilesDir("Audio");
        MainActivity.traverse(storageDir);
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

    private void startPlaying(String filePath) {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(filePath);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "startPlaying prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private File getAudioStorageDir() {
        return getContext().getExternalFilesDir("Audio");
    }

    private String getAudioFilePath(String name){
        File dir = getAudioStorageDir();
        return dir.getAbsolutePath() + "/" + name;
    }

    private File createAudioFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "Rec_" + timeStamp;
        String suffix = ".3gp";

        File storageDir =  getAudioStorageDir();

        Log.i(LOG_TAG, "STORAGE DIR: "+storageDir.getAbsolutePath());

        File file = File.createTempFile(
                fileName,  /* prefix */
                suffix,
                storageDir      /* directory */
        );

        Log.i(LOG_TAG, "Created file: "+file.getAbsolutePath());

        currentAudioFileName = fileName + suffix;

        return file;
    }

    private void startRecording() {
        File audioFile = null;
        try {
            audioFile = createAudioFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mRecorder = new MediaRecorder();
//        mRecorder.setCaptureRate(30);
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(audioFile.getAbsolutePath());
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        mRecorder.setMaxDuration(50000);
        mRecorder.setMaxFileSize(5000000);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "startRecording prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }
}
