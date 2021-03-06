package com.sudip.drumkitapp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.AudioRecord;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

/**
 * @author: Xu
 * @create: 2021-07-02 15:53
 **/
public class ForegroundService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        MediaProjectionManager manager = null;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            int code = intent.getIntExtra("code", -1);
            Intent intentData = intent.getParcelableExtra("data");
            manager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            MediaProjectionManager finalManager = manager;
            new Thread(){
                @Override
                public void run() {
                    MediaProjection mediaProjection = finalManager.getMediaProjection(code, intentData);
                    AudioPlaybackCaptureConfiguration config = null;

                    config = new AudioPlaybackCaptureConfiguration.Builder(mediaProjection)
                            .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                            .build();
                    AudioRecord record;
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        record = new AudioRecord.Builder()
                                .setAudioFormat(new AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                        .setSampleRate(8000)
                                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO).build())
                                .setAudioPlaybackCaptureConfig(config)
                                .build();
                        record.startRecording();

                        int bufferSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
                        short[] buffer = new short[bufferSize];
                        while (true){
                            record.read(buffer, 0, buffer.length);
                        }
                    }
                }
            }.start();

            //for information
            //https://blog.csdn.net/jiangliloveyou/article/details/11218555
            //https://github.com/AFinalStone/AudioRecord.git
            //http://www.audiobar.cn/  find tone
            //https://www.bilibili.com/read/cv3250059/
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void createNotificationChannel() {
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext()); //????????????Notification?????????
        Intent nfIntent = new Intent(this, MainActivity.class); //???????????????????????????????????????????????????

        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)) // ??????PendingIntent
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher)) // ??????????????????????????????(?????????)
                //.setContentTitle("SMI InstantView") // ??????????????????????????????
                .setSmallIcon(R.mipmap.ic_launcher) // ??????????????????????????????
                .setContentText("is running......") // ?????????????????????
                .setWhen(System.currentTimeMillis()); // ??????????????????????????????

        /*????????????Android 8.0?????????*/
        //??????notification??????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("notification_id");
        }
        //????????????notification??????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("notification_id", "notification_name", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = builder.build(); // ??????????????????Notification
        notification.defaults = Notification.DEFAULT_SOUND; //????????????????????????
        startForeground(110, notification);

    }
}