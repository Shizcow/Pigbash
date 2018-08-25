package e.notroot.pigbash;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class OptionsActivity extends AppCompatActivity {
    private final SoundPool samples = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
    private MediaPlayer musicPlayer = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        final SpriteView pigsprite = findViewById(R.id.pigsprite);
        pigsprite.setSpriteType("pig");
        final Handler stateHandler = new Handler();
        stateHandler.postDelayed(new Runnable(){
            public void run(){
                pigsprite.advanceSprite();
                stateHandler.postDelayed(this, 100);
            }
        }, 100);

        final TouchbarView music = findViewById(R.id.music);
        final TouchbarView sfx = findViewById(R.id.sfx);
        music.setColor("#27B723");
        sfx.setColor("#DFCB00");

        SharedPreferences prefs = this.getSharedPreferences("e.notroot.pigbash", Context.MODE_PRIVATE);
        music.setPercent((int)prefs.getLong("e.notroot.pigbash.volmusic", 54));
        sfx.setPercent((int)prefs.getLong("e.notroot.pigbash.volsfx", 73));

        ImageView defaults = findViewById(R.id.defaults);
        defaults.setOnClickListener((View v) -> {
            music.setPercent(54);
            sfx.setPercent(73);
            float vol_music = 54f;
            musicPlayer.setVolume(vol_music, vol_music);
        });


        Context mContext = getApplicationContext();
        final int sfxID = samples.load(mContext, R.raw.sample_cymbol, 1);
        final int musicID = samples.load(mContext, R.raw.sample_music, 1);

        findViewById(R.id.back).setOnClickListener((View v) -> {
            prefs.edit().putLong("e.notroot.pigbash.volmusic", music.getVol()).apply();
            prefs.edit().putLong("e.notroot.pigbash.volsfx", sfx.getVol()).apply();
            finish();
        });

        findViewById(R.id.samplemusic).setOnClickListener((View v) -> {
            float vol_music = (float)(1-Math.log(100-music.getVol())/Math.log(100));
            samples.play(musicID, vol_music, vol_music, 0, 0, 1);
            musicPlayer.setVolume(vol_music, vol_music);
        });
        findViewById(R.id.samplesfx).setOnClickListener((View v) -> {
            float vol_sfx = (float)(1-Math.log(100-sfx.getVol())/Math.log(100));
            samples.play(sfxID, vol_sfx, vol_sfx, 0, 0, 1);
        });

        if(musicPlayer==null)
            musicPlayer = MediaPlayer.create(OptionsActivity.this,R.raw.options);
        musicPlayer.setLooping(true);
    }

    private int length = 0;
    @Override
    protected void onPause(){
        super.onPause();
        musicPlayer.pause();
        length = musicPlayer.getCurrentPosition();
        samples.autoPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        musicPlayer.seekTo(length);
        musicPlayer.start();
        samples.autoResume();
    }
}
