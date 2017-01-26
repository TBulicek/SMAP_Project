package cz.uhk.bulicek.smartlog;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by bulicek on 26. 1. 2017.
 */

public class BackgroundService extends Service {
    public Context context = this;
    public Handler handler = null;
    public static Runnable runnable = null;
    int count;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        handler = new Handler();

        //wait until the start of next minute
        Calendar c = Calendar.getInstance();
        int secs = c.get(Calendar.SECOND);

        runnable = new Runnable() {
            public void run() {
                
                Toast.makeText(context, "Service is still running, " + count++, Toast.LENGTH_LONG).show();
                //restart runnable every minute
                handler.postDelayed(runnable, 60000);
            }
        };

        //wait until the start of next minute
        handler.postDelayed(runnable, (60 - secs)*1000);
    }

    @Override
    public void onDestroy() {
        //handler.removeCallbacks(runnable);
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
    }
}
