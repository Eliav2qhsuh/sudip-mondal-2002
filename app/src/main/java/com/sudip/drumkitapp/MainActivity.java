package com.sudip.drumkitapp;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    Map<Integer, Integer> map = new HashMap<>();
    SoundPool soundPool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewGroup group = ((ViewGroup) findViewById(R.id.layout));
        soundPool = new SoundPool(200,AudioManager.STREAM_MUSIC,0);
        for (int i = 0; i < group.getChildCount(); i++) {
            int resourceId = getResources().getIdentifier(group.getResources().getResourceEntryName(group.getChildAt(i).getId()),"raw","com.sudip.drumkitapp");
            map.put(resourceId,soundPool.load(this,resourceId,1));
        }



        for (int i = 0; i < group.getChildCount(); i++) {
            group.getChildAt(i).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                        int id = view.getId();
                        String ourId = view.getResources().getResourceEntryName(id);

                        int resourceId = getResources().getIdentifier(ourId,"raw","com.sudip.drumkitapp");
//                        SoundPoolPlayer mPlayer = SoundPoolPlayer.create(MainActivity.this, resourceId);
//                        mPlayer.play();
                        int soundId = getSoundPool(resourceId);
                        soundPool.play(soundId,1,1,1,0,1);
                    }
                    return true;
                }
            });
        }
    }


    public Integer getSoundPool(int id){
        return map.get(id);
    }
}