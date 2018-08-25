package e.notroot.pigbash;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Arrays;

public class MenuActivity extends AppCompatActivity {
    private DisplayMetrics displayMetrics;
    private SharedPreferences pref;
    private static final int[] pig_animate_array = {R.drawable.menu_animation_pig_1, R.drawable.menu_animation_pig_2, R.drawable.menu_animation_pig_3, R.drawable.menu_animation_pig_4, R.drawable.menu_animation_pig_5, R.drawable.menu_animation_pig_6};
    private static final int[] topshooter_animate_array = {R.drawable.menu_animation_topshooter_1,R.drawable.menu_animation_topshooter_2,R.drawable.menu_animation_topshooter_3,R.drawable.menu_animation_topshooter_4,R.drawable.menu_animation_topshooter_5};
    private MediaPlayer music = null;
    private boolean isPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        //Splash time
        //Step 0, init some stuff

        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        findViewById(R.id.et).animate().x(0).y(-(float)getResources().getDrawable(R.drawable.menu_et).getIntrinsicHeight()*1.5f).setDuration(0).start(); // mve stuff offscreen
        findViewById(R.id.pigbash).animate().x(-(float)getResources().getDrawable(R.drawable.menu_pigbash).getIntrinsicWidth()).y(0).setDuration(0).start();

        //Step 1, start laughing track and setup music for future
        pref = getSharedPreferences("e.notroot.pigbash", MODE_PRIVATE);
        music = MediaPlayer.create(this,R.raw.menu);
        music.start();

        Handler musicHandler = new Handler();
        musicHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!music.isPlaying()&&!isPaused) {
                    music.seekTo(5750);
                    music.start();
                }
                musicHandler.postDelayed(this, 100);
            }
        }, 100);

        (new Handler()).postDelayed(this::secondSetup, 50);
    }

    private void secondSetup() {
        //Step 2, scroll in TURBO, frozen sprites, and topshooter
        findViewById(R.id.turbo).setVisibility(View.VISIBLE);
        int[] towrite = new int[2];
        final ImageView[] ref = {findViewById(R.id.menu_pig_ref_0), findViewById(R.id.menu_pig_ref_1)};

        RelativeLayout mainAppendor = findViewById(R.id.mainAppendor);
        ImageView[] toAppend = {new ImageView(this), new ImageView(this)};
        for(int i=0; i<2; ++i) {
            toAppend[i].setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mainAppendor.addView(toAppend[i]);

            ref[i].getLocationOnScreen(towrite);
            toAppend[i].animate().x(towrite[0]).y(towrite[1]).setDuration(0).start();
        }
        Handler setupHandler = new Handler();
        setupHandler.postDelayed(new Runnable() {
            int animationState = -1;
            @Override
            public void run() {
                if(++animationState!=6) {
                    for(int i=0; i<2; ++i)
                        toAppend[i].setImageResource(pig_animate_array[animationState]);
                    setupHandler.postDelayed(this, 100);
                } else {
                    for(int i=0; i<2; ++i) {
                        toAppend[i].animate().x(displayMetrics.widthPixels).y(displayMetrics.heightPixels).setDuration(0).start();
                        toAppend[i].setVisibility(View.GONE);
                    }
                    thirdSetup();
                }
            }
        }, 0);
    }

    private void thirdSetup(){
        RelativeLayout mainAppendor = findViewById(R.id.mainAppendor);
        ImageView toAppend = new ImageView(this);
        toAppend.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mainAppendor.addView(toAppend);

        final ImageView ref = findViewById(R.id.topshooter);
        int[] towrite = new int[2];
        ref.getLocationOnScreen(towrite);
        toAppend.animate().x(towrite[0]).y(towrite[1]).setDuration(0).start();
        Handler setupHandler = new Handler();
        setupHandler.postDelayed(new Runnable() {
            int animationState = -1;
            @Override
            public void run() {
                if(++animationState!=5) {
                    toAppend.setImageResource(topshooter_animate_array[animationState]);
                    setupHandler.postDelayed(this, 100);
                } else {
                    fourthSetup();
                }
            }
        }, 200);
    }

    private static final int[] rotate_pig_array = {R.drawable.rotate_pig_1, R.drawable.rotate_pig_2, R.drawable.rotate_pig_3, R.drawable.rotate_pig_4, R.drawable.rotate_pig_5, R.drawable.rotate_pig_6, R.drawable.rotate_pig_7, R.drawable.rotate_pig_8, R.drawable.rotate_pig_9, R.drawable.rotate_pig_10};
    private void fourthSetup(){
        //Step 3, Set visibility for buttons and highscore
        findViewById(R.id.start).setVisibility(View.VISIBLE);
        findViewById(R.id.options).setVisibility(View.VISIBLE);
        findViewById(R.id.scores).setVisibility(View.VISIBLE);
        findViewById(R.id.exit).setVisibility(View.VISIBLE);
        findViewById(R.id.scorewrapper).setVisibility(View.VISIBLE);

        View view_instance = findViewById(R.id.scorewrapper);
        ViewGroup.LayoutParams params = view_instance.getLayoutParams();
        params.width = getResources().getDrawable(R.drawable.menu_turbo).getIntrinsicWidth(); // probably faster than a dp to px conversion
        view_instance.setLayoutParams(params);
        UpdateScore();


        //Step 4, set up sprites
        //get ready for bullshit
        final ImageView[] toStart = {findViewById(R.id.menu_pig_ref_0), findViewById(R.id.menu_pig_ref_1), findViewById(R.id.menu_pig_ref_2), findViewById(R.id.menu_pig_ref_3), findViewById(R.id.menu_pig_ref_4), findViewById(R.id.menu_pig_ref_5)}; // this is leagues less bullshit than it used to be
        final Handler stateHandler = new Handler();
        stateHandler.postDelayed(new Runnable() {
            int mSpriteState = 2; // needed to align with files
            @Override
            public void run() {
                if (++mSpriteState > 9) //yeah I had to rewrite the whole class
                    mSpriteState = 0;   // it's necessary so the sprites stay in sync

                for(int i=0; i<6; ++i)
                    toStart[i].setImageResource(rotate_pig_array[mSpriteState]); // idk where but postdelayed DOES get called
                                                //accessing gets optimized out
                stateHandler.postDelayed(this, 105);
            }
        }, 0);

        //Step 5, functionality
        findViewById(R.id.start).setOnClickListener((View v) -> {
            isPaused=true;
            startActivity(new Intent(this, MainActivity.class));
        });

        findViewById(R.id.options).setOnClickListener((View v) -> {
            isPaused=true;
            startActivity(new Intent(this, OptionsActivity.class));
        });

        findViewById(R.id.scores).setOnClickListener((View v) -> {
            isPaused=true;
            startActivity(new Intent(this, HighscoreActivity.class));
        });

        findViewById(R.id.exit).setOnClickListener((View v) -> {
            finish();
            System.exit(0);
        });

        //Step 6, animate in the rest
        findViewById(R.id.et).animate().x(0).y(0).setDuration(2000).start();
        (new Handler()).postDelayed(this::fifthSetup, 2000);
    }

    private void fifthSetup(){
        //We need to wait until the animations are done and this seems the easiest/fastest way
        final ImageView animator = findViewById(R.id.pigbash);
        animator.animate().x(displayMetrics.widthPixels/2-(float)(getResources().getDrawable(R.drawable.menu_pigbash).getIntrinsicWidth())/2).y(0).setDuration(850).start();
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
        UpdateScore();
        isPaused=false;
        float vol_music = (float) (1 - Math.log(100 - (int) pref.getLong("e.notroot.pigbash.volmusic", 54)) / Math.log(100));
        music.setVolume(vol_music, vol_music);
    }

    private void UpdateScore(){
        ArrayList<String> arr = new ArrayList<>(Arrays.asList(this.getSharedPreferences("e.notroot.pigbash", Context.MODE_PRIVATE).getString("e.notroot.pigbash.scores_0", "0|").split("\\|")));
        String right = arr.get(0);
        if(Integer.parseInt(right)>0) {
            arr.remove(0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ((TextView)findViewById(R.id.name)).setText(String.join("|", arr.toArray(new String[0])));
            else
                ((TextView) findViewById(R.id.name)).setText(TextUtils.join("|", arr.toArray(new String[0])));
            ((TextView)findViewById(R.id.score)).setText(right);
        } else {
            ((TextView)findViewById(R.id.name)).setText("");
            ((TextView)findViewById(R.id.score)).setText("");
        }
    }
}