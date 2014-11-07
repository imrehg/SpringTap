package com.viaembedded.springtap;

/**
 * Created by greg on 11/5/14.
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import com.via.SmartETK;

public class MySurfaceView extends SurfaceView implements Runnable, OnTouchListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private SurfaceHolder surfaceHolder;
    private Bitmap bmpIcon;
    // coordinates of the touched position
    private float mX;
    private float mY;

    // A thread where the painting activities are taking place
    private Thread mThread;
    // A flag which controls the start and stop of the repainting of the SurfaceView
    private boolean mFlag = false;
    // Paint
    private Paint mPaint;

    public MySurfaceView(Context context) {
        super(context);
        init();

    }

    public MySurfaceView(Context context,
                         AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MySurfaceView(Context context,
                         AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init(){
        surfaceHolder = getHolder();
        bmpIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_launcher);

        // Initializing the X position
        mX = -100;

        // Initializing the Y position
        mY = -100;

        // Initializing the paint object mPaint
        mPaint = new Paint();

        // Setting the color for the paint object
        mPaint.setColor(Color.BLUE);

        surfaceHolder.addCallback(new SurfaceHolder.Callback(){

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Canvas canvas = holder.lockCanvas(null);
                drawGrid(canvas);
                holder.unlockCanvasAndPost(canvas);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder,
                                       int format, int width, int height) {
                // TODO Auto-generated method stub

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // TODO Auto-generated method stub

            }});
    }

    protected void drawGrid(Canvas canvas) {
        int canvasWidth = getWidth();
        int canvasHeight = getHeight();
        int[] colours = {Color.RED, Color.BLUE, Color.GREEN,
                         Color.YELLOW, Color.MAGENTA, Color.WHITE,
                         Color.BLACK, Color.CYAN, Color.GRAY};
        // Create colourful grid
        Paint paint = new Paint();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int n = i*3 + j;
                paint.setColor(colours[n]);
                canvas.drawRect((canvasWidth/3)*j, (canvasHeight/3)*i, (canvasWidth/3)*(j+1), (canvasHeight/3)*(i+1), paint);
            }
        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                // Getting the X-Coordinate of the touched position
                mX = event.getX();

                // Getting the Y-Coordinate of the touched position
                mY = event.getY();
                Log.d(TAG, String.format("Click at %.1f/%.1f", mY, mY));
                break;
        }
        return true;
    }

    public void resume(){
        // Instantiating the thread
        mThread = new Thread(this);

        // setting the mFlag to true for start repainting
        mFlag = true;

        // Start repaint the SurfaceView
        mThread.start();
    }

    public void pause(){
        mFlag = false;
    }

    @Override
    public void run() {
        while(mFlag){
            // Check whether the object holds a valid surface
            if(!surfaceHolder.getSurface().isValid())
                continue;
            // Start editing the surface
//            Canvas canvas = surfaceHolder.lockCanvas();
            // Draw a background color
//            canvas.drawARGB(55, 155, 255, 255);
            // Draw a circle at (mX,mY) with radius 5
//            canvas.drawCircle(mX, mY, 5, mPaint);
            // Finish editing the canvas and show to the user
//            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

}