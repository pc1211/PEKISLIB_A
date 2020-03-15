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

        private long durationMs;       //  Durée de l'unité (en millisecondes)
        private String numberFormatD;  //  Format pour nombres si temps exprimé en format D (HH:MM:SS.nnn); Il n'y a pas de format pour format DL (...h...m...s...t...u...v); ; HOURS100 et DAY ne sont pas concernés par un décodage de formatD ou DL
        private String separatorD;     //  Séparateur se trouvant après l'unité si format D; Si "" => Il n'existe pas de séparateur spécifique pour les fractions de seconde (TS, HS, MS)
        private String separatorDL;    //  Séparateur se trouvant après l'unité si format DL; sert uniquement à un encodage direct (p.ex. 00:00:00.06 entré comme 6u mais aussi éventuellement comme 0s06 )
        private long tag;              //  Pour stockage temporaire d'un nombre associé à l'unité pendant une conversion (p.ex. HOUR.setTag(20) => stocke 20 heures dans HOUR)
        private TIME_UNITS nextDecodeUnit;   //  Prochaine unité à décoder dans le format D ou DL  (H->M->S...)

        TIME_UNITS(long durationMs, String numberFormatD, String separatorD, String separatorDL) {
            this.durationMs = durationMs;
            this.numberFormatD = numberFormatD;
            this.separatorD = separatorD;
            this.separatorDL = separatorDL;
            this.nextDecodeUnit = this;   //  nextDecodeUnit sera calculé au 1er appel de getNextDecodeUnit ("lazy")
        }

        public long MS() {
            return durationMs;
        }

        public String NUMBER_FORMAT_D() {
            return numberFormatD;
        }

        public String SEPARATOR_D() {
            return separatorD;
        }

        public String SEPARATOR_DL() {
            return separatorDL;
        }

        public void setTag(long tag) {
            this.tag = tag;
        }

        public long getTag() {
            return tag;
        }

        public TIME_UNITS getNextDecodeUnit() {  //  Obtenir la prochaine unité à décoder (en format D ou DL)
            if (this.equals(nextDecodeUnit)) {  //  nextDecodeUnit est calculé au 1er appel de getNextDecodeUnit ("lazy")
                if (this.equals(TIME_UNITS.HOUR)) {
                    nextDecodeUnit = TIME_UNITS.MIN;   //  On décode les minutes après les heures
                }
                if (this.equals(TIME_UNITS.MIN)) {
                    nextDecodeUnit = TIME_UNITS.SEC;
                }
                if (this.equals(TIME_UNITS.SEC)) {
                    nextDecodeUnit = TIME_UNITS.TS;
                }
                if (this.equals(TIME_UNITS.TS)) {
                    nextDecodeUnit = TIME_UNITS.HS;
                }
                if (this.equals(TIME_UNITS.HS)) {
                    nextDecodeUnit = TIME_UNITS.MS;
                }
                if (this.equals(TIME_UNITS.MS)) {   //  MS est la dernière unité à décoder => null
                    nextDecodeUnit = null;
                }
            }
            return nextDecodeUnit;
        }
    }

    public static final SimpleDateFormat ddMMyyyy = new SimpleDateFormat("dd/MM/yyyy");
    public static final SimpleDateFormat HHmm = new SimpleDateFormat("HH:mm");
    public static final SimpleDateFormat HHmmss = new SimpleDateFormat("HH:mm:ss");
    public static final int HOURS_PER_DAY = 24;
    public static final int MINUTES_PER_HOUR = 60;
    public static final int SECONDS_PER_MINUTE = 60;
    public static final int MILLISECONDS_PER_SECOND = 1000;

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
        long p = timeUnit.MS();
        long n = p * ((ms + (p / 2)) / p);  //  Arrondir à l'unité nécessaire
        TIME_UNITS tu = getFirstDecodeUnit();
        do {
            long q = n / tu.MS();
            ret = ret + String.format(tu.NUMBER_FORMAT_D(), q);
            if (!tu.equals(timeUnit)) {
                n = n - q * tu.MS();
            } else {
                break;  //  Pas de séparateur pour terminer
            }
            ret = ret + tu.SEPARATOR_D();   //  Séparateur pour Format D
            tu = tu.getNextDecodeUnit();
        } while (tu != null);
        return ret;
    }

    public static String msToTimeFormatDL(long ms, TIME_UNITS timeUnit) {
        String ret = "";
        long p = timeUnit.MS();
        long n = p * ((ms + (p / 2)) / p);  //  Arrondir à l'unité nécessaire
        TIME_UNITS tu = getFirstDecodeUnit();
        do {
            long q = n / tu.MS();
            ret = ret + q;
            if (tu.SEPARATOR_D().length() != 0)   //  Si HOUR, MIN, SEC => ajouter le séparateur DL prévu
                ret = ret + tu.SEPARATOR_DL();   //  Pas de format de nombre en format DL;
            if (!tu.equals(timeUnit)) {
                n = n - q * tu.MS();
            } else {
                break;
            }
            tu = tu.getNextDecodeUnit();
        } while (tu != null);
        return ret;
    }

    public static long timeFormatDToMs(String timeFormatD) {
        long ret = ERROR_VALUE;
        if (timeFormatDToTIMEUNITSTags(timeFormatD)) {
            ret = TIME_UNITS.HOUR.getTag() * TIME_UNITS.HOUR.MS() + TIME_UNITS.MIN.getTag() * TIME_UNITS.MIN.MS() + TIME_UNITS.SEC.getTag() * TIME_UNITS.SEC.MS() + TIME_UNITS.TS.getTag() * TIME_UNITS.TS.MS() + TIME_UNITS.HS.getTag() * TIME_UNITS.HS.MS() + TIME_UNITS.MS.getTag() * TIME_UNITS.MS.MS();
        }
        return ret;
    }

    public static boolean timeFormatDToTIMEUNITSTags(String timeFormatD) {
        String str = timeFormatD;
        TIME_UNITS tu = getFirstDecodeUnit();
        boolean ret = true;
        try {
            do {
                tu.setTag(0);
                if (str.length() > 0) {
                    int i = str.indexOf(tu.separatorD);
                    if (i > 0) {   //  Séparateur (non vide) trouvé  = concerne HOUR, MIN, SEC
                        tu.setTag(Long.parseLong(str.substring(0, i)));   //  Avant le séparateur
                        str = str.substring(i + 1);   //  Après le séparateur
                    } else {
                        if (i == NOT_FOUND) {   //  Séparateur (non vide) non trouvé => concerne HOUR ou MIN ou SEC
                            tu.setTag(Long.parseLong(str));   //  Tout est attribué à cette unité
                            str = "";
                        } else {  // 0  (séparateur vide) => concerne TS, HS, MS => Il reste <TS> ou <TS><HS> ou <TS><HS><MS>
                            tu.setTag(Long.parseLong(str.substring(0, 1)));   //  Un seul caractère par unité
                            str = str.substring(1);
                        }
                    }
                }
                tu = tu.getNextDecodeUnit();
            } while (tu != null);
        } catch (NumberFormatException ex) {
            ret = false;
        }
        tu = null;
        return ret;
    }

    public static long timeFormatDLToMs(String timeFormatDL) {
        long ret = ERROR_VALUE;
        if (timeFormatDLToTIMEUNITSTags(timeFormatDL)) {
            ret = TIME_UNITS.HOUR.getTag() * TIME_UNITS.HOUR.MS() + TIME_UNITS.MIN.getTag() * TIME_UNITS.MIN.MS() + TIME_UNITS.SEC.getTag() * TIME_UNITS.SEC.MS() + TIME_UNITS.TS.getTag() * TIME_UNITS.TS.MS() + TIME_UNITS.HS.getTag() * TIME_UNITS.HS.MS() + TIME_UNITS.MS.getTag() * TIME_UNITS.MS.MS();
        }
        return ret;
    }

    public static boolean timeFormatDLToTIMEUNITSTags(String timeFormatDL) {
        String str = timeFormatDL;
        TIME_UNITS tu = getFirstDecodeUnit();
        TIME_UNITS tud = null;   // Contiendra la dernière unité précisée p.ex. 2m30 => MIN; 2h30s => SEC
        boolean ret = true;
        try {
            int k = -1;   //  Séparateur de l'unité précédente
            do {   //  Attribuer tout ce qui est posible aux unités précisées
                tu.setTag(0);
                int i = str.indexOf(tu.separatorDL);  //  Séparateur DL n'est jamais vide => i<>0
                if (i != NOT_FOUND) {  //  Séparateur trouvé
                    tu.setTag(Long.parseLong(str.substring(k + 1, i)));   //  Après le séparateur de l'unité précédente et avant le séparateur de l'unité en cours
                    k = i;
                    tud = tu;   //  Dernière unité précisée rencontrée
                }
                tu = tu.getNextDecodeUnit();
            } while (tu != null);
            if (k != (str.length() - 1)) {   //  Dernières unités non précisées  (p.ex. 14m2s26 => TS=2 HS=6)
                if (tud != null) {  //  Dernière unité précisée rencontrée
                    TIME_UNITS tun = tud.getNextDecodeUnit();  //  la 1e unité parmi les unités non précisées
                    str = str.substring(k + 1);   //  Après la dernière unité précisée
                    if (tun != null) {
                        if (tun.separatorD.length() == 0) {   //  concerne TS, HS, MS => Il reste <TS> ou <TS><HS> ou <TS><HS><MS> à attribuer  (p.ex. 3s45 => TS=4 HS=5)
                            do {
                                tun.setTag(Long.parseLong(str.substring(0, 1)));   //  Un seul caractère par unité
                                str = str.substring(1);
                                tun = tun.nextDecodeUnit;
                            }
                            while ((tun != null) && (str.length() != 0));
                        } else {   //  concerne MIN, SEC   (p.ex. 2h50 => MIN = 50 ou 2h14m56 => SEC = 56)
                            tun.setTag(Long.parseLong(str));   //  attribution du tout à cette unité
                        }
                    } else {    //  tun=null => concerne MS => Que faire ??? ( p.ex 7h4v15 ???)
                        ret = false;   //  Désolé
                    }
                    tun = null;
                } else {  // Aucune unité précisée => HOUR
                    TIME_UNITS.HOUR.setTag(Long.parseLong(str));
                }
            }
        } catch (NumberFormatException ex) {
            ret = false;
        }
        tud = null;
        return ret;
    }

    private static TIME_UNITS getFirstDecodeUnit() {  //  1e unité à décoder
        return TIME_UNITS.HOUR;
    }

}
