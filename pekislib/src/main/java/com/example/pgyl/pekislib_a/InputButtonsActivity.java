package com.example.pgyl.pekislib_a;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.example.pgyl.pekislib_a.Constants.ACTIVITY_EXTRA_KEYS;
import static com.example.pgyl.pekislib_a.Constants.ERROR_VALUE;
import static com.example.pgyl.pekislib_a.Constants.HEX_RADIX;
import static com.example.pgyl.pekislib_a.Constants.NOT_FOUND;
import static com.example.pgyl.pekislib_a.Constants.SHP_FILE_NAME_SUFFIX;
import static com.example.pgyl.pekislib_a.HelpActivity.HELP_ACTIVITY_EXTRA_KEYS;
import static com.example.pgyl.pekislib_a.HelpActivity.HELP_ACTIVITY_TITLE;
import static com.example.pgyl.pekislib_a.MiscUtils.msgBox;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseTables.ACTIVITY_START_STATUS;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseTables.TABLE_EXTRA_KEYS;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.getCurrentValueInInputButtonsActivity;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.getKeyboard;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.getMax;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.getMin;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.getRegExp;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.getTimeUnit;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.isColdStartStatusInInputButtonsActivity;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.setCurrentValueInInputButtonsActivity;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.setStartStatusInInputButtonsActivity;
import static com.example.pgyl.pekislib_a.TimeDateUtils.TIME_UNITS;
import static com.example.pgyl.pekislib_a.TimeDateUtils.ddMMyyyy;
import static com.example.pgyl.pekislib_a.TimeDateUtils.formattedStringTimeDate;
import static com.example.pgyl.pekislib_a.TimeDateUtils.msToTimeFormatD;
import static com.example.pgyl.pekislib_a.TimeDateUtils.msToTimeFormatDL;
import static com.example.pgyl.pekislib_a.TimeDateUtils.timeFormatDLToMs;
import static com.example.pgyl.pekislib_a.TimeDateUtils.timeFormatDToMs;

public class InputButtonsActivity extends Activity {
    //region Constantes
    public enum KEYBOARDS {
        ALPHANUM(
                new String[]{   //  Portrait
                        "a", "b", "c", SPECIAL_BUTTONS.BACK.toString(),
                        "d", "e", "f", SPECIAL_BUTTONS.CLEAR.toString(),
                        "g", "h", "i", SPECIAL_BUTTONS.CASE.toString(),
                        "j", "k", "l", SPECIAL_BUTTONS.NEXTP.toString(),
                        "m", "n", "o", "p",
                        "q", "r", "s", "t",
                        "u", "v", "w", "x",
                        "y", "z", ".", " ",

                        "1", "2", "3", SPECIAL_BUTTONS.BACK.toString(),
                        "4", "5", "6", SPECIAL_BUTTONS.CLEAR.toString(),
                        "7", "8", "9", SPECIAL_BUTTONS.CASE.toString(),
                        "#", "0", ".", SPECIAL_BUTTONS.NEXTP.toString(),
                        "+", "-", "*", "/",
                        "(", ")", "[", "]",
                        "{", "}", "<", ">",
                        "=", "$", "£", "@",

                        "&", "§", "~", SPECIAL_BUTTONS.BACK.toString(),
                        "?", "!", "|", SPECIAL_BUTTONS.CLEAR.toString(),
                        "\\", "_", "^", SPECIAL_BUTTONS.CASE.toString(),
                        "\"", "'", "`", SPECIAL_BUTTONS.NEXTP.toString(),
                        ".", ",", ";", ":",
                        "%", NA, NA, NA,
                        NA, NA, NA, NA,
                        NA, NA, NA, NA,

                        "à", "á", "â", SPECIAL_BUTTONS.BACK.toString(),
                        "ã", "ä", "å", SPECIAL_BUTTONS.CLEAR.toString(),
                        "ò", "ó", "ô", SPECIAL_BUTTONS.CASE.toString(),
                        "õ", "ö", "ø", SPECIAL_BUTTONS.NEXTP.toString(),
                        "è", "é", "ê", "ë",
                        "ì", "í", "î", "ï",
                        "ù", "ú", "û", "ü",
                        "ÿ", "ñ", "ç", NA},

                new String[]{   //  Paysage
                        "a", "b", "c", "d", "e", "f", "g", SPECIAL_BUTTONS.BACK.toString(),
                        "h", "i", "j", "k", "l", "m", "n", SPECIAL_BUTTONS.CLEAR.toString(),
                        "o", "p", "q", "r", "s", "t", "u", SPECIAL_BUTTONS.CASE.toString(),
                        "v", "w", "x", "y", "z", ".", " ", SPECIAL_BUTTONS.NEXTP.toString(),

                        "1", "2", "3", "+", "-", "*", "/", SPECIAL_BUTTONS.BACK.toString(),
                        "4", "5", "6", "(", ")", "[", "]", SPECIAL_BUTTONS.CLEAR.toString(),
                        "7", "8", "9", "{", "}", "<", ">", SPECIAL_BUTTONS.CASE.toString(),
                        "#", "0", ".", "=", "$", "£", "@", SPECIAL_BUTTONS.NEXTP.toString(),

                        "&", "§", "~", "?", "!", "|", "\\", SPECIAL_BUTTONS.BACK.toString(),
                        "_", "^", "\"", "'", "`", ".", ",", SPECIAL_BUTTONS.CLEAR.toString(),
                        ";", ":", NA, NA, NA, NA, NA, SPECIAL_BUTTONS.CASE.toString(),
                        NA, NA, NA, NA, NA, NA, NA, SPECIAL_BUTTONS.NEXTP.toString(),

                        "à", "á", "â", "ã", "ä", "å", "ò", SPECIAL_BUTTONS.BACK.toString(),
                        "ó", "ô", "õ", "ö", "ø", "è", "é", SPECIAL_BUTTONS.CLEAR.toString(),
                        "ê", "ë", "ì", "í", "î", "ï", "ù", SPECIAL_BUTTONS.CASE.toString(),
                        "ú", "û", "ü", "ÿ", "ñ", "ç", NA, SPECIAL_BUTTONS.NEXTP.toString()}),

        ASCII(
                new String[]{   //  Portrait
                        "a", "b", "c", SPECIAL_BUTTONS.BACK.toString(),
                        "d", "e", "f", SPECIAL_BUTTONS.CLEAR.toString(),
                        "g", "h", "i", SPECIAL_BUTTONS.CASE.toString(),
                        "j", "k", "l", SPECIAL_BUTTONS.NEXTP.toString(),
                        "m", "n", "o", "p",
                        "q", "r", "s", "t",
                        "u", "v", "w", "x",
                        "y", "z", ".", " ",

                        "1", "2", "3", SPECIAL_BUTTONS.BACK.toString(),
                        "4", "5", "6", SPECIAL_BUTTONS.CLEAR.toString(),
                        "7", "8", "9", SPECIAL_BUTTONS.CASE.toString(),
                        "#", "0", ".", SPECIAL_BUTTONS.NEXTP.toString(),
                        "+", "-", "*", "/",
                        "(", ")", "[", "]",
                        "{", "}", "<", ">",
                        "=", "$", "@", NA,

                        "&", "%", "~", SPECIAL_BUTTONS.BACK.toString(),
                        "?", "!", "|", SPECIAL_BUTTONS.CLEAR.toString(),
                        "\\", "_", "^", SPECIAL_BUTTONS.CASE.toString(),
                        "\"", "'", "`", SPECIAL_BUTTONS.NEXTP.toString(),
                        ".", ",", ";", ":",
                        NA, NA, NA, NA,
                        NA, NA, NA, NA,
                        NA, NA, NA, NA},

                new String[]{   //  Paysage
                        "a", "b", "c", "d", "e", "f", "g", SPECIAL_BUTTONS.BACK.toString(),
                        "h", "i", "j", "k", "l", "m", "n", SPECIAL_BUTTONS.CLEAR.toString(),
                        "o", "p", "q", "r", "s", "t", "u", SPECIAL_BUTTONS.CASE.toString(),
                        "v", "w", "x", "y", "z", ".", " ", SPECIAL_BUTTONS.NEXTP.toString(),

                        "1", "2", "3", "+", "-", "*", "/", SPECIAL_BUTTONS.BACK.toString(),
                        "4", "5", "6", "(", ")", "[", "]", SPECIAL_BUTTONS.CLEAR.toString(),
                        "7", "8", "9", "{", "}", "<", ">", SPECIAL_BUTTONS.CASE.toString(),
                        "#", "0", ".", "=", "$", "@", NA, SPECIAL_BUTTONS.NEXTP.toString(),

                        "&", "%", "~", "?", "!", "|", "\\", SPECIAL_BUTTONS.BACK.toString(),
                        "_", "^", "\"", "'", "`", ".", ",", SPECIAL_BUTTONS.CLEAR.toString(),
                        ";", ":", NA, NA, NA, NA, NA, SPECIAL_BUTTONS.CASE.toString(),
                        NA, NA, NA, NA, NA, NA, NA, SPECIAL_BUTTONS.NEXTP.toString()}),

        TIME_FORMAT_D(
                new String[]{   //  Portrait
                        "1", "2", "3", SPECIAL_BUTTONS.BACK.toString(),
                        "4", "5", "6", SPECIAL_BUTTONS.CLEAR.toString(),
                        "7", "8", "9", ":", NA, "0", NA, "."},

                new String[]{   //  Paysage
                        "1", "2", "3", ":", ".", NA, NA, SPECIAL_BUTTONS.BACK.toString(),
                        "4", "5", "6", NA, NA, NA, NA, SPECIAL_BUTTONS.CLEAR.toString(),
                        "7", "8", "9", NA, NA, NA, NA, NA,
                        NA, "0", NA, NA, NA, NA, NA, NA}),

        TIME_FORMAT_DL(
                new String[]{   //  Portrait
                        "1", "2", "3", SPECIAL_BUTTONS.BACK.toString(),
                        "4", "5", "6", SPECIAL_BUTTONS.CLEAR.toString(),
                        "7", "8", "9", NA,
                        NA, "0", NA, NA,
                        "h", "m", "s", NA,
                        "t", "u", "v", NA},
                new String[]{   //  Paysage
                        "1", "2", "3", "h", "m", "s", NA, SPECIAL_BUTTONS.BACK.toString(),
                        "4", "5", "6", "t", "u", "v", NA, SPECIAL_BUTTONS.CLEAR.toString(),
                        "7", "8", "9", NA, NA, NA, NA, NA,
                        NA, "0", NA, NA, NA, NA, NA, NA}),

        DATE_JJMMAAAA(
                new String[]{   //  Portrait
                        "1", "2", "3", SPECIAL_BUTTONS.BACK.toString(),
                        "4", "5", "6", SPECIAL_BUTTONS.CLEAR.toString(),
                        "7", "8", "9", NA,
                        NA, "0", "/", NA},

                new String[]{   //  Paysage
                        "1", "2", "3", NA, NA, NA, NA, SPECIAL_BUTTONS.BACK.toString(),
                        "4", "5", "6", NA, NA, NA, NA, SPECIAL_BUTTONS.CLEAR.toString(),
                        "7", "8", "9", NA, NA, NA, NA, NA,
                        NA, "0", "/", NA, NA, NA, NA, NA}),

        POSINT(
                new String[]{   //  Portrait
                        "1", "2", "3", SPECIAL_BUTTONS.BACK.toString(),
                        "4", "5", "6", SPECIAL_BUTTONS.CLEAR.toString(),
                        "7", "8", "9", NA,
                        NA, "0", NA, NA},

                new String[]{   //  Paysage
                        "1", "2", "3", NA, NA, NA, NA, SPECIAL_BUTTONS.BACK.toString(),
                        "4", "5", "6", NA, NA, NA, NA, SPECIAL_BUTTONS.CLEAR.toString(),
                        "7", "8", "9", NA, NA, NA, NA, NA,
                        NA, "0", NA, NA, NA, NA, NA, NA}),

        LONG(
                new String[]{   //  Portrait
                        "1", "2", "3", SPECIAL_BUTTONS.BACK.toString(),
                        "4", "5", "6", SPECIAL_BUTTONS.CLEAR.toString(),
                        "7", "8", "9", NA,
                        "-", "0", NA, NA},

                new String[]{   //  Paysage
                        "1", "2", "3", NA, NA, NA, NA, SPECIAL_BUTTONS.BACK.toString(),
                        "4", "5", "6", NA, NA, NA, NA, SPECIAL_BUTTONS.CLEAR.toString(),
                        "7", "8", "9", NA, NA, NA, NA, NA,
                        "-", "0", NA, NA, NA, NA, NA, NA}),

        FLOAT(
                new String[]{   //  Portrait
                        "1", "2", "3", SPECIAL_BUTTONS.BACK.toString(),
                        "4", "5", "6", SPECIAL_BUTTONS.CLEAR.toString(),
                        "7", "8", "9", NA,
                        "-", "0", ".", NA},

                new String[]{   //  Paysage
                        "1", "2", "3", NA, NA, NA, NA, SPECIAL_BUTTONS.BACK.toString(),
                        "4", "5", "6", NA, NA, NA, NA, SPECIAL_BUTTONS.CLEAR.toString(),
                        "7", "8", "9", NA, NA, NA, NA, NA,
                        "-", "0", ".", NA, NA, NA, NA, NA}),

        HEX(
                new String[]{   //  Portrait
                        "1", "2", "3", SPECIAL_BUTTONS.BACK.toString(),
                        "4", "5", "6", SPECIAL_BUTTONS.CLEAR.toString(),
                        "7", "8", "9", NA,
                        NA, "0", NA, NA,
                        "A", "B", "C", NA,
                        "D", "E", "F", NA},

                new String[]{   //  Paysage
                        "1", "2", "3", "A", "B", "C", NA, SPECIAL_BUTTONS.BACK.toString(),
                        "4", "5", "6", "D", "E", "F", NA, SPECIAL_BUTTONS.CLEAR.toString(),
                        "7", "8", "9", NA, NA, NA, NA, NA,
                        NA, "0", NA, NA, NA, NA, NA, NA});

        private String[] valuePortraitButtonTexts;
        private String[] valueLandscapeButtonTexts;

        KEYBOARDS(String[] valuePortraitButtonTexts, String[] valueLandscapeButtonTexts) {
            this.valuePortraitButtonTexts = valuePortraitButtonTexts;
            this.valueLandscapeButtonTexts = valueLandscapeButtonTexts;
        }

        public String[] PORTRAIT_BUTTON_TEXTS() {
            return valuePortraitButtonTexts;
        }

        public String[] LANDSCAPE_BUTTON_TEXTS() {
            return valueLandscapeButtonTexts;
        }
    }

    private enum COMMANDS {
        CANCEL("Cancel"), OK("OK");

        private String valueText;

        COMMANDS(String valueText) {
            this.valueText = valueText;
        }

        public String TEXT() {
            return valueText;
        }

        public int INDEX() {
            return ordinal();
        }
    }

    private enum SPECIAL_BUTTONS {
        CASE("Aa"), NEXTP("=>"), BACK("<"), CLEAR("<<");

        private String valueText;

        SPECIAL_BUTTONS(String valueText) {
            this.valueText = valueText;
        }

        public String REPLACEMENT_TEXT() {
            return valueText;
        }
    }

    private enum CASES {
        NO_CASE, UPPER_CASE, LOWER_CASE;

        public static CASES nextCase(CASES caze) {    //  NO_CASE->UPPER_CASE->LOWER_CASE->UPPER_CASE->LOWER_CASE...
            switch (caze) {
                case NO_CASE:
                    return UPPER_CASE;
                case UPPER_CASE:
                    return LOWER_CASE;
                case LOWER_CASE:
                    return UPPER_CASE;
                default:
                    return UPPER_CASE;
            }
        }
    }

    private enum SHP_KEY_NAMES {
        CURRENT_PAGE_INDEX, CURRENT_CASE, APPEND
    }

    private final int BUTTONS_PER_PAGE = 32;      //  Nombre maximum de boutons d'input par page (8x4 ou 4x8)
    private static final String NA = "NA";        //  Emplacement occupé mais invisible
    //endregion
    //region Variables
    private String[] buttonTexts;
    private String[][] pageButtonTexts;
    private String editString;
    private int pages;
    private int pageIndex;
    private CASES caze;
    private TIME_UNITS timeUnit;
    private boolean append;             //  True si les caractères entrés s'ajoutent à la chaîne d'entrée
    private String tableName;
    private int columnIndex;
    private KEYBOARDS keyboard;
    private Button[] keyboardButtons;
    private Button[] buttons;
    private TextView lbldisplay;
    private StringShelfDatabase stringShelfDatabase;
    private String shpFileName;
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().setTitle(getIntent().getStringExtra(ACTIVITY_EXTRA_KEYS.TITLE.toString()));
        setupOrientationLayout();
        setupButtons();
        setupKeyboardButtons();
    }

    @Override
    protected void onPause() {
        super.onPause();

        savePreferences();
        setCurrentValueInInputButtonsActivity(stringShelfDatabase, tableName, columnIndex, editString);
        stringShelfDatabase.close();
        stringShelfDatabase = null;
    }

    @Override
    protected void onResume() {
        final int COLUMN_INDEX_DEFAULT_VALUE = NOT_FOUND;
        final int CURRENT_PAGE_INDEX_DEFAULT_VALUE = 0;
        final boolean APPEND_DEFAULT_VALUE = false;

        super.onResume();

        shpFileName = getPackageName() + "." + getClass().getSimpleName() + SHP_FILE_NAME_SUFFIX;
        tableName = getIntent().getStringExtra(TABLE_EXTRA_KEYS.TABLE.toString());
        columnIndex = getIntent().getIntExtra(TABLE_EXTRA_KEYS.INDEX.toString(), COLUMN_INDEX_DEFAULT_VALUE);
        setupStringShelfDatabase();
        editString = getCurrentValueInInputButtonsActivity(stringShelfDatabase, tableName, columnIndex);
        keyboard = KEYBOARDS.valueOf(getKeyboard(stringShelfDatabase, tableName, columnIndex));
        if ((keyboard.equals(KEYBOARDS.TIME_FORMAT_D)) || (keyboard.equals(KEYBOARDS.TIME_FORMAT_DL))) {
            timeUnit = TIME_UNITS.valueOf(getTimeUnit(stringShelfDatabase, tableName, columnIndex));
        }
        buttonTexts = getButtonTexts(keyboard);
        pages = ((buttonTexts.length - 1) / BUTTONS_PER_PAGE) + 1;
        pageButtonTexts = getPageButtonTexts(buttonTexts, BUTTONS_PER_PAGE, pages);

        if (isColdStartStatusInInputButtonsActivity(stringShelfDatabase)) {
            setStartStatusInInputButtonsActivity(stringShelfDatabase, ACTIVITY_START_STATUS.HOT);
            pageIndex = CURRENT_PAGE_INDEX_DEFAULT_VALUE;
            caze = CASES.NO_CASE;
            append = APPEND_DEFAULT_VALUE;
            if (keyboard.equals(KEYBOARDS.TIME_FORMAT_D)) {
                editString = msToTimeFormatD(Long.parseLong(editString), timeUnit);
            }
            if (keyboard.equals(KEYBOARDS.TIME_FORMAT_DL)) {
                editString = msToTimeFormatDL(Long.parseLong(editString), timeUnit);
            }
        } else {
            pageIndex = getSHPcurrentPageIndex();
            caze = getSHPcurrentCase();
            append = getSHPappend();
        }
        lbldisplay.setText(editString);
        updateDisplayButtonTexts();
        setupTextSizes();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_help_only, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.HELP) {
            launchHelpActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onButtonClick(COMMANDS command) {
        if (command.equals(COMMANDS.CANCEL)) {
            onButtonClickCancel();
        }
        if (command.equals(COMMANDS.OK)) {
            onButtonClickOK();
        }
    }

    private void onButtonClickOK() {
        String candidate = editString;
        String smin = getMin(stringShelfDatabase, tableName, columnIndex);
        String smax = getMax(stringShelfDatabase, tableName, columnIndex);
        String regexp = getRegExp(stringShelfDatabase, tableName, columnIndex);
        String error = noErrorMessage();
        if (regexp != null) {
            if (!candidate.matches(regexp)) {
                error = errorMessageRegexp(regexp);
            }
        }
        if (error.equals(noErrorMessage())) {
            if (smin == null) {
                smin = candidate;
                if (keyboard.equals(KEYBOARDS.TIME_FORMAT_D)) {
                    smin = String.valueOf(timeFormatDToMs(candidate));
                }
                if (keyboard.equals(KEYBOARDS.TIME_FORMAT_DL)) {
                    smin = String.valueOf(timeFormatDLToMs(candidate));
                }
            }
            if (smax == null) {
                smax = candidate;
                if (keyboard.equals(KEYBOARDS.TIME_FORMAT_D)) {
                    smax = String.valueOf(timeFormatDToMs(candidate));
                }
                if (keyboard.equals(KEYBOARDS.TIME_FORMAT_DL)) {
                    smax = String.valueOf(timeFormatDLToMs(candidate));
                }
            }
            error = parseCandidate(candidate, smin, smax);
        }
        if (error.equals(noErrorMessage())) {  //  Good guy
            editString = normalizedCandidate(candidate);
            Intent returnIntent = new Intent();
            setResult(RESULT_OK, returnIntent);
            finish();
        } else {  //  Bad guy
            msgBox(error, this);
            append = false;
        }
    }

    private void onButtonClickCancel() {
        finish();
    }

    private void onKeyboardButtonClick(int index) {
        final String TEXT_EMPTY = "";
        String newString;

        String buttonText = pageButtonTexts[pageIndex][index];
        if ((buttonText.equals(SPECIAL_BUTTONS.CASE.toString())) || (buttonText.equals(SPECIAL_BUTTONS.NEXTP.toString()))) {
            if (buttonText.equals(SPECIAL_BUTTONS.CASE.toString())) {
                caze = CASES.nextCase(caze);
            }
            if (buttonText.equals(SPECIAL_BUTTONS.NEXTP.toString())) {
                pageIndex = (pageIndex + 1) % pages;
            }
            updateDisplayButtonTexts();
        } else {
            String curString = editString;
            if (buttonText.equals(SPECIAL_BUTTONS.BACK.toString())) {
                if (curString.length() >= 1) {
                    newString = curString.substring(0, curString.length() - 1);
                } else {
                    newString = TEXT_EMPTY;
                }
            } else {
                if (buttonText.equals(SPECIAL_BUTTONS.CLEAR.toString())) {
                    newString = TEXT_EMPTY;
                } else {
                    newString = currentCase(buttonText);
                    if (append) {
                        newString = curString + newString;
                    }
                }
            }
            editString = newString;
            lbldisplay.setText(newString);
            append = true;  // Dorénavant toujours ajouter les caractères à la chaîne d'entrée
        }
    }

    private void updateDisplayButtonTexts() {
        int lastPageMaxIndex = ((buttonTexts.length - 1) % BUTTONS_PER_PAGE);
        for (int i = 0; i <= (BUTTONS_PER_PAGE - 1); i = i + 1) { // Affectation des textes, styles
            if ((pageIndex < (pages - 1)) || ((pageIndex == (pages - 1)) && (i <= lastPageMaxIndex))) {  // Dernière page éventuellement incomplète
                String buttonText = pageButtonTexts[pageIndex][i];
                if (!buttonText.equals(NA)) { // Pas NA => Bouton à afficher
                    String newButtonText = currentCase(buttonText);
                    if ((buttonText.equals(SPECIAL_BUTTONS.BACK.toString())) || (buttonText.equals(SPECIAL_BUTTONS.CLEAR.toString())) || (buttonText.equals(SPECIAL_BUTTONS.CASE.toString())) || (buttonText.equals(SPECIAL_BUTTONS.NEXTP.toString()))) {
                        keyboardButtons[i].setTypeface(null, Typeface.BOLD);
                        if ((buttonText.equals(SPECIAL_BUTTONS.BACK.toString())) || (buttonText.equals(SPECIAL_BUTTONS.CLEAR.toString()))) {
                            keyboardButtons[i].setTextColor(Color.RED);
                            if (buttonText.equals(SPECIAL_BUTTONS.BACK.toString())) {
                                newButtonText = SPECIAL_BUTTONS.BACK.REPLACEMENT_TEXT();
                            } else {  //  CLEAR
                                newButtonText = SPECIAL_BUTTONS.CLEAR.REPLACEMENT_TEXT();
                            }
                        }
                        if ((buttonText.equals(SPECIAL_BUTTONS.CASE.toString())) || (buttonText.equals(SPECIAL_BUTTONS.NEXTP.toString()))) {
                            keyboardButtons[i].setTextColor(Color.BLUE);
                            if (buttonText.equals(SPECIAL_BUTTONS.CASE.toString())) {
                                newButtonText = SPECIAL_BUTTONS.CASE.REPLACEMENT_TEXT();
                            } else {  //  NEXTP
                                newButtonText = SPECIAL_BUTTONS.NEXTP.REPLACEMENT_TEXT();
                            }
                        }
                    }
                    keyboardButtons[i].setText(newButtonText);
                    keyboardButtons[i].setVisibility(VISIBLE);
                } else {
                    keyboardButtons[i].setVisibility(INVISIBLE);
                }
            } else { // Dernière page incomplète => Emplacements invisibles
                keyboardButtons[i].setVisibility(INVISIBLE);
            }
        }
    }

    public void updateDisplayResizeLbldisplayText() {
        final double DISPLAY_RELATIVE_TEXT_SIZE = 0.5;  // Ratio souhaité entre textSize de lbldisplay et sa hauteur

        float ts = (float) (DISPLAY_RELATIVE_TEXT_SIZE * lbldisplay.getHeight());
        lbldisplay.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts);
    }

    public void updateDisplayResizeButtonText(Button button) {
        final double BUTTON_RELATIVE_TEXT_SIZE = 0.4;   //  Ratio souhaité entre textSize d'un bouton et sa hauteur

        float ts = (float) (BUTTON_RELATIVE_TEXT_SIZE * button.getHeight());
        button.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts);
    }

    private String currentCase(String str) {  //  Transforme la chaîne selon la casse courante
        String ret = str;
        if (caze.equals(CASES.UPPER_CASE)) {
            ret = str.toUpperCase(Locale.ENGLISH);
        }
        if (caze.equals(CASES.LOWER_CASE)) {
            ret = str.toLowerCase(Locale.ENGLISH);
        }
        return ret;
    }

    private String parseCandidate(String candidate, String smin, String smax) {
        String ret = noErrorMessage();
        if (candidate.length() >= 1) {
            if ((keyboard.equals(KEYBOARDS.ALPHANUM)) || (keyboard.equals(KEYBOARDS.ASCII))) {
                ret = parseAlphanum(candidate, smin, smax);
            }
            if (keyboard.equals(KEYBOARDS.TIME_FORMAT_D)) {
                ret = parseTimeFormatD(candidate, smin, smax);
            }
            if (keyboard.equals(KEYBOARDS.TIME_FORMAT_DL)) {
                ret = parseTimeFormatDL(candidate, smin, smax);
            }
            if (keyboard.equals(KEYBOARDS.DATE_JJMMAAAA)) {
                ret = parseDATEJJMMAAAA(candidate, smin, smax);
            }
            if (keyboard.equals(KEYBOARDS.POSINT)) {
                ret = parseInteger(candidate, smin, smax);
            }
            if (keyboard.equals(KEYBOARDS.LONG)) {
                ret = parseLong(candidate, smin, smax);
            }
            if (keyboard.equals(KEYBOARDS.FLOAT)) {
                ret = parseFloat(candidate, smin, smax);
            }
            if (keyboard.equals(KEYBOARDS.HEX)) {
                ret = parseHex(candidate, smin, smax);
            }
        }
        return ret;
    }

    private String normalizedCandidate(String candidate) {
        String ret = candidate;
        if (candidate.length() >= 1) {
            if (keyboard.equals(KEYBOARDS.TIME_FORMAT_D)) {
                ret = String.valueOf(timeFormatDToMs(candidate));
            }
            if (keyboard.equals(KEYBOARDS.TIME_FORMAT_DL)) {
                ret = String.valueOf(timeFormatDLToMs(candidate));
            }
            if (keyboard.equals(KEYBOARDS.DATE_JJMMAAAA)) {
                ret = formattedStringTimeDate(candidate, ddMMyyyy);
            }
        }
        return ret;
    }

    private String noErrorMessage() {
        return "OK";
    }

    private String errorMessageRegexp(String regexp) {
        return "Error: Must match " + regexp;
    }

    private String errorMessageParse(KEYBOARDS keyboard) {
        return "Error: Must parse " + keyboard.toString();
    }

    private String errorMessageMin(String smin) {
        return "Error: Minimum " + smin;
    }

    private String errorMessageMax(String smax) {
        return "Error: Maximum " + smax;
    }

    private String parseAlphanum(String sed, String smin, String smax) {
        String ret = noErrorMessage();
        if (sed.compareTo(smin) < 0) {
            ret = errorMessageMin(smin);
        }
        if (sed.compareTo(smax) > 0) {
            ret = errorMessageMax(smax);
        }
        return ret;
    }

    private String parseTimeFormatD(String sed, String smin, String smax) {
        String ret = noErrorMessage();
        long ms = timeFormatDToMs(sed);
        if (ms != ERROR_VALUE) {
            if (ms < Long.parseLong(smin)) {
                ret = errorMessageMin(msToTimeFormatD(Long.parseLong(smin), timeUnit));
            }
            if (ms > Long.parseLong(smax)) {
                ret = errorMessageMax(msToTimeFormatD(Long.parseLong(smax), timeUnit));
            }
        } else {
            ret = errorMessageParse(KEYBOARDS.TIME_FORMAT_D);
        }
        return ret;
    }

    private String parseTimeFormatDL(String sed, String smin, String smax) {
        String ret = noErrorMessage();
        long ms = timeFormatDLToMs(sed);
        if (ms != ERROR_VALUE) {
            if (ms < Long.parseLong(smin)) {
                ret = errorMessageMin(msToTimeFormatDL(Long.parseLong(smin), timeUnit));
            }
            if (ms > Long.parseLong(smax)) {
                ret = errorMessageMax(msToTimeFormatDL(Long.parseLong(smax), timeUnit));
            }
        } else {
            ret = errorMessageParse(KEYBOARDS.TIME_FORMAT_DL);
        }
        return ret;
    }

    private String parseDATEJJMMAAAA(String sed, String smin, String smax) {
        String ret = noErrorMessage();
        try {
            Date d = ddMMyyyy.parse(sed);
            if (d.compareTo(ddMMyyyy.parse(smin)) < 0) {   //  Date inférieure au minimum
                ret = errorMessageMin(smin);
            }
            if (d.compareTo(ddMMyyyy.parse(smax)) > 0) {   //  Date supérieure au maximum
                ret = errorMessageMax(smax);
            }
        } catch (ParseException ex) {
            ret = errorMessageParse(KEYBOARDS.DATE_JJMMAAAA);
        }
        return ret;
    }

    private String parseInteger(String sed, String smin, String smax) {
        String ret = noErrorMessage();
        try {
            int l = Integer.parseInt(sed);
            if (l < Integer.parseInt(smin)) {
                ret = errorMessageMin(smin);
            }
            if (l > Integer.parseInt(smax)) {
                ret = errorMessageMax(smax);
            }
        } catch (NumberFormatException ex) {
            ret = errorMessageParse(KEYBOARDS.POSINT);
        }
        return ret;
    }

    private String parseLong(String sed, String smin, String smax) {
        String ret = noErrorMessage();
        try {
            long l = Long.parseLong(sed);
            if (l < Long.parseLong(smin)) {
                ret = errorMessageMin(smin);
            }
            if (l > Long.parseLong(smax)) {
                ret = errorMessageMax(smax);
            }
        } catch (NumberFormatException ex) {
            ret = errorMessageParse(KEYBOARDS.LONG);
        }
        return ret;
    }

    private String parseFloat(String sed, String smin, String smax) {
        String ret = noErrorMessage();
        try {
            float f = Float.parseFloat(sed);
            if (f < Float.parseFloat(smin)) {
                ret = errorMessageMin(smin);
            }
            if (f > Float.parseFloat(smax)) {
                ret = errorMessageMax(smax);
            }
        } catch (NumberFormatException ex) {
            ret = errorMessageParse(KEYBOARDS.FLOAT);
        }
        return ret;
    }

    private String parseHex(String sed, String smin, String smax) {
        String ret = noErrorMessage();
        try {
            long l = Long.parseLong(sed, HEX_RADIX);
            if (l < Long.parseLong(smin, HEX_RADIX)) {
                ret = errorMessageMin(smin);
            }
            if (l > Long.parseLong(smax, HEX_RADIX)) {
                ret = errorMessageMax(smax);
            }
        } catch (NumberFormatException ex) {
            ret = errorMessageParse(KEYBOARDS.HEX);
        }
        return ret;
    }

    private String[][] getPageButtonTexts(String[] buttonTexts, int buttonsPerPage, int pages) {
        String[][] ret = new String[pages][buttonsPerPage];
        for (int i = 0; i <= (buttonTexts.length - 1); i = i + 1) {
            int pageNumber = (i / buttonsPerPage);
            int pageButtonIndex = (i % buttonsPerPage);
            ret[pageNumber][pageButtonIndex] = buttonTexts[i];
        }
        return ret;
    }

    private String[] getButtonTexts(KEYBOARDS keyboard) {
        return ((getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) ? keyboard.PORTRAIT_BUTTON_TEXTS() : keyboard.LANDSCAPE_BUTTON_TEXTS());
    }

    private int getSHPcurrentPageIndex() {
        SharedPreferences shp = getSharedPreferences(shpFileName, MODE_PRIVATE);
        return shp.getInt(SHP_KEY_NAMES.CURRENT_PAGE_INDEX.toString(), 0);
    }

    private CASES getSHPcurrentCase() {
        SharedPreferences shp = getSharedPreferences(shpFileName, MODE_PRIVATE);
        return CASES.valueOf(shp.getString(SHP_KEY_NAMES.CURRENT_CASE.toString(), null));
    }

    private boolean getSHPappend() {
        SharedPreferences shp = getSharedPreferences(shpFileName, MODE_PRIVATE);
        return shp.getBoolean(SHP_KEY_NAMES.APPEND.toString(), false);
    }

    private void savePreferences() {
        SharedPreferences shp = getSharedPreferences(shpFileName, MODE_PRIVATE);
        SharedPreferences.Editor shpEditor = shp.edit();
        shpEditor.putInt(SHP_KEY_NAMES.CURRENT_PAGE_INDEX.toString(), pageIndex);
        shpEditor.putString(SHP_KEY_NAMES.CURRENT_CASE.toString(), caze.toString());
        shpEditor.putBoolean(SHP_KEY_NAMES.APPEND.toString(), append);
        shpEditor.commit();
    }

    private void setupOrientationLayout() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.inputbuttons_p);
        } else {
            setContentView(R.layout.inputbuttons_l);
        }
    }

    private void setupButtons() {
        final String BUTTON_XML_PREFIX = "BTN_";

        buttons = new Button[COMMANDS.values().length];
        Class rid = R.id.class;
        for (COMMANDS command : COMMANDS.values()) {
            try {
                buttons[command.INDEX()] = findViewById(rid.getField(BUTTON_XML_PREFIX + command.toString()).getInt(rid));   //  1, 2, 3 ... dans le XML
                buttons[command.INDEX()].setText(command.TEXT());
                final COMMANDS fcommand = command;
                buttons[command.INDEX()].setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onButtonClick(fcommand);
                    }
                });
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void setupKeyboardButtons() {
        final String BUTTON_XML_PREFIX = "BTN_P";

        Class rid = R.id.class;
        lbldisplay = findViewById(R.id.LBL_DISPLAY);
        keyboardButtons = new Button[BUTTONS_PER_PAGE];
        for (int i = 0; i <= (BUTTONS_PER_PAGE - 1); i = i + 1) {
            try {
                keyboardButtons[i] = findViewById(rid.getField(BUTTON_XML_PREFIX + (i + 1)).getInt(rid));  // BTN_P1, BTN_P2, ...
                final int index = i;
                keyboardButtons[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onKeyboardButtonClick(index);
                    }
                });
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
                Logger.getLogger(InputButtonsActivity.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void setupStringShelfDatabase() {
        stringShelfDatabase = new StringShelfDatabase(this);
        stringShelfDatabase.open();
    }

    private void setupTextSizes() {
        lbldisplay.post(new Runnable() {   // UI Dimensions sont alors seulement connues
            @Override
            public void run() {
                updateDisplayResizeLbldisplayText();
            }
        });
        for (int i = 0; i <= (BUTTONS_PER_PAGE - 1); i = i + 1) {
            final int q = i;
            keyboardButtons[q].post(new Runnable() {
                @Override
                public void run() {
                    updateDisplayResizeButtonText(keyboardButtons[q]);
                }
            });
        }
    }

    private void launchHelpActivity() {
        Intent callingIntent = new Intent(this, HelpActivity.class);
        callingIntent.putExtra(ACTIVITY_EXTRA_KEYS.TITLE.toString(), HELP_ACTIVITY_TITLE);
        callingIntent.putExtra(HELP_ACTIVITY_EXTRA_KEYS.HTML_ID.toString(), R.raw.helpinputbuttonsactivity);
        startActivity(callingIntent);
    }

}
