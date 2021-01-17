package com.sudip.drumkitapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    Map<Integer, Integer> map = new HashMap<>();
    SoundPool soundPool;
    MediaRecorder mMediaRecorder;
    private ExecutorService mExecutorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();

        mExecutorService = Executors.newSingleThreadExecutor();
        ViewGroup group = ((ViewGroup) findViewById(R.id.layout));
        soundPool = new SoundPool(200, AudioManager.STREAM_MUSIC, 0);
        for (int i = 0; i < group.getChildCount(); i++) {
            int resourceId = getResources().getIdentifier(group.getResources().getResourceEntryName(group.getChildAt(i).getId()), "raw", "com.sudip.drumkitapp");
            map.put(resourceId, soundPool.load(this, resourceId, 1));
        }


        for (int i = 0; i < group.getChildCount(); i++) {
            group.getChildAt(i).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        int id = view.getId();
                        String ourId = view.getResources().getResourceEntryName(id);

                        int resourceId = getResources().getIdentifier(ourId, "raw", "com.sudip.drumkitapp");
//                        SoundPoolPlayer mPlayer = SoundPoolPlayer.create(MainActivity.this, resourceId);
//                        mPlayer.play();
                        int soundId = getSoundPool(resourceId);
                        soundPool.play(soundId, 1, 1, 1, 0, 1);
                    }
                    return true;
                }
            });
        }


        findViewById(R.id.record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((TextView) v).getText().equals("record")) {
                    startRecorder();
                    ((TextView) v).setText("lift to stop");
                } else if (((TextView) v).getText().equals("lift to stop")) {
                    stopRecorder();
                    ((TextView) v).setText("record");
                }
            }
        });

        findViewById(R.id.play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    FileInputStream fileInputStream = new FileInputStream(new File(getFilesDir() + "/recorderdemo", "record.m4a"));
                    mediaPlayer.setDataSource(fileInputStream.getFD());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "MediaPlayer init error !", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    public Integer getSoundPool(int id) {
        return map.get(id);
    }


    private void startRecorder() {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                releaseRecorder();

                if (!doStart()) {
                    recorderFial();
                }
            }
        });
    }


    private void releaseRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }


    private boolean doStart() {

        try {
            mMediaRecorder = new MediaRecorder();
            File file = new File(getFilesDir()
                    + "/recorderdemo/record.m4a");
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
            file.createNewFile();


            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mMediaRecorder.setAudioSamplingRate(44100);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setAudioEncodingBitRate(96000);
            mMediaRecorder.setOutputFile(file.getAbsolutePath());

            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "record failed，retry plz", Toast.LENGTH_SHORT).show();
            return false;
        }


        return true;
    }

    private boolean doStop() {
        try {
            mMediaRecorder.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


    private void recorderFial() {
    }


    private void stopRecorder() {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (!doStop()) {
                    recorderFial();
                }
                releaseRecorder();

            }
        });
    }


    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, permissions, 200);
                    return;
                }
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && requestCode == 200) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, 200);
                    return;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 200) {
            checkPermission();
        }
    }
}