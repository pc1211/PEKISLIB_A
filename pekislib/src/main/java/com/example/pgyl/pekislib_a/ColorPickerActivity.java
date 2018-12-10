package com.example.pgyl.pekislib_a;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.example.pgyl.pekislib_a.Constants.ACTIVITY_EXTRA_KEYS;
import static com.example.pgyl.pekislib_a.Constants.COLOR_PREFIX;
import static com.example.pgyl.pekislib_a.Constants.HEX_RADIX;
import static com.example.pgyl.pekislib_a.Constants.PEKISLIB_ACTIVITIES;
import static com.example.pgyl.pekislib_a.Constants.SHP_FILE_NAME_SUFFIX;
import static com.example.pgyl.pekislib_a.HelpActivity.HELP_ACTIVITY_EXTRA_KEYS;
import static com.example.pgyl.pekislib_a.HelpActivity.HELP_ACTIVITY_TITLE;
import static com.example.pgyl.pekislib_a.PresetsActivity.PRESETS_ACTIVITY_DISPLAY_TYPE;
import static com.example.pgyl.pekislib_a.PresetsActivity.PRESETS_ACTIVITY_EXTRA_KEYS;
import static com.example.pgyl.pekislib_a.StringShelfDatabase.TABLE_DATA_INDEX;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.ACTIVITY_START_STATUS;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.TABLE_EXTRA_KEYS;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.getCurrentColorsInColorPickerActivity;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.getCurrentPresetInPresetsActivity;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.getCurrentStringInInputButtonsActivity;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.getLabels;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.isColdStartStatusInColorPickerActivity;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.setCurrentColorsInColorPickerActivity;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.setCurrentPresetInPresetsActivity;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.setCurrentStringInInputButtonsActivity;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.setStartStatusInColorPickerActivity;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.setStartStatusInInputButtonsActivity;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.setStartStatusInPresetsActivity;

public class ColorPickerActivity extends Activity {
    //region Constantes
    private enum COMMANDS {
        NEXT_COLOR_ITEM(""), NEXT_COLOR_SPACE(""), CANCEL("Cancel"), RGB_COLOR_VALUE(""), PRESETS("Presets"), OK("OK");

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

    private enum COLOR_PARAMS {
        RED_HUE(Color.RED), GREEN_SAT(Color.GREEN), BLUE_VAL(Color.BLUE);

        private int rgbColorValue;

        COLOR_PARAMS(int rgbColorValue) {
            this.rgbColorValue = rgbColorValue;
        }

        public int RGB_COLOR_VALUE() {
            return rgbColorValue;
        }

        public int INDEX() {
            return ordinal();
        }
    }

    private enum COLOR_SPACES {RGB, HSV}

    private enum SHP_KEY_NAMES {COLOR_INDEX, COLOR_SPACE}

    private final int COLOR_INDEX_DEFAULT_VALUE = 1;
    private final COLOR_SPACES COLOR_SPACE_DEFAULT_VALUE = COLOR_SPACES.RGB;
    //endregion
    //region Variables
    private SeekBar[] seekBars;
    private Drawable[] processDrawables;
    private String[] colors;
    private int colorIndex;
    private String[] labelNames;
    private COLOR_SPACES colorSpace;
    private float[] hsvStruc;
    private ColorWheelView colorWheelView;
    private ColorWheelViewUpdater colorWheelViewUpdater;
    private String tableName;
    private Button[] buttons;
    private boolean validReturnFromCalledActivity;
    private String calledActivity;
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
        setupColorWheelView();
        setupSeekBars();
        validReturnFromCalledActivity = false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        savePreferences();
        setCurrentColorsInColorPickerActivity(stringShelfDatabase, tableName, colors);
        stringShelfDatabase.close();
        stringShelfDatabase = null;
        colorWheelViewUpdater.close();
        colorWheelViewUpdater = null;
    }

    @Override
    protected void onResume() {
        super.onResume();

        shpFileName = getPackageName() + "." + getClass().getSimpleName() + SHP_FILE_NAME_SUFFIX;
        tableName = getIntent().getStringExtra(TABLE_EXTRA_KEYS.TABLE.toString());
        setupStringShelfDatabase();
        colors = getCurrentColorsInColorPickerActivity(stringShelfDatabase, tableName);
        labelNames = getLabels(stringShelfDatabase, tableName);

        if (isColdStartStatusInColorPickerActivity(stringShelfDatabase)) {
            setStartStatusInColorPickerActivity(stringShelfDatabase, ACTIVITY_START_STATUS.HOT);
            colorIndex = COLOR_INDEX_DEFAULT_VALUE;
            colorSpace = COLOR_SPACE_DEFAULT_VALUE;
        } else {
            colorIndex = getSHPcolorIndex();
            colorSpace = getSHPcolorSpace();
            if (validReturnFromCalledActivity) {
                validReturnFromCalledActivity = false;
                if (returnsFromInputButtonsActivity()) {
                    colors[colorIndex] = getCurrentStringInInputButtonsActivity(stringShelfDatabase, tableName, colorIndex);
                }
                if (returnsFromPresetsActivity()) {
                    colors = getCurrentPresetInPresetsActivity(stringShelfDatabase, tableName);
                }
            }
        }

        setupColorWheelViewColors();
        setupColorWheelViewUpdater();
        setupHSVColorSpace();
        updateDisplayButtonTextColorItem();
        updateDisplayButtonTextColorValue();
        updateDisplayColorSpace();
        updateDisplaySeekBarsProgress();
        colorWheelView.invalidate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {
        validReturnFromCalledActivity = false;
        if (requestCode == PEKISLIB_ACTIVITIES.INPUT_BUTTONS.INDEX()) {
            calledActivity = PEKISLIB_ACTIVITIES.INPUT_BUTTONS.toString();
            if (resultCode == RESULT_OK) {
                validReturnFromCalledActivity = true;
            }
        }
        if (requestCode == PEKISLIB_ACTIVITIES.PRESETS.INDEX()) {
            calledActivity = PEKISLIB_ACTIVITIES.PRESETS.toString();
            if (resultCode == RESULT_OK) {
                validReturnFromCalledActivity = true;
            }
        }
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
        long nowm = System.currentTimeMillis();
        if (command.equals(COMMANDS.NEXT_COLOR_ITEM)) {
            onButtonClickNextColorItem(nowm);
        }
        if (command.equals(COMMANDS.NEXT_COLOR_SPACE)) {
            onButtonClickNextColorSpace();
        }
        if (command.equals(COMMANDS.CANCEL)) {
            onButtonClickCancel();
        }
        if (command.equals(COMMANDS.RGB_COLOR_VALUE)) {
            onButtonClickRGB();
        }
        if (command.equals(COMMANDS.PRESETS)) {
            onButtonClickPresets();
        }
        if (command.equals(COMMANDS.OK)) {
            onButtonClickOK();
        }
    }

    private void onButtonClickNextColorItem(long nowm) {
        colorWheelViewUpdater.rotateAnimation(colorWheelView.getAngleSpread(), nowm);  //  Sens inverse des aiguilles d'une montre
    }

    private void onButtonClickNextColorSpace() {
        colorSpace = ((colorSpace.equals(COLOR_SPACES.RGB)) ? COLOR_SPACES.HSV : COLOR_SPACES.RGB);
        updateDisplayColorSpace();
        updateDisplaySeekBarsProgress();
        updateDisplayButtonTextColorValue();
    }

    private void onButtonClickCancel() {
        finish();
    }

    private void onButtonClickRGB() {
        setCurrentStringInInputButtonsActivity(stringShelfDatabase, tableName, colorIndex, colors[colorIndex]);
        launchInputButtonsActivity();
    }

    private void onButtonClickPresets() {
        setCurrentPresetInPresetsActivity(stringShelfDatabase, tableName, colors);
        launchPresetsActivity();
    }

    private void onButtonClickOK() {
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    private void onWheelColorIndexChange(int newColorIndex) {      //  La rotation de la roue fait passer à une autre couleur
        colorIndex = newColorIndex + 1;                            //  colors stocke le 1er élément (ID)
        updateDisplayButtonTextColorItem();
        updateDisplayButtonTextColorValue();
        updateDisplaySeekBarsProgress();
    }

    private void onSeekBarProgressChanged(boolean fromUser) {
        if (fromUser) {
            int redHueSeekbarValue = seekBars[COLOR_PARAMS.RED_HUE.INDEX()].getProgress();   //  0..65535
            int greenSatSeekbarValue = seekBars[COLOR_PARAMS.GREEN_SAT.INDEX()].getProgress();
            int blueValSeekbarValue = seekBars[COLOR_PARAMS.BLUE_VAL.INDEX()].getProgress();
            if (colorSpace.equals(COLOR_SPACES.RGB)) {
                colors[colorIndex] = String.format("%02X", (int) ((float) redHueSeekbarValue / 257f + 0.5f)) +    //  257 = 65535 / 255
                        String.format("%02X", (int) ((float) greenSatSeekbarValue / 257f + 0.5f)) +
                        String.format("%02X", (int) ((float) blueValSeekbarValue / 257f + 0.5f));
            } else {
                hsvStruc[0] = (float) redHueSeekbarValue / 65535f * 360f;
                hsvStruc[1] = (float) greenSatSeekbarValue / 65535f;
                hsvStruc[2] = (float) blueValSeekbarValue / 65535f;
                colors[colorIndex] = String.format("%06X", Color.HSVToColor(0, hsvStruc));
            }
            updateDisplayButtonTextColorValue();
            colorWheelView.setColor(colorIndex - 1, colors[colorIndex]);  //  colorWheelView ne stocke pas le 1er élément (ID)
            colorWheelView.invalidate();
        }
    }

    private void updateDisplayButtonTextColorItem() {
        final String SYMBOL_NEXT = " >";               //  Pour signifier qu'on peut passer au suivant en poussant sur le bouton

        buttons[COMMANDS.NEXT_COLOR_ITEM.INDEX()].setText(labelNames[colorIndex] + SYMBOL_NEXT);
    }

    private void updateDisplayButtonTextColorValue() {
        buttons[COMMANDS.RGB_COLOR_VALUE.INDEX()].setText(colors[colorIndex]);
    }

    private void updateDisplayColorSpace() {
        final String SYMBOL_NEXT = " >";               //  Pour signifier qu'on peut passer au suivant en poussant sur le bouton
        final String HSV_SEEKBAR_COLOR = "C0C0C0";

        buttons[COMMANDS.NEXT_COLOR_SPACE.INDEX()].setText(colorSpace.toString() + SYMBOL_NEXT);
        for (COLOR_PARAMS colorParam : COLOR_PARAMS.values()) {
            int seekBarColor = ((colorSpace.equals(COLOR_SPACES.RGB)) ? colorParam.RGB_COLOR_VALUE() : Color.parseColor(COLOR_PREFIX + HSV_SEEKBAR_COLOR));
            processDrawables[colorParam.INDEX()].setColorFilter(seekBarColor, PorterDuff.Mode.SRC_IN);  // Colorier uniquement la 1e partie de la seekbar
        }
    }

    private void updateDisplaySeekBarsProgress() {
        int red = Integer.parseInt(colors[colorIndex].substring(0, 2), HEX_RADIX);  //  0..255
        int green = Integer.parseInt(colors[colorIndex].substring(2, 4), HEX_RADIX);
        int blue = Integer.parseInt(colors[colorIndex].substring(4, 6), HEX_RADIX);
        if (colorSpace.equals(COLOR_SPACES.RGB)) {
            seekBars[COLOR_PARAMS.RED_HUE.INDEX()].setProgress(257 * red);    //  257 = 65535 / 255
            seekBars[COLOR_PARAMS.GREEN_SAT.INDEX()].setProgress(257 * green);
            seekBars[COLOR_PARAMS.BLUE_VAL.INDEX()].setProgress(257 * blue);
        } else {
            Color.RGBToHSV(red, green, blue, hsvStruc);
            seekBars[COLOR_PARAMS.RED_HUE.INDEX()].setProgress((int) (hsvStruc[0] / 360f * 65535f + 0.5f));
            seekBars[COLOR_PARAMS.GREEN_SAT.INDEX()].setProgress((int) (hsvStruc[1] * 65535f + 0.5f));
            seekBars[COLOR_PARAMS.BLUE_VAL.INDEX()].setProgress((int) (hsvStruc[2] * 65535f + 0.5f));
        }
    }

    private int getSHPcolorIndex() {
        SharedPreferences shp = getSharedPreferences(shpFileName, MODE_PRIVATE);
        return shp.getInt(SHP_KEY_NAMES.COLOR_INDEX.toString(), COLOR_INDEX_DEFAULT_VALUE);
    }

    private COLOR_SPACES getSHPcolorSpace() {
        SharedPreferences shp = getSharedPreferences(shpFileName, MODE_PRIVATE);
        return COLOR_SPACES.valueOf(shp.getString(SHP_KEY_NAMES.COLOR_SPACE.toString(), COLOR_SPACE_DEFAULT_VALUE.toString()));
    }

    private void savePreferences() {
        SharedPreferences shp = getSharedPreferences(shpFileName, MODE_PRIVATE);
        SharedPreferences.Editor shpEditor = shp.edit();
        shpEditor.putInt(SHP_KEY_NAMES.COLOR_INDEX.toString(), colorIndex);
        shpEditor.putString(SHP_KEY_NAMES.COLOR_SPACE.toString(), colorSpace.toString());
        shpEditor.commit();
    }

    private void setupOrientationLayout() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.colorpicker_p);
        } else {
            setContentView(R.layout.colorpicker_l);
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
            } catch (IllegalAccessException ex) {
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchFieldException ex) {
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void setupHSVColorSpace() {
        hsvStruc = new float[3];
    }

    private void setupColorWheelView() {
        colorWheelView = findViewById(R.id.COLORS_VIEW);
        colorWheelView.setOnColorIndexChangeListener(new ColorWheelView.onColorIndexChangeListener() {
            @Override
            public void onColorIndexChange(int colorIndex) {
                onWheelColorIndexChange(colorIndex);
            }
        });
        colorWheelView.enableMarker();
    }

    private void setupSeekBars() {
        final String SEEKBAR_XML_PREFIX = "SEEKB_";

        seekBars = new SeekBar[COLOR_PARAMS.values().length];
        LayerDrawable[] progressDrawables = new LayerDrawable[COLOR_PARAMS.values().length];
        processDrawables = new Drawable[COLOR_PARAMS.values().length];
        Class rid = R.id.class;
        for (COLOR_PARAMS colorParam : COLOR_PARAMS.values()) {
            try {
                seekBars[colorParam.INDEX()] = findViewById(rid.getField(SEEKBAR_XML_PREFIX + colorParam.toString()).getInt(rid));
                seekBars[colorParam.INDEX()].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        onSeekBarProgressChanged(fromUser);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                progressDrawables[colorParam.INDEX()] = (LayerDrawable) seekBars[colorParam.INDEX()].getProgressDrawable();
                processDrawables[colorParam.INDEX()] = progressDrawables[colorParam.INDEX()].findDrawableByLayerId(android.R.id.progress);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(InputButtonsActivity.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(InputButtonsActivity.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchFieldException ex) {
                Logger.getLogger(InputButtonsActivity.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(InputButtonsActivity.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void setupStringShelfDatabase() {
        stringShelfDatabase = new StringShelfDatabase(this);
        stringShelfDatabase.open();
    }

    private void setupColorWheelViewColors() {
        colorWheelView.setColors(Arrays.copyOfRange(colors, TABLE_DATA_INDEX, colors.length));   //  colorWheelView ne stocke pas le 1er élément (ID)
        colorWheelView.setColorIndex(colorIndex - 1);
    }

    private void setupColorWheelViewUpdater() {
        colorWheelViewUpdater = new ColorWheelViewUpdater(colorWheelView);
    }

    private void launchInputButtonsActivity() {
        setStartStatusInInputButtonsActivity(stringShelfDatabase, ACTIVITY_START_STATUS.COLD);
        Intent callingIntent = new Intent(this, InputButtonsActivity.class);
        callingIntent.putExtra(ACTIVITY_EXTRA_KEYS.TITLE.toString(), labelNames[colorIndex]);
        callingIntent.putExtra(TABLE_EXTRA_KEYS.TABLE.toString(), tableName);
        callingIntent.putExtra(TABLE_EXTRA_KEYS.INDEX.toString(), colorIndex);
        startActivityForResult(callingIntent, PEKISLIB_ACTIVITIES.INPUT_BUTTONS.INDEX());
    }

    private void launchPresetsActivity() {
        final String SEPARATOR = " - ";

        setStartStatusInPresetsActivity(stringShelfDatabase, ACTIVITY_START_STATUS.COLD);
        Intent callingIntent = new Intent(this, PresetsActivity.class);
        callingIntent.putExtra(ACTIVITY_EXTRA_KEYS.TITLE.toString(), "Color Presets");
        callingIntent.putExtra(PRESETS_ACTIVITY_EXTRA_KEYS.SEPARATOR.toString(), SEPARATOR);
        callingIntent.putExtra(PRESETS_ACTIVITY_EXTRA_KEYS.DISPLAY_TYPE.toString(), PRESETS_ACTIVITY_DISPLAY_TYPE.COLORS.toString());
        callingIntent.putExtra(TABLE_EXTRA_KEYS.TABLE.toString(), tableName);
        startActivityForResult(callingIntent, PEKISLIB_ACTIVITIES.PRESETS.INDEX());
    }

    private void launchHelpActivity() {
        Intent callingIntent = new Intent(this, HelpActivity.class);
        callingIntent.putExtra(ACTIVITY_EXTRA_KEYS.TITLE.toString(), HELP_ACTIVITY_TITLE);
        callingIntent.putExtra(HELP_ACTIVITY_EXTRA_KEYS.HTML_ID.toString(), R.raw.helpcolorpickeractivity);
        startActivity(callingIntent);
    }

    private boolean returnsFromInputButtonsActivity() {
        return (calledActivity.equals(PEKISLIB_ACTIVITIES.INPUT_BUTTONS.toString()));
    }

    private boolean returnsFromPresetsActivity() {
        return (calledActivity.equals(PEKISLIB_ACTIVITIES.PRESETS.toString()));
    }

}
