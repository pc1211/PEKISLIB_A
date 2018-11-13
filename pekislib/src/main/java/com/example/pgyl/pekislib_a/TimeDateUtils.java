package com.example.pgyl.pekislib_a;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.example.pgyl.pekislib_a.Constants.ERROR_VALUE;
import static com.example.pgyl.pekislib_a.Constants.NOT_FOUND;

public class TimeDateUtils {
    public enum TIMEUNITS {
        HOURS100(100 * 60 * 60 * 1000), DAY(24 * 60 * 60 * 1000), HOUR(60 * 60 * 1000), MIN(60 * 1000), SEC(1000), CS(10);

        private long valueDurationMs;

        TIMEUNITS(long valueDurationMs) {
            this.valueDurationMs = valueDurationMs;
        }

        public long MS() {
            return valueDurationMs;
        }
    }

    public static class TimeUnitsStruc {
        public long hour;
        public long min;
        public long sec;
        public long cs;
    }

    public static final SimpleDateFormat ddmmyyyy = new SimpleDateFormat("dd/MM/yyyy");
    public static final SimpleDateFormat hhmm = new SimpleDateFormat("hh:mm");
    public static final String[] days = {"??", "Dim", "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam"};

    public static long midnightTimeMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long ret = calendar.getTimeInMillis();
        calendar.clear();
        calendar = null;
        return ret;
    }

    public static String convertMsToHms(long ms, TIMEUNITS timeUnit) {    //   n (en ms) -> HH:MM:SS.CC (format "hmsc")
        String ret = "";
        long p = timeUnit.MS();
        long n = p * ((ms + (p / 2)) / p);  //  Arrondir à l'unité nécessaire
        long h = n / TIMEUNITS.HOUR.MS();
        ret = ret + String.format("%02d", h);
        if (!timeUnit.equals(TIMEUNITS.HOUR)) {  //  => MIN, SEC ou CS
            n = n - h * TIMEUNITS.HOUR.MS();
            long m = n / TIMEUNITS.MIN.MS();
            ret = ret + ":" + String.format("%02d", m);
            if (!timeUnit.equals(TIMEUNITS.MIN)) {  //  => SEC ou CS
                n = n - m * TIMEUNITS.MIN.MS();
                long s = n / TIMEUNITS.SEC.MS();
                ret = ret + ":" + String.format("%02d", s);
                if (!timeUnit.equals(TIMEUNITS.SEC)) {  //  => CS
                    n = n - s * TIMEUNITS.SEC.MS();
                    long c = n / TIMEUNITS.CS.MS();
                    ret = ret + "." + String.format("%02d", c);
                }
            }
        }
        return ret;
    }

    public static String convertMsToXhms(long ms, TIMEUNITS timeUnit) {    //   n (en ms) -> p.ex. 3m, 20s, 2m30s, 1h2s, 8s7c, ... (format "xhmsc")
        String ret = "";
        long tums = timeUnit.MS();
        long n = tums * ((ms + (tums / 2)) / tums);  //  Arrondir à l'unité nécessaire
        long h = n / TIMEUNITS.HOUR.MS();
        ret = ret + h + "h";
        if (!timeUnit.equals(TIMEUNITS.HOUR)) {  //  => MIN, SEC ou CS
            n = n - h * TIMEUNITS.HOUR.MS();
            long m = n / TIMEUNITS.MIN.MS();
            ret = ret + m + "m";
            if (!timeUnit.equals(TIMEUNITS.MIN)) {  //  => SEC ou CS
                n = n - m * TIMEUNITS.MIN.MS();
                long s = n / TIMEUNITS.SEC.MS();
                ret = ret + s + "s";
                if (!timeUnit.equals(TIMEUNITS.SEC)) {  //  => CS
                    n = n - s * TIMEUNITS.SEC.MS();
                    long c = n / TIMEUNITS.CS.MS();
                    ret = ret + c + "c";
                }
            }
        }
        return ret;
    }

    public static long convertHmsToMs(String hms) {
        long ret = ERROR_VALUE;
        TimeUnitsStruc tus = convertHmsToTimeUnitsStruc(hms);
        if (tus != null) {
            ret = tus.hour * TIMEUNITS.HOUR.MS() + tus.min * TIMEUNITS.MIN.MS() + tus.sec * TIMEUNITS.SEC.MS() + tus.cs * TIMEUNITS.CS.MS();
            tus = null;
        }
        return ret;
    }

    public static TimeUnitsStruc convertHmsToTimeUnitsStruc(String hms) {
        String str = hms;
        long h = 0;
        long m = 0;
        long s = 0;
        long c = 0;
        long retc = 0;
        TimeUnitsStruc ret = null;
        try {
            int i = str.indexOf(":");
            if (i == NOT_FOUND) { //  => H
                h = Long.parseLong(str);
            } else {   //  1er ":" trouvé => H:M
                h = Long.parseLong(str.substring(0, i));
                str = str.substring(i + 1);
                i = str.indexOf(":");
                if (i == NOT_FOUND) {
                    m = Long.parseLong(str);
                } else {  //  2e ":" trouvé => H:M:S
                    m = Long.parseLong(str.substring(0, i));
                    str = str.substring(i + 1);
                    i = str.indexOf(".");
                    if (i == NOT_FOUND) {
                        s = Long.parseLong(str);
                    } else { //  "." trouvé => H:M:S.C
                        s = Long.parseLong(str.substring(0, i));
                        c = Long.parseLong(str.substring(i + 1));
                    }
                }
            }
        } catch (NumberFormatException ex) {
            retc = ERROR_VALUE;
        }
        if (retc != ERROR_VALUE) {
            ret = new TimeUnitsStruc();
            ret.hour = h;
            ret.min = m;
            ret.sec = s;
            ret.cs = c;
        }
        return ret;
    }

    public static long convertXhmsToMs(String xhms) {
        long ret = ERROR_VALUE;
        TimeUnitsStruc tus = convertXhmsToTimeUnitsStruc(xhms);
        if (tus != null) {
            ret = tus.hour * TIMEUNITS.HOUR.MS() + tus.min * TIMEUNITS.MIN.MS() + tus.sec * TIMEUNITS.SEC.MS() + tus.cs * TIMEUNITS.CS.MS();
            tus = null;
        }
        return ret;
    }

    public static TimeUnitsStruc convertXhmsToTimeUnitsStruc(String xhms) {
        String str = xhms;
        long h = 0;
        long m = 0;
        long s = 0;
        long c = 0;
        long retc = 0;
        TIMEUNITS tu = null;   // Contiendra la dernière unité précisée p.ex. 2m30 => m; 2h30s => s
        TimeUnitsStruc ret = null;
        try {
            int k = NOT_FOUND;
            int i = str.indexOf("h");
            if (i != NOT_FOUND) {
                h = Long.parseLong(str.substring(k + 1, i));
                k = i;
                tu = TIMEUNITS.HOUR;
            }
            i = str.indexOf("m");
            if (i != NOT_FOUND) {
                m = Long.parseLong(str.substring(k + 1, i));
                k = i;
                tu = TIMEUNITS.MIN;
            }
            i = str.indexOf("s");
            if (i != NOT_FOUND) {
                s = Long.parseLong(str.substring(k + 1, i));
                k = i;
                tu = TIMEUNITS.SEC;
            }
            i = str.indexOf("c");
            if (i != NOT_FOUND) {
                c = Long.parseLong(str.substring(k + 1, i));
                k = i;
                tu = TIMEUNITS.CS;
            }
            if (k != (str.length() - 1)) {   //  Dernière unité non précisée
                long n = Long.parseLong(str.substring(k + 1));  // Valeur pour l'unité non précisée => Ajout de l'unité dans l'ordre logique
                if (tu != null) {
                    if (tu.equals(TIMEUNITS.HOUR)) {  //  H => M
                        m = n;
                    }
                    if (tu.equals(TIMEUNITS.MIN)) {  //  M => S   p.ex. 2m30 => 2m30s
                        s = n;
                    }
                    if (tu.equals(TIMEUNITS.SEC)) {  //  S => C
                        c = n;
                    }
                    if (tu.equals(TIMEUNITS.CS)) {   //  C => ???  p.ex 7h4c15
                        retc = ERROR_VALUE;
                    }
                } else {  // Aucune unité précisée => H
                    h = n;
                }
            }
        } catch (NumberFormatException ex) {
            retc = ERROR_VALUE;
        }
        if (retc != ERROR_VALUE) {
            ret = new TimeUnitsStruc();
            ret.hour = h;
            ret.min = m;
            ret.sec = s;
            ret.cs = c;
        }
        return ret;
    }

    public static String formattedStringTimeDate(String string, SimpleDateFormat sdf) {
        String ret = "";
        try {
            Date fdate = sdf.parse(string);
            ret = sdf.format(fdate);
        } catch (ParseException ex) {
            //   NOP
        }
        return ret;
    }

    public static String formattedCalendarTimeDate(Calendar calendar, SimpleDateFormat sdf) {
        return sdf.format(calendar.getTime());
    }

}
