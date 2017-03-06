package cz.uhk.bulicek.smartlog;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by bulicek on 26. 1. 2017.
 */

public class BackgroundService extends Service {
    public SharedPreferences shprefs;
    public Context context = this;
    public Handler handler = null;
    public Validator validator;
    public static Runnable runnable = null;
    LogDatabaseHandler dbh = new LogDatabaseHandler(this);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        shprefs = PreferenceManager.getDefaultSharedPreferences(context);
        handler = new Handler();
        validator = new Validator(context);

        //wait until the end of the minute, then update
        Calendar c = Calendar.getInstance();
        //SimpleDateFormat justSeconds = new SimpleDateFormat("ss");
        //int secs = Integer.parseInt(justSeconds.format(c.getTime()));
        int secs = c.get(Calendar.SECOND);

        runnable = new Runnable() {
            public void run() {
                Calendar loopCalendar = Calendar.getInstance();
                Boolean isAttendanceRunning;
                Log initLog = dbh.getLastLog();
                if (initLog == null || initLog.get_type() == 0) {
                    isAttendanceRunning = false;
                } else {
                    isAttendanceRunning = true;
                }
                if (shprefs.getBoolean("gps_checking", true) && !validator.validateGPS()) {
                    Toast.makeText(getApplicationContext(), "GPS: Conditions not met. Creating end log.", Toast.LENGTH_SHORT).show();
                    if (isAttendanceRunning) dbh.addLog(new Log(sdf.format(loopCalendar.getTime()), 0));
                    else Toast.makeText(getApplicationContext(), "But nothing is running.", Toast.LENGTH_SHORT).show();
                    handler.postDelayed(runnable, 60000);
                    return;
                }

                if (shprefs.getBoolean("wifi_checking", true) && !validator.validateWiFi()) {
                    Toast.makeText(getApplicationContext(), "WiFi: Conditions not met. Creating end log.", Toast.LENGTH_SHORT).show();
                    if (isAttendanceRunning) dbh.addLog(new Log(sdf.format(loopCalendar.getTime()), 0));
                    else Toast.makeText(getApplicationContext(), "But nothing is running.", Toast.LENGTH_SHORT).show();
                    handler.postDelayed(runnable, 60000);
                    return;
                }

                if (shprefs.getBoolean("mac_checking", true) && !validator.validateMAC()) {
                    Toast.makeText(getApplicationContext(), "MAC: Conditions not met. Creating end log.", Toast.LENGTH_SHORT).show();
                    if (isAttendanceRunning) dbh.addLog(new Log(sdf.format(loopCalendar.getTime()), 0));
                    else Toast.makeText(getApplicationContext(), "But nothing is running.", Toast.LENGTH_SHORT).show();
                    handler.postDelayed(runnable, 60000);
                    return;
                }
                Toast.makeText(context, "Everything OK. Creating start log." , Toast.LENGTH_LONG).show();
                if (!isAttendanceRunning) dbh.addLog(new Log(sdf.format(loopCalendar.getTime()), 1));
                else Toast.makeText(getApplicationContext(), "But it is already there.", Toast.LENGTH_SHORT).show();
                // TODO: shprefs some value to tell if manually ended, then dont force start

                //restart runnable every minute
                handler.postDelayed(runnable, 60000);
            }
        };

        //wait until the end of the minute, then update
        long s = (60 - secs - 30);
        if (secs > 30) s += 60;
        handler.postDelayed(runnable, s*1000);
    }

    @Override
    public void onDestroy() {
        //handler.removeCallbacks(runnable);
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
    }
}
