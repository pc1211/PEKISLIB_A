package com.example.pgyl.pekislib_a;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import static com.example.pgyl.pekislib_a.Constants.NOT_FOUND;
import static com.example.pgyl.pekislib_a.Constants.TABLE_IDS;
import static com.example.pgyl.pekislib_a.StringShelfDatabase.TABLE_DATA_INDEX;
import static com.example.pgyl.pekislib_a.StringShelfDatabase.TABLE_ID_INDEX;
import static com.example.pgyl.pekislib_a.TimeDateUtils.TIMEUNITS;
import static com.example.pgyl.pekislib_a.TimeDateUtils.convertMsToHms;

public class PresetsHandler {
    //region Variables
    private StringShelfDatabase stringShelfDatabase;
    private String tableName;
    private ArrayList<String[]> presets;
    private String[] keyboards;
    private String[] timeUnits;
    private String separator;
    //endregion

    public PresetsHandler(StringShelfDatabase stringShelfDatabase) {
        this.stringShelfDatabase = stringShelfDatabase;
        init();
    }

    private void init() {
        //   NOP
    }

    public void saveAndClose() {
        savePresets();
        stringShelfDatabase = null;
        presets.clear();
        presets = null;
    }

    public void createNewPreset(String[] preset) {
        String[] sa = Arrays.copyOf(preset, preset.length);
        sa[TABLE_ID_INDEX] = TABLE_IDS.PRESET.toString() + (getMaxId() + 1);   // => PRESET1, PRESET2, PRESET3, ...
        presets.add(sa);
    }

    public void removePreset(int index) {
        presets.remove(index);
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
        presets = convertPresetRowsToPresets(getPresetRows());
        keyboards = getKeyboards();
        timeUnits = getTimeUnits();
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public String[] getPreset(int index) {
        return presets.get(index);
    }

    public int getCount() {
        int ret = 0;
        if (!presets.isEmpty()) {
            ret = presets.size();
        }
        return ret;
    }

    public String getPresetId(int index) {
        return presets.get(index)[TABLE_ID_INDEX];
    }

    public int getIndex(String PresetId) {
        int ret = NOT_FOUND;
        if (!presets.isEmpty()) {
            for (int i = 0; i <= (presets.size() - 1); i = i + 1) {
                if (PresetId.equals(getPresetId(i))) {     //  Found !!
                    ret = i;
                    break;
                }
            }
        }
        return ret;
    }

    public void setPresetColumn(int index, int columnIndex, String value) {
        presets.get(index)[columnIndex] = value;
    }

    public void sortPresets() {
        if (presets.size() >= 2) {
            Collections.sort(presets, new Comparator<String[]>() {
                public int compare(String[] sa1, String[] sa2) {
                    return concatenatedDisplayPresetData(sa1).compareTo(concatenatedDisplayPresetData(sa2));
                }
            });
        }
    }

    public ArrayList<String[]> presetDataList() {
        ArrayList<String[]> ret = new ArrayList<String[]>();
        if (!presets.isEmpty()) {
            for (int i = 0; i <= (presets.size() - 1); i = i + 1) {
                ret.add(Arrays.copyOfRange(presets.get(i), TABLE_DATA_INDEX, presets.get(i).length));   //  Exclure le champ ID);
            }
        }
        return ret;
    }

    public String[] concatenatedDisplayPresetDataList() {
        String[] ret = new String[0];
        if (!presets.isEmpty()) {
            ret = new String[presets.size()];
            for (int i = 0; i <= (presets.size() - 1); i = i + 1) {
                ret[i] = concatenatedDisplayPresetData(presets.get(i));
            }
        }
        return ret;
    }

    private String concatenatedDisplayPresetData(String[] preset) {
        String ret = "";
        for (int j = TABLE_DATA_INDEX; j <= (preset.length - 1); j = j + 1) {   //  Exclure le champ ID
            String s = preset[j];
            if ((keyboards[j].equals(InputButtonsActivity.KEYBOARDS.TIME_HMS.toString())) || (keyboards[j].equals(InputButtonsActivity.KEYBOARDS.TIME_XHMS.toString()))) {
                s = convertMsToHms(Long.parseLong(s), TIMEUNITS.valueOf(timeUnits[j]));
            }
            ret = ret + s;
            if (j < (preset.length - 1)) {
                ret = ret + separator;
            }
        }
        return ret;
    }

    private int getMaxId() {
        int ret = 0;
        if (!presets.isEmpty()) {
            for (int i = 0; i <= (presets.size() - 1); i = i + 1) {
                int k = Integer.valueOf(presets.get(i)[TABLE_ID_INDEX].substring(TABLE_IDS.PRESET.toString().length()));  // N° de PRESET
                if (k > ret) {
                    ret = k;
                }
            }
        }
        return ret;
    }

    private ArrayList<String[]> convertPresetRowsToPresets(String[][] presetRows) {
        ArrayList<String[]> ret = new ArrayList<String[]>();
        if (presetRows != null) {
            for (int i = 0; i <= (presetRows.length - 1); i = i + 1) {
                ret.add(presetRows[i]);
            }
        }
        return ret;
    }

    private String[][] convertPresetsToPresetRows(ArrayList<String[]> presets) {
        String[][] ret = null;
        if (!presets.isEmpty()) {
            ret = presets.toArray(new String[presets.size()][presets.get(0).length]);
        }
        return ret;
    }

    private String[] getKeyboards() {
        return stringShelfDatabase.selectRowByIdOrCreate(tableName, TABLE_IDS.KEYBOARD.toString());
    }

    private String[] getTimeUnits() {
        return stringShelfDatabase.selectRowByIdOrCreate(tableName, TABLE_IDS.TIMEUNIT.toString());
    }

    private String whereConditionForPresets() {
        return stringShelfDatabase.getFieldName(0) + " LIKE '" + TABLE_IDS.PRESET.toString() + "%'";
    }

    private void savePresets() {
        stringShelfDatabase.deleteRows(tableName, whereConditionForPresets());
        stringShelfDatabase.insertOrReplaceRows(tableName, convertPresetsToPresetRows(presets));
    }

    private String[][] getPresetRows() {
        return stringShelfDatabase.selectRows(tableName, whereConditionForPresets());
    }

}
