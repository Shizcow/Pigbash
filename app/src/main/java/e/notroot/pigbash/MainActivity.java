package e.notroot.pigbash;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import static android.os.SystemClock.sleep;

public class MainActivity extends AppCompatActivity {
    private final Random r = new Random();
    private int score = 0, timer = 60;
    private float vol_sfx;
    private String name = "";

    private final int SpawnCheckDelay_ms = 100;
    private final int SpawnCheckProb = 15;
    private final int DespawnTimer_ms = 2500;
    private int StarCount = 0;

    private MediaPlayer music = null;

    private final SoundPool samples = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
    private int beepID, explosionStarID, explosionPigID, missedShotID, snortID, goID;

    private volatile boolean isPaused = false, isFinished = false;
    private int delayed = 0;
    private final Thread timerThread = new Thread(() -> {
        while(!isFinished) {
            if(!isPaused) {
                for (; delayed<100&&!isPaused&&!isFinished; ++delayed)
                    sleep(10L);
                if(!isPaused&&!isFinished) {
                    delayed = 0;
                    runOnUiThread(this::decrementTimer);
                }
            } else {
                sleep(10L);
            }
        }
    });

    private final Handler pigHandler = new Handler();
    private final ArrayList<String> scores = new ArrayList<>();
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = this.getSharedPreferences("e.notroot.pigbash", Context.MODE_PRIVATE);
        float vol_music = (float)(1-Math.log(100-(int)pref.getLong("e.notroot.pigbash.volmusic", 54))/Math.log(100));
        vol_sfx = (float)(1-Math.log(100-(int)pref.getLong("e.notroot.pigbash.volsfx", 73))/Math.log(100));

        goID = samples.load(this, R.raw.go, 1); // must be first
        beepID = samples.load(this, R.raw.timer_beep, 1);
        explosionPigID = samples.load(this, R.raw.shot_pig, 1);
        missedShotID = samples.load(this, R.raw.shot_missed, 1);
        explosionStarID = samples.load(this, R.raw.shot_star, 1);
        snortID = samples.load(this, R.raw.snort, 1);

        samples.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            boolean needtoPlay = true;
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if(needtoPlay) {
                    samples.play(goID, vol_sfx, vol_sfx, 1, 0, 1); // so this gets celled every time one of the samples loads, and we need to make sure the samples are loaded first before they can be ran, else theyre just ignored
                    needtoPlay = false; // solution is to wait for the cue to shout "GO!", and kind of just wing it for the rest
                }
            }
        });

        updateScore();
        updateTimer();

        if(music==null)
            music = MediaPlayer.create(this,R.raw.main);
        music.setLooping(true);
        music.setVolume(vol_music, vol_music);
    }


    public void missedShot(View v){
        samples.play(missedShotID, vol_sfx, vol_sfx, 0, 0, 1);
    }

    private void spawnSprite(final String toSpawn){
        final RelativeLayout spawnbox = findViewById(R.id.spawnbox);
        final SpriteView toAppend = new SpriteView(this);

        final float dx = r.nextFloat() * (spawnbox.getMeasuredWidth()-toAppend.getPredrawnWidth());
        final float dy = r.nextFloat() * (spawnbox.getMeasuredHeight()-toAppend.getPredrawnHeight());

        final Handler stateHandler = new Handler();

        final Thread despawnThread = new Thread(new Runnable() {
            int progress;
            @Override
            public void run() {
                while(!isFinished) {
                    if (!isPaused) {
                        for (progress = 0; (progress<DespawnTimer_ms/10)&&!isPaused&&!isFinished; ++progress)
                            sleep(10L);
                        if(!isPaused&&!isFinished) {
                            stateHandler.removeCallbacksAndMessages(null);
                            runOnUiThread(() -> {
                                if (toAppend.getParent() != null)
                                    ((RelativeLayout) toAppend.getParent()).removeView(toAppend);
                            });
                            return;
                        }
                    } else {
                        sleep(10L);
                    }
                }
            }
        });
        despawnThread.start();

        toAppend.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        toAppend.setSpriteType(toSpawn);

        toAppend.setClickable(true);
        toAppend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V){
                if(!isFinished) {
                    stateHandler.removeCallbacksAndMessages(null); // try to stop stateChange
                    samples.play(toSpawn.equals("pig") ? explosionPigID : explosionStarID, vol_sfx, vol_sfx, 0, 0, 1);
                    if (toSpawn.equals("pig"))
                        MainActivity.this.incrementScore();
                    else
                        MainActivity.this.incrimentTimer();
                    if (toAppend.getParent()!=null) // check if despawned
                        ((RelativeLayout) toAppend.getParent()).removeView(toAppend);

                    final ExplosionView explosionGraphic = new ExplosionView(MainActivity.this);
                    explosionGraphic.setSpriteType(toSpawn).setCenterX(dx + toAppend.getPredrawnWidth() / 2).setCenterY(dy + toAppend.getPredrawnHeight() / 2).setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)); // lol spaghetti
                    spawnbox.addView(explosionGraphic);
                    explosionGraphic.animate()
                            .x(explosionGraphic.getAnimateX())
                            .y(explosionGraphic.getAnimateY())
                            .setDuration(0).start();

                    final Handler explosionHandler = new Handler();
                    explosionHandler.postDelayed(new Runnable() {
                        public void run() {
                            if (explosionGraphic.shouldRemove())
                                ((RelativeLayout) explosionGraphic.getParent()).removeView(explosionGraphic);
                            else {
                                explosionGraphic.advanceSprite();
                                explosionGraphic.animate().x(explosionGraphic.getAnimateX()).y(explosionGraphic.getAnimateY()).setDuration(0).start();
                                if (!isFinished)
                                    explosionHandler.postDelayed(this, 35);
                            }
                        }
                    }, 35);
                }
            }
        });
        spawnbox.addView(toAppend);

        toAppend.animate().x(dx).y(dy).setDuration(0).start();

        stateHandler.postDelayed(new Runnable(){
            public void run(){
                if(toAppend.getParent()!=null) // check if despawned
                    toAppend.advanceSprite();
                if(!isFinished)
                    stateHandler.postDelayed(this, 45); // 10 images, 2 rotations per second
            }                                                   // and a little less because slow Java
        }, 45);
    }

    private void incrimentTimer() {
        timer+=5;
        updateTimer();
    }

    private int secToSnort = r.nextInt(21)+20;
    private void decrementTimer() {
        --secToSnort;
        if(secToSnort==0)
            samples.play(snortID, vol_sfx, vol_sfx, 0, 0, 1);
        if(secToSnort==-3)
            secToSnort = r.nextInt(21)+20;
        if(secToSnort>=0)
            samples.play(beepID, vol_sfx, vol_sfx, 0, 0, 1);
        --timer;
        ++StarCount;
        if(StarCount==10&&timer!=0) {
            StarCount=0;
            spawnSprite("star");
        }
        updateTimer();
    }

    private final static int[] timer_id_array = {R.drawable.timer_0, R.drawable.timer_1, R.drawable.timer_2, R.drawable.timer_3, R.drawable.timer_4, R.drawable.timer_5, R.drawable.timer_6, R.drawable.timer_7, R.drawable.timer_8, R.drawable.timer_9};
    private void updateTimer(){
        LinearLayout timerbox = findViewById(R.id.timerbox);

        if(timer<10)
            findViewById(R.id.leftDigit).setVisibility(View.GONE);
        else
            if(findViewById(R.id.leftDigit).getVisibility()==View.GONE)
                findViewById(R.id.leftDigit).setVisibility(View.VISIBLE);

        for(int index=0; index<2; ++index) {
            ImageView nextChild = (ImageView) timerbox.getChildAt(index);

            int digit=(int) ((timer / Math.pow(10, 1-index)) % 10);

            nextChild.setImageResource(timer_id_array[digit]);
        }

        if(timer==0)
            cleanup();
    }

    private void incrementScore(){
        ++score;
        updateScore();
    }

    private final static int[] custom_id_array = {R.drawable.custom_0, R.drawable.custom_1, R.drawable.custom_2, R.drawable.custom_3, R.drawable.custom_4, R.drawable.custom_5, R.drawable.custom_6, R.drawable.custom_7, R.drawable.custom_8, R.drawable.custom_9};
    private void updateScore(){
        LinearLayout scorebox = findViewById(R.id.scorebox);
        while(scorebox.getChildCount()<(int)(Math.log10(score)+1)) {
            ImageView toAppend = new ImageView(this);
            toAppend.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            toAppend.setImageResource(R.drawable.custom_0);
            scorebox.addView(toAppend);
        }

        for(int index=0; index<scorebox.getChildCount(); ++index) {
            ImageView nextChild = (ImageView) scorebox.getChildAt(index);

            int digit=(int) ((score / Math.pow(10, scorebox.getChildCount()-index-1)) % 10);

            nextChild.setImageResource(custom_id_array[digit]);
        }
    }


    private int length = 0;
    @Override
    protected void onPause(){
        super.onPause();
        music.pause();
        length = music.getCurrentPosition();
        samples.autoPause();
        isPaused = true;
        pigHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onResume(){
        super.onResume();
        music.seekTo(length);
        music.start();
        samples.autoResume();
        isPaused = false;
        if(!timerThread.isAlive())
            timerThread.start();
        pigHandler.post(new Runnable(){
            public void run(){
                if(r.nextInt(100)<=SpawnCheckProb) // change prob for higher levels?
                    MainActivity.this.spawnSprite("pig");
                pigHandler.postDelayed(this, SpawnCheckDelay_ms);
            }
        });
    }

    private void cleanup(){
        music.stop();

        for(int i=0; i<10; ++i)
            scores.add(pref.getString("e.notroot.pigbash.scores_"+Integer.toString(i), "0|"));

        if(Integer.parseInt((scores.get(9)).split("\\|")[0])<score) {
            music.pause();
            samples.autoPause();
            pigHandler.removeCallbacksAndMessages(null);
            isFinished = true;
            doLastDialogue();
        } else {
            finish();
        }
    }

    private void doLastDialogue(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Highscore: " + Integer.toString(score));
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (DialogInterface dialog, int which) -> {
            name = input.getText().toString();
            scores.add(Integer.toString(score)+"|"+name);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) // trying out multi-api support
                scores.sort((p1, p2) -> Integer.parseInt(p2.split("\\|")[0])-Integer.parseInt(p1.split("\\|")[0]));
            else
                Collections.sort(scores, (o1, o2) -> Integer.parseInt(o2.split("\\|")[0])-Integer.parseInt(o1.split("\\|")[0]));
            int k = scores.size();
            if(k>10)
                scores.subList(10, k).clear();
            for(int i=0; i<10; ++i)
                pref.edit().putString("e.notroot.pigbash.scores_"+Integer.toString(i), scores.get(i)).apply();
            finish();
        });
        builder.setOnCancelListener((DialogInterface g)->doLastDialogue());

        builder.show();
    }
}
