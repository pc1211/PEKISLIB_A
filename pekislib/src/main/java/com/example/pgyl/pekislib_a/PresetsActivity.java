package com.example.pgyl.pekislib_a;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.example.pgyl.pekislib_a.Constants.ACTIVITY_EXTRA_KEYS;
import static com.example.pgyl.pekislib_a.Constants.BUTTON_STATES;
import static com.example.pgyl.pekislib_a.Constants.NOT_FOUND;
import static com.example.pgyl.pekislib_a.Constants.PEKISLIB_ACTIVITIES;
import static com.example.pgyl.pekislib_a.Constants.SHP_FILE_NAME_SUFFIX;
import static com.example.pgyl.pekislib_a.HelpActivity.HELP_ACTIVITY_EXTRA_KEYS;
import static com.example.pgyl.pekislib_a.HelpActivity.HELP_ACTIVITY_TITLE;
import static com.example.pgyl.pekislib_a.InputButtonsActivity.KEYBOARDS;
import static com.example.pgyl.pekislib_a.MiscUtils.toastLong;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseTables.ACTIVITY_START_STATUS;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseTables.TABLE_EXTRA_KEYS;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.getCurrentValueInActivity;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.getCurrentValuesInActivity;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.getDefaults;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.getKeyboards;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.getLabels;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.getTimeUnits;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.isColdStartStatusInActivity;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.setCurrentValueInActivity;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.setCurrentValuesInActivity;
import static com.example.pgyl.pekislib_a.StringShelfDatabaseUtils.setStartStatusInActivity;
import static com.example.pgyl.pekislib_a.TimeDateUtils.TIME_UNITS;
import static com.example.pgyl.pekislib_a.TimeDateUtils.msToTimeFormatD;

public class PresetsActivity extends Activity {
    //region Constantes
    private enum COMMANDS {
        NEXT_FIELD(""), CANCEL("Cancel"), FIELD(""), OK("OK"), ADD("Add"), REMOVE("Remove"), DESELECT("Deselect"), DEFAULT("Default");

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

    public enum PRESETS_ACTIVITY_EXTRA_KEYS {
        SEPARATOR, DISPLAY_TYPE
    }

    public enum PRESETS_ACTIVITY_DISPLAY_TYPE {
        COLORS, NO_COLORS
    }

    private enum SHP_KEY_NAMES {
        SELECT_INDEX, COLUMN_INDEX
    }

    private final int LIST_INDEX_DEFAULT_VALUE = NOT_FOUND;
    private final int COLUMN_INDEX_DEFAULT_VALUE = 1;
    //endregion
    //region Variables
    private PresetsHandler presetsHandler;
    private String[] preset;
    private String[] labelNames;
    private String[] keyboards;
    private String[] timeUnits;
    private String[] defaults;
    private String tableName;
    private boolean isTypeColors;
    private int listIndex;
    private int columnIndex;
    private CustomButton[] buttons;
    private ListView listView;
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
        setupList();
        validReturnFromCalledActivity = false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        savePreferences();
        setCurrentValuesInActivity(stringShelfDatabase, PEKISLIB_ACTIVITIES.PRESETS.toString(), tableName, preset);
        presetsHandler.saveAndClose();
        presetsHandler = null;
        stringShelfDatabase.close();
        stringShelfDatabase = null;
    }

    @Override
    protected void onResume() {
        super.onResume();

        shpFileName = getPackageName() + "." + getClass().getSimpleName() + SHP_FILE_NAME_SUFFIX;
        tableName = getIntent().getStringExtra(TABLE_EXTRA_KEYS.TABLE.toString());
        isTypeColors = ((getIntent().getStringExtra(PRESETS_ACTIVITY_EXTRA_KEYS.DISPLAY_TYPE.toString()).equals(PRESETS_ACTIVITY_DISPLAY_TYPE.COLORS.toString())) ? true : false);

        setupStringShelfDatabase();
        setupPresetsHandler();
        setupButtonSpecialColors();
        preset = getCurrentValuesInActivity(stringShelfDatabase, PEKISLIB_ACTIVITIES.PRESETS.toString(), tableName);
        labelNames = getLabels(stringShelfDatabase, tableName);
        keyboards = getKeyboards(stringShelfDatabase, tableName);
        timeUnits = getTimeUnits(stringShelfDatabase, tableName);
        defaults = getDefaults(stringShelfDatabase, tableName);

        if (isColdStartStatusInActivity(stringShelfDatabase, PEKISLIB_ACTIVITIES.PRESETS.toString())) {
            setStartStatusInActivity(stringShelfDatabase, PEKISLIB_ACTIVITIES.PRESETS.toString(), ACTIVITY_START_STATUS.HOT);
            listIndex = LIST_INDEX_DEFAULT_VALUE;
            columnIndex = COLUMN_INDEX_DEFAULT_VALUE;
        } else {
            listIndex = getSHPselectIndex();
            columnIndex = getSHPcolumnIndex();
            if (validReturnFromCalledActivity) {
                validReturnFromCalledActivity = false;
                if (returnsFromInputButtonsActivity()) {
                    preset[columnIndex] = getCurrentValueInActivity(stringShelfDatabase, PEKISLIB_ACTIVITIES.INPUT_BUTTONS.toString(), tableName, columnIndex);
                    if (listIndex != LIST_INDEX_DEFAULT_VALUE) {
                        presetsHandler.setPresetColumn(listIndex, columnIndex, preset[columnIndex]);
                    }
                }
            }
        }
        updateDisplayButtonTexts();
        rebuildPresets();
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
        if (command.equals(COMMANDS.NEXT_FIELD)) {
            onButtonClickNext();
        }
        if (command.equals(COMMANDS.CANCEL)) {
            onButtonClickCancel();
        }
        if (command.equals(COMMANDS.FIELD)) {
            onButtonClickField();
        }
        if (command.equals(COMMANDS.OK)) {
            onButtonClickOK();
        }
        if (command.equals(COMMANDS.ADD)) {
            onButtonClickAdd();
        }
        if (command.equals(COMMANDS.REMOVE)) {
            onButtonClickRemove();
        }
        if (command.equals(COMMANDS.DESELECT)) {
            onButtonClickDeselect();
        }
        if (command.equals(COMMANDS.DEFAULT)) {
            onButtonClickDefault();
        }
    }

    private void onButtonClickNext() {
        int k = columnIndex;
        k = k + 1;
        if (k > (preset.length - 1)) {
            k = 1;
        }
        columnIndex = k;
        updateDisplayButtonTexts();
    }

    private void onButtonClickCancel() {
        finish();
    }

    private void onButtonClickField() {
        setCurrentValueInActivity(stringShelfDatabase, PEKISLIB_ACTIVITIES.INPUT_BUTTONS.toString(), tableName, columnIndex, preset[columnIndex]);
        launchInputButtonsActivity();
    }

    private void onButtonClickOK() {
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    private void onButtonClickAdd() {
        presetsHandler.createNewPreset(preset);
        rebuildDisplay();
        updateDisplayButtonFieldColor();
    }

    private void onButtonClickRemove() {
        if (listIndex != LIST_INDEX_DEFAULT_VALUE) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Remove preset");
            builder.setMessage("Are you sure?");
            builder.setCancelable(false);   // false = pressing back button won't dismiss this alert
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int id) {
                    presetsHandler.removePreset(listIndex);
                    rebuildDisplay();
                }
            });
            builder.setNegativeButton("No", null);
            Dialog dialog = builder.create();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {    // OK pour modifier UI sous-jacente à la boîte de dialogue
                    updateDisplayButtonFieldColor();
                }
            });
            dialog.show();
        } else {
            toastLong("A preset must be selected in the list", this);
        }
    }

    private void onButtonClickDeselect() {
        if (listIndex != LIST_INDEX_DEFAULT_VALUE) {
            listIndex = LIST_INDEX_DEFAULT_VALUE;
            updateDisplayButtonFieldColor();
        } else {
            toastLong("A preset must be selected in the list", this);
        }
    }

    private void onButtonClickDefault() {
        preset[columnIndex] = defaults[columnIndex];
        if (listIndex != LIST_INDEX_DEFAULT_VALUE) {
            presetsHandler.setPresetColumn(listIndex, columnIndex, preset[columnIndex]);
            rebuildPresets();
        }
        updateDisplayButtonTexts();
    }

    private void onPresetClick(int pos) {
        if (presetsHandler.getCount() > 0) {
            preset = presetsHandler.getPreset(pos);
            listIndex = pos;
            columnIndex = COLUMN_INDEX_DEFAULT_VALUE;
            updateDisplayButtonTexts();
            updateDisplayButtonFieldColor();
        }
    }

    private void updateDisplayButtonTexts() {
        final String SYMBOL_NEXT = " >";   //  Pour signifier qu'on peut passer au champ suivant en poussant sur le bouton

        String fieldText = preset[columnIndex];
        if ((keyboards[columnIndex].equals(KEYBOARDS.TIME_FORMAT_D.toString())) || (keyboards[columnIndex].equals(KEYBOARDS.TIME_FORMAT_DL.toString()))) {
            fieldText = msToTimeFormatD(Long.parseLong(fieldText), TIME_UNITS.valueOf(timeUnits[columnIndex]));
        }
        buttons[COMMANDS.FIELD.INDEX()].setText(fieldText);
        buttons[COMMANDS.NEXT_FIELD.INDEX()].setText(labelNames[columnIndex] + SYMBOL_NEXT);
    }

    private void updateDisplayButtonFieldColor() {
        final String SPECIAL_FIELD_UNPRESSED_COLOR = "FF9A22";
        final String SPECIAL_FIELD_PRESSED_COLOR = "995400";

        boolean needSpecialColor = (listIndex != LIST_INDEX_DEFAULT_VALUE);
        String pressedColor = ((needSpecialColor) ? SPECIAL_FIELD_PRESSED_COLOR : BUTTON_STATES.PRESSED.DEFAULT_COLOR());
        String unpressedColor = ((needSpecialColor) ? SPECIAL_FIELD_UNPRESSED_COLOR : BUTTON_STATES.UNPRESSED.DEFAULT_COLOR());
        buttons[COMMANDS.FIELD.INDEX()].setColors(pressedColor, unpressedColor);
    }

    private void rebuildDisplay() {
        listIndex = LIST_INDEX_DEFAULT_VALUE;
        columnIndex = COLUMN_INDEX_DEFAULT_VALUE;
        rebuildPresets();
        updateDisplayButtonTexts();
    }

    private void rebuildPresets() {
        if (listIndex != LIST_INDEX_DEFAULT_VALUE) {
            String presetId = presetsHandler.getPresetId(listIndex);
            presetsHandler.sortPresets();
            listIndex = presetsHandler.getIndex(presetId);
        } else {
            presetsHandler.sortPresets();
        }
        if (isTypeColors) {   //  Afficher aussi la roue de couleurs
            ListItemColorAdapter lvAdapter = new ListItemColorAdapter(this);
            lvAdapter.setColorItems(presetsHandler.presetDataList());
            lvAdapter.setTextItems(presetsHandler.concatenatedDisplayPresetDataList());
            listView.setAdapter(lvAdapter);
            lvAdapter = null;
        } else {
            ListItemNoColorAdapter lvAdapter = new ListItemNoColorAdapter(this, presetsHandler.concatenatedDisplayPresetDataList());
            listView.setAdapter(lvAdapter);
            lvAdapter = null;
        }
    }

    private int getSHPselectIndex() {
        SharedPreferences shp = getSharedPreferences(shpFileName, MODE_PRIVATE);
        return shp.getInt(SHP_KEY_NAMES.SELECT_INDEX.toString(), LIST_INDEX_DEFAULT_VALUE);
    }

    private int getSHPcolumnIndex() {
        SharedPreferences shp = getSharedPreferences(shpFileName, MODE_PRIVATE);
        return shp.getInt(SHP_KEY_NAMES.COLUMN_INDEX.toString(), COLUMN_INDEX_DEFAULT_VALUE);
    }

    private void savePreferences() {
        SharedPreferences shp = getSharedPreferences(shpFileName, MODE_PRIVATE);
        SharedPreferences.Editor shpEditor = shp.edit();
        shpEditor.putInt(SHP_KEY_NAMES.SELECT_INDEX.toString(), listIndex);
        shpEditor.putInt(SHP_KEY_NAMES.COLUMN_INDEX.toString(), columnIndex);
        shpEditor.commit();
    }

    private void setupOrientationLayout() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.presets_p);
        } else {
            setContentView(R.layout.presets_l);
        }
    }

    private void setupButtons() {
        final String BUTTON_XML_PREFIX = "BTN_";
        final long BUTTON_MIN_CLICK_TIME_INTERVAL_MS = 500;

        buttons = new CustomButton[COMMANDS.values().length];
        Class rid = R.id.class;
        for (COMMANDS command : COMMANDS.values()) {
            try {
                buttons[command.INDEX()] = findViewById(rid.getField(BUTTON_XML_PREFIX + command.toString()).getInt(rid));   //  1, 2, 3 ... dans le XML
                buttons[command.INDEX()].setText(command.TEXT());
                buttons[command.INDEX()].setMinClickTimeInterval(BUTTON_MIN_CLICK_TIME_INTERVAL_MS);
                final COMMANDS fcommand = command;
                buttons[command.INDEX()].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onButtonClick(fcommand);
                    }
                });
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void setupList() {
        listView = findViewById(R.id.CT_LIST);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onPresetClick(position);
            }
        });
    }

    private void setupStringShelfDatabase() {
        stringShelfDatabase = new StringShelfDatabase(this);
        stringShelfDatabase.open();
    }

    private void setupPresetsHandler() {
        presetsHandler = new PresetsHandler(stringShelfDatabase);
        presetsHandler.setSeparator(getIntent().getStringExtra(PRESETS_ACTIVITY_EXTRA_KEYS.SEPARATOR.toString()));
        presetsHandler.setTableName(tableName);
    }

    private void setupButtonSpecialColors() {
        final String OK_UNPRESSED_COLOR_DEFAULT = "668CFF";
        final String OK_PRESSED_COLOR_DEFAULT = "0040FF";

        for (COMMANDS command : COMMANDS.values()) {
            if (command.equals(COMMANDS.OK)) {
                buttons[command.INDEX()].setColors(OK_PRESSED_COLOR_DEFAULT, OK_UNPRESSED_COLOR_DEFAULT);
            }
        }
    }

    private void launchInputButtonsActivity() {
        setStartStatusInActivity(stringShelfDatabase, PEKISLIB_ACTIVITIES.INPUT_BUTTONS.toString(), ACTIVITY_START_STATUS.COLD);
        Intent callingIntent = new Intent(this, InputButtonsActivity.class);
        callingIntent.putExtra(TABLE_EXTRA_KEYS.TABLE.toString(), tableName);
        callingIntent.putExtra(TABLE_EXTRA_KEYS.INDEX.toString(), columnIndex);
        callingIntent.putExtra(ACTIVITY_EXTRA_KEYS.TITLE.toString(), labelNames[columnIndex]);
        startActivityForResult(callingIntent, PEKISLIB_ACTIVITIES.INPUT_BUTTONS.INDEX());
    }

    private void launchHelpActivity() {
        Intent callingIntent = new Intent(this, HelpActivity.class);
        callingIntent.putExtra(ACTIVITY_EXTRA_KEYS.TITLE.toString(), HELP_ACTIVITY_TITLE);
        callingIntent.putExtra(HELP_ACTIVITY_EXTRA_KEYS.HTML_ID.toString(), R.raw.helppresetsactivity);
        startActivity(callingIntent);
    }

    private boolean returnsFromInputButtonsActivity() {
        return (calledActivity.equals(PEKISLIB_ACTIVITIES.INPUT_BUTTONS.toString()));
    }

}