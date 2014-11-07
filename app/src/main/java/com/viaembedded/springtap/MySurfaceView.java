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
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.via.SmartETK;

import java.util.ArrayList;

public class MySurfaceView extends SurfaceView implements Runnable, View.OnGenericMotionListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private SurfaceHolder surfaceHolder;
    private Bitmap bmpIcon;
    // coordinates of the touched position
    private int mX;
    private int mY;

    // A thread where the painting activities are taking place
    private Thread mThread;
    // A flag which controls the start and stop of the repainting of the SurfaceView
    private boolean mFlag = false;
    // Paint
    private Paint mPaint;

    private int WorldStatus;

    private int canvasWidth;
    private int canvasHeight;
    private float sectionWidth;
    private float sectionHeight;

/*
    Seven segment to GPIO channel mapping
    A -> 4
    B -> 5
    C -> 3
    D -> 2
    E -> 1
    F -> 6
    G -> 7
    DP -> NC

    World:
    0 : out of area
    1 : upper left
    2 : upper centre
    3 : upper right
    4 : middle left
    5 : dead centre
    6 : middle right
    7 : lower left
    8 : lower centre
    9 : lower right
    10: other (eg. click)
*/

    static final int[] gpioworld = new int[]{
            0,
            48,
            40,
            12,
            16,
            128,
            4,
            80,
            66,
            6,
            254,
            };

    public MySurfaceView(Context context) {
        super(context);
        init();

    }

    public MySurfaceView(Context context,
                         AttributeSet attrs) {
        super(context, attrs);
        WorldStatus = 0;
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
        canvasWidth = getWidth();
        canvasHeight = getHeight();
        sectionHeight = canvasHeight/3;
        sectionWidth = canvasWidth/3;
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

    // Based on https://github.com/todesschaf/m-c/blob/a48cb6dad7f748a078fac0cd6f45ca1d9acf7d83/mobile/android/base/ScrollAnimator.java
    @Override
    public boolean onGenericMotion(View view, MotionEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_MOUSE) != 0) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_MOVE:
                    mX = (int) (event.getAxisValue(MotionEvent.AXIS_X));
                    mY = (int) (event.getAxisValue(MotionEvent.AXIS_Y));
//                    Log.d(TAG, String.format("Move to: %.1f/%.1f", mY, mY));
                    setWorldStatus(mX, mY);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    Log.d(TAG, "HOVER EXIT!");
                    setWorldStatus(-1, -1);
                    break;
                default:
                    Log.d(TAG, String.format("event: %d ", event.getAction()));
                    setWorldStatus(-2, -2);
                    break;
            }
        }
        return false;
    }

    private void setWorldStatus(int X, int Y) {
        int newWorldStatus;
        if (X == -1) {
            newWorldStatus = 0;
        } else if (X == -2) {
            newWorldStatus = 10;
        } else {
            newWorldStatus = (int) (Math.floor(Y / sectionHeight)*3 + Math.floor(X / sectionWidth)) + 1;
        }
        if (WorldStatus != newWorldStatus) {
            Log.d(TAG, String.format("New World: %d", newWorldStatus));
            // Deal with possible overflow
            if (newWorldStatus < 0 || newWorldStatus > 9) {
                newWorldStatus = 0;
            }
            WorldStatus = newWorldStatus;
            GPIOout(gpioworld[newWorldStatus]);
        }
    }

    private void GPIOout(int configuration) {
        for (int i = 0; i < 8; i++) {
            int value = (configuration >> i) & 1;
            Log.d(TAG, String.format("Send value: %d -> %d", i, value));
            SmartETK.Gpio_Write(i, value);
        }
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