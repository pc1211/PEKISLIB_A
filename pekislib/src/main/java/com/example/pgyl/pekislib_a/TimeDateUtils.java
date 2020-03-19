package com.example.pgyl.pekislib_a;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.example.pgyl.pekislib_a.Constants.ERROR_VALUE;
import static com.example.pgyl.pekislib_a.Constants.NOT_FOUND;

public class TimeDateUtils {
    public static class TimeUnitFormat {
        TimeUnitFormat(String numberFormat, String separator) {
            this.numberFormat = numberFormat;   //  Format pour nombre de l'unité TIME_UNITS
            this.separator = separator;         //  Séparateur suivant l'unité TIME_UNITS
        }

        public String numberFormat;
        public String separator;
    }

    public enum TIME_UNITS {
        HOURS100(100 * MINUTES_PER_HOUR * SECONDS_PER_MINUTE * MILLISECONDS_PER_SECOND, null, null),
        DAY(HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE * MILLISECONDS_PER_SECOND, null, null),
        HOUR(MINUTES_PER_HOUR * SECONDS_PER_MINUTE * MILLISECONDS_PER_SECOND, new TimeUnitFormat("%02d", ":"), new TimeUnitFormat(null, "h")),
        MIN(SECONDS_PER_MINUTE * MILLISECONDS_PER_SECOND, new TimeUnitFormat("%02d", ":"), new TimeUnitFormat(null, "m")),
        SEC(MILLISECONDS_PER_SECOND, new TimeUnitFormat("%02d", "."), new TimeUnitFormat(null, "s")),
        TS(MILLISECONDS_PER_SECOND / 10, new TimeUnitFormat("%01d", ""), new TimeUnitFormat(null, "t")),    //  10e de seconde
        HS(MILLISECONDS_PER_SECOND / 100, new TimeUnitFormat("%01d", ""), new TimeUnitFormat(null, "u")),   //  100e de seconde
        MS(MILLISECONDS_PER_SECOND / 1000, new TimeUnitFormat("%01d", ""), new TimeUnitFormat(null, "v"));  //  1000e de seconde

        private long durationMs;          //  Durée de l'unité (en millisecondes)
        private TimeUnitFormat formatD;      //  HH:MM:SS.nnn               p.ex. 03:00:02.000,  00:02:00.06
        private TimeUnitFormat formatDL;     //  ...h...m...s...t...u...v   p.ex. 3h2s,          2m6u ou 2m0s06
        private TIME_UNITS nextTimeUnit;  //  Prochaine unité à décoder (H->M->S...)

        TIME_UNITS(long durationMs, TimeUnitFormat formatD, TimeUnitFormat formatDL) {
            this.durationMs = durationMs;
            this.formatD = formatD;
            this.formatDL = formatDL;
            this.nextTimeUnit = null;   //  nextDecodeUnit sera calculé au 1er appel de getFirstTimeUnit
        }

        public long DURATION_MS() {
            return durationMs;
        }

        public TimeUnitFormat FORMAT_D() {
            return formatD;
        }

        public TimeUnitFormat FORMAT_DL() {
            return formatDL;
        }

        public TIME_UNITS getNextTimeUnit() {  //  Obtenir la prochaine unité à décoder (en format D ou DL) (après avoir appelé getFirstTimeUnit pour initialisation (lazy))
            return nextTimeUnit;
        }

        public void setNextTimeUnit(TIME_UNITS nextTimeUnit) {
            this.nextTimeUnit = nextTimeUnit;
        }
    }

    public static final SimpleDateFormat ddMMyyyy = new SimpleDateFormat("dd/MM/yyyy");
    public static final SimpleDateFormat HHmm = new SimpleDateFormat("HH:mm");
    public static final SimpleDateFormat HHmmss = new SimpleDateFormat("HH:mm:ss");
    public static final int HOURS_PER_DAY = 24;
    public static final int MINUTES_PER_HOUR = 60;
    public static final int SECONDS_PER_MINUTE = 60;
    public static final int MILLISECONDS_PER_SECOND = 1000;

    public static TIME_UNITS getFirstTimeUnit() {  //  1e unité à décoder et initialisation (lazy) de nextTimeUnit de chaque unité
        TIME_UNITS ret = TIME_UNITS.HOUR;
        if (ret.getNextTimeUnit() == null) {  //  nextDecodeUnit pas encore initialisés
            TIME_UNITS.HOUR.setNextTimeUnit(TIME_UNITS.MIN);   //  On décode les minutes après les heures
            TIME_UNITS.MIN.setNextTimeUnit(TIME_UNITS.SEC);
            TIME_UNITS.SEC.setNextTimeUnit(TIME_UNITS.TS);
            TIME_UNITS.TS.setNextTimeUnit(TIME_UNITS.HS);
            TIME_UNITS.HS.setNextTimeUnit(TIME_UNITS.MS);
            TIME_UNITS.MS.setNextTimeUnit(null);   //  MS est la dernière unité à décoder
        }
        return ret;
    }

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

    public static String formattedTimeZoneLongTimeDate(long timeDateMillis, SimpleDateFormat sdf) {  //  OK TimeZone
        String ret = null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeDateMillis);
        ret = sdf.format(calendar.getTime());
        calendar.clear();
        calendar = null;
        return ret;
    }

    public static String msToTimeFormatD(long ms, TIME_UNITS timeUnit) {
        String ret = "";
        long p = timeUnit.DURATION_MS();
        long n = p * ((ms + (p / 2)) / p);  //  Arrondir à l'unité nécessaire
        TIME_UNITS tu = getFirstTimeUnit();
        do {
            long q = n / tu.DURATION_MS();
            ret = ret + String.format(tu.FORMAT_D().numberFormat, q);
            if (!tu.equals(timeUnit)) {
                n = n - q * tu.DURATION_MS();
            } else {
                break;  //  Pas de séparateur pour terminer
            }
            ret = ret + tu.FORMAT_D().separator;
            tu = tu.getNextTimeUnit();
        } while (tu != null);
        return ret;
    }

    public static String msToTimeFormatDL(long ms, TIME_UNITS timeUnit) {
        String ret = "";
        long p = timeUnit.DURATION_MS();
        long n = p * ((ms + (p / 2)) / p);  //  Arrondir à l'unité nécessaire
        TIME_UNITS tu = getFirstTimeUnit();
        do {
            long q = n / tu.DURATION_MS();
            ret = ret + q;
            if (tu.FORMAT_D().separator.length() != 0)   //  Si HOUR, MIN, SEC => ajouter le séparateur DL prévu
                ret = ret + tu.FORMAT_DL().separator;   //  Pas de format de nombre en format DL;
            if (!tu.equals(timeUnit)) {
                n = n - q * tu.DURATION_MS();
            } else {
                break;
            }
            tu = tu.getNextTimeUnit();
        } while (tu != null);
        return ret;
    }

    public static long timeFormatDToMs(String timeFormatD) {
        String str = timeFormatD;
        TIME_UNITS tu = getFirstTimeUnit();
        long msCount = 0;
        try {
            do {
                if (str.length() > 0) {
                    int i = str.indexOf(tu.formatD.separator);
                    if (i > 0) {   //  Séparateur (non vide) trouvé  = concerne HOUR, MIN, SEC
                        msCount = msCount + tu.DURATION_MS() * Long.parseLong(str.substring(0, i));   //  Avant le séparateur
                        str = str.substring(i + 1);   //  Après le séparateur
                    } else {
                        if (i == NOT_FOUND) {   //  Séparateur (non vide) non trouvé => concerne HOUR ou MIN ou SEC
                            msCount = msCount + tu.DURATION_MS() * Long.parseLong(str);   //  Tout est attribué à cette unité
                            str = "";
                        } else {  // 0  (séparateur vide) => concerne TS, HS, MS => Il reste <TS> ou <TS><HS> ou <TS><HS><MS>
                            msCount = msCount + tu.DURATION_MS() * Long.parseLong(str.substring(0, 1));   //  Un seul caractère par unité
                            str = str.substring(1);
                        }
                    }
                }
                tu = tu.getNextTimeUnit();
            } while (tu != null);
        } catch (NumberFormatException ex) {
            msCount = ERROR_VALUE;
        }
        tu = null;
        return msCount;
    }

    public static long timeFormatDLToMs(String timeFormatDL) {
        String str = timeFormatDL;
        TIME_UNITS tu = getFirstTimeUnit();
        TIME_UNITS tud = null;   // Contiendra la dernière unité précisée p.ex. 2m30 => MIN; 2h30s => SEC
        long msCount = 0;
        try {
            int k = -1;   //  Séparateur de l'unité précédente
            do {   //  Attribuer tout ce qui est posible aux unités précisées
                int i = str.indexOf(tu.formatDL.separator);  //  Séparateur DL n'est jamais vide => i<>0
                if (i != NOT_FOUND) {  //  Séparateur trouvé
                    msCount = msCount + tu.DURATION_MS() * Long.parseLong(str.substring(k + 1, i));   //  Après le séparateur de l'unité précédente et avant le séparateur de l'unité en cours
                    k = i;
                    tud = tu;   //  Dernière unité précisée rencontrée
                }
                tu = tu.getNextTimeUnit();
            } while (tu != null);
            if (k != (str.length() - 1)) {   //  Dernières unités non précisées  (p.ex. 14m2s26 => TS=2 HS=6)
                if (tud != null) {  //  Dernière unité précisée rencontrée
                    TIME_UNITS tun = tud.getNextTimeUnit();  //  la 1e unité parmi les unités non précisées
                    str = str.substring(k + 1);   //  Après la dernière unité précisée
                    if (tun != null) {
                        if (tun.formatD.separator.length() == 0) {   //  concerne TS, HS, MS => Il reste <TS> ou <TS><HS> ou <TS><HS><MS> à attribuer  (p.ex. 3s45 => TS=4 HS=5)
                            do {
                                msCount = msCount + tun.DURATION_MS() * Long.parseLong(str.substring(0, 1));   //  Un seul caractère par unité
                                str = str.substring(1);
                                tun = tun.nextTimeUnit;
                            }
                            while ((tun != null) && (str.length() != 0));
                        } else {   //  concerne MIN, SEC   (p.ex. 2h50 => MIN = 50 ou 2h14m56 => SEC = 56)
                            msCount = msCount + tun.DURATION_MS() * Long.parseLong(str);   //  attribution du tout à cette unité
                        }
                    } else {    //  tun=null => concerne MS => Que faire ??? ( p.ex 7h4v15 ???)
                        msCount = ERROR_VALUE;   //  Désolé
                    }
                    tun = null;
                } else {  // Aucune unité précisée => Tout attribué à la 1e unité décodable
                    msCount = msCount + getFirstTimeUnit().DURATION_MS() * Long.parseLong(str);
                }
            }
        } catch (NumberFormatException ex) {
            msCount = ERROR_VALUE;
        }
        tud = null;
        return msCount;
    }

}
