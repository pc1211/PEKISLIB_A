package com.example.pgyl.pekislib_a;

import android.content.Context;
import android.content.Intent;
import android.provider.AlarmClock;
import android.widget.Toast;

import java.util.Calendar;

import static com.example.pgyl.pekislib_a.TimeDateUtils.TIME_HMS_SEPARATOR;

public class ClockAppAlarmUtils {

    public static void setClockAppAlarm(Context context, long timeExp, String message) {
        boolean error = true;
        Calendar calendar = Calendar.getInstance();    // Calendar => OK Time Zone
        int nowHour = calendar.get(Calendar.HOUR_OF_DAY);
        int nowMinutes = calendar.get(Calendar.MINUTE);
        calendar.setTimeInMillis(timeExp);
        int expHour = calendar.get(Calendar.HOUR_OF_DAY);
        int expMinutes = calendar.get(Calendar.MINUTE);
        if ((nowHour != expHour) || (nowMinutes != expMinutes)) {
            Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
            intent.putExtra(AlarmClock.EXTRA_HOUR, expHour);
            intent.putExtra(AlarmClock.EXTRA_MINUTES, expMinutes);
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, message);
            intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);    //  Skip UI
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                error = false;
                Toast.makeText(context, "Setting Clock App alarm on " + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + TIME_HMS_SEPARATOR + String.format("%02d", calendar.get(Calendar.MINUTE)), Toast.LENGTH_LONG).show();
                context.startActivity(intent);
            }
        }
        calendar = null;
        if (error) {
            Toast.makeText(context, "Error Setting Clock App alarm " + message, Toast.LENGTH_LONG).show();
        }
    }

    public static void dismissClockAppAlarm(Context context, String message) {
        boolean error = true;
        Intent intent = new Intent(AlarmClock.ACTION_DISMISS_ALARM);
        intent.putExtra(AlarmClock.EXTRA_ALARM_SEARCH_MODE, AlarmClock.ALARM_SEARCH_MODE_LABEL);
        intent.putExtra(AlarmClock.EXTRA_MESSAGE, message);      //  Pas de Skip UI possible
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            error = false;
            Toast.makeText(context, "Dismissing Clock App alarm " + message, Toast.LENGTH_LONG).show();
            context.startActivity(intent);
        }
        if (error) {
            Toast.makeText(context, "Error Dismissing Clock App alarm " + message, Toast.LENGTH_LONG).show();
        }
    }

}
