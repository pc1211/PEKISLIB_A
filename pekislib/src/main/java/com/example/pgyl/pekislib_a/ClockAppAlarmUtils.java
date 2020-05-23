package com.example.pgyl.pekislib_a;

import android.content.Context;
import android.content.Intent;
import android.provider.AlarmClock;

import java.util.Calendar;

import static com.example.pgyl.pekislib_a.MiscUtils.toastLong;
import static com.example.pgyl.pekislib_a.TimeDateUtils.HHmm;
import static com.example.pgyl.pekislib_a.TimeDateUtils.getFormattedCalendarTimeDate;

public class ClockAppAlarmUtils {

    public static boolean setClockAppAlarm(Context context, long timeExp, String alarmLabel, String toastMessage) {
        boolean setOK = false;
        Calendar calendar = Calendar.getInstance();    // Calendar => OK Time Zone
        String sTimeNow = getFormattedCalendarTimeDate(calendar, HHmm);
        calendar.setTimeInMillis(timeExp);
        String sTimeExp = getFormattedCalendarTimeDate(calendar, HHmm);
        int hourExp = calendar.get(Calendar.HOUR_OF_DAY);
        int minExp = calendar.get(Calendar.MINUTE);
        calendar.clear();
        calendar = null;
        if (!sTimeExp.equals(sTimeNow)) {
            Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
            intent.putExtra(AlarmClock.EXTRA_HOUR, hourExp);
            intent.putExtra(AlarmClock.EXTRA_MINUTES, minExp);
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, alarmLabel);
            intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);    //  Ne pas afficher Clock App
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                setOK = true;
                if (toastMessage != null) {
                    toastLong(toastMessage, context);
                }
                context.startActivity(intent);
            }
        }
        if (!setOK) {
            toastLong("Error" + ((toastMessage != null) ? " " + toastMessage : ""), context);
        }
        return setOK;
    }

    public static boolean dismissClockAppAlarm(Context context, String alarmLabel, String toastMessage) {   //  On ne peut pas empêcher Clock App de s'afficher
        boolean setOK = false;
        Intent intent = new Intent(AlarmClock.ACTION_DISMISS_ALARM);
        intent.putExtra(AlarmClock.EXTRA_ALARM_SEARCH_MODE, AlarmClock.ALARM_SEARCH_MODE_LABEL);
        intent.putExtra(AlarmClock.EXTRA_MESSAGE, alarmLabel);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            setOK = true;
            if (toastMessage != null) {
                toastLong(toastMessage, context);
            }
            context.startActivity(intent);
        }
        if (!setOK) {
            toastLong("Error" + ((toastMessage != null) ? " " + toastMessage : ""), context);
        }
        return setOK;
    }

}
