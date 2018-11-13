package com.example.pgyl.pekislib_a;

import android.content.Context;
import android.content.Intent;
import android.provider.AlarmClock;
import android.widget.Toast;

import java.util.Calendar;

import static com.example.pgyl.pekislib_a.MiscUtils.toastLong;
import static com.example.pgyl.pekislib_a.TimeDateUtils.formattedCalendarTimeDate;
import static com.example.pgyl.pekislib_a.TimeDateUtils.hhmm;

public class ClockAppAlarmUtils {

    public static boolean setClockAppAlarm(Context context, long timeExp, String message) {
        boolean ret = false;
        Calendar calendar = Calendar.getInstance();    // Calendar => OK Time Zone
        String timeNowAsHHMM = formattedCalendarTimeDate(calendar, hhmm);
        calendar.setTimeInMillis(timeExp);
        String timeExpAsHHMM = formattedCalendarTimeDate(calendar, hhmm);
        int hourExp = Calendar.HOUR_OF_DAY;
        int minExp = Calendar.MINUTE;
        calendar.clear();
        calendar = null;
        String errorMsg = "Setting Clock App alarm on " + timeExpAsHHMM;
        if (!timeExpAsHHMM.equals(timeNowAsHHMM)) {
            Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
            intent.putExtra(AlarmClock.EXTRA_HOUR, hourExp);
            intent.putExtra(AlarmClock.EXTRA_MINUTES, minExp);
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, message);
            intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);    //  Ne pas afficher Clock App
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                ret = true;
                toastLong(errorMsg, context);
                context.startActivity(intent);
            }
        }
        if (!ret) {
            errorMsg = "Error " + errorMsg;
            toastLong(errorMsg, context);
        }
        return ret;
    }

    public static boolean dismissClockAppAlarm(Context context, String message) {
        boolean ret = false;
        String errorMsg = "Dismissing Clock App alarm " + message;
        Intent intent = new Intent(AlarmClock.ACTION_DISMISS_ALARM);
        intent.putExtra(AlarmClock.EXTRA_ALARM_SEARCH_MODE, AlarmClock.ALARM_SEARCH_MODE_LABEL);
        intent.putExtra(AlarmClock.EXTRA_MESSAGE, message);      //  On ne peut pas empêcher Clock App de s'afficher
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            ret = true;
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
            context.startActivity(intent);
        }
        if (!ret) {
            errorMsg = "Error " + errorMsg;
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
        }
        return ret;
    }

}
