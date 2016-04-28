package com.huiwu.temperaturecontrol.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.huiwu.temperaturecontrol.R;

/**
 * Created by HuiWu on 2015/11/27.
 */
public class TempSQLite extends SQLiteOpenHelper {
    public static final String DB_NAME = "temperature.db";
    private Context context;

    public TempSQLite(Context context, int version) {
        super(context, DB_NAME, null, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(context.getString(R.string.sql_box));

        db.execSQL(context.getString(R.string.sql_goods));

        db.execSQL(context.getString(R.string.sql_tmprfid));

        db.execSQL(context.getString(R.string.sql_rfidgood));

        db.execSQL(context.getString(R.string.sql_tagInfo));

        db.execSQL(context.getString(R.string.sql_picture));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(context.getString(R.string.sql_box));

        db.execSQL(context.getString(R.string.sql_goods));

        db.execSQL(context.getString(R.string.sql_tmprfid));

        db.execSQL(context.getString(R.string.sql_rfidgood));

        db.execSQL(context.getString(R.string.sql_tagInfo));

        db.execSQL(context.getString(R.string.sql_picture));
    }
}
