package cz.uhk.bulicek.smartlog;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences shprefs;
    boolean isAttendanceRunning;
    TextView txtToday, txtTodayLeft, txtMonth, txtMonthLeft, txtTime;
    LogDatabaseHandler dbh = new LogDatabaseHandler(this);
    FloatingActionButton fab;
    Button btnStart, btnStop;
    CheckBox cbGPS, cbWiFi, cbMAC;
    String strToday, strTodayLeft, strMonth, strMonthLeft;
    Boolean todayLess = true, monthLess = true;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Handler handler; Runnable update;
    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        shprefs = PreferenceManager.getDefaultSharedPreferences(this);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /**
         * GUI init
         */
        txtToday = (TextView) findViewById(R.id.txt_hoursToday);
        txtTodayLeft = (TextView) findViewById(R.id.txt_leftToday);
        txtMonth = (TextView) findViewById(R.id.txt_hoursMonth);
        txtMonthLeft = (TextView) findViewById(R.id.txt_leftMonth);
        txtTime = (TextView) findViewById(R.id.txtTime);

        cbGPS = (CheckBox) findViewById(R.id.cb_GPS);
        cbGPS.setChecked(checkGPS());
        cbWiFi = (CheckBox) findViewById(R.id.cb_WiFi);
        cbWiFi.setChecked(checkWiFi());
        cbMAC = (CheckBox) findViewById(R.id.cb_MAC);
        if (cbWiFi.isChecked() && shprefs.getBoolean("mac_checking", true)) {
            cbMAC.setEnabled(true);
            cbMAC.setChecked(checkMAC());
        } else {
            cbMAC.setEnabled(false);
            cbMAC.setChecked(false);
        }

        btnStop = (Button) findViewById(R.id.btn_stop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                //STOP BUTTON: if attendance is running, stop it. Always.
                if (isAttendanceRunning) {
                    btnStart.setText("Start working");
                    btnStop.setEnabled(false);
                    btnStop.setVisibility(View.INVISIBLE);
                    btnStart.setEnabled(true);
                    isAttendanceRunning = false;
                    dbh.addLog(new Log(sdf.format(cal.getTime()), 0));
                }
            }
        });
        btnStart = (Button) findViewById(R.id.btn_start);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //START BUTTON: if attendance is not running, CHECK if conditions are met, and start it.
                if (shprefs.getBoolean("gps_checking", true) && !checkGPS()) {
                    Toast.makeText(getApplicationContext(), "GPS: Conditions not met. You can't start work right now.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (shprefs.getBoolean("wifi_checking", true) && !checkWiFi()) {
                    Toast.makeText(getApplicationContext(), "WiFi: Conditions not met. You can't start work right now.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (shprefs.getBoolean("mac_checking", true) && !checkMAC()) {
                    Toast.makeText(getApplicationContext(), "MAC: Conditions not met. You can't start work right now.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Calendar cal = Calendar.getInstance();
                if (!isAttendanceRunning) {
                    btnStart.setText("Working...");
                    btnStart.setEnabled(false);
                    btnStop.setVisibility(View.VISIBLE);
                    btnStop.setEnabled(true);
                    isAttendanceRunning = true;
                    dbh.addLog(new Log(sdf.format(cal.getTime()), 1));
                }
            }
        });

        //GUI update
        updateButtons();
        updateHours();

        //Hours update every sec
        timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Calendar c = Calendar.getInstance();
                        c.setFirstDayOfWeek(c.MONDAY);
                        SimpleDateFormat displaySDF = new SimpleDateFormat("EEEE, HH:mm:ss", Locale.US);
                        txtTime.setText(displaySDF.format(c.getTime()));
                    }
                });
            }
        }, 0, 1000);

        startService(new Intent(this, BackgroundService.class));
        updateDisplay();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivity(settingsIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /**
         * If Main Activity is killed, attendance should be stopped.
         */
        Calendar cal = Calendar.getInstance();
        if (isAttendanceRunning) {
            isAttendanceRunning = false;
            dbh.addLog(new Log(sdf.format(cal.getTime()), 0));
            Toast.makeText(getApplicationContext(), "Attendance logging stopped due to exiting the activity.", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    /**
     * Method to update worked hours text.
     */
    private void updateHours() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtToday.setText(strToday);
                txtTodayLeft.setText(strTodayLeft);
                if (todayLess) {
                    txtTodayLeft.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorFABStop)));
                } else {
                    txtTodayLeft.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorFABRun)));
                }
                txtMonth.setText(strMonth);
                txtMonthLeft.setText(strMonthLeft);
                if (monthLess) {
                    txtMonthLeft.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorFABStop)));
                } else {
                    txtMonthLeft.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorFABRun)));
                }
            }
        });
    }

    /**
     * Method to update buttons (enabled/disabled) according to running attendance.
     */
    private void updateButtons() {
        Log initLog = dbh.getLastLog();
        if (initLog == null || initLog.get_type() == 0) {
            isAttendanceRunning = false;
        } else {
            isAttendanceRunning = true;
        }

        if (isAttendanceRunning) {
            btnStart.setText("Working...");
            btnStart.setEnabled(false);
            btnStop.setVisibility(View.VISIBLE);
            btnStop.setEnabled(true);
        } else {
            btnStart.setText("Start working");
            btnStop.setEnabled(false);
            btnStop.setVisibility(View.INVISIBLE);
            btnStart.setEnabled(true);
        }
    }

    /**
     * General GUI update calling other updates, handling recounting hours.
     */
    private void updateDisplay() {
        handler = new Handler();

        //wait until the start of next minute
        Calendar c = Calendar.getInstance();
        int secs = c.get(Calendar.SECOND);

        update = new Runnable() {
            public void run() {
                Calendar cal = Calendar.getInstance();

                /*
                    TODAY
                 */

                List<Log> logs = dbh.getLogsToday();
                long count = getSecondsCount(logs);
                strToday = " " + countToString(count, false);

                //Fond pracovní doby (víkendy 0)
                int fpd = 28800;
                if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                    fpd = 0;
                }

                if (count < fpd) {
                    todayLess = true;
                } else {
                    todayLess = false;
                }

                boolean isNegative = false;
                count = count - fpd;
                if (count < 0) {
                    isNegative = true;
                    count = -count;
                }

                strTodayLeft = countToString(count, isNegative);
                if (todayLess) {
                    strTodayLeft = "-" + strTodayLeft;
                } else {
                    strTodayLeft = " " + strTodayLeft;
                }

                /*
                    MONTH
                 */

                logs = dbh.getLogsMonth();
                count = getSecondsCount(logs);
                strMonth = " " + countToString(count, false);

                //Fond pracovní doby
                fpd = 0;
                int currMonth = cal.get(Calendar.MONTH);
                int currDay = cal.get(Calendar.DAY_OF_MONTH);
                cal.set(Calendar.DAY_OF_MONTH, 1);

                while (currMonth == cal.get(Calendar.MONTH) && currDay >= cal.get(Calendar.DAY_OF_MONTH)) {
                    if (!(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)) {
                        fpd += 28800;
                    }
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }

                if (count < fpd) {
                    monthLess = true;
                } else {
                    monthLess = false;
                }

                count = count - fpd;
                isNegative = false;
                if (count < 0) {
                    isNegative = true;
                    count = -count;
                }

                strMonthLeft = countToString(count, isNegative);
                if (monthLess) {
                    strMonthLeft = "-" + strMonthLeft;
                } else {
                    strMonthLeft = " " + strMonthLeft;
                }

                updateHours();
                updateButtons();
                handler.postDelayed(update, 60000);
            }
        };

        //wait until the start of next minute
        handler.postDelayed(update, (60 - secs)*1000);
    }

    /**
     * Method to get the worked time from the database.
     */
    private long getSecondsCount(List<Log> logs) {
        boolean firstStarting = false;
        String temp = "";
        long count = 0;
        int last = 0;
        for (Log log : logs) {
            if (log.get_type() == 0 && !firstStarting) {
                continue;
            }
            if (log.get_type() == 1 && !firstStarting) {
                firstStarting = true;
            }
            if (log.get_type() == 1) {
                temp = log.get_time();
                last = 1;
            } else {
                try {
                    Date start = sdf.parse(temp);
                    Date end = sdf.parse(log.get_time());
                    long seconds = (end.getTime() - start.getTime()) / 1000;
                    count += seconds;
                    last = 0;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        if (last == 1) {
            try {
                Date start = sdf.parse(temp);
                Date now = new Date();
                long seconds = (now.getTime() - start.getTime()) / 1000;
                count += seconds;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return count;
    }

    /**
     * Method to convert seconds to hours:minutes text.
     */
    private String countToString(long secondsCount, boolean isNegative) {
        int secs = (int) (secondsCount % 60);
        int mins = (int) (secondsCount / 60);
        int hours = mins / 60;
        mins = mins % 60;
        if (secs >= 1 && !isNegative) mins++;
        return hours + ":" + String.format("%02d", mins); // + ":" + String.format("%02d", secs);
    }

    /**
     * Checking (GPS/WiFi/MAC) methods.
     */
    private boolean checkGPS() {
        Validator validator = new Validator(getApplicationContext());
        boolean valid = validator.validateGPS();
        cbGPS.setChecked(valid);
        return valid;

    }

    private boolean checkWiFi() {
        Validator validator = new Validator(getApplicationContext());
        boolean valid = validator.validateWiFi();
        cbWiFi.setChecked(valid);
        return valid;
    }

    private boolean checkMAC() {
        Validator validator = new Validator(getApplicationContext());
        boolean valid = validator.validateMAC();
        cbMAC.setChecked(valid);
        return valid;
    }
}
