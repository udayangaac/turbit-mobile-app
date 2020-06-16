package com.manitaz.turbit.utill;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.manitaz.turbit.model.BankModel;
import com.manitaz.turbit.model.PreferenceModel;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "turbit.db";

    private static DatabaseHandler sInstance;
    private final String TABLE_CAT = "category";
    private final String CAT_ID = "id";
    private final String CAT_NAME = "category_name";
    private final String CAT_LOGO = "image";
    private final String CAT_IS_SELECTED = "is_selected";

    private String TABLE_BANKS = "banks";
    private String BANK_ID = "id";
    private String BANK_NAME = "name";
    private String BANK_LOGO = "logo_url";
    private String BANK_IS_SELECTED = "is_selected";

    private DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseHandler getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHandler(context.getApplicationContext());
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_CAT_TABLE = "CREATE TABLE IF NOT EXISTS "+ TABLE_CAT +" ( " +
                CAT_ID +  " int(11) NOT NULL , " +
                CAT_NAME + " varchar(255) NOT NULL , " +
                CAT_LOGO + " varchar(255) NOT NULL , " +
                CAT_IS_SELECTED+ " int(1) NOT NULL DEFAULT '0', " +
                "  PRIMARY KEY ("+ CAT_ID +") )";

        String CREATE_BANKS_TABLE = "CREATE TABLE IF NOT EXISTS "+ TABLE_BANKS +" ( " +
                BANK_ID +  " int(11) NOT NULL , " +
                BANK_NAME + " varchar(255) NOT NULL , " +
                BANK_LOGO + " varchar(255) NOT NULL , " +
                BANK_IS_SELECTED+ " int(1) NOT NULL DEFAULT '0', " +
                "  PRIMARY KEY ("+ BANK_ID +") )";

        sqLiteDatabase.execSQL(CREATE_CAT_TABLE);
        sqLiteDatabase.execSQL(CREATE_BANKS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_CAT );
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ TABLE_BANKS );

        onCreate(sqLiteDatabase);
    }

    public void addPreferenceDetails(List<PreferenceModel> preferenceModels) {
        for (PreferenceModel model : preferenceModels) {
            SQLiteDatabase db = getWritableDatabase();
            db.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(CAT_ID, model.getId());
            values.put(CAT_NAME, model.getCategory_name());
            values.put(CAT_LOGO, model.getImage());
            values.put(CAT_IS_SELECTED, model.getIs_selected());
            int result = (int) db.insertWithOnConflict(TABLE_CAT, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            //db.insertOrThrow(TABLE_BANKS, null, values);
            try {
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }

    public List<PreferenceModel> getAllPreference() {
        List<PreferenceModel> preferenceList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CAT;  //  WHERE is_active = 1
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    PreferenceModel preferenceModel = new PreferenceModel();
                    preferenceModel.setId(cursor.getInt(cursor.getColumnIndex(CAT_ID)));
                    preferenceModel.setCategory_name(cursor.getString(cursor.getColumnIndex(CAT_NAME)));
                    preferenceModel.setImage(cursor.getString(cursor.getColumnIndex(CAT_LOGO)));
                    preferenceModel.setIs_selected(cursor.getInt(cursor.getColumnIndex(CAT_IS_SELECTED)));
                    preferenceList.add(preferenceModel);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d("manitaz", "Error while trying to get cards from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return preferenceList;
    }

    public void changeSelectedStatusPreference(List<PreferenceModel> preferenceModels) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (PreferenceModel preferenceModel : preferenceModels) {
                values.put(CAT_IS_SELECTED, preferenceModel.getIs_selected());
                db.update(TABLE_CAT, values, CAT_ID + " = ? ", new String[]{String.valueOf(preferenceModel.getId())});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void addBanksDetails(List<BankModel> bankItems) {
        Log.d("manitaz", "insert bank "+ bankItems.size());
        for (BankModel bank : bankItems) {
            SQLiteDatabase db = getWritableDatabase();
            db.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(BANK_ID, bank.getId());
            values.put(BANK_NAME, bank.getBank_name());
            values.put(BANK_LOGO, bank.getImage());
            values.put(BANK_IS_SELECTED, bank.getIs_selected());
            int result = (int) db.insertWithOnConflict(TABLE_BANKS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            //db.insertOrThrow(TABLE_BANKS, null, values);
            Log.d("manitaz", "insert bank return "+ result);
            try {
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }

    public List<BankModel> getAllBanks() {
        List<BankModel> banks = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_BANKS;  //  WHERE is_active = 1
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    BankModel bankItem = new BankModel();
                    bankItem.setId(cursor.getInt(cursor.getColumnIndex(BANK_ID)));
                    bankItem.setBank_name(cursor.getString(cursor.getColumnIndex(BANK_NAME)));
                    bankItem.setImage(cursor.getString(cursor.getColumnIndex(BANK_LOGO)));
                    bankItem.setIs_selected(cursor.getInt(cursor.getColumnIndex(BANK_IS_SELECTED)));
                    banks.add(bankItem);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d("manitaz", "Error while trying to get cards from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        Log.d("manitaz", "Active banks count : " + banks.size());
        return banks;
    }

    public void changeSelectedStatusBanks(List<BankModel> banks) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (BankModel bank : banks) {
                values.put(BANK_IS_SELECTED, bank.getIs_selected());
                db.update(TABLE_BANKS, values, BANK_ID + " = ? ", new String[]{String.valueOf(bank.getId())});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }


}
