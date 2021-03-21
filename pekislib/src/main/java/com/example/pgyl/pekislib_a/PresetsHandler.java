package com.example.pgyl.pekislib_a;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import static com.example.pgyl.pekislib_a.Constants.NOT_FOUND;
import static com.example.pgyl.pekislib_a.InputButtonsActivity.KEYBOARDS;
import static com.example.pgyl.pekislib_a.StringDB.TABLE_DATA_INDEX;
import static com.example.pgyl.pekislib_a.StringDB.TABLE_ID_INDEX;
import static com.example.pgyl.pekislib_a.StringDBTables.TABLE_IDS;
import static com.example.pgyl.pekislib_a.StringDBUtils.getKeyboards;
import static com.example.pgyl.pekislib_a.StringDBUtils.getTimeUnitPrecisions;
import static com.example.pgyl.pekislib_a.TimeDateUtils.TIME_UNITS;
import static com.example.pgyl.pekislib_a.TimeDateUtils.msToTimeFormatD;

public class PresetsHandler {
    //region Variables
    private StringDB stringDB;
    private String tableName;
    private ArrayList<String[]> presets;
    private String[] keyboards;
    private String[] timeUnitPrecisions;
    private String separator;
    //endregion

    public PresetsHandler(StringDB stringDB) {
        this.stringDB = stringDB;
        init();
    }

    private void init() {
        //   NOP
    }

    public void saveAndClose() {
        savePresets();
        stringDB = null;
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
        presets = presetRowsToPresets(getPresetRows());
        keyboards = getKeyboards(stringDB, tableName);
        timeUnitPrecisions = getTimeUnitPrecisions(stringDB, tableName);
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public String[] getPreset(int index) {
        return presets.get(index);
    }

    public int getCount() {
        int count = 0;
        if (!presets.isEmpty()) {
            count = presets.size();
        }
        return count;
    }

    public String getPresetId(int index) {
        return presets.get(index)[TABLE_ID_INDEX];
    }

    public int getIndex(String PresetId) {
        int index = NOT_FOUND;
        if (!presets.isEmpty()) {
            for (int i = 0; i <= (presets.size() - 1); i = i + 1) {
                if (PresetId.equals(getPresetId(i))) {     //  Found !!
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    public void setPresetColumn(int index, int columnIndex, String value) {
        presets.get(index)[columnIndex] = value;
    }

    public void sortPresets() {
        if (presets.size() >= 2) {
            Collections.sort(presets, new Comparator<String[]>() {
                public int compare(String[] sa1, String[] sa2) {
                    return getConcatenatedDisplayPresetData(sa1).compareTo(getConcatenatedDisplayPresetData(sa2));
                }
            });
        }
    }

    public ArrayList<String[]> getPresetDataList() {
        ArrayList<String[]> presetDataList = new ArrayList<String[]>();
        if (!presets.isEmpty()) {
            for (int i = 0; i <= (presets.size() - 1); i = i + 1) {
                presetDataList.add(Arrays.copyOfRange(presets.get(i), TABLE_DATA_INDEX, presets.get(i).length));   //  Exclure le champ ID);
            }
        }
        return presetDataList;
    }

    public String[] getConcatenatedDisplayPresetDataList() {
        String[] concatenatedDisplayPresetDataList = new String[0];
        if (!presets.isEmpty()) {
            concatenatedDisplayPresetDataList = new String[presets.size()];
            for (int i = 0; i <= (presets.size() - 1); i = i + 1) {
                concatenatedDisplayPresetDataList[i] = getConcatenatedDisplayPresetData(presets.get(i));
            }
        }
        return concatenatedDisplayPresetDataList;
    }

    private String getConcatenatedDisplayPresetData(String[] preset) {
        String concatenatedDisplayPresetData = "";
        for (int j = TABLE_DATA_INDEX; j <= (preset.length - 1); j = j + 1) {   //  Exclure le champ ID
            String s = preset[j];
            if ((keyboards[j].equals(KEYBOARDS.TIME_FORMAT_D.toString())) || (keyboards[j].equals(KEYBOARDS.TIME_FORMAT_DL.toString()))) {
                s = msToTimeFormatD(Long.parseLong(s), TIME_UNITS.valueOf(timeUnitPrecisions[j]), TIME_UNITS.valueOf(timeUnitPrecisions[j]));
            }
            concatenatedDisplayPresetData = concatenatedDisplayPresetData + s;
            if (j < (preset.length - 1)) {
                concatenatedDisplayPresetData = concatenatedDisplayPresetData + separator;
            }
        }
        return concatenatedDisplayPresetData;
    }

    private int getMaxId() {
        int maxId = 0;
        if (!presets.isEmpty()) {
            for (int i = 0; i <= (presets.size() - 1); i = i + 1) {
                int k = Integer.valueOf(presets.get(i)[TABLE_ID_INDEX].substring(TABLE_IDS.PRESET.toString().length()));  // N° de PRESET
                if (k > maxId) {
                    maxId = k;
                }
            }
        }
        return maxId;
    }

    private ArrayList<String[]> presetRowsToPresets(String[][] presetRows) {
        ArrayList<String[]> presets = new ArrayList<String[]>();
        if (presetRows != null) {
            for (int i = 0; i <= (presetRows.length - 1); i = i + 1) {
                presets.add(presetRows[i]);
            }
        }
        return presets;
    }

    private String[][] presetsToPresetRows(ArrayList<String[]> presets) {
        String[][] presetRows = null;
        if (!presets.isEmpty()) {
            presetRows = presets.toArray(new String[presets.size()][presets.get(0).length]);
        }
        return presetRows;
    }

    private String whereConditionForPresets() {
        return stringDB.getFieldName(TABLE_ID_INDEX) + " LIKE '" + TABLE_IDS.PRESET.toString() + "%'";
    }

    private void savePresets() {
        stringDB.deleteRows(tableName, whereConditionForPresets());
        stringDB.insertOrReplaceRows(tableName, presetsToPresetRows(presets));
    }

    private String[][] getPresetRows() {
        return stringDB.selectRows(tableName, whereConditionForPresets());
    }

}
