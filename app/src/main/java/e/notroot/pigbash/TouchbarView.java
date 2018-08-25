package e.notroot.pigbash;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

public class TouchbarView extends android.support.v7.widget.AppCompatImageView{

    private int vol;
    private int height;
    private int width;
    private Rect rectangle;
    private final Paint paint = new Paint();

    public TouchbarView(Context context) {
        super(context);
        init();
    }

    public TouchbarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TouchbarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        vol = 0; // just a placeholder so it doesn't give a fit when being drawn
        runJustBeforeBeingDrawn(this, () -> {
            width = getWidth();
            height = getHeight();
            rectangle = new Rect(0, (int)(height*(1-(double)vol/100)), width, height);
        });
    }

    private static void runJustBeforeBeingDrawn(final View view, final Runnable runnable) {
        final ViewTreeObserver.OnPreDrawListener preDrawListener = new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                runnable.run();
                return true;
            }
        };
        view.getViewTreeObserver().addOnPreDrawListener(preDrawListener);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.BLACK);
        canvas.drawRect(rectangle, paint);
    }

    @SuppressLint("ClickableViewAccessibility") // Accessibility? Considering the mechanics of the main game... don't need it
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int y = (int)event.getY();
        updateSize(y);
        return true;
    }

    private void updateSize(int y){
        vol = (int)((double)(height-y)/(double)height*100);
        if(vol<0)
            vol = 0;
        if(vol>100)
            vol = 100;
        rectangle = new Rect(0, (int)(height*(1-(double)vol/100)), width, height);
        postInvalidate();
    }

    public void setColor(String color){
        paint.setColor(Color.parseColor(color));
    }

    public void setPercent(int _vol){
        vol = _vol;
        rectangle = new Rect(0, (int)(height*(1-(double)vol/100)), width, height);
        postInvalidate();
    }

    public int getVol(){
        return vol;
    }
}
