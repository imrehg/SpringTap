package com.viaembedded.springtap;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import com.via.SmartETK;

public class MainActivity extends Activity {

    MySurfaceView mySurfaceView;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mySurfaceView = (MySurfaceView)findViewById(R.id.bigField);
        mySurfaceView.setOnTouchListener(mySurfaceView);

        if (savedInstanceState != null) {
            Log.d(TAG, "onCreate() Restoring previous state");
            /* restore state */
        } else {
            Log.d(TAG, "onCreate() No saved state available");
        }
        SmartETK.Init();
        // Initialize Springboard's 8xGPIO
        for (int i = 0; i < 8; i++) {
            SmartETK.Gpio_Enable(i, true);
            SmartETK.Gpio_Set(i, SmartETK.GM_GPO, SmartETK.GM_NO_PULL);
            SmartETK.Gpio_Write(i, 1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mySurfaceView   .resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mySurfaceView.pause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
