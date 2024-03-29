package com.example.pgyl.pekislib_a;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Arrays;

//  Les champs de toutes les tables sont toujours _id, ID, DATA1, DATA2, DATA3, ... selon le nombre de champs spécifié lors du createTableIfNotExists
//  Le champ _id est la véritable clé primaire, mais invisible pour l'utilisateur
//  Le champ ID est la clé primaire apparente pour l'utilisateur (cf contrainte UNIQUE sur le champ ID au createTableIfNotExists)
public class StringDB extends SQLiteOpenHelper {

    //region Constantes
    public static final int TABLE_ID_INDEX = FIELDS.ID.USER_INDEX();
    public static final int TABLE_DATA_INDEX = FIELDS.DATA.USER_INDEX();
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "SSDB";

    private enum FIELDS {
        _id, ID, DATA;

        public int INDEX() {
            return ordinal();
        }

        public int USER_INDEX() {
            return INDEX() - 1;
        }    //  Index apparent pour l'utilisateur
    }

    private final String NULL_STRING = "@NULL@";   //  Chaîne stockée en cas de champ null
    //endregion

    //region Variables
    SQLiteDatabase ssdb;
    //endregion

    public StringDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {  //  Tables créées uniquement après appel de createTableIfNotExists
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

    public static String getFieldName(int fieldIndex) {
        return ((fieldIndex == FIELDS.ID.USER_INDEX()) ? FIELDS.ID.toString() : FIELDS.DATA.toString() + fieldIndex);
    }

    public static String getIDPatternWhereCondition(String pattern) {   //    "<String>%" ou "%<String>" ou ...
        return FIELDS.ID.toString() + " LIKE '" + pattern + "'";
    }

    public boolean tableExists(String tableName) {
        final int COUNT_INDEX = 0;
        Cursor cursor;
        boolean tableExists;

        cursor = ssdb.rawQuery(sqlForTableExists(tableName), null);
        cursor.moveToFirst();    //  cursor jamais null après rawQuery de sqlForTableExists
        tableExists = (cursor.getInt(COUNT_INDEX) > 0);
        cursor.close();
        cursor = null;
        return tableExists;
    }

    public void createTableIfNotExists(String tableName, int tableFieldsCount) {
        ssdb.execSQL(sqlForCreateTableIfNotExists(tableName, tableFieldsCount));
    }

    public String[][] selectRows(String tableName, String whereCondition) {
        String[][] rows = null;
        Cursor cursor = ssdb.rawQuery(sqlForSelectUserRows(tableName, whereCondition), null);
        if (cursor != null) {
            int rowCount = cursor.getCount();
            if (rowCount > 0) {
                cursor.moveToFirst();
                int columnCount = cursor.getColumnCount();
                rows = new String[rowCount][columnCount];
                for (int i = 0; i <= (rowCount - 1); i = i + 1) {
                    for (int j = 0; j <= (columnCount - 1); j = j + 1) {
                        String fieldValue = cursor.getString(j);
                        rows[i][j] = ((fieldValue.equals(NULL_STRING)) ? null : fieldValue);
                    }
                    cursor.moveToNext();
                }
            }
            cursor.close();
            cursor = null;
        }
        return rows;
    }

    public String[] selectRowById(String tableName, String idValue) {
        String[][] rows = selectRows(tableName, FIELDS.ID.toString() + " = '" + idValue + "'");
        return ((rows != null) ? rows[0] : null);    //  Prendre le 1er (et unique) record (cf contrainte UNIQUE sur le champ ID);
    }

    public String[] selectRowByIdOrCreate(String tableName, String idValue) {
        String[] row = selectRowById(tableName, idValue);
        if (row == null) {   //  IdValue inconnu dans la table => Enregistrer un record vide dans la table, avec ce IdValue
            row = new String[getTableFieldsCount(tableName) - 1];   //  Champ _id non compté
            row[FIELDS.ID.USER_INDEX()] = idValue;
            insertOrReplaceRow(tableName, row);
        }
        return row;
    }

    public String selectFieldById(String tableName, String idValue, int fieldIndex) {
        String[] row = selectRowById(tableName, idValue);
        return ((row != null) ? row[fieldIndex] : null);
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
        String[] rowCopy = Arrays.copyOf(row, row.length);
        rowCopy[FIELDS.ID.USER_INDEX()] = idValue;
        insertOrReplaceRow(tableName, rowCopy);
    }

    public void insertOrReplaceFieldById(String tableName, String idValue, int fieldIndex, String value) {
        String[] row = selectRowByIdOrCreate(tableName, idValue);
        row[fieldIndex] = value;
        insertOrReplaceRow(tableName, row);
    }

    public void deleteRows(String tableName, String whereCondition) {
        ssdb.execSQL(sqlForDeleteRows(tableName, whereCondition));
    }

    public void deleteTableIfExists(String tablename) {
        if (tableExists(tablename)) {
            ssdb.execSQL(sqlForDeleteTable(tablename));
        }
    }

    private int getTableFieldsCount(String tableName) {
        Cursor cursor = ssdb.rawQuery(sqlForGetTableFieldsCount(tableName), null);
        int tableFieldsCount = cursor.getColumnNames().length;    //  cursor jamais null après rawQuery de sqlForGetTableFieldsCount
        cursor.close();
        cursor = null;
        return tableFieldsCount;
    }

    private String sqlForCreateTableIfNotExists(String tableName, int tableUserFieldsCount) {
        String fieldNamesAndTypes = FIELDS._id.toString() + " INTEGER PRIMARY KEY, ";   //  Champ _id
        fieldNamesAndTypes = fieldNamesAndTypes + FIELDS.ID.toString() + " TEXT NOT NULL UNIQUE, ";   //  Champ ID
        for (int j = 1; j <= (tableUserFieldsCount - 1); j = j + 1) {
            fieldNamesAndTypes = fieldNamesAndTypes + FIELDS.DATA.toString() + j + " TEXT";    //  Champs DATA1, DATA2, DATA3, ...
            if (j < (tableUserFieldsCount - 1)) {
                fieldNamesAndTypes = fieldNamesAndTypes + ", ";
            }
        }
        return "CREATE TABLE IF NOT EXISTS " + tableName + " ( " + fieldNamesAndTypes + " )";
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
        String sql = "SELECT " + fieldNames + " FROM " + tableName;
        if (whereCondition != null) {
            sql = sql + " WHERE " + whereCondition;
        }
        return sql;
    }

    private String sqlForInsertOrReplaceUserRow(String tableName, String[] userRow) {   //  Fonctionne grâce à la contrainte UNIQUE sur le champ ID
        String fieldNames = "";
        String fieldValues = "";
        int dataFieldCount = 0;
        for (int j = 0; j <= (userRow.length - 1); j = j + 1) {
            fieldNames = fieldNames + ((j == FIELDS.ID.USER_INDEX()) ? FIELDS.ID.toString() : FIELDS.DATA.toString() + String.valueOf(++dataFieldCount));
            fieldValues = fieldValues + "'" + ((userRow[j] != null) ? userRow[j].replace("'", "''") : NULL_STRING) + "'";   //  Escaper les single quotes
            if (j < (userRow.length - 1)) {
                fieldNames = fieldNames + ", ";
                fieldValues = fieldValues + ", ";
            }
        }
        return "INSERT OR REPLACE INTO " + tableName + " ( " + fieldNames + " )" + " VALUES (" + fieldValues + ")";
    }

    private String sqlForDeleteRows(String tableName, String whereCondition) {
        String sql = "DELETE FROM " + tableName;
        if (whereCondition != null) {
            sql = sql + " WHERE " + whereCondition;
        }
        return sql;
    }

    private String sqlForDeleteTable(String tableName) {
        return "DROP TABLE " + tableName;
    }

    private String sqlForTableExists(String tableName) {
        return "SELECT COUNT(*) FROM sqlite_master WHERE ((name = '" + tableName + "') AND (type = 'table'))";   //  cursor jamais null après le rawQuery de ce SQL
    }

    private String sqlForGetTableFieldsCount(String tableName) {
        return "SELECT * FROM " + tableName + " LIMIT 0";    //  cursor jamais null après le rawQuery de ce SQL
    }

}
