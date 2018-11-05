package com.example.pgyl.pekislib_a;

import android.app.ActionBar;
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
import static com.example.pgyl.pekislib_a.Constants.PEKISLIB_ACTIVITIES;
import static com.example.pgyl.pekislib_a.Constants.SHP_FILE_NAME_SUFFIX;
import static com.example.pgyl.pekislib_a.HelpActivity.HELP_ACTIVITY_EXTRA_KEYS;
import static com.example.pgyl.pekislib_a.HelpActivity.HELP_ACTIVITY_TITLE;
import static com.example.pgyl.pekislib_a.PresetsActivity.PRESETS_ACTIVITY_IS_COLOR_TYPE;
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
        NEXT("Next"), CANCEL("Cancel"), RGB("RGB"), PRESETS("Presets"), OK("OK");

        private String valueText;

        COMMANDS(String valueText) {
            this.valueText = valueText;
        }

        public String TEXT() {
            return valueText;
        }
    }

    private enum PRIMARY_COLORS {
        RED(Color.RED, 0), GREEN(Color.GREEN, 1), BLUE(Color.BLUE, 2);

        private int valueColor;
        private int valueIndex;

        PRIMARY_COLORS(int valueColor, int valueIndex) {
            this.valueColor = valueColor;
            this.valueIndex = valueIndex;
        }

        int VALUE() {
            return valueColor;
        }

        int INDEX() {
            return valueIndex;
        }
    }

    private enum SHP_KEY_NAMES {COLOR_INDEX}

    private final int COLOR_INDEX_DEFAULT_VALUE = 1;
    //endregion
    //region Variables
    private SeekBar[] seekbars;
    private String[] colors;
    private int colorIndex;
    private String[] labelNames;
    private ColorWheelView colorWheelView;
    private ColorWheelViewRobot colorWheelViewRobot;
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
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(getIntent().getStringExtra(ACTIVITY_EXTRA_KEYS.TITLE.toString()));
        setupOrientationLayout();
        setupButtons();
        setupColorWheelView();
        setupSeekbars();
        validReturnFromCalledActivity = false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        savePreferences();
        setCurrentColorsInColorPickerActivity(stringShelfDatabase, tableName, colors);
        stringShelfDatabase.close();
        stringShelfDatabase = null;
        colorWheelViewRobot.close();
        colorWheelViewRobot = null;
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
        } else {
            colorIndex = getSHPcolorIndex();
            if (validReturnFromCalledActivity) {
                validReturnFromCalledActivity = false;
                if (returnsFromInputButtonsActivity()) {
                    colors[colorIndex] = getCurrentStringInInputButtonsActivity(stringShelfDatabase, tableName, colorIndex);
                }
                if (returnsFromPresetsActivity()) {
                    colors = getCurrentPresetInPresetsActivity(stringShelfDatabase, tableName);
                }
                if (returnsFromHelpActivity()) {
                    //  NOP
                }
            }
        }
        setupColorWheelViewColors();
        setupColorWheelViewRobot();
        updateButtonTexts();
        updateSeekBars();
        colorWheelView.invalidate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {
        validReturnFromCalledActivity = false;
        if (requestCode == PEKISLIB_ACTIVITIES.INPUT_BUTTONS.ordinal()) {
            calledActivity = PEKISLIB_ACTIVITIES.INPUT_BUTTONS.toString();
            if (resultCode == RESULT_OK) {
                validReturnFromCalledActivity = true;
            }
        }
        if (requestCode == PEKISLIB_ACTIVITIES.PRESETS.ordinal()) {
            calledActivity = PEKISLIB_ACTIVITIES.PRESETS.toString();
            if (resultCode == RESULT_OK) {
                validReturnFromCalledActivity = true;
            }
        }
        if (requestCode == PEKISLIB_ACTIVITIES.HELP.ordinal()) {
            calledActivity = PEKISLIB_ACTIVITIES.HELP.toString();
            validReturnFromCalledActivity = true;
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
        if (command.equals(COMMANDS.NEXT)) {
            onButtonClickNext(nowm);
        }
        if (command.equals(COMMANDS.CANCEL)) {
            onButtonClickCancel();
        }
        if (command.equals(COMMANDS.RGB)) {
            onButtonClickRGB();
        }
        if (command.equals(COMMANDS.PRESETS)) {
            onButtonClickPresets();
        }
        if (command.equals(COMMANDS.OK)) {
            onButtonClickOK();
        }
    }

    private void onButtonClickNext(long nowm) {
        colorWheelViewRobot.rotateAnimation(colorWheelView.getAngleSpread(), nowm);  //  Sens inverse des aiguilles d'une montre
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
        updateButtonTexts();
        updateSeekBars();
    }

    private void onSeekBarProgressChanged(boolean fromUser) {
        if (fromUser) {
            String rgb = String.format("%02X", seekbars[PRIMARY_COLORS.RED.INDEX()].getProgress());
            rgb = rgb + String.format("%02X", seekbars[PRIMARY_COLORS.GREEN.INDEX()].getProgress());
            rgb = rgb + String.format("%02X", seekbars[PRIMARY_COLORS.BLUE.INDEX()].getProgress());
            colors[colorIndex] = rgb;
            int index = COMMANDS.RGB.ordinal();
            buttons[index].setText(rgb);
            colorWheelView.setColor(colorIndex - 1, rgb);  //  colorWheelView ne stocke pas le 1er élément (ID)
            colorWheelView.invalidate();
        }
    }

    private void updateButtonTexts() {
        final String SYMBOL_NEXT = " >";               //  Pour signifier qu'on peut passer au suivant en poussant sur le bouton

        int index = COMMANDS.NEXT.ordinal();
        buttons[index].setText(labelNames[colorIndex] + SYMBOL_NEXT);
        index = COMMANDS.RGB.ordinal();
        buttons[index].setText(colors[colorIndex]);
    }

    private void updateSeekBars() {
        for (PRIMARY_COLORS primaryColor : PRIMARY_COLORS.values()) {
            int index = primaryColor.INDEX();
            seekbars[index].setProgress(Integer.parseInt(colors[colorIndex].substring(2 * index, 2 * (index + 1)), 16));
        }
    }

    private int getSHPcolorIndex() {
        SharedPreferences shp = getSharedPreferences(shpFileName, MODE_PRIVATE);
        return shp.getInt(SHP_KEY_NAMES.COLOR_INDEX.toString(), COLOR_INDEX_DEFAULT_VALUE);
    }

    private void savePreferences() {
        SharedPreferences shp = getSharedPreferences(shpFileName, MODE_PRIVATE);
        SharedPreferences.Editor shpEditor = shp.edit();
        shpEditor.putInt(SHP_KEY_NAMES.COLOR_INDEX.toString(), colorIndex);
        shpEditor.commit();
    }

    private void setupOrientationLayout() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.colorpicker_p);
        } else {
            setContentView(R.layout.colorpicker_l);
        }
    }

    private void setupColorWheelViewColors() {
        colorWheelView.setColors(Arrays.copyOfRange(colors, TABLE_DATA_INDEX, colors.length));   //  colorWheelView ne stocke pas le 1er élément (ID)
        colorWheelView.setColorIndex(colorIndex - 1);
    }

    private void setupButtons() {
        final String BUTTON_XML_PREFIX = "BTN_";

        buttons = new Button[COMMANDS.values().length];
        Class rid = R.id.class;
        for (COMMANDS command : COMMANDS.values()) {
            try {
                int index = command.ordinal();
                buttons[index] = findViewById(rid.getField(BUTTON_XML_PREFIX + command.toString()).getInt(rid));   //  1, 2, 3 ... dans le XML
                buttons[index].setText(command.TEXT());
                final COMMANDS fcommand = command;
                buttons[index].setOnClickListener(new Button.OnClickListener() {
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

    private void setupSeekbars() {
        final String SEEKBAR_XML_PREFIX = "SEEKB_";

        seekbars = new SeekBar[PRIMARY_COLORS.values().length];
        LayerDrawable[] progressDrawables = new LayerDrawable[PRIMARY_COLORS.values().length];
        Drawable[] processDrawables = new Drawable[PRIMARY_COLORS.values().length];
        Class rid = R.id.class;
        for (PRIMARY_COLORS primaryColor : PRIMARY_COLORS.values()) {
            try {
                int index = primaryColor.INDEX();
                seekbars[index] = findViewById(rid.getField(SEEKBAR_XML_PREFIX + (index + 1)).getInt(rid));
                seekbars[index].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
                progressDrawables[index] = (LayerDrawable) seekbars[index].getProgressDrawable();
                processDrawables[index] = progressDrawables[index].findDrawableByLayerId(android.R.id.progress);
                processDrawables[index].setColorFilter(primaryColor.VALUE(), PorterDuff.Mode.SRC_IN);  // Colorier uniquement la 1e partie de la seekbar
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

    private void setupColorWheelViewRobot() {
        colorWheelViewRobot = new ColorWheelViewRobot(colorWheelView);
    }

    private void setupStringShelfDatabase() {
        stringShelfDatabase = new StringShelfDatabase(this);
        stringShelfDatabase.open();
    }

    private void launchHelpActivity() {
        Intent callingIntent = new Intent(this, HelpActivity.class);
        callingIntent.putExtra(ACTIVITY_EXTRA_KEYS.TITLE.toString(), HELP_ACTIVITY_TITLE);
        callingIntent.putExtra(HELP_ACTIVITY_EXTRA_KEYS.HTML_ID.toString(), R.raw.helpcolorpickeractivity);
        startActivityForResult(callingIntent, PEKISLIB_ACTIVITIES.HELP.ordinal());
    }

    private void launchInputButtonsActivity() {
        setStartStatusInInputButtonsActivity(stringShelfDatabase, ACTIVITY_START_STATUS.COLD);
        Intent callingIntent = new Intent(this, InputButtonsActivity.class);
        callingIntent.putExtra(ACTIVITY_EXTRA_KEYS.TITLE.toString(), labelNames[colorIndex]);
        callingIntent.putExtra(TABLE_EXTRA_KEYS.TABLE.toString(), tableName);
        callingIntent.putExtra(TABLE_EXTRA_KEYS.INDEX.toString(), colorIndex);
        startActivityForResult(callingIntent, PEKISLIB_ACTIVITIES.INPUT_BUTTONS.ordinal());
    }

    private void launchPresetsActivity() {
        final String SEPARATOR = " - ";

        setStartStatusInPresetsActivity(stringShelfDatabase, ACTIVITY_START_STATUS.COLD);
        Intent callingIntent = new Intent(this, PresetsActivity.class);
        callingIntent.putExtra(ACTIVITY_EXTRA_KEYS.TITLE.toString(), "Color Presets");
        callingIntent.putExtra(PRESETS_ACTIVITY_EXTRA_KEYS.SEPARATOR.toString(), SEPARATOR);
        callingIntent.putExtra(PRESETS_ACTIVITY_EXTRA_KEYS.IS_COLOR_TYPE.toString(), String.valueOf(PRESETS_ACTIVITY_IS_COLOR_TYPE ? 1 : 0));
        callingIntent.putExtra(TABLE_EXTRA_KEYS.TABLE.toString(), tableName);
        startActivityForResult(callingIntent, PEKISLIB_ACTIVITIES.PRESETS.ordinal());
    }

    private boolean returnsFromInputButtonsActivity() {
        return (calledActivity.equals(PEKISLIB_ACTIVITIES.INPUT_BUTTONS.toString()));
    }

    private boolean returnsFromPresetsActivity() {
        return (calledActivity.equals(PEKISLIB_ACTIVITIES.PRESETS.toString()));
    }

    private boolean returnsFromHelpActivity() {
        return (calledActivity.equals(PEKISLIB_ACTIVITIES.HELP.toString()));
    }

}
