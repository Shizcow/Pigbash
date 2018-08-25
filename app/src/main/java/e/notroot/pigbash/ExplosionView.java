package e.notroot.pigbash;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

public class ExplosionView extends android.support.v7.widget.AppCompatImageView {

    private float mCenterX;
    private float mCenterY;
    private int mSpriteState;
    private String mSpriteType;

    public ExplosionView(Context context) {
        super(context);
        init(null);
    }

    public ExplosionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ExplosionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public ExplosionView setSpriteType(String str){
        mSpriteType = str;
        updateImage();
        return this;
    }

    private void init(@Nullable AttributeSet set){
        mSpriteState = 1;
        if(set==null)
            return;

        TypedArray ta = getContext().obtainStyledAttributes(set, R.styleable.ExplosionView);
        mCenterX = ta.getInteger(R.styleable.ExplosionView_CenterX, 0);
        mCenterY = ta.getInteger(R.styleable.ExplosionView_CenterY, 0);
        mSpriteType = ta.getString(R.styleable.ExplosionView_explosionType);
        ta.recycle();

        updateImage();
    }

    private static final int[] explosion_pig_array = {R.drawable.explosion_pig_1, R.drawable.explosion_pig_2, R.drawable.explosion_pig_3, R.drawable.explosion_pig_4, R.drawable.explosion_pig_5, R.drawable.explosion_pig_6, R.drawable.explosion_pig_7};
    private static final int[] explosion_star_array = {R.drawable.explosion_star_1, R.drawable.explosion_star_2, R.drawable.explosion_star_3, R.drawable.explosion_star_4, R.drawable.explosion_star_5, R.drawable.explosion_star_6, R.drawable.explosion_star_7, R.drawable.explosion_star_8, R.drawable.explosion_star_9, R.drawable.explosion_star_10, R.drawable.explosion_star_11, R.drawable.explosion_star_12, R.drawable.explosion_star_13, R.drawable.explosion_star_14, R.drawable.explosion_star_15};
    private int currentID(){
        return mSpriteType.equals("pig")?explosion_pig_array[mSpriteState]:explosion_star_array[mSpriteState];
    }

    public void advanceSprite(){
        ++mSpriteState;
        updateImage();
    }

    private void updateImage(){
        setImageResource(currentID());
        postInvalidate();
    }

    public float getAnimateX(){
        Drawable d = getResources().getDrawable(currentID());
        return mCenterX-d.getIntrinsicWidth()/2;
    }

    public float getAnimateY(){
        Drawable d = getResources().getDrawable(currentID());
        return mCenterY-d.getIntrinsicHeight()/2;
    }

    public ExplosionView setCenterX(float CenterX) {
        mCenterX = CenterX;
        return this;
    }

    public ExplosionView setCenterY(float CenterY){
        mCenterY = CenterY;
        return this;
    }

    public boolean shouldRemove(){
        return mSpriteState==(mSpriteType.equals("pig")?6:14);
    }
}
