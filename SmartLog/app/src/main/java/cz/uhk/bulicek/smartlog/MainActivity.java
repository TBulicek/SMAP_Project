package cz.uhk.bulicek.smartlog;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    boolean isAttendanceRunning;
    TextView txtToday, txtTodayLeft, txtMonth, txtMonthLeft;
    LogDatabaseHandler dbh = new LogDatabaseHandler(this);
    FloatingActionButton fab;
    Button btnStart, btnStop;
    String strToday, strTodayLeft, strMonth, strMonthLeft;
    Boolean todayLess = true, monthLess = true;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log initLog = dbh.getLastLog();
        if (initLog == null || initLog.get_type() == 0) {
            isAttendanceRunning = false;
        } else {
            isAttendanceRunning = true;
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        txtToday = (TextView) findViewById(R.id.txt_hoursToday) ;
        txtTodayLeft = (TextView) findViewById(R.id.txt_leftToday) ;
        txtMonth = (TextView) findViewById(R.id.txt_hoursMonth) ;
        txtMonthLeft = (TextView) findViewById(R.id.txt_leftMonth) ;
        updateDisplay();


        btnStop = (Button) findViewById(R.id.btn_stop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
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

        /*fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                if (!isAttendanceRunning) {
                    isAttendanceRunning = true;
                    fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorFABStop)));
                    dbh.addLog(new Log(sdf.format(cal.getTime()), 1));
                    Toast.makeText(getApplicationContext(), "Attendance logging started.", Toast.LENGTH_SHORT).show();
                } else {
                    isAttendanceRunning = false;
                    fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorFABRun)));
                    dbh.addLog(new Log(sdf.format(cal.getTime()), 0));
                    Toast.makeText(getApplicationContext(), "Attendance logging stopped.", Toast.LENGTH_SHORT).show();
                }
                //Snackbar.make(view, "Attendance logging stopped.", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });*/
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
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            //myIntent.putExtra("key", value); //Optional parameters
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
        Calendar cal = Calendar.getInstance();
        if (isAttendanceRunning) {
            isAttendanceRunning = false;
            fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorFABRun)));
            dbh.addLog(new Log(sdf.format(cal.getTime()), 0));
            Toast.makeText(getApplicationContext(), "Attendance logging stopped due to exiting the activity.", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

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

    private void updateDisplay() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                Calendar cal = Calendar.getInstance();

                /*
                    TODAY
                 */

                List<Log> logs = dbh.getLogsToday();
                long count = getSecondsCount(logs);
                strToday = " " + countToString(count);

                //Fond pracovní doby (víkendy 0)
                int fpd = 28800;
                if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {fpd = 0;}

                if (count < fpd) {
                    todayLess = true;
                } else {
                    todayLess = false;
                }

                count = count - fpd;
                if (count < 0) {
                    count = -count;
                }

                strTodayLeft = countToString(count);
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
                strMonth = " " + countToString(count);

                //Fond pracovní doby
                fpd = 0;
                int currMonth=cal.get(Calendar.MONTH);
                int currDay=cal.get(Calendar.DAY_OF_MONTH);
                cal.set(Calendar.DAY_OF_MONTH, 1);

                while (currMonth==cal.get(Calendar.MONTH) && currDay >= cal.get(Calendar.DAY_OF_MONTH)) {
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
                if (count < 0) {
                    count = -count;
                }

                strMonthLeft = countToString(count);
                if (monthLess) {
                    strMonthLeft = "-" + strMonthLeft;
                } else {
                    strMonthLeft = " " + strMonthLeft;
                }

                updateHours();
            }

        },0,60000);//Update text every second
    }

    private long getSecondsCount(List<Log> logs) {
        boolean firstStarting = false;
        String temp = "";
        long count = 0;
        int last = 0;
        for (Log log : logs) {
            if (log.get_type() == 0 && !firstStarting) { continue; }
            if (log.get_type() == 1 && !firstStarting) { firstStarting = true; }
            if (log.get_type() == 1) {
                temp = log.get_time();
                last = 1;
            } else {
                try {
                    Date start = sdf.parse(temp);
                    Date end = sdf.parse(log.get_time());
                    long seconds = (end.getTime()-start.getTime())/1000;
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
                long seconds = (now.getTime()-start.getTime())/1000;
                count += seconds;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return count;
    }

    private String countToString(long secondsCount) {
        int secs = (int) (secondsCount % 60);
        int mins = (int) (secondsCount / 60);
        int hours = mins / 60;
        mins = mins % 60;
        if (secs >= 30) mins++;
        return hours + ":" + String.format("%02d", mins); // + ":" + String.format("%02d", secs);
    }
}
