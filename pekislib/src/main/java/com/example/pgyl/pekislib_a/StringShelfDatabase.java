package com.example.pgyl.pekislib_a;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Arrays;

//  Les champs de toutes les tables sont toujours _id, ID, DATA1, DATA2, DATA3, ... selon le nombre de champs spécifié lors du createTable
//  Le champ _id est la véritable clé primaire, mais invisible pour l'utilisateur
//  Le champ ID est la clé primaire apparente pour l'utilisateur (cf contrainte UNIQUE sur le champ ID au createTable)
public class StringShelfDatabase extends SQLiteOpenHelper {

    //region Constantes
    public static final int TABLE_ID_INDEX = FIELDS.ID.USER_INDEX();
    public static final int TABLE_DATA_INDEX = FIELDS.DATA.USER_INDEX();
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "ssDB";

    private enum FIELDS {
        _id(0), ID(1), DATA(2);

        private int valueIndex;

        FIELDS(int valueIndex) {
            this.valueIndex = valueIndex;
        }

        public int INDEX() {
            return valueIndex;
        }

        public int USER_INDEX() {
            return INDEX() - 1;
        }    //  Index apparent pour l'utilisateur
    }

    private final String NULL_STRING = "NULL@SSDB";   //  Chaîne stockée en cas de champ null
    //endregion
    //region Variables
    SQLiteDatabase ssdb;
    //endregion

    public StringShelfDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {  //  Tables créées uniquement après appel de createTable
        //  NOP
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        //  NOP
    }

    public void open() {
        ssdb = getWritableDatabase();
    }

    public void close() {
        ssdb.close();
        ssdb = null;
    }

    public String getFieldName(int fieldIndex) {
        String ret;

        if (fieldIndex == FIELDS.ID.USER_INDEX()) {
            ret = FIELDS.ID.toString();
        } else {
            ret = FIELDS.DATA.toString() + fieldIndex;
        }
        return ret;
    }

    public boolean tableExists(String tableName) {
        final int COUNT_INDEX = 0;
        Cursor cursor;
        boolean ret;

        cursor = ssdb.rawQuery(sqlForTableExists(tableName), null);
        cursor.moveToFirst();    //  cursor jamais null après rawQuery de sqlForTableExists
        ret = (cursor.getInt(COUNT_INDEX) > 0);
        cursor.close();
        cursor = null;
        return ret;
    }

    public void createTable(String tableName, int tableFieldsCount) {
        ssdb.execSQL(sqlForCreateTable(tableName, tableFieldsCount));
    }

    public String[][] selectRows(String tableName, String whereCondition) {
        String[][] ret = null;
        Cursor cursor = ssdb.rawQuery(sqlForSelectUserRows(tableName, whereCondition), null);
        if (cursor != null) {
            int rowCount = cursor.getCount();
            if (rowCount > 0) {
                cursor.moveToFirst();
                int columnCount = cursor.getColumnCount();
                ret = new String[rowCount][columnCount];
                for (int i = 0; i <= (rowCount - 1); i = i + 1) {
                    for (int j = 0; j <= (columnCount - 1); j = j + 1) {
                        String fieldValue = cursor.getString(j);
                        if (fieldValue.equals(NULL_STRING)) {
                            fieldValue = null;
                        }
                        ret[i][j] = fieldValue;
                    }
                    cursor.moveToNext();
                }
            }
            cursor.close();
            cursor = null;
        }
        return ret;
    }

    public String[] selectRowByIdOrCreate(String tableName, String idValue) {
        String[] ret;

        String[][] stsa = selectRows(tableName, FIELDS.ID.toString() + " = '" + idValue + "'");
        if (stsa != null) {
            ret = stsa[0];       //  Prendre le 1er (et unique) record (cf contrainte UNIQUE sur le champ ID)
        } else {      //  IdValue inconnu dans la table => Enregistrer un record vide dans la table, avec ce IdValue
            ret = new String[getTableFieldsCount(tableName) - 1];   //  Champ _id non compté
            ret[FIELDS.ID.USER_INDEX()] = idValue;
            insertOrReplaceRow(tableName, ret);
        }
        return ret;
    }

    public String selectFieldByIdOrCreate(String tableName, String idValue, int fieldIndex) {
        return selectRowByIdOrCreate(tableName, idValue)[fieldIndex];
    }

    public void insertOrReplaceRows(String tableName, String[][] rows) {
        if (rows != null) {
            for (int i = 0; i <= (rows.length - 1); i = i + 1) {
                insertOrReplaceRow(tableName, rows[i]);
            }
        }
    }

    public void insertOrReplaceRow(String tableName, String[] row) {
        ssdb.execSQL(sqlForInsertOrReplaceUserRow(tableName, row));
    }

    public void insertOrReplaceRowById(String tableName, String idValue, String[] row) {
        String[] sts = Arrays.copyOf(row, row.length);
        sts[FIELDS.ID.USER_INDEX()] = idValue;
        insertOrReplaceRow(tableName, sts);
    }

    public void insertOrReplaceFieldById(String tableName, String idValue, int fieldIndex, String value) {
        String[] sts = selectRowByIdOrCreate(tableName, idValue);
        sts[fieldIndex] = value;
        insertOrReplaceRow(tableName, sts);
    }

    public void deleteRows(String tableName, String whereCondition) {
        ssdb.execSQL(sqlForDeleteRows(tableName, whereCondition));
    }

    private int getTableFieldsCount(String tableName) {
        Cursor cursor = ssdb.rawQuery(sqlForGetTableFieldsCount(tableName), null);
        int ret = cursor.getColumnNames().length;    //  cursor jamais null après rawQuery de sqlForGetTableFieldsCount
        cursor.close();
        cursor = null;
        return ret;
    }

    private String sqlForCreateTable(String tableName, int tableUserFieldsCount) {
        String fieldNamesAndTypes = FIELDS._id.toString() + " INTEGER PRIMARY KEY, ";   //  Champ _id
        fieldNamesAndTypes = fieldNamesAndTypes + FIELDS.ID.toString() + " TEXT NOT NULL UNIQUE, ";   //  Champ ID
        for (int j = 1; j <= (tableUserFieldsCount - 1); j = j + 1) {
            fieldNamesAndTypes = fieldNamesAndTypes + FIELDS.DATA.toString() + j + " TEXT";    //  Champs DATA1, DATA2, DATA3, ...
            if (j < (tableUserFieldsCount - 1)) {
                fieldNamesAndTypes = fieldNamesAndTypes + ", ";
            }
        }
        return "CREATE TABLE " + tableName + " ( " + fieldNamesAndTypes + " )";
    }

    private String sqlForSelectUserRows(String tableName, String whereCondition) {
        int tableUserFieldsCount = getTableFieldsCount(tableName) - 1;   //  Champ _id non compté
        String fieldNames = FIELDS.ID.toString() + ", ";
        for (int j = 1; j <= (tableUserFieldsCount - 1); j = j + 1) {
            fieldNames = fieldNames + FIELDS.DATA.toString() + j;
            if (j < (tableUserFieldsCount - 1)) {
                fieldNames = fieldNames + ", ";
            }
        }
        String ret = "SELECT " + fieldNames + " FROM " + tableName;
        if (whereCondition != null) {
            ret = ret + " WHERE " + whereCondition;
        }
        return ret;
    }

    private String sqlForInsertOrReplaceUserRow(String tableName, String[] userRow) {   //  Fonctionne grâce à la contrainte UNIQUE sur le champ ID
        String fieldNames = "";
        String fieldValues = "";
        int d = 0;
        for (int j = 0; j <= (userRow.length - 1); j = j + 1) {
            String fieldName = "";
            String fieldValue = userRow[j];
            if (fieldValue == null) {
                fieldValue = NULL_STRING;
            }
            if (j == FIELDS.ID.USER_INDEX()) {
                fieldName = FIELDS.ID.toString();
            } else {
                d = d + 1;
                fieldName = FIELDS.DATA.toString() + d;
            }
            fieldNames = fieldNames + fieldName;
            fieldValues = fieldValues + "'" + fieldValue + "'";
            if (j < (userRow.length - 1)) {
                fieldNames = fieldNames + ", ";
                fieldValues = fieldValues + ", ";
            }
        }
        return "INSERT OR REPLACE INTO " + tableName + " ( " + fieldNames + " )" + " VALUES (" + fieldValues + ")";
    }

    private String sqlForDeleteRows(String tableName, String whereCondition) {
        String ret = "DELETE FROM " + tableName;
        if (whereCondition != null) {
            ret = ret + " WHERE " + whereCondition;
        }
        return ret;
    }

    private String sqlForTableExists(String tableName) {
        return "SELECT COUNT(*) FROM sqlite_master WHERE ((name = '" + tableName + "') AND (type = 'table'))";   //  cursor jamais null après le rawQuery de ce SQL
    }

    private String sqlForGetTableFieldsCount(String tableName) {
        return "SELECT * FROM " + tableName + " LIMIT 0";    //  cursor jamais null après le rawQuery de ce SQL
    }

}
