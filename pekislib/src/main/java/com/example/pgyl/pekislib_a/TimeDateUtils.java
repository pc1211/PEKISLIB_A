package com.example.pgyl.pekislib_a;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.example.pgyl.pekislib_a.Constants.ERROR_VALUE;
import static com.example.pgyl.pekislib_a.Constants.NOT_FOUND;

public class TimeDateUtils {
    public enum TIME_UNITS {
        HOURS100(100 * MINUTES_PER_HOUR * SECONDS_PER_MINUTE * MILLISECONDS_PER_SECOND, null, null, null),
        DAY(HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE * MILLISECONDS_PER_SECOND, null, null, null),
        HOUR(MINUTES_PER_HOUR * SECONDS_PER_MINUTE * MILLISECONDS_PER_SECOND, "%02d", ":", "h"),
        MIN(SECONDS_PER_MINUTE * MILLISECONDS_PER_SECOND, "%02d", ":", "m"),
        SEC(MILLISECONDS_PER_SECOND, "%02d", ".", "s"),
        TS(MILLISECONDS_PER_SECOND / 10, "%01d", "", "t"),    //  10e de seconde
        HS(MILLISECONDS_PER_SECOND / 100, "%01d", "", "u"),   //  100e de seconde
        MS(MILLISECONDS_PER_SECOND / 1000, "%01d", "", "v");  //  1000e de seconde

        private long durationMs;              //  Durée de l'unité (en millisecondes)
        private String formatDNumberFormat;   //  Format D:  HH:MM:SS.nnn                 p.ex. 03:00:02.000,  00:02:00.06
        private String formatDSeparator;      //  :
        private String formatDLSeparator;     //  Format DL:   ...h...m...s...t...u...v   p.ex. 3h2s,          2m6u ou 2m0s06
        private TIME_UNITS nextTimeUnit;      //  Prochaine unité à décoder (H->M->S...)

        TIME_UNITS(long durationMs, String formatDNumberFormat, String formatDSeparator, String formatDLSeparator) {
            this.durationMs = durationMs;
            this.formatDNumberFormat = formatDNumberFormat;
            this.formatDSeparator = formatDSeparator;
            this.formatDLSeparator = formatDLSeparator;
            this.nextTimeUnit = null;   //  nextDecodeUnit sera calculé au 1er appel de getFirstTimeUnit
        }

        public long DURATION_MS() {
            return durationMs;
        }

        public String FORMAT_D_SEPARATOR() {
            return formatDSeparator;
        }

        public String FORMAT_DL_SEPARATOR() {
            return formatDLSeparator;
        }

        public String FORMAT_D_NUMBER_FORMAT() {
            return formatDNumberFormat;
        }

        public TIME_UNITS getNext() {  //  Obtenir la prochaine unité à décoder (en format D ou DL) (après avoir appelé getFirstTimeUnitToDecode pour initialisation (lazy))
            return (this.ordinal() < (TIME_UNITS.values().length - 1) ? TIME_UNITS.values()[this.ordinal() + 1] : null);
        }
    }

    public static final SimpleDateFormat ddMMyyyy = new SimpleDateFormat("dd/MM/yyyy");
    public static final SimpleDateFormat HHmm = new SimpleDateFormat("HH:mm");
    public static final SimpleDateFormat HHmmss = new SimpleDateFormat("HH:mm:ss");
    public static final int HOURS_PER_DAY = 24;
    public static final int MINUTES_PER_HOUR = 60;
    public static final int SECONDS_PER_MINUTE = 60;
    public static final int MILLISECONDS_PER_SECOND = 1000;

    public static TIME_UNITS getFirstTimeUnitToDecode() {  //  1e unité à décoder
        return TIME_UNITS.HOUR;
    }

    public static long getMidnightTimeMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long midnightTimeMillis = calendar.getTimeInMillis();
        calendar.clear();
        calendar = null;
        return midnightTimeMillis;
    }

    public static String getFormattedStringTimeDate(String string, SimpleDateFormat sdf) {
        String formattedStringTimeDate = "";
        try {
            Date fdate = sdf.parse(string);
            formattedStringTimeDate = sdf.format(fdate);
        } catch (ParseException ex) {
            //   NOP
        }
        return formattedStringTimeDate;
    }

    public static String getFormattedCalendarTimeDate(Calendar calendar, SimpleDateFormat sdf) {
        return sdf.format(calendar.getTime());
    }

    public static String getFormattedTimeZoneLongTimeDate(long timeDateMillis, SimpleDateFormat sdf) {  //  OK TimeZone
        String formattedTimeZoneLongTimeDate = null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeDateMillis);
        formattedTimeZoneLongTimeDate = sdf.format(calendar.getTime());
        calendar.clear();
        calendar = null;
        return formattedTimeZoneLongTimeDate;
    }

    public static String msToTimeFormatD(long ms, TIME_UNITS timeUnitDisplayPrecision, TIME_UNITS timeUnitRoundingPrecision) {
        String timeFormatD = "";
        long n = ms;
        if (timeUnitRoundingPrecision != null) {
            long p = timeUnitRoundingPrecision.DURATION_MS();
            n = p * ((ms + (p / 2)) / p);   //  Arrondir à l'unité si demandé
        }
        TIME_UNITS tu = getFirstTimeUnitToDecode();
        do {
            long q = n / tu.DURATION_MS();
            timeFormatD = timeFormatD + String.format(tu.FORMAT_D_NUMBER_FORMAT(), q);
            if (!tu.equals(timeUnitDisplayPrecision)) {
                n = n - q * tu.DURATION_MS();
                timeFormatD = timeFormatD + tu.FORMAT_D_SEPARATOR();
            } else {   //  C'est terminé
                break;
            }
            tu = tu.getNext();
        } while (tu != null);
        return timeFormatD;
    }

    public static long msToTimeUnit(long ms, TIME_UNITS timeUnitDisplayPrecision, TIME_UNITS timeUnitRoundingPrecision) {
        long n = ms;
        if (timeUnitRoundingPrecision != null) {
            long p = timeUnitRoundingPrecision.DURATION_MS();
            n = p * ((ms + (p / 2)) / p);   //  Arrondir à l'unité si demandé
        }
        return (n / timeUnitDisplayPrecision.DURATION_MS());
    }

    public static String msToTimeFormatDL(long ms, TIME_UNITS timeUnitDisplayPrecision, TIME_UNITS timeUnitRoundingPrecision) {
        String timeFormatDL = "";
        String collectZeros = "";
        long n = ms;
        if (timeUnitRoundingPrecision != null) {
            long p = timeUnitRoundingPrecision.DURATION_MS();
            n = p * ((ms + (p / 2)) / p);   //  Arrondir à l'unité si demandé
        }
        TIME_UNITS tu = getFirstTimeUnitToDecode();
        do {
            long q = n / tu.DURATION_MS();
            if (q != 0) {   //  Afficher l'unité de temps si différente de 0
                timeFormatDL = timeFormatDL + q;
                if (!tu.FORMAT_D_SEPARATOR().isEmpty()) {  //  Si HOUR, MIN, SEC => ajouter le séparateur DL prévu
                    timeFormatDL = timeFormatDL + tu.FORMAT_DL_SEPARATOR();  //  Pas de format de nombre en format DL;
                }
            } else {   //  0
                collectZeros = collectZeros + "0" + tu.FORMAT_DL_SEPARATOR();
            }
            if (!tu.equals(timeUnitDisplayPrecision)) {   //  Précision demandée non encore atteinte
                n = n - q * tu.DURATION_MS();
            } else {  //  C'est terminé
                if ((q != 0) && (tu.FORMAT_D_SEPARATOR().isEmpty())) {   // Si dixièmes, centièmes ou millièmes, Ajouter le séparateur DL prévu
                    timeFormatDL = timeFormatDL + tu.FORMAT_DL_SEPARATOR();  //  Pas de format de nombre en format DL;
                }
                if (timeFormatDL.isEmpty()) {
                    timeFormatDL = collectZeros;   //  0h0m0s0t si précision TS
                }
                break;
            }
            tu = tu.getNext();
        } while (tu != null);
        return timeFormatDL;
    }

    public static long timeFormatDToMs(String timeFormatD) {
        String str = timeFormatD;
        TIME_UNITS tu = getFirstTimeUnitToDecode();
        long msCount = 0;
        try {
            do {
                if (str.length() > 0) {
                    int i = str.indexOf(tu.FORMAT_D_SEPARATOR());
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
                tu = tu.getNext();
            } while (tu != null);
        } catch (NumberFormatException ex) {
            msCount = ERROR_VALUE;
        }
        tu = null;
        return msCount;
    }

    public static long timeFormatDLToMs(String timeFormatDL) {
        String str = timeFormatDL;
        TIME_UNITS tu = getFirstTimeUnitToDecode();
        TIME_UNITS tud = null;   // Contiendra la dernière unité précisée p.ex. 2m30 => MIN; 2h30s => SEC
        long msCount = 0;
        try {
            int k = -1;   //  Séparateur de l'unité précédente
            do {   //  Attribuer tout ce qui est posible aux unités précisées
                int i = str.indexOf(tu.FORMAT_DL_SEPARATOR());  //  Séparateur DL n'est jamais vide => i<>0
                if (i != NOT_FOUND) {  //  Séparateur trouvé
                    msCount = msCount + tu.DURATION_MS() * Long.parseLong(str.substring(k + 1, i));   //  Après le séparateur de l'unité précédente et avant le séparateur de l'unité en cours
                    k = i;
                    tud = tu;   //  Dernière unité précisée rencontrée
                }
                tu = tu.getNext();
            } while (tu != null);
            if (k != (str.length() - 1)) {   //  Dernières unités non précisées  (p.ex. 14m2s26 => TS=2 HS=6)
                if (tud != null) {  //  Dernière unité précisée rencontrée
                    TIME_UNITS tun = tud.getNext();  //  la 1e unité parmi les unités non précisées
                    str = str.substring(k + 1);   //  Après la dernière unité précisée
                    if (tun != null) {
                        if (tun.FORMAT_D_SEPARATOR().isEmpty()) {   //  concerne TS, HS, MS => Il reste <TS> ou <TS><HS> ou <TS><HS><MS> à attribuer  (p.ex. 3s45 => TS=4 HS=5)
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
                    msCount = msCount + getFirstTimeUnitToDecode().DURATION_MS() * Long.parseLong(str);
                }
            }
        } catch (NumberFormatException ex) {
            msCount = ERROR_VALUE;
        }
        tud = null;
        return msCount;
    }

}
