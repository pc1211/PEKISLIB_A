package com.example.pgyl.pekislib_a;

import android.content.Context;
import android.content.Intent;
import android.provider.AlarmClock;

import java.util.Calendar;

import static com.example.pgyl.pekislib_a.MiscUtils.toastLong;
import static com.example.pgyl.pekislib_a.TimeDateUtils.HHmm;
import static com.example.pgyl.pekislib_a.TimeDateUtils.formattedCalendarTimeDate;

public class ClockAppAlarmUtils {

    public static boolean setClockAppAlarm(Context context, long timeExp, String message) {
        boolean ret = false;
        Calendar calendar = Calendar.getInstance();    // Calendar => OK Time Zone
        String sTimeNow = formattedCalendarTimeDate(calendar, HHmm);
        calendar.setTimeInMillis(timeExp);
        String sTimeExp = formattedCalendarTimeDate(calendar, HHmm);
        int hourExp = calendar.get(Calendar.HOUR_OF_DAY);
        int minExp = calendar.get(Calendar.MINUTE);
        calendar.clear();
        calendar = null;
        String settingMessage = "Setting Clock App alarm on " + sTimeExp;
        if (!sTimeExp.equals(sTimeNow)) {
            Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
            intent.putExtra(AlarmClock.EXTRA_HOUR, hourExp);
            intent.putExtra(AlarmClock.EXTRA_MINUTES, minExp);
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, message);
            intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);    //  Ne pas afficher Clock App
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                ret = true;
                toastLong(settingMessage, context);
                context.startActivity(intent);
            }
        }
        if (!ret) {
            toastLong("Error " + settingMessage, context);
        }
        return ret;
    }

    public static boolean dismissClockAppAlarm(Context context, String message) {
        boolean ret = false;
        Intent intent = new Intent(AlarmClock.ACTION_DISMISS_ALARM);
        intent.putExtra(AlarmClock.EXTRA_ALARM_SEARCH_MODE, AlarmClock.ALARM_SEARCH_MODE_LABEL);
        intent.putExtra(AlarmClock.EXTRA_MESSAGE, message);      //  On ne peut pas empêcher Clock App de s'afficher
        String dismissingMessage = "Dismissing Clock App alarm " + message;
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            ret = true;
            toastLong(dismissingMessage, context);
            context.startActivity(intent);
        }
        if (!ret) {
            toastLong("Error " + dismissingMessage, context);
        }
        return ret;
    }

}
