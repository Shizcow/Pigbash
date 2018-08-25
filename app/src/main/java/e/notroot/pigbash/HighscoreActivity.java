package e.notroot.pigbash;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class HighscoreActivity extends AppCompatActivity {
    private SharedPreferences pref;
    private MediaPlayer music = null;
    private boolean isPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscore);

        pref = getSharedPreferences("e.notroot.pigbash", Context.MODE_PRIVATE);
        music = MediaPlayer.create(this,R.raw.highscores);
        music.start();

        Handler musicHandler = new Handler();
        musicHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!music.isPlaying()&&!isPaused) {
                    music.seekTo(8750);
                    music.start();
                }
                musicHandler.postDelayed(this, 100);
            }
        }, 100);

        findViewById(R.id.goback).setOnClickListener((View v) -> finish());

        findViewById(R.id.reset).setOnClickListener((View v) -> {
            for(int i=0; i<10; ++i)
                pref.edit().putString("e.notroot.pigbash.scores_"+Integer.toString(i), "0|").apply();
            drawScores();
        });

        findViewById(R.id.more).setOnClickListener((View v) -> startActivity(new Intent(this, MainActivity.class)));

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        float SPperRow = ((displayMetrics.heightPixels - ((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 58+54+32+3, getResources().getDisplayMetrics())))/14) / getResources().getDisplayMetrics().scaledDensity;

        LinearLayout scorebox = findViewById(R.id.allhighscores);
        for(int i=0; i<10; ++i) {
            LinearLayout nextChild = (LinearLayout) scorebox.getChildAt(i);
            ((TextView)nextChild.getChildAt(0)).setTextSize(TypedValue.COMPLEX_UNIT_SP, SPperRow);
            ((TextView)nextChild.getChildAt(2)).setTextSize(TypedValue.COMPLEX_UNIT_SP, SPperRow);
        }
    }

    private int length = 0;
    @Override
    protected void onPause(){
        super.onPause();
        isPaused=true;
        music.pause();
        length = music.getCurrentPosition();
    }

    @Override
    protected void onResume(){
        super.onResume();
        music.seekTo(length);
        music.start();
        drawScores();
        isPaused=false;
        float vol_music = (float) (1 - Math.log(100 - (int) pref.getLong("e.notroot.pigbash.volmusic", 54)) / Math.log(100));
        music.setVolume(vol_music, vol_music);
    }

    private final static int[] custom_array = {R.drawable.custom_0, R.drawable.custom_1, R.drawable.custom_2, R.drawable.custom_3, R.drawable.custom_4, R.drawable.custom_5, R.drawable.custom_6, R.drawable.custom_7, R.drawable.custom_8, R.drawable.custom_9};
    private void drawScores(){
        LinearLayout scorebox = findViewById(R.id.highscore_wrapper);
        int topscore = Integer.parseInt(pref.getString("e.notroot.pigbash.scores_0", "0|").split("\\|")[0]);

        for(int index=0; index<scorebox.getChildCount(); ++index)
            ((ImageView)scorebox.getChildAt(index)).setImageResource(custom_array[(int)((topscore / Math.pow(10, scorebox.getChildCount()-index-1)) % 10)]);

        if(topscore>=10)
            scorebox.getChildAt(1).setVisibility(View.VISIBLE);
        if(topscore>=100)
            scorebox.getChildAt(0).setVisibility(View.VISIBLE);

        scorebox = findViewById(R.id.allhighscores);
        for(int i=0; i<10; ++i) {
            LinearLayout nextChild = (LinearLayout) scorebox.getChildAt(i);

            ArrayList<String> arr = new ArrayList<>(Arrays.asList(pref.getString("e.notroot.pigbash.scores_"+Integer.toString(i), "0|").split("\\|")));
            String right = arr.get(0);
            if(Integer.parseInt(right)>0) {
                arr.remove(0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    ((TextView)nextChild.getChildAt(0)).setText(String.join("|", arr.toArray(new String[0])));
                 else
                    ((TextView)nextChild.getChildAt(0)).setText(TextUtils.join("|", arr.toArray(new String[0])));
                ((TextView)nextChild.getChildAt(2)).setText(right);
            } else {
                ((TextView)nextChild.getChildAt(0)).setText("");
                ((TextView)nextChild.getChildAt(2)).setText("");
            }
        }
    }

}
