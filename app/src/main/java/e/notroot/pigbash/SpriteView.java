package e.notroot.pigbash;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import java.util.Random;

public class SpriteView extends android.support.v7.widget.AppCompatImageView {

    private String mSpriteType;
    private float mPredrawnWidth;
    private float mPredrawnHeight;
    private int mSpriteState;

    public SpriteView(Context context) {
        super(context);
        init(null);
    }

    public SpriteView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SpriteView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public void setSpriteType(String str){
        mSpriteType = str;
        //updateImage(); // Apparently this isn't needed but if we get bugs this might fix it
    }

    private void init(@Nullable AttributeSet set){
        Random r = new Random();
        mSpriteState = r.nextInt(10)+1;
        Drawable d = getResources().getDrawable(R.drawable.rotate_pig_1);
        mPredrawnWidth = d.getIntrinsicWidth();
        mPredrawnHeight = d.getIntrinsicHeight();

        if(set==null)
            return;

        TypedArray ta = getContext().obtainStyledAttributes(set, R.styleable.SpriteView);
        mSpriteType = ta.getString(R.styleable.SpriteView_spriteType);
        ta.recycle();
    }

    public void advanceSprite(){
        if(mSpriteType.equals("pig")) {
            if (++mSpriteState > 9)
                mSpriteState = 0;
        } else {
            if (++mSpriteState > 8)
                mSpriteState = 0;
        }
        updateImage();
    }

    private static final int[] rotate_pig_array = {R.drawable.rotate_pig_1, R.drawable.rotate_pig_2, R.drawable.rotate_pig_3, R.drawable.rotate_pig_4, R.drawable.rotate_pig_5, R.drawable.rotate_pig_6, R.drawable.rotate_pig_7, R.drawable.rotate_pig_8, R.drawable.rotate_pig_9, R.drawable.rotate_pig_10};
    private static final int[] rotate_star_array = {R.drawable.rotate_star_1, R.drawable.rotate_star_2, R.drawable.rotate_star_3, R.drawable.rotate_star_4, R.drawable.rotate_star_5, R.drawable.rotate_star_6, R.drawable.rotate_star_7, R.drawable.rotate_star_8, R.drawable.rotate_star_9};
    private void updateImage(){
        setImageResource(mSpriteType.equals("pig")?rotate_pig_array[mSpriteState]:rotate_star_array[mSpriteState]);
        postInvalidate();
    }

    public float getPredrawnWidth(){
        return mPredrawnWidth;
    }

    public float getPredrawnHeight(){
        return mPredrawnHeight;
    }
}
