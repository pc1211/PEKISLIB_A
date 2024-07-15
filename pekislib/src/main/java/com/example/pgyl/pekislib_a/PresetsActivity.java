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

import static com.example.pgyl.pekislib_a.ColorUtils.BUTTON_COLOR_TYPES;
import static com.example.pgyl.pekislib_a.Constants.PEKISLIB_ACTIVITIES;
import static com.example.pgyl.pekislib_a.Constants.PEKISLIB_ACTIVITY_EXTRA_KEYS;
import static com.example.pgyl.pekislib_a.Constants.SHP_FILE_NAME_SUFFIX;
import static com.example.pgyl.pekislib_a.Constants.UNDEFINED;
import static com.example.pgyl.pekislib_a.HelpActivity.HELP_ACTIVITY_EXTRA_KEYS;
import static com.example.pgyl.pekislib_a.HelpActivity.HELP_ACTIVITY_TITLE;
import static com.example.pgyl.pekislib_a.InputButtonsActivity.KEYBOARDS;
import static com.example.pgyl.pekislib_a.MiscUtils.toastLong;
import static com.example.pgyl.pekislib_a.StringDBTables.ACTIVITY_START_STATUS;
import static com.example.pgyl.pekislib_a.StringDBTables.TABLE_EXTRA_KEYS;
import static com.example.pgyl.pekislib_a.StringDBUtils.getCurrentFromActivity;
import static com.example.pgyl.pekislib_a.StringDBUtils.getCurrentsFromActivity;
import static com.example.pgyl.pekislib_a.StringDBUtils.getDefaults;
import static com.example.pgyl.pekislib_a.StringDBUtils.getKeyboards;
import static com.example.pgyl.pekislib_a.StringDBUtils.getLabels;
import static com.example.pgyl.pekislib_a.StringDBUtils.getTimeUnitPrecisions;
import static com.example.pgyl.pekislib_a.StringDBUtils.isColdStartStatusOfActivity;
import static com.example.pgyl.pekislib_a.StringDBUtils.setCurrentForActivity;
import static com.example.pgyl.pekislib_a.StringDBUtils.setCurrentsForActivity;
import static com.example.pgyl.pekislib_a.StringDBUtils.setStartStatusOfActivity;
import static com.example.pgyl.pekislib_a.TimeDateUtils.TIME_UNITS;
import static com.example.pgyl.pekislib_a.TimeDateUtils.msToTimeFormatD;

public class PresetsActivity extends Activity {
    //region Constantes
    private enum COMMANDS {
        NEXT_FIELD(""), CANCEL("Cancel"), FIELD(""), OK("OK"), ADD("Add"), REMOVE("Remove"), DEFAULT("Default");

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

    private final int LIST_INDEX_DEFAULT_VALUE = UNDEFINED;
    private final int COLUMN_INDEX_DEFAULT_VALUE = 1;
    //endregion
    //region Variables
    private PresetsHandler presetsHandler;
    private String[] preset;   //  Valeurs des champs du preset actuel
    private String[] labelNames;
    private String[] keyboards;
    private String[] timeUnitPrecisions;
    private String[] defaults;
    private String tableName;
    private boolean isTypeColors;
    private int listIndex;
    private int columnIndex;
    private ImageButtonView[] buttons;
    private ListView listView;
    private boolean validReturnFromCalledActivity;
    private String calledActivityName;
    private StringDB stringDB;
    private String shpFileName;
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().setTitle(getIntent().getStringExtra(PEKISLIB_ACTIVITY_EXTRA_KEYS.TITLE.toString()));

        validReturnFromCalledActivity = false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        savePreferences();
        setCurrentsForActivity(stringDB, PEKISLIB_ACTIVITIES.PRESETS.toString(), tableName, preset);
        presetsHandler.saveAndClose();
        presetsHandler = null;
        stringDB.close();
        stringDB = null;
    }

    @Override
    protected void onResume() {
        super.onResume();

        setupOrientationLayout();
        setupButtons();
        setupList();

        shpFileName = getPackageName() + "." + getClass().getSimpleName() + SHP_FILE_NAME_SUFFIX;
        tableName = getIntent().getStringExtra(TABLE_EXTRA_KEYS.TABLE.toString());
        isTypeColors = getIntent().getStringExtra(PRESETS_ACTIVITY_EXTRA_KEYS.DISPLAY_TYPE.toString()).equals(PRESETS_ACTIVITY_DISPLAY_TYPE.COLORS.toString());

        setupStringDB();
        setupPresetsHandler();
        updateDisplayButtonSpecialColors();
        preset = getCurrentsFromActivity(stringDB, PEKISLIB_ACTIVITIES.PRESETS.toString(), tableName);
        labelNames = getLabels(stringDB, tableName);
        keyboards = getKeyboards(stringDB, tableName);
        timeUnitPrecisions = getTimeUnitPrecisions(stringDB, tableName);
        defaults = getDefaults(stringDB, tableName);

        if (isColdStartStatusOfActivity(stringDB, PEKISLIB_ACTIVITIES.PRESETS.toString())) {
            setStartStatusOfActivity(stringDB, PEKISLIB_ACTIVITIES.PRESETS.toString(), ACTIVITY_START_STATUS.HOT);
            listIndex = LIST_INDEX_DEFAULT_VALUE;
            columnIndex = COLUMN_INDEX_DEFAULT_VALUE;
        } else {
            listIndex = getSHPselectIndex();
            columnIndex = getSHPcolumnIndex();
            if (validReturnFromCalledActivity) {
                validReturnFromCalledActivity = false;
                if (calledActivityName.equals(PEKISLIB_ACTIVITIES.INPUT_BUTTONS.toString())) {
                    preset[columnIndex] = getCurrentFromActivity(stringDB, PEKISLIB_ACTIVITIES.INPUT_BUTTONS.toString(), tableName, columnIndex);
                    if (listIndex != LIST_INDEX_DEFAULT_VALUE) {
                        presetsHandler.setPresetColumn(listIndex, columnIndex, preset[columnIndex]);
                    }
                }
            }
        }
        rebuildPresets();
        updateDisplayButtonTexts();
        updateDisplayButtonFieldColor();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {
        validReturnFromCalledActivity = false;
        if (requestCode == PEKISLIB_ACTIVITIES.INPUT_BUTTONS.INDEX()) {
            calledActivityName = PEKISLIB_ACTIVITIES.INPUT_BUTTONS.toString();
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
        launchInputButtonsActivity();
    }

    private void onButtonClickOK() {
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    private void onButtonClickAdd() {
        presetsHandler.createNewPreset(preset);
        rebuildPresets();
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
                    listIndex = LIST_INDEX_DEFAULT_VALUE;
                    columnIndex = COLUMN_INDEX_DEFAULT_VALUE;
                    rebuildPresets();
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

    private void onButtonClickDefault() {
        preset[columnIndex] = defaults[columnIndex];
        if (listIndex != LIST_INDEX_DEFAULT_VALUE) {
            presetsHandler.setPresetColumn(listIndex, columnIndex, preset[columnIndex]);
            rebuildPresets();
        }
        updateDisplayButtonTexts();
    }

    private void onPresetClick(int pos, View view) {
        if (pos != listIndex) {
            view.setSelected(true);
            if (presetsHandler.getCount() > 0) {
                preset = presetsHandler.getPreset(pos);
                listIndex = pos;
                columnIndex = COLUMN_INDEX_DEFAULT_VALUE;
                updateDisplayButtonTexts();
            }
        } else {
            listView.clearFocus();   //   Plus aucun item n'est sélectionné
            listIndex = LIST_INDEX_DEFAULT_VALUE;
        }
        updateDisplayButtonFieldColor();
    }

    private void updateDisplayButtonTexts() {
        final String SYMBOL_NEXT = " >";   //  Pour signifier qu'on peut passer au champ suivant en poussant sur le bouton

        String fieldText = preset[columnIndex];
        if ((keyboards[columnIndex].equals(KEYBOARDS.TIME_FORMAT_D.toString())) || (keyboards[columnIndex].equals(KEYBOARDS.TIME_FORMAT_DL.toString()))) {
            fieldText = msToTimeFormatD(Long.parseLong(fieldText), TIME_UNITS.valueOf(timeUnitPrecisions[columnIndex]), TIME_UNITS.valueOf(timeUnitPrecisions[columnIndex]));
        }
        buttons[COMMANDS.FIELD.INDEX()].setText(fieldText);
        buttons[COMMANDS.NEXT_FIELD.INDEX()].setText(labelNames[columnIndex] + SYMBOL_NEXT);
    }

    private void updateDisplayButtonFieldColor() {
        final String FIELD_UNPRESSED_COLOR = "FF9A22";
        final String FIELD_PRESSED_COLOR = "995400";

        ColorBox colorBox = buttons[COMMANDS.FIELD.INDEX()].getColorBox();
        ColorBox defaultColorbox = buttons[COMMANDS.FIELD.INDEX()].getDefaultColorBox();
        boolean needSpecialColor = (listIndex != LIST_INDEX_DEFAULT_VALUE);
        colorBox.setColor(BUTTON_COLOR_TYPES.UNPRESSED_BACK.INDEX(), (needSpecialColor) ? FIELD_UNPRESSED_COLOR : defaultColorbox.getColor(BUTTON_COLOR_TYPES.UNPRESSED_BACK.INDEX()).RGBString);
        colorBox.setColor(BUTTON_COLOR_TYPES.PRESSED_BACK.INDEX(), (needSpecialColor) ? FIELD_PRESSED_COLOR : defaultColorbox.getColor(BUTTON_COLOR_TYPES.PRESSED_BACK.INDEX()).RGBString);
        buttons[COMMANDS.FIELD.INDEX()].updateDisplay();
    }

    private void updateDisplayButtonSpecialColors() {
        final String OK_UNPRESSED_COLOR_DEFAULT = "668CFF";
        final String OK_PRESSED_COLOR_DEFAULT = "0040FF";

        ColorBox colorBox = buttons[COMMANDS.OK.INDEX()].getColorBox();
        colorBox.setColor(BUTTON_COLOR_TYPES.UNPRESSED_BACK.INDEX(), OK_UNPRESSED_COLOR_DEFAULT);
        colorBox.setColor(BUTTON_COLOR_TYPES.PRESSED_BACK.INDEX(), OK_PRESSED_COLOR_DEFAULT);
        buttons[COMMANDS.OK.INDEX()].updateDisplay();
    }

    private void rebuildPresets() {
        if (listIndex != LIST_INDEX_DEFAULT_VALUE) {
            String presetId = presetsHandler.getPresetId(listIndex);
            presetsHandler.sortPresets();
            listIndex = presetsHandler.getIndex(presetId);
            preset = presetsHandler.getPreset(listIndex);
        } else {
            presetsHandler.sortPresets();
        }
        if (isTypeColors) {   //  Afficher aussi la roue de couleurs
            ListItemColorAdapter lvAdapter = new ListItemColorAdapter(this);
            lvAdapter.setColorItems(presetsHandler.getPresetDataList());
            lvAdapter.setTextItems(presetsHandler.getConcatenatedDisplayPresetDataList());
            listView.setAdapter(lvAdapter);
            lvAdapter = null;
        } else {   //  Sans roue de couleur
            ListItemSimpleAdapter lvAdapter = new ListItemSimpleAdapter(this);
            lvAdapter.setTextItems(presetsHandler.getConcatenatedDisplayPresetDataList());
            listView.setAdapter(lvAdapter);
            lvAdapter = null;
        }
        if (listIndex != LIST_INDEX_DEFAULT_VALUE) {   //  Simuler un click sur l'item à la position listIndex
            final long MINIMAL_DELAY_MILLIS = 10;

            final Runnable runnable = new Runnable() {
                boolean isFirstTime = true;

                public void run() {
                    if (listView.getChildCount() == 0) {   //  listView pas encore prêt
                        listView.postDelayed(this, MINIMAL_DELAY_MILLIS);   //  On attendra jusqu'à ce qu'il soit prêt
                    } else {   //  listView est maintenant prêt
                        if ((listIndex < listView.getFirstVisiblePosition()) || (listIndex > listView.getLastVisiblePosition())) {   //  L'item à la position listIndex est invisible
                            if (isFirstTime) {
                                isFirstTime = false;   //  Le setSelection ne doit être effectué qu'au 1er passage!
                                listView.setSelection(listIndex);   //  Pour voir l'item à la position listIndex (sans le sélectionner)
                            }
                            listView.postDelayed(this, MINIMAL_DELAY_MILLIS);   //  On attendra jusqu'à ce qu'il soit visible
                        } else {   //  L'item à la position listIndex est maintenant visible
                            View view = listView.getChildAt(listIndex - listView.getFirstVisiblePosition());
                            view.setSelected(true);   //  et apparaît alors comme sélectionné; Il n'y a plus de nécessité de relancer le runnable
                        }
                    }
                }
            };
            listView.postDelayed(runnable, MINIMAL_DELAY_MILLIS);   //  Lancer le runnable
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

        buttons = new ImageButtonView[COMMANDS.values().length];
        Class rid = R.id.class;
        for (COMMANDS command : COMMANDS.values()) {
            try {
                buttons[command.INDEX()] = findViewById(rid.getField(BUTTON_XML_PREFIX + command.toString()).getInt(rid));   //  BTN_... dans le XML
                buttons[command.INDEX()].setText(command.TEXT());
                buttons[command.INDEX()].setMinClickTimeInterval(BUTTON_MIN_CLICK_TIME_INTERVAL_MS);
                final COMMANDS fcommand = command;
                buttons[command.INDEX()].setOnCustomClickListener(new ImageButtonView.onCustomClickListener() {
                    @Override
                    public void onCustomClick() {
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
                onPresetClick(position, view);
            }
        });
    }

    private void setupStringDB() {
        stringDB = new StringDB(this);
        stringDB.open();
    }

    private void setupPresetsHandler() {
        presetsHandler = new PresetsHandler(stringDB);
        presetsHandler.setSeparator(getIntent().getStringExtra(PRESETS_ACTIVITY_EXTRA_KEYS.SEPARATOR.toString()));
        presetsHandler.setTableName(tableName);
    }

    private void launchInputButtonsActivity() {
        setCurrentForActivity(stringDB, PEKISLIB_ACTIVITIES.INPUT_BUTTONS.toString(), tableName, columnIndex, preset[columnIndex]);
        setStartStatusOfActivity(stringDB, PEKISLIB_ACTIVITIES.INPUT_BUTTONS.toString(), ACTIVITY_START_STATUS.COLD);
        Intent callingIntent = new Intent(this, InputButtonsActivity.class);
        callingIntent.putExtra(TABLE_EXTRA_KEYS.TABLE.toString(), tableName);
        callingIntent.putExtra(TABLE_EXTRA_KEYS.INDEX.toString(), columnIndex);
        callingIntent.putExtra(PEKISLIB_ACTIVITY_EXTRA_KEYS.TITLE.toString(), labelNames[columnIndex]);
        startActivityForResult(callingIntent, PEKISLIB_ACTIVITIES.INPUT_BUTTONS.INDEX());
    }

    private void launchHelpActivity() {
        Intent callingIntent = new Intent(this, HelpActivity.class);
        callingIntent.putExtra(PEKISLIB_ACTIVITY_EXTRA_KEYS.TITLE.toString(), HELP_ACTIVITY_TITLE);
        callingIntent.putExtra(HELP_ACTIVITY_EXTRA_KEYS.HTML_ID.toString(), R.raw.helppresetsactivity);
        startActivity(callingIntent);
    }

}